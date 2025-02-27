package com.pwing.mmoconversion;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class PwingMMOConversion extends JavaPlugin {
    private ItemConverter itemConverter;
    private SetConverter setConverter;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        StatConverter statConverter = new StatConverter(new File(getDataFolder().getParentFile(), "MythicMobs"));
        this.setConverter = new SetConverter(this, statConverter);
        this.itemConverter = new ItemConverter(this, statConverter, setConverter);

        getCommand("convertitems").setExecutor(new ConvertCommand());
        
        getLogger().info("PwingMMOConversion enabled successfully!");
    }
    @Override
    public void onDisable() {
        getLogger().info("PwingMMOConversion disabled!");
    }

    private class ConvertCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!sender.hasPermission("pwingmmoconversion.convert")) {
                sender.sendMessage("§cYou don't have permission to use this command!");
                return true;
            }

            sender.sendMessage("§aStarting conversion process...");

            // Convert items
            int convertedItems = itemConverter.convertAllItems();

            // Convert sets
            File mmoSetsFile = new File(getDataFolder().getParentFile(), "MMOItems/item-sets.yml");
            File mythicSetsFile = new File(getDataFolder().getParentFile(), "MythicCrucible/equipment-sets.yml");
            setConverter.convertSets(mmoSetsFile, mythicSetsFile);

            sender.sendMessage("§aSuccessfully converted " + convertedItems + " items and their sets!");

            return true;
        }
    }
}