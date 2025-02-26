package com.klanting.signclick.economy;

import org.bukkit.OfflinePlayer;

public class CountryNull extends Country{
    /*
    * Object in case a country would equal to Null.
    * To avoid many checks, by inheritance, when a country is Null, this object will be provided
    * This makes sure, that it provides the default information where needed
    * */

    public CountryNull() {
        super();
    }

    @Override
    public double getPolicyBonus(int id, int index){

        return 0.0;
    }

    @Override
    public double getStability(){
        return 100.0;
    }
}
