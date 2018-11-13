
package org.wargamer2010.sshotel;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.SSTimeUtil;
import org.wargamer2010.sshotel.timing.RoomExpiration;
import org.wargamer2010.sshotel.util.SSHotelUtil;

public class RoomRegistration {
    private RoomRegistration() {

    }

    public static int registerRoom(Block door, String hotel) {
        Storage storage = Storage.get();
        if(!storage.getShopsByBlock(door).isEmpty())
            return -1;

        return (storage.getShopsWithMiscSetting("Hotel", hotel).size() + 1);
    }

    public static int registerHouse(Block door, String city) {
        Storage storage = Storage.get();
        if(!storage.getShopsByBlock(door).isEmpty())
            return -1;

        return storage.getShopsWithMiscSetting("City", city).size() + 1;
    }


    public static int getRoomNumber(Seller seller) {
        if(seller.hasMisc("RoomNr")) {
            try {
                return Integer.parseInt(seller.getMisc("RoomNr"));
            } catch(NumberFormatException ex) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public static Seller getRoomByDoor(Block door) {
        List<Seller> sellers = Storage.get().getShopsByBlock(door);
        if(sellers.isEmpty())
            return null;
        else
            return sellers.get(0);
    }

    public static SignShopPlayer getPlayerFromShop(Seller seller) {
        if(seller.hasMisc("Renter")) {
            String renter = seller.getMisc("Renter");
            if(renter == null || renter.isEmpty())
                return null;
            return PlayerIdentifier.getPlayerFromString(renter);
        }
        return null;
    }

    public static void setPlayerForShop(Seller seller, SignShopPlayer player, Boolean house) {
        String playerString = "";
        if(player != null)
            playerString = player.getName();
        if(house){
            seller.addMisc("Owner", playerString);
        } else {
            seller.addMisc("Renter", playerString);
        }

        Sign sign = (Sign) seller.getSign().getState();

        if(player != null)
            sign.setLine(3, (ChatColor.DARK_GREEN + player.getName()));
        else
            sign.setLine(3, seller.getMisc("Price"));
        sign.update();
        Storage.get().SafeSave();
    }

    public static int getAmountOfRentsForPlayer(SignShopPlayer player) {
        return getRentsForPlayer(player).size();
    }


    public static List<Block> getRentsForPlayer(SignShopPlayer player) {
        return Storage.get().getShopsWithMiscSetting("Renter", player.getName());
    }

    public static List<Block> getHousesForPlayer(SignShopPlayer player) {
        return Storage.get().getShopsWithMiscSetting("Owner", player.getName());
    }

    /**
     * Returns a string representation of the time left for the current Hotel room
     * Or returns N/A if the room is not rented out
     *
     * @param seller
     * @return String representation of the time left
     */
    public static String getTimeLeftForRoom(Seller seller) {
        if(seller == null)
            return "N/A";
        RoomExpiration roomex = SSHotelUtil.getRoomExpirationFromSeller(seller);
        if(roomex == null)
            return "N/A";
        int left = SignShop.getTimeManager().getTimeLeftForExpirable(roomex.getEntry());
        if(left == -1)
            return "N/A"; // TODO: Handle this somewhere else?
        return SSTimeUtil.parseTime(left);
    }
}
