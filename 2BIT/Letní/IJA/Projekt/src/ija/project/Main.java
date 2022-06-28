package ija.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends Application {

    HashMap<String, Street> streets;
    HashMap<String, BusLine> lines;
    HashMap<String, Stop> stops;
    HashMap<String,Route> routes;
    HashMap<String,List<Bus>> buses;
    List<Drawable> elements;
    List<Coordinate> points;
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/res/main.fxml"));
        BorderPane root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        MainController controller = loader.getController();
        elements = new ArrayList<>();

        /*
        Street uliceCeska = new Street("Ceska", new Coordinate(100,100),new Coordinate(800,800));


        Stop zastavkaCeska = new Stop("Ceska 1", new Coordinate(100,100));
        Stop zastavkaKlusackova = new Stop("Klusackova 1", new Coordinate(200,200));
        Stop zastavkaPrivrat = new Stop("Privrat 1", new Coordinate(300,300));
        Stop zastavkaSpanelska = new Stop("Spanelska 1", new Coordinate(600,600));
        Stop zastavkaKrakovska = new Stop("Krakovska 1", new Coordinate(750,750));
        Stop zastavkaHej = new Stop("Hej 1", new Coordinate(800,800));

        uliceCeska.addStop(zastavkaCeska);
        uliceCeska.addStop(zastavkaKlusackova);
        uliceCeska.addStop(zastavkaPrivrat);
        uliceCeska.addStop(zastavkaSpanelska);
        uliceCeska.addStop(zastavkaKrakovska);
        uliceCeska.addStop(zastavkaHej);

        elements.add(uliceCeska);

        elements.add(zastavkaCeska);
        elements.add(zastavkaKlusackova);
        elements.add(zastavkaPrivrat);
        elements.add(zastavkaSpanelska);
        elements.add(zastavkaKrakovska);
        elements.add(zastavkaHej);


        Line linka36 = new Line("Linka 36");
        linka36.addStop(zastavkaCeska);
        linka36.addStop(zastavkaKlusackova);
        linka36.addStop(zastavkaPrivrat);
        linka36.addStop(zastavkaSpanelska);
        linka36.addStop(zastavkaKrakovska);
        linka36.addStop(zastavkaHej);

        Rychlost 10 při periodě 1 tick za 1 sekundu odpovídá zhruba rychlosti 11s na 100px
        Bus bus = new Bus(route, linka36, 10);
        elements.add(bus);
        */
        streets = new HashMap<String, Street>();
        lines = new HashMap<String, BusLine>();
        stops = new HashMap<String, Stop>();
        routes = new HashMap<String,Route>();
        buses = new HashMap<String,List<Bus>>();
        points = new ArrayList<>();
        loadData("src/data/data_routes.txt",4);
        loadData("src/data/data_streets1.txt",0);
        loadData("src/data/data_stops1.txt",1);
        loadData("src//data/data_lines1.txt",2);
        loadData("src/data/data_paths1.txt",3);
        loadData("src/data/data_times.txt",5);
        //loadData("src/data/data_routes.txt",4);
        /*for(Coordinate coor : points){
            for(Map.Entry<String,Street> entryStreet : streets.entrySet()) {
                Street street = entryStreet.getValue();
                if(coor.equals(street.begin())){
                    for(Map.Entry<String,Stop> entryStop : stops.entrySet()) {
                        Stop stop = entryStop.getValue();
                        if(stop.getStreet().equals(street)){

                        }
                    }
                }else if(coor.equals(street.end())){

                }
            }
        }*/
        /*for(Map.Entry<String,Street> entryStreet : streets.entrySet()) {
            Street street = entryStreet.getValue();
            elements.add(new RouteLine(new Coordinate(street.begin().getX(),street.begin().getY()),new Coordinate(street.end().getX(),street.end().getY())));

        }*/
        /*List<Coordinate> routeList = new ArrayList<>();
        //Stop
        routeList.add(new Coordinate(100,100));
        routeList.add(new Coordinate(200,200));
        //Stop
        routeList.add(new Coordinate(250,200));
        routeList.add(new Coordinate(400,200));
        routeList.add(new Coordinate(400,300));
        //Stop
        routeList.add(new Coordinate(400,400));
        routeList.add(new Coordinate(400,500));
        //Stop
        routeList.add(new Coordinate(600,500));
        routeList.add(new Coordinate(800,500));
        //Stop
        routeList.add(new Coordinate(800,580));
        //Stop
        routeList.add(new Coordinate(800,660));
        routeList.add(new Coordinate(800,800));
        Route route = new Route(routeList);*/

        List<BusLine> busLines = new ArrayList<>();
        for(Map.Entry<String, BusLine> entry : lines.entrySet()) {
            BusLine tmp = entry.getValue();
            for(Map.Entry<String,Route> entryRoute : routes.entrySet()) {
                if(entryRoute.getKey().equals(tmp.getName())){
                    entryRoute.getValue().initRoute();
                    tmp.setRoute(entryRoute.getValue());
                    tmp.setDefaultRoute(entryRoute.getValue());

                }
            }
            for(Map.Entry<String,List<Bus>> entryBus : buses.entrySet()) {
                if(entryBus.getKey().equals(tmp.getName())){
                    tmp.setBuses(entryBus.getValue());
                    for(Bus b : entryBus.getValue()) {
                        b.setBusLine(tmp);
                        b.setRouteFromBusline();
                        b.initBus();
                        elements.add(b);
                    }
                }
            }
            busLines.add(tmp);
            //Bus bus = new Bus(tmp.getRoute(), tmp,25);
            //tmp.getBuses().add(bus);
            //elements.add(bus);
        }

        controller.setBusLines(busLines);
        controller.setElements(elements);
        controller.startTimer();

    }

    public void loadData(String path,int type){
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ";";
        try {
            br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) {
                String[] result = line.split(cvsSplitBy);
                switch(type){
                    case 0:
                        /*if(streets.containsKey(result[0])){
                            streets.get(result[0]).addCoordinate(new Coordinate(Integer.parseInt(result[1]),Integer.parseInt(result[2])));
                        }else{
                            Street str = new Street(result[0]);
                            streets.put(result[0], str);
                            str.addCoordinate(new Coordinate(Integer.parseInt(result[1]),Integer.parseInt(result[2])));
                        }*/
                        Street str = new Street(result[0],new Coordinate(Double.valueOf(result[1]),Double.valueOf(result[2])),new Coordinate(Double.valueOf(result[3]),Double.valueOf(result[4])));
                        streets.put(result[0], str);
                        elements.add(new RouteLine(new Coordinate(str.begin().getX(),str.begin().getY()),new Coordinate(str.end().getX(),str.end().getY())));
                        elements.add(str);
                        break;
                    case 1:
                        Stop st = new Stop(result[0],new Coordinate(Integer.parseInt(result[2]),Integer.parseInt(result[3])));
                        streets.get(result[1]).addStop(st);
                        stops.put(result[0], st);
                        elements.add(st);
                        //streets.get(result[1]).addStop(st);
                        break;
                    case 2:
                        if(lines.containsKey(result[0])){
                            lines.get(result[0]).addStop(stops.get(result[1]));
                        }else{
                            BusLine ln = new BusLine(result[0]);
                            lines.put(result[0], ln);
                            ln.addStop(stops.get(result[1]));
                        }
                        break;
                    case 3:
                        if(routes.containsKey(result[0])){
                            routes.get(result[0]).getPath().add(new Coordinate(Integer.parseInt(result[1]),Integer.parseInt(result[2])));
                        }else{
                            List<Coordinate> tmp = new ArrayList<>();
                            tmp.add(new Coordinate(Integer.parseInt(result[1]),Integer.parseInt(result[2])));
                            routes.put(result[0], new Route(tmp,new ArrayList<Street>(streets.values())));
                        }
                        break;
                    case 4:
                        //elements.add(new RouteLine(new Coordinate(Integer.parseInt(result[0]),Integer.parseInt(result[1])),new Coordinate(Integer.parseInt(result[2]),Integer.parseInt(result[3]))));
                        points.add(new Coordinate(Integer.parseInt(result[0]),Integer.parseInt(result[1])));
                        break;
                    case 5:
                        //elements.add(new RouteLine(new Coordinate(Integer.parseInt(result[0]),Integer.parseInt(result[1])),new Coordinate(Integer.parseInt(result[2]),Integer.parseInt(result[3]))));
                        if(buses.containsKey(result[0])){
                            buses.get(result[0]).add(new Bus(null,null,10,result[1]));
                        }else{
                            List<Bus> tmp = new ArrayList<>();
                            tmp.add(new Bus(null,null,10,result[1]));
                            buses.put(result[0], tmp);
                        }
                        break;
                    default:break;
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
