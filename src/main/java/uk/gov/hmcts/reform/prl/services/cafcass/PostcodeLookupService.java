package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.prl.config.cafcass.PostcodeLookupConfiguration;
import uk.gov.hmcts.reform.prl.exception.cafcass.PostcodeValidationException;
import uk.gov.hmcts.reform.prl.models.cafcass.PostcodeResponse;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostcodeLookupService {

    @Autowired
    ObjectMapper objectMapper;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    PostcodeLookupConfiguration configuration;

    public boolean isValidNationalPostCode(String postcode, String countryCode) {
        boolean returnValue = true;

        if (StringUtils.isEmpty(postcode) || StringUtils.isEmpty(countryCode)) {
            returnValue = false;
        }

        PostcodeResponse response = fetchNationalPostcodeBuildings(postcode.toUpperCase(Locale.UK));

        if (returnValue && (response == null || response.getResults() == null || response.getResults().isEmpty())) {
            returnValue = false;
        }

        returnValue = returnValue && (!response.getResults().stream()
            .filter(eachObj -> null != eachObj.getDpa()
                && eachObj.getDpa().getCountryCode().equalsIgnoreCase(countryCode))
            .map(eachObj -> eachObj.getDpa().getBuildingNumber())
            .collect(Collectors.toList()).isEmpty());

        return returnValue;
    }

    private PostcodeResponse fetchNationalPostcodeBuildings(String postcode) {
        PostcodeResponse results = null;
        try {
            Map<String, String> params = new HashMap<>();
            params.put("postcode", StringUtils.deleteWhitespace(postcode));
            String url = configuration.getUrl();
            String key = configuration.getAccessKey();
            params.put("key", key);
            if (StringUtils.isEmpty(url)) {
                throw new PostcodeValidationException("Postcode URL is null");
            }
            if (StringUtils.isEmpty(key)) {
                throw new PostcodeValidationException("Postcode API Key is null");
            }
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url + "/postcode");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }

            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> httpHeader = new HttpEntity<>(url, headers);

            HttpEntity<String> response =
                restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    httpHeader,
                    String.class
                );

            HttpStatus responseStatus = ((ResponseEntity) response).getStatusCode();

            if (responseStatus.value() == org.apache.http.HttpStatus.SC_OK) {
                return objectMapper.readValue(response.getBody(), PostcodeResponse.class);
            }
        } catch (Exception e) {
            log.error("Postcode Lookup Failed - ", e.getMessage());
            throw new PostcodeValidationException(e.getMessage(), e);
        }

        return results;
    }
}
