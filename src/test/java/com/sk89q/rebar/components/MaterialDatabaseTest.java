package com.sk89q.rebar.components;

import java.io.File;
import java.io.IOException;

import com.sk89q.rebar.config.Configuration;
import com.sk89q.rebar.config.PairedKeyValueLoaderBuilder;
import com.sk89q.rebar.config.types.LowercaseStringLoaderBuilder;
import com.sk89q.rebar.config.types.MaterialPatternLoaderBuilder;
import com.sk89q.rebar.util.MaterialPattern;

public class MaterialDatabaseTest {
    
    public static void main(String[] args) throws IOException {
        File file = new File("C:/Users/Albert/Desktop/materials.yml");
        
        Configuration config = new Configuration(file);
        config.load();
        
        PairedKeyValueLoaderBuilder<String, MaterialPattern> loader =
                PairedKeyValueLoaderBuilder.build(
                        new LowercaseStringLoaderBuilder(),
                        new MaterialPatternLoaderBuilder());
        System.out.println(config.mapOf("materials", loader));
    }

}
