package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AmendCourtServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private DfjLookupService dfjLookupService;

    @InjectMocks
    private AmendCourtService amendCourtService;

    @Mock
    private CaseUtils caseUtils;

    @Mock
    private LocationRefDataService locationRefDataService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;

    @Mock
    private C100IssueCaseService c100IssueCaseService;

    @Mock
    private CourtSealFinderService courtSealFinderService;

    @Mock
    private EventService eventService;

    @Mock
    private CaseSummaryTabService caseSummaryTab;

    private CaseData caseData;
    private CallbackRequest callbackRequest;
    private Map<String, Object> caseDataMap;
    private CourtVenue courtVenue;

    @Before
    public void setUp() {
        caseDataMap = new HashMap<>();
        courtVenue = CourtVenue.builder().courtEpimmsId("234946").build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder().id(123L).state(State.CASE_ISSUED.getValue()).data(caseDataMap).build()).build();
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .courtList(DynamicList.builder().value(DynamicListElement.builder().code("234946:test@test.com").build()).build())
            .build();
        when(locationRefDataService.getCourtDetailsFromEpimmsId(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.of(courtVenue));
        when(c100IssueCaseService.getFactCourtId(courtVenue)).thenReturn("");
        when(courtSealFinderService.getCourtSeal(Mockito.anyString())).thenReturn("");
        when(dfjLookupService.getDfjAreaFieldsByCourtId("234946"))
            .thenReturn(Map.of("dfjArea", "SWANSEA", "swanseaDFJCourt", "234946"));
    }

    @Test
    public void testC100EmailNotification() throws Exception {
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        when(locationRefDataService.getCourtDetailsFromEpimmsId(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.of(courtVenue));
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        amendCourtService.handleAmendCourtSubmission("", callbackRequest, caseDataMap);
        verifyNoInteractions(emailService);
    }

    @Test
    public void shouldAddDfjFilterFields() {
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        when(locationRefDataService.getCourtDetailsFromEpimmsId(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.of(courtVenue));
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        Map<String, Object> fields = amendCourtService.handleAmendCourtSubmission("", callbackRequest, caseDataMap);
        assertThat(fields).extracting("dfjArea").isEqualTo("SWANSEA");
        assertThat(fields).extracting("swanseaDFJCourt").isEqualTo("234946");
    }

    @Test
    @Ignore("Removed this test case since no emails are now triggered "
        + "upon selecting court from the dropdown list when selecting Transfer to another court event")
    public void testC100EmailNotificationWithEmail() throws Exception {
        caseData = caseData.toBuilder()
            .cantFindCourtCheck(List.of())
            .courtEmailAddress("").build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        when(locationRefDataService.getCourtDetailsFromEpimmsId(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.empty());
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        amendCourtService.handleAmendCourtSubmission("", callbackRequest, caseDataMap);
        verify(emailService, times(1)).send(Mockito.anyString(),
                                            Mockito.any(),
                                            Mockito.any(), Mockito.any()
        );
    }

    @Test
    public void testFL401EmailNotificationWithEmail() throws Exception {
        caseData = caseData.toBuilder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt))
            .courtEmailAddress("").build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        when(locationRefDataService.getCourtDetailsFromEpimmsId(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.empty());
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        amendCourtService.handleAmendCourtSubmission("", callbackRequest, caseDataMap);
        verify(emailService, times(0)).send(Mockito.anyString(),
                                            Mockito.any(),
                                            Mockito.any(), Mockito.any()
        );
    }

    @Test
    @Ignore("Removed this test case since no emails are now triggered "
        + "upon selecting court from the dropdown list when selecting Transfer to another court event")
    public void testFL401CourtAdminEmailEmail() throws Exception {
        caseData = caseData.toBuilder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .cantFindCourtCheck(List.of())
            .courtEmailAddress("").build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        when(locationRefDataService.getCourtDetailsFromEpimmsId(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.empty());
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        amendCourtService.handleAmendCourtSubmission("", callbackRequest, caseDataMap);
        verify(caseWorkerEmailService, times(1)).sendEmailToFl401LocalCourt(Mockito.any(),
                                            Mockito.any()
        );
    }

    @Test
    public void testValidateCourtShouldGiveErrorWhenCantFindCourtIsNotSelected() {
        caseData = CaseData.builder()
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt))
            .courtList(DynamicList.builder().build())
            .courtEmailAddress("email@test.com")
            .anotherCourt("test court").build();
        List<String> errorList  = new ArrayList<>();

        Boolean error =  amendCourtService
            .validateCourtFields(caseData, errorList);
        Assertions.assertTrue(error);
    }

    @Test
    public void testValidateCourtShouldGiveErrorWhenBothOptionSelelcted() {
        caseData = CaseData.builder()
            .courtEmailAddress("email@test.com")
            .anotherCourt("test court").build();
        List<String> errorList  = new ArrayList<>();

        Boolean error =  amendCourtService
            .validateCourtFields(caseData, errorList);
        Assertions.assertNotNull(error);
    }

    @Test
    public void testValidateCourtShouldNotGiveError() {
        caseData = CaseData.builder()
            .courtEmailAddress("email@test.com")
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt))
            .anotherCourt("test court").build();
        List<String> errorList  = new ArrayList<>();

        Boolean error =  amendCourtService
            .validateCourtFields(caseData, errorList);
        Assertions.assertFalse(error);
    }

    @Test
    public void testValidateCourtShouldGiveError() {
        caseData = CaseData.builder()
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt)).build();

        List<String> errorList  = new ArrayList<>();

        Boolean error =  amendCourtService
            .validateCourtFields(caseData, errorList);
        Assertions.assertTrue(error);
    }

    @Test
    public void testValidateCourtEmailAddressWhenEmailIsValid() throws Exception {
        CaseData testData = CaseData.builder()
            .courtEmailAddress("test@test.com").build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(testData);
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(testData);

        List<String> errorList = amendCourtService.validateCourtEmailAddress(callbackRequest);
        assertTrue(errorList.isEmpty());
    }

    @Test
    public void testValidateCourtEmailAddressWhenEmailIsInvalid() throws Exception {
        CaseData testData = CaseData.builder()
            .courtEmailAddress("testtest.com").build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(testData);
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(testData);

        List<String> errorList = amendCourtService.validateCourtEmailAddress(callbackRequest);
        assertEquals("Please enter valid court email address.", errorList.getFirst());
    }

}
