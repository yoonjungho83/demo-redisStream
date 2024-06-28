package com.demo.redis.support;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisOperator {

	
	private final RedisTemplate<String , Object> redisTemplate;
	
	public Object getRedisValue(String key , String field) {
		
		return redisTemplate.opsForHash().get(key, field);
	}
	
	public long increaseRedisValue(String key , String field) {

		return redisTemplate.opsForHash().increment(key , field, 1);
	}
	
	public void ackStream(String consumerGroupName , MapRecord<String , Object , Object> message) {
		
		redisTemplate.opsForStream().acknowledge(consumerGroupName, message);
	}
	
	public void claimStream(PendingMessage pendingMessage , String consumerName) {
		
		RedisAsyncCommands commands = (RedisAsyncCommands)
				redisTemplate.getConnectionFactory()
				             .getConnection()
				             .getNativeConnection();
		
		CommandArgs<String ,String> args = 
				new CommandArgs<>(StringCodec.UTF8)
					.add(pendingMessage.getIdAsString())
					.add(pendingMessage.getGroupName())
					.add(consumerName)
					.add("20")
					.add(pendingMessage.getIdAsString())
					;
		commands.dispatch(CommandType.XCLAIM, new StatusOutput(StringCodec.UTF8) ,args);
	}
	
	
	 
	public MapRecord<String , Object ,Object > findStreamMessageById(String streamKey , String id){
		List<MapRecord<String , Object ,Object >> mapRecordList = findStreamMessageByRange(streamKey,id,id);
		if(mapRecordList.isEmpty()) return null;
		return mapRecordList.get(0);
	}
	public List<MapRecord<String , Object ,Object >> findStreamMessageByRange(String streamKey , String startId , String endId){
		return redisTemplate.opsForStream().range(streamKey, Range.closed(startId, endId));
		
	}
	
	public void createStreamConsumerGroup(String streamKey , String consumerGroupName) {
		
		if(Boolean.FALSE.equals(redisTemplate.hasKey(streamKey))) {
			RedisAsyncCommands commands = 
					(RedisAsyncCommands)redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
			
			CommandArgs<String ,String> args = new CommandArgs<>(StringCodec.UTF8)
					.add(CommandKeyword.CREATE)
					.add(streamKey)
					.add(consumerGroupName)
					.add("0")
					.add("MKSTREAM");
			
			commands.dispatch(CommandType.XGROUP, new StatusOutput(StringCodec.UTF8),args);
		}
		else {//stream is exist , create consumerGroup if is not exist
			if(!isStreamConsumerGroupExist(streamKey,consumerGroupName)) {
				redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0") , consumerGroupName);
			}
		}
	}
	
	public PendingMessages findStreamPendingMessages(String streamKey , String consumerGroupName , String consumerName) {
		return redisTemplate.opsForStream()
				.pending(streamKey, Consumer.from(consumerGroupName, consumerName), Range.unbounded() , 100L);
	}
	
	
	public boolean isStreamConsumerGroupExist(String streamKey , String consumerGroupName) {
		
		Iterator<StreamInfo.XInfoGroup> iterator = redisTemplate.opsForStream().groups(streamKey).stream().iterator();
		
		while (iterator.hasNext()) {
			StreamInfo.XInfoGroup xInfoGroup = iterator.next();
			if(xInfoGroup.groupName().equals(consumerGroupName)) {
				return true;
			}
		}
		return false;
	}
	
	public StreamMessageListenerContainer createStreamMessageListenerContainer() {
		
		StreamMessageListenerContainer result = 
		StreamMessageListenerContainer.create(
				redisTemplate.getConnectionFactory(),
				StreamMessageListenerContainer.StreamMessageListenerContainerOptions
				                              .builder()
				                              .hashKeySerializer(new StringRedisSerializer())
				                              .hashValueSerializer(new StringRedisSerializer())
				                              .pollTimeout(Duration.ofMillis(20))
				                              .build()
				);
		
		return result;
	}
	
	
	
	
	
	
	
}
