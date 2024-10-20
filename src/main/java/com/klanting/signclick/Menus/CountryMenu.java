package com.klanting.signclick.Menus;

import com.klanting.signclick.economy.Country;
import com.klanting.signclick.economy.CountryManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

public class CountryMenu implements InventoryHolder {
    private Inventory menu;
    private UUID uuid;

    public CountryMenu(UUID uuid){
        menu = Bukkit.createInventory(this, 54, "Country Menu");
        this.uuid = uuid;
        init();
    }

    public void init(){
        Country country = CountryManager.getCountry(uuid);
        
        ItemStack value;
        ItemMeta m;

        value = new ItemStack(Material.GOLD_BLOCK, 1);
        m = value.getItemMeta();
        DecimalFormat df = new DecimalFormat("###,###,###.00");
        m.setDisplayName("§6"+country);
        ArrayList<String> l = new ArrayList<>();

        l.add("§6balance: §9"+ df.format(country.getBalance()));
        l.add("§6Stability: §9"+ df.format(country.getStability()));
        m.setLore(l);
        value.setItemMeta(m);
        menu.setItem(13, value);

        
        value = new ItemStack(Material.ANVIL, 1);
        m = value.getItemMeta();
        m.setDisplayName("§6Policy");
        value.setItemMeta(m);
        menu.setItem(22, value);

        value = new ItemStack(Material.PAPER, 1);
        m = value.getItemMeta();
        m.setDisplayName("§6Decisions");
        value.setItemMeta(m);
        menu.setItem(21, value);
    }

    @Override
    public Inventory getInventory() {
        return menu;
    }
}
