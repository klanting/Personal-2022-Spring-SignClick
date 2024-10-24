package com.company;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.klanting.signclick.economy.Company;
import com.klanting.signclick.economy.CountryManager;
import com.klanting.signclick.economy.Market;
import com.klanting.signclick.SignClick;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.TestTools;

import static org.junit.jupiter.api.Assertions.*;



class CompanyTests {


    private ServerMock server;
    private SignClick plugin;

    @BeforeEach
    public void setUp() {

        server = MockBukkit.mock();

        plugin = TestTools.setupPlugin(server);
    }

    @AfterEach
    public void tearDown() {

        MockBukkit.unmock();
        Market.clear();
    }

    @Test
    void companyCreate(){
        PlayerMock testPlayer = TestTools.addPermsPlayer(server, plugin);

        /*
        * Give player 40 million
        * */

        SignClick.getEconomy().depositPlayer(testPlayer, 40000000);
        assertTrue(SignClick.getEconomy().has(testPlayer, 40000000));

        Boolean succes = Market.addBusiness("TestCaseInc", "TCI", Market.getAccount(testPlayer));
        assertTrue(succes);
        SignClick.getEconomy().withdrawPlayer(testPlayer, 40000000);

        Company comp = Market.getBusiness("TCI");
        assertEquals(0, comp.get_value());
        assertEquals(1000000, Market.getAccount(testPlayer).shares.get("TCI"));

    }

    @Test
    void companyAddMoney(){
        Player testPlayer = server.addPlayer();

        Boolean succes = Market.addBusiness("TestCaseInc", "TCI", Market.getAccount(testPlayer));
        assertTrue(succes);


        Company comp = Market.getBusiness("TCI");

    }

    @Test
    void companySaveLoad(){
        Player testPlayer = server.addPlayer();

        Boolean succes = Market.addBusiness("TestCaseInc", "TCI", Market.getAccount(testPlayer));
        assertTrue(succes);

        Company comp = Market.getBusiness("TCI");
        comp.addBal(1000.0);

        plugin.onDisable();
        CountryManager.clear();
        Market.clear();
        plugin = TestTools.setupPlugin(server);

    }

    @Test
    void companyRunTicks(){
        Player testPlayer = server.addPlayer();

        Boolean succes = Market.addBusiness("TestCaseInc", "TCI", Market.getAccount(testPlayer));
        assertTrue(succes);

        Company comp = Market.getBusiness("TCI");
        comp.addBal(1000.0);

        server.getScheduler().performTicks(60*60*24*7*20+1);

    }
}

