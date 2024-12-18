package com.klanting.signclick.Menus;

import com.klanting.signclick.economy.Country;
import com.klanting.signclick.economy.Company;
import com.klanting.signclick.economy.companyUpgrades.Upgrade;
import com.klanting.signclick.economy.CountryManager;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

public class CompanyUpgradeMenu implements InventoryHolder {
    private Inventory menu;
    public Company comp;

    public CompanyUpgradeMenu(UUID uuid, Company company){
        menu = Bukkit.createInventory(this, 27, "Company Upgrade Menu");
        comp = company;
        if (!comp.isOwner(uuid)){
            return;
        }

        init();
    }

     public void init(){
        int counter = 11;
        for (Upgrade up: comp.upgrades){
            ItemStack value = new ItemStack(up.material, 1);
            ItemMeta m = value.getItemMeta();
            m.setDisplayName("§6"+up.name +" Lvl. §c"+ up.level);
            ArrayList<String> l = new ArrayList<>();
            DecimalFormat df = new DecimalFormat("###,###,###");
            Country country = CountryManager.getCountry(comp.getCountry());
            if (country != null){
                if (up.getUpgradeCost() != -1){
                    l.add("§6Cost: §8"+ df.format((double) up.getUpgradeCost()*(1.0- country.getPolicyBonus(1, 3))));
                }
                if (up.getUpgradeCostPoints() != -1){
                    l.add("§6Points: §8"+ df.format((double) up.getUpgradeCostPoints()*(1.0- country.getPolicyBonus(1, 3))));
                }
            }


            m.setLore(l);
            value.setItemMeta(m);
            menu.setItem(counter, value);
            counter ++;
        }
    }

    @Override
    public Inventory getInventory() {
        return menu;
    }
}
