package com.klanting.signclick.events;

import com.klanting.signclick.Economy.Banking;
import com.klanting.signclick.Economy.Company;
import com.klanting.signclick.Economy.CompanyPatent.Auction;
import com.klanting.signclick.Economy.CompanyPatent.Patent;
import com.klanting.signclick.Economy.CompanyPatent.PatentUpgrade;
import com.klanting.signclick.Economy.Decisions.Decision;
import com.klanting.signclick.Economy.Decisions.DecisionAboardMilitary;
import com.klanting.signclick.Economy.Decisions.DecisionBanParty;
import com.klanting.signclick.Economy.Decisions.DecisionForbidParty;
import com.klanting.signclick.Economy.Market;
import com.klanting.signclick.Economy.Parties.Party;
import com.klanting.signclick.Menus.*;
import com.klanting.signclick.SignClick;
import com.klanting.signclick.commands.BankCommands;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.*;

public class MenuEvents implements Listener {
    @EventHandler
    public static void OnClick(InventoryClickEvent event){
        if (event.getClickedInventory() == null || event.getCurrentItem() == null){
            return;
        }

        if (event.getClickedInventory().getHolder() instanceof CompanySelector){
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            Company company = Market.get_business(event.getCurrentItem().getItemMeta().getDisplayName());
            CompanyOwnerMenu screen = new CompanyOwnerMenu(player.getUniqueId(), company);
            player.openInventory(screen.getInventory());
        }

        if (event.getClickedInventory().getHolder() instanceof CompanyOwnerMenu){
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            String option = event.getCurrentItem().getItemMeta().getDisplayName();
            CompanyOwnerMenu old_screen = (CompanyOwnerMenu) event.getClickedInventory().getHolder();
            if (option.equalsIgnoreCase("§6Upgrades")){
                CompanyUpgradeMenu new_screen = new CompanyUpgradeMenu(player.getUniqueId(), old_screen.comp);
                player.openInventory(new_screen.getInventory());
            }else if(option.equalsIgnoreCase("§6Patent")){
                if (Banking.get_stability(old_screen.comp.GetCountry()) < 30){
                    player.sendMessage("§bcan`t acces patent auction with country stability under 30");
                    return;
                }
                CompanyPatentIDMenu new_screen = new CompanyPatentIDMenu(old_screen.comp, true);
                player.openInventory(new_screen.getInventory());

            }else if(option.equalsIgnoreCase("§6Auction")){
                CompanyAuctionMenu new_screen = new CompanyAuctionMenu(old_screen.comp);
                player.openInventory(new_screen.getInventory());

            }else if(option.equalsIgnoreCase("§6Recipes")){
                CompanyPatentIDMenu new_screen = new CompanyPatentIDMenu(old_screen.comp, false);
                player.openInventory(new_screen.getInventory());

            }else if(option.equalsIgnoreCase("§6Type")){
                CompanyTypeSelect new_screen = new CompanyTypeSelect(old_screen.comp);
                player.openInventory(new_screen.getInventory());

            }
        }

        if (event.getClickedInventory().getHolder() instanceof CompanyUpgradeMenu){
            CompanyUpgradeMenu screen = (CompanyUpgradeMenu) event.getClickedInventory().getHolder();
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            int id = event.getSlot()-11;
            screen.comp.DoUpgrade(id);
            screen.init();
        }
        if (event.getClickedInventory().getHolder() instanceof CompanyPatentIDMenu){
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            String option = event.getCurrentItem().getItemMeta().getDisplayName();
            CompanyPatentIDMenu old_screen = (CompanyPatentIDMenu) event.getClickedInventory().getHolder();

            if (old_screen.designer){
                if (option.equalsIgnoreCase("§6Empty Patent")){
                    CompanyPatentSelectorMenu new_screen = new CompanyPatentSelectorMenu(player.getUniqueId(), old_screen.comp);
                    player.openInventory(new_screen.getInventory());
                }else{
                    CompanyPatentDesignerMenu new_screen = new CompanyPatentDesignerMenu(old_screen.comp.patent.get(event.getSlot()), old_screen.comp);
                    player.openInventory(new_screen.getInventory());
                }
            }else{
                if (!option.equalsIgnoreCase("§6Empty Patent")){
                    CompanyPatentCrafting new_screen = new CompanyPatentCrafting(old_screen.comp, old_screen.comp.patent.get(event.getSlot()));
                    player.openInventory(new_screen.getInventory());
                }
            }

        }

        if (event.getClickedInventory().getHolder() instanceof CompanyPatentSelectorMenu){
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();

            CompanyPatentSelectorMenu old_screen = (CompanyPatentSelectorMenu) event.getClickedInventory().getHolder();
            Patent pat = new Patent("Nameless", item.getType(), new ArrayList<PatentUpgrade>());
            CompanyPatentDesignerMenu new_screen = new CompanyPatentDesignerMenu(pat, old_screen.comp);
            player.openInventory(new_screen.getInventory());
        }

        if (event.getClickedInventory().getHolder() instanceof CompanyPatentDesignerMenu){
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);

            CompanyPatentDesignerMenu old_screen = (CompanyPatentDesignerMenu) event.getClickedInventory().getHolder();
            Patent pat = old_screen.patent;

            String option = event.getCurrentItem().getItemMeta().getDisplayName();
            if (option.equalsIgnoreCase("§aSave")){
                if (!old_screen.comp.patent.contains(old_screen.patent)){
                    old_screen.comp.patent.add(old_screen.patent);
                    old_screen.patent.createCraft(old_screen.comp);
                }

                CompanyPatentIDMenu new_screen = new CompanyPatentIDMenu(old_screen.comp, true);
                player.openInventory(new_screen.getInventory());
                return;
            }
            if (event.getCurrentItem().getType().equals(Material.NAME_TAG)){
                pat.setName(event.getCurrentItem().getItemMeta().getDisplayName());
                old_screen.init();
                return;
            }

            if (event.getCurrentItem().getType().equals(Material.LIGHT_GRAY_DYE)){
                CompanyPatentDesignerUpgrade new_screen = new CompanyPatentDesignerUpgrade(pat, old_screen.comp);
                player.openInventory(new_screen.getInventory());
            }
        }

