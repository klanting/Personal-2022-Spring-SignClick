package com.klanting.signclick;

import com.klanting.signclick.Calculate.*;
import com.klanting.signclick.Economy.Banking;
import com.klanting.signclick.Economy.CompanyPatent.Auction;
import com.klanting.signclick.Economy.Market;
import com.klanting.signclick.commands.*;
import com.klanting.signclick.events.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;
import org.dynmap.DynmapAPI;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class SignClick extends JavaPlugin{

    public SignClick(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description, @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, dataFolder, file);
    }


    private static SignClick plugin;

    @Override
    public void onEnable() {
        plugin = this;

        if (!setupEconomy() ) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "Economy failed!");
            getServer().getPluginManager().disablePlugin(this);
            return;}
        if (!setupDynmap() ) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "Dynmap failed!");
            getServer().getPluginManager().disablePlugin(this);
            return;}

        if (this.getConfig().contains("data")){
            this.RestoreDoors();
        }

            Banking.RestoreData();
        if (this.getConfig().contains("weekly")){
            WeeklyPay.restore();
        }

        if (this.getConfig().contains("accounts")){
            Market.RestoreData();
        }

        DynmapCheck.Hide();
        WeeklyPay.check();
        WeeklyComp.check();
        WeeklyAuction.check();
        Auction.Restore();

        getServer().getPluginManager().registerEvents(new SignEvents(), this);
        getServer().getPluginManager().registerEvents(new DynmapEvents(), this);
        getServer().getPluginManager().registerEvents(new CountryEvents(), this);
        getServer().getPluginManager().registerEvents(new BusEvents(), this);
        getServer().getPluginManager().registerEvents(new MenuEvents(), this);
        getServer().getPluginManager().registerEvents(new PatentEvents(), this);
        getCommand("signclickpos").setExecutor(new SignCommands());
        getCommand("signclick").setExecutor(new BasicCommands());
        getCommand("weeklypay").setExecutor(new BasicCommands());
        getCommand("discord").setExecutor(new BasicCommands());
        getCommand("dynmap").setExecutor(new BasicCommands());
        getCommand("country").setExecutor(new BankCommands());
        getCommand("company").setExecutor(new BusCommands());
        getCommand("party").setExecutor(new PartyCommands());
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "SignClick is enabled!");


    }

    @Override
    public void onDisable() {
        getConfig().options().copyDefaults(false);
        if (!SignIncome.owner.isEmpty()){
            this.SaveDoors();
        }

        WeeklyPay.save();
        Banking.SaveData();
        Market.SaveData();
        Auction.Save();
        WeeklyComp.Save();

        getServer().getConsoleSender().sendMessage(ChatColor.RED + "SignClick is disabled!");
        this.saveConfig();

    }


    private static Economy econ = null;
    private static DynmapAPI dynmap = null;

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "Vault Not found!");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        getServer().getConsoleSender().sendMessage(ChatColor.RED + String.valueOf(Economy.class));
        if (rsp == null) {
            rsp = getServer().getServicesManager().getRegistration(Economy.class);
        }
        if (rsp == null) {
            rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        }
        if (rsp == null) {
            rsp = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        }
        if (rsp == null) {
            rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        }



        if (rsp == null) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "RSP is null!");
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupDynmap(){
        if (getServer().getPluginManager().getPlugin("dynmap") == null) {
            return false;
        }
        RegisteredServiceProvider<DynmapAPI> rsp = getServer().getServicesManager().getRegistration(DynmapAPI.class);
        dynmap = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");
        return true;


        //DynmapAPI dynmap = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("Dynmap");
    }

    public void SaveDoors(){
        for (Map.Entry<Location, UUID> entry : SignIncome.owner.entrySet()){
            this.getConfig().set("data." + entry.getValue().toString(), entry.getKey());
        }


    }
    public void RestoreDoors(){
        this.getConfig().getConfigurationSection("data").getKeys(true).forEach(key ->{
            Location location = ((Location) this.getConfig().get("data." + key));
            UUID content = UUID.fromString(key);

            SignIncome.owner.put(location, content);
        });

    }


    public static Economy getEconomy() {
        return econ;
    }

    public static DynmapAPI getDynmap() {
        return dynmap;
    }

    public static SignClick getPlugin() {
        return plugin;
    }
}