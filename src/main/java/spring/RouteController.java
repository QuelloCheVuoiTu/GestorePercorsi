package spring;

import json.JSONManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/routes")
public class RouteController {
    private final JSONManager jsonManager = new JSONManager();

    @CrossOrigin(origins = "*")
    @GetMapping("/addDataToDB")
    public ResponseEntity<Void> addDataToDB() {
        jsonManager.addJsonToDB();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/printGeometries")
    public String printAllGeometries() {
        StringBuilder sb = new StringBuilder();
        jsonManager.getVehicleGeometryMap().forEach((id, geometry) -> sb.append("ID: ").append(id).append("<br>Geometry: ").append(geometry).append("<br><br><br><br><br>"));
        return sb.toString();
    }

    @GetMapping("/geometries")
    public Map<Integer, String> getGeometries() {
        return jsonManager.getVehicleGeometryMap();
    }

    @GetMapping("/geometries/{id}")
    public String getGeometryById(@PathVariable int id) {
        return jsonManager.getVehicleGeometryMap().get(id);
    }

    @GetMapping("bins/{id}")
    public List<String> getBinsById(@PathVariable int id) {
        return jsonManager.getVehicleBinsMap().get(id);
    }
}
