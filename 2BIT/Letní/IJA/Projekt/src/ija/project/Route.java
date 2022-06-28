package ija.project;

import java.util.ArrayList;
import java.util.List;

public class Route {

    private Stop lastStop = null;
    private Stop nextStop = null;
    private Street currentStreet = null;
    private List<Street> streets = new ArrayList<>();
    private List<Coordinate> path = new ArrayList<>();
    /**
     * {@link Coordinate} startovací pozice.
     */
    private Coordinate startPosition;
    /**
     * {@link Coordinate} realné pozice.
     */
    private Coordinate realPosition;
    /**
     * {@link Coordinate} naposledy navštíveného bodu.
     */
    private Coordinate lastPosition;
    /**
     * {@link Coordinate} následujícího bodu.
     */
    private Coordinate nextPosition;
    /**
     * {@link Coordinate} cíle.
     */
    private Coordinate finishPosition;
    /**
     * Proměná pro prácí s linearní interpolací.
     */
    private double t = 0;
    /**
     * Index naposled navštíveného bodu.
     */
    private int currentPositionIndex = 0;
    /**
     * Perioad ticku hodin v sekundách;
     */



    public Route(List<Coordinate> path, List<Street> streets) {
        this.path = path;
        this.streets = streets;
        this.streets = streets;
    }
    public void initRoute(){
        lastPosition = realPosition = startPosition = this.path.get(currentPositionIndex);
        nextPosition = this.path.get(currentPositionIndex+1);
        finishPosition = this.path.get(this.path.size()-1);
        for(Street s : streets)
        {
            if (s.begin().equals(lastPosition) || s.end().equals(lastPosition)) {
                currentStreet = s;
            }
        }
    }
    public int getCurrentPositionIndex() {
        return currentPositionIndex;
    }

    public void setCurrentPositionIndex(int currentPositionIndex) {
        System.out.println("setter router index");
        this.currentPositionIndex = currentPositionIndex;
    }

