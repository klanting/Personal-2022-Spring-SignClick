package com.klanting.signclick.calculate;

import com.klanting.signclick.economy.Market;
import com.klanting.signclick.SignClick;


import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import java.text.DecimalFormat;

import static com.klanting.signclick.economy.Market.stock_signs;

public class SignStock {
    /*
    * SignStock is a tool to track the stock value of a company on a sign
    * */
    public static void set(SignChangeEvent sign, Player player){
        String Sname = sign.getLine(1);
        Sname = Sname.toUpperCase();
        if (Market.hasBusiness(Sname)){
            stock_signs.add(sign.getBlock().getLocation());

            sign.setLine(0, "§b[stock]");
            sign.setLine(1, Sname);
            DecimalFormat df = new DecimalFormat("###,##0.00");
            sign.setLine(2, df.format(Market.getBusiness(Sname).stockCompareGet()));

            Market.getBusiness(Sname).addBal(100000.0);
            SignClick.getEconomy().withdrawPlayer(player, 100000);
        }else{
            player.sendMessage("§b not a valid company");
        }


    }

    public static void update(Sign sign){
        String stock_name = sign.getLine(1);
        if (Market.hasBusiness(stock_name)){

            DecimalFormat df = new DecimalFormat("###,##0.00");
            DecimalFormat df2 = new DecimalFormat("###,###,###");
            double pct = Market.getBusiness(stock_name).stockCompareGet();
            String color;
            if (pct < 0){
                color = "§c";
            }else{
                color = "§a";
            }
            sign.setLine(2, color + df.format(pct));
            sign.setLine(3, df2.format(Market.getBusiness(stock_name).getValue()));
            sign.update();
        }



    }

    public static void delete(Sign sign){
        try{
            stock_signs.remove(sign.getBlock().getLocation());
        }catch (Exception e){

        }

    }
}
