package routes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RouteManager {
    public RouteManager() {}

    public List<JSONObject> getAvailableVehicles() {
        List<JSONObject> messageList = new ArrayList<>();
        String urlString = "http://172.31.0.110:32080/vehicles/available";
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();
            JSONArray messagesArray = new JSONArray(response.toString());
            for (int i = 0; i < messagesArray.length(); i++) {
                JSONObject msgObj = messagesArray.getJSONObject(i);
                messageList.add(msgObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageList;
    }

    public List<JSONObject> getRouteBins() {
        List<JSONObject> messageList = new ArrayList<>();
        String urlString = "http://172.31.0.110:32081/bins/route";
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();
            JSONArray messagesArray = new JSONArray(response.toString());
            for (int i = 0; i < messagesArray.length(); i++) {
                JSONObject msgObj = messagesArray.getJSONObject(i);
                messageList.add(msgObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageList;
    }
}
