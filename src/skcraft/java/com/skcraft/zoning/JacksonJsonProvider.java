/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sk89q.worldedit.Vector;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and writes zones to disk using the Jackson JSON library.
 */
public class JacksonJsonProvider implements ZoneProvider {

    private final ObjectMapper mapper = new ObjectMapper();
    private final File file;
    private final Class<?> dataType;
    @Getter @Setter
    private boolean createData;

    /**
     * Create a new instance of a provider.
     *
     * @param file the file to store the zones at
     * @param dataType the type of the data class
     */
    public JacksonJsonProvider(@NonNull File file, @NonNull Class<?> dataType) {
        this.dataType = dataType;
        SimpleModule module = new SimpleModule("JacksonJsonProvider",
                new Version(1, 0, 0, "SNAPSHOT", "com.sk89q", "worldguard"));
        module.addSerializer(Vector.class, new VectorSerializer());
        module.addDeserializer(Vector.class, new VectorDeserializer());
        module.addDeserializer(Zone.class, new ZoneDeserializer());
        mapper.registerModule(module);
        this.file = file;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> int read(SpatialIndex<T> index) throws IOException {
        try {
            EntryList<T> data = mapper.readValue(file, EntryList.class);
            for (Zone<T> entry : data.getEntries()) {
                index.add(entry);
            }
            return data.getEntries().size();
        } catch (FileNotFoundException ignored) {
            return 0;
        }
    }

    @Override
    public <T> void write(List<Zone<T>> entries) throws IOException {
        EntryList<T> data = new EntryList<T>();
        data.setEntries(entries);
        mapper.writeValue(file, data);
    }

    @Data
    private static class EntryList<T> {
        private List<Zone<T>> entries = new ArrayList<>();
    }

    /**
     * Used to deserialize {@link com.skcraft.zoning.Zone}.
     */
    public class ZoneDeserializer extends JsonDeserializer<Zone<?>> {
        @Override
        public Zone<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode node = jp.readValueAsTree();
            Boundary boundary = mapper.convertValue(node.get("boundary"), Boundary.class);
            Object data = mapper.convertValue(node.get("data"), dataType);
            if (data == null && createData) {
                try {
                    data = dataType.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IOException(e);
                }
            }
            Zone<Object> zone = new Zone<Object>(boundary);
            zone.setData(data);
            return zone;
        }
    }

    /**
     * Used to deserialize {@link com.sk89q.worldedit.Vector}.
     */
    public class VectorDeserializer extends JsonDeserializer<Vector> {
        @Override
        public Vector deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (!jp.isExpectedStartArrayToken()) {
                throw new JsonMappingException("Expected array start, got " + jp.getCurrentToken());
            }
            jp.nextToken();
            double x = jp.getDoubleValue();
            jp.nextToken();
            double y = jp.getDoubleValue();
            jp.nextToken();
            double z = jp.getDoubleValue();
            jp.nextToken();
            if (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                throw new JsonMappingException("Expected array end, got " + jp.getCurrentToken());
            }
            return new Vector(x, y, z);
        }
    }

    /**
     * Used to serialize {@link com.sk89q.worldedit.Vector}.
     */
    public class VectorSerializer extends JsonSerializer<Vector> {
        @Override
        public void serialize(Vector value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartArray();
            jgen.writeNumber(value.getX());
            jgen.writeNumber(value.getY());
            jgen.writeNumber(value.getZ());
            jgen.writeEndArray();
        }
    }

}
