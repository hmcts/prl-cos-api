package uk.gov.hmcts.reform.prl.clients;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.prl.clients.os.OsCourtFinderApi;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.Dpa;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.OsPlacesResponse;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.Result;
import uk.gov.hmcts.reform.prl.services.LocalAuthorityCourtDataLoader;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.OsCourtFinderService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OsCourtFinderApiClientIntegrationTest {
    private MockMvc mockMvc;

    @MockBean
    private SystemUserService systemUserService;
    @MockBean
    private OsCourtFinderApi osCourtFinderApi;

    @MockBean
    private LocalAuthorityCourtDataLoader localAuthorityCourtDataLoader;
    @MockBean
    private LocationRefDataService locationRefDataService;
    @MockBean
    private CourtFinderApi courtFinderApi;

    private OsCourtFinderService osCourtFinderService;

    @BeforeEach
    public void setUp() throws Exception {
        osCourtFinderService = new OsCourtFinderService(systemUserService, osCourtFinderApi, localAuthorityCourtDataLoader,
                                                        locationRefDataService, courtFinderApi);
    }

    @Test
    void shouldReturnOsResponse() throws Exception {
        OsPlacesResponse osPlacesResponse = OsPlacesResponse.builder()
            .results(List.of(Result.builder()
                                 .dpa(Dpa.builder()
                                          .localCustodianCode("123").build())
                                 .build()))
            .build();
        Mockito.when(osCourtFinderApi.findCouncilByPostcode("EC2A2AB")).thenReturn(osPlacesResponse);

        String response = osCourtFinderService.getLocalCustodianCodeByPostCode("EC2A2AB");

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo("123");
    }

    @Test
    void shouldReturnNullIfApiThrowException() throws Exception {
        assertThat(osCourtFinderApi)
            .isNotNull();

        Mockito.doThrow(FeignException.FeignClientException.class).when(osCourtFinderApi).findCouncilByPostcode("EC2A2AB");

        String response = osCourtFinderService.getLocalCustodianCodeByPostCode("EC2A2AB");

        assertThat(response).isNull();
    }
}
