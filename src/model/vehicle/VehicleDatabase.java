package model.vehicle;

import com.mongodb.client.*;
import model.MongoDBConnection;
import model.journey.Journey;
import model.journey.JourneyDatabase;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VehicleDatabase implements AutoCloseable {
    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

    public VehicleDatabase() {
        this.mongoClient = MongoDBConnection.getMongoClient(); // Sử dụng singleton từ MongoDBConnection
        MongoDatabase database = mongoClient.getDatabase(MongoDBConnection.DATABASE_NAME); // Sử dụng hằng số tĩnh
        this.collection = database.getCollection("vehicles");
    }

    public void insertVehicle(Vehicle v) {
        Document doc = new Document("_id", v.getVehicleId())
                .append("licensePlate", v.getLicensePlate())
                .append("brand", v.getBrand())
                .append("model", v.getModel())
                .append("year", v.getYear())
                .append("totalJourneys", v.getTotalJourneys());
        collection.insertOne(doc);
        System.out.println("Vehicle inserted successfully.");
    }

    public void updateVehicle(Vehicle v) {
        Document updated = new Document("$set", new Document("licensePlate", v.getLicensePlate())
                .append("brand", v.getBrand())
                .append("model", v.getModel())
                .append("year", v.getYear())
                .append("totalJourneys", v.getTotalJourneys()));
        collection.updateOne(new Document("_id", v.getVehicleId()), updated);
        System.out.println("Vehicle updated successfully.");
    }

    public void deleteVehicleById(String vehicleId) {
        collection.deleteOne(new Document("_id", vehicleId));
        System.out.println("Vehicle deleted successfully.");
    }

    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        FindIterable<Document> docs = collection.find();
        for (Document doc : docs) {
            vehicles.add(documentToVehicle(doc));
        }
        return vehicles;
    }

    public List<Vehicle> searchVehicleByLicensePlate(String licensePlate) {
        List<Vehicle> result = new ArrayList<>();
        Document query = new Document("licensePlate", new Document("$regex", licensePlate).append("$options", "i"));
        FindIterable<Document> docs = collection.find(query);
        for (Document doc : docs) {
            result.add(documentToVehicle(doc));
        }
        return result;
    }

    public Vehicle getVehicleById(String vehicleId) {
        Document query = new Document("_id", vehicleId);
        Document doc = collection.find(query).first();
        if (doc != null) {
            return documentToVehicle(doc);
        }
        return null;
    }

    private Vehicle documentToVehicle(Document doc) {
        String vehicleId = doc.getString("_id");
        String licensePlate = doc.getString("licensePlate");
        String brand = doc.getString("brand");
        String model = doc.getString("model");
        int year = doc.getInteger("year", 0);
        int totalJourneys = doc.getInteger("totalJourneys", 0);

        Vehicle v = new Vehicle(licensePlate, brand, model, year);
        v.setTotalJourneys(totalJourneys);

        try {
            java.lang.reflect.Field idField = Vehicle.class.getDeclaredField("vehicleId");
            idField.setAccessible(true);
            idField.set(v, vehicleId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return v;
    }

    public void countAndUpdateTotalJourneys() {
        // Lấy tất cả hành trình từ JourneyDatabase
        JourneyDatabase journeyDb = new JourneyDatabase();
        List<Journey> allJourneys;
        try {
            allJourneys = journeyDb.getAllJourneys();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi truy cập JourneyDatabase: " + e.getMessage());
            return;
        } finally {

        }

        var journeyCounts = allJourneys.stream()
                .collect(Collectors.groupingBy(Journey::getVehicleId, Collectors.counting()));

        // Lấy danh sách tất cả xe
        List<Vehicle> vehicles = getAllVehicles();
        for (Vehicle vehicle : vehicles) {
            long journeyCount = journeyCounts.getOrDefault(vehicle.getVehicleId(), 0L);
            vehicle.setTotalJourneys((int) journeyCount); // Cập nhật số hành trình
            updateVehicle(vehicle); // Cập nhật vào cơ sở dữ liệu
        }
    }

    @Override
    public void close() {
    }
}