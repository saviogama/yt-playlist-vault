package com.savio.ytplaylistvault.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

@Configuration
@EnableCaching
@EnableConfigurationProperties(YoutubePlaylistCacheProperties.class)
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
public class RedisCacheConfig {

  @Bean
  CacheManager cacheManager(
      RedisConnectionFactory connectionFactory, YoutubePlaylistCacheProperties properties) {
    RedisCacheConfiguration cacheConfiguration =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(properties.ttl())
            .disableCachingNullValues()
            .serializeValuesWith(
                SerializationPair.fromSerializer(
                    GenericJacksonJsonRedisSerializer.builder().build()));

    return RedisCacheManager.builder(connectionFactory).cacheDefaults(cacheConfiguration).build();
  }
}
