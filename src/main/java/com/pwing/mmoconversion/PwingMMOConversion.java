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
    private StatConverter statConverter;

    @Override
    public void onEnable() {
        // Create plugin directory
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        // Initialize converters
        this.statConverter = new StatConverter();
        this.itemConverter = new ItemConverter(this, statConverter);
        
        // Register command
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
            int converted = itemConverter.convertAllItems();
            sender.sendMessage("§aSuccessfully converted " + converted + " items!");
            
            return true;
        }
    }
}
