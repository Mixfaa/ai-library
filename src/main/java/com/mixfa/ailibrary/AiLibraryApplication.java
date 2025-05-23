package com.mixfa.ailibrary;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mixfa.ailibrary.misc.MongoLocaleConverter;
import com.mixfa.ailibrary.misc.cache.ByUserCache;
import com.mixfa.ailibrary.misc.cache.CacheMaintainer;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@Slf4j
@EnableScheduling
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableMongoRepositories
@PWA(name = "ai-library", shortName = "ailib")
@Theme(variant = Lumo.DARK)
public class AiLibraryApplication implements AppShellConfigurator {
    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(
                List.of(
                        new MongoLocaleConverter.LocaleToStringConverter(),
                        new MongoLocaleConverter.StringToLocaleConverter()
                )
        );
    }

    private static <T> RedisTemplate<String, T> makeRedisTemplate(Class<T> tClass, RedisConnectionFactory rcf) {
        RedisTemplate<String, T> template = new RedisTemplate<>();
        template.setConnectionFactory(rcf);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<T>(tClass));

        return template;
    }

    @Bean
    public RedisTemplate<String, ?> redisTemplate(RedisConnectionFactory rcf) {
        RedisTemplate<String, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(rcf);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, false);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public <T> ByUserCache<T> byUserCache(CacheMaintainer cacheMaintainer) {
        return new ByUserCache<>(cacheMaintainer);
    }


    public static void main(String[] args) {
        SpringApplication.run(AiLibraryApplication.class, args);
    }
}
