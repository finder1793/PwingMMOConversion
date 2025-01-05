package com.pwing.mmoconversion;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import java.io.File;
import java.io.IOException;

public class ItemConverter {
    private final PwingMMOConversion plugin;
    private final StatConverter statConverter;

    public ItemConverter(PwingMMOConversion plugin, StatConverter statConverter) {
        this.plugin = plugin;
        this.statConverter = statConverter;
    }

    public int convertAllItems() {
        int convertedCount = 0;
        File mmoItemsFolder = new File(plugin.getServer().getPluginManager().getPlugin("MMOItems").getDataFolder(), "item");
        
        if (mmoItemsFolder.exists() && mmoItemsFolder.isDirectory()) {
            // Each subfolder represents an item type
            for (File typeFolder : mmoItemsFolder.listFiles()) {
                if (typeFolder.isDirectory()) {
                    String type = typeFolder.getName();
                    
                    // Process each YAML file in the type folder
                    for (File itemFile : typeFolder.listFiles()) {
                        if (itemFile.getName().endsWith(".yml")) {
                            YamlConfiguration itemConfig = YamlConfiguration.loadConfiguration(itemFile);
                            convertItem(type, itemFile.getName().replace(".yml", ""), itemConfig);
                            convertedCount++;
                        }
                    }
                }
            }
        }
        
        return convertedCount;
    }

    private void convertItem(String type, String id, YamlConfiguration config) {
        // Create MythicMobs item configuration
        YamlConfiguration mythicConfig = new YamlConfiguration();
        
        // Basic item properties
        mythicConfig.set("Id", id);
        mythicConfig.set("Material", config.getString("material"));
        mythicConfig.set("Display", config.getString("name"));
        
        // Convert stats and properties
        statConverter.convertConfigStats(config, mythicConfig);
        
        saveToCrucible(type, id, mythicConfig);
    }

    private void saveToCrucible(String type, String id, YamlConfiguration config) {
        // Get MythicMobs plugin folder and create converted directory
        File mythicFolder = plugin.getServer().getPluginManager().getPlugin("MythicMobs").getDataFolder();
        File convertedFolder = new File(mythicFolder, "items/converted/" + type.toLowerCase());
        convertedFolder.mkdirs();
    
        // Create item file
        File itemFile = new File(convertedFolder, id + ".yml");
    
        try {
            config.save(itemFile);
            plugin.getLogger().info("Converted item " + type + ":" + id + " saved to " + itemFile.getPath());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save converted item " + type + ":" + id);
            e.printStackTrace();
        }
    }
}