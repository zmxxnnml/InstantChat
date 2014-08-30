package com.zhiyi.InstantChat.storage;

import java.util.List;

import com.zhiyi.InstantChat.protobuf.ChatPkg.ChatMessage;

public interface DbService {

	// Return chat message seq
	public long saveChatMessage(ChatMessage msg);
	
	// For reading history message
	public List<ChatMessage> getChatMessageByDate(long uid, long startp, long num);
	
	// For reading history message
	public List<ChatMessage> getDeviceChatMessageByDate(String deviceId, long startp, long num);
	
	// For getting new message
	public List<ChatMessage> getChatMessageBySeq(long uid, long startSeq, long endSeq);
	
	// For getting new message
	public List<ChatMessage> getChatMessageBySeq(String deviceId, long startSeq, long endSeq);

	boolean updateAckSeq(long uid, long newAckSeq);

}
