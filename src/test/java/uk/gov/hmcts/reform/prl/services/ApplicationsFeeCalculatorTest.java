package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2AdditionalOrdersRequestedCa;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.CaOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DaApplicantOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DaRespondentOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.OtherApplicationType;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ADDITIONAL_APPLICATION_FEES_TO_PAY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CA_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CURRENCY_SIGN_POUND;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@RunWith(MockitoJUnitRunner.class)
public class ApplicationsFeeCalculatorTest {

    @InjectMocks
    private ApplicationsFeeCalculator applicationsFeeCalculator;
    @Mock
    private FeeService feeService;

    @Captor
    private ArgumentCaptor<List<FeeType>> actualFeeTypes;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private C2DocumentBundle c2DocumentBundle;

    @Before
    public void setup() {
        List<DynamicListElement> hearingDropdowns = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hearingDate = now.plusDays(15L);
        DynamicListElement hearingElement = DynamicListElement.builder()
            .label("First Hearing - " + hearingDate.format(formatter))
            .code("testId123456 - First Hearing")
            .build();

        c2DocumentBundle = C2DocumentBundle.builder().hearingList(DynamicList.builder()
                                                                      .value(hearingElement)
                                                                      .listItems(hearingDropdowns).build())
            .caReasonsForC2Application(List.of(C2AdditionalOrdersRequestedCa.REQUESTING_ADJOURNMENT)).build();

    }

