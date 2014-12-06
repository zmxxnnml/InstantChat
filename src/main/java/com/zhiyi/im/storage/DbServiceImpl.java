package com.zhiyi.im.storage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.zhiyi.im.common.StringUtil;
import com.zhiyi.im.config.InstantChatConfig;
import com.zhiyi.im.protobuf.ChatPkg.ChatMessage;

/*
 * http://docs.mongodb.org/manual/tutorial/
 * http://docs.mongodb.org/manual/tutorial/install-mongodb-on-windows/
 * http://docs.mongodb.org/manual/tutorial/getting-started/
 * http://docs.mongodb.org/manual/core/crud-introduction/
 */
public class DbServiceImpl implements DbService {
	private static final Logger logger = Logger.getLogger(DbServiceImpl.class);
	
	private static final String DB_NAME = "instantchat";
	
	private static final String MESSAGE_COLLECTION = "message";
	
	private static final String SEQ_COLLECTION = "seq";
	
	private Lock getNextMsgSeqLock = new ReentrantLock();
	
	private MongoDbMgr mongoDbMgr;
	
	private static DbServiceImpl instance = new DbServiceImpl();
	
	private DbServiceImpl() {}
	
	public static DbServiceImpl getInstance() {
		return instance;
	}
	
	public void init() {
		mongoDbMgr = new MongoDbMgrImpl();
		mongoDbMgr.init(InstantChatConfig.getInstance().getMongoDbAddr(),
				InstantChatConfig.getInstance().getMongoDbPort(), DB_NAME);
	}
	
	@Override
	public boolean updateAckSeq(long uid, long newAckSeq) {
		// if newAckSeq > current ack seq, update. else do nothing
		DBObject queryObj = new BasicDBObject();
		queryObj.put("uid", uid);
		queryObj.put("server_ack_seq", new BasicDBObject().append("$lt", newAckSeq));
		
		DBObject updateObj = new BasicDBObject();
		updateObj.put("$set", new BasicDBObject().append("server_ack_seq", newAckSeq));
		return mongoDbMgr.updateDocument(SEQ_COLLECTION, queryObj, updateObj);
	}
	
	@Override
	public boolean updateAckSeq(String deviceId, long newAckSeq) {
		// if newAckSeq > current ack seq, update. else do nothing
		DBObject queryObj = new BasicDBObject();
		queryObj.put("device_id", deviceId);
		queryObj.put("server_ack_seq", new BasicDBObject().append("$lt", newAckSeq));
		
		DBObject updateObj = new BasicDBObject();
		updateObj.put("$set", new BasicDBObject().append("server_ack_seq", newAckSeq));
		return mongoDbMgr.updateDocument(SEQ_COLLECTION, queryObj, updateObj);
	}
	
