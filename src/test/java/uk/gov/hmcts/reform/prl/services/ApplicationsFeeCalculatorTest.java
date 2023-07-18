package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationCategory;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
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

    private DynamicList otherApplicationTypes;

    @Before
    public void setup() {
        List<DynamicListElement> hearingDropdowns = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hearingDate = now.plusDays(15L);
        DynamicListElement hearingElement = DynamicListElement.builder()
            .label("First Hearing - " + hearingDate.format(formatter))
            .code("testId123456 - First Hearing")
            .build();

        List<DynamicMultiselectListElement> c2DynamicMultiselectListElements = new ArrayList<>();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code(AdditionalApplicationType.C2_REQUESTING_ADJOURNMENT.getId())
            .label(AdditionalApplicationType.C2_REQUESTING_ADJOURNMENT.getDisplayValue())
            .build();
        c2DynamicMultiselectListElements.add(dynamicMultiselectListElement);
        DynamicMultiSelectList c2DynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(c2DynamicMultiselectListElements)
            .listItems(c2DynamicMultiselectListElements)
            .build();

        c2DocumentBundle = C2DocumentBundle.builder().hearingList(DynamicList.builder()
                                                                      .value(hearingElement)
                                                                      .listItems(hearingDropdowns).build())
            .c2ApplicationTypes(c2DynamicMultiSelectList).build();

        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        DynamicListElement dynamicListElement = DynamicListElement.builder().code(AdditionalApplicationType.C1_CHILD_ORDER.getId())
            .label(AdditionalApplicationType.C1_CHILD_ORDER.getDisplayValue()).build();
        dynamicListElements.add(dynamicListElement);
        otherApplicationTypes = DynamicList.builder().value(dynamicListElement).listItems(dynamicListElements).build();
    }

    @Test
    public void testCalculateAdditionalApplicationsFeeForCa() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(List.of(
                AdditionalApplicationCategory.c2Order,
                AdditionalApplicationCategory.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().otherApplicationTypes(
                otherApplicationTypes).build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                                                              .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                           .otherApplicationReason(
                                                                                               AdditionalApplicationType.D89_BAILIFF
                                                                                                   .getDisplayValue())
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
                AdditionalApplicationCategory.c2Order,
                AdditionalApplicationCategory.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().otherApplicationTypes(
                otherApplicationTypes)
            .build()).build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                                                              .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                           .otherApplicationReason(
                                                                                               AdditionalApplicationType.D89_BAILIFF
                                                                                                   .getDisplayValue())
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
                AdditionalApplicationCategory.c2Order,
                AdditionalApplicationCategory.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().otherApplicationTypes(
                otherApplicationTypes).build())
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
                AdditionalApplicationCategory.c2Order,
                AdditionalApplicationCategory.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder()
                                                  .otherApplicationTypes(
                                                      otherApplicationTypes).build())
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
                AdditionalApplicationCategory.c2Order,
                AdditionalApplicationCategory.otherOrder
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

        List<DynamicMultiselectListElement> c2DynamicMultiselectListElements = new ArrayList<>();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code(AdditionalApplicationType.C2_REQUESTING_ADJOURNMENT.getId())
            .label(AdditionalApplicationType.C2_REQUESTING_ADJOURNMENT.getDisplayValue())
            .build();
        DynamicMultiselectListElement dynamicMultiselectListElement1 = DynamicMultiselectListElement.builder()
            .code(AdditionalApplicationType.C2_APPOINTMENT_OF_GUARDIAN.getId())
            .label(AdditionalApplicationType.C2_APPOINTMENT_OF_GUARDIAN.getDisplayValue())
            .build();
        c2DynamicMultiselectListElements.add(dynamicMultiselectListElement);
        c2DynamicMultiselectListElements.add(dynamicMultiselectListElement1);
        DynamicMultiSelectList c2DynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(
                c2DynamicMultiselectListElements)
            .build();


        c2DocumentBundle = C2DocumentBundle.builder().hearingList(DynamicList.builder()
                                                                      .value(hearingElement)
                                                                      .listItems(hearingDropdowns).build())
            .c2ApplicationTypes(c2DynamicMultiSelectList).build();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(List.of(
                AdditionalApplicationCategory.c2Order,
                AdditionalApplicationCategory.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().otherApplicationTypes(
                otherApplicationTypes).build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                                                              .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                           .otherApplicationReason(
                                                                                               AdditionalApplicationType.D89_BAILIFF
                                                                                                   .getDisplayValue())
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

        List<DynamicMultiselectListElement> c2DynamicMultiselectListElements = new ArrayList<>();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code(AdditionalApplicationType.C2_REQUESTING_ADJOURNMENT.getId())
            .label(AdditionalApplicationType.C2_REQUESTING_ADJOURNMENT.getDisplayValue())
            .build();
        DynamicMultiselectListElement dynamicMultiselectListElement1 = DynamicMultiselectListElement.builder()
            .code(AdditionalApplicationType.C2_APPOINTMENT_OF_GUARDIAN.getId())
            .label(AdditionalApplicationType.C2_APPOINTMENT_OF_GUARDIAN.getDisplayValue())
            .build();
        c2DynamicMultiselectListElements.add(dynamicMultiselectListElement);
        c2DynamicMultiselectListElements.add(dynamicMultiselectListElement1);
        DynamicMultiSelectList c2DynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(
                c2DynamicMultiselectListElements)
            .build();

        c2DocumentBundle = C2DocumentBundle.builder().hearingList(DynamicList.builder()
                                                                      .value(hearingElement)
                                                                      .listItems(hearingDropdowns).build())
            .c2ApplicationTypes(c2DynamicMultiSelectList).build();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(List.of(
                AdditionalApplicationCategory.c2Order,
                AdditionalApplicationCategory.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithoutNotice)
            .representedPartyType(CA_APPLICANT)
            .temporaryC2Document(c2DocumentBundle)
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().otherApplicationTypes(
                otherApplicationTypes)
            .build()).build();
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle.builder()
                                                              .c2DocumentBundle(C2DocumentBundle.builder().build())
                                                              .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                           .otherApplicationReason(
                                                                                               AdditionalApplicationType.D89_BAILIFF
                                                                                                   .getDisplayValue())
                                                                                           .build())
                                                              .partyType(PartyEnum.respondent)
                                                              .build())))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        assertNotNull(applicationsFeeCalculator.getFeeTypes(caseData));
    }
}
