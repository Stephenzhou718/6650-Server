package dao;

import Skier.SkiersLog;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

public class MongoDBService {

  private MongoDatabase database;

  public MongoDBService() {
    String connectionString = "mongodb+srv://stephenzhou718:mJhJvq8iW6TBj6gh@cluster-oregon.phv2lgu.mongodb.net/?retryWrites=true&w=majority&appName=cluster-oregon";
    String dbName = "6650";

    CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    MongoClientSettings clientSettings = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(connectionString))
        .codecRegistry(pojoCodecRegistry)
        .build();

    MongoClient mongoClient = MongoClients.create(clientSettings);
    this.database = mongoClient.getDatabase(dbName);

    // Select the database
    this.database = mongoClient.getDatabase(dbName);
  }

  public void addSkierLog(SkiersLog log) {
    // Get the collection
    String collectionName = "skiers-data";
    MongoCollection<SkiersLog> collection = database.getCollection(collectionName, SkiersLog.class);

    // Insert the document
    collection.insertOne(log);

    System.out.println("Document added to the collection successfully.");
  }

  public long getDays(String seasonID, String skierID) {
    String collectionName = "skiers-data";
    MongoCollection<Document> collection = database.getCollection(collectionName, Document.class);

    Bson match = new Document("$match", new Document("skierID", skierID)
        .append("seasonID", seasonID));
    Bson group = new Document("$group", new Document("_id", "$dayID"));
    Bson count = new Document("$count", "uniqueDaysSkied");

    AggregateIterable<Document> result = collection.aggregate(Arrays.asList(match, group, count));
    if (result.iterator().hasNext()) {
      Document doc = result.first();
      System.out.println("Skier " + skierID + " has skied " + doc.getInteger("uniqueDaysSkied") + " unique days in season " + seasonID + ".");
      return doc.getInteger("uniqueDaysSkied");
    } else {
      System.out.println("No skiing data found for Skier " + skierID + " in season " + seasonID + ".");
      return 0;
    }
  }

  public long getSkiersForDay(String resortID, String dayID) {
    String collectionName = "skiers-data";
    MongoCollection<Document> collection = database.getCollection(collectionName, Document.class);

    Bson match = new Document("$match", new Document("resortID", resortID).append("dayID", dayID));
    Bson group = new Document("$group", new Document("_id", "$skierID")); // Group by skierID
    Bson count = new Document("$count", "uniqueSkiers");

    AggregateIterable<Document> result = collection.aggregate(Arrays.asList(match, group, count));

    if (result.iterator().hasNext()) {
      Document doc = result.first();
      System.out.println("Unique skiers visited resort " + resortID + " on day " + dayID + ": " + doc.getInteger("uniqueSkiers"));
      return doc.getInteger("uniqueSkiers");
    } else {
      System.out.println("No skiers visited resort " + resortID + " on day " + dayID + ".");
      return 0;
    }
  }

  public List<int[]> getSkierDayVertical(String skierID) {
    String collectionName = "skiers-data";
    MongoCollection<Document> collection = database.getCollection(collectionName, Document.class);

    Bson match = new Document("$match", new Document("skierID", skierID));
    Bson project = new Document("$project", new Document("dayID", 1)
        .append("vertical", new Document("$multiply", Arrays.asList("$liftID", 10))));
    Bson group = new Document("$group", new Document("_id", "$dayID")
        .append("totalVertical", new Document("$sum", "$vertical")));
    Bson sort = new Document("$sort", new Document("_id", 1)); // Optional: sorts results by dayID

    AggregateIterable<Document> result = collection.aggregate(Arrays.asList(match, project, group, sort));
    List<int[]> ans = new ArrayList<>();
    for (Document doc : result) {
      System.out.println("Day: " + doc.getInteger("_id") + ", Total Vertical: " + doc.getInteger("totalVertical"));
      ans.add(new int[]{doc.getInteger("_id"), doc.getInteger("totalVertical")});
    }
    return ans;
  }

  public Map<Integer, List<Integer>> getLiftsOfEachDay(String skierID) {
    String collectionName = "skiers-data";
    MongoCollection<Document> collection = database.getCollection(collectionName, Document.class);

    Bson match = new Document("$match", new Document("skierID", skierID));

    // Group by dayID and collect liftIDs into a list
    Bson group = new Document("$group", new Document("_id", "$dayID")
        .append("lifts", new Document("$push", "$liftID")));

    // Optional: sort by dayID to ensure chronological order
    Bson sort = new Document("$sort", new Document("_id", 1));

    // Aggregate the documents
    AggregateIterable<Document> result = collection.aggregate(Arrays.asList(match, group, sort));

    // Iterate through the results and print them
    Map<Integer, List<Integer>> ans = new HashMap<>();
    for (Document doc : result) {
      System.out.println("Day: " + doc.getInteger("_id") + ", Lifts: " + doc.getList("lifts", Integer.class));
      ans.put(doc.getInteger("_id"), doc.getList("lifts", Integer.class));
    }
    return ans;
  }

  // Close the MongoDB client connection
  public void closeClient(MongoClient mongoClient) {
    if (mongoClient != null) {
      mongoClient.close();
    }
  }
}
