package ru.customchunklimit;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class Main extends JavaPlugin implements Listener {
    private Map<Material, Integer> blockLimits;
    private boolean enable;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        // Load configuration
        loadConfiguration();

        // Register event listener
        getServer().getPluginManager().registerEvents(this, this);
    }
    private void loadConfiguration() {
        // Create default config file if it does not exist
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Load block limits from config
        FileConfiguration config = getConfig();
        blockLimits = new HashMap<>();

        for (String blockType : config.getConfigurationSection("block-limit").getKeys(false)) {
            Material material = Material.getMaterial(blockType.toUpperCase());

            if (material != null) {
                int limit = config.getInt("block-limit." + blockType);
                blockLimits.put(material, limit);
            }
        }
        enable = config.getBoolean("enable");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!enable) {
            return;
        }
        Block block = event.getBlock();
        Material material = block.getType();

        if (blockLimits.containsKey(material)) {
            // Check if the limit has been reached
            int limit = blockLimits.get(material);
            World world = block.getWorld();
            Chunk chunk = block.getChunk();
            ChunkSnapshot snapshot = chunk.getChunkSnapshot();

            int count = 0;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < world.getMaxHeight(); y++) {
                    for (int z = 0; z < 16; z++) {
                        if (snapshot.getBlockType(x, y, z) == material) {
                            count++;
                        }
                    }
                }
            }

            if (count >= limit) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have reached the limit of " + limit + " " + material.name() + " blocks per chunk."));
            }
        }
    }
}