        if (event.getClickedInventory().getHolder() instanceof CompanyAuctionMenu){
            event.setCancelled(true);
            CompanyAuctionMenu old_screen = (CompanyAuctionMenu) event.getClickedInventory().getHolder();
            int location = event.getSlot();

            int add_price = 100000;
            if (Auction.bits_owner.get(location) == null){
                add_price = 0;
            }

            Auction.setBit(location, Auction.getBit(location)+add_price, old_screen.comp.Sname);
            //old_screen.comp.patent_upgrades.add(Auction.to_buy.get(location));
            old_screen.init();
        }

        if (event.getClickedInventory().getHolder() instanceof CompanyPatentDesignerUpgrade){
            event.setCancelled(true);
            CompanyPatentDesignerUpgrade old_screen = (CompanyPatentDesignerUpgrade) event.getClickedInventory().getHolder();
            PatentUpgrade pat_up = old_screen.pat_list.get(event.getSlot());
            Patent pat = old_screen.patent;
            pat.upgrades.add(pat_up);

            Player player = (Player) event.getWhoClicked();
            CompanyPatentDesignerMenu new_screen = new CompanyPatentDesignerMenu(pat, old_screen.comp);
            player.openInventory(new_screen.getInventory());
        }

        if (event.getClickedInventory().getHolder() instanceof CompanyPatentCrafting){
            event.setCancelled(true);
            CompanyPatentCrafting old_screen = (CompanyPatentCrafting) event.getClickedInventory().getHolder();
            String option = event.getCurrentItem().getItemMeta().getDisplayName();
            if (option.equalsIgnoreCase("§6Get Patent Sheet")){
                Player player = (Player) event.getWhoClicked();
                if (old_screen.comp.patent_crafted < old_screen.comp.upgrades.get(3).getBonus()){
                    old_screen.comp.patent_crafted += 1;

                    ItemStack item = new ItemStack(Material.PAPER, 1);

                    ItemMeta m = item.getItemMeta();
                    m.setDisplayName("§6"+old_screen.comp.Sname+":"+old_screen.patent.getName()+":"+old_screen.comp.patent.indexOf(old_screen.patent));
                    item.setItemMeta(m);

                    player.getInventory().setItem(player.getInventory().firstEmpty(), item);
                }else{
                    player.sendMessage(ChatColor.RED+"Craft limit Reached");
                }


            }
        }

        if (event.getClickedInventory().getHolder() instanceof CountryPolicy){
            event.setCancelled(true);
            CountryPolicy old_screen = (CountryPolicy) event.getClickedInventory().getHolder();
            Player player = (Player) event.getWhoClicked();

            int slot = event.getSlot();
            int row = slot/9;
            int level = slot - 9*row-2;
            if (level < 0){
                return;
            }

            String country = Banking.ElementUUID(player.getUniqueId());

            Banking.setPolicies(country, row-1, level);
            old_screen.init(player.getUniqueId());
        }

        if (event.getClickedInventory().getHolder() instanceof CountryElectionMenu){
            ItemStack item = event.getCurrentItem();
            ItemMeta m = item.getItemMeta();
            String party = m.getDisplayName().substring(2);

            CountryElectionMenu old_screen = (CountryElectionMenu) event.getClickedInventory().getHolder();
            old_screen.e.vote(party, event.getWhoClicked().getUniqueId());

            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
        }