    /**
     * Číselně porovná Coordinate předchozího bodu s Coordinate následujícího a vrátí jejích vzdálenost.
     * @return {@code double}
     */
    public double getDistanceToNextFromLast() {
        return Coordinate.distance(getNextPosition(), getLastPosition());
    }
    /**
     * Číselně porovná Coordinate aktuální pozice s Coordinate následujícího a vrátí jejích vzdálenost.
     * @return {@code double}
     */
    public double getDistanceToNextFromReal() {
        return Coordinate.distance(getNextPosition(), getRealPosition());
    }
    /**
     * Číselně porovná Coordinate s Coordinate následujícího a vrátí jejích vzdálenost.
     * @param position souřadnice {@code Coordinate} na porovnání
     * @return {@code double}
     */
    public double getDistanceToNextFrom(Coordinate position) {
        return Coordinate.distance(getNextPosition(), position);
    }
    /**
     * Vypočítá směrový vektor z Coordinate předchozího bodu s Coordinate následujícího.
     * @return {@code Coordinate}
     */
    public Coordinate getDirectionToNextFromLast() {
        return Coordinate.direction(getNextPosition().getX() - getLastPosition().getX(), getNextPosition().getY() - getLastPosition().getY());
    }
    /**
     * Číselně porovná Coordinate s Coordinate následujícího a vrátí jejích vzdálenost.
     * @param position souřadnice {@code Coordinate} na porovnání
     * @return {@code double}
     */
    public double getDistance(Coordinate position) {
        return Coordinate.distance(getNextPosition(), position);
    }
    /**
     * Vrátí reálnou vzálenost dvou bodů v path.
     * @param positionA, souřadnice {@code Coordinate} na porovnání
     * @param positionB souřadnice {@code Coordinate} na porovnání
     * @return {@code double}
     */
    public double getDistanceFromTo(Coordinate positionA, Coordinate positionB) {
        boolean startToCount = false;
        double distance = 0;
        for (int i = 0; i < path.size()-1; i++) {
            if(path.get(i).equals(positionA))
                startToCount = true;
            if(startToCount)
            {
                distance += Coordinate.distance(path.get(i), path.get(i+1));
            }
            if(path.get(i+1).equals(positionB))
                return distance;
        }
        return distance;
    }
    /**
     * Vrátí reálnou vzálenost dvou bodů v path.
     * @param position, souřadnice {@code Coordinate} na porovnání
     * @return {@code double}
     */
    public double getDistanceFromRealToNext(Coordinate position) {
        boolean startToCount = false;
        double distance = Coordinate.distance(getRealPosition(), getNextPosition());
        for (int i = 0; i < path.size()-1; i++) {
            if(path.get(i).equals(getNextPosition()))
                startToCount = true;
            if(startToCount)
            {
                distance += Coordinate.distance(path.get(i), path.get(i+1));
            }
            if(path.get(i+1).equals(position))
                return distance;
        }
        return distance;
    }
    /**
     * Vrátí počáteční bod trasy.
     * @return {@code Coordinate}
     */
    public Coordinate getStartPosition() {
        return startPosition;
    }
    /**
     * Vrátí realnou pozici na trase.
     * @return {@code Coordinate}
     */
    public Coordinate getRealPosition() {
        return realPosition;
    }
    /**
     * Nastaví novou realnou pozici na trase.
     * @param newPosition souřadnice {@code Coordinate} nové pozice
     * @return {@code Coordinate}
     */
    public void setRealPosition(Coordinate newPosition) {
        realPosition = newPosition;
    }
    /**
     * Vrátí naposledy navštívený bod na trase.
     * @return {@code Coordinate}
     */
    public Coordinate getLastPosition() {
        return lastPosition;
    }
    /**
     * Vrátí následující bod na trase.
     * @return {@code Coordinate}
     */
    public Coordinate getNextPosition() {
        return nextPosition;
    }
    /**
     * Vrátí konečný bod na trasy.
     * @return {@code Coordinate}
     */
    public Coordinate getFinishPosition() {
        return finishPosition;
    }
    /**
     * Souřadnice trasy.
     * @return {@code List<Coordinate>}
     */
    public List<Coordinate> getPath() {
        return path;
    }
    /**
     * Slouží pouze pro volání jiné metody. Metoda je volaná z {@link Bus}.
     * @param speed souřadnice {@code Coordinate} rychlost jakou se má autobus pohyboat
     */
    public void move(double speed) {
        interpolateBetweenPoints(getLastPosition(), getNextPosition(), speed);
    }
    /**
     * Lineární interpolace mezi dvěma body. Slouží k vypočítání nové pozice autobusu na trati.
     * @param lastPosition souřadnice {@code Coordinate} počátečního bodu
     * @param nextPosition souřadnice {@code Coordinate} konečného bodu
     * @param speed souřadnice {@code Coordinate} rychlost jakou se má autobus pohyboat
     */
    private void interpolateBetweenPoints(Coordinate lastPosition, Coordinate nextPosition, double speed) {
        double x = lastPosition.getX()*(1-t) + nextPosition.getX()*t;
        double y = lastPosition.getY()*(1-t) + nextPosition.getY()*t;
        setRealPosition(new Coordinate(x,y));
        if(currentStreet!=null) {
            if (t <= 1) {
                double delay = currentStreet.getDelayValue();
                System.out.println(delay + " " + currentStreet.toString());
                if (delay<=0)
                    delay=0.1;
                t += (1 + (1 - MainController.getTimeSpeed()/1000)) / (getDistanceToNextFromLast() / (speed*delay));
            }
            if (t > 1) {
                t = 0;
                updateRouteInfo();
            }
        }
    }
    public void setT(double t) {
        this.t = t;
    }

    public double getT() {
        return t;
    }
    /**
     * Aktualizuje informace o trase.
     */
    public void updateRouteInfo() {
        for(Street s : streets)
        {
            if (s.begin().equals(nextPosition) || s.end().equals(nextPosition)) {
                currentStreet = s;
            }
        }
        currentPositionIndex++;
        lastPosition = path.get(currentPositionIndex);
        setRealPosition(lastPosition);
        if(currentPositionIndex + 1 == path.size())
            return;
        nextPosition = path.get(currentPositionIndex + 1);
    }


    public List<Street> getStreets() {
        return streets;
    }

    public void setLastPosition(Coordinate lastPosition) {
        this.lastPosition = lastPosition;
    }

    public void setNextPosition(Coordinate nextPosition) {
        this.nextPosition = nextPosition;
    }

    @Override
    public String toString() {
        return "Route{" +
                "path=" + path +
                '}';
    }
}
