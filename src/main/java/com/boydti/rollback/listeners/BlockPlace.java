package com.boydti.rollback.listeners;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.player.PlayerBucketEmptyEvent;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.Plugin;
import com.boydti.fawe.FaweCache;
import com.boydti.rollback.LogAPI;
import com.boydti.rollback.config.Loggers;

public class BlockPlace extends BasicListener {
    
    public BlockPlace(Plugin plugin) {
        super(Loggers.BLOCK_PLACE, plugin);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        int combinedTo = FaweCache.getCombined(block.getId(), block.getDamage());
        LogAPI.getLogger(player.getLevel()).logPlace(player.getName(), block.getFloorX(), block.getFloorY(), block.getFloorZ(), (short) combinedTo, null);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) { //TODO: If a liquid is placed on any block other than the bottom block it is not logged. (or doesn't show up in inspection?)
        Player player = event.getPlayer();
        Block block = event.getBlockClicked().getSide(event.getBlockFace());
        Item bucket = event.getBucket();
        int combinedTo = FaweCache.getCombined(bucket.getId(), bucket.getDamage());
        LogAPI.getLogger(player.getLevel()).logBlock(player.getName(), block.getFloorX(), block.getFloorY(), block.getFloorZ(), (short) 0, (short) combinedTo);
    }
}
