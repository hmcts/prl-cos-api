package uk.gov.hmcts.reform.prl.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.prl.services.RefDataUserService.JUDICIAL_USER_CACHE;
import static uk.gov.hmcts.reform.prl.services.RefDataUserService.STAFF_REF_DATA_CACHE;
import static uk.gov.hmcts.reform.prl.services.SystemUserService.SYS_USER_CACHE;
import static uk.gov.hmcts.reform.prl.services.factv2.FactV2TokenService.FACT_V2_TOKEN_CACHE;

class CacheConfigTest {

    @Test
    void shouldCreateCacheManagerWithRequiredCaches() {
        CacheConfig cacheConfig = new CacheConfig();

        CacheManager cacheManager = cacheConfig.cacheManager();

        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCache(SYS_USER_CACHE)).isNotNull();
        assertThat(cacheManager.getCache(JUDICIAL_USER_CACHE)).isNotNull();
        assertThat(cacheManager.getCache(STAFF_REF_DATA_CACHE)).isNotNull();
    }

    @Test
    void shouldReturnCacheNames() {
        CacheConfig cacheConfig = new CacheConfig();

        CacheManager cacheManager = cacheConfig.cacheManager();

        assertThat(cacheManager.getCacheNames())
            .containsExactlyInAnyOrder(SYS_USER_CACHE, JUDICIAL_USER_CACHE, STAFF_REF_DATA_CACHE, FACT_V2_TOKEN_CACHE);
    }
}
