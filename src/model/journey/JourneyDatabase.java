package model.journey;

import com.mongodb.client.*;
import model.MongoDBConnection;
import model.vehicle.Vehicle;
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
                .append("startTime", j.getStartTime().toString())
                .append("endTime", j.getEndTime().toString())
                .append("distance", j.getDistance())
                .append("sensorData", j.getSensorDataList().stream().map(SensorReading::toDocument).collect(Collectors.toList()));
        collection.insertOne(doc);
        // Cập nhật id sau khi insert (MongoDB tự sinh ObjectId)
        Document insertedDoc = collection.find(doc).first();
        if (insertedDoc != null) {
            j.setId(insertedDoc.getObjectId("_id").toString());
        }
        System.out.println("Journey inserted successfully.");

        updateVehicleJourneyCount(j.getVehicleId());
    }

    private static void updateVehicleJourneyCount(String vehicleId) {
        long count = collection.countDocuments(new Document("vehicleId", vehicleId));

        try (VehicleDatabase vehicleDb = new VehicleDatabase()) {
            List<Vehicle> list = vehicleDb.getAllVehicles();
            for (Vehicle v : list) {
                if (v.getVehicleId().equals(vehicleId)) {
                    v.setTotalJourneys((int) count);
                    vehicleDb.updateVehicle(v);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                        .append("startTime", journey.getStartTime().toString())
                        .append("endTime", journey.getEndTime().toString())
                        .append("distance", journey.getDistance())
                        .append("sensorData", journey.getSensorDataList().stream().map(SensorReading::toDocument).collect(Collectors.toList())));
        collection.updateOne(filter, update);
        System.out.println("Journey updated successfully.");

        updateVehicleJourneyCount(journey.getVehicleId());
    }

    // Cập nhật phương thức deleteJourney sử dụng id
    public static void deleteJourney(String journeyId) {
        if (journeyId == null || journeyId.isEmpty()) {
            throw new IllegalArgumentException("Journey ID is required for deletion.");
        }
        Document filter = new Document("_id", new ObjectId(journeyId));
        collection.deleteOne(filter);
        System.out.println("Journey deleted successfully.");
    }
}