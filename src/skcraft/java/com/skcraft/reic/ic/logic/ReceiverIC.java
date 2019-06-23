/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic.ic.logic;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.skcraft.reic.AbstractIC;
import com.skcraft.reic.AbstractICFactory;
import com.skcraft.reic.CreatedOnChunkLoad;
import com.skcraft.reic.Family;
import com.skcraft.reic.IC;
import com.skcraft.reic.ICDocumentation;
import com.skcraft.reic.ICException;
import com.skcraft.reic.ReIC;
import com.skcraft.reic.State;

public class ReceiverIC extends AbstractIC {
    
    private String network;
    
    public ReceiverIC(Block block, State state, String network) {
        super(block, state);
        this.network = network;
        
        TransmitterIC.register(this);
    }

    public void trigger() {
    }

    public void receive(boolean val) {
        getState().out(0, val);
    }

    public void unload() {
        TransmitterIC.unregister(this);
    }
    
    public String getNetwork() {
        return network;
    }
    
    public String getSummary() {
        return "Will receive a signal on the '" + network + "' network.";
    }
    
    public String getDebugInfo() {
        return "Currently aware of " + TransmitterIC.getListenersSize(network)
                + " total listener(s) (including me) on network '" + network + "'";
    }

    public static class ReceiverICFactory extends AbstractICFactory implements CreatedOnChunkLoad {
        public IC create(ReIC reic, Family family, Block sign, String[] lines) throws ICException {
            String network = lines[1].trim();
            
            if (network.length() == 0) {
                throw new ICException("The second line should be a network name.");
            }
            
            expectNoArg(lines, 2);
            expectNoArg(lines, 3);
            
            return new ReceiverIC(sign, family.createState(sign), network);
        }

        public boolean canCreate(Player player) {
            return true;
        }

        public String getDescription() {
            return "Transmits a signal on the given network name.";
        }

        public ICDocumentation getDocumentation() {
            return new ICDocumentation()
                    .summary("Receives a signal on the given network name. Network names are case-sensitive. " +
                            "When a chunk with a receiver IC is loaded, the receiver will not try to sync.")
                    .param("Network name")
                    .output("Value transmitted");
        }
    }

}
