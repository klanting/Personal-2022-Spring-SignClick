package com.klanting.signclick.calculate;

import com.klanting.signclick.SignClick;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class SignIncome {
    private static ArrayList<Door> doorCooldown = new ArrayList<Door>();
    public static Map<Location, UUID> owner = new HashMap<>();

    public static void Open(Sign sign, Player player){
        if (isWallSign(sign.getType())){
            double amount = Integer.parseInt(sign.getLine(1));
            String receiver = sign.getLine(2);

            Block block = getAttachedBlock(sign.getBlock());
            if (block.getState().getData().toString().contains("DOOR")){

                Door door;
                BlockState DoorState;

                if (block.getRelative(BlockFace.DOWN).getState().getData().toString().contains("DOOR")){
                    door = (Door) block.getRelative(BlockFace.DOWN).getBlockData();
                    DoorState = block.getRelative(BlockFace.DOWN).getState();
                }else{
                    door = (Door) block.getBlockData();
                    DoorState = block.getState();
                }

                if (!doorCooldown.contains(door)){

                    if (SignClick.getEconomy().getBalance(player) >= amount) {
                        try {
                            Player target = Bukkit.getServer().getPlayer(receiver);
                            SignClick.getEconomy().depositPlayer(target, amount);

                        } catch (Exception e) {
                            for (OfflinePlayer target : Bukkit.getOfflinePlayers()) {
                                if (target.getName().equals(receiver)) {
                                    SignClick.getEconomy().depositPlayer(target, amount);
                                }
                            }
                        }

                        SignClick.getEconomy().withdrawPlayer(player, amount);
                        Boolean old = door.isOpen();
                        door.setOpen(!old);
                        DoorState.setBlockData(door);
                        DoorState.update();
                        doorCooldown.add(door);


                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SignClick.getPlugin(), new Runnable() {
                            public void run() {
                                door.setOpen(old);
                                DoorState.setBlockData(door);
                                DoorState.update();
                                doorCooldown.remove(door);

                            }
                        }, 20*5L);
                    }else{
                        player.sendMessage("§bYou have not enough money");
                    }
                }
            }


        }


    }

    public static boolean isWallSign(Material type) {
        switch (type) {
            case OAK_WALL_SIGN:
            case SPRUCE_WALL_SIGN:
            case BIRCH_WALL_SIGN:
            case JUNGLE_WALL_SIGN:
            case ACACIA_WALL_SIGN:
            case DARK_OAK_WALL_SIGN:
                return true;
            default:
                return false;
        }
    }

    public static Block getAttachedBlock(Block sign){ // Requires isSign
        BlockFace facing = getFacing(sign);
        return sign.getRelative(facing.getOppositeFace());
    }

    public static BlockFace getFacing(Block block) {
        BlockData data = block.getBlockData();
        BlockFace f = null;
        if (data instanceof Directional && data instanceof Waterlogged && ((Waterlogged) data).isWaterlogged()) {
            String str = ((Directional) data).toString();
            if (str.contains("facing=west")) {
                f = BlockFace.WEST;
            } else if (str.contains("facing=east")) {
                f = BlockFace.EAST;
            } else if (str.contains("facing=south")) {
                f = BlockFace.SOUTH;
            } else if (str.contains("facing=north")) {
                f = BlockFace.NORTH;
            }
        } else if (data instanceof Directional) {
            f = ((Directional) data).getFacing();
        }
        return f;
    }

    public static void Set(SignChangeEvent sign, Player player){
        if (getAttachedBlock(sign.getBlock()).getRelative(BlockFace.DOWN).getState().getData().toString().contains("DOOR")){
            if (!owner.containsKey(getAttachedBlock(sign.getBlock()).getRelative(BlockFace.DOWN).getLocation())){
                sign.setLine(0, "§b[sign_in]");
                sign.setLine(2, player.getName());
                owner.put(getAttachedBlock(sign.getBlock()).getRelative(BlockFace.DOWN).getLocation(), player.getUniqueId());
            }else {
                player.sendMessage("§bThis door is already locked");
            }
        }
    }

    public static Boolean Destroy(BlockBreakEvent event){
        if ((owner.containsKey(event.getBlock().getLocation()))||(owner.containsKey(event.getBlock().getRelative(BlockFace.DOWN).getLocation()))){
            Player player = event.getPlayer();
            if (owner.get(event.getBlock().getLocation()) == player.getUniqueId()){
                return false;
            }else{
                return true;
            }


        }else{

            if(event.getBlock().getState() instanceof org.bukkit.block.Sign){
                Sign sign = (Sign) event.getBlock().getState();
                if (sign.getLine(0).equals("§b[sign_in]")){
                    if ((sign.getLine(2).equals(event.getPlayer().getName())) || event.getPlayer().getName().equals("signclick.staff")){

                        Block door = getAttachedBlock(sign.getBlock());
                        try{
                            owner.remove(door.getLocation());
                        }catch (Exception e){

                        }

                        try{
                            owner.remove(door.getRelative(BlockFace.DOWN).getLocation());
                        }catch (Exception e){

                        }


                        return false;
                    }else{
                        return true;
                    }

                }else{
                    return false;
                }
            }else{
            return false;
            }
        }


    }


    public static Boolean OpenDoor(PlayerInteractEvent event){

        if ((owner.containsKey(event.getClickedBlock().getLocation()))||(owner.containsKey(event.getClickedBlock().getRelative(BlockFace.DOWN).getLocation()))) {
            Player player = event.getPlayer();
            if (owner.get(event.getClickedBlock().getLocation()) == player.getUniqueId()) {
                return false;
            } else {
                return true;
            }

        }else{
            return false;
        }


    }

}