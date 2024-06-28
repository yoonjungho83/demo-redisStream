package com.demo.redis.producer;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisStreamProducer {

	private AtomicInteger atomicInteger = new AtomicInteger(0);
	private final RedisTemplate<String ,Object> redisTemplate;
	
	
	public void setRedisStream(String streamKey , String message ) {
		
		
		ObjectRecord<String ,String> record = 
				StreamRecords.newRecord()
				             .ofObject(message)
				             .withStreamKey(streamKey)
				;
		redisTemplate.opsForStream().add(record);
		atomicInteger.incrementAndGet();
	}
	
}
