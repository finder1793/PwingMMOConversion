package com.pwing.mmoconversion;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ItemConverter {
    private final PwingMMOConversion plugin;
    private final SetConverter setConverter;
    private final File mmoItemsFolder;

    public ItemConverter(PwingMMOConversion plugin, StatConverter statConverter, SetConverter setConverter) {
        this.plugin = plugin;
        this.setConverter = setConverter;
        this.mmoItemsFolder = new File(plugin.getDataFolder().getParentFile(), "MMOItems/item");
    }

    public int convertAllItems() {
        int count = 0;
        if (!mmoItemsFolder.exists() || !mmoItemsFolder.isDirectory()) {
            plugin.getLogger().warning("MMOItems item folder not found!");
            return count;
        }

        File[] typeFiles = mmoItemsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (typeFiles != null) {
            for (File typeFile : typeFiles) {
                YamlConfiguration mmoConfig = YamlConfiguration.loadConfiguration(typeFile);
                for (String itemId : mmoConfig.getKeys(false)) {
                    ConfigurationSection itemSection = mmoConfig.getConfigurationSection(itemId);
                    if (itemSection != null) {
                        convertSingleItem(typeFile.getName().replace(".yml", ""), itemId, itemSection);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private void convertSingleItem(String type, String id, ConfigurationSection itemSection) {
        File outputDir = new File(plugin.getDataFolder().getParentFile(), "MythicMobs/Items/converted/" + type);
        outputDir.mkdirs();

        File outputFile = new File(outputDir, id + ".yml");
        YamlConfiguration mythicConfig = new YamlConfiguration();

        convertItem(itemSection, mythicConfig);

        try {
            mythicConfig.save(outputFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save converted item: " + id);
            e.printStackTrace();
        }
    }

    private void convertItem(ConfigurationSection mmoConfig, YamlConfiguration mythicConfig) {
        ConfigurationSection baseSection = mmoConfig.getConfigurationSection("base");
        if (baseSection == null) return;

        mythicConfig.options().indent(2);

        // Core properties
        mythicConfig.set("Id", mmoConfig.getName());
        mythicConfig.set("Material", baseSection.getString("material"));
        mythicConfig.set("Display", baseSection.getString("name"));

        // Custom Model Data
        if (baseSection.contains("custom-model-data")) {
            mythicConfig.set("CustomModelData", baseSection.getInt("custom-model-data"));
        }

        // Lore
        if (baseSection.contains("lore")) {
            List<String> lore = baseSection.getStringList("lore");
            if (!lore.isEmpty()) {
                mythicConfig.set("Lore", lore);
            }
        }

        // Stats
        ConfigurationSection statsSection = mythicConfig.createSection("Stats");

        // Combat Stats
        mapStat(baseSection, statsSection, "attack-damage", "ATTACK_DAMAGE");
        mapStat(baseSection, statsSection, "physical-damage", "PHYSICAL_DAMAGE");
        mapStat(baseSection, statsSection, "magical-damage", "MAGICAL_DAMAGE");
        mapStat(baseSection, statsSection, "true-damage", "TRUE_DAMAGE");
        mapStat(baseSection, statsSection, "penetration", "ARMOR_PENETRATION");
        mapStat(baseSection, statsSection, "critical-strike-chance", "CRITICAL_STRIKE_CHANCE");
        mapStat(baseSection, statsSection, "critical-strike-power", "CRITICAL_STRIKE_DAMAGE");

        // Defense Stats
        mapStat(baseSection, statsSection, "block-power", "BLOCK_RATING");
        mapStat(baseSection, statsSection, "block-rate", "BLOCK_CHANCE");
        mapStat(baseSection, statsSection, "magic-resistance", "MAGIC_DEFENSE");
        mapStat(baseSection, statsSection, "defense", "DEFENSE");
        mapStat(baseSection, statsSection, "armor", "DEFENSE");
        mapStat(baseSection, statsSection, "armor-toughness", "DAMAGE_REDUCTION");

        // Resource Stats
        mapStat(baseSection, statsSection, "mana", "MAX_MANA");
        mapStat(baseSection, statsSection, "stamina", "MAX_STAMINA");
        mapStat(baseSection, statsSection, "energy", "MAX_ENERGY");
        mapStat(baseSection, statsSection, "max-health", "HEALTH");
        mapStat(baseSection, statsSection, "regeneration", "HEALTH_REGENERATION");

        // Convert set data
        if (baseSection.contains("set")) {
            setConverter.convertSetData(mmoConfig, mythicConfig);
        }
    }

    private void mapStat(ConfigurationSection source, ConfigurationSection target, String sourceKey, String targetKey) {
        if (source.contains(sourceKey)) {
            double value = source.getDouble(sourceKey);
            target.set(targetKey, value);
        }
    }
}