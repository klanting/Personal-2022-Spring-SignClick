package com.klanting.signclick.economy;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.klanting.signclick.economy.companyPatent.Patent;
import com.klanting.signclick.economy.companyPatent.PatentUpgrade;
import com.klanting.signclick.economy.companyPatent.PatentUpgradeCustom;
import com.klanting.signclick.economy.companyUpgrades.*;
import com.klanting.signclick.SignClick;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import static org.bukkit.Bukkit.getServer;

public class Company {

    private String name;
    private String stockName;

    public ArrayList<UUID> owners = new ArrayList<>();

    public double bal = 0.0;
    public double books = 0.0;
    public double securityFunds = 0.0;
    public double spendable = 0.0;
    public Boolean openTrade = false;
    public double lastValue = 40000000.0;
    public Map<UUID, UUID> support = new HashMap<UUID, UUID>();
    public Map<UUID, Integer> shareHolders = new HashMap<UUID, Integer>();
    public String compNamePending = null;
    public double compAmountPending = 0.0;
    public int compWeeksPending = 0;
    public String compReason = "no_reason";
    public String playerNamePending = null;
    public double playerAmountPending = 0.0;
    public int playerWeeksPending = 0;
    public String playerReason = "no_reason";
    public ArrayList<Upgrade> upgrades = new ArrayList<>();

    public ArrayList<Patent> patent = new ArrayList<>();
    public ArrayList<PatentUpgrade> patentUpgrades = new ArrayList<>();
    public Integer patentCrafted = 0;

    public Country country;
    public String type;

    public Company(String n, String StockName, Account creater){
        name = n;
        stockName = StockName;

        support.put(creater.getUuid(), creater.getUuid());
        shareHolders.put(creater.getUuid(), 1000000);
        creater.receivePrivate(stockName, 1000000);

        upgrades.add(new UpgradeExtraPoints(0));
        upgrades.add(new UpgradePatentSlot(0));
        upgrades.add(new UpgradePatentUpgradeSlot(0));
        upgrades.add(new UpgradeCraftLimit(0));
        upgrades.add(new UpgradeInvestReturnTime(0));
        type = "other";
    }

    private static final List<String> softLink = new ArrayList<>(List.of("country"));


    public Company(JsonObject jsonObject, JsonDeserializationContext context){
        /*
        * Load/create company from json file
        * */

        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {

            try{
                String fieldName = field.getName();

                JsonElement element = jsonObject.get(fieldName);

                if (element == null){
                    field.set(this, null);
                    continue;
                }

                if (softLink.contains(fieldName)){
                    switch (fieldName){
                        case "country":
                            field.set(this, CountryManager.getCountry(element.getAsString()));
                    }

                    continue;
                }

                Object o;

                if (field.getType() == String.class){
                    o = element.getAsString();
                }else if (field.getType() == Integer.class){
                    o = element.getAsInt();
                }else if (field.getType() == Double.class){
                    o = element.getAsDouble();
                }else{
                    o = context.deserialize(element, field.getType());
                }

                field.set(this, o);

            }catch (IllegalAccessException ignored){

            }

        }


    }

    public JsonObject toJson(JsonSerializationContext context){

        JsonObject jsonObject = new JsonObject();
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            try{
                String fieldName = field.getName();
                Object fieldValue = field.get(this);

                if (softLink.contains(fieldName)){

                    switch (fieldName){
                        case "country":
                            field.set(this, fieldValue);
                    }
                }

                jsonObject.add(fieldName, context.serialize(fieldValue));

            }catch (IllegalAccessException ignored){

            }

        }

