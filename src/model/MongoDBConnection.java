package model;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME     = "vehicle_tracker";

    // Singleton MongoClient
    private static MongoClient mongoClient;

    /** 
     * Trả về MongoClient singleton 
     */
    public static MongoClient getMongoClient() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            System.out.println("✅ Connected to MongoDB");
        }
        return mongoClient;
    }

    /**
     * Trả về MongoDatabase dùng chung cho toàn ứng dụng
     */
    public static MongoDatabase getDatabase() {
        return getMongoClient().getDatabase(DATABASE_NAME);
    }

    /**
     * Đóng kết nối MongoClient khi ứng dụng tắt
     */
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            System.out.println("🔌 MongoDB connection closed");
        }
    }
}
