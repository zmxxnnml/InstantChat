package com.zhiyi.InstantChat.storage;

import java.util.List;

import com.mongodb.DBObject;

public interface MongoDbMgr {

	void init(String mongoServerAddr, int mongoServerPort, String dbName);

	boolean isDocumentExsit(String collectionName, DBObject query);

	List<DBObject> selectDocument(String collectionName, DBObject query);

	void insertDocument(String collectionName, DBObject newDocument);

	boolean updateDocument(String collectionName, DBObject query, DBObject updatedDocument);

	boolean deleteDocument(String collectionName, DBObject query);

	DBObject findAndModify(String collectionName, DBObject query,
			DBObject updatedDocument);

	List<DBObject> selectDocumentByPage(String collectionName, DBObject query,
			DBObject sort, long startp, long num);

}
