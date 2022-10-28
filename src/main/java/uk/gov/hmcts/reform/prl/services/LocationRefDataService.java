package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.config.LrdConfiguration;
import uk.gov.hmcts.reform.prl.models.court.CourtDetails;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.concat;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationRefDataService {

    private final RestTemplate restTemplate;
    private final LrdConfiguration lrdConfiguration;
    private final AuthTokenGenerator authTokenGenerator;

    public List<String> getCourtLocations(String authToken) {
        try {
            ResponseEntity<CourtDetails> responseEntity = restTemplate.exchange(
                buildServiceIdUri(),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<CourtDetails>() {});
            return onlyEnglandAndWalesLocations(responseEntity.getBody());
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public CourtVenue getCcmccLocation(String authToken) {
        try {
            ResponseEntity<CourtDetails> responseEntity = restTemplate.exchange(
                buildUriforCcmcc(),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<CourtDetails>() {});
            CourtDetails ccmccLocations = responseEntity.getBody();
            if (ccmccLocations == null || ccmccLocations.getCourtVenues().isEmpty()) {
                log.warn("Location Reference Data Lookup did not return any CCMCC location");
                return CourtVenue.builder().build();
            } else {
                if (ccmccLocations.getCourtVenues().size() > 1) {
                    log.warn("Location Reference Data Lookup returned more than one CCMCC location");
                }
                return ccmccLocations.getCourtVenues().get(0);
            }
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return CourtVenue.builder().build();
    }

    public CourtDetails getCourtLocationsForDefaultJudgments(String authToken) {
        try {
            ResponseEntity<CourtDetails> responseEntity = restTemplate.exchange(
                buildUriForDefaultJudgments(),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<>() {});
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return CourtDetails.builder().build();
    }

    private URI buildUri() {
        String queryUrl = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryUrl)
            .queryParam("is_hearing_location", "Y")
            .queryParam("location_type", "Court");
        return builder.buildAndExpand(new HashMap<>()).toUri();
    }

    private URI buildServiceIdUri() {
        String queryUrl = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryUrl)
            .queryParam("is_hearing_location", "Y")
            .queryParam("location_type", "Court");
        return builder.buildAndExpand(new HashMap<>()).toUri();
    }

    private URI buildUriforCcmcc() {
        String queryUrl = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryUrl)
            .queryParam("court_venue_name", "County Court Money Claims Centre");
        return builder.buildAndExpand(new HashMap<>()).toUri();
    }

    private URI buildUriForDefaultJudgments() {
        String queryUrl = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryUrl)
            .queryParam("is_hearing_location", "Y")
            .queryParam("is_case_management_location", "Y")
            .queryParam("court_type_id", "10")
            .queryParam("location_type", "Court");
        return builder.buildAndExpand(new HashMap<>()).toUri();
    }

    private HttpEntity<String> getHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);
        headers.add("ServiceAuthorization", authTokenGenerator.generate());
        return new HttpEntity<>(headers);
    }

    private List<String> onlyEnglandAndWalesLocations(CourtDetails locationRefData) {
        return locationRefData == null
            ? new ArrayList<>()
            : locationRefData.getCourtVenues().stream().filter(location -> !"Scotland".equals(location.getRegion()))
            .map(this::getDisplayEntry).collect(Collectors.toList());
    }

    private String getDisplayEntry(CourtVenue location) {
        return concat(concat(concat(location.getSiteName(), " - "), concat(location.getCourtAddress(), " - ")),
                      location.getPostcode());
    }
}
