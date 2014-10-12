package com.zhiyi.InstantChat.storage;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class MongoDbMgrImpl implements MongoDbMgr {
	private static final Logger logger = Logger.getLogger(MongoDbMgrImpl.class);

    private MongoClient mongo = null;
    private DB dbConnection = null;
    private static Map<String, DBCollection> dbCollectionMap
    	= new ConcurrentHashMap<String, DBCollection>();
    
    @Override
    public void init(String mongoServerAddr, int mongoServerPort, String dbName) {
        if (this.mongo == null) {
            try {
                this.mongo = new MongoClient( mongoServerAddr , mongoServerPort);
                if (null != this.mongo) {
                    this.dbConnection = this.mongo.getDB(dbName);
                    logger.info("connected to mongo db.");
                }
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private DBCollection getDBCollection(String collectionName) {
        DBCollection collection = null;
        if (dbCollectionMap.containsKey(collectionName)) {
            collection = dbCollectionMap.get(collectionName);
        } else {
            collection = this.dbConnection.getCollection(collectionName);
            if (null != collection) {
                dbCollectionMap.put(collectionName, collection);
            }
        }
        return collection;
    }
    
    @Override
    public boolean isDocumentExsit(String collectionName, DBObject query) {
        boolean result = false;
        DBCursor dbCursor = null;
        DBCollection collection = this.getDBCollection(collectionName);
        if (null != collection) {
            dbCursor = collection.find(query);
            if (null != dbCursor && dbCursor.hasNext()) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public List<DBObject> selectDocument(String collectionName, DBObject query) {
    	List<DBObject> result = new ArrayList<DBObject>();
        DBCursor dbCursor = null;
        DBCollection collection = this.getDBCollection(collectionName);
        if (null != collection) {
            dbCursor = collection.find(query);
            while (null != dbCursor && dbCursor.hasNext()) {
            	result.add(dbCursor.next());
            }
        }
        return result;
    }
    
    @Override
    public List<DBObject> selectDocumentByPage(
    		String collectionName, DBObject query, DBObject sort, long startp, long num) {
    	List<DBObject> result = new ArrayList<DBObject>();
        DBCursor dbCursor = null;
        DBCollection collection = this.getDBCollection(collectionName);
        if (null != collection) {
            dbCursor = collection.find(query).sort(sort).skip((int)startp).limit((int)num);
            while (null != dbCursor && dbCursor.hasNext()) {
            	result.add(dbCursor.next());
            }
        }
        return result;
    }

    @Override
    public void insertDocument(String collectionName, DBObject newDocument) {
        DBCollection collection = this.getDBCollection(collectionName);
        if (null != collection) {
            if (!this.isDocumentExsit(collectionName, newDocument)) { 
                collection.insert(newDocument);
            }
        }
    }
    
    @Override
    public boolean updateDocument(
    		String collectionName, DBObject query, DBObject updatedDocument) {
        boolean result = false;
        WriteResult writeResult = null;
        DBCollection collection = this.getDBCollection(collectionName);
        if (null != collection) {
            writeResult = collection.update(query, updatedDocument);
            
            if (null != writeResult) {
                if (writeResult.getN() > 0) {
                    result = true;
                }
            }
        }
        return result;
    }
    
    @Override
    public DBObject findAndModify(String collectionName, DBObject query, DBObject updatedDocument) {
        DBObject result = null;
        DBCollection collection = this.getDBCollection(collectionName);
        if (null != collection) {
        	result = collection.findAndModify(query, updatedDocument);
        }
        return result;
    }
    
    @Override
    public boolean deleteDocument(String collectionName, DBObject query) {
        boolean result = false;
        WriteResult writeResult = null;
        DBCollection collection = this.getDBCollection(collectionName);
        if (null != collection) {
            writeResult = collection.remove(query);
            if (null != writeResult) {
                if (writeResult.getN() > 0) {
                    result = true;
                }
            }
        }
        return result;
    }
    
}
