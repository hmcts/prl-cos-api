package uk.gov.hmcts.reform.prl.services.caseinitiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseInitiationServiceTest {

    public static final String AUTHORISATION = "Bearer token";
    @InjectMocks
    CaseInitiationService caseInitiationService;

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private EventService eventPublisher;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    private CaseData caseData;
    private CaseDetails caseDetails;
    private Map<String, Object> caseDataMap;
    public static final String CASE_ID = "1234567891234567";

    @Before
    public void setup() {
        caseDataMap = new HashMap<>();
        caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .id(1234567891234567L)
            .state("SUBMITTED_PAID")
            .build();
        PartyDetails partyDetailsApplicant = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .build();
        PartyDetails partyDetailsRespondent = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .build();

        caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetailsApplicant).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                     .value(partyDetailsRespondent).build()))
            .build();
    }

    @Test
    public void testHandleCaseInitiation() {
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseData);
        when(coreCaseDataApi.submitSupplementaryData(any(), any(), any(), any()))
            .thenReturn(CaseDetails.builder().build());

        caseInitiationService.handleCaseInitiation(
            AUTHORISATION,
            CallbackRequest.builder().caseDetails(caseDetails).build());

        verify(eventPublisher, times(1)).publishEvent(any());
    }

}
