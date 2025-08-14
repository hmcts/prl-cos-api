package uk.gov.hmcts.reform.prl.rpa.mappers;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class AllegationsOfHarmMapperTest {

    @InjectMocks
    AllegationsOfHarmMapper allegationsOfHarmMapper;

    List<ApplicantOrChildren> physicalAbuseVictim;
    List<ApplicantOrChildren> emotionalAbuseVictim;
    List<ApplicantOrChildren> psychologicalAbuseVictim;
    List<ApplicantOrChildren> sexualAbuseVictim;
    List<ApplicantOrChildren> financialAbuseVictim;
    List<Element<Behaviours>> listOfbehaviours;

    Behaviours behaviours;

    @Before
    public void setUp() {
        physicalAbuseVictim = new ArrayList<>();
        physicalAbuseVictim.add(ApplicantOrChildren.applicants);
        emotionalAbuseVictim = new ArrayList<>();
        emotionalAbuseVictim.add(ApplicantOrChildren.applicants);
        psychologicalAbuseVictim = new ArrayList<>();
        psychologicalAbuseVictim.add(ApplicantOrChildren.applicants);
        sexualAbuseVictim = new ArrayList<>();
        sexualAbuseVictim.add(ApplicantOrChildren.applicants);
        financialAbuseVictim = new ArrayList<>();
        financialAbuseVictim.add(ApplicantOrChildren.applicants);
        behaviours = Behaviours.builder().behavioursNature("Nature").behavioursApplicantSoughtHelp(YesOrNo.Yes)
            .build();
        Element<Behaviours> behavioursWithChildElement = Element
            .<Behaviours>builder().value(behaviours).build();
        listOfbehaviours = Collections.singletonList(behavioursWithChildElement);

    }

    @Test
    public void testAllegationsOfHarmMapperWithSomeFields() {

        AllegationOfHarm allegationOfHarm = AllegationOfHarm.builder().allegationsOfHarmYesNo(YesOrNo.Yes)
            .allegationsOfHarmChildAbuseYesNo(YesOrNo.Yes)
            .physicalAbuseVictim(physicalAbuseVictim)
            .emotionalAbuseVictim(emotionalAbuseVictim)
            .financialAbuseVictim(financialAbuseVictim)
            .psychologicalAbuseVictim(psychologicalAbuseVictim)
            .sexualAbuseVictim(sexualAbuseVictim)
            .allegationsOfHarmChildAbductionYesNo(YesOrNo.Yes)
            .childAbductionReasons("reasons")
            .previousAbductionThreats(YesOrNo.Yes)
            .previousAbductionThreatsDetails("ThreadDetails")
            .allegationsOfHarmOtherConcernsDetails("OtherHarm")
            .agreeChildUnsupervisedTime(YesOrNo.Yes)
            .agreeChildSupervisedTime(YesOrNo.Yes)
            .agreeChildOtherContact(YesOrNo.Yes)
            .childrenLocationNow("LocationNow")
            .abductionPassportOfficeNotified(YesOrNo.Yes).agreeChildOtherContact(YesOrNo.Yes)
            .childrenLocationNow("locationNow").abductionPassportOfficeNotified(YesOrNo.Yes)
            .abductionChildHasPassport(YesOrNo.Yes).abductionChildPassportPosession(AbductionChildPassportPossessionEnum.father)
            .abductionChildPassportPosessionOtherDetail("OtherDetails").build();
        CaseData caseData = CaseData.builder().allegationOfHarm(allegationOfHarm).build();

        assertNotNull(allegationsOfHarmMapper.map(caseData));

    }

    @Test
    public void testAllegationsOfHarmMapperMapBehaviours() {

        AllegationOfHarm allegationOfHarm = AllegationOfHarm.builder()
            .allegationsOfHarmYesNo(YesOrNo.Yes)
            .allegationsOfHarmChildAbuseYesNo(YesOrNo.Yes)
            .physicalAbuseVictim(physicalAbuseVictim)
            .emotionalAbuseVictim(emotionalAbuseVictim)
            .financialAbuseVictim(financialAbuseVictim)
            .psychologicalAbuseVictim(psychologicalAbuseVictim)
            .sexualAbuseVictim(sexualAbuseVictim).behaviours(listOfbehaviours).build();
        CaseData caseData = CaseData.builder().allegationOfHarm(allegationOfHarm).build();
        assertNotNull(allegationsOfHarmMapper.map(caseData));
    }

    @Test
    public void testNoDataAllegationsOfHarmMapperMapBehaviours() {

        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder().build()).build();
        assertNotNull(allegationsOfHarmMapper.map(caseData));
    }
}


