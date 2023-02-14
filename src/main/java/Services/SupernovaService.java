package Services;
import Models.*;
import Enums.*;

import java.util.*;
import java.util.stream.*;

public class SupernovaService {
    static private int superNovaSize = 100; // asumsi besar ledakan supernova

    static public boolean isSupernovaAvailable(GameObject bot) {
        // True jika supernovaAvailable

        return (bot.supernovaAvailable == 1);
    }

    static public boolean isSupernovaPickupExist(GameState gameState) {
        // Mengecek apakah terdapat supernova pickup di world
        var SupernovaPickup = gameState.getGameObjects().stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP).collect(Collectors.toList());
        return !SupernovaPickup.isEmpty();
    }

    static public GameObject getSupernovaPickupObject(GameState gameState) {
        // I.S Supernova Pickup Exist
        var SupernovaPickup = gameState.getGameObjects().stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP).collect(Collectors.toList());
        return SupernovaPickup.get(0);
    }

    static public boolean isBotNearestfromPickup(GameState gameState, GameObject bot) {
        // I.S Supernova Pickup Exist
        List<GameObject> Players = gameState.getPlayerGameObjects();
        GameObject nearest = Players.get(0);
        GameObject pickup = getSupernovaPickupObject(gameState);

        for (int i = 1; i < Players.size(); i++) {
            if (RadarService.getRealDistance(Players.get(i), pickup) < RadarService.getRealDistance(nearest, pickup)) {
                nearest = Players.get(i);
            }
        }

        return nearest == bot;
    }

    static public boolean isSupernovaBombExist(GameState gameState) {
        // Mengecek apakah terdapat bom supernova di world
        var SupernovaPickup = gameState.getGameObjects().stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB).collect(Collectors.toList());
        return !SupernovaPickup.isEmpty();
    }

    static public List<GameObject> getSupernovaBombs(GameState gameState) {
        // I.S Supernova Pickup Exist
        var bombList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB)
                .collect(Collectors.toList());

        return bombList;
    }

    static public boolean isSupernovaNearPlayer(GameState gameState, GameObject bot) {
        // Mengecek apakah sekitar supernova yang ditembak dekat player

        List<GameObject> supernova = getSupernovaBombs(gameState);

        if (supernova.size() == 0) {
            return false;
        }

        List<GameObject> playersList = PlayerService.getOtherPlayerList(gameState, bot);

        for (int i = 0; i < playersList.size(); i ++) {
            if (RadarService.isCollapsing(supernova.get(0), playersList.get(i), 50)) {
                return true;
            }
        }

        return false;
    }

    static public boolean isSuperNovaOutsideMap(GameState gameState, GameObject superNovaBomb, int offset)
    {
        return FieldService.isOutsideMap(gameState, superNovaBomb, offset);
    }

    static public boolean isIncoming(GameObject bot, GameObject supernova) {
        // Mengembalikan true jika supernova mengarah ke bot
        // perhitungan dengan konsep segitiga

        int supernovaHeading = supernova.getHeading();
        int headingBetween = RadarService.getHeadingBetween(supernova, bot);
        double distance = RadarService.getRealDistance(supernova, bot);
        
        // 100 = radius ledakan supernova
        int radius = bot.getSize() + superNovaSize;

        // offset = asin(radius / jarak supernova ke bot)
        double offSet = RadarService.toDegrees(Math.asin(radius / distance));

        if (((angleBetween(supernovaHeading, headingBetween + offSet)
                + angleBetween(supernovaHeading, headingBetween - offSet)) <= 2 * offSet)) {
            return true;
        }

        return false;
    }

    static public List<GameObject> getIncomingSupernova(GameState gameState, GameObject bot) {
        // Mendapat supernova yang incoming to bot

        List<GameObject> incomingSupernova = new ArrayList<GameObject>();
        List<GameObject> supernovaList = RadarService.getOtherObjects(gameState, bot, ObjectTypes.SUPERNOVABOMB);

        // jika ada supernova di map
        if (!supernovaList.isEmpty()) {
            if (isIncoming(bot, supernovaList.get(0))) {
                incomingSupernova.add(supernovaList.get(0));
            }
        }

        return incomingSupernova;
    }

    static public WorldVector nextHeadingAfterSupernova(GameObject bot, GameObject incomingSupernova) {
        // Mendapat angle heading terbaik mempertimbangkan
        // supernova yang menuju ke bot dengan menggunakan vector

        WorldVector res = RadarService.degreeToVector(incomingSupernova.getHeading());

        // random
        int tmp = (int) (Math.random() * 1) + 1;

        if (tmp == 1) {
            res.getRotatedBy(90);
        } else {
            res.getRotatedBy(-90);
        }

        return res;
    }
    
    static private double angleBetween(double angle1, double angle2) {
        // Menghitung angle dari 2 sudut

        return Math.abs((angle1 - angle2 + 180 + 360) % 360 - 180);
    }
}
