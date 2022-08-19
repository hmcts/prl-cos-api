package uk.gov.hmcts.reform.prl.services.cafcass.postcode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.prl.config.cafcass.PostcodeLookupConfiguration;
import uk.gov.hmcts.reform.prl.services.cafcass.PostcodeLookupService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ENGLAND_POSTCODE_COUNTRYCODE;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.WALES_POSTCODE_COUNTRYCODE;

@RunWith(MockitoJUnitRunner.class)
public class PostcodeLookupServiceTest {

    @InjectMocks
    PostcodeLookupService postcodeLookupService;

    @Mock
    RestTemplate restTemplate;

    @Mock
    PostcodeLookupConfiguration postcodeLookupConfiguration;

    @Before
    public void setup() {
        when(postcodeLookupConfiguration.getUrl()).thenReturn("https://api.os.uk/search/places/v1");
        when(postcodeLookupConfiguration.getAccessKey()).thenReturn("dummy");
    }

    @Test
    public void shouldReturnFailureWhenGivenPostCodeIsValidButNoCountryCode() {

        ResponseEntity<String> responseEntity =
                new ResponseEntity<String>("Ok", HttpStatus.ACCEPTED);
        when(restTemplate.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.<HttpEntity<?>>any(),
                ArgumentMatchers.<Class<String>>any()))
                .thenReturn(responseEntity);

        assertThat(postcodeLookupService.isValidNationalPostCode("IG11 7YL", null)).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenPostCodeIsNotValidForEngland() {

        ResponseEntity<String> responseEntity =
                new ResponseEntity<String>("Ok", HttpStatus.ACCEPTED);
        when(postcodeLookupConfiguration.getUrl()).thenReturn("https://api.os.uk/search/places/v1");
        when(postcodeLookupConfiguration.getAccessKey()).thenReturn("dummy");
        when(restTemplate.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.<HttpEntity<?>>any(),
                ArgumentMatchers.<Class<String>>any()))
                .thenReturn(responseEntity);

        assertThat(postcodeLookupService.isValidNationalPostCode("AB41 7RR", ENGLAND_POSTCODE_COUNTRYCODE)).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenCountryIsInvaldGivenPostCodeIsValid() {

        ResponseEntity<String> responseEntity =
                new ResponseEntity<String>("Ok", HttpStatus.ACCEPTED);

        when(restTemplate.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.<HttpEntity<?>>any(),
                ArgumentMatchers.<Class<String>>any()))
                .thenReturn(responseEntity);

        assertThat(postcodeLookupService.isValidNationalPostCode("N8 8PE", WALES_POSTCODE_COUNTRYCODE)).isFalse();
    }

    @Test
    public void shouldReturnExpcetionWhenUrlIsEmpty() {
        when(postcodeLookupConfiguration.getUrl()).thenReturn("");
        assertThrows(
                RuntimeException.class, () -> postcodeLookupService
                        .isValidNationalPostCode("IG11 7YL", ENGLAND_POSTCODE_COUNTRYCODE));
    }

    @Test
    public void shouldReturnExpcetionWhenKeyIsEmpty() {
        when(postcodeLookupConfiguration.getAccessKey()).thenReturn("");
        assertThrows(
                RuntimeException.class, () -> postcodeLookupService
                        .isValidNationalPostCode("IG11 7YL", ENGLAND_POSTCODE_COUNTRYCODE));
    }
}