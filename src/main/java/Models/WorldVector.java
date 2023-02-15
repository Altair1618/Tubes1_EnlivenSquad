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

    public WorldVector getAdd( WorldVector v)
    {
        return new WorldVector(x + v.x, y + v.y);
    }

    public void normalize()
    {
        double magnitude = Math.sqrt(x * x + y * y);

        x /= magnitude;
        y /= magnitude;
    }

    public WorldVector toNormalize()
    {
        WorldVector res = new WorldVector(x, y);
        
        res.normalize();

        return res;

    }

    public WorldVector mult(double constant)
    {
        return new WorldVector(x * constant, y * constant);
    }

    public WorldVector div(double constant)
    {
        return new WorldVector(x / constant, y / constant);
    }

    public boolean isZero()
    {
        return (Math.abs(x) < Double.MIN_VALUE && Math.abs(y) < Double.MIN_VALUE);
    }

    public WorldVector getAdjacent()
    {
        // rotate the vector 90 degrees counterclock wise
        return new WorldVector(-y, x);
    }

    public void rotateBy(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double rx = x * cos - y * sin;
        y = x * sin + y * cos;
        x = rx;
    }

    public WorldVector getRotatedBy(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new WorldVector(x * cos - y * sin, x * sin + y * cos);
    }

    public Double magnitude()
    {
        return Math.sqrt(x * x + y * y);
    }
    public Double dot(WorldVector other)
    {
        return x * other.x + y * other.y;
    }
    public Double getAngleTo(WorldVector other)
    {
        return (this.dot(other) / (this.magnitude() * other.magnitude())) * 180 / Math.PI;
    }
}
