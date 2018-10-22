package org.wargamer2010.sshotel.commands;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.commands.ICommandHandler;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.sshotel.RoomRegistration;
import org.wargamer2010.sshotel.util.SSHotelUtil;

import java.util.*;

/**
 * Created by laurent on 14.02.17.
 */
public class TeleportHandler implements ICommandHandler {

    private static ICommandHandler instance = new TeleportHandler();

    private TeleportHandler() {

    }

    public void setup(HashMap<String, String> map){
    }

    public static ICommandHandler getInstance() {
        return instance;
    }

    @Override
    public boolean handle(String command, String[] args, SignShopPlayer signShopPlayer) {
        Player sender = signShopPlayer.getPlayer();
        if(!signShopPlayer.isOp() && !sender.hasPermission("sshotel.tp")){
            return true;
        }

        List<Block> placesRented = RoomRegistration.getRentsForPlayer(signShopPlayer);
        List<Block> placesOwned = RoomRegistration.getHousesForPlayer(signShopPlayer);
        Storage storage = Storage.get();

        if(!placesRented.size() && !placesOwned.size()){
            signShopPlayer.sendMessage(SignShopConfig.getError("no_rented_room", null));
            return true;
        }
        if(args.length == 0){
            HashMap<String, Integer> already = new HashMap<>(); //This is to avoid double entry in the same hotel
            for(Block b : placesRented){
                Seller shop = storage.getSeller(b.getLocation());
                String hotel = shop.getMisc("Hotel");
                if(already.containsKey(hotel))
                    continue;
                already.put(hotel, 1);
                String message = ChatColor.GOLD + "[SignShop] " + ChatColor.WHITE + "  Hotel disponibles :" + ChatColor.DARK_GREEN + hotel;
                sender.sendMessage(message);
            }
            for(Block b : placesOwned){
                Seller shop = storage.getSeller(b.getLocation());
                String city = shop.getMisc("City");
                if(already.containsKey(city))
                    continue;
                already.put(hotel, 1);
                String message = ChatColor.GOLD + "[SignShop] " + ChatColor.WHITE + "  Villes disponibles :" + ChatColor.DARK_GREEN + city;
                sender.sendMessage(message);
            }
            return true;
        }

        String place = args[0];

        List<Block> places = new ArrayList(placesOwned, placesRented);
        List<Block> hotelSelected = storage.getShopsWithMiscSetting("Hotel", place);
        List<Block> houseSelected = storage.getShopsWithMiscSetting("House", place);

        List<Block> selected = new ArrayList(hotelSelected, houseSelected);


        for(Block b : places){
            for(Block h : selected) {
                if (compareBlock(b, h)) {
                    sender.teleport(b.getLocation());
                    return true;
                }
            }
        }
        signShopPlayer.sendMessage(SignShopConfig.getError("no_rented_room_hotel", null));


        return true;
    }

    private boolean compareBlock(Block a, Block b){
        if(a.getX() == b.getX()){
            if(a.getY() == b.getY()){
                if(a.getZ() == b.getZ()){
                    return true;
                }
            }
        }
        return false;
    }
}