    @Test
    public void testCalculateAdditionalApplicationsFeeForCa() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(
                AdditionalApplicationTypeEnum.otherOrder)
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().caApplicantApplicationType(
                CaOtherApplicationType.C1_CHILD_ORDER).build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                                                              .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                           .applicationType(
                                                                                               OtherApplicationType
                                                                                                   .D89_COURT_BAILIFF)
                                                                                           .build())
                                                              .build())))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);
        assertNotNull(stringObjectMap);
        assertTrue(stringObjectMap.containsKey(ADDITIONAL_APPLICATION_FEES_TO_PAY));
        assertEquals(CURRENCY_SIGN_POUND + BigDecimal.TEN, stringObjectMap.get(ADDITIONAL_APPLICATION_FEES_TO_PAY));
    }

    @Test
    public void testCalculateAdditionalApplicationsFeeForCaC2WithoutNotice() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(
                AdditionalApplicationTypeEnum.c2Order)
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().caRespondentApplicationType(
                CaOtherApplicationType.N161_APPELLANT_NOTICE).build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                                                              .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                           .applicationType(
                                                                                               OtherApplicationType
                                                                                                   .D89_COURT_BAILIFF)
                                                                                           .build())
                                                              .build())))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);
        assertNotNull(stringObjectMap);
        assertTrue(stringObjectMap.containsKey(ADDITIONAL_APPLICATION_FEES_TO_PAY));
        assertEquals(CURRENCY_SIGN_POUND + BigDecimal.TEN, stringObjectMap.get(ADDITIONAL_APPLICATION_FEES_TO_PAY));
    }



    @Test
    public void testCalculateAdditionalApplicationsFeeForDaApplicant() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(
                AdditionalApplicationTypeEnum.c2Order)
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder()
                                                  .daApplicantApplicationType(DaApplicantOtherApplicationType.N161_APPELLANT_NOTICE)
                                                  .build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);
        assertNotNull(stringObjectMap);
        assertTrue(stringObjectMap.containsKey(ADDITIONAL_APPLICATION_FEES_TO_PAY));
        assertEquals(CURRENCY_SIGN_POUND + BigDecimal.TEN, stringObjectMap.get(ADDITIONAL_APPLICATION_FEES_TO_PAY));
    }

    @Test
    public void testCalculateAdditionalApplicationsFeeForDaRespondnet() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(
                AdditionalApplicationTypeEnum.c2Order)
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder()
                                                  .daRespondentApplicationType(DaRespondentOtherApplicationType.N161_APPELLANT_NOTICE)
                                                  .build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);
        assertNotNull(stringObjectMap);
        assertTrue(stringObjectMap.containsKey(ADDITIONAL_APPLICATION_FEES_TO_PAY));
        assertEquals(CURRENCY_SIGN_POUND + BigDecimal.TEN, stringObjectMap.get(ADDITIONAL_APPLICATION_FEES_TO_PAY));
    }

    @Test(expected = Exception.class)
    public void testCalculateAdditionalApplicationsFeeForError() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(
                AdditionalApplicationTypeEnum.otherOrder
            )
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .build();
        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenThrow(new Exception());
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .build();
        applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);

    }

    @Test
    public void testCalculateAdditionalApplicationsFeeForHearingLessThan14DaysForFC600_Committal_Application() {
        List<DynamicListElement> hearingDropdowns = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hearingDate = now.plusDays(10L);
        DynamicListElement hearingElement = DynamicListElement.builder()
            .label("First Hearing - " + hearingDate.format(formatter))
            .code("testId123456 - First Hearing")
            .build();

        c2DocumentBundle = C2DocumentBundle.builder().hearingList(DynamicList.builder()
                                                                      .value(hearingElement)
                                                                      .listItems(hearingDropdowns).build())
            .caReasonsForC2Application(List.of(
                C2AdditionalOrdersRequestedCa.REQUESTING_ADJOURNMENT,
                C2AdditionalOrdersRequestedCa.APPOINTMENT_OF_GUARDIAN
            )).build();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(
                AdditionalApplicationTypeEnum.otherOrder
            )
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().caApplicantApplicationType(
                CaOtherApplicationType.FC600_COMMITTAL_APPLICATION).build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                                                              .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                           .applicationType(
                                                                                               OtherApplicationType
                                                                                                   .FC600_COMMITTAL_APPLICATION)
                                                                                           .build())
                                                              .build())))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);
        assertNotNull(stringObjectMap);
        assertTrue(stringObjectMap.containsKey(ADDITIONAL_APPLICATION_FEES_TO_PAY));
        assertEquals(CURRENCY_SIGN_POUND + BigDecimal.TEN, stringObjectMap.get(ADDITIONAL_APPLICATION_FEES_TO_PAY));
    }

    @Test
    public void testOtherApplicationTypeEmpty() {
        List<DynamicListElement> hearingDropdowns = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hearingDate = now.plusDays(10L);
        DynamicListElement hearingElement = DynamicListElement.builder()
            .label("First Hearing - " + hearingDate.format(formatter))
            .code("testId123456 - First Hearing")
            .build();

        c2DocumentBundle = C2DocumentBundle.builder().hearingList(DynamicList.builder()
                                                                      .value(hearingElement)
                                                                      .listItems(hearingDropdowns).build())
            .caReasonsForC2Application(List.of(
                C2AdditionalOrdersRequestedCa.REQUESTING_ADJOURNMENT,
                C2AdditionalOrdersRequestedCa.APPOINTMENT_OF_GUARDIAN
            )).build();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(
                AdditionalApplicationTypeEnum.otherOrder
            )
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                                                              .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                           .applicationType(
                                                                                               OtherApplicationType
                                                                                                   .FC600_COMMITTAL_APPLICATION)
                                                                                           .build())
                                                              .build())))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);
        assertNotNull(stringObjectMap);
        assertTrue(stringObjectMap.containsKey(ADDITIONAL_APPLICATION_FEES_TO_PAY));
        assertEquals(CURRENCY_SIGN_POUND + BigDecimal.TEN, stringObjectMap.get(ADDITIONAL_APPLICATION_FEES_TO_PAY));
    }

    @Test
    public void testCalculateAdditionalApplicationsFeeForHearingLessThan14DaysForD89_Court_Bailiff() {
        List<DynamicListElement> hearingDropdowns = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hearingDate = now.plusDays(10L);
        DynamicListElement hearingElement = DynamicListElement.builder()
            .label("First Hearing - " + hearingDate.format(formatter))
            .code("testId123456 - First Hearing")
            .build();

        c2DocumentBundle = C2DocumentBundle.builder().hearingList(DynamicList.builder()
                                                                      .value(hearingElement)
                                                                      .listItems(hearingDropdowns).build())
            .caReasonsForC2Application(List.of(
                C2AdditionalOrdersRequestedCa.REQUESTING_ADJOURNMENT,
                C2AdditionalOrdersRequestedCa.APPOINTMENT_OF_GUARDIAN
            )).build();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(
                AdditionalApplicationTypeEnum.otherOrder
            )
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().caApplicantApplicationType(
                CaOtherApplicationType.D89_COURT_BAILIFF).build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                                                              .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                           .applicationType(
                                                                                               OtherApplicationType
                                                                                                   .D89_COURT_BAILIFF)
                                                                                           .build())
                                                              .build())))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);
        assertNotNull(stringObjectMap);
        assertTrue(stringObjectMap.containsKey(ADDITIONAL_APPLICATION_FEES_TO_PAY));
        assertEquals(CURRENCY_SIGN_POUND + BigDecimal.TEN, stringObjectMap.get(ADDITIONAL_APPLICATION_FEES_TO_PAY));
    }

    @Test
    public void testCalculateAdditionalApplicationsFeeForHearingLessThan14DaysForC79_Child_Order() {
        List<DynamicListElement> hearingDropdowns = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hearingDate = now.plusDays(10L);
        DynamicListElement hearingElement = DynamicListElement.builder()
            .label("First Hearing - " + hearingDate.format(formatter))
            .code("testId123456 - First Hearing")
            .build();

        c2DocumentBundle = C2DocumentBundle.builder().hearingList(DynamicList.builder()
                                                                      .value(hearingElement)
                                                                      .listItems(hearingDropdowns).build())
            .caReasonsForC2Application(List.of(
                C2AdditionalOrdersRequestedCa.REQUESTING_ADJOURNMENT,
                C2AdditionalOrdersRequestedCa.APPOINTMENT_OF_GUARDIAN
            )).build();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(
                AdditionalApplicationTypeEnum.otherOrder
            )
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().caApplicantApplicationType(
                CaOtherApplicationType.C79_CHILD_ORDER).build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                                                              .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                           .applicationType(
                                                                                               OtherApplicationType
                                                                                                   .C79_CHILD_ORDER)
                                                                                           .build())
                                                              .build())))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);
        assertNotNull(stringObjectMap);
        assertTrue(stringObjectMap.containsKey(ADDITIONAL_APPLICATION_FEES_TO_PAY));
        assertEquals(CURRENCY_SIGN_POUND + BigDecimal.TEN, stringObjectMap.get(ADDITIONAL_APPLICATION_FEES_TO_PAY));
    }

    @Test
    public void testGetFeeTypes() {
        List<DynamicListElement> hearingDropdowns = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hearingDate = now.plusDays(10L);
        DynamicListElement hearingElement = DynamicListElement.builder()
            .label("First Hearing - " + hearingDate.format(formatter))
            .code("testId123456 - First Hearing")
            .build();

        c2DocumentBundle = C2DocumentBundle.builder().hearingList(DynamicList.builder()
                                                                      .value(hearingElement)
                                                                      .listItems(hearingDropdowns).build())
            .caReasonsForC2Application(List.of(
                C2AdditionalOrdersRequestedCa.REQUESTING_ADJOURNMENT,
                C2AdditionalOrdersRequestedCa.APPOINTMENT_OF_GUARDIAN
            )).build();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(
                AdditionalApplicationTypeEnum.c2Order
            )
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .representedPartyType(CA_APPLICANT)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().caApplicantApplicationType(
                CaOtherApplicationType.C1_CHILD_ORDER).build())
            .build();
        OtherApplicationType applicationType = OtherApplicationType
            .FL403_EXTEND_AN_ORDER;
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                                                              .c2DocumentBundle(C2DocumentBundle.builder().build())
                                                              .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                           .applicationType(
                                                                                               applicationType)
                                                                                           .build())
                                                              .partyType(PartyEnum.respondent)
                                                              .build())))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        assertNotNull(applicationsFeeCalculator.getFeeTypes(caseData));
    }

    @Test
    public void testCalculateAdditionalApplicationsFeeForDaApplicantWithD89_Court_Bailiff() {
        List<DynamicListElement> hearingDropdowns = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hearingDate = now.plusDays(15L);
        DynamicListElement hearingElement = DynamicListElement.builder()
            .label("First Hearing - " + hearingDate.format(formatter))
            .code("testId123456 - First Hearing")
            .build();
        C2DocumentBundle c2DocumentBundle1 = C2DocumentBundle.builder().hearingList(DynamicList.builder()
                                                                        .value(hearingElement)
                                                                        .listItems(hearingDropdowns).build())
            .caReasonsForC2Application(List.of(C2AdditionalOrdersRequestedCa.REQUESTING_ADJOURNMENT,
                                               C2AdditionalOrdersRequestedCa.APPOINTMENT_OF_GUARDIAN))
            .daReasonsForC2Application(List.of(C2AdditionalOrdersRequestedCa.REQUESTING_ADJOURNMENT)).build();

        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(
                AdditionalApplicationTypeEnum.c2Order)
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(c2DocumentBundle1)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder()
                                                  .daApplicantApplicationType(DaApplicantOtherApplicationType.D89_COURT_BAILIFF)
                                                  .build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);
        assertNotNull(stringObjectMap);
        assertTrue(stringObjectMap.containsKey(ADDITIONAL_APPLICATION_FEES_TO_PAY));
        assertEquals(CURRENCY_SIGN_POUND + BigDecimal.TEN, stringObjectMap.get(ADDITIONAL_APPLICATION_FEES_TO_PAY));
    }

    @Test
    public void testCalculateAdditionalApplicationsFeeForDaApplicantFP25() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(
                AdditionalApplicationTypeEnum.otherOrder)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder()
                                                  .daRespondentApplicationType(DaRespondentOtherApplicationType.FP25_WITNESS_SUMMONS)
                                                  .build())
            .representedPartyType(DA_APPLICANT)
            .build();

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.ZERO).build());

        Map<String, Object> stringObjectMap = applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);
        assertEquals(Collections.emptyMap(), stringObjectMap);
        verify(feeService).getFeesDataForAdditionalApplications(actualFeeTypes.capture());
        assertEquals(Collections.emptyList(), actualFeeTypes.getValue());
    }

    @Test
    public void testCalculateAdditionalApplicationsFeeForDaRespondentFP25() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(
                AdditionalApplicationTypeEnum.otherOrder)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder()
                                                  .daRespondentApplicationType(DaRespondentOtherApplicationType.FP25_WITNESS_SUMMONS)
                                                  .build())
            .representedPartyType(DA_RESPONDENT)
            .build();

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        Map<String, Object> stringObjectMap = applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);
        ArrayList<FeeType> expectedFeeTypes = new ArrayList<>();
        expectedFeeTypes.add(FeeType.FP25_WITNESS_SUMMONS);

        assertNotNull(stringObjectMap);
        assertTrue(stringObjectMap.containsKey(ADDITIONAL_APPLICATION_FEES_TO_PAY));
        assertEquals(CURRENCY_SIGN_POUND + BigDecimal.TEN, stringObjectMap.get(ADDITIONAL_APPLICATION_FEES_TO_PAY));
        verify(feeService).getFeesDataForAdditionalApplications(actualFeeTypes.capture());
        assertEquals(expectedFeeTypes, actualFeeTypes.getValue());
    }

    @Test
    public void testExceptionCalculateAdditionalApplicationsFee() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();
        assertEquals(new HashMap<>(), applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData));
    }


}
