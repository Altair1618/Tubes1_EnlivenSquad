package Services;

import Models.*;
import java.util.*;

import Enums.ObjectTypes;
import java.util.stream.*;

public class TeleportService extends ProjectileService {
    
    static public UUID firedTeleportId = null;
    static public int heading = 0;
    static public boolean isFired = false;
    static public int teleportSizeLimit = 25;
    static public int profitLimit = 0;
    static public boolean hasFound = false;

    static public void shoot(int angle)
    {
        System.out.println("Shooting teleporter");
        isFired = true;
        heading = angle;
        hasFound = false;
    }

    static public void teleport()
    {
        System.out.println("Deleting teleporter");
        isFired = false;
        heading = 0;
        hasFound = false;
        firedTeleportId = null;
    }

    static public Boolean isTeleportAvailable(GameObject bot)
    {
        // mengembalikan true jika player dapat menggunakan teleport
        return bot.teleporterCount > 0 && bot.size >= teleportSizeLimit;
    }

    static public Boolean isTeleportFired()
    {
        return isFired;
    }

    static public GameObject getFiredTeleport(GameState gameState, GameObject bot)
    {
        List<GameObject> temp = RadarService.getOtherObjects(ObjectTypes.TELEPORTER);
        
        for (GameObject teleporter : temp) {
            if ((!hasFound && teleporter.getHeading() == heading) || (hasFound && firedTeleportId == teleporter.id))
            {
                if (FieldService.isOutsideMap(gameState, teleporter.position, bot.size))
                {
                    teleport();
                    return null;

                }

                else
                {
                    hasFound = true;
                    firedTeleportId = teleporter.id;
                    return teleporter;
                }
            }  
        }  

        if (hasFound) teleport();
        return null;
    }


    static public List<GameObject> getIncomingTeleporter(GameObject bot, int biggestEnemySize)
    {
        return RadarService.getOtherObjects(ObjectTypes.TELEPORTER)
            .stream()
            .filter(teleporter -> RadarService.isCollapsing(bot, teleporter.position, biggestEnemySize + PlayerService.playerDangerRange))
            .collect(Collectors.toList());
    }

    static public boolean isTeleportDangerous(GameObject bot)
    {
    
        return false;
    }

    static public List<GameObject> getCollapsingObjectsAfterTeleport(GameState gameState, GameObject bot, GameObject teleporter, int sizeOffset)
    {
        // mengembalikan semua objek yang akan collapse dengan player setelah memasuki wormhole (pair wormhole tidak diketahui sehingga perlu ditinjau semua untuk kasus terburuk)
   
        return RadarService.getCollapsingObjects(gameState, teleporter.position, bot.size + sizeOffset);
    }

    static public List<GameObject> getCollapsingPlayersAfterTeleport(GameState gameState, GameObject bot, GameObject teleporter, int sizeOffset)
    {
        // mengembalikan semua objek yang akan collapse dengan player setelah memasuki wormhole (pair wormhole tidak diketahui sehingga perlu ditinjau semua untuk kasus terburuk)

        return RadarService.getCollapsingPlayers(gameState, teleporter.position, bot.size + sizeOffset);
    }


    static public WorldVector getAttackDirection(GameObject bot)
    {
        List<GameObject> players = RadarService.players;
        GameObject target = null;
        int currentTargetSize = 0;

        for (GameObject player : players)
        {
            if (player.size + PlayerService.sizeDifferenceOffset <= bot.size - 20
                    && currentTargetSize < player.size
                    && isPriorHit(bot, player, 60, bot.size))
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

    static public boolean isTeleportSafe(GameObject bot, GameObject teleporter, List<GameObject> collapsingObjects, List<GameObject> collapsingPlayers, boolean isAttacking)
    {
        System.out.print("Collapsing objects count : ");
        System.out.println(collapsingObjects.size());
        if (collapsingObjects.isEmpty())
        {
            if (isAttacking) return false;

            return true;
        }

        int totalPreySize = 0;
        int maxPreySize = 0;
        int totalTorpedoDamage = 0;
        boolean cloudFlag = false;

        for (GameObject player: collapsingPlayers)
        {
            System.out.println("Player near teleporter!");
            if (RadarService.isCollapsing(player, teleporter.position, bot.size + 20))
            {
                if (bot.size > player.size + PlayerService.sizeDifferenceOffset)
                {

                    totalPreySize += player.size;

                    if (maxPreySize < player.size) maxPreySize = player.size;
                }

                else return false; 
            }

            else if (bot.size <= player.size + PlayerService.sizeDifferenceOffset)
            {
                return false;
            }
        }

        for (GameObject obj : collapsingObjects)
        {

            ObjectTypes type = obj.gameObjectType;

           if (type == ObjectTypes.GASCLOUD)
           {
               cloudFlag = true;
           }

            else if (type == ObjectTypes.TORPEDOSALVO)
            {
                if (RadarService.isCollapsing(obj, teleporter.position, bot.size))
                {
                    totalTorpedoDamage += obj.size;

                    if (bot.size - totalTorpedoDamage <= Math.max(maxPreySize, teleportSizeLimit - 20)) return false;
                }
            }

            else if (type == ObjectTypes.FOOD)
            {
                if (RadarService.isCollapsing(obj, teleporter.position, bot.size))
                {
                    totalPreySize += obj.size;
                }
            }
        }

       
        if (totalPreySize <= totalTorpedoDamage + (isAttacking? profitLimit : 0))
        {
            if (totalPreySize > totalTorpedoDamage) return !cloudFlag && (isAttacking? maxPreySize > 0 : true);


            else return false;
        }

        if (isAttacking && maxPreySize == 0) return false;

        return true;

    }

}
