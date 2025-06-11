package uk.gov.hmcts.reform.prl.services.caseinitiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.services.caseinitiation.CaseInitiationService.COURT_LIST;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    private LocationRefDataService locationRefDataService;

    private CaseData caseData;
    private CaseDetails caseDetails;
    private Map<String, Object> caseDataMap;
    public static final String CASE_ID = "1234567891234567";

    @BeforeEach
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

        caseDataMap.put("caseTypeOfApplication",C100_CASE_TYPE);
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

    @Test
    public void testHandlePrePopulateCourtDetailsC100() {
        caseData = CaseData.builder()
                .caseTypeOfApplication(C100_CASE_TYPE)
                .build();
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseData);
        when(locationRefDataService.getCourtLocations(Mockito.anyString())).thenReturn(List.of(DynamicListElement.EMPTY));
        Assertions.assertTrue(caseInitiationService.prePopulateCourtDetails(AUTHORISATION, caseDataMap).containsKey(COURT_LIST));
    }

    @Test
    public void testHandlePrePopulateCourtDetailsFL401() {
        caseDataMap.put("caseTypeOfApplication",FL401_CASE_TYPE);
        caseData = CaseData.builder()
                .caseTypeOfApplication(FL401_CASE_TYPE)
                .build();
        List<DynamicListElement> courts = List.of(DynamicListElement.builder().label("123").build());
        when(locationRefDataService.getDaCourtLocations(AUTHORISATION))
                .thenReturn(courts);
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseData);
        when(locationRefDataService.getDaCourtLocations(Mockito.anyString())).thenReturn(courts);
        Assertions.assertTrue(caseInitiationService.prePopulateCourtDetails(AUTHORISATION, caseDataMap).containsKey(COURT_LIST));
    }

}
