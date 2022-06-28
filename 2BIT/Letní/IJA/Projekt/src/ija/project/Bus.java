package ija.project;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import java.time.LocalTime;
import java.util.*;
/**
 * Třída reprezentující autobus jezdící po cestě.
 * @author Adam Woska
 */
public class Bus implements Drawable, TickUpdate {

    private Route route;
    private BusLine busLine;
    private double speed;
    private List<Shape> guiElements;
    private int lastStopIndex = 0;
    private Stop lastStop = null;
    private Stop startStop = null;
    private Stop endStop = null;
    private Stop nextStop = null;
    private LocalTime startTime;
    private boolean startBus = false;
    private Circle circle;
    public Bus(Route route, BusLine busLine, double speed,String startTime) {
        this.startTime = LocalTime.parse(startTime);
        this.route = route;
        this.busLine = busLine;
        this.speed = speed;
        this.guiElements = new ArrayList<>();

        /*guiElements.add(new Circle(0, 0, 15, Color.CORNFLOWERBLUE));
        moveTo(route.getStartPosition());
        lastStop = busLine.getAllStops().get(lastStopIndex);
        nextStop = busLine.getAllStops().get(lastStopIndex + 1);*/
    }
	/**
     * Inicializace proměnných.
     */
    public void initBus(){
        circle = new Circle(0, 0, 15, Color.TRANSPARENT);
        guiElements.add(circle);
        moveTo(route.getStartPosition());
        lastStop = busLine.getAllStops().get(lastStopIndex);
        nextStop = busLine.getAllStops().get(lastStopIndex + 1);
        startStop = lastStop = busLine.getAllStops().get(lastStopIndex);
        System.out.println("FFF");

    }
	/**
     * Zobrazí na daném místě graficky objekt.
     * @param position souřadnice {@Code Coordinate}
     */
    private void moveTo(Coordinate position) {
        for (Shape s : guiElements) {
            s.setTranslateX(position.getX());
            s.setTranslateY(position.getY());
        }
    }
	/**
     * Vrací Circle - grafický prvek reprezentující autobus.
     * @return Circle
     */
    public Circle getCircle() {
        return circle;
    }
	/**
     * Vrací autobusovou linku BusLine, do které autobus patří.
     * @return BusLine
     */
    public BusLine getBusLine() {
        return busLine;
    }
	/**
     * Vrací route.
     * @return Route
     */
    public Route getRoute() {
        return route;
    }
	
    public void setRoute(Route route) {
        this.route = route;
    }
	
    public Stop getLastStop() {
        return lastStop;
    }
    public Stop getNextStop() {
        return nextStop;
    }
    public int getLastStopIndex() {
        return lastStopIndex;
    }
    public double getDistanceToNextStopFromPreviousStop() {
        return route.getDistanceFromTo(lastStop.getCoordinate(), nextStop.getCoordinate());
    }
    public double getDistanceToNextStopFromRealPosition() {
        return route.getDistanceFromRealToNext(nextStop.getCoordinate());
    }
    public double getDistanceToStopFromStart(int index) {
        return route.getDistanceFromTo(startStop.getCoordinate(), getBusLine().getAllStops().get(index).getCoordinate());
    }
    public double getDistanceToEndFromStart() {
        return route.getDistanceFromTo(startStop.getCoordinate(), getBusLine().getAllStops().get(getBusLine().getAllStops().size()-1).getCoordinate());
    }
    public LocalTime getStartTime() {
        return startTime;
    }
    public double calculateArriveTime(int index)
    {
        //t = v/s
        double s = getDistanceToStopFromStart(index);
        double v = speed/(MainController.getTimeSpeed()/1000);
        double t = s/v;
        return t;
    }
    public double calculateDelayTime(int index)
    {
        //t = v/s
        double s = getDistanceToStopFromStart(index);
        double delay = busLine.getAllStops().get(index).getStreet().getDelayValue();
        if (delay<=0)
            delay=0.1;
        double v = (speed*delay)/(MainController.getTimeSpeed()/1000);
        double t = s/v;
        return Math.abs(calculateArriveTime(index)-t);
    }
    public double[] calculateDelayArr()
    {
        double[] tmp = new double[busLine.getAllStops().size()];

        for(int i = 1; i < busLine.getAllStops().size(); i++)
        {
            tmp[i-1] = calculateDelayTime(i);
        }
        return tmp;
    }
    @Override
    public void tick(LocalTime time) {
        if(startBus == false)
        {
            if(time.equals(startTime)) {
                startBus = true;
                getCircle().setFill(Color.CORNFLOWERBLUE);
            }else {
                return;
            }
        }

        if(route.getRealPosition().equals(route.getFinishPosition()))
            return;
        route.move(speed);
        moveTo(route.getRealPosition());
        Stop tmp = busLine.isStop(route.getRealPosition());
        if(tmp != null && tmp != lastStop)
        {
            lastStop = tmp;
            lastStopIndex++;
            if(lastStopIndex + 1 >= getBusLine().getAllStops().size())
                return;
            nextStop = getBusLine().getAllStops().get(lastStopIndex + 1);
        }
    }
    @Override
    public List<Shape> getGui() {
        return guiElements;
    }

    public void setBusLine(BusLine busLine) {
        this.busLine = busLine;
    }


    public void setRouteFromBusline() {
        List<Coordinate> newList = new ArrayList<>(getBusLine().getDefaultRoute().getPath());
        //Route r = new Route(newList);
        Route r = new Route(getBusLine().getRoute().getPath(), getBusLine().getRoute().getStreets());
        r.initRoute();
        setRoute(r);
    }


}