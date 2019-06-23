/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventorySerializer {

    public static final int VERSION = 1;

    private InventorySerializer() {
    }
    
    public static void write(File file, Inventory inven) throws IOException {
        FileOutputStream out = null;
        
        try {
            out = new FileOutputStream(file);
            BufferedOutputStream buffered = new BufferedOutputStream(out);
            DataOutputStream dataOut = new DataOutputStream(buffered);
            write(dataOut, inven);
            dataOut.close();
        } finally {
            if (out != null)
                out.close();
        }
    }
    
    public static void write(DataOutputStream stream, Inventory inven) throws IOException {
        stream.writeInt(VERSION);
        
        ItemStack[] items = inven.getContents();
        
        int numValid = 0;
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null) continue;
            if (item.getTypeId() <= 0) continue;
            if (item.getAmount() == 0) continue;
            numValid++;
        }
        
        stream.writeInt(numValid);
        
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null) continue;
            if (item.getTypeId() <= 0) continue;
            if (item.getAmount() == 0) continue;
            
            Map<Enchantment, Integer> enchantments = item.getEnchantments();
            
            stream.writeInt(i);
            stream.writeInt(item.getTypeId());
            stream.writeInt(item.getDurability());
            stream.writeInt(item.getAmount());
            
            stream.writeInt(enchantments.size());
            
            for (Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
                stream.writeInt(enchantment.getKey().getId());
                stream.writeInt(enchantment.getValue());
            }
        }
    }
    
    public static void read(File file, Inventory inven) throws IOException {
        FileInputStream out = null;
        
        try {
            out = new FileInputStream(file);
            BufferedInputStream buffered = new BufferedInputStream(out);
            DataInputStream dataIn = new DataInputStream(buffered);
            read(dataIn, inven);
            dataIn.close();
        } finally {
            if (out != null)
                out.close();
        }
    }
    
    public static void read(DataInputStream stream, Inventory inven) throws IOException {
        int version = stream.readInt();
        
        if (version != 1) {
            throw new IOException("Inventory storage version is not 1");
        }
        
        int numItems = stream.readInt();
        
        for (int i = 0; i < numItems; i++) {
            int index = stream.readInt();
            int type = stream.readInt();
            int data = stream.readInt();
            int amount = stream.readInt();
            
            ItemStack item = new ItemStack(type, amount, (short) data);

            int numEnchantments = stream.readInt();
            
            for (int j = 0; j < numEnchantments; j++) {
                int id = stream.readInt();
                int level = stream.readInt();
                
                Enchantment enchantment = Enchantment.getById(id);
                
                if (enchantment != null) {
                    item.addUnsafeEnchantment(enchantment, level);
                }
            }
            
            inven.setItem(index, item);
        }
    }
    
}
