package ija.project;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
public class Street implements Drawable {

    private Coordinate start;
    private Coordinate end;
    private List<Stop> stops = new ArrayList<>();
    private String name;
    private List<Shape> gui = new ArrayList<>();
    private double traffic = 1;
    private Line line;
    private Color lineColor;
    private boolean closed;
    private List<Shape> closedSigns = new ArrayList<>();
    private double delayValue = 1;

    public Street(String name, Coordinate start, Coordinate end) {
        this.start = start;
        this.end = end;
        this.name = name;
        this.closed = false;
        this.line = new Line(start.getX(), start.getY(), end.getX(), end.getY());
        /*Line line = new Line(start.getX(), start.getY(), end.getX(), end.getY());
        line.setStrokeWidth(5.00);
        line.setStroke(Color.rgb(0,0,0,0.3));
        gui.add(line);*/
        lineColor = Color.rgb(0,128,0,0.3);
        setDefaultGui();
        gui.add(line);

    }

    public Color getLineColor() {
        return lineColor;
    }
    public void setLineColor(Color color) {
        lineColor = color;
    }
    public void setDefaultGui(){
        this.line.setStrokeWidth(5.00);
        this.line.setStroke(Color.color(getLineColor().getRed(),getLineColor().getGreen(),getLineColor().getBlue(),0.3));
    }
    public void setSelectedGui(){
        this.line.setStrokeWidth(5.00);
        this.line.setStroke(Color.color(getLineColor().getRed(),getLineColor().getGreen(),getLineColor().getBlue(),1));
    }
    public List<Stop> getStops() {
        return stops;
    }

    public double getTrafficValue(){
        return this.traffic;
    }
    public void setTraffic(double value){
        //Color imageColor = Color.GREEN.interpolate(Color.RED, value / 100.0);
        //setLineColor(imageColor);
        //setSelectedGui();
        Color imageColor = null;
        Color destColor = null;
        if(value>=1&&value<1.5){

            if(value> getTrafficValue())
                destColor = Color.rgb(255,255,0,1);
            else
                destColor = Color.rgb(0,128,0,1);
            imageColor = Color.color(getLineColor().getRed(),getLineColor().getGreen(),getLineColor().getBlue()).interpolate(destColor, value / 10.0);
            setLineColor(imageColor);
            setSelectedGui();
        }
        else if(value>=1.5&&value<2){
            if(value> getTrafficValue())
                destColor = Color.rgb(255,0,0,1);
            else
                destColor = Color.rgb(255,255,0,1);
            imageColor = Color.color(getLineColor().getRed(),getLineColor().getGreen(),getLineColor().getBlue()).interpolate(destColor, value / 10.0);
            setLineColor(imageColor);
            setSelectedGui();
        }
       else if(value>=2){
            setLineColor(Color.rgb(255,0,0,1));
            setSelectedGui();
            value = 2;
        }else if(value<=1){
            setLineColor(Color.rgb(0,128,0,1));
            setSelectedGui();
            value = 1;
        }
        this.traffic = value;
        setDelayValue(1-(value-1));

    }
    public void createClosedSigns(){
        Circle tmpCircle1 = new Circle(line.getStartX(), line.getStartY(), 10, Color.RED);
        Line tmpLine1 = new Line(line.getStartX()-5,line.getStartY(),line.getStartX()+5,line.getStartY());
        tmpLine1.setStrokeWidth(3.00);
        tmpLine1.setStroke(Color.rgb(255,255,255,1));
        Circle tmpCircle2 = new Circle(line.getEndX(), line.getEndY(), 10, Color.RED);
        Line tmpLine2 = new Line(line.getEndX()-5,line.getEndY(),line.getEndX()+5,line.getEndY());
        tmpLine2.setStrokeWidth(3.00);
        tmpLine2.setStroke(Color.rgb(255,255,255,1));
        closedSigns.add(tmpCircle1);
        closedSigns.add(tmpLine1);
        closedSigns.add(tmpCircle2);
        closedSigns.add(tmpLine2);
    }
    public void setClosed(boolean closed, Pane map){
        if(closed){
            createClosedSigns();
            map.getChildren().addAll(closedSigns);
            line.getStrokeDashArray().addAll(25d, 10d);
        }else{
            map.getChildren().removeAll(closedSigns);
            closedSigns = new ArrayList<>();
            line.getStrokeDashArray().removeAll(25d,10d);
        }
        this.closed = closed;
    }
    public boolean isClosed(){
        return this.closed;
    }
    public boolean addStop(Stop stop) {

        stop.setStreet(this);
        stops.add(stop);
        return true;
    }

    public boolean follows(Street s) {

        if (this.begin().equals(s.begin()))
            return true;

        if (this.end().equals(s.end()))
            return true;

        if (this.begin().equals(s.end()))
            return true;

        if (this.end().equals(s.begin()))
            return true;

        return false;


    }
    public Coordinate begin() {
        return start;
    }

    public Coordinate end() {
        return end;
    }
    private double distance(Coordinate get, Coordinate coordinate) {

        return Math.sqrt((coordinate.getX()-get.getX())*(coordinate.getX()-get.getX()) + (coordinate.getY()-get.getY())*(coordinate.getY()-get.getY()));
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public List<Shape> getGui() {
        return gui;
    }

    public double getDelayValue() {
        return delayValue;
    }

    public void setDelayValue(double delayValue) {
        this.delayValue = delayValue;
    }

}
