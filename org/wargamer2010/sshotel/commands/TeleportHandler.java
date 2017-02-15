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
    private int price;
    private int priceOwner;
    private int minimumPrice;
    private boolean dynamicPrice;
    private String stateAccount;

    private TeleportHandler() {

    }

    public void setup(HashMap<String, String> map){
        this.price = Integer.parseInt(map.get("price"));
        this.priceOwner = Integer.parseInt(map.get("price"));
        this.stateAccount = map.get("state_account");
        this.dynamicPrice = Boolean.parseBoolean(map.get("dynamic_price"));
        this.minimumPrice = Integer.parseInt(map.get("minimum_price"));

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
        Storage storage = Storage.get();

        if(placesRented.size() == 0){
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
                double price = getPrice(shop, signShopPlayer);
                String message = ChatColor.GOLD + "[SignShop] " + ChatColor.WHITE + " The tp to the Hotel ";
                message += ChatColor.DARK_GREEN + hotel + ChatColor.WHITE + " will cost you : " + economyUtil.formatMoney(price);
                sender.sendMessage(message);
/*
                Map<String, String> messageParts = new LinkedHashMap<String, String>();
                messageParts.put("!hotel", hotel);
                messageParts.put("!price", Double.toString(price));
                signShopPlayer.sendMessage(SignShopConfig.getMessage("tp", "price_for_tp", messageParts));
                signShopPlayer.sendMessage(SignShopConfig.getMessage("price_for_tp", "tp", messageParts));*/

            }
            return true;
        }

        String hotel = args[0];



        List<Block> hotelSelected = storage.getShopsWithMiscSetting("Hotel", hotel);

        for(Block b : placesRented){
            for(Block h : hotelSelected)
                if (compareBlock(b, h)) {
                    Seller s = storage.getSeller(b.getLocation());
                    double price = getPrice(s, signShopPlayer);
                    String winner = "";
                    if(!signShopPlayer.hasMoney(price))
                        return true; //TODO error msg

                    if (s.getOwner().getName().compareTo(signShopPlayer.getName()) == 0) {
                        signShopPlayer.mutateMoney(price * -1);
                        winner = this.stateAccount;
                        Vault.getEconomy().depositPlayer(this.stateAccount, price);

                    } else {
                        signShopPlayer.mutateMoney(price * -1);
                        s.getOwner().mutateMoney(price);
                        winner = s.getOwner().getName();
                    }


                    SignShop.logTransaction(signShopPlayer.getName(), winner, "tp_hotel", "", economyUtil.formatMoney(price));

                    sender.teleport(b.getLocation());
                    return true;
                }
        }
        signShopPlayer.sendMessage(SignShopConfig.getError("no_rented_room_hotel", null));


        return true;
    }

    private double getPrice(Seller shop, SignShopPlayer player) {
        double price = 0;
        String sPrice = shop.getMisc("Price");
        if (shop.getOwner().getName().compareTo(player.getName()) == 0) {
            return this.priceOwner;
        }

        if (dynamicPrice) {
            String period = shop.getMisc("Period");
            double timeInDay = (double) SSHotelUtil.getPeriod(period) / (60 * 60 * 24);
            double dailyPrice = Double.parseDouble(sPrice) / timeInDay;
            price = Math.max(this.price - dailyPrice, minimumPrice);
        } else {
            price = this.price;
        }

        return price;
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
