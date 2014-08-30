package com.zhiyi.InstantChat.storage;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.Binary;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.zhiyi.InstantChat.protobuf.ChatPkg.ChatMessage;

public class MongoDbServiceImpl implements DbService {

	private static final String MONGODB_HOST = "localhost";
	
	private static final Short MONGODB_PORT = 27017;
	
	private static final String DB_NAME = "instantchat";
	
	private static final String MESSAGE_COLLECTION = "message";
	
	private static final String SEQ_COLLECTION = "seq";
	
	private MongoDbMgr mongoDbMgr;
	
	public MongoDbServiceImpl() {}
	
	public void init() throws StorageException {
		mongoDbMgr = new MongoDbMgrImpl();
		mongoDbMgr.init(MONGODB_HOST, MONGODB_PORT, DB_NAME);
	}
	
	private long getNextServerSeq(long uid) {
		DBObject queryObj = new BasicDBObject();
		queryObj.put("uid", uid);
		DBObject updateObj = new BasicDBObject();
		updateObj.put("$inc", new BasicDBObject().append("server_max_seq", 1));
		// TODO: make it to be atomic
		DBObject result =
				mongoDbMgr.findAndModify(SEQ_COLLECTION, queryObj, updateObj);
		if (result == null) {
			DBObject insertObj = new BasicDBObject();
			insertObj.put("uid", uid);
			insertObj.put("server_max_seq", 1);
			insertObj.put("acked_max_seq", 0);
			mongoDbMgr.insertDocument(SEQ_COLLECTION, insertObj);
			return 1;
		}
		
		return (Long) result.get("server_max_seq");
	}
	
	@Override
	public boolean updateAckSeq(long uid, long newAckSeq) {
		// TODO: if newAckSeq > current ack seq, update. else do nothing
		DBObject queryObj = new BasicDBObject();
		queryObj.put("uid", uid);
		DBObject updateObj = new BasicDBObject();
		updateObj.put("$set", new BasicDBObject().append("server_ack_seq", newAckSeq));
		return mongoDbMgr.updateDocument(SEQ_COLLECTION, queryObj, updateObj);
	}
	
	@Override
	public long saveChatMessage(ChatMessage msg) {
		if (msg == null) {
			return -1;
		}
		
		// Get next seq and seq
		long nextSeq = getNextServerSeq(msg.getToUid());
		ChatMessage.Builder b = ChatMessage.newBuilder(msg);
		b.setSeq(nextSeq);
		
		DBObject insertObj = new BasicDBObject();
		insertObj.put("seq", nextSeq);
		insertObj.put("time", b.getUserSendTime());
		insertObj.put("to_uid", b.getToUid());
		insertObj.put("from_uid", b.getFromUid());
		insertObj.put("from_device_id", b.getFromDeviceId());
		insertObj.put("to_device_id", b.getToDeviceId());
		insertObj.put("msg", b.build().toByteArray());
		
		mongoDbMgr.insertDocument(MESSAGE_COLLECTION, insertObj);
		return 0;
	}

	@Override
	public List<ChatMessage> getChatMessageByDate(long uid, long startp, long num) {
		DBObject queryObj = new BasicDBObject();
		queryObj.put("to_uid", uid);
		
		DBObject sortObj = new BasicDBObject();
		sortObj.put("time", -1);
		
		List<DBObject> dbObjs = mongoDbMgr.selectDocumentByPage(
				MESSAGE_COLLECTION, queryObj, sortObj, startp, num);

		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		try {
			for (DBObject obj : dbObjs) {
				Binary msgBinary = (Binary) obj.get("msg");
				ChatMessage msg = ChatMessage.parseFrom(msgBinary.getData());
				messages.add(msg);
			}
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			messages.clear();
			return null;
		}
		
		return messages;
	}

	@Override
	public List<ChatMessage> getDeviceChatMessageByDate(String deviceId, long startp, long num) {
		DBObject queryObj = new BasicDBObject();
		queryObj.put("to_device_id", deviceId);
		
		DBObject sortObj = new BasicDBObject();
		sortObj.put("time", -1);
		
		List<DBObject> dbObjs = mongoDbMgr.selectDocumentByPage(
				MESSAGE_COLLECTION, queryObj, sortObj, startp, num);

		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		try {
			for (DBObject obj : dbObjs) {
				Binary msgBinary = (Binary) obj.get("msg");
				ChatMessage msg = ChatMessage.parseFrom(msgBinary.getData());
				messages.add(msg);
			}
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			messages.clear();
			return null;
		}
		
		return messages;
	}

	@Override
	public List<ChatMessage> getChatMessageBySeq(long uid, long startSeq,long endSeq) {
		DBObject queryObj = new BasicDBObject();
		queryObj.put("to_uid", uid);
		queryObj.put("seq", new BasicDBObject().append("$gt", startSeq));
		queryObj.put("seq", new BasicDBObject().append("$lt", endSeq));
		
		List<DBObject> dbObjs = mongoDbMgr.selectDocument(MESSAGE_COLLECTION, queryObj);

		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		try {
			for (DBObject obj : dbObjs) {
				Binary msgBinary = (Binary) obj.get("msg");
				ChatMessage msg = ChatMessage.parseFrom(msgBinary.getData());
				messages.add(msg);
			}
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			messages.clear();
			return null;
		}
		
		return messages;
	}

	@Override
	public List<ChatMessage> getChatMessageBySeq(String deviceId, long startSeq, long endSeq) {
		DBObject queryObj = new BasicDBObject();
		queryObj.put("to_device_id", deviceId);
		queryObj.put("seq", new BasicDBObject().append("$gt", startSeq));
		queryObj.put("seq", new BasicDBObject().append("$lt", endSeq));
		
		List<DBObject> dbObjs = mongoDbMgr.selectDocument(MESSAGE_COLLECTION, queryObj);

		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		try {
			for (DBObject obj : dbObjs) {
				Binary msgBinary = (Binary) obj.get("msg");
				ChatMessage msg = ChatMessage.parseFrom(msgBinary.getData());
				messages.add(msg);
			}
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			messages.clear();
			return null;
		}
		
		return messages;
	}

}
