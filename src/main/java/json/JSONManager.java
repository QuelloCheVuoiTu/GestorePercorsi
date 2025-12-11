package json;

import com.mongodb.client.*;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import routes.RouteManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class JSONManager {
    private final RouteManager routeManager;

    public JSONManager() {
        this.routeManager = new RouteManager();
    }

    public void addJsonToDB() {
        MongoClient mongoClient = MongoClients.create("mongodb://172.31.0.110:31000");
        MongoDatabase database = mongoClient.getDatabase("ProgettoASSD");
        MongoCollection<Document> collection = database.getCollection("Routes");
        collection.drop();
        List<Document> documents = new ArrayList<>();
        JSONArray routes = getFinalJSON();
        for (int i = 0; i < routes.length(); i++) {
            JSONObject obj = routes.getJSONObject(i);
            Document doc = Document.parse(obj.toString());
            documents.add(doc);
        }
        collection.insertMany(documents);
    }

    private JSONArray readDataFromDB() {
        JSONArray routes = new JSONArray();
        MongoClient mongoClient = MongoClients.create("mongodb://172.31.0.110:31000");
        MongoDatabase db = mongoClient.getDatabase("ProgettoASSD");
        MongoCollection<Document> collection = db.getCollection("Routes");
        for (Document doc : collection.find()) {
            JSONObject obj = new JSONObject(doc.toJson());
            routes.put(obj);
        }
        return routes;
    }

    private JSONArray createShipments() {
        int idShipment = 0;
        JSONArray shipments = new JSONArray();
        List<JSONObject> routeBins = routeManager.getRouteBins();
        for (JSONObject routeBin : routeBins) {
            JSONObject shipment = new JSONObject();
            String amountString = routeBin.getString("state");
            int amount = switch (amountString) {
                case "FULL" -> 1000;
                case "THREE QUARTERS FULL" -> 750;
                case "HALF FULL" -> 500;
                case "QUARTER FULL" -> 250;
                default -> 0;
            };
            shipment.put("amount", new JSONArray(Arrays.asList(amount)));
            JSONObject pickup = new JSONObject();
            pickup.put("id", idShipment++);
            pickup.put("service", 0);
            pickup.put("location", new JSONArray(Arrays.asList(routeBin.getDouble("lon"), routeBin.getDouble("lat"))));
            shipment.put("pickup", pickup);
            JSONObject delivery = new JSONObject();
            delivery.put("id", idShipment++);
            delivery.put("location", new JSONArray(Arrays.asList(14.796099924893962, 41.14060022326699)));
            shipment.put("delivery", delivery);
            shipments.put(shipment);
        }
        return shipments;
    }

    private JSONObject createOptions() {
        JSONObject options = new JSONObject();
        options.put("g", true);
        return options;
    }

    private JSONArray createVehicles() {
        JSONArray vehicles = new JSONArray();
        List<JSONObject> availableVehicles = routeManager.getAvailableVehicles();
        for (JSONObject availableVehicle : availableVehicles) {
            JSONObject vehicle = new JSONObject();
            int id = availableVehicle.getInt("id");
            vehicle.put("id", id);
            vehicle.put("start", new JSONArray(Arrays.asList(availableVehicle.getDouble("startLon"), availableVehicle.getDouble("startLat"))));
            vehicle.put("end", new JSONArray(Arrays.asList(availableVehicle.getDouble("endLon"), availableVehicle.getDouble("endLat"))));
            int capacity = availableVehicle.getInt("capacity");
            vehicle.put("capacity", new JSONArray(Arrays.asList(capacity)));
            JSONObject costs = new JSONObject();
            costs.put("fixed", availableVehicle.getDouble("fixedCost"));
            costs.put("per_km", availableVehicle.getDouble("costPerKm"));
            costs.put("per_hour", availableVehicle.getDouble("costPerHour"));
            vehicle.put("costs", costs);
            vehicle.put("time_window", new JSONArray(Arrays.asList(0, availableVehicle.getDouble("timeWindow"))));
            vehicles.put(vehicle);
        }
        return vehicles;
    }

    private JSONObject createFinalJsonPOST() {
        JSONObject root = new JSONObject();
        root.put("shipments", createShipments());
        root.put("options", createOptions());
        root.put("vehicles", createVehicles());
        return root;
    }

    private JSONArray getFinalJSON() {
        JSONObject json = createFinalJsonPOST();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json.toString(), headers);
        ResponseEntity<Void> response = restTemplate.postForEntity("http://172.30.0.74:8080/trust/api/v1/planner/problems", request, Void.class);
        String locationHeader = response.getHeaders().getFirst("Location");
        if (locationHeader == null) throw new RuntimeException();
        String problemId = locationHeader.substring(locationHeader.lastIndexOf('=') + 1);
        String url = "http://172.30.0.74:8080/trust/api/v1/planner/plans?problem=" + problemId;
        JSONObject lastPlan = null;
        int maxAttempts = 30, delayMs = 2000;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String responseBody = restTemplate.getForObject(url, String.class);
                if (responseBody != null) {
                    lastPlan = new JSONObject(responseBody);
                    break;
                }
            } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                System.out.println("Non funziona, tentativo " + attempt);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ignored) {}
            }
        }
        if (lastPlan == null) throw new RuntimeException();
        System.out.println(url);
        return lastPlan.getJSONArray("routes");
    }

    public HashMap<Integer, String> getVehicleGeometryMap() {
        JSONArray routes = readDataFromDB();
        HashMap<Integer, String> vehicleGeometryMap = new HashMap<>();
        for (int i = 0; i < routes.length(); i++) {
            JSONObject route = routes.getJSONObject(i);
            int vehicleId = route.getInt("vehicle");
            String geometry = route.getString("geometry");
            vehicleGeometryMap.put(vehicleId, geometry);
        }
        return vehicleGeometryMap;
    }

    public HashMap<Integer, List<String>> getVehicleBinsMap() {
        HashMap<Integer, List<String>> vehicleBinsMap = new HashMap<>();
        JSONArray routes = readDataFromDB();
        for (int i = 0; i < routes.length(); i++) {
            JSONObject route = routes.getJSONObject(i);
            int vehicleId = route.getInt("vehicle");
            JSONArray steps = route.getJSONArray("steps");
            List<String> coordsList = new ArrayList<>();
            for (int j = 0; j < steps.length(); j++) {
                JSONObject step = steps.getJSONObject(j);
                String type = step.getString("type");
                if (type.equals("pickup") || type.equals("delivery")) {
                    JSONArray loc = step.getJSONArray("location");
                    double lon = loc.getDouble(0);
                    double lat = loc.getDouble(1);
                    coordsList.add(lat + "," + lon);
                }
            }
            vehicleBinsMap.put(vehicleId, coordsList);
        }
        return vehicleBinsMap;
    }
}
