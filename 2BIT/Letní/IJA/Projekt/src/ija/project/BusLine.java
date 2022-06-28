package ija.project;
import java.util.*;

public class BusLine {
    private String name;
    private final List<Stop> stops;
    private Route route;
    private Route defaultRoute;
    private List<Bus> buses;
    public BusLine(String name) {
        this.name = name;
        this.stops = new ArrayList<>();
        this.buses = new ArrayList<>();
    }

    public List<Bus> getBuses() {
        return buses;
    }

    public void setBuses(List<Bus> buses) {
        this.buses = buses;
    }

    public void addStop(Stop stop) {
        stops.add(stop);
    }
    /**
     * Zjisti jestli na {@link Coordinate} zastávka
     * @param position souřadnice {@link Coordinate} nové pozice
     * @return {@link Stop} if  {@code true};
     * else {@code null}
     */
    public Stop isStop(Coordinate position) {
        for (Stop stop: stops) {
            if(stop.getCoordinate().equals(position))
                return stop;
        }
        return null;
    }

    public List<Stop> getAllStops(){
        return stops;
    }
    public Stop getFirstStop(){
        return stops.get(0);
    }
    public Stop getLastStop(){
        return stops.get(stops.size()-1);
    }
    public String getName() {
        return name;
    }
    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
        for(Bus bus : getBuses()){
            List<Coordinate> newL = new ArrayList<>(route.getPath());
            Route r = new Route(newL, route.getStreets());
            r.initRoute();
            bus.setRoute(r);
        }
    }


    public Route getDefaultRoute() {
        return defaultRoute;
    }
    public void setDefaultRoute(Route route) {
        this.defaultRoute = route;
    }

    @Override
    public String toString() {
        return name;
    }
}
