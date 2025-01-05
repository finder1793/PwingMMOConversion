package com.pwing.mmoconversion;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.HashMap;
import java.util.Map;

public class StatConverter {
    private final Map<String, String> statMappings;
    
    public StatConverter() {
        this.statMappings = initializeStatMappings();
    }

    private Map<String, String> initializeStatMappings() {
        Map<String, String> mappings = new HashMap<>();
        
        // Combat Stats
        mappings.put("ATTACK_DAMAGE", "ATTACK_DAMAGE");
        mappings.put("ATTACK_SPEED", "ATTACK_SPEED");
        mappings.put("CRITICAL_STRIKE_CHANCE", "CRITICAL_STRIKE_CHANCE");
        mappings.put("CRITICAL_STRIKE_POWER", "CRITICAL_STRIKE_DAMAGE");
        mappings.put("DODGE_RATING", "DODGE_CHANCE");
        mappings.put("PARRY_RATING", "PARRY_CHANCE");
        
        // Defense Stats
        mappings.put("DEFENSE", "DEFENSE");
        mappings.put("ARMOR", "DEFENSE");
        mappings.put("ARMOR_TOUGHNESS", "DAMAGE_REDUCTION");
        
        // Utility Stats
        mappings.put("MOVEMENT_SPEED", "MOVEMENT_SPEED");
        mappings.put("MAX_HEALTH", "HEALTH");
        mappings.put("REGENERATION", "HEALTH_REGENERATION");
        
        return mappings;
    }

    public void convertConfigStats(ConfigurationSection mmoConfig, YamlConfiguration mythicConfig) {
        if (mmoConfig.contains("stats")) {
            ConfigurationSection statsSection = mythicConfig.createSection("Stats");
            ConfigurationSection mmoStats = mmoConfig.getConfigurationSection("stats");
            
            for (String statKey : mmoStats.getKeys(false)) {
                String mappedStat = getMappedStat(statKey);
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

    private String getMappedStat(String mmoStat) {
        return statMappings.getOrDefault(mmoStat.toUpperCase(), mmoStat.toUpperCase());
    }
}