package com.pwing.mmoconversion;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatConverter {
    private final Map<String, String> statMappings;
    private final File mythicStatsFile;
    private final Map<String, List<String>> statCategories;
    private final Map<String, String> statFormats;

    public StatConverter(File mythicFolder) {
        this.statMappings = initializeStatMappings();
        this.mythicStatsFile = new File(mythicFolder, "stats.yml");
        this.statCategories = initializeStatCategories();
        this.statFormats = initializeStatFormats();
    }

    private Map<String, String> initializeStatMappings() {
        Map<String, String> mappings = new HashMap<>();

        // Combat Stats
        mappings.put("ATTACK_DAMAGE", "ATTACK_DAMAGE");
        mappings.put("PHYSICAL_DAMAGE", "PHYSICAL_DAMAGE");
        mappings.put("MAGICAL_DAMAGE", "MAGICAL_DAMAGE");
        mappings.put("TRUE_DAMAGE", "TRUE_DAMAGE");
        mappings.put("PENETRATION", "ARMOR_PENETRATION");
        mappings.put("CRITICAL_STRIKE_CHANCE", "CRITICAL_STRIKE_CHANCE");
        mappings.put("CRITICAL_STRIKE_POWER", "CRITICAL_STRIKE_DAMAGE");

        // Defense Stats
        mappings.put("BLOCK_POWER", "BLOCK_RATING");
        mappings.put("BLOCK_RATE", "BLOCK_CHANCE");
        mappings.put("MAGIC_RESISTANCE", "MAGIC_DEFENSE");
        mappings.put("DEFENSE", "DEFENSE");
        mappings.put("ARMOR", "DEFENSE");
        mappings.put("ARMOR_TOUGHNESS", "DAMAGE_REDUCTION");

        // Resource Stats
        mappings.put("MANA", "MAX_MANA");
        mappings.put("STAMINA", "MAX_STAMINA");
        mappings.put("ENERGY", "MAX_ENERGY");
        mappings.put("MAX_HEALTH", "HEALTH");
        mappings.put("REGENERATION", "HEALTH_REGENERATION");

        return mappings;
    }

    private Map<String, List<String>> initializeStatCategories() {
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("COMBAT", Arrays.asList("ATTACK_DAMAGE", "CRITICAL_STRIKE", "PHYSICAL_DAMAGE", "MAGICAL_DAMAGE"));
        categories.put("DEFENSE", Arrays.asList("ARMOR", "MAGIC_RESIST", "BLOCK_RATING", "DAMAGE_REDUCTION"));
        categories.put("RESOURCE", Arrays.asList("HEALTH", "MANA", "STAMINA", "ENERGY"));
        categories.put("UTILITY", Arrays.asList("MOVEMENT_SPEED", "JUMP_POWER"));
        return categories;
    }

    private Map<String, String> initializeStatFormats() {
        Map<String, String> formats = new HashMap<>();
        formats.put("PERCENTAGE", "&e{value}%");
        formats.put("DECIMAL", "&e{value.###}");
        formats.put("INTEGER", "&e{value}");
        return formats;
    }

    public void updateMythicStats(String statName, ConfigurationSection originalStat) {
        try {
            YamlConfiguration statsConfig = YamlConfiguration.loadConfiguration(mythicStatsFile);

            if (!statsConfig.contains(statName)) {
                ConfigurationSection newStat = statsConfig.createSection(statName);
                newStat.set("name", originalStat.getString("name", statName));
                newStat.set("scaling", originalStat.getDouble("scaling", 1.0));
                newStat.set("max", originalStat.getDouble("max", 100.0));
                newStat.set("min", originalStat.getDouble("min", 0.0));
                newStat.set("format", getStatFormat(getCategoryForStat(statName)));

                statsConfig.save(mythicStatsFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCategoryForStat(String statName) {
        for (Map.Entry<String, List<String>> category : statCategories.entrySet()) {
            if (category.getValue().contains(statName)) {
                return category.getKey();
            }
        }
        return "COMBAT";
    }

    private String getStatFormat(String category) {
        switch (category) {
            case "COMBAT":
            case "DEFENSE":
                return statFormats.get("DECIMAL");
            case "RESOURCE":
                return statFormats.get("INTEGER");
            default:
                return statFormats.get("PERCENTAGE");
        }
    }

    public void convertConfigStats(ConfigurationSection mmoConfig, YamlConfiguration mythicConfig) {
        if (mmoConfig.contains("stats")) {
            ConfigurationSection statsSection = mythicConfig.createSection("Stats");
            ConfigurationSection mmoStats = mmoConfig.getConfigurationSection("stats");

            for (String statKey : mmoStats.getKeys(false)) {
                String mappedStat = getMappedStat(statKey);
                updateMythicStats(mappedStat, mmoStats.getConfigurationSection(statKey));

                if (mmoStats.isConfigurationSection(statKey)) {
                    handleRangeStat(mmoStats.getConfigurationSection(statKey), statsSection, mappedStat);
                } else {
                    statsSection.set(mappedStat, mmoStats.getDouble(statKey));
                }
            }
        }
    }

    private void handleRangeStat(ConfigurationSection statSection, ConfigurationSection statsSection, String mappedStat) {
        double min = statSection.getDouble("min", 0);
        double max = statSection.getDouble("max", 0);
        double base = (min + max) / 2;
        statsSection.set(mappedStat, base);
    }

    public String getMappedStat(String mmoStat) {
        return statMappings.getOrDefault(mmoStat.toUpperCase(), mmoStat.toUpperCase());
    }
}