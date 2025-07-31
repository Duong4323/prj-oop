package model.vehicle;

import com.mongodb.client.*;

import model.MongoDBConnection;

import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class VehicleDatabase implements AutoCloseable {
    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

    public VehicleDatabase() {
    MongoDatabase database = MongoDBConnection.getDatabase();
    this.collection = database.getCollection("vehicles");  // Sử dụng collection "vehicles"
}

    public void insertVehicle(Vehicle v) {
        Document doc = new Document("_id", v.getId())
                .append("brand", v.getBrand())
                .append("model", v.getModel())
                .append("year", v.getYear());
        collection.insertOne(doc);
        System.out.println("Vehicle inserted successfully.");
    }

    public void updateVehicle(Vehicle v) {
        Document updated = new Document("$set", new Document("brand", v.getBrand())
                .append("model", v.getModel())
                .append("year", v.getYear()));
        collection.updateOne(new Document("_id", v.getId()), updated);
        System.out.println("Vehicle updated successfully.");
    }

    public void deleteVehicleById(String id) {
        collection.deleteOne(new Document("_id", id));
        System.out.println("Vehicle deleted successfully.");
    }

    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        FindIterable<Document> docs = collection.find();
        for (Document doc : docs) {
            String id = doc.getString("_id");
            String brand = doc.getString("brand");
            String model = doc.getString("model");
            int year = doc.getInteger("year", 0);
            vehicles.add(new Vehicle(id, brand, model, year));
        }
        return vehicles;
    }

    @Override
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
