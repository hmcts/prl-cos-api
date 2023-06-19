package uk.gov.hmcts.reform.prl.rpa.mappers;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.NewPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildPassportDetails;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class AllegationsOfHarmRevisedMapperTest {

    @InjectMocks
    AllegationsOfHarmRevisedMapper allegationsOfHarmRevisedMapper;


    @Before
    public void setUp() {

    }

    @Test
    public void testAllegationsOfHarmMapperWithSomeFields() {

        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder().newAllegationsOfHarmYesNo(YesOrNo.Yes)
                .newAllegationsOfHarmChildAbuseYesNo(YesOrNo.Yes)
                .newAllegationsOfHarmChildAbductionYesNo(YesOrNo.Yes)
                .newChildAbductionReasons("reasons")
                .newPreviousAbductionThreats(YesOrNo.Yes)
                .newPreviousAbductionThreatsDetails("ThreadDetails")
                .newAllegationsOfHarmOtherConcernsDetails("OtherHarm")
                .newAgreeChildUnsupervisedTime(YesOrNo.Yes)
                .newAgreeChildSupervisedTime(YesOrNo.Yes)
                .newAgreeChildOtherContact(YesOrNo.Yes)
                .newChildrenLocationNow("LocationNow")
                .newAbductionPassportOfficeNotified(YesOrNo.Yes).newAgreeChildOtherContact(YesOrNo.Yes)
                .newChildrenLocationNow("locationNow").newAbductionPassportOfficeNotified(YesOrNo.Yes)
                .newAbductionChildHasPassport(YesOrNo.Yes)
                .build();
        CaseData caseData = CaseData.builder().allegationOfHarmRevised(allegationOfHarmRevised).build();

        assertNotNull(allegationsOfHarmRevisedMapper.map(caseData));

    }


    @Test
    public void testNoDataAllegationsOfHarmMapperMapBehaviours() {

        CaseData caseData = CaseData.builder()
                .allegationOfHarmRevised(AllegationOfHarmRevised.builder().build()).build();
        assertNotNull(allegationsOfHarmRevisedMapper.map(caseData));
    }

    @Test
    public void testDataAllegationsOfHarmMapperMapCollections() {


        DomesticAbuseBehaviours domesticAbuseBehaviours = DomesticAbuseBehaviours.builder().typeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
                .newAbuseNatureDescription("des").newBehavioursApplicantHelpSoughtWho("sought").newBehavioursApplicantSoughtHelp(YesOrNo.Yes).build();

        Element<DomesticAbuseBehaviours> domesticAbuseBehavioursElement = Element
                .<DomesticAbuseBehaviours>builder().value(domesticAbuseBehaviours).build();


        ChildAbuse childAbuse = ChildAbuse.builder().abuseNatureDescription("test")
            //.allChildrenAreRisk(YesOrNo.Yes)
            .build();

        ChildPassportDetails childPassportDetails = ChildPassportDetails.builder().newChildHasMultiplePassports(YesOrNo.Yes)
                .newChildPassportPossession(List
                .of(NewPassportPossessionEnum.father)).newChildPassportPossessionOtherDetails("de").build();

        CaseData caseData = CaseData.builder()
                .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                        .childPassportDetails(childPassportDetails)
                        .domesticBehaviours(Collections.singletonList(domesticAbuseBehavioursElement))
                        .newAllegationsOfHarmChildAbuseYesNo(YesOrNo.Yes)
                        .childPhysicalAbuse(childAbuse)
                        .childPsychologicalAbuse(childAbuse)
                        .childEmotionalAbuse(childAbuse)
                        .childFinancialAbuse(childAbuse)
                        .childSexualAbuse(childAbuse)
                        .build()).build();
        assertNotNull(allegationsOfHarmRevisedMapper.map(caseData));
    }
}


