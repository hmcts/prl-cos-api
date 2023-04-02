package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.AbuseTypes;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.WhomConsistPassportList;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentChildAbduction;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentOtherConcerns;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RespondentAllegationsOfHarmCheckerTest {

    @InjectMocks
    RespondentAllegationsOfHarmChecker respondentAllegationsOfHarmChecker;

    CaseData caseData;

    @Before
    public void setUp() {

        Behaviours behaviours = Behaviours
            .builder()
            .typesOfAbuse(AbuseTypes.emotionalAbuse)
            .natureOfBehaviour("Test")
            .abuseStartDateAndLength("02/12/2000")
            .respondentSoughtHelp(Yes)
            .respondentTypeOfHelp("Test")
            .build();

        Element<Behaviours> wrappedBehaviours = Element.<Behaviours>builder().value(behaviours).build();
        List<Element<Behaviours>> behaviourList = Collections.singletonList(wrappedBehaviours);

        Behaviours childBehaviours = Behaviours
            .builder()
            .typesOfAbuse(AbuseTypes.emotionalAbuse)
            .natureOfBehaviour("Test")
            .abuseStartDateAndLength("02/12/2000")
            .respondentSoughtHelp(Yes)
            .respondentTypeOfHelp("Test")
            .build();

        Element<Behaviours> wrappedChildBehaviours = Element.<Behaviours>builder().value(childBehaviours).build();
        List<Element<Behaviours>> behaviourChildList = Collections.singletonList(wrappedChildBehaviours);

        WhomConsistPassportList passportList = WhomConsistPassportList.otherPeople;

        PartyDetails respondent = PartyDetails.builder()
            .response(Response
                          .builder()
                          .activeRespondent(Yes)
                          .respondentAllegationsOfHarmData(RespondentAllegationsOfHarmData
                                   .builder()
                                   .respDomesticAbuseInfo(behaviourList)
                                   .respChildAbuseInfo(behaviourChildList)
                                   .respChildAbductionInfo(RespondentChildAbduction
                                                               .builder()
                                                               .previousThreatsForChildAbduction(Yes)
                                                               .previousThreatsForChildAbductionDetails("Test")
                                                               .reasonForChildAbductionBelief("Test")
                                                               .whereIsChild("Test")
                                                               .hasPassportOfficeNotified(Yes)
                                                               .childrenHavePassport(Yes)
                                                               .childrenHaveMoreThanOnePassport(Yes)
                                                               .whoHasChildPassport(Collections.singletonList(
                                                                   passportList))
                                                               .whoHasChildPassportOther("father")
                                                               .anyOrgInvolvedInPreviousAbduction(Yes)
                                                               .anyOrgInvolvedInPreviousAbductionDetails("Test")
                                                               .build())
                                   .respOtherConcernsInfo(RespondentOtherConcerns
                                                              .builder()
                                                              .childHavingOtherFormOfContact(Yes)
                                                              .childSpendingSupervisedTime(Yes)
                                                              .ordersRespondentWantFromCourt("Test")
                                                              .childSpendingUnsupervisedTime(Yes)
                                                              .build())
                                   .respAllegationsOfHarmInfo(RespondentAllegationsOfHarm
                                                                  .builder()
                                                                  .respondentChildAbuse(Yes)
                                                                  .isRespondentChildAbduction(Yes)
                                                                  .respondentNonMolestationOrder(Yes)
                                                                  .respondentOccupationOrder(Yes)
                                                                  .respondentForcedMarriageOrder(Yes)
                                                                  .respondentDrugOrAlcoholAbuse(Yes)
                                                                  .respondentOtherInjunctiveOrder(Yes)
                                                                  .respondentRestrainingOrder(Yes)
                                                                  .respondentDomesticAbuse(Yes)
                                                                  .respondentDrugOrAlcoholAbuseDetails("Test")
                                                                  .respondentOtherSafetyConcerns(Yes)
                                                                  .respondentOtherSafetyConcernsDetails("Test")
                                                                  .build())
                                   .respAohYesOrNo(Yes)
                                   .build())
                          .build())
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder().respondents(respondentList).build();
    }

    @Test
    public void isStarted() {
        Boolean bool = respondentAllegationsOfHarmChecker.isStarted(caseData, "A");

        assertTrue(bool);
    }

    @Test
    public void hasMandatoryCompleted() {

        Boolean bool = respondentAllegationsOfHarmChecker.isFinished(caseData, "A");

        assertTrue(bool);
    }

}
