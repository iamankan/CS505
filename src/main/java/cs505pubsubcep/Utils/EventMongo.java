package cs505pubsubcep.Utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import cs505pubsubcep.database.MongoEngine;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventMongo implements DBImpl {
    public MongoEngine mongoEngine;
    public MongoDatabase mongoDatabase;
    public MongoCollection<Document> collection;
    public String COLLECTION_NAME = "event";

    public Map<String, String> getEventMap() {
        return eventMap;
    }

    public void setEventMap(Map<String, String> eventMap) {
        this.eventMap = eventMap;
    }

    public Map<String, String> eventMap;

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    public void setCollection(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public EventMongo(MongoEngine mongoEngine, MongoDatabase mongoDatabase) {
        this.mongoEngine = mongoEngine;
        this.mongoDatabase = mongoDatabase;
        this.collection = this.mongoDatabase.getCollection(COLLECTION_NAME);
        System.out.println("Inilialized mongo - "+COLLECTION_NAME);
    }


    public MongoEngine getMongoEngine() {
        return this.mongoEngine;
    }

    public void setMongoEngine(MongoEngine mongoEngine) {
        this.mongoEngine = mongoEngine;
    }

    public MongoDatabase getMongoDatabase() {
        return this.mongoDatabase;
    }

    public void setMongoDatabase(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    public boolean update() {
        for(String key : this.eventMap.keySet()) {
            Bson filter = Filters.eq("type","event");
            Bson update = Updates.push(key, this.eventMap.get(key));



            UpdateOptions options = new UpdateOptions().upsert(true);
            System.out.println(collection.updateOne(filter, update, options));

//            FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
//                    .returnDocument(ReturnDocument.AFTER);
//            Document result = collection.findOneAndUpdate(filter, update, options);
//            System.out.println(result.toJson());
        }
        return false;
    }

    @Override
    public boolean delete() {

        boolean success= true;

        BasicDBObject document = new BasicDBObject();

        try {

            MongoCollection collection = mongoDatabase.getCollection(COLLECTION_NAME);
            // Delete All documents from collection Using blank BasicDBObject
            collection.deleteMany(document);
            System.out.println("Successfully deleted data documents in "+COLLECTION_NAME);
        }catch (Exception e){
            e.printStackTrace();
            success=false;
        }

        return success;


    }

    @Override
    public boolean insert(Document document) {
        boolean succcess = true;
        try {
            MongoCollection<Document> collection = mongoDatabase.getCollection(COLLECTION_NAME);
            ObjectId objectId = new ObjectId();
            collection.insertOne(document.append("_id", objectId));
            System.out.println("Successfully inserted _id: "+ document.get("_id")+" to "+COLLECTION_NAME);
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
            succcess=false;
        }
        return succcess;
    }

    public Map<String, ArrayList<String>> getEventContactList(ArrayList<String> mrnsList){
        FindIterable<Document> mongoIter = this.collection.find();
        try {
            ArrayList<String> mrnList = new ArrayList<String>();
            Document document  = mongoIter.first();

            Map<String, ArrayList<String>> eventContactList = new HashMap<String, ArrayList<String>>();

            document.forEach((key, value) -> {
                if(!(key.equals("type") || key.equals("_id"))){
                    String event = key;
                    System.out.println("value: "+value);
                    ArrayList<String> contactList = (ArrayList<String>) value;

//                    ArrayList<String> fCont = new ArrayList<String>();
//                    fCont = (ArrayList<String>) value;
//                    System.out.println("fCont: "+fCont);
//                    for(Object str: fCont){
//                        System.out.println("Content: "+str);
//                    }
                    if(contactList!=null) {
                        System.out.println("contactList: " + contactList);
                        System.out.println("mrnsList: " + mrnsList);
                        contactList.retainAll(mrnsList);
                        if (contactList.size() > 0) {
                            eventContactList.put(key, contactList);
                        }
                    }
                    System.out.println(eventContactList);
                }
            });
            return  eventContactList;
        }catch (NullPointerException nex){
            nex.printStackTrace();
            return new HashMap<String, ArrayList<String>>();
        }
    }
}