	@Override
	public long saveChatMessage(ChatMessage msg) throws StorageException {
		if (msg == null) {
			logger.warn("Chat message is null!");
			return -1;
		}
		
		// Get next seq and seq
		long nextSeq = getNextServerSeq(msg.getToUid(), msg.getToDeviceId());
		ChatMessage.Builder b = ChatMessage.newBuilder(msg);
		b.setSeq(nextSeq);
		
		DBObject insertObj = new BasicDBObject();
		insertObj.put("seq", nextSeq);
		insertObj.put("timestamp", b.getUserSendTime());
		insertObj.put("to_uid", b.getToUid());
		insertObj.put("to_device_id", b.getToDeviceId());
		insertObj.put("from_uid", b.getFromUid());
		insertObj.put("from_device_id", b.getFromDeviceId());
		
		String str = "";
		try {
			str = new String(msg.toByteArray(), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			logger.error("Invalid messag from db : ", e);
			throw new StorageException(e);
		}
		insertObj.put("msg", str);

		mongoDbMgr.insertDocument(MESSAGE_COLLECTION, insertObj);
		
		return nextSeq;
	}

	@Override
	public List<ChatMessage> getChatMessages(long uid, long startp, long num) {
		DBObject queryObj = new BasicDBObject();
		queryObj.put("to_uid", uid);
		
		DBObject sortObj = new BasicDBObject();
		sortObj.put("timestamp", -1);
		
		List<DBObject> dbObjs = mongoDbMgr.selectDocumentByPage(
				MESSAGE_COLLECTION, queryObj, sortObj, startp, num);

		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		try {
			for (DBObject obj : dbObjs) {
				ChatMessage msg = ChatMessage.parseFrom(obj.get("msg")
						.toString().getBytes("ISO-8859-1"));
				messages.add(msg);
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("Invalid messag from db : ", e);
			return null;
		} catch (UnsupportedEncodingException e) {
			logger.error("Invalid messag from db : ", e);
			return null;
		}
		
		return messages;
	}

	@Override
	public List<ChatMessage> getDeviceChatMessages(String deviceId, long startp, long num) {
		DBObject queryObj = new BasicDBObject();
		queryObj.put("to_device_id", deviceId);
		
		DBObject sortObj = new BasicDBObject();
		sortObj.put("timestamp", -1);
		
		List<DBObject> dbObjs = mongoDbMgr.selectDocumentByPage(
				MESSAGE_COLLECTION, queryObj, sortObj, startp, num);

		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		try {
			for (DBObject obj : dbObjs) {
				ChatMessage msg = ChatMessage.parseFrom(obj.get("msg")
						.toString().getBytes("ISO-8859-1"));
				messages.add(msg);
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("Invalid messag from db : ", e);
			return null;
		} catch (UnsupportedEncodingException e) {
			logger.error("Invalid messag from db : ", e);
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
				ChatMessage msg = ChatMessage.parseFrom(obj.get("msg")
						.toString().getBytes("ISO-8859-1"));
				messages.add(msg);
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("Invalid messag from db : ", e);
			return null;
		} catch (UnsupportedEncodingException e) {
			logger.error("Invalid messag from db : ", e);
			return null;
		}
		
		return messages;
	}

	@Override
	public List<ChatMessage> getChatMessageBySeq(String deviceId,
			long startSeq, long endSeq) {
		logger.info("Get messages by { deviceId" + deviceId + "; startSeq: "
				+ startSeq + "; endSeq: " + endSeq + " }");

		DBObject queryObj = new BasicDBObject();
		queryObj.put("to_device_id", deviceId);
		queryObj.put("seq", new BasicDBObject().append("$gt", startSeq));
		queryObj.put("seq", new BasicDBObject().append("$lt", endSeq));

		List<DBObject> dbObjs = mongoDbMgr.selectDocument(MESSAGE_COLLECTION,
				queryObj);
		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		try {
			for (DBObject obj : dbObjs) {
				ChatMessage msg = ChatMessage.parseFrom(obj.get("msg")
						.toString().getBytes("ISO-8859-1"));
				messages.add(msg);
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("Invalid messag from db : ", e);
			return null;
		} catch (UnsupportedEncodingException e) {
			logger.error("Invalid messag from db : ", e);
			return null;
		}
		
		return messages;
	}

	@Override
	public List<ChatMessage> getChatMessagesByTimestamp(long uid, long timestamp,
			long num, boolean greater) {
		DBObject queryObj = new BasicDBObject();
		queryObj.put("to_uid", uid);
		if (greater) {
			queryObj.put("timestamp", new BasicDBObject().append("$gt", timestamp));
		} else {
			queryObj.put("timestamp", new BasicDBObject().append("$lt", timestamp));
		}
		
		DBObject sortObj = new BasicDBObject();
		if (greater) {
			sortObj.put("timestamp", 1);
		} else {
			sortObj.put("timestamp", -1);
		}
		
		List<DBObject> dbObjs = mongoDbMgr.selectDocumentByPage(
				MESSAGE_COLLECTION, queryObj, sortObj, 0, num);

		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		try {
			for (DBObject obj : dbObjs) {
				ChatMessage msg = ChatMessage.parseFrom(obj.get("msg")
						.toString().getBytes("ISO-8859-1"));
				messages.add(msg);
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("Invalid messag from db : ", e);
			return null;
		} catch (UnsupportedEncodingException e) {
			logger.error("Invalid messag from db : ", e);
			return null;
		}
		
		return messages;
	}

	@Override
	public List<ChatMessage> getDeviceChatMessagesByTimestamp(String deviceId,
			long timestamp, long num, boolean greater) {
		DBObject queryObj = new BasicDBObject();
		queryObj.put("to_device_id", deviceId);
		if (greater) {
			queryObj.put("timestamp", new BasicDBObject().append("$gt", timestamp));
		} else {
			queryObj.put("timestamp", new BasicDBObject().append("$lt", timestamp));
		}
		
		DBObject sortObj = new BasicDBObject();
		if (greater) {
			sortObj.put("timestamp", 1);
		} else {
			sortObj.put("timestamp", -1);
		}
		
		List<DBObject> dbObjs = mongoDbMgr.selectDocumentByPage(
				MESSAGE_COLLECTION, queryObj, sortObj, 0, num);

		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		try {
			for (DBObject obj : dbObjs) {
				ChatMessage msg = ChatMessage.parseFrom(obj.get("msg")
						.toString().getBytes("ISO-8859-1"));
				messages.add(msg);
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("Invalid messag from db : ", e);
			return null;
		} catch (UnsupportedEncodingException e) {
			logger.error("Invalid messag from db : ", e);
			return null;
		}
		
		return messages;
	}

	private long getNextServerSeq(Long uid, String deviceId) {
		logger.info("Get next server seq for: " + "{uid: " + uid + "; deviceId: " + deviceId + "}");
		DBObject queryObj = new BasicDBObject();
		if (uid != null) {
			queryObj.put("uid", uid);
		} else if (!StringUtil.isBlank(deviceId)) {
			queryObj.put("device_id", deviceId);
		}
		DBObject updateObj = new BasicDBObject();
		updateObj.put("$inc", new BasicDBObject().append("server_max_seq", 1));

		// TODO: There are 2 issues of the following code
		// 1. Lock make the function slowly
		// 2. Even we use lock, it's still not safe under distribute environment. Change to distribution lock.
		DBObject result = null;
		getNextMsgSeqLock.lock();  // Use lock to avoid duplicated insert.
		try {
			result = mongoDbMgr.findAndModify(SEQ_COLLECTION, queryObj, updateObj);
			if (result == null) {
				logger.info("Get and update seq return null.");
				DBObject insertObj = new BasicDBObject();
				if (uid != null) {
					insertObj.put("uid", uid);
				}
				if (!StringUtil.isBlank(deviceId)) {
					insertObj.put("device_id", deviceId);
				}
				insertObj.put("server_max_seq", 1);
				insertObj.put("acked_max_seq", 0);
				mongoDbMgr.insertDocument(SEQ_COLLECTION, insertObj);
				return 1;
			}
		} finally {
			getNextMsgSeqLock.unlock();
		}
		
		Object v = result.get("server_max_seq");
		Long seqId = Long.parseLong(v.toString());
		return seqId + 1;
	}
	
}
