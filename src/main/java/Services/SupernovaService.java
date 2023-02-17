package Services;
import Models.*;
import Enums.*;

import java.util.*;
import java.util.stream.*;

public class SupernovaService extends ProjectileService {
    static public int superNovaSize = 100; // asumsi besar ledakan supernova
    static public boolean isDetonated = false; // apakah supernova bomb sudah didetonate
    static public boolean isFired = false; // apakah supernova bomb sudah ditembak

    static public boolean isSupernovaAvailable(GameObject bot) {
        // True jika supernovaAvailable

        return (bot.supernovaAvailable == 1);
    }

    static public boolean isSupernovaPickupExist(GameState gameState, GameObject bot, int radarRadius) {
        // Mengecek apakah terdapat supernova pickup di world
        var SupernovaPickup = RadarService.getOtherObjects(ObjectTypes.SUPERNOVAPICKUP, bot, radarRadius);
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
            if (RadarService.isCollapsing(supernova.get(0), playersList.get(i), superNovaSize)) {
                return true;
            }
        }

        return false;
    }

    static public boolean isSuperNovaOutsideMap(GameState gameState, GameObject superNovaBomb, int offset)
    {
        return FieldService.isOutsideMap(gameState, superNovaBomb, offset);
    }

    static public List<GameObject> getIncomingSupernova(GameState gameState, GameObject bot) {
        // Mendapat supernova yang incoming to bot

        return RadarService.getOtherObjects(ObjectTypes.SUPERNOVABOMB).stream().filter(item -> isIncoming(bot, item, superNovaSize)).collect(Collectors.toList());

    }
    
}
