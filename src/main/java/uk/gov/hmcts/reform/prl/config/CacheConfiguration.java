package uk.gov.hmcts.reform.prl.config;

import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching
@Configuration
public class CacheConfiguration {

    public static final String SYS_USER_CACHE = "systemUserCache";
    public static final String USER_DETAILS_CACHE = "userDetailsCache";
    public static final String ORGANISATION_CACHE = "organisationCache";

    @Bean
    public CacheManagerCustomizer<CaffeineCacheManager> cacheManagerCustomizer() {
        return cacheManager -> cacheManager.setAllowNullValues(false);
    }
}
