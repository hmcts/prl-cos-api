package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.clients.os.OsCourtFinderApi;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.Dpa;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.OsPlacesResponse;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.Result;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class OsCourtFinderServiceTest {

    @Mock
    private OsCourtFinderApi osCourtFinderApi;

    @InjectMocks
    private OsCourtFinderService osCourtFinderService;

    @Test
    void shouldReturnLocalCustodianCodeByPostCode() throws NotFoundException {
        String expectedValue = "123";
        OsPlacesResponse osPlacesResponse = OsPlacesResponse.builder()
            .results(List.of(Result.builder()
                                 .dpa(Dpa.builder()
                                          .localCustodianCode(expectedValue).build())
                                 .build()))
            .build();
        when(osCourtFinderApi.findCouncilByPostcode("EC2A2AB")).thenReturn(osPlacesResponse);

        String result = osCourtFinderService.getLocalCustodianCodeByPostCode("EC2A2AB");

        assertEquals(expectedValue, result);
        verify(osCourtFinderApi).findCouncilByPostcode(anyString());
    }

    @Test
    void shouldReturnNullIfOsApiReturnNoResultByPostCode() throws NotFoundException {

        when(osCourtFinderApi.findCouncilByPostcode("EC2A2AB")).thenReturn(OsPlacesResponse.builder().build());

        String result = osCourtFinderService.getLocalCustodianCodeByPostCode("EC2A2AB");

        assertNull(result);
        verify(osCourtFinderApi).findCouncilByPostcode(anyString());
    }

    @Test
    void shouldReturnNullIfOsApiReturnNull() throws NotFoundException {

        when(osCourtFinderApi.findCouncilByPostcode("EC2A2AB")).thenReturn(null);

        String result = osCourtFinderService.getLocalCustodianCodeByPostCode("EC2A2AB");

        assertNull(result);
        verify(osCourtFinderApi).findCouncilByPostcode(anyString());
    }
}
