package com.demo.redis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.redis.consumer.RedisStreamConsumer;
import com.demo.redis.producer.RedisStreamProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/stream")
@RequiredArgsConstructor
public class StreamController {

	
	private final RedisStreamProducer redisStreamProducer;
	private final RedisStreamConsumer redisStreamConsumer;
	
	
	@GetMapping("/start")
	public String startListener() {
		try {
			redisStreamConsumer.startListener();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "listener start fail";
		}
		return "listener start success";
	}
	@GetMapping("/add")
	public String setRedisStream( @RequestParam("stream-val") String val) {
		
		log.info("key= stream-key1 / val= {} "  , val);
		redisStreamProducer.setRedisStream("stream-key1", val);
		return "succ";
	}
	
	
}
