package ija.project;

public class Coordinate {

    private final double x;
    private final double y;


    public static Coordinate create(double posX, double posY) {
        if (posX < 0 || posY < 0)
            return null;
        else
            return new Coordinate(posX, posY);
    }
    public Coordinate(double posX, double posY) {
        this.x = posX;
        this.y = posY;
    }

    /*
        Gettery
    */
    public double getX() {
        return  x;
    }
    public double getY() {
        return  y;
    }
    /*
        Statické metody
    */
    public static double length(double x, double y) { return Math.sqrt(x * x + y * y); }
    public static double distance(Coordinate a, Coordinate b) { return length(b.x - a.x, b.y - a.y); }
    public static Coordinate add(Coordinate a, Coordinate b) { return (new Coordinate(a.x + b.x, a.y + b.y)); }
    public static Coordinate direction(double x, double y) { return new Coordinate(x / length(x, y), y / length(x, y)); }

    @Override
    public boolean equals(Object obj) {
        if (obj == (null))
            return false;
        Coordinate obj0 = (Coordinate) obj;
        return getX() == obj0.getX() && getY() == obj0.getY();
    }

    @Override
    public String toString() {
        return "Coordination("+getX()+", "+getY()+")";
    }

    public double diffX(Coordinate c){
        return getX()-c.getX();
    }
    public double diffY(Coordinate c){
        return getY()-c.getY();
    }
}
