package uk.gov.hmcts.reform.prl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableList;
import feign.Feign;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Decoder;
import feign.jackson.JacksonEncoder;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import uk.gov.hmcts.reform.prl.util.CosApiClient;

import java.util.Collection;
import java.util.Map;


@Configuration
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.prl.*", "uk.gov.hmcts.reform.prl.services.*",
    "uk.gov.hmcts.reform.prl.controllers.*", "uk.gov.hmcts.reform.idam.client"})
@PropertySource("classpath:application.properties")
public class ServiceContextConfiguration {

    @Bean
    public CosApiClient cosApiClient(
        @Value("${case.orchestration.service.base.uri}") final String cosApiClientUrl) {
        return Feign.builder()
            .requestInterceptor(requestInterceptor())
            .encoder(new JacksonEncoder())
            .decoder(feignDecoder())
            .contract(new SpringMvcContract())
            .target(CosApiClient.class, cosApiClientUrl);
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            if (template.request().httpMethod() == Request.HttpMethod.POST) {
                Map<String, Collection<String>> headers = template.request().headers();
                if (headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                    Collection<String> listOfContentType = headers.get(HttpHeaders.CONTENT_TYPE);
                    if (!listOfContentType.contains("x-www-form-urlencoded")) {
                        template.header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
                    } else {
                        template.header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
                    }
                } else {
                    template.header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
                }

            }
        };
    }

    @Bean
    public Decoder feignDecoder() {
        MappingJackson2HttpMessageConverter jacksonConverter =
            new MappingJackson2HttpMessageConverter(customObjectMapper());
        jacksonConverter.setSupportedMediaTypes(ImmutableList.of(MediaType.APPLICATION_JSON));

        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(jacksonConverter);
        return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
    }

    @Bean
    public ObjectMapper customObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        return objectMapper;
    }

}
