package com.klanting.signclick.Menus;

import com.klanting.signclick.Economy.Company;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.klanting.signclick.Economy.Market;

import java.util.UUID;

public class CompanySelector implements InventoryHolder {

    private Inventory menu;

    public CompanySelector(UUID uuid){
        menu = Bukkit.createInventory(this, 54, "Company Selector");
        init(uuid);
    }

    private void init(UUID uuid){
        ItemStack item;
        for(Company c: Market.get_business_by_owner(uuid)){
            item = new ItemStack(Material.DIAMOND_BLOCK,1);
            ItemMeta m = item.getItemMeta();
            m.setDisplayName(c.Sname);
            item.setItemMeta(m);
            menu.setItem(menu.firstEmpty(), item);
        }


    }

    @Override
    public Inventory getInventory() {
        return menu;
    }
}
