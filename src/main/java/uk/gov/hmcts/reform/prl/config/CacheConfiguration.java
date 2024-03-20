package uk.gov.hmcts.reform.prl.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

@Configuration
public class CacheConfiguration {

    public static final String SYS_USER_CACHE = "systemUserCache";

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public CacheManager applicationScopeCacheManager() {
        final SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(List.of(
            new ConcurrentMapCache(SYS_USER_CACHE)));
        simpleCacheManager.initializeCaches();
        return simpleCacheManager;
    }
}
