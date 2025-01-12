package ro.faur.apollo.libs.persistence;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration(ObjectMapper mapper) {
        // Create a customized ObjectMapper
        ObjectMapper myMapper = mapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // Ignore unknown properties
                .registerModule(new Hibernate6Module()) // Handle Hibernate lazy-loading
                .activateDefaultTyping(
                        mapper.getPolymorphicTypeValidator(),
                        ObjectMapper.DefaultTyping.NON_FINAL, // Add type info for all non-final classes
                        JsonTypeInfo.As.PROPERTY // Include '@class' metadata for deserialization
                );

        // Use Jackson2JsonRedisSerializer with the configured ObjectMapper
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        serializer.setObjectMapper(myMapper);

        // Configure Redis cache with the serializer
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // Cache TTL of 5 minutes
                .disableCachingNullValues() // Avoid caching null values
                .serializeValuesWith(
                        SerializationPair.fromSerializer(serializer) // Use custom serializer
                );
    }
}
