/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.skcraft;

import java.util.Arrays;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class TestWorldGenerator extends ChunkGenerator {
    private byte[] data;
    private boolean grid = false;

    public TestWorldGenerator(boolean grid) {
        this.grid = grid;
        data = new byte[16 * 16 * 128];

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (grid & (x == 0 || z == 0)) {
                    data[(x * 16 + z) * 128 + 63] = (byte) Material.STONE.getId();
                } else {
                    data[(x * 16 + z) * 128 + 63] = (byte) Material.GRASS.getId();
                }
                for (int y = 60; y < 63; y++) {
                    data[(x * 16 + z) * 128 + y] = (byte) Material.STONE.getId();
                }
            }
        }

        buildLightLayer(data, 59);
        buildLightLayer(data, 59 - 10 * 1);
        buildLightLayer(data, 59 - 10 * 2);
        buildLightLayer(data, 59 - 10 * 3);
        buildLightLayer(data, 1);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int y = 0;
                data[(x * 16 + z) * 128 + y] = (byte) Material.BEDROCK.getId();
            }
        }
    }

    /**
     * Shapes the chunk for the given coordinates.<br />
     * <br />
     * This method should return a byte[32768] in the following format:
     * <pre>
     * for (int x = 0; x < 16; x++) {
     *     for (int z = 0; z < 16; z++) {
     *         for (int y = 0; y < 128; y++) {
     *             // result[(x * 16 + z) * 128 + y] = ??;
     *         }
     *     }
     * }
     * </pre>
     *
     * Note that this method should <b>never</b> attempt to get the Chunk at
     * the passed coordinates, as doing so may cause an infinite loop
     *
     * @param world The world this chunk will be used for
     * @param random The random generator to use
     * @param chunkX The X-coordinate of the chunk
     * @param chunkZ The Z-coordinate of the chunk
     * @return byte[] containing the types for each block created by this generator
     */
    @Override
    public byte[] generate(World world, Random random, int chunkX, int chunkZ) {

        //logger.info("Generating chunk at " + chunkX + ", " + chunkZ);
        //return new byte[16 * 16 * 128];
        return Arrays.copyOf(data, data.length);
    }

    private void buildLightLayer(byte[] data, int y) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (x % 4 == 0 && z % 4 == 0) {
                    data[(x * 16 + z) * 128 + y] = (byte) Material.GLOWSTONE.getId();
                } else {
                    if (grid & (x == 0 || z == 0)) {
                        data[(x * 16 + z) * 128 + y] = (byte) Material.COBBLESTONE.getId();
                    } else {
                        data[(x * 16 + z) * 128 + y] = (byte) Material.STONE.getId();
                    }
                }
            }
        }
    }

    /**
     * Gets a fixed spawn location to use for a given world.
     *
     * A null value is returned if a world should not use a fixed spawn point,
     * and will instead attempt to find one randomly.
     *
     * @param world The world to locate a spawn point for
     * @param random Random generator to use in the calculation
     * @return Location containing a new spawn point, otherwise null
     */
    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, 65, 0);
    }

}
