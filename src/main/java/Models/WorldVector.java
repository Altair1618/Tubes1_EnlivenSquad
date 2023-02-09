package Models;

public class WorldVector {

    public double x;
    public double y;

    public WorldVector() {
        x = 0;
        y = 0;
    }

    public WorldVector(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public WorldVector(Position init, Position target)
    {
        this.x = target.x - init.x;
        this.y = target.y - target.x;
    }

    public double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public void add(WorldVector v)
    {
        x += v.x;
        y += v.y;
    }

    public WorldVector add(WorldVector v1, WorldVector v2)
    {
        return new WorldVector(v1.x + v2.x, v1.y + v2.y);
    }
}
