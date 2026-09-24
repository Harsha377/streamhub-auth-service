package com.streamhub.auth.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;


@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper){
          RedisTemplate<String,Object> redisTemplate=new RedisTemplate<>();
          redisTemplate.setConnectionFactory(connectionFactory);
          StringRedisSerializer keySerializer=new StringRedisSerializer();
          GenericJacksonJsonRedisSerializer valueSerializer=new GenericJacksonJsonRedisSerializer(objectMapper);
          redisTemplate.setKeySerializer(keySerializer);
          redisTemplate.setValueSerializer(valueSerializer);
          redisTemplate.setHashKeySerializer(keySerializer);
          redisTemplate.setHashValueSerializer(valueSerializer);
          redisTemplate.afterPropertiesSet();
          return redisTemplate;

    }

    @Bean
    public StringRedisTemplate stringRedisSerializer(RedisConnectionFactory redisConnectionFactory){
        return new StringRedisTemplate (redisConnectionFactory);
    }
}
