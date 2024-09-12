package com.klanting.signclick.Menus;

import com.klanting.signclick.Economy.Country;
import com.klanting.signclick.Economy.Parties.Party;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class CountryPartyBan implements InventoryHolder {
    private Inventory menu;
    private UUID uuid;

    public CountryPartyBan(UUID uuid){
        menu = Bukkit.createInventory(this, 27, "Country Decision Menu");
        this.uuid = uuid;
        init();
    }

    public void init(){
        String country = Country.ElementUUID(uuid);
        for (Party p: Country.parties.get(country)){
            String name = p.name;

            ItemStack value;
            ItemMeta m;
            value = new ItemStack(Material.WHITE_BANNER, 1);
            m = value.getItemMeta();
            m.setDisplayName("§6"+name);
            value.setItemMeta(m);
            menu.setItem(menu.firstEmpty(), value);
        }
    }

    @Override
    public Inventory getInventory() {
        return menu;
    }
}
