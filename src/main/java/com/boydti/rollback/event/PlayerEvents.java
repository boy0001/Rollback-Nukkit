package com.boydti.rollback.event;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.Plugin;
import com.boydti.fawe.FaweCache;
import com.boydti.fawe.config.BBC;
import com.boydti.fawe.object.FawePlayer;
import com.boydti.fawe.util.MainUtil;
import com.boydti.rollback.Rollback;
import com.boydti.rollback.config.Config;
import com.boydti.rollback.database.SQLDatabase;
import com.boydti.rollback.database.SimpleBlockChange;
import com.boydti.rollback.util.LogUser;
import java.util.List;

public class PlayerEvents implements Listener {

    private final Plugin plugin;

    public PlayerEvents(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        Item hand = event.getItem();
        if (hand == null || hand.getId() != Config.ITEM || !player.hasPermission("rollback.inspect")) {
            return;
        }
        Block block = event.getBlock();
        interact(player, block);
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        Item hand = event.getItem();
        if (hand == null || hand.getId() != Config.ITEM || !player.hasPermission("rollback.inspect")) {
            return;
        }
        Block block;
        switch (event.getAction()) {
            case LEFT_CLICK_AIR:
                block = player.getLevelBlock();
                if (block == null) {
                    return;
                }
                break;
            case RIGHT_CLICK_BLOCK:
                block = event.getBlock().getSide(event.getFace());
                break;
            default:
                return;
        }
        event.setCancelled(true);
        interact(player, block);
    }

    public void interact(final Player player, Block block) {
        final FawePlayer<Object> fp = FawePlayer.wrap(player);
        final Block fblock = block;
        final SQLDatabase database = Rollback.db().getDatabase(player.getLevel().getName());
        if (fp.getMeta("fawe_action") != null) {
            fp.sendMessage(Config.PREFIX + BBC.WORLDEDIT_COMMAND_LIMIT);
            return;
        }
        fp.setMeta("fawe_action", true);
        database.addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    List<SimpleBlockChange> changes = database.getChanges(fblock.getFloorX(), fblock.getFloorY(), fblock.getFloorZ());
                    if (changes.isEmpty()) {
                        fp.sendMessage(BBC.color(Config.PREFIX + "No changes."));
                    } else {
                    	player.sendMessage(BBC.color(Config.PREFIX + "===== (" + fblock.getFloorX() + ", " + fblock.getFloorY() + ", " + fblock.getFloorZ() + ") ====="));
                        for (SimpleBlockChange change : changes) {
                            String name = LogUser.getName(change.player);
                            String age = MainUtil.secToTime((System.currentTimeMillis() - change.timestamp) / 1000);
                            String from = Item.get(FaweCache.getId(change.combinedFrom), FaweCache.getData(change.combinedFrom)).getName();
                            String to = Item.get(FaweCache.getId(change.combinedTo), FaweCache.getData(change.combinedTo)).getName();
                            player.sendMessage(BBC.color(name + ": " + from + " -> " + to + " (" + age +")"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                fp.deleteMeta("fawe_action");
            }
        });
    }
}