        return jsonObject;
    }

    public Company(String n, String StockName){
        name = n;
        stockName = StockName;
        lastValue = getValue();

        upgrades.add(new UpgradeExtraPoints(0));
        upgrades.add(new UpgradePatentSlot(0));
        upgrades.add(new UpgradePatentUpgradeSlot(0));
        upgrades.add(new UpgradeCraftLimit(0));
        upgrades.add(new UpgradeInvestReturnTime(0));

    }

    Double getBal(){
        return bal;
    }

    public Double getValue(){
        return bal+books;
    }

    public Boolean addBal(Double amount){
        bal += amount;
        Market.changeBase(stockName);

        double modifier = 0.0;
        if (country != null){
            modifier += country.getPolicyBonus(0, 3);
        }

        if (amount > 0){

            spendable += ((0.2+ modifier)*amount);
        }

        double sub_pct = 1.0;

        double modifier2 = 0.0;
        if (country != null){
            if (country.getStability() < 30){
                sub_pct -= 0.20;
            }
            if (country.getStability() < 50){
                sub_pct -= 0.10;
            }
            if (country.getStability() > 80){
                sub_pct += 0.10;
            }
            modifier2 += country.getPolicyBonus(0, 2);
        }


        securityFunds += (0.01*amount)*(sub_pct+(double) upgrades.get(0).getBonus()/100.0)*(1.0+ modifier2);
        return true;
    }

    public Boolean addBalNoPoint(Double amount){
        bal += amount;
        Market.changeBase(stockName);

        return true;
    }

    public Boolean removeBal(Double amount){
        if ((bal+books >= amount) & (spendable >= amount)){
            bal -= amount;
            spendable -= amount;
            Market.changeBase(stockName);
            return true;
        }
        return false;
    }

    void addBooks(Double amount){
        books += amount;
        spendable += (0.2*amount);
    }

    void removeBooks(Double amount){
        books -= amount;
        spendable -= amount;
    }

    public void changeShareHolder(Account holder, Integer amount){
        if (shareHolders.getOrDefault(holder.getUuid(), null) != null){
            Integer am = shareHolders.get(holder.getUuid());
            shareHolders.put(holder.getUuid(), am+amount);

        }else {
            shareHolders.put(holder.getUuid(), amount);
            support.put(holder.getUuid(), null);
        }

        if (shareHolders.getOrDefault(holder.getUuid(), 0) == 0){
            shareHolders.remove(holder.getUuid());
            support.remove(holder.getUuid());
        }
    }

    public void supportUpdate(Account holder, UUID uuid){
        support.put(holder.getUuid(), uuid);
        checkSupport();
        calculateCountry();
    }

    public void checkSupport(){
        double neutral = 0.0;
        Integer total = Market.getTotal(stockName);
        Map<UUID, Integer> s_dict = new HashMap<UUID, Integer>();

        int highest = 0;
        UUID highest_uuid = null;

        for(Entry<UUID, UUID> entry : support.entrySet()){
            UUID k = entry.getKey();
            UUID v = entry.getValue();

            Integer impact = shareHolders.getOrDefault(k, 0);
            if (v == null){
                neutral +=impact;
            }else{
                Integer bef = s_dict.getOrDefault(v, 0);
                s_dict.put(v, bef+impact);

                if (bef+impact > highest){
                    highest = bef+impact;
                    highest_uuid = v;
                }
            }

        }

        neutral = neutral/total.doubleValue();
        ArrayList<UUID> new_owners = new ArrayList<UUID>();
        for(Entry<UUID, Integer> entry : s_dict.entrySet()){
            UUID k = entry.getKey();
            double v = entry.getValue();

            v = v/total.doubleValue();

            if (v >= 0.45){
                new_owners.add(k);
            }else if ((owners.contains(k)) & (v+neutral >= 0.5)){
                new_owners.add(k);
            }
        }
        if (new_owners.size() != 0){
            owners = new_owners;
        }else if (highest_uuid != null){
            new_owners.add(highest_uuid);
            owners = new_owners;
        }


    }

    public Boolean isOwner(UUID uuid){
        return owners.contains(uuid);
    }

    public void getShareTop(Player player){
        ArrayList<UUID> order = new ArrayList<UUID>();
        ArrayList<Integer> values = new ArrayList<Integer>();

        for(Entry<UUID, Integer> entry : shareHolders.entrySet()){
            UUID s = entry.getKey();
            int v = entry.getValue();

            if (!order.isEmpty()){

                boolean found = false;

                for (int i = 0; i < values.size(); i++) {
                    int o = values.get(i);
                    if (v > o){
                        order.add(i, s);
                        values.add(i, v);
                        found = true;
                        break;

                    }
                }

                if (!found){
                    order.add(s);
                    values.add(v);
                }


            }else{
                order.add(s);
                values.add(v);
            }

        }

        player.sendMessage("§bsharetop:");
        Integer total = Market.getTotal(stockName);
        DecimalFormat df = new DecimalFormat("###,###,###");
        DecimalFormat df2 = new DecimalFormat("0.00");
        for (int i = 0; i < values.size(); i++) {
            player.sendMessage("§9"+Bukkit.getOfflinePlayer(order.get(i)).getName()+": §f"+df.format(values.get(i))+
                    " ("+df2.format(values.get(i)/total.doubleValue()*100.0)+"%)");
        }

        if (openTrade){
            player.sendMessage("§eMarket: §f"+"inf"+" ("+"inf"+"%)");
        }else{
            player.sendMessage("§eMarket: §f"+df.format(Market.getMarketAmount(stockName))+" ("+df2.format(Market.getMarketAmount(stockName)/total.doubleValue()*100.0)+"%)");
        }

        order.clear();
        values.clear();
    }


    void dividend(){

        double modifier1 = 0.0;
        double modifier2 = 0.0;
        if (country != null){
            modifier1 = country.getPolicyBonus(0, 1);
            modifier2 = country.getPolicyBonus(1, 1);
        }

        double value_one = (getValue()/Market.getTotal(stockName).doubleValue())*(0.01- modifier1-modifier2);
        removeBal(value_one*(Market.getTotal(stockName)-Market.getMarketAmount(stockName)));
        for (Entry<UUID, Integer> entry : shareHolders.entrySet()){
            UUID holder = entry.getKey();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(holder);
            int shares = entry.getValue();
            double payout = value_one*shares;
            SignClick.getEconomy().depositPlayer(offlinePlayer, payout);
            DecimalFormat df = new DecimalFormat("###,###,##0.00");
            Market.getAccount(holder).sendPlayer("§byou got §7"+df.format(payout)+" §b from dividends in §7"+ stockName);

        }
    }

    public void info(Player player){
        DecimalFormat df = new DecimalFormat("###,###,###");
        ArrayList<String> name_array = new ArrayList<String>();
        ArrayList<String> owner_array = new ArrayList<String>();
        for(Entry<UUID, Integer> entry : shareHolders.entrySet()){
            UUID uuid = entry.getKey();
            name_array.add(Bukkit.getOfflinePlayer(uuid).getName());
        }

        for (int i = 0; i < owners.size(); i++) {
            owner_array.add(Bukkit.getOfflinePlayer(owners.get(i)).getName());
        }

        player.sendMessage("§bName: §7"+name+"\n" +
                "§bStockname: §7"+ stockName +"\n" +
                "§bCEO: §7"+owner_array+"\n" +
                "§bbal: §7"+df.format(getValue())+"\n" +
                "§bshares: §7"+df.format(Market.getTotal(stockName))+"\n" +
                "§bshareholders: §7"+ name_array);

        name_array.clear();
        owner_array.clear();

    }

    public void sendOwner(String message){
        for (int i = 0; i < owners.size(); i++){
            Player p = Bukkit.getPlayer(owners.get(i));
            if (p != null){
                p.sendMessage(message);
            }

        }
    }

    public void acceptOfferCompContract(){
        if (compNamePending == null){
            return;
        }

        Market.setContractComptoComp(stockName, compNamePending, compAmountPending, compWeeksPending, compReason);

        compNamePending = null;
        compAmountPending = 0.0;
        compWeeksPending = 0;
        compReason = "no_reason";

    }

    public void sendOfferCompContract(String stock_name, double amount, int weeks, String reason){
        Market.getBusiness(stock_name).receiveOfferCompContract(stockName, amount, weeks, reason);
    }

    public void receiveOfferCompContract(String stock_name, double amount, int weeks, String reason){
        compNamePending = stock_name;
        compAmountPending = amount;
        compWeeksPending = weeks;
        compReason = reason;

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SignClick.getPlugin(), new Runnable() {
            public void run() {
                compNamePending = null;
                compAmountPending = 0.0;
                compWeeksPending = 0;
                compReason = "no_reason";

            }
        }, 20*120L);

        sendOwner("§b your company §7"+ stockName +"§b got a contract from §7" + stock_name
                + "§b they will ask you §7"+amount+"§b for §7"+weeks+"§b weeks, do §c/company sign_contract_ctc "+ stockName);
    }


    //correct
    public void acceptOfferPlayerContract(){
        if (playerNamePending == null){
            return;
        }

        Market.setContractComptoPlayer(stockName, playerNamePending, playerAmountPending, playerWeeksPending, playerReason);

        playerNamePending = null;
        playerAmountPending = 0.0;
        playerWeeksPending = 0;
        playerReason = "no_reason";

    }



    //correct
    public void receiveOfferPlayerContract(String playerUUID, double amount, int weeks, String reason){
        playerNamePending = playerUUID;
        playerAmountPending = amount;
        playerWeeksPending = weeks;
        playerReason = reason;

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SignClick.getPlugin(), new Runnable() {
            public void run() {
                playerNamePending = null;
                playerAmountPending = 0.0;
                playerWeeksPending = 0;
                playerReason = "no_reason";

            }
        }, 20*120L);

        sendOwner("§b your company §7"+ stockName +"§b got a contract from §7" + Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName()
                + "§b he/she will ask you §7"+amount+"§b for §7"+weeks+"§b weeks, do §c/company sign_contract_ctp "+ stockName);
    }



    public double getSpendable(){
        return spendable;
    }

    public void resetSpendable(){
        double base = 0.2;

        if (country == null || country.getStability() < 50){
            base -= 0.03;
        }

        double modifier = 0.0;
        if (country != null){
            modifier = country.getPolicyBonus(0, 3);
        }

        double pct = (base+modifier);
        if (type.equals("bank") && country != null){
            pct += country.getPolicyBonus(0, 7);
            pct += country.getPolicyBonus(1, 5);
            pct += country.getPolicyBonus(2, 11);
        }
        spendable = getValue()*pct;
    }

    public void resetPatentCrafted(){
        patentCrafted = 0;
    }


    public void saveData(){

        List<String> f_list = new ArrayList<String>();
        for (UUID uuid: owners){
            f_list.add(uuid.toString());
        }
        f_list.clear();

        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "owners", f_list);
        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "name", name);
        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "bal", bal);
        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "books", books);
        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "spendable", spendable);
        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "support", support.toString());
        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "share_holders", shareHolders.toString());
        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "open_trade", openTrade);
        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "share_value", Market.getValue(stockName));
        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "share_base", Market.getBase(stockName));
        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "market_amount", Market.getMarketAmount(stockName));
        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "total", Market.getTotal(stockName));
        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "last_value", lastValue);
        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "security_funds", securityFunds);
        SignClick.getPlugin().getConfig().set("company."+ stockName +"." + "type", type);

        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "SignClick save company "+ stockName +" completed!");

        SignClick.getPlugin().getConfig().options().copyDefaults(true);
        SignClick.getPlugin().saveConfig();

        for (Upgrade u: upgrades){
            u.save(this);
        }

        SignClick.getPlugin().getConfig().set("company."+ stockName +".patent", null);
        for (Patent p: patent){
            p.save(this);
        }

        SignClick.getPlugin().getConfig().set("company."+ stockName +".patent_up", null);
        Integer counter = 0;
        for (PatentUpgrade up: patentUpgrades){
            if (up instanceof PatentUpgradeCustom){
                PatentUpgradeCustom u = (PatentUpgradeCustom) up;
                u.save(this, counter);
            }else{
                up.save(this, counter);
            }
            counter++;
        }
    }

    public double stockCompareGet(){
        return ((getValue()/ lastValue)-1)*100;

    }
    public double stockCompare(){
        double diff = ((getValue()/ lastValue)-1)*100;
        lastValue = getValue();
        return diff;
    }

    public void doUpgrade(Integer id){
        Upgrade u = upgrades.get(id);
        if (u.canUpgrade((int) (bal+books), (int) securityFunds)){
            double base = 1.0;

            double modifier = 0.0;
            double modifier2 = 0.0;
            if (country != null){
                if (country.getStability() < 30){
                    base += 0.05;
                }
                if (country.getStability() < 50){
                    base += 0.15;
                }
                modifier += country.getPolicyBonus(1, 3);
                modifier2 += country.getPolicyBonus(3, 2);
            }

            securityFunds -= u.getUpgradeCostPoints()*(base-modifier);
            int cost = (int) ((double) u.getUpgradeCost()*(base-modifier));
            bal -= cost;
            u.DoUpgrade();

            int pct = upgrades.get(4).getBonus()+(int) (modifier2*100.0);
            double weeks = (10.0-(10.0*pct/100.0));
            double weekly_back = cost/weeks;
            Market.setContractServertoComp(this.stockName, weekly_back, (int) Math.floor(weeks), "Upgrade["+u.id+"] "+u.level, 0);
            if (Math.floor(weeks) < weeks){
                Market.setContractServertoComp(this.stockName, cost - (weekly_back*Math.floor(weeks)), 1, "Upgrade["+u.id+"] "+u.level, (int) Math.floor(weeks));
            }

        }
    }

    public void calculateCountry(){
        HashMap<String, Integer> country_top = new HashMap<String, Integer>();

        Integer highest = -1;
        String linked_name = null;

        for (Entry<UUID, Integer> entry : shareHolders.entrySet()){

            if (CountryManager.getCountry(entry.getKey()) == null){
                continue;
            }

            String countryName = CountryManager.getCountry(entry.getKey()).getName();
            Integer amount = country_top.getOrDefault(countryName, 0);
            amount += entry.getValue();
            country_top.put(countryName, amount);

            if (highest < amount){
                highest = amount;
                linked_name = countryName;
            }
        }

        country = CountryManager.getCountry(linked_name);

    }

    public String getCountry(){
        if (country == null){
            return "none";
        }

        return country.getName();
    }

    public String getName() {
        return name;
    }

    public String getStockName() {
        return stockName;
    }
}