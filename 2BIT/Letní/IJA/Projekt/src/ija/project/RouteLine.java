package ija.project;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.List;

public class RouteLine implements Drawable{
    boolean on = false;
    List<Shape> lineList = new ArrayList<>();
    Line line = null;
    Coordinate start = null;
    Coordinate end = null;
    boolean editing = false;
    public RouteLine(Coordinate start,Coordinate end){
        this.start = start;
        this.end = end;
        this.line = new Line(start.getX(),start.getY(),end.getX(),end.getY());
        this.line.setStrokeWidth(5.00);
        this.line.setStroke(Color.rgb(0,0,0,0));
        this.lineList.add(line);
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public Coordinate getStart() {
        return start;
    }

    public void setStart(Coordinate start) {
        this.start = start;
    }

    public Coordinate getEnd() {
        return end;
    }

    public void setEnd(Coordinate end) {
        this.end = end;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    @Override
    public List<Shape> getGui() {
        return lineList;
    }
}
