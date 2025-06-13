package uk.gov.hmcts.reform.prl.rpa.mappers;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class AllegationsOfHarmMapperTest {

    @InjectMocks
    private AllegationsOfHarmMapper allegationsOfHarmMapper;

    private List<ApplicantOrChildren> physicalAbuseVictim;
    private List<ApplicantOrChildren> emotionalAbuseVictim;
    private List<ApplicantOrChildren> psychologicalAbuseVictim;
    private List<ApplicantOrChildren> sexualAbuseVictim;
    private List<ApplicantOrChildren> financialAbuseVictim;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void testAllegationsOfHarmMapperWithSomeFields() {

        AllegationOfHarm allegationOfHarm = AllegationOfHarm.builder().allegationsOfHarmYesNo(Yes)
            .allegationsOfHarmChildAbuseYesNo(Yes)
            .physicalAbuseVictim(physicalAbuseVictim)
            .emotionalAbuseVictim(emotionalAbuseVictim)
            .financialAbuseVictim(financialAbuseVictim)
            .psychologicalAbuseVictim(psychologicalAbuseVictim)
            .sexualAbuseVictim(sexualAbuseVictim)
            .allegationsOfHarmChildAbductionYesNo(Yes)
            .childAbductionReasons("reasons")
            .previousAbductionThreats(Yes)
            .previousAbductionThreatsDetails("ThreadDetails")
            .allegationsOfHarmOtherConcernsDetails("OtherHarm")
            .agreeChildUnsupervisedTime(Yes)
            .agreeChildSupervisedTime(Yes)
            .agreeChildOtherContact(Yes)
            .childrenLocationNow("LocationNow")
            .abductionPassportOfficeNotified(Yes).agreeChildOtherContact(Yes)
            .childrenLocationNow("locationNow").abductionPassportOfficeNotified(Yes)
            .abductionChildHasPassport(Yes).abductionChildPassportPosession(AbductionChildPassportPossessionEnum.father)
            .abductionChildPassportPosessionOtherDetail("OtherDetails").build();
        CaseData caseData = CaseData.builder().allegationOfHarm(allegationOfHarm).build();

        assertNotNull(allegationsOfHarmMapper.map(caseData));

    }

    @Test
    void testAllegationsOfHarmMapperMapBehaviours() {

        List<Element<Behaviours>> behaviours = List.of(
            Element.<Behaviours>builder()
                .value(Behaviours.builder()
                           .behavioursNature("Nature")
                           .behavioursApplicantSoughtHelp(Yes)
                           .build())
                .build());


        AllegationOfHarm allegationOfHarm = AllegationOfHarm.builder()
            .allegationsOfHarmYesNo(Yes)
            .allegationsOfHarmChildAbuseYesNo(Yes)
            .physicalAbuseVictim(physicalAbuseVictim)
            .emotionalAbuseVictim(emotionalAbuseVictim)
            .financialAbuseVictim(financialAbuseVictim)
            .psychologicalAbuseVictim(psychologicalAbuseVictim)
            .sexualAbuseVictim(sexualAbuseVictim).behaviours(behaviours).build();
        CaseData caseData = CaseData.builder().allegationOfHarm(allegationOfHarm).build();
        assertNotNull(allegationsOfHarmMapper.map(caseData));
    }

    @Test
    void testNoDataAllegationsOfHarmMapperMapBehaviours() {

        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder().build()).build();
        assertNotNull(allegationsOfHarmMapper.map(caseData));
    }
}