        if (event.getClickedInventory().getHolder() instanceof PartyDecisionVote){
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();

            PartyDecisionVote old_screen = (PartyDecisionVote) event.getClickedInventory().getHolder();
            String country = old_screen.p.country;
            Decision d = Banking.decisions.get(country).get(slot);

            PartyDecisionChoice new_screen = new PartyDecisionChoice(old_screen.p, d);
            player.openInventory(new_screen.getInventory());

        }

        if (event.getClickedInventory().getHolder() instanceof PartyDecisionChoice){
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();

            PartyDecisionChoice old_screen = (PartyDecisionChoice) event.getClickedInventory().getHolder();
            old_screen.d.vote(old_screen.p, slot == 11);

            player.closeInventory();

        }

        if (event.getClickedInventory().getHolder() instanceof CountryMenu){
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            String option = event.getCurrentItem().getItemMeta().getDisplayName();
            if (option.equalsIgnoreCase("§6Policy")){
                CountryPolicy screen = new CountryPolicy(player.getUniqueId());
                player.openInventory(screen.getInventory());
            }else if (option.equalsIgnoreCase("§6Decisions")){
                CountryDecisionMenu screen = new CountryDecisionMenu(player.getUniqueId());
                player.openInventory(screen.getInventory());
            }
        }

        if (event.getClickedInventory().getHolder() instanceof CountryDecisionMenu){
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            String option = event.getCurrentItem().getItemMeta().getDisplayName();
            if (option.equalsIgnoreCase("§6Ban party")){
                if (BankCommands.CountryElections.containsKey(Banking.Element(player))){
                    player.sendMessage("§b you can`t ban parties during elections");
                    return;
                }
                CountryPartyBan screen = new CountryPartyBan(player.getUniqueId());
                player.openInventory(screen.getInventory());
            }else if (option.equalsIgnoreCase("§6Forbid party") || option.equalsIgnoreCase("§6Allow party")){
                String name;
                if (option.equalsIgnoreCase("§6Allow party")){
                    name = "§6Allow Parties";
                }else{
                    name = "§6Forbid Parties";
                }


                String country = Banking.Element(player);
                boolean go_to = !Banking.forbid_party.getOrDefault(country, false);

                if (Banking.get_stability(country) < 30.0){
                    player.sendMessage("§brequired stability is 30");
                    return;
                }

                Decision d = new DecisionForbidParty(name, 0.5, country, go_to);
                List<Decision> d_list = Banking.decisions.getOrDefault(country, new ArrayList<>());
                d_list.add(d);
                Banking.decisions.put(country, d_list);
            }else if (option.equalsIgnoreCase("§6Abort military payments") || option.equalsIgnoreCase("§6Allow military payments")){
                String name;
                if (option.equalsIgnoreCase("§6Allow military payments")){
                    name = "§6Allow military payments";
                }else{
                    name = "§6Abort military payments";
                }

                String country = Banking.Element(player);
                boolean go_to = !Banking.aboard_military.getOrDefault(country, false);

                Decision d = new DecisionAboardMilitary(name, 0.5, country, go_to);
                List<Decision> d_list = Banking.decisions.getOrDefault(country, new ArrayList<>());
                d_list.add(d);
                Banking.decisions.put(country, d_list);

            }

        }

        if (event.getClickedInventory().getHolder() instanceof CountryPartyBan){
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            int slot = event.getSlot();
            String country = Banking.Element(player);
            Party p = Banking.parties.get(country).get(slot);
            Party ph = Banking.getRuling(country);
            if (ph == p){
                player.sendMessage("§bcan`t ban ruling party");
                return;
            }

            if (Banking.get_stability(country) < 40.0){
                player.sendMessage("§brequired stability is 40");
                return;
            }

            Decision d = new DecisionBanParty("§6Ban Party §9"+p.name, 0.5, country, p);
            List<Decision> d_list = Banking.decisions.getOrDefault(country, new ArrayList<>());
            d_list.add(d);
            Banking.decisions.put(country, d_list);

            player.closeInventory();


        }

        if (event.getClickedInventory().getHolder() instanceof CompanyTypeSelect){
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            CompanyTypeSelect old_screen = (CompanyTypeSelect) event.getClickedInventory().getHolder();
            String name = event.getCurrentItem().getItemMeta().getDisplayName();
            old_screen.comp.type = name.substring(2);
            player.closeInventory();
        }




    }

    /*
    @EventHandler
    public static void OnMove(PlayerMoveEvent e){
        Player p = e.getPlayer();
        Particle.DustOptions dot = new Particle.DustOptions(Color.RED, 1.0F);
        p.spawnParticle(Particle.REDSTONE, p.getLocation(), 10, dot);
    }*/
}