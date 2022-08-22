package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
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
import uk.gov.hmcts.reform.prl.models.cafcass.AddressDetails;
import uk.gov.hmcts.reform.prl.models.cafcass.PostcodeResponse;
import uk.gov.hmcts.reform.prl.models.cafcass.PostcodeResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.cafcass.CafcassAppConstants.ENGLAND_POSTCODE_NATIONALCODE;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.WALES_POSTCODE_NATIONALCODE;

@RunWith(MockitoJUnitRunner.class)
public class PostcodeLookupServiceTest {

    @InjectMocks
    PostcodeLookupService postcodeLookupService;

    @Mock
    RestTemplate restTemplate;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    PostcodeLookupConfiguration postcodeLookupConfiguration;

    @Before
    public void setup() {
        when(postcodeLookupConfiguration.getUrl()).thenReturn("https://api.os.uk/search/places/v1");
        when(postcodeLookupConfiguration.getAccessKey()).thenReturn("dummy");
    }

    @DisplayName("Should generate False (Failure) when valid Postcode is given but no national code")
    @Test
    public void shouldReturnFailureWhenGivenPostCodeIsValidButNullNationalCode() {
        ResponseEntity<String> responseEntity =
                new ResponseEntity<String>("Ok", HttpStatus.ACCEPTED);
        assertThat(postcodeLookupService.isValidNationalPostCode("IG11 7YL", null)).isFalse();
        assertThat(postcodeLookupService.isValidNationalPostCode("IG11 7YL", "")).isFalse();
    }

    @DisplayName("Should generate False (Failure) when null/empty Postcode is given but valid national code")
    @Test
    public void shouldReturnFailureWhenGivenPostCodeNullButValidNationalCode() {

        ResponseEntity<String> responseEntity =
                new ResponseEntity<String>("Ok", HttpStatus.ACCEPTED);
        assertThat(postcodeLookupService.isValidNationalPostCode(null, ENGLAND_POSTCODE_NATIONALCODE)).isFalse();
        assertThat(postcodeLookupService.isValidNationalPostCode("", ENGLAND_POSTCODE_NATIONALCODE)).isFalse();
    }

    @DisplayName("Should generate False (Failure) when given null/empty Postcode and national code")
    @Test
    public void shouldReturnFailureWhenGivenBothNullPostCodeAndNullNationalCode() {

        ResponseEntity<String> responseEntity =
                new ResponseEntity<String>("Ok", HttpStatus.ACCEPTED);

        assertThat(postcodeLookupService.isValidNationalPostCode(null, null)).isFalse();
        assertThat(postcodeLookupService.isValidNationalPostCode("", "")).isFalse();
    }

    @DisplayName("Should generate False (Failure) when invalid Postcode is given for England nation")
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

        assertThat(postcodeLookupService.isValidNationalPostCode("AB45 1AB", ENGLAND_POSTCODE_NATIONALCODE)).isFalse();
    }

    @DisplayName("Should generate False (Failure) when invalid Postcode is given for valid nation")
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

        assertThat(postcodeLookupService.isValidNationalPostCode("FalseCode", ENGLAND_POSTCODE_NATIONALCODE)).isFalse();
    }

    @DisplayName("Should generate True (Success) when valid Postcode is given for valid nation")
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
        when(objectMapper.readValue(PostcodeResponse.builder().results(resultLst).build().toString(),
                PostcodeResponse.class))
                .thenReturn(PostcodeResponse.builder().results(resultLst).build());

        assertThat(postcodeLookupService
                .isValidNationalPostCode("N8 8PE", ENGLAND_POSTCODE_NATIONALCODE))
                .isTrue();
    }

    @DisplayName("Should generate False (Failure) when valid UK Postcode is given for wrong Nation")
    @Test
    public void shouldReturnFalseWhenNationIsInvalidGivenPostCodeIsValid() {

        ResponseEntity<String> responseEntity =
                new ResponseEntity<String>("Ok", HttpStatus.ACCEPTED);

        when(restTemplate.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.<HttpEntity<?>>any(),
                ArgumentMatchers.<Class<String>>any()))
                .thenReturn(responseEntity);

        assertThat(postcodeLookupService.isValidNationalPostCode("N8 8PE", WALES_POSTCODE_NATIONALCODE)).isFalse();
    }

    @DisplayName("Should throw an exception when lookup service url is not given")
    @Test
    public void shouldReturnExpcetionWhenUrlIsEmpty() {
        when(postcodeLookupConfiguration.getUrl()).thenReturn("");
        assertThrows(
                RuntimeException.class, () -> postcodeLookupService
                        .isValidNationalPostCode("IG11 7YL", ENGLAND_POSTCODE_NATIONALCODE));
    }

    @DisplayName("Should throw an exception when lookup service key is not given")
    @Test
    public void shouldReturnExpcetionWhenKeyIsEmpty() {
        when(postcodeLookupConfiguration.getAccessKey()).thenReturn("");
        assertThrows(
                RuntimeException.class, () -> postcodeLookupService
                        .isValidNationalPostCode("IG11 7YL", ENGLAND_POSTCODE_NATIONALCODE));
    }
}
