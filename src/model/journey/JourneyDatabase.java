package model.journey;

import com.mongodb.client.*;
import model.MongoDBConnection;
import model.vehicle.VehicleDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JourneyDatabase {
    private static final MongoCollection<Document> collection =
            MongoDBConnection.getDatabase().getCollection("journeys");

    public static void addJourney(Journey j) {
        Document doc = new Document()
                .append("vehicleId", j.getVehicleId())
                .append("totalTime", j.getTotalTime())
                .append("distance", j.getDistance())
                .append("averageSpeed", j.getAverageSpeed())
                .append("averageRpm", j.getAverageRpm())
                .append("maxRpm", j.getMaxRpm())
                .append("fuelConsumption", j.getFuelConsumption())
                .append("sensorDataList", j.getSensorDataList().stream().map(SensorReading::toDocument).collect(Collectors.toList()));
        collection.insertOne(doc);
        // Cập nhật id sau khi insert (MongoDB tự sinh ObjectId)
        Document insertedDoc = collection.find(doc).first();
        if (insertedDoc != null) {
            j.setId(insertedDoc.getObjectId("_id").toString());
        }
        System.out.println("Journey inserted successfully.");

        updateVehicleJourneyCount();
    }

    private static void updateVehicleJourneyCount() {
        try (VehicleDatabase vehicleDb = new VehicleDatabase()) {
            vehicleDb.countAndUpdateTotalJourneys();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi cập nhật tổng số hành trình của xe: " + e.getMessage());
        }
    }

    public static List<Journey> getAllJourneys() {
        List<Journey> list = new ArrayList<>();
        FindIterable<Document> docs = collection.find();
        for (Document d : docs) {
            list.add(Journey.fromDocument(d));
        }
        return list;
    }

    // Cập nhật phương thức updateJourney sử dụng id
    public static void updateJourney(Journey journey) {
        if (journey.getId() == null || journey.getId().isEmpty()) {
            throw new IllegalArgumentException("Journey ID is required for update.");
        }
        Document filter = new Document("_id", new ObjectId(journey.getId()));
        Document update = new Document()
                .append("$set", new Document()
                        .append("vehicleId", journey.getVehicleId())
                        .append("totalTime", journey.getTotalTime())
                        .append("distance", journey.getDistance())
                        .append("averageSpeed", journey.getAverageSpeed())
                        .append("averageRpm", journey.getAverageRpm())
                        .append("maxRpm", journey.getMaxRpm())
                        .append("fuelConsumption", journey.getFuelConsumption())
                        .append("sensorDataList", journey.getSensorDataList().stream().map(SensorReading::toDocument).collect(Collectors.toList())));
        collection.updateOne(filter, update);
        System.out.println("Journey updated successfully.");

        updateVehicleJourneyCount();
    }

    // Cập nhật phương thức deleteJourney sử dụng id
    public static void deleteJourney(String journeyId) {
        if (journeyId == null || journeyId.isEmpty()) {
            throw new IllegalArgumentException("Journey ID is required for deletion.");
        }
        Document filter = new Document("_id", new ObjectId(journeyId));
        collection.deleteOne(filter);
        System.out.println("Journey deleted successfully.");

        updateVehicleJourneyCount();
    }
}