package com.pwing.mmoconversion;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SetConverter {
    private final PwingMMOConversion plugin;
    private final StatConverter statConverter;

    public SetConverter(PwingMMOConversion plugin, StatConverter statConverter) {
        this.plugin = plugin;
        this.statConverter = statConverter;
    }

    public void convertSets(File mmoSetsFile, File mythicSetsFile) {
        YamlConfiguration mmoSets = YamlConfiguration.loadConfiguration(mmoSetsFile);
        YamlConfiguration mythicSets = new YamlConfiguration();

        for (String setKey : mmoSets.getKeys(false)) {
            ConfigurationSection mmoSet = mmoSets.getConfigurationSection(setKey);
            ConfigurationSection mythicSet = mythicSets.createSection(setKey);

            // Convert basic set properties
            mythicSet.set("Enabled", true);
            mythicSet.set("Display", mmoSet.getString("name", setKey));

            // Convert set lore
            List<String> lore = new ArrayList<>();
            lore.add("<aqua>" + mmoSet.getString("name", setKey) + " Set");
            mythicSet.set("Lore", lore);

            // Convert bonuses
            ConfigurationSection bonusesSection = mythicSet.createSection("Bonuses");
            convertSetBonuses(mmoSet.getConfigurationSection("bonuses"), bonusesSection);
        }

        try {
            mythicSets.save(mythicSetsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void convertSetBonuses(ConfigurationSection mmoBonuses, ConfigurationSection mythicBonuses) {
        if (mmoBonuses == null) return;

        for (String key : mmoBonuses.getKeys(false)) {
            ConfigurationSection mmoBonus = mmoBonuses.getConfigurationSection(key);
            ConfigurationSection bonus = mythicBonuses.createSection(key);

            // Set required pieces
            bonus.set("Pieces", mmoBonus.getInt("required-pieces"));

            // Convert stats
            if (mmoBonus.contains("stats")) {
                List<String> stats = new ArrayList<>();
                ConfigurationSection mmoStats = mmoBonus.getConfigurationSection("stats");

                for (String statKey : mmoStats.getKeys(false)) {
                    String mappedStat = statConverter.getMappedStat(statKey);
                    double value = mmoStats.getDouble(statKey);
                    stats.add(mappedStat + " " + value + " ADDITIVE");
                }

                bonus.set("Stats", stats);
            }

            // Convert skills/abilities if present
            if (mmoBonus.contains("abilities")) {
                List<String> skills = new ArrayList<>();
                ConfigurationSection abilities = mmoBonus.getConfigurationSection("abilities");

                for (String abilityKey : abilities.getKeys(false)) {
                    skills.add(abilityKey + " @t");
                }

                bonus.set("Skills", skills);
            }
        }
    }


    public void convertSetData(ConfigurationSection mmoConfig, YamlConfiguration mythicConfig) {
        String setName = mmoConfig.getString("set");
        if (setName != null && !setName.isEmpty()) {
            mythicConfig.set("Set", setName);

            // Add any set-specific properties
            if (mmoConfig.contains("set-bonus")) {
                ConfigurationSection setBonusSection = mythicConfig.createSection("SetBonus");
                ConfigurationSection mmoBonuses = mmoConfig.getConfigurationSection("set-bonus");

                for (String key : mmoBonuses.getKeys(false)) {
                    setBonusSection.set(key, mmoBonuses.get(key));
                }
            }
        }
    }
}
