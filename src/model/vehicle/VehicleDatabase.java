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

    // ✅ Thêm chức năng tìm kiếm theo biển số
    public List<Vehicle> searchVehicleByLicensePlate(String licensePlate) {
        List<Vehicle> result = new ArrayList<>();
        Document query = new Document("licensePlate", new Document("$regex", licensePlate).append("$options", "i")); // không phân biệt hoa thường
        FindIterable<Document> docs = collection.find(query);
        for (Document doc : docs) {
            result.add(documentToVehicle(doc));
        }
        return result;
    }

    // ✅ Thêm phương thức getVehicleById
    public Vehicle getVehicleById(String vehicleId) {
        Document query = new Document("_id", vehicleId);
        Document doc = collection.find(query).first();
        if (doc != null) {
            return documentToVehicle(doc);
        }
        return null; // Trả về null nếu không tìm thấy
    }

    // Hàm hỗ trợ chuyển Document MongoDB thành đối tượng Vehicle
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

    @Override
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}