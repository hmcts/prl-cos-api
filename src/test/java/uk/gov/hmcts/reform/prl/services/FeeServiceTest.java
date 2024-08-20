package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.FeesRegisterApi;
import uk.gov.hmcts.reform.prl.config.FeesConfig;
import uk.gov.hmcts.reform.prl.enums.AwpApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.OtherApplicationType;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeResponseForCitizen;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.AwpApplicationReasonEnum.DELAY_CANCEL_HEARING_DATE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class FeeServiceTest {

    @InjectMocks
    private FeeService feeService;

    @Mock
    private FeesRegisterApi feesRegisterApi;

    @Mock
    private FeeResponse feeResponse;

    @Mock
    private FeesConfig feesConfig;

    @Mock
    private FeesConfig.FeeParameters feeParameters;

    public static final String authToken = "Bearer TestAuthToken";
    private final String serviceAuthToken = "Bearer testServiceAuth";

    public static final String TEST_CASE_ID = "1656350492135029";

    @Mock
    ObjectMapper objectMapper;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    CaseData newCaseData;

    CaseDetails caseDetails;

    Map<String, Object> stringObjectMap;

    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String ZERO_AMOUNT = "0.00";


    @Before
    public void setUp() {
        //MockitoAnnotations.openMocks(this);
        feeResponse = FeeResponse.builder()
            .code("FEE0325")
            .amount(BigDecimal.valueOf(232.00))
            .build();

        ReflectionTestUtils.setField(
            feeService, "feesConfig", feesConfig);
        ReflectionTestUtils.setField(
            feeService, "feesRegisterApi", feesRegisterApi);

        newCaseData = CaseData.builder().build();
        stringObjectMap = newCaseData.toMap(new ObjectMapper());
        caseDetails =
            uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                .id(Long.parseLong(TEST_CASE_ID)).data(stringObjectMap)
                .build();

        feeParameters = FeesConfig.FeeParameters
            .builder()
            .channel("default")
            .event("miscellaneous")
            .service("private law")
            .jurisdiction1("family")
            .jurisdiction2("family court")
            .keyword("GAOnNotice")
            .build();

    }

    @Test
    public void testToCheckFeeAmount() throws Exception {

        FeesConfig.FeeParameters feeParameters = FeesConfig.FeeParameters
            .builder()
            .channel("default")
            .event("miscellaneous")
            .service("private law")
            .jurisdiction1("family")
            .jurisdiction2("family court")
            .keyword("ChildArrangements")
            .build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeParameters);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        assertEquals(feesConfig.getFeeParametersByFeeType(FeeType.C100_SUBMISSION_FEE),feeParameters);

        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        assertEquals(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE),feeResponse);
        BigDecimal actualResult = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE).getAmount();

        assertEquals(BigDecimal.valueOf(232.00), actualResult);

    }

    @Test
    public void testToCheckFeeAmountException() {

        FeesConfig.FeeParameters feeParameters = FeesConfig.FeeParameters
            .builder()
            .channel("default")
            .event("miscellaneous")
            .service("private law")
            .jurisdiction1("family")
            .jurisdiction2("family court")
            .keyword("ChildArrangements")
            .build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeParameters);
        when(feesRegisterApi.findFee(any(),any(),any(),any(),any(),any())).thenThrow(FeignException.class);

        assertExpectedException(() -> {
            feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
        }, WorkflowException.class, null);


    }



    @Test
    public void testToCheckFeeAmountWithWrongCode() throws Exception {

        when(feesConfig.getFeeParametersByFeeType(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeParameters);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        BigDecimal actualResult = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE).getAmount();

        assertNotEquals(BigDecimal.valueOf(65.00), actualResult);
    }

    @Test
    public void whenFeeDetailsNotFetchedThrowError() throws Exception {

        FeesConfig.FeeParameters feeParameters = FeesConfig.FeeParameters
            .builder()
            .channel("default")
            .event("miscellaneous")
            .service("private law")
            .jurisdiction1("family")
            .jurisdiction2("family court")
            .keyword("ChildArrangements")
            .build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeParameters);
        assertNotNull(when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenThrow(FeignException.class));

    }

    @Test
    public void testGetFeesDataForAdditionalApplications() throws Exception {

        List<FeeType> applicationsFeeTypes = List.of(FeeType.C2_WITH_NOTICE, FeeType.CHILD_ARRANGEMENTS_ORDER);

        FeesConfig.FeeParameters feeParameters = FeesConfig.FeeParameters
            .builder()
            .channel("default")
            .event("miscellaneous")
            .service("private law")
            .jurisdiction1("family")
            .jurisdiction2("family court")
            .keyword("GAOnNotice")
            .build();

        FeesConfig.FeeParameters feeParameters1 = FeesConfig.FeeParameters
            .builder()
            .channel("default")
            .event("miscellaneous")
            .service("private law")
            .jurisdiction1("family")
            .jurisdiction2("family court")
            .keyword("ParentalResponsibility")
            .build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C2_WITH_NOTICE)).thenReturn(feeParameters);
        when(feesConfig.getFeeParametersByFeeType(FeeType.CHILD_ARRANGEMENTS_ORDER)).thenReturn(feeParameters1);

        FeeResponse feeResponse1 = FeeResponse.builder()
            .code("FEE0324")
            .amount(BigDecimal.valueOf(167.00))
            .build();
        when(feeService.fetchFeeDetails(FeeType.C2_WITH_NOTICE)).thenReturn(feeResponse1);
        when(feeService.fetchFeeDetails(FeeType.CHILD_ARRANGEMENTS_ORDER)).thenReturn(feeResponse);
        FeeResponse response = feeService.getFeesDataForAdditionalApplications(applicationsFeeTypes);

        assertEquals(feeResponse, response);


    }


    @Test
    public void testFetchFeeCode() throws Exception {
        FeeRequest feeRequest = FeeRequest.builder().caseId(TEST_CASE_ID).applicationType(AwpApplicationTypeEnum.C2.toString()).build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C2_WITH_NOTICE)).thenReturn(feeParameters);

        FeeResponse feeResponse1 = FeeResponse.builder()
            .code("FEE0324")
            .amount(BigDecimal.valueOf(167.00))
            .build();
        when(feeService.fetchFeeDetails(FeeType.C2_WITH_NOTICE)).thenReturn(feeResponse1);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(newCaseData);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, feeRequest
            .getCaseId())).thenReturn(caseDetails);

        FeeResponseForCitizen response = feeService.fetchFeeCode(feeRequest, authToken, serviceAuthToken);

        assertNotNull(response);
        assertEquals(FeeType.C2_WITH_NOTICE.toString(),response.getFeeType());
        assertEquals("167.0",response.getAmount());
    }

    @Test
    public void testFetchFeeCodeWhenFeeTypeIsNull() throws Exception {
        FeeRequest feeRequest = FeeRequest.builder().caseId(TEST_CASE_ID).applicationType(AwpApplicationTypeEnum.C3.toString()).build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C2_WITH_NOTICE)).thenReturn(feeParameters);

        FeeResponse feeResponse1 = FeeResponse.builder()
            .code("FEE0324")
            .amount(BigDecimal.valueOf(167.00))
            .build();
        when(feeService.fetchFeeDetails(FeeType.C2_WITH_NOTICE)).thenReturn(feeResponse1);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(newCaseData);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, feeRequest
            .getCaseId())).thenReturn(caseDetails);

        FeeResponseForCitizen response = feeService.fetchFeeCode(feeRequest, authToken, serviceAuthToken);

        assertNotNull(response);
        assertEquals("Invalid Parameters to fetch fee code",response.getErrorRetrievingResponse());
    }

    @Test
    public void testFetchFeeCodeWhenFeeTypeOtherThanC2AndIsFl403ApplicationAlreadyPresent() throws Exception {
        FeeRequest feeRequest = FeeRequest.builder()
            .caseId(TEST_CASE_ID).applicationType(AwpApplicationTypeEnum.FL403.toString()).partyType("respondent")
            .caseType("FL401").build();

        CaseData caseData1 = CaseData.builder()
            .additionalApplicationsBundle(List.of(element(
                AdditionalApplicationsBundle.builder()
                    .partyType(PartyEnum.respondent)
                    .otherApplicationsBundle(
                        OtherApplicationsBundle.builder()
                            .applicationType(OtherApplicationType
                                                 .FL403_CHANGE_EXTEND_OR_CANCEL_NON_MOLESTATION_ORDER_OR_OCCUPATION_ORDER)
                            .build())
                    .build())))
            .build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.FL403_EXTEND_AN_ORDER)).thenReturn(feeParameters);

        FeeResponse feeResponse1 = FeeResponse.builder()
            .code("FEE0324").feeType(FeeType.FL403_EXTEND_AN_ORDER.toString())
            .amount(BigDecimal.valueOf(167.00))
            .build();
        when(feeService.fetchFeeDetails(FeeType.FL403_EXTEND_AN_ORDER)).thenReturn(feeResponse1);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData1);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, feeRequest
            .getCaseId())).thenReturn(caseDetails);

        FeeResponseForCitizen response = feeService.fetchFeeCode(feeRequest, authToken, serviceAuthToken);
        assertNotNull(response);
        assertEquals(FeeType.FL403_EXTEND_AN_ORDER.toString(),response.getFeeType());
        assertEquals("167.0",response.getAmount());
    }

    @Test
    public void testFetchFeeCodeWhenisHearingDate14DaysAwayFalseAndOtherPartyConsentNo() throws Exception {
        FeeRequest feeRequest = FeeRequest.builder().caseId(TEST_CASE_ID)
            .applicationType(AwpApplicationTypeEnum.C2.toString()).otherPartyConsent(NO)
            .applicationReason(DELAY_CANCEL_HEARING_DATE.getId())
            .hearingDate("First Hearing - 24/04/2022").build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C2_WITH_NOTICE)).thenReturn(feeParameters);
        FeeResponse feeResponse1 = FeeResponse.builder()
            .code("FEE0324").feeType(FeeType.C2_WITH_NOTICE.toString())
            .amount(BigDecimal.valueOf(167.00))
            .build();
        when(feeService.fetchFeeDetails(FeeType.C2_WITH_NOTICE)).thenReturn(feeResponse1);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(newCaseData);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, feeRequest
            .getCaseId())).thenReturn(caseDetails);

        FeeResponseForCitizen response = feeService.fetchFeeCode(feeRequest, authToken, serviceAuthToken);

        assertNotNull(response);
        assertEquals(FeeType.C2_WITH_NOTICE.toString(),response.getFeeType());
        assertEquals("167.0",response.getAmount());
    }

    @Test
    public void testFetchFeeCodeWhenIsHearingDate14DaysAwayTrueAndOtherPartyConsentNo() throws Exception {
        FeeRequest feeRequest = FeeRequest.builder().caseId(TEST_CASE_ID)
            .applicationType(AwpApplicationTypeEnum.C2.toString()).otherPartyConsent(NO)
            .applicationReason(DELAY_CANCEL_HEARING_DATE.getId())
            .hearingDate("First Hearing - 27/12/2023").build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C2_WITH_NOTICE)).thenReturn(feeParameters);
        FeeResponse feeResponse1 = FeeResponse.builder()
            .code("FEE0324").feeType(FeeType.C2_WITH_NOTICE.toString())
            .amount(BigDecimal.valueOf(167.00))
            .build();
        when(feeService.fetchFeeDetails(FeeType.C2_WITH_NOTICE)).thenReturn(feeResponse1);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(newCaseData);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, feeRequest
            .getCaseId())).thenReturn(caseDetails);

        FeeResponseForCitizen response = feeService.fetchFeeCode(feeRequest, authToken, serviceAuthToken);

        assertNotNull(response);
        assertEquals(FeeType.C2_WITH_NOTICE.toString(),response.getFeeType());
        assertEquals("167.0",response.getAmount());
    }

    @Test
    public void testFetchFeeCodeWhenIsHearingDate14DaysAwayFalseAndOtherPartyConsentYes() throws Exception {
        FeeRequest feeRequest = FeeRequest.builder().caseId(TEST_CASE_ID)
            .applicationType(AwpApplicationTypeEnum.C2.toString()).otherPartyConsent(YES)
            .applicationReason(DELAY_CANCEL_HEARING_DATE.getId())
            .hearingDate("First Hearing - 27/10/2023").build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C2_WITHOUT_NOTICE)).thenReturn(feeParameters);
        FeeResponse feeResponse1 = FeeResponse.builder()
            .code("FEE0324").feeType(FeeType.C2_WITHOUT_NOTICE.toString())
            .amount(BigDecimal.valueOf(167.00))
            .build();
        when(feeService.fetchFeeDetails(FeeType.C2_WITHOUT_NOTICE)).thenReturn(feeResponse1);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(newCaseData);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, feeRequest
            .getCaseId())).thenReturn(caseDetails);

        FeeResponseForCitizen response = feeService.fetchFeeCode(feeRequest, authToken, serviceAuthToken);

        assertNotNull(response);
        assertEquals(FeeType.C2_WITHOUT_NOTICE.toString(),response.getFeeType());
        assertEquals("167.0",response.getAmount());
    }

    @Test
    public void testFetchFeeCodeWhenHearingDateBlankButPartyConsentYes() throws Exception {
        FeeRequest feeRequest = FeeRequest.builder().caseId(TEST_CASE_ID)
            .applicationType(AwpApplicationTypeEnum.C2.toString()).otherPartyConsent(YES)
            .applicationReason(DELAY_CANCEL_HEARING_DATE.getId())
            .notice(YES)
            .build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C2_WITHOUT_NOTICE)).thenReturn(feeParameters);
        FeeResponse feeResponse1 = FeeResponse.builder()
            .code("FEE0324").feeType(FeeType.C2_WITHOUT_NOTICE.toString())
            .amount(BigDecimal.valueOf(167.00))
            .build();
        when(feeService.fetchFeeDetails(FeeType.C2_WITHOUT_NOTICE)).thenReturn(feeResponse1);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(newCaseData);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, feeRequest
            .getCaseId())).thenReturn(caseDetails);

        FeeResponseForCitizen response = feeService.fetchFeeCode(feeRequest, authToken, serviceAuthToken);

        assertNotNull(response);
        assertEquals(FeeType.C2_WITHOUT_NOTICE.toString(),response.getFeeType());
        assertEquals("167.0",response.getAmount());
    }

    @Test
    public void testFetchFeeCodeWhenHearingDateBlankButPartyConsentNoAndNoticeNo() throws Exception {
        FeeRequest feeRequest = FeeRequest.builder().caseId(TEST_CASE_ID)
            .applicationType(AwpApplicationTypeEnum.C2.toString()).otherPartyConsent(NO)
            .applicationReason(DELAY_CANCEL_HEARING_DATE.getId())
            .notice(NO)
            .build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C2_WITHOUT_NOTICE)).thenReturn(feeParameters);
        FeeResponse feeResponse1 = FeeResponse.builder()
            .code("FEE0324").feeType(FeeType.C2_WITHOUT_NOTICE.toString())
            .amount(BigDecimal.valueOf(167.00))
            .build();
        when(feeService.fetchFeeDetails(FeeType.C2_WITHOUT_NOTICE)).thenReturn(feeResponse1);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(newCaseData);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, feeRequest
            .getCaseId())).thenReturn(caseDetails);

        FeeResponseForCitizen response = feeService.fetchFeeCode(feeRequest, authToken, serviceAuthToken);

        assertNotNull(response);
        assertEquals(FeeType.C2_WITHOUT_NOTICE.toString(),response.getFeeType());
        assertEquals("167.0",response.getAmount());
    }

    @Test
    public void testFetchFeeCodeWhenHearingDateBlankButPartyConsentNoAndNoticeYes() throws Exception {
        FeeRequest feeRequest = FeeRequest.builder().caseId(TEST_CASE_ID)
            .applicationType(AwpApplicationTypeEnum.C2.toString()).otherPartyConsent(NO)
            .applicationReason(DELAY_CANCEL_HEARING_DATE.getId())
            .notice(YES)
            .build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C2_WITH_NOTICE)).thenReturn(feeParameters);
        FeeResponse feeResponse1 = FeeResponse.builder()
            .code("FEE0324").feeType(FeeType.C2_WITH_NOTICE.toString())
            .amount(BigDecimal.valueOf(167.00))
            .build();
        when(feeService.fetchFeeDetails(FeeType.C2_WITH_NOTICE)).thenReturn(feeResponse1);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(newCaseData);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, feeRequest
            .getCaseId())).thenReturn(caseDetails);

        FeeResponseForCitizen response = feeService.fetchFeeCode(feeRequest, authToken, serviceAuthToken);

        assertNotNull(response);
        assertEquals(FeeType.C2_WITH_NOTICE.toString(),response.getFeeType());
        assertEquals("167.0",response.getAmount());
    }

    @Test
    public void testFetchFeeCodeWhenHearingDateBlankButPartyConsentNoAndNoticeIsNull() throws Exception {
        FeeRequest feeRequest = FeeRequest.builder().caseId(TEST_CASE_ID)
            .applicationType(AwpApplicationTypeEnum.C2.toString()).otherPartyConsent(NO)
            .applicationReason(DELAY_CANCEL_HEARING_DATE.getId())
            .notice(null)
            .build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C2_WITH_NOTICE)).thenReturn(feeParameters);
        FeeResponse feeResponse1 = FeeResponse.builder()
            .code("FEE0324").feeType(FeeType.C2_WITH_NOTICE.toString())
            .amount(BigDecimal.valueOf(167.00))
            .build();
        when(feeService.fetchFeeDetails(FeeType.C2_WITH_NOTICE)).thenReturn(feeResponse1);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(newCaseData);
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, feeRequest
            .getCaseId())).thenReturn(caseDetails);

        FeeResponseForCitizen response = feeService.fetchFeeCode(feeRequest, authToken, serviceAuthToken);

        assertNotNull(response);
        assertEquals("Invalid Parameters to fetch fee code",response.getErrorRetrievingResponse());
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

}
