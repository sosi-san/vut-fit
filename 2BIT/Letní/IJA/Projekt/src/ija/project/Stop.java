package ija.project;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Stop implements Drawable {
    private String name;
    private Coordinate coordinate;
    private Street street;
    private List<Shape> gui;

    public Stop(String name, Coordinate coordinate) {
        this.name = name;
        this.coordinate = coordinate;
        this.gui = new ArrayList<>();
        gui.add(new Circle(coordinate.getX(), coordinate.getY(), 8, Color.BLACK));
    }

    public String getName() {
        return name;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setStreet(Street street) {
        this.street = street;
    }
    public Street getStreet() {
        return street;
    }

    @Override
    public boolean equals(Object obj) {
        Stop stop = (Stop) obj;
        return (Objects.equals(stop.name, this.name));
    }

    @Override
    public String toString()
    {
        return "stop("+ getName()+")";

    }

    @Override
    public List<Shape> getGui() {
        return gui;
    }
}
