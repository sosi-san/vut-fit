package ija.project;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainController {

    @FXML
    private Pane mapa;
    @FXML
    private Text timerText;
    @FXML
    private Text timeInfo;
    @FXML
    private Label sideBarLabel;
    @FXML
    private Text lineName;
    @FXML
    private Slider timeTabel;
    @FXML
    private ListView sideBar;
    @FXML
    private Pane sideBarBottom;
    private Bus currentBus = null;
    private Street currentStreet = null;
    private LocalTime localTime = LocalTime.of(9,59,55);
    private Timer timer;
    private Timer ticker;
    private static int timeSpeed = 1000;
    private ToggleGroup group = null;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    List<Drawable> elements = new ArrayList<>();
    List<TickUpdate> busList = new ArrayList<>();
    static List<Street> streets = new ArrayList<>();
    List<RouteLine> displayedRoute = new ArrayList<>();
    List<BusLine> busLines = new ArrayList<>();
    List<RouteLine> routeLines = new ArrayList<>();
    List<Coordinate> editedRoute = new ArrayList<>();
    boolean editMode = false;
    Button buttonAccept;
    Button buttonCancel;
    Button buttonStop;
    BusLine chosenBusLine = null;
    boolean pause = false;

    /*public int getTimeSpeed() {
        return timeSpeed;
    }*/

    public List<Coordinate> getEditedRoute() {
        return editedRoute;
    }

    public void setEditedRoute(List<Coordinate> editedRoute) {
        this.editedRoute = editedRoute;
    }

    public BusLine getChosenBusLine() {
        return chosenBusLine;
    }

    public void setChosenBusLine(BusLine chosenBusLine) {
        this.chosenBusLine = chosenBusLine;
    }

    public Pane getMapa() {
        return mapa;
    }

    public ToggleGroup getGroup() {
        return group;
    }

    public Button getButtonAccept() {
        return buttonAccept;
    }

    public Button getButtonCancel() {
        return buttonCancel;
    }

    public Button getButtonStop() {
        return buttonStop;
    }

    /**
     * Zapne vnitřní hodiny aplikace. Perioda aktualizace je 1 sekunda.
     */
    public void startTimer() {
        timer = new Timer(false);
        ticker = new Timer(false);
        timerText.setText(localTime.format(dateTimeFormatter));
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                localTime = localTime.plusSeconds(1);
                timerText.setText(localTime.format(dateTimeFormatter));
                for(BusLine busLine : getBusLines()){
                    for(Bus bus : busLine.getBuses()){
                        if(bus.getStartTime().equals(localTime)){
                            System.out.println("Autobus vyjel");
                        }
                    }
                }
                if(!isPause()){

                    for (TickUpdate tickUpdate : busList) {
                        tickUpdate.tick(localTime);
                    }

                    if(currentBus != null)
                    {

                        timeTabel.setValue((1-(currentBus.getDistanceToNextStopFromRealPosition()/currentBus.getDistanceToNextStopFromPreviousStop()))*100+(100*(currentBus.getLastStopIndex())));
                    }
                    // Uprav periodu v Route na stejnou hodnotu !!!!!!!!
                }

            }
        }, 0, timeSpeed);
    }
    /**
     * Přidá na pozadní všechny gui prvky aplikace. Autobusy, ulice.
     */

    public void setElements(List<Drawable> elements) {
        this.elements = elements;
        for (Drawable drawable : elements) {
            mapa.getChildren().addAll(drawable.getGui());
            if(drawable instanceof TickUpdate)
                busList.add((TickUpdate) drawable);
            if(drawable instanceof Street)
                streets.add((Street) drawable);
            if(drawable instanceof RouteLine)
                routeLines.add((RouteLine) drawable);
        }
        //currentBus = ((Bus)busList.get(0));
        //changeStreetLabels();
        setListeners();
    }
    public void stopBusLine(BusLine busLine){


    }
    public boolean tryToOpenBusLine(BusLine busLine){
        return false;
    }
    @FXML
    private void timePlus(ActionEvent actionEvent){
        // Speed plus
        this.timer.cancel();
        timeSpeed /= 2;
        if(timeSpeed <= 125)
            timeSpeed = 125;
        startTimer();
        timeInfo.setText("1 sekunda = "+((double)timeSpeed)/1000.0+" sekund");
    }
    @FXML
    private void timeMinus(ActionEvent actionEvent){
        // Speed slower
        this.timer.cancel();
        timeSpeed *= 2;
        if(timeSpeed >= 1000)
            timeSpeed = 1000;
        startTimer();
        timeInfo.setText("1 sekunda = "+((double)timeSpeed)/1000.0+" sekund");

    }
    /**
     * Přidá onClick listener všem autobusům.
     */
    public void showRouteGui(List<Coordinate> route){
        for(int i = 0;i<route.size()-1;i++){


            for(RouteLine rl: getRouteLines()){
                if((rl.getStart().equals(route.get(i))&&(rl.getEnd().equals(route.get(i+1))))||(rl.getEnd().equals(route.get(i))&&(rl.getStart().equals(route.get(i+1))))){
                    if(lineFromPoints(route.get(i), route.get(i+1))){
                        rl.getLine().setStroke(Color.rgb(255,165,0,0.6));
                    }else {
                        rl.getLine().setStroke(Color.rgb(0,0,200,0.3));
                    }
                        rl.getLine().setStrokeWidth(5.00);
                    displayedRoute.add(rl);
            }
            }

            //displayedRoute.add(ln);
            //mapa.getChildren().add(ln);
        }
    }
    public void setListeners() {
        for (TickUpdate t: busList) {
            Bus bus = ((Bus)t);
            bus.getGui().get(0).addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                event.consume();
                if(!isEditMode()) {
                    this.currentBus = bus;
                    setBlankRouteLines();
                    showRouteGui(currentBus.getRoute().getPath());
                    changeStreetLabels();
                }
            });
        }
        for (Street street: streets) {
            street.getGui().get(0).addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if(!isEditMode()) {
                    System.out.println(street.toString());
                    if (this.currentStreet != null)
                        this.currentStreet.setDefaultGui();
                    this.currentStreet = street;
                    this.currentStreet.setSelectedGui();
                    pause(true);

                    if (event.getClickCount() == 2 && !event.isConsumed()) {

                        street.setClosed(!street.isClosed(), mapa);
                        /*if(street.isClosed()) {

                            List<RadioButtonBusLine> changedLines = checkForChangedLines(street);
                            if (changedLines.size() != 0) {

                                setEditMode(true);
                                ObservableList<RadioButtonBusLine> oListBusLine = FXCollections.observableArrayList(changedLines);
                                setBlankRouteLines();
                                sideBar.setItems(oListBusLine);
                                sideBarLabel.setText("Tyto linky potřebují náhradní cestu!");
                                GridPane gridPane = new GridPane();

                                gridPane.setPadding(new Insets(10, 10, 10, 10));
                                gridPane.setVgap(5);
                                gridPane.setHgap(5);
                                gridPane.setAlignment(Pos.CENTER);
                                buttonAccept = new Button("Potvrdit");
                                buttonAccept.setDisable(true);
                                buttonAccept.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent e) {
                                        System.out.println("acceptBUtton");
                                        System.out.println(getChosenBusLine().toString());
                                        getChosenBusLine().setAltRoute(getEditedRoute());
                                        for (RadioButtonBusLine radioButtonBusLine : changedLines) {
                                            if (radioButtonBusLine.getBusLine().equals(getChosenBusLine())) {
                                                sideBar.getItems().remove(radioButtonBusLine);
                                                oListBusLine.remove(radioButtonBusLine);
                                            }
                                        }
                                        if (oListBusLine.size() == 0) {
                                            setEditMode(false);
                                            setBlankRouteLines();
                                            sideBar.getItems().clear();
                                            sideBarBottom.getChildren().clear();
                                            sideBarLabel.setText("Aktivní spoje");
                                        }

                                    }
                                });
                                buttonCancel = new Button("Zrušit");
                                buttonCancel.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent e) {
                                        System.out.println("acceptCancel");
                                        if (getGroup().getSelectedToggle() != null) {
                                            getGroup().getSelectedToggle().setSelected(false);
                                            setBlankRouteLines();
                                            setEditedRoute(null);

                                        }

                                    }
                                });
                                buttonStop = new Button("Zastavit linku");
                                buttonStop.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent e) {
                                        System.out.println("acceptStop");
                                    }
                                });
                                Label text = new Label("Zpoždění:");
                                TextField textField = new TextField();
                                gridPane.add(text, 0, 0, 2, 1);
                                gridPane.add(textField, 2, 0);
                                gridPane.add(buttonAccept, 0, 1);
                                gridPane.add(buttonCancel, 1, 1);
                                gridPane.add(buttonStop, 2, 1);
                                sideBarBottom.getChildren().add(gridPane);
                            }
                        }else{
                            for(BusLine busLine : getBusLines()){

                                if(!busLine.getRoute().equals(busLine.getDefaultRoute())){
                                    for(int i = 0;i<busLine.getRoute().getPath().size()-1;i++){
                                        if(!checkIfLineLiesOnStreet(this.currentStreet,busLine.getRoute().getPath().get(i),busLine.getRoute().getPath().get(i+1))){
                                            for(Bus bus : busLine.getBuses()){
                                                Route rb = new Route(busLine.getDefaultRoute().getPath());
                                                rb.initRoute();
                                                rb.setCurrentPositionIndex(bus.getRoute().getCurrentPositionIndex());
                                                rb.setNextPosition(bus.getRoute().getNextPosition());
                                                rb.setLastPosition(bus.getRoute().getLastPosition());
                                                rb.setRealPosition(bus.getRoute().getRealPosition());
                                                rb.setT(bus.getRoute().getT());
                                                bus.setRoute(rb);


                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                            setBlankRouteLines();
                        }*/

                    }
                    event.consume();
                }else{
                    if (event.getClickCount() == 2 && !event.isConsumed()) {
                        street.setClosed(!street.isClosed(), mapa);
                                 setEditMode(false);
                    }
                    event.consume();
                }

            });

        }
        for (RouteLine routeLine : getRouteLines()) {

            routeLine.getGui().get(0).addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getClickCount() == 2 && !event.isConsumed()) {
                    /*if(checkIfLineLiesOnStreet(this.currentStreet,routeLine.getStart(),routeLine.getEnd())) {
                        this.currentStreet.setClosed(!this.currentStreet.isClosed(), mapa);
                        for(BusLine busLine : getBusLines()){
                            if(busLine.getRoute().equals(busLine.getDefaultRoute())){
                                for(int i = 0;i<busLine.getRoute().getPath().size()-1;i++){
                                    if(checkIfLineLiesOnStreet(this.currentStreet,busLine.getRoute().getPath().get(i),busLine.getRoute().getPath().get(i+1))){
                                        for(Bus bus : busLine.getBuses()){
                                            Route rb = new Route(busLine.getDefaultRoute().getPath());
                                            rb.initRoute();
                                            rb.setCurrentPositionIndex(bus.getRoute().getCurrentPositionIndex());
                                            rb.setNextPosition(bus.getRoute().getNextPosition());
                                            rb.setLastPosition(bus.getRoute().getLastPosition());
                                            rb.setRealPosition(bus.getRoute().getRealPosition());
                                            rb.setT(bus.getRoute().getT());
                                            bus.setRoute(rb);


                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        setBlankRouteLines();
                        setEditMode(false);
                        sideBar.getItems().clear();
                        sideBarBottom.getChildren().clear();
                        sideBarLabel.setText("Aktivní spoje");
                        event.consume();
                        return;
                    }*/
                    setEditMode(false);
                }
               /* event.consume();
                if(isEditMode()) {
                    if(routeLine.isEditing()){
                        routeLine.getLine().setStroke(Color.rgb(0,0,200,0.3));
                        if(!getEditedRoute().get(getEditedRoute().size()-1).equals(routeLine.getStart())){
                            getEditedRoute().add(routeLine.getStart());
                        }else{
                            getEditedRoute().add(routeLine.getEnd());
                        }
                        for(RouteLine routeLine1 : getRouteLines()){
                            if(routeLine1.isEditing()&&!routeLine1.equals(routeLine)){
                                routeLine1.getLine().setStroke(Color.rgb(0,0,200,0));
                                routeLine1.setEditing(false);
                            }else if(routeLine1.isEditing()&&routeLine1.equals(routeLine)) {
                                routeLine1.setEditing(false);
                            }
                        }
                        findRouteOptions(getEditedRoute());
                    }
                }
                event.consume();
                */
            });
        }
    }
    public void pause(boolean b){
        if(b){
            this.timer.cancel();
            this.ticker.cancel();
            setPause(true);
        }else{
            setPause(false);
            startTimer();
        }
    }
    public void changeStreetLabels() {

        // Max hodnota slideru bude pocet zastavek - 1 * 100 (takze mezi dvema zastavkami je 100
        timeTabel.setMax((currentBus.getBusLine().getAllStops().size()-1)*100);
        lineName.setText(currentBus.getBusLine().getName());
        // System.out.println(currentBus.getBusLine().getAllStops());
        timeTabel.setLabelFormatter(null);
        timeTabel.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                int index = object.intValue();
                if(index == 0)
                    return currentBus.getBusLine().getAllStops().get((index/100)).getName() + " " + currentBus.getStartTime().format(dateTimeFormatter);
                else
                {
                    double[] tmp = currentBus.calculateDelayArr();
                    double allDelay = 0;

                    for(int i = 0; i < index/100; i++)
                    {
                        allDelay += tmp[i];
                    }
                    allDelay += currentBus.calculateArriveTime(index/100);
                    int minutes = (int)(currentBus.calculateArriveTime(index/100) % 3600) / 60;
                    int seconds = (int)currentBus.calculateArriveTime(index/100) % 60;
                    String time = String.format("10:%02d:%02d", minutes, seconds);
                    System.out.println(allDelay);
                    int minutesD = (int)(allDelay % 3600) / 60;
                    int secondsD = (int) allDelay % 60;
                    String timeZ = String.format("(10:%02d:%02d)", minutes+minutesD, seconds);

                    return currentBus.getBusLine().getAllStops().get(
                            (index/100)).getName()
                            + " " + time + " " + timeZ;
                }
            }

            @Override
            public Double fromString(String string) {
                return null;
            }
        });

    }
    @FXML
    private void setMapNull(MouseEvent event){
        if(!isEditMode()) {
            System.out.println("map_click");

            if (this.currentStreet != null)
                this.currentStreet.setDefaultGui();
            this.currentStreet = null;
            if(isPause())
                pause(false);
            for (RouteLine ln : getRouteLines()) {
                ln.getLine().setStroke(Color.rgb(255, 165, 0, 0));
            }
            mapa.getChildren().removeAll(displayedRoute);
        }
    }
    @FXML
    private void onScroll(ScrollEvent event) {

        event.consume();
        double zoom = event.getDeltaY() > 0 ? 1.1 : 0.90;
        if(this.currentStreet!=null){
            if(!currentStreet.isClosed()) {
                double traffic = this.currentStreet.getTrafficValue();
                traffic = event.getDeltaY() > 0 ? traffic + 0.02 : traffic - 0.02;

                this.currentStreet.setTraffic(traffic);
            }
        }else{
            if(zoom * mapa.getScaleX() < 1)
            {
                mapa.setScaleX(1);
                mapa.setScaleY(1);
            }
            else
            {
                mapa.setScaleX(zoom * mapa.getScaleX());
                mapa.setScaleY(zoom * mapa.getScaleY());
            }
        }
        mapa.layout();
    }
    public boolean lineFromPoints(Coordinate coor1, Coordinate coor2)
    {

        List<Street> streets = getStreets();
        for(Street s : streets){
            if(s.isClosed()) {
                    /*boolean check1 = (s.end().getY()-s.begin().getY())*(cor1.getX()-s.end().getX()) == (cor1.getY()-s.end().getY())*(s.end().getX()-s.begin().getX());
                    boolean check2 = (s.end().getY()-s.begin().getY())*(cor2.getX()-s.end().getX()) == (cor2.getY()-s.end().getY())*(s.end().getX()-s.begin().getX());


                    System.out.println("Street: " + s.toString() + "Beg: X:" + s.begin().getX() + " Y:" + s.begin().getY() + "End: X:" + s.end().getX() + " Y:" + s.end().getY());
                    System.out.println("Cor1:X: " + cor1.getX() + " Y: " + cor1.getY());
                    System.out.println("Cor2:X: " + cor2.getX() + " Y: " + cor2.getY());
                    if (check1) System.out.println("COR 1 leží");
                    if (check2) System.out.println("COR 2 leží");
                    System.out.println("Leží: " + (check1 && check2));
                    System.out.println("----------------------------------------");*/
                    if (checkIfLineLiesOnStreet(s,coor1,coor2))
                        return true; // C is on the line.


            }
        }
        return false;

    }
    public void setBlankRouteLines(){
        for(RouteLine rl :  getRouteLines()){
            rl.getLine().setStroke(Color.rgb(148, 0, 211, 0));
        }
    }
    public static boolean checkIfLineLiesOnStreet(Street street,Coordinate coor1,Coordinate coor2){
        boolean check1 = (street.end().getY()-street.begin().getY())*(coor1.getX()-street.end().getX()) == (coor1.getY()-street.end().getY())*(street.end().getX()-street.begin().getX());
        boolean check2 = (street.end().getY()-street.begin().getY())*(coor2.getX()-street.end().getX()) == (coor2.getY()-street.end().getY())*(street.end().getX()-street.begin().getX());

        /*System.out.println("Street: " + s.toString() + "Beg: X:" + s.begin().getX() + " Y:" + s.begin().getY() + "End: X:" + s.end().getX() + " Y:" + s.end().getY());
        System.out.println("Cor1:X: " + cor1.getX() + " Y: " + cor1.getY());
        System.out.println("Cor2:X: " + cor2.getX() + " Y: " + cor2.getY());
        if (check1) System.out.println("COR 1 leží");
        if (check2) System.out.println("COR 2 leží");
        System.out.println("Leží: " + (check1 && check2));
        System.out.println("----------------------------------------");*/
        if (check1 && check2)
            return true;
        return false;
    }
    public List<RadioButtonBusLine> checkForChangedLines(Street street)
    {
        List<RadioButtonBusLine> cbl = new ArrayList<>();
        group = new ToggleGroup();
        for(BusLine bl : getBusLines()){
            boolean b = false;
            for(int i = 0;i<bl.getRoute().getPath().size()-1;i++){
                if(checkIfLineLiesOnStreet(street,bl.getRoute().getPath().get(i),bl.getRoute().getPath().get(i+1))){
                    /*RadioButtonBusLine tmp = new RadioButtonBusLine(bl);
                    tmp.setToggleGroup(group);
                    cbl.add(tmp);*/
                    b=true;
                    break;
                }
            }
            if(b){
                RadioButtonBusLine tmp = new RadioButtonBusLine(bl);
                tmp.setToggleGroup(group);
                cbl.add(tmp);
            }
        }
        return cbl;
    }
    public static List<Street> getStreets(){   return streets;   }

    public void setBusLines(List<BusLine> busLines) {
        this.busLines = busLines;
    }
    public List<BusLine> getBusLines() {
        return this.busLines;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        if(editMode){
            for (Street street: streets) {
                mapa.getChildren().removeAll(street.getGui());
            }
            for (RouteLine rl: routeLines) {
                mapa.getChildren().removeAll(rl.getGui());
            }
            for (Street street: streets) {
                mapa.getChildren().addAll(street.getGui());
            }
            for (RouteLine rl: routeLines) {
                mapa.getChildren().addAll(rl.getGui());
            }
        }else{
            for (Street street: streets) {
                mapa.getChildren().removeAll(street.getGui());
            }
            for (RouteLine rl: routeLines) {
                mapa.getChildren().removeAll(rl.getGui());
            }
            for (RouteLine rl: routeLines) {
                mapa.getChildren().addAll(rl.getGui());
            }
            for (Street street: streets) {
                mapa.getChildren().addAll(street.getGui());
            }
        }
        this.editMode = editMode;
    }

    public List<RouteLine> getRouteLines() {
        return routeLines;
    }

    public void setRouteLines(List<RouteLine> routeLines) {
        this.routeLines = routeLines;
    }

    public void findRouteOptions(List<Coordinate> path){
        Coordinate preLastOpen = path.get(path.size()-2);
        Coordinate lastOpen = path.get(path.size()-1);
        Coordinate endPoint = new Coordinate(lastOpen.getX(),lastOpen.getY());
        outerloop: for(RouteLine rl : getRouteLines()){
            for(Street street : streets){
                if(street.isClosed()){
                    if(checkIfLineLiesOnStreet(street,rl.getStart(),rl.getEnd())){
                        continue outerloop;
                    }
                }

            }
            if((rl.getStart().equals(lastOpen)||rl.getEnd().equals(lastOpen))&&(!(rl.getStart().equals(preLastOpen)||rl.getEnd().equals(preLastOpen)))){
                if(!rl.getLine().getStroke().equals(Color.rgb(0,0,200,0.3))) {
                    rl.getLine().setStroke(Color.rgb(148, 0, 211, 0.6));
                    rl.setEditing(true);
                }else{
                    buttonAccept.setDisable(false);
                }
            }
        }
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }
    public static int getTimeSpeed(){
        return timeSpeed;
    }
    public static Street getStreetFromPoint(Coordinate coordinate) {
        System.out.println(MainController.getStreets());
        for (Street s : getStreets()) {
            if (checkIfLineLiesOnStreet(s, coordinate, coordinate)) {
                return s;
            }
        }
        return null;
    }
    private class RadioButtonBusLine extends RadioButton {
        BusLine busLine;
        public RadioButtonBusLine(BusLine busLine){
            super(busLine.toString());
            this.busLine = busLine;
            Line lastOpenLine = null;
            this.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> obs, Boolean wasPreviouslySelected, Boolean isNowSelected) {
                    if (isNowSelected) {
                        setEditMode(true);
                        System.out.println("Selected: "+busLine.toString());
                        showRouteGui(busLine.getRoute().getPath());
                        List<Coordinate> newPath = new ArrayList<>();
                        Coordinate firstClosed = null;
                        for(int i = 0;i<displayedRoute.size()-1;i++){
                            RouteLine rl = displayedRoute.get(i);
                            if(rl.getLine().getStroke().equals(Color.rgb(255,165,0,0.6))){
                                //System.out.println(displayedRoute.get(i-1).getEnd().getX() + " a "+ displayedRoute.get(i-1).getEnd().getY()));
                                //lastOpenLine = displayedRoute.get(i-1);
                                newPath.add(new Coordinate(displayedRoute.get(i-1).getEnd().getX(),displayedRoute.get(i-1).getEnd().getY()));
                                firstClosed = new Coordinate(displayedRoute.get(i).getEnd().getX(),displayedRoute.get(i).getEnd().getY());
                                break;
                            }else{
                                newPath.add(new Coordinate(rl.getLine().getStartX(),rl.getLine().getStartY()));
                            }
                        }
                        System.out.println("size: "+newPath.size());

                        setEditedRoute(newPath);
                        findRouteOptions(newPath);
                        //chosenBusLine = busLine;
                        setChosenBusLine(busLine);
                    } else {

                        System.out.println("NOTSelected: "+busLine.toString());
                    }
                }
            });
        }
        public BusLine getBusLine(){
            return busLine;
        }

    }
}
