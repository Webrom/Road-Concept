package fr.enssat.lanniontech.repositories;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import fr.enssat.lanniontech.jsonparser.MapJSONParser;
import fr.enssat.lanniontech.jsonparser.entities.Map;
import fr.enssat.lanniontech.repositories.connectors.MongoDBConnector;
import fr.enssat.lanniontech.services.MapService;
import fr.enssat.lanniontech.utilities.Constants;
import fr.enssat.lanniontech.utilities.JSONSerializer;
import org.bson.Document;

import java.util.Random;

public class TestMongoDBRepository {

    public void insertMap(Map map, String collectionName) {
        try (MongoClient mongo = MongoDBConnector.getConnection()) { // try-with-resource block, don't need to call mongo.close()
            MongoDatabase db = mongo.getDatabase(Constants.MONGODB_DATABASE_NAME);
            MongoCollection<Document> collection = db.getCollection(collectionName);

            Document document = Document.parse(JSONSerializer.toJSON(map));
            collection.insertOne(document); // insertMany(List) also available :)
        } catch (Exception e) {
            e.printStackTrace(); // :( TODO: Handle all possible errors (read the doc...)
        }
    }

    public Map getMap(int id, String collectionName) {
        Map map = null;
        try (MongoClient mongo = MongoDBConnector.getConnection()) {
            MongoDatabase db = mongo.getDatabase(Constants.MONGODB_DATABASE_NAME);
            MongoCollection<Document> collection = db.getCollection(collectionName);

            System.out.println("Total items in the collection -> " + collection.count());

            // Display all the collection
            for (Document item : collection.find()) {
                System.out.println(item);
            }

            // Display only the filtered items
            BasicDBObject query = new BasicDBObject("mapID", id);
            for (Document item : collection.find(query)) {
                System.out.println("@@@ FILTERED => " + item);
                map = MapJSONParser.unmarshall(item.toJson());
            }
        }
        return map;
    }

    public static void main(String[] args) {
        //  MongoDatabase db = MongoDBConnector.getConnection().getDatabase(Constants.MONGODB_DATABASE_NAME);
        //  MongoCollection<Document> collection = db.getCollection("test");
        //  collection.drop();

        TestMongoDBRepository repository = new TestMongoDBRepository();

        Map map = new MapService().getMap(null, 1); // Fake data
        map.setId(new Random().nextInt());
        repository.insertMap(map, "test"); // "test" collection *MUST* already exist

        Map fromMongo = repository.getMap(-1513300328, "test");
        System.out.println("fromMongo id => " + fromMongo.getId());

    }
}
