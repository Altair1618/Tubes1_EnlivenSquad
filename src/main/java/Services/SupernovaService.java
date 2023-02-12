package Services;
import Models.*;
import Enums.*;
import java.util.*;
import java.util.stream.*;

public class SupernovaService {


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

    static public GameObject getNearestPlayerFromSupernovaPickup(GameState gameState) {
        // I.S Supernova Pickup Exist
        var distanceList = gameState.getPlayerGameObjects()
                .stream()
                .sorted(Comparator
                        .comparing(item -> RadarService.getRealDistance(item.size, 0, RadarService.getDistanceBetween(getSupernovaPickupObject(gameState), item))))
                .collect(Collectors.toList());

        return distanceList.get(0);
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
            if (RadarService.isCollapsing(supernova.get(0), playersList.get(i), 30)) {
                return true;
            }
        }

        return false;
    }
}
