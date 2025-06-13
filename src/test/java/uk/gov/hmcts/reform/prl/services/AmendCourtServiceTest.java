package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CantFindCourtEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmendCourtServiceTest {

    @InjectMocks
    private AmendCourtService amendCourtService;

    @Mock
    private EmailService emailService;

    @Mock
    private LocationRefDataService locationRefDataService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private C100IssueCaseService c100IssueCaseService;

    @Mock
    private CourtSealFinderService courtSealFinderService;

    @Mock
    private CaseSummaryTabService caseSummaryTab;

    private CaseData caseData;
    private CallbackRequest callbackRequest;
    private Map<String, Object> caseDataMap;

    @BeforeEach
    void setUp() {
        caseDataMap = new HashMap<>();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder().id(123L).state(State.CASE_ISSUED.getValue()).data(caseDataMap).build()).build();
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .courtList(DynamicList.builder().value(DynamicListElement.builder().code(":test@test.com").build()).build())
            .build();
    }

    @Test
    void testC100EmailNotification() {
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        when(locationRefDataService.getCourtDetailsFromEpimmsId(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.of(CourtVenue.builder().build()));
        amendCourtService.handleAmendCourtSubmission("", callbackRequest, caseDataMap);
        verifyNoInteractions(emailService);
    }

    @Test
    void testFL401EmailNotificationWithEmail() {
        caseData = caseData.toBuilder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt))
            .courtEmailAddress("").build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        amendCourtService.handleAmendCourtSubmission("", callbackRequest, caseDataMap);
        verify(emailService, times(0)).send(Mockito.anyString(),
                                            Mockito.any(),
                                            Mockito.any(), Mockito.any()
        );
    }

    @Test
    void testValidateCourtShouldGiveErrorWhenCantFindCourtIsNotSelected() {
        caseData = CaseData.builder()
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt))
            .courtList(DynamicList.builder().build())
            .courtEmailAddress("email@test.com")
            .anotherCourt("test court").build();
        List<String> errorList  = new ArrayList<>();

        assertTrue(amendCourtService.validateCourtFields(caseData, errorList));
    }

    @Test
    void testValidateCourtShouldNotGiveError() {
        caseData = CaseData.builder()
            .courtEmailAddress("email@test.com")
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt))
            .anotherCourt("test court").build();
        List<String> errorList  = new ArrayList<>();

        assertFalse(amendCourtService.validateCourtFields(caseData, errorList));
    }

    @Test
    void testValidateCourtShouldGiveError() {
        caseData = CaseData.builder()
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt)).build();

        List<String> errorList  = new ArrayList<>();

        assertTrue(amendCourtService.validateCourtFields(caseData, errorList));
    }
}
