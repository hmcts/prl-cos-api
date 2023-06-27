package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.CaApplicantOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DaApplicantOtherApplicationType;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ADDITIONAL_APPLICATION_FEES_TO_PAY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CURRENCY_SIGN_POUND;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;


@RunWith(MockitoJUnitRunner.class)
public class ApplicationsFeeCalculatorTest {

    @InjectMocks
    private ApplicationsFeeCalculator applicationsFeeCalculator;
    @Mock
    private FeeService feeService;


    @Test
    public void testCalculateAdditionalApplicationsFeeForCa() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(List.of(
                AdditionalApplicationTypeEnum.c2Order,
                AdditionalApplicationTypeEnum.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(C2DocumentBundle.builder().build())
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().caApplicantApplicationType(
                CaApplicantOtherApplicationType.C1_APPLY_FOR_CERTAIN_ORDERS_UNDER_THE_CHILDREN_ACT).build())
            .build();

        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenReturn(FeeResponse.builder().amount(
            BigDecimal.TEN).build());

        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);
        assertNotNull(stringObjectMap);
        assertTrue(stringObjectMap.containsKey(ADDITIONAL_APPLICATION_FEES_TO_PAY));
        assertEquals(CURRENCY_SIGN_POUND + BigDecimal.TEN, stringObjectMap.get(ADDITIONAL_APPLICATION_FEES_TO_PAY));
    }

    @Test
    public void testCalculateAdditionalApplicationsFeeForDa() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(List.of(
                AdditionalApplicationTypeEnum.c2Order,
                AdditionalApplicationTypeEnum.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(C2DocumentBundle.builder().build())
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

    @Test(expected = Exception.class)
    public void testCalculateAdditionalApplicationsFeeForError() {
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(List.of(
                AdditionalApplicationTypeEnum.c2Order,
                AdditionalApplicationTypeEnum.otherOrder
            ))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(C2DocumentBundle.builder().build())
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .build();
        when(feeService.getFeesDataForAdditionalApplications(anyList())).thenThrow(new Exception());
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .build();
        applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);

    }
}
