package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.CaApplicantOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.CaRespondentOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DaApplicantOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DaRespondentOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.OtherApplicationType;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
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
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ADDITIONAL_APPLICATION_FEES_TO_PAY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CA_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CURRENCY_SIGN_POUND;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@RunWith(MockitoJUnitRunner.class)
public class ApplicationsFeeCalculatorTest {

    @InjectMocks
    private ApplicationsFeeCalculator applicationsFeeCalculator;
    @Mock
    private FeeService feeService;

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
            .reasonsForC2Application(List.of(C2AdditionalOrdersRequested.REQUESTING_ADJOURNMENT)).build();
    }

    @Test
    public void testCalculateAdditionalApplicationsFeeForCa() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(List.of(
                AdditionalApplicationTypeEnum.c2Order,
                AdditionalApplicationTypeEnum.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().caApplicantApplicationType(
                CaApplicantOtherApplicationType.C1_APPLY_FOR_CERTAIN_ORDERS_UNDER_THE_CHILDREN_ACT).build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                                                              .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                           .applicationType(
                                                                                               OtherApplicationType
                                                                                                   .D89_BAILIFF_CA)
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
            .additionalApplicationsApplyingFor(List.of(
                AdditionalApplicationTypeEnum.c2Order,
                AdditionalApplicationTypeEnum.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().caRespondentApplicationType(
                CaRespondentOtherApplicationType.C1_APPLY_FOR_CERTAIN_ORDERS_UNDER_THE_CHILDREN_ACT).build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                                                              .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                           .applicationType(
                                                                                               OtherApplicationType
                                                                                                   .D89_BAILIFF_CA)
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
            .additionalApplicationsApplyingFor(List.of(
                AdditionalApplicationTypeEnum.c2Order,
                AdditionalApplicationTypeEnum.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder()
                                                  .daApplicantApplicationType(DaApplicantOtherApplicationType.N161_APPELLANT_NOTICE_DA)
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
            .additionalApplicationsApplyingFor(List.of(
                AdditionalApplicationTypeEnum.c2Order,
                AdditionalApplicationTypeEnum.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder()
                                                  .daRespondentApplicationType(DaRespondentOtherApplicationType.N161_APPELLANT_NOTICE_DA)
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
            .additionalApplicationsApplyingFor(List.of(
                AdditionalApplicationTypeEnum.c2Order,
                AdditionalApplicationTypeEnum.otherOrder
            ))
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
    public void testCalculateAdditionalApplicationsFeeForHearingLessThan14Days() {
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
            .reasonsForC2Application(List.of(
                C2AdditionalOrdersRequested.REQUESTING_ADJOURNMENT,
                C2AdditionalOrdersRequested.APPOINTMENT_OF_GUARDIAN
            )).build();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(List.of(
                AdditionalApplicationTypeEnum.c2Order,
                AdditionalApplicationTypeEnum.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().caApplicantApplicationType(
                CaApplicantOtherApplicationType.C1_APPLY_FOR_CERTAIN_ORDERS_UNDER_THE_CHILDREN_ACT).build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                                                              .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                           .applicationType(
                                                                                               OtherApplicationType
                                                                                                   .D89_BAILIFF_CA)
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
            .reasonsForC2Application(List.of(
                C2AdditionalOrdersRequested.REQUESTING_ADJOURNMENT,
                C2AdditionalOrdersRequested.APPOINTMENT_OF_GUARDIAN
            )).build();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(List.of(
                AdditionalApplicationTypeEnum.c2Order,
                AdditionalApplicationTypeEnum.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .representedPartyType(CA_APPLICANT)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().caApplicantApplicationType(
                CaApplicantOtherApplicationType.C1_APPLY_FOR_CERTAIN_ORDERS_UNDER_THE_CHILDREN_ACT).build())
            .build();
        OtherApplicationType applicationType = OtherApplicationType
            .FL403_APPLICATION_TO_VARY_DISCHARGE_OR_EXTEND_AN_ORDER;
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
}
