package Models;

import Enums.*;
import java.util.*;

public class GameObject {
  public UUID id;
  public Integer size;
  public Integer speed;
  public Integer currentHeading;
  public Position position;
  public ObjectTypes gameObjectType;
  public Integer effectsCode;
  public Integer torpedoSalvoCount;
  public Integer supernovaAvailable;
  public Integer teleporterCount;
  public Integer shieldCount;

  public GameObject(UUID id, Integer size, Integer speed, Integer currentHeading, Position position, ObjectTypes gameObjectType) {
    this.id = id;
    this.size = size;
    this.speed = speed;
    this.currentHeading = currentHeading;
    this.position = position;
    this.gameObjectType = gameObjectType;
    this.effectsCode = 0;
    this.torpedoSalvoCount = 0;
    this.supernovaAvailable = 0;
    this.teleporterCount = 0;
    this.shieldCount = 0;
  }

  public GameObject(UUID id, Integer size, Integer speed, Integer currentHeading, Position position, ObjectTypes gameObjectType, Integer effectsCode, Integer torpedoSalvoCount, int supernovaAvailable, int teleporterCount, int shieldCount) {
    this.id = id;
    this.size = size;
    this.speed = speed;
    this.currentHeading = currentHeading;
    this.position = position;
    this.gameObjectType = gameObjectType;
    this.effectsCode = effectsCode;
    this.torpedoSalvoCount = torpedoSalvoCount;
    this.supernovaAvailable = supernovaAvailable;
    this.teleporterCount = teleporterCount;
    this.shieldCount = shieldCount;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getSpeed() {
    return speed;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
  }

  public Position getPosition() {
    return position;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public ObjectTypes getGameObjectType() {
    return gameObjectType;
  }

  public void setGameObjectType(ObjectTypes gameObjectType) {
    this.gameObjectType = gameObjectType;
  }

  public int getEffectsCode() {
    return effectsCode;
  }

  public void setEffectsCode(int effectsCode) {
    this.effectsCode = effectsCode;
  }

  public int getTorpedoSalvoCount() {
    return torpedoSalvoCount;
  }

  public void setTorpedoSalvoCount(int torpedoSalvoCount) {
    this.torpedoSalvoCount = torpedoSalvoCount;
  }

  public int getSupernovaAvailable() {
    return supernovaAvailable;
  }

  public void setSupernovaAvailable(int supernovaAvailable) {
    this.supernovaAvailable = supernovaAvailable;
  }

  public int getTeleporterCount() {
    return teleporterCount;
  }

  public void setTeleporterCount(int teleporterCount) {
    this.teleporterCount = teleporterCount;
  }

  public int getShieldCount() {
    return shieldCount;
  }

  public void setShieldCount(int shieldCount) {
    this.shieldCount = shieldCount;
  }

  public static GameObject FromStateList(UUID id, List<Integer> stateList)
  {
    Position position = new Position(stateList.get(4), stateList.get(5));
    
    Integer length = stateList.size();
    Integer effectsCode = 0;
    Integer torpedoSalvoCount = 0;
    Integer supernovaAvailable = 0;
    Integer teleporterCount = 0;
    Integer shieldCount = 0;
    
    if (length >= 7)
    {
      effectsCode = stateList.get(6);
    }

    if (length >= 8)
    {
      torpedoSalvoCount = stateList.get(7);
    }

    if (length >= 9)
    {
      supernovaAvailable = stateList.get(8);
    }

    if (length >= 10)
    {
      teleporterCount = stateList.get(9);
    }

    if (length >= 11)
    {
      shieldCount = stateList.get(10);
    }

    return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position, ObjectTypes.valueOf(stateList.get(3)), effectsCode, torpedoSalvoCount, supernovaAvailable, teleporterCount, shieldCount);

  }
}
