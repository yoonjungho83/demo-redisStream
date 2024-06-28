package com.demo.redis.consumer;

import java.time.Duration;

import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

import com.demo.redis.support.RedisOperator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamConsumer implements StreamListener<String ,MapRecord<String ,Object ,Object>>{

	private final RedisTemplate<String ,Object> redisTemplate;
	private final RedisOperator redisOperator;
	
	
	private Subscription subscription; 
	private StreamMessageListenerContainer<String , MapRecord<String ,Object, Object>> listenerContainer;
	
	
	private String consumerGroupName = "TEST-GRP1";
	private String consumerName = "server1";
	
	/** start listener */
	public void startListener() throws InterruptedException {
		String streamKey = "stream-key1";
		redisOperator.createStreamConsumerGroup(streamKey, consumerGroupName);
		
		listenerContainer = redisOperator.createStreamMessageListenerContainer();
		
		subscription = listenerContainer.receive(
				Consumer.from(consumerGroupName, consumerName),
				StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
				this
				);
		subscription.await(Duration.ofSeconds(2));
		
		
		listenerContainer.start();
		
		log.info("##################### listenerContainer start ######################");
	}
	
	/** stop listener */
	public void stopListener() {
		if(listenerContainer != null)  listenerContainer.stop();
	}
	public void stopSubscription1() {
		if(subscription != null)  subscription.cancel();
	}
	
	@Override
	public void onMessage(MapRecord<String, Object, Object> message) {
		// TODO Auto-generated method stub
		/* message.getRequiredStream() : stream key
		 * message.getStream() : stream key 
		 * 
		 * */
		log.info("onMessage = {} / {} / {} / {} ",message.getValue() , message.getRequiredStream() , message.getId() , message.getStream());
		
		//비즈니스 로직 처리  보통 ws로 client에 전송 해줌.
		
		redisOperator.ackStream(consumerGroupName, message);
	}
	
}
