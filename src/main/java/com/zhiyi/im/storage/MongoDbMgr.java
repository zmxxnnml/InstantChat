package com.zhiyi.im.storage;

import java.util.List;

import com.mongodb.DBObject;

public interface MongoDbMgr {

	public void init(String mongoServerAddr, int mongoServerPort, String dbName);

	public boolean isDocumentExsit(String collectionName, DBObject query);

	public List<DBObject> selectDocument(String collectionName, DBObject query);

	public void insertDocument(String collectionName, DBObject newDocument);

	public boolean updateDocument(String collectionName, DBObject query, DBObject updatedDocument);

	public boolean deleteDocument(String collectionName, DBObject query);

	public DBObject findAndModify(String collectionName, DBObject query,
			DBObject updatedDocument);

	public List<DBObject> selectDocumentByPage(String collectionName, DBObject query,
			DBObject sort, long startp, long num);

}
