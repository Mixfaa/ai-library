package com.mixfa.ailibrary;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mixfa.ailibrary.misc.MongoLocaleConverter;
import com.mixfa.ailibrary.misc.cache.ByUserCache;
import com.mixfa.ailibrary.misc.cache.CacheMaintainer;
import com.mixfa.ailibrary.service.AdminAuthenticator;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
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
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
@EnableScheduling
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableMongoRepositories
@PWA(name = "ai-library", shortName = "ailib")
@Theme(variant = Lumo.DARK)
@Push
public class AiLibraryApplication implements AppShellConfigurator {
    @Bean
    public AdminAuthenticator adminAuthenticator() {
        var adminEmails = new HashMap<String, String>();
        try {
            var bundle = ResourceBundle.getBundle("adminemails");
            var adminNameIter = bundle.getKeys().asIterator();
            while (adminNameIter.hasNext()) {
                var name = adminNameIter.next();
                var email = bundle.getString(name);

                adminEmails.put(name, email);
            }
        } catch (Exception e) {
            log.error("Failed to load admin emails", e);
        }
        return (email) -> {
            var isAdmin = adminEmails.values().stream().anyMatch(adminEmail -> adminEmail.equals(email));

            if (isAdmin)
                log.info("Admin authenticated: {}", email);

            return isAdmin;
        };
    }

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

//    @Bean
//    public CommandLineRunner filler(BookService bookService, OfflineLibService offlineLibService, SearchEngine.ForBooks booksSearch, SearchEngine.ForLibraries librariesSearch) {
//        return (_) -> {
//            var random = new Random();
//
//            var booksPage = booksSearch.findAll(PageRequest.of(0, 15));
//
//            var booksList = new ArrayList<Book>();
//            booksList.addAll(booksPage.getContent());
//
//            for (int i = 1; i < booksPage.getTotalPages(); i++) {
//                booksList.addAll(booksSearch.findAll(PageRequest.of(i, 15)).getContent());
//            }
//
//            var libsPage = librariesSearch.findAll(Pageable.unpaged());
//            var libs = new ArrayList<>(librariesSearch.findAll(PageRequest.of(random.nextInt(libsPage.getTotalPages()), 15)).getContent());
//
//            for (Book book : booksList) {
//                Collections.shuffle(libs);
//
//                for (int i = 0; i < 3; i++) {
//                    var lib = libs.get(i);
//
//                    offlineLibService.setBookAvailability(
//                            lib.name(),
//                            book.id(),
//                            Map.of(
//                                    Locale.ENGLISH, random.nextLong(25),
//                                    Locale.FRENCH, random.nextLong(25),
//                                    Locale.GERMAN, random.nextLong(25)
//                            )
//                    );
//                }
//            }
//        };
//    }

    public static void main(String[] args) {
        SpringApplication.run(AiLibraryApplication.class, args);
    }
}
