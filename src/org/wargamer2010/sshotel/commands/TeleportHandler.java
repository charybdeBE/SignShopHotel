package org.wargamer2010.sshotel.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.commands.ICommandHandler;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.sshotel.RoomRegistration;

import java.util.*;

/**
 * Created by laurent on 14.02.17.
 */
public class TeleportHandler implements ICommandHandler {

    private static ICommandHandler instance = new TeleportHandler();

    private TeleportHandler() {

    }

    public void setup(HashMap<String, String> map) {
    }

    public static ICommandHandler getInstance() {
        return instance;
    }

    @Override
    public boolean handle(String command, String[] args, SignShopPlayer signShopPlayer) {
        Player sender = signShopPlayer.getPlayer();
        if (!signShopPlayer.isOp() && !sender.hasPermission("sshotel.tp")) {
            return true;
        }

        List<Block> placesRented = RoomRegistration.getRentsForPlayer(signShopPlayer);
        List<Block> placesOwned = RoomRegistration.getHousesForPlayer(signShopPlayer);
        Storage storage = Storage.get();

        if (placesRented.size() <= 0 && placesOwned.size() <= 0) {
            signShopPlayer.sendMessage(SignShopConfig.getError("no_rented_room", null));
            return true;
        }
        HashMap<String, Block> already = new HashMap<>(); //This is to avoid double entry in the same hotel
        for (Block b : placesRented) {
            Seller shop = storage.getSeller(b.getLocation());
            String hotel = shop.getMisc("Hotel").toLowerCase();
            if (already.containsKey(hotel))
                continue;
            already.put(hotel, b);
        }
        for (Block b : placesOwned) {
            Seller shop = storage.getSeller(b.getLocation());
            String city = shop.getMisc("City").toLowerCase();
            if (already.containsKey(city))
                continue;
            already.put(city, b);
        }

        if (args.length == 0) {
            String message = ChatColor.GOLD + "[SignShop] " + ChatColor.WHITE + "Villes et Hotels disponibles :" + ChatColor.DARK_GREEN;
            for (String place : already.keySet()) {
                message += " " + place + "("+getCost(already.get(place), sender)+" charbon de bois)";
            }
            sender.sendMessage(message);
            return true;
        }

        String place = args[0].toLowerCase();

        if(already.containsKey(place)){
            int cost = getCost(already.get(place), sender);
            if(countNumberOfItem(sender.getInventory(), Material.CHARCOAL) >= cost){
                Inventory after = removeNumberFromInventory(sender.getInventory(), Material.CHARCOAL, cost);
                sender.updateInventory();
                sender.teleport(already.get(place).getLocation());
            } else {
                sender.sendMessage(ChatColor.GOLD + "[SignShop] " + ChatColor.WHITE + "Pas assez de charbon" + ChatColor.DARK_GREEN);
            }
           return true;
        }

        signShopPlayer.sendMessage(SignShopConfig.getError("no_rented_room_hotel", null));


        return true;
    }

    private boolean compareBlock(Block a, Block b) {
        if (a.getX() == b.getX()) {
            if (a.getY() == b.getY()) {
                if (a.getZ() == b.getZ()) {
                    return true;
                }
            }
        }
        return false;
    }


    private int getCost(Block shop, Player p){
        Location l1 = p.getLocation();
        Location l2 = shop.getLocation();
        double dist = l1.distance(l2);
        return (int) Math.floor(dist / 64) + 1;
    }

    //TODO export to library
    private int countNumberOfItem(Inventory inventory, Material item){
        int res = 0;
        for (ItemStack stackk : inventory) {
            if (stackk != null && stackk.getType() == item) {
                res += stackk.getAmount();
            }
        }
        return res;
    }

    private Inventory removeNumberFromInventory(Inventory inventory, Material item, int count){
        int reminder = count;
        for (ItemStack stackk : inventory) {
            if (stackk != null && stackk.getType() == item) {
                if (reminder > stackk.getAmount()) {
                    reminder -= stackk.getAmount();
                    stackk.setAmount(0);
                } else if (reminder > 0) {
                    stackk.setAmount(stackk.getAmount() - reminder);
                    reminder = 0;
                }
            }
        }
        return inventory;
    }
}
