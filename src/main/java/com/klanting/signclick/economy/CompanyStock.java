package com.klanting.signclick.economy;

import com.klanting.signclick.SignClick;

public class CompanyStock {
    /*
    * This class represents the economic information of a company
    * */

    private final String stockName;

    /*
    * Represents the share base value
    * */
    private double shareBase = 0.0;

    /*
    * Represents the amount of money in the company back account
    * */
    private double bal = 0.0;

    public double getShareBalance() {
        return shareBalance;
    }

    public double getSecurityFunds() {
        return securityFunds;
    }

    public double getSpendable() {
        return spendable;
    }

    public double getLastValue() {
        return lastValue;
    }

    public void setSecurityFunds(double securityFunds) {
        this.securityFunds = securityFunds;
    }

    public void setSpendable(double spendable) {
        this.spendable = spendable;
    }

    private double shareBalance = 0.0;

    private double securityFunds = 0.0;

    private double spendable = 0.0;

    public Integer getTotalShares() {
        return totalShares;
    }

    public void setTotalShares(Integer totalShares) {
        this.totalShares = totalShares;
    }

    private Integer totalShares = SignClick.getPlugin().getConfig().getInt("companyStartShares");

    public void setLastValue(double lastValue) {
        this.lastValue = lastValue;
    }

    public double getValue(){
        return getBal() + getShareBalance();
    }

    private double lastValue = 0.0;

    public CompanyStock(String stockName){
        this.stockName = stockName;
    }

    public double getShareBase() {
        return shareBase;
    }

    public double getBal() {
        return bal;
    }

    public boolean addBal(double amount) {

        Company comp = Market.getCompany(stockName);
        Country country = comp.country;

        double modifier = 0.0;
        if (country != null){
            modifier += country.getPolicyBonus(0, 3);
        }

        double modifier2 = 0.0;
        double sub_pct = 1.0;
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

        double modifier3 = (sub_pct+(double) comp.upgrades.get(0).getBonus()/100.0);

        this.bal += amount;
        changeBase();

        if (amount > 0){
            spendable += (0.2+ modifier)*amount;
        }
        securityFunds += (0.01*amount)*modifier3*(1.0+ modifier2);
        return true;
    }


    public boolean removeBal(double amount){
        if ((getBal()+ shareBalance >= amount) & (spendable >= amount)){
            this.bal -= amount;
            spendable -= amount;
            changeBase();
            return true;
        }
        return false;
    }

    public boolean addBalNoPoint(double bal) {
        this.bal += bal;
        changeBase();
        return true;
    }

    void addBooks(Double amount){
        shareBalance += amount;
        spendable += (0.2*amount);
    }

    void removeBooks(Double amount){
        shareBalance -= amount;
        spendable -= amount;
    }

    public void changeBase(){
        shareBase = (getBal()/getTotalShares()) / Market.calculateFluxChange(-10, 15);
    }

}
