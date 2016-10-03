package fr.enssat.lanniontech.services;

import fr.enssat.lanniontech.entities.MapInfo;
import fr.enssat.lanniontech.entities.User;
import fr.enssat.lanniontech.jsonparser.MapJSONParser;
import fr.enssat.lanniontech.jsonparser.entities.Map;
import fr.enssat.lanniontech.repositories.MapRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MapService {

    private MapRepository repository = new MapRepository();

    public List<MapInfo> getAllMapsInfo(User user) {
        //TODO: Retrive data from db. Now is just fake data
        List<MapInfo> result = new ArrayList<>();

        MapInfo map1 = new MapInfo();
        map1.setId(1);
        map1.setName("Ville de Lannion");

        MapInfo map2 = new MapInfo();
        map2.setId(2);
        map2.setImageURL("http://www.notempire.com/images/uploads/poke-3.jpg");
        map2.setName("Pokemon GO Plan");

        MapInfo map3 = new MapInfo();
        map3.setId(76);
        map3.setImageURL("http://pcad.lib.washington.edu/media/geo-images/gmap/2632.png");
        map3.setName("JFK Plazza area");

        result.add(map1);
        result.add(map2);
        result.add(map3);
        return result;
    }

    public Map getMap(User user, int mapID) {
        //TODO: Retrive data from db. Now is just example data
        File json = new File("src/test/resources/map_all.json");
        Map map = MapJSONParser.unmarshall(json);
        // TODO: Check mapID incorrect (400)
        // TODO: Check mapID pour 404
        // TODO: Check mapID pas consistent avec l'user logué
        return map;
    }
}
