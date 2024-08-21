package uk.gov.hmcts.reform.prl.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static java.util.Arrays.asList;

@Configuration
public class ApplicationConfiguration {

    private int httpConnectTimeout = 60000;
    private int httpConnectRequestTimeout = 60000;


    @Bean
    public RestTemplate restTemplate(@Autowired MappingJackson2HttpMessageConverter jackson2HttpConverter) {
        return getRestTemplate(jackson2HttpConverter, httpConnectTimeout, httpConnectRequestTimeout);
    }


    private RestTemplate getRestTemplate(
        @Autowired MappingJackson2HttpMessageConverter jackson2HttpConverter,
        int connectTimeout,
        int connectRequestTimeout) {
        RestTemplate restTemplate = new RestTemplate(asList(jackson2HttpConverter,
                                                            new FormHttpMessageConverter(),
                                                            new ResourceHttpMessageConverter(),
                                                            new ByteArrayHttpMessageConverter(),
                                                            new StringHttpMessageConverter()));

        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectRequestTimeout))
            .setResponseTimeout(Timeout.ofMilliseconds(connectRequestTimeout))
            .build();

        CloseableHttpClient client = HttpClientBuilder
            .create()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .build();

        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(client));

        return restTemplate;
    }
}
