package org.wargamer2010.signshop.operations;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.sshotel.RoomRegistration;
import org.wargamer2010.sshotel.util.SSHotelUtil;

public class HouseSign implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        Block bDoor = SSHotelUtil.getHotelPartFromBlocklist(ssArgs.getActivatables().get());
        if(bDoor == null) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("need_door", ssArgs.getMessageParts()));
            return false;
        }

        Sign sign = (Sign)ssArgs.getSign().get().getState();

        ssArgs.getPrice().set(SSHotelUtil.getNumberFromFourthLine(ssArgs.getSign().get()));

        String city = sign.getLine(1);
        if(city == null || SSHotelUtil.trimBrackets(city).isEmpty()) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("invalid_city", ssArgs.getMessageParts()));
            return false;
        }

        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        Block door = SSHotelUtil.getHotelPartFromBlocklist(ssArgs.getActivatables().get());
        if(door == null)
            return false;
        ssArgs.setMessagePart("!city", ssArgs.miscSettings.get("City"));
        ssArgs.setMessagePart("!nr", ssArgs.miscSettings.get("Nr"));
        SignShopPlayer player = ssArgs.getPlayer().get();

        if(RoomRegistration.getRoomByDoor(door) == null) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("no_door", ssArgs.getMessageParts()));
            return false;
        }

        Seller seller = Storage.get().getSeller(ssArgs.getSign().get().getLocation());
        SignShopPlayer renter = RoomRegistration.getPlayerFromShop(seller);

        if(renter != null)
            ssArgs.getPrice().set(economyUtil.parsePrice(seller.getMisc("Price")));
        else
            ssArgs.getPrice().set(SSHotelUtil.getNumberFromFourthLine(ssArgs.getSign().get()));

        if(renter != null && renter.compareTo(ssArgs.getPlayer().get())) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("already_buy_self", ssArgs.getMessageParts()));
            return false;
        } else if(renter != null) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("already_buy_other", ssArgs.getMessageParts()));
            return false;
        }


        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Block door = SSHotelUtil.getHotelPartFromBlocklist(ssArgs.getActivatables().get());
        if(door == null)
            return false;
        ssArgs.setMessagePart("!city", ssArgs.miscSettings.get("City"));
        ssArgs.setMessagePart("!nr", ssArgs.miscSettings.get("Nr"));

        Seller seller = Storage.get().getSeller(ssArgs.getSign().get().getLocation());

        Double fPrice = SSHotelUtil.getNumberFromFourthLine(ssArgs.getSign().get());
        seller.addMisc("Price", fPrice.toString());
        ssArgs.getPrice().set(fPrice);

        RoomRegistration.setPlayerForShop(seller, ssArgs.getPlayer().get());


        return true;
    }
}
