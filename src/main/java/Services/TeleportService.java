package Services;

import Models.*;
import java.util.*;

import Enums.ObjectTypes;

public class TeleportService {
    
    static UUID firedTeleportId = null;

    static public Boolean isTeleportAvailable(GameObject bot)
    {
        // mengembalikan true jika player dapat menggunakan teleport
        return bot.teleporterCount > 0 && bot.size >= 25;
    }

    static public Boolean isTeleportAvailable(GameObject bot, int sizeLimit)
    {
        // mengembalikan true jika player dapat menggunakan teleport dengan tambahan batas bawah size player yang diperbolehkan
        return bot.teleporterCount > 0 && bot.size >= sizeLimit;
    }

    static public Boolean isTeleportFired()
    {
        return firedTeleportId != null;
    }

    static public GameObject getFiredTeleport(GameState gameState, GameObject bot)
    {
        List<GameObject> temp = RadarService.getOtherObjects(gameState, ObjectTypes.TELEPORTER);
        
        for (GameObject teleporter : temp) {
            if (teleporter.id == firedTeleportId)
            {
                if (FieldService.isOutsideMap(gameState, teleporter.position, bot.size))
                {
                    firedTeleportId = null;
                    return null;

                }

                else
                {
                    return teleporter;
                }
            }  
        }

        return null;
    }

    static public List<GameObject> getCollapsingObjectsAfterTeleport(GameState gameState, GameObject bot, GameObject teleporter)
    {
        // mengembalikan semua objek yang akan collapse dengan player setelah memasuki wormhole (pair wormhole tidak diketahui sehingga perlu ditinjau semua untuk kasus terburuk)
   
        return RadarService.getCollapsingObjects(gameState, teleporter.position, bot.size);
    }

}
