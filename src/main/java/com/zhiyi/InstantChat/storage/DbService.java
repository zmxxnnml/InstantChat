package com.zhiyi.InstantChat.storage;

import java.util.List;

import com.zhiyi.InstantChat.protobuf.ChatPkg.ChatMessage;

public interface DbService {

	/**
	 * Save chat message.
	 * @return chat message seq
	 */
	public long saveChatMessage(ChatMessage msg);
	
	/**
	 * First sort the messages of the user, then get @{num} messages beginning from @{startp}
	 */
	public List<ChatMessage> getChatMessages(long uid, long startp, long num);
	
	/**
	 * First sort the messages of the user, then get @{num} messages beginning from @{startp}
	 */
	public List<ChatMessage> getDeviceChatMessages(String deviceId, long startp, long num);
	
	/**
	 * Get the @{num} messages which timestamp is
	 * greater(if @{greater}==true) or less(if @{greater}==false) than @{timestamp}.
	 */
	public List<ChatMessage> getChatMessagesByDate(
			long uid, long timestamp, long num, boolean greater);
	
	/**
	 * Get the @{num} messages which timestamp is
	 * greater(if @{greater}==true) or less(if @{greater}==false) than @{timestamp}.
	 */
	public List<ChatMessage> getDeviceChatMessagesByDate(
			String deviceId, long timestamp, long num, boolean greater);
	
	/**
	 * Get message that which seq is between @{startSeq} and @{endSeq}.
	 * User is identified by @{uid}
	 */
	public List<ChatMessage> getChatMessageBySeq(long uid, long startSeq, long endSeq);
	
	/**
	 * Get message that which seq is between @{startSeq} and @{endSeq}.
	 * User is identified by @{deviceId}
	 */
	public List<ChatMessage> getChatMessageBySeq(String deviceId, long startSeq, long endSeq);
	
	/**
	 * Update the ack seq.
	 */
	boolean updateAckSeq(long uid, long newAckSeq);

	/**
	 * Update the ack seq.
	 */
	boolean updateAckSeq(String deviceId, long newAckSeq);
}
