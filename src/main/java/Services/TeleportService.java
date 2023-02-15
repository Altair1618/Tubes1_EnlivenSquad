package Services;

import Models.*;
import java.util.*;

import Enums.ObjectTypes;

public class TeleportService extends ProjectileService {
    
    static UUID firedTeleportId = null;
    static public int teleportSizeLimit = 25;
    static public int lowestTeleportSize = 20;
    static public int profitLimit = 15;

    static public Boolean isTeleportAvailable(GameObject bot)
    {
        // mengembalikan true jika player dapat menggunakan teleport
        return bot.teleporterCount > 0 && bot.size >= teleportSizeLimit;
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
        List<GameObject> temp = RadarService.getOtherObjects(ObjectTypes.TELEPORTER);
        
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

    static public List<GameObject> getCollapsingObjectsAfterTeleport(GameState gameState, GameObject bot, GameObject teleporter, int sizeOffset)
    {
        // mengembalikan semua objek yang akan collapse dengan player setelah memasuki wormhole (pair wormhole tidak diketahui sehingga perlu ditinjau semua untuk kasus terburuk)
   
        return RadarService.getCollapsingObjects(gameState, teleporter.position, bot.size + sizeOffset);
    }

    static public boolean isTeleportSafe(GameObject bot, List<GameObject> collapsingObjects)
    {
        int totalPreySize = 0;
        int maxPreySize = 0;
        int totalTorpedoDamage = 0;
        boolean cloudFlag = false;
        boolean asteroidField = false;

        for (GameObject obj : collapsingObjects)
        {
            ObjectTypes type = obj.getGameObjectType();
            if (type == ObjectTypes.PLAYER)
            {
                if (RadarService.isCollapsing(obj, bot))
                {
                    if (bot.size + totalPreySize > obj.size + PlayerService.sizeDifferenceOffset) 
                    {
                        totalPreySize += obj.size; 
                        
                        if (maxPreySize < obj.size) maxPreySize = obj.size;
                    }

                    else return false; 
                }

                else if (bot.size + totalPreySize <= obj.size + PlayerService.sizeDifferenceOffset)
                {
                    return false;
                }
            }

            else if (type == ObjectTypes.GASCLOUD)
            {
                cloudFlag = true;
            }

            else if (type == ObjectTypes.ASTEROIDFIELD)
            {
                asteroidField = true;
            }

            else if (type == ObjectTypes.TORPEDOSALVO)
            {
                if (isIncoming(bot, obj))
                {
                    totalTorpedoDamage += obj.size;

                    if (bot.size - totalTorpedoDamage <= Math.max(maxPreySize, teleportSizeLimit)) return false;
                }
            }
        }

        if (bot.size - totalTorpedoDamage <= Math.max(maxPreySize, teleportSizeLimit)) return false;

        if (totalPreySize <= totalTorpedoDamage + profitLimit)
        {
            if (totalPreySize > totalTorpedoDamage) return !cloudFlag && !asteroidField;
            else return false;
        }

        return true;
    }

}
