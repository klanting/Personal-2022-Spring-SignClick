package com.klanting.signclick.economy;

import com.google.common.reflect.TypeToken;
import com.klanting.signclick.calculate.SignStock;
import com.klanting.signclick.SignClick;
import com.klanting.signclick.economy.contracts.*;
import com.klanting.signclick.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class Market {
    /*
    * Stores which account corresponds to which UUID (player)
    * */
    private static Map<UUID, Account> accounts = new HashMap<UUID, Account>();
    private static Map<String, Company> companies = new HashMap<String, Company>();

    public static final Double fee = SignClick.getPlugin().getConfig().getDouble("fee");
    public static final Double flux = SignClick.getPlugin().getConfig().getDouble("flux");

    public static ArrayList<Contract> contractCompToComp = new ArrayList<>();

    public static ArrayList<Contract> contractCompToPlayer = new ArrayList<>();

    public static ArrayList<Contract> contractPlayerToComp = new ArrayList<>();

    public static ArrayList<ContractSTC> contractServerToComp = new ArrayList<>();

    public static ArrayList<Location> stockSigns = new ArrayList<>();

    public static void clear(){
        /*
        * Clear all static information
        * */
        accounts.clear();
        companies.clear();

        contractCompToComp.clear();
        contractCompToPlayer.clear();
        contractServerToComp.clear();
        contractPlayerToComp.clear();
        stockSigns.clear();
    }

    public static Double getBuyPrice(String Sname, Integer amount){
        Company comp = Market.getCompany(Sname);

        double market_pct = (comp.getMarketShares().doubleValue()/(comp.getTotalShares().doubleValue()+Math.min(comp.getMarketShares(), 0)));
        double a = (1.0 - market_pct) * 25.0 - 10.0;

        market_pct = ((comp.getMarketShares().doubleValue()-amount.doubleValue())/(comp.getTotalShares().doubleValue()+Math.min(comp.getMarketShares(), 0)));
        double b = (1.0 - market_pct) * 25.0 - 10.0;


        double base = comp.getShareBase();
        double v = base * calculateFluxChange(a, b);
        return v*amount;
    }

    public static  Double getSellPrice(String Sname, Integer amount){

        String countryName = Market.getCompany(Sname).getCountry();
        Country country = CountryManager.getCountry(countryName);
        if (country == null){
            country = new CountryNull();
        }


        double sub_fee = fee;

        if (country.getStability() < 50){
            sub_fee += 0.01;
        }
        return (getBuyPrice(Sname, -amount)*-1)*(1.0 - (sub_fee - country.getPolicyBonus(0, 0)- country.getPolicyBonus(1, 1)));

    }


    public static Boolean buy(String Sname, Integer amount, Account acc){
        Company comp = getCompany(Sname);
        if (comp.getMarketShares() >= amount || comp.openTrade){
            int market_am = comp.getMarketShares();
            comp.setMarketShares(market_am-amount);

            comp.getCOM().changeShareHolder(acc, amount);

            return true;
        }
        return false;
    }

    public static Boolean sell(String Sname, Integer amount, Account acc){
        Company comp = companies.get(Sname);

        int market_am = comp.getMarketShares();
        comp.setMarketShares(market_am+amount);

        comp.getCOM().changeShareHolder(acc, -amount);

        return true;
    }

    public static double calculateFluxChange(double a, double b){
        return (Math.pow(flux, b) - Math.pow(flux, a))/Math.log(flux)/(b-a);
    }

    public static Company getCompany(String Sname){
        return companies.get(Sname);
    }

    public static Boolean addCompany(String namebus, String StockName, Account acc){

        /*
        * Check StockName already in use
        * */
        if (companies.containsKey(StockName)){
            return false;
        }

        /*
         * Check name already in use
         * */
        for (Company c: companies.values()){
            if (c.getName().equals(namebus)){
                return false;
            }
        }

        Company comp = new Company(namebus, StockName, acc);
        companies.put(StockName, comp);

        comp.changeBase();

        comp.checkSupport();
        comp.calculateCountry();

        return true;
    }

    public static Boolean hasBusiness(String Sname){
        return companies.containsKey(Sname);
    }

    public static ArrayList<Company> getBusinessByOwner(UUID uuid){
        ArrayList<Company> outputs = new ArrayList<Company>();
        for(Map.Entry<String, Company> entry : companies.entrySet()){
            if (entry.getValue().getCOM().isOwner(uuid)){
                outputs.add(entry.getValue());
            }
        }
        return outputs;
    }

    public static void getMarketValueTop(Player player){
        /*
        * Make a ranking of the top companies by value
        * */

        ArrayList<Map.Entry<String, Company>> entries = new ArrayList<>(companies.entrySet());

        entries.sort(Comparator.comparing(item -> -item.getValue().getValue()));

        for (int i=0; i<entries.size(); i++){
            String b = entries.get(i).getKey();
            Double v = entries.get(i).getValue().getValue();
            DecimalFormat df = new DecimalFormat("###,###,###");
            int i2 = i + 1;
            player.sendMessage("§b"+i2+". §3"+b+": §7" +df.format(v)+"\n");
        }
    }


    public static Double getFee(){
        return fee;
    }

    public static Boolean hasAccount(Player player){

        return accounts.containsKey(player.getUniqueId());

    }

    public static void createAccount(Player player){
        createAccount(player.getUniqueId());
    }

    public static void createAccount(UUID uuid){
        Account acc = new Account(uuid);
        accounts.put(uuid, acc);

    }

    public static Account getAccount(Player player){

        return getAccount(player.getUniqueId());

    }

    public static Account getAccount(UUID uuid){
        if (!accounts.containsKey(uuid)){
            Market.createAccount(uuid);
        }
        return accounts.get(uuid);

    }


    public static Boolean hasAccount(UUID uuid){
        return accounts.containsKey(uuid);

    }


    public static void resetSpendable(){

        for(Map.Entry<String, Company> entry : companies.entrySet()){
            Company comp = entry.getValue();
            comp.resetSpendable();
        }

    }

    public static void resetPatentCrafted(){

        for(Map.Entry<String, Company> entry : companies.entrySet()){
            Company comp = entry.getValue();
            comp.resetPatentCrafted();
        }

    }

    public static void runDividends(){

        for(Map.Entry<String, Company> entry : companies.entrySet()){
            Company comp = entry.getValue();
            comp.dividend();
        }

    }

    public static void marketAvailable(Player player){

        ArrayList<Map.Entry<String, Company>> entries = new ArrayList<>(companies.entrySet());

        entries.sort(Comparator.comparing(item -> -item.getValue().getMarketShares()));

        ArrayList<String> marketList = new ArrayList<>();

        marketList.add("§eMarket:");

        for (int i=0; i<entries.size(); i++){
            String b = entries.get(i).getKey();
            Company comp = Market.getCompany(b);
            double v = entries.get(i).getValue().getMarketShares();
            DecimalFormat df = new DecimalFormat("###,###,###");
            DecimalFormat df2 = new DecimalFormat("0.00");
            int i2 = i + 1;

            if (Market.getCompany(b).openTrade){
                marketList.add("§b"+i2+". §9"+b+": §7" +"inf"+" ("+"inf"+"%)");
            }else{
                marketList.add("§b"+i2+". §9"+b+": §7" +df.format(v)+" ("+df2.format((v/comp.getTotalShares().doubleValue()*100.0))+"%)");
            }

        }

        player.sendMessage(String.join("\n", marketList));
    }

    public static void SaveData(){
        Utils.writeSave("accounts", accounts);

        Utils.writeSave("companies", companies);

        Utils.writeSave("contractCompToComp", contractCompToComp);

        Utils.writeSave("contractCompToPlayer", contractCompToPlayer);

        Utils.writeSave("contractServerToComp", contractServerToComp);

        Utils.writeSave("contractPlayerToComp", contractPlayerToComp);

        Utils.writeSave("stockSigns", stockSigns);

        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "SignClick save Market completed!");


    }

    public static void restoreData(){
        accounts = Utils.readSave("accounts", new TypeToken<HashMap<UUID, Account>>(){}.getType(), new HashMap<>());

        companies = Utils.readSave("companies", new TypeToken<HashMap<String, Company>>(){}.getType(), new HashMap<>());

        stockSigns = Utils.readSave("stockSigns", new TypeToken<ArrayList<Location>>(){}.getType(), new ArrayList<>());

        contractCompToComp = Utils.readSave("contractCompToComp",
                new TypeToken<ArrayList<ContractCTC>>(){}.getType(), new ArrayList<>());

        contractCompToPlayer = Utils.readSave("contractCompToPlayer",
                new TypeToken<ArrayList<ContractCTP>>(){}.getType(), new ArrayList<>());

        contractServerToComp = Utils.readSave("contractServerToComp",
                new TypeToken<ArrayList<ContractSTC>>(){}.getType(), new ArrayList<>());

        contractPlayerToComp = Utils.readSave("contractPlayerToComp",
                new TypeToken<ArrayList<ContractPTC>>(){}.getType(), new ArrayList<>());



    }

    public static void runContracts(){
        ArrayList<Contract> new_ctc = new ArrayList<>();
        for (Contract c: contractCompToComp){
            boolean keep = c.runContract();
            if (keep){
             new_ctc.add(c);
            }

        }

        contractCompToComp = new_ctc;

        ArrayList<Contract> new_ctp = new ArrayList<>();
        for (Contract c: contractCompToPlayer){

            boolean keep = c.runContract();
            if (keep){
                new_ctp.add(c);
            }

        }

        contractCompToPlayer = new_ctp;

        ArrayList<Contract> new_ptc = new ArrayList<>();
        for (Contract c: contractPlayerToComp){

            boolean keep = c.runContract();
            if (keep){
                new_ptc.add(c);
            }

        }

        contractPlayerToComp = new_ptc;

        ArrayList<ContractSTC> new_stc = new ArrayList<>();
        for (ContractSTC c: contractServerToComp){

            boolean keep = c.runContract();
            if (keep){
                new_stc.add(c);
            }

        }

        contractServerToComp = new_stc;
    }

    public static void setContractComptoComp(String from, String to, double amount, int weeks, String reason){
        contractCompToComp.add(new ContractCTC(Market.getCompany(from), Market.getCompany(to), amount, weeks, reason));

    }

    public static void setContractComptoPlayer(String from, String toUUID, double amount, int weeks, String reason){
        Contract contract = new ContractCTP(Market.getCompany(from), UUID.fromString(toUUID),
                amount, weeks, reason);

        contractCompToPlayer.add(contract);

    }

    public static void setContractPlayertoComp(String fromUUID, String to, double amount, int weeks, String reason){
        contractPlayerToComp.add(new ContractPTC(UUID.fromString(fromUUID),
                Market.getCompany(to), amount, weeks, reason));

    }

    public static void setContractServertoComp(String to, double amount, int weeks, String reason, int delay){
        contractServerToComp.add(new ContractSTC(Market.getCompany(to), amount, weeks, reason, delay));

    }

    public static List<String> getBusinesses(){
        List<String> autoCompletes = new ArrayList<>();
        for (Company comp : companies.values()){
            autoCompletes.add(comp.getStockName());
        }
        return autoCompletes;
    }

    public static void getContracts(String stock_name, Player p){
        ArrayList<String> income = new ArrayList<>();
        ArrayList<String> outcome = new ArrayList<>();

        for (Contract c : contractCompToComp) {

            Company from = Market.getCompany(c.from());
            double amount = c.getAmount();
            Company to = Market.getCompany(c.to());
            int weeks = c.getWeeks();
            if (to.getStockName().equals(stock_name)){
                income.add("§aContract: from " + from.getStockName() + "(C) to " + to.getStockName() + "(C) amount: " + amount
                        + " for "+weeks+" weeks, " + "reason: "+c.getReason());
            }

            if (from.getStockName().equals(stock_name)){
                outcome.add("§cContract: from " + from.getStockName() + "(C) to " + to.getStockName() + "(C) amount: " + amount
                        + " for "+weeks+" weeks, "+ "reason: "+c.getReason());
            }

        }

        for (Contract c : contractCompToPlayer) {
            Company from = Market.getCompany(c.toString());
            double amount = c.getAmount();
            Account to = Market.getAccount(Bukkit.getServer().getPlayer(c.to()));
            int weeks = c.getWeeks();

            if (from.getStockName().equals(stock_name)){
                outcome.add("§cContract: from " + from.getStockName() + "(C) to " + to.getName() + "(P) amount: " + amount
                        + " for "+weeks+" weeks, "+ "reason: "+c.getReason());
            }

        }

        for (Contract c : contractPlayerToComp) {
            Account from = Market.getAccount(UUID.fromString(c.from()));
            double amount = c.getAmount();
            Company to = Market.getCompany(c.to());
            int weeks = c.getWeeks();

            if (to.getStockName().equals(stock_name)){
                income.add("§aContract: from " + from.getName() + "(P) to " + to.getStockName() + "(C) amount: " + amount
                        + " for "+weeks+" weeks, "+ "reason: "+c.getReason());
            }

        }

        for (ContractSTC c : contractServerToComp) {
            Company to = Market.getCompany(c.to());

            if (to.getStockName().equals(stock_name)){
                income.add("§aContract: from SERVER (S) to " + to.getStockName() + "(C) amount: " + c.getAmount()
                        + " for "+c.getWeeks()+" weeks, " + "reason: "+c.getReason() + " delay: "+c.getDelay());
            }
        }

        p.sendMessage("§aincome:");
        for (String s : income){
            p.sendMessage(s);
        }

        p.sendMessage("§coutgoing:");
        for (String s : outcome){
            p.sendMessage(s);
        }
    }

    public static void runStockCompare(){
        for (Location o: stockSigns){
            Sign s = (Sign) o.getBlock().getState();
            SignStock.update(s);
        }

        for (Company comp: companies.values()){
            comp.stockCompare();
        }
    }

    public static void runWeeklyCompanySalary(){
        for (Company comp : companies.values()){

            String countryName = comp.getCountry();
            Country country = CountryManager.getCountry(countryName);
            if (country == null){
                country = new CountryNull();
            }

            int total = 0;

            if (comp.type.equals("product")){

                comp.addBal(0+ country.getPolicyBonus(0, 4));
                comp.addBal(0+ country.getPolicyBonus(4, 2));
                total += (int) (country.getPolicyBonus(0, 4)+ country.getPolicyBonus(4, 2));

            }else if (comp.type.equals("building")){

                total+= 1000;
                comp.addBal(1000.0);

                comp.addBal(0+ country.getPolicyBonus(0, 6));
                comp.addBal(0+ country.getPolicyBonus(3, 5));
                comp.addBal(0+ country.getPolicyBonus(4, 5));
                total += (int) (country.getPolicyBonus(0, 6)+ country.getPolicyBonus(3, 5)+ country.getPolicyBonus(4, 5));
            }else if (comp.type.equals("military")){

                if (!country.isAboardMilitary()){
                    total+= 4000;
                    comp.addBal(4000.0);
                }

                comp.addBal(0+ country.getPolicyBonus(2, 5));
                comp.addBal(0+ country.getPolicyBonus(4, 4));
                total += (int) (country.getPolicyBonus(2, 5)+ country.getPolicyBonus(4, 4));
            }else if (comp.type.equals("transport")){
                comp.addBal(0+ country.getPolicyBonus(2, 6));
                comp.addBal(0+ country.getPolicyBonus(3, 3));
                comp.addBal(0+ country.getPolicyBonus(4, 1));
                total += (int) (country.getPolicyBonus(2, 6)+ country.getPolicyBonus(3, 3)+ country.getPolicyBonus(4, 1));
            }else if (comp.type.equals("bank")){
                comp.addBal(0+ country.getPolicyBonus(2, 7));
                comp.addBal(0+ country.getPolicyBonus(4, 0));
                total += (int) (country.getPolicyBonus(2, 7)+ country.getPolicyBonus(4, 0));
            }else if(comp.type.equals("real estate")){
                comp.addBal(0+ country.getPolicyBonus(3, 4));
                comp.addBal(0+country.getPolicyBonus(4, 3));
                total += (int) (country.getPolicyBonus(3, 4)+ country.getPolicyBonus(4, 3));
            }else{
                if (country != null){
                    comp.addBal(0+ country.getPolicyBonus(4, 6));
                    total += (int) country.getPolicyBonus(4, 6);
                }

            }

            if (!comp.openTrade){
                double value = country.getPolicyBonus(1, 0);
                total += (int) value;
                if (value > 0.0){
                    comp.addBal(value);
                }else{
                    comp.removeBal(value*-1);
                }
            }

            if (total > 0){
                country.withdraw(total);
            }else{
                country.deposit(total);
            }
        }
    }



}
