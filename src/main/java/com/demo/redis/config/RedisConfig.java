package com.demo.redis.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.Getter;


@Getter
@Configuration
@EnableCaching
public class RedisConfig {

	
	@Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;
    
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
    	
    	RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host.trim(),port);
    	GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig<>();
    	
    	poolConfig.setMaxTotal(128);
    	poolConfig.setMaxIdle(128);
    	poolConfig.setMinIdle(36);
    	
    	LettuceClientConfiguration clientConfig = 
    			LettucePoolingClientConfiguration.builder()
    			                                 .poolConfig(poolConfig)
    			                                 .build();
    	
    	LettuceConnectionFactory lettuceFactory = new LettuceConnectionFactory(config , clientConfig);
    	lettuceFactory.setShareNativeConnection(Boolean.FALSE);
    	
    	return lettuceFactory;
    	
//        return new LettuceConnectionFactory(host, port);
    }
    
    @Bean
    public StringRedisTemplate getStrRedisTemplate() {
    	StringRedisTemplate redisTemplate = new StringRedisTemplate();
    	redisTemplate.setConnectionFactory(redisConnectionFactory());
    	
    	return redisTemplate;
    }
    
    @Bean
    public RedisTemplate<String ,Object> redisTemplate(){
    	
    	RedisTemplate<String ,Object> redisTemplate = new RedisTemplate<>();
    	redisTemplate.setConnectionFactory(redisConnectionFactory());
    	//일반적인 key:value의 경우 시리얼라이저
    	redisTemplate.setKeySerializer(new StringRedisSerializer());
    	redisTemplate.setValueSerializer(new StringRedisSerializer());
    	
    	//hash르 사용할 경우 시리얼라이저
    	redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    	redisTemplate.setHashValueSerializer(new StringRedisSerializer());
    	
    	//모든경우
    	redisTemplate.setDefaultSerializer(new StringRedisSerializer());
    	
    	return redisTemplate;
    }
    
//    @Bean
//    public CacheManager cacheManager() {
//        RedisCacheManager.RedisCacheManagerBuilder builder =
//                RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(redisConnectionFactory());
//
//        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
//        		//GenericJackson2JsonRedisSerializer > JSON 형태로 Serialization 적용
//	        	//Jackson2JsonRedisSerializer > @class 필드를 포함하지 않고 Json 으로 저장 / 항상 Class Type 정보를 Serializer 에 함께 지정	
//        		//JdkSerializationRedisSerializer > Default로 적용되는 Serializer로 기본 자바 직렬화 방식을 사용한다.  Serializable 인터페이스만 구현
//        		//StringRedisSerializer > String 값을 그대로 저장하는 Serializer / Json 타입으로 별도로 변환하는 과정이 필요
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())) // Value Serializer 변경
//				.disableCachingNullValues()//데이터가 null일 경우 캐싱하지 않음
//                .entryTtl(Duration.ofMinutes(30L))//유효기간 설정
//                ;
//
//        builder.cacheDefaults(configuration);
//
//        return builder.build();
//    }
}
