package Services;

import Models.*;
import java.util.*;

import Enums.ObjectTypes;

public class TeleportService extends ProjectileService {
    
    static public UUID firedTeleportId = null;
    static public int heading = 0;
    static public boolean isFired = false;
    static public int teleportSizeLimit = 50;
    static public int profitLimit = 15;

    static public void shoot(int angle)
    {
        isFired = true;
        
        heading = angle;
    }

    static public void teleport()
    {
        firedTeleportId = null;
        isFired = false;
        heading = 0;
    }

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
            if (teleporter.getHeading() == heading)
            {
                if (FieldService.isOutsideMap(gameState, teleporter.position, bot.size))
                {
                    teleport();
                    return null;

                }

                else
                {
                    return teleporter;
                }
            }  
        }  

        teleport();
        return null;
    }

    static public List<GameObject> getCollapsingObjectsAfterTeleport(GameState gameState, GameObject bot, GameObject teleporter, int sizeOffset)
    {
        // mengembalikan semua objek yang akan collapse dengan player setelah memasuki wormhole (pair wormhole tidak diketahui sehingga perlu ditinjau semua untuk kasus terburuk)
   
        return RadarService.getCollapsingObjects(gameState, teleporter.position, bot.size + sizeOffset);
    }


    static public WorldVector getAttackDirection(GameObject bot)
    {
        List<GameObject> players = RadarService.players;
        GameObject target = null;
        int currentTargetSize = 0;

        for (GameObject player : players)
        {
            if (player.size + PlayerService.sizeDifferenceOffset - 20 <= bot.size && currentTargetSize < player.size)
            {
                target = player;
                currentTargetSize = player.size;
            }
        }

        if (currentTargetSize != 0)
        {
            return new WorldVector(bot.position, target.position);
        }

        return new WorldVector();
    }

    static public boolean isTeleportSafe(GameObject bot, List<GameObject> collapsingObjects, boolean isAttacking)
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

       
        if (totalPreySize <= totalTorpedoDamage + (isAttacking? profitLimit : 0))
        {
            if (totalPreySize > totalTorpedoDamage) return !cloudFlag && !asteroidField;
            else return false;
        }
        

        return true;

    }

}
