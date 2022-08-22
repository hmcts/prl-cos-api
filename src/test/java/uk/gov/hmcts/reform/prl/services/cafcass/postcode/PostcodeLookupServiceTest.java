package uk.gov.hmcts.reform.prl.services.cafcass.postcode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.prl.config.cafcass.PostcodeLookupConfiguration;
import uk.gov.hmcts.reform.prl.models.cafcass.AddressDetails;
import uk.gov.hmcts.reform.prl.models.cafcass.PostcodeResponse;
import uk.gov.hmcts.reform.prl.models.cafcass.PostcodeResult;
import uk.gov.hmcts.reform.prl.services.cafcass.PostcodeLookupService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.cafcass.CafcassAppConstants.ENGLAND_POSTCODE_COUNTRYCODE;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.WALES_POSTCODE_COUNTRYCODE;

@RunWith(MockitoJUnitRunner.class)
public class PostcodeLookupServiceTest {

    @InjectMocks
    PostcodeLookupService postcodeLookupService;

    @Mock
    RestTemplate restTemplate;

    @Mock
    PostcodeLookupConfiguration postcodeLookupConfiguration;

    @Mock
    ObjectMapper objectMapper;

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
    public void shouldReturnFalseWhenPostCodeIsNotValidForUK() {

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

        assertThat(postcodeLookupService.isValidNationalPostCode("FalseCode", ENGLAND_POSTCODE_COUNTRYCODE)).isFalse();
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

    @Test
    public void shouldReturnTrueWhenStatusIsOK() throws JsonProcessingException {
        AddressDetails address = AddressDetails.builder()
            .address("59 Middle Lane")
            .postcode("N8 8PE")
            .status(HttpStatus.OK.toString())
            .countryCode("E")
            .build();
        PostcodeResult result = PostcodeResult.builder().dpa(address).build();
        List<PostcodeResult> resultLst = new ArrayList<>();
        resultLst.add(result);
//ObjectA myobjectA = new ObjectA();
//        //define the entity you want the exchange to return
//        ResponseEntity<List<ObjectA>> myEntity = new ResponseEntity<List<ObjectA>>(HttpStatus.ACCEPTED);
//        Mockito.when(restTemplate.exchange(
//            Matchers.eq("/objects/get-objectA"),
//            Matchers.eq(HttpMethod.POST),
//            Matchers.<HttpEntity<List<ObjectA>>>any(),
//            Matchers.<ParameterizedTypeReference<List<ObjectA>>>any())
//        ).thenReturn(myEntity);

        ResponseEntity<String> responseEntity =
            new ResponseEntity<String>(PostcodeResponse.builder().results(resultLst).build().toString(), HttpStatus.OK);
        when(postcodeLookupConfiguration.getUrl()).thenReturn("https://api.os.uk/search/places/v1");
        when(postcodeLookupConfiguration.getAccessKey()).thenReturn("dummy");


        when(restTemplate.exchange(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.any(HttpMethod.class),
            ArgumentMatchers.<HttpEntity<PostcodeResponse>>any(),
            ArgumentMatchers.<Class<String>>any()))
            .thenReturn(responseEntity);
        when(objectMapper.readValue(PostcodeResponse.builder().results(resultLst).build().toString(), PostcodeResponse.class)).thenReturn(PostcodeResponse.builder().results(resultLst).build());

        assertThat(postcodeLookupService
                       .isValidNationalPostCode("N8 8PE", ENGLAND_POSTCODE_COUNTRYCODE))
            .isTrue();
    }
}
