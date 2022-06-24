package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class MiamMapperTest {

    @InjectMocks
    MiamMapper miamMapper;

    ArrayList<MiamExemptionsChecklistEnum> miamExemptionsChecklistEnum;
    ArrayList<MiamDomesticViolenceChecklistEnum> miamDomesticViolenceChecklistEnum;
    ArrayList<MiamUrgencyReasonChecklistEnum> miamUrgencyReasonChecklistEnum;

    @Before
    public void setUp() {
        miamExemptionsChecklistEnum = new ArrayList<>();
        miamExemptionsChecklistEnum.add(MiamExemptionsChecklistEnum.previousMIAMattendance);
        miamExemptionsChecklistEnum.add(MiamExemptionsChecklistEnum.domesticViolence);
        miamDomesticViolenceChecklistEnum = new ArrayList<>();
        miamDomesticViolenceChecklistEnum
            .add(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_1);
        miamUrgencyReasonChecklistEnum = new ArrayList<>();
        miamUrgencyReasonChecklistEnum.add(MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_1);


    }

    @Test
    public void testMiamMapperWithAllFields() {


        CaseData caseData = CaseData.builder().applicantAttendedMiam(YesOrNo.Yes)
            .claimingExemptionMiam(YesOrNo.Yes).familyMediatorMiam(YesOrNo.Yes)
            .miamDomesticViolenceChecklist(miamDomesticViolenceChecklistEnum)
            .miamUrgencyReasonChecklist(miamUrgencyReasonChecklistEnum)
            .miamOtherGroundsChecklist(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_1)
            .miamPreviousAttendanceChecklist(MiamPreviousAttendanceChecklistEnum
                                                 .miamPreviousAttendanceChecklistEnum_Value_1)
            .mediatorRegistrationNumber("registrationNumber").familyMediatorServiceName("familyServiceNumber")
            .soleTraderName1("SoleTraderName1").build();
        assertNotNull(miamMapper.map(caseData));

    }

    @Test
    public void testMiamMapperWithSomeFields() {
        CaseData caseData = CaseData.builder().applicantAttendedMiam(YesOrNo.Yes)
            .claimingExemptionMiam(YesOrNo.Yes).familyMediatorMiam(YesOrNo.Yes).miamExemptionsChecklist(
                miamExemptionsChecklistEnum)
            .miamDomesticViolenceChecklist(miamDomesticViolenceChecklistEnum)
            .miamUrgencyReasonChecklist(miamUrgencyReasonChecklistEnum)
            .miamOtherGroundsChecklist(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_1)
            .mediatorRegistrationNumber("registrationNumber").familyMediatorServiceName("familyServiceNumber")
            .soleTraderName1("SoleTraderName1").build();
        assertNotNull(miamMapper.map(caseData));

    }

}
