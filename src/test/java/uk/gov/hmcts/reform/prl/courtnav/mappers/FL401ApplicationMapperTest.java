package uk.gov.hmcts.reform.prl.courtnav.mappers;

import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.*;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.*;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FL401ApplicationMapperTest {

    @InjectMocks
    FL401ApplicationMapper fl401ApplicationMapper;

    @Mock
    private CourtFinderService courtFinderService;

    @Mock
    private Court court;

    private CourtNavFl401 courtNavFl401;

    @Before
    public void setUp() {

        courtNavFl401 = null;

        court = Court.builder()
            .courtName("testcourt")
            .build();

    }

    @Test
    public void testCourtnavMetaDataIsNull() throws NotFoundException {

        courtNavFl401 = CourtNavFl401.builder()
            .metaData(CourtNavMetaData.builder()
                          .caseOrigin(null)
                          .build())
            .build();
        CaseData caseData = CaseData.builder()
            .caseOrigin(null)
            .build();

        assertEquals(courtNavFl401.getMetaData().getCaseOrigin(), caseData.getCaseOrigin());
        assertNull(courtNavFl401.getMetaData().getCaseOrigin());

    }

    @Test
    public void testCourtnavCaseDataIsNull() throws NotFoundException {

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(null)
            .build();

        assertNull(courtNavFl401.getFl401());

    }

    @Test
    public void testCourtnavCaseDataWithBeforeStart() throws NotFoundException {

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(BeforeStart.builder()
                                        .applicantHowOld(ApplicantAge.eighteenOrOlder)
                                        .build())
                       .build())
            .build();
        CaseData caseData = CaseData.builder()
            .applicantAge(ApplicantAge.eighteenOrOlder)
            .build();

        assertEquals(courtNavFl401.getFl401().getBeforeStart().getApplicantHowOld(), caseData.getApplicantAge());
        assertNotNull(courtNavFl401.getFl401().getBeforeStart().getApplicantHowOld());

    }

    @Test
    public void testCourtnavCaseDataWithSituationDetails() throws NotFoundException {

        BeforeStart beforeStart = BeforeStart.builder()
            .applicantHowOld(ApplicantAge.eighteenOrOlder)
            .build();

        List<FL401OrderTypeEnum> fl401OrderTypeEnum = new ArrayList<>();

        fl401OrderTypeEnum.add(FL401OrderTypeEnum.nonMolestationOrder);

        List<WithoutNoticeReasonEnum> withoutNoticeReasonEnum = new ArrayList<>();

        withoutNoticeReasonEnum.add(WithoutNoticeReasonEnum.riskOfSignificantHarm);

        Situation situation = Situation.builder()
            .ordersAppliedFor(fl401OrderTypeEnum)
            .ordersAppliedWithoutNotice(true)
            .additionalDetailsForCourt("test details")
            .bailConditionsOnRespondent(true)
            .ordersAppliedWithoutNoticeReasonDetails("test1")
            .bailConditionsEndDate(CourtNavDate.builder()
                                       .day(8)
                                       .month(9)
                                       .year(1996)
                                       .build())
            .ordersAppliedWithoutNoticeReason(withoutNoticeReasonEnum)
            .build();

        ApplicantsDetails applicantsDetails = ApplicantsDetails.builder()
            .applicantFirstName("courtnav Applicant")
            .applicantLastName("test")
            .applicantDateOfBirth(CourtNavDate.builder()
                                      .day(10)
                                      .month(9)
                                      .year(1992)
                                      .build())
            .applicantGender(CourtNavGender.builder()
                                 .value(ApplicantGenderEnum.Female)
                                 .other(null)
                                 .build())
            .shareContactDetailsWithRespondent(false)
            .applicantEmailAddress("test@courtNav.com")
            .applicantPhoneNumber("12345678907")
            .applicantHasLegalRepresentative(false)
            .applicantAddress(Address.builder()
                                  .addressLine1("55 Test Street")
                                  .postTown("Town")
                                  .postCode("LU1 5ET")
                                  .build())
            .build();

        RespondentDetails respondentDetails = RespondentDetails.builder()
            .respondentFirstName("resp test")
            .respondentLastName("fl401")
            .respondentDateOfBirth(CourtNavDate.builder()
                                       .day(10)
                                       .month(9)
                                       .year(1989)
                                       .build())
            .respondentEmailAddress("test@resp.com")
            .respondentAddress(Address.builder()
                                   .addressLine1("55 Test Street")
                                   .postTown("Town")
                                   .postCode("LU1 5ET")
                                   .build())
            .respondentLivesWithApplicant(true)
            .respondentPhoneNumber("12345670987")
            .build();

        List<CourtProceedings> courtProceedings = new ArrayList<>();
        courtProceedings.add(CourtProceedings.builder()
                                 .caseDetails("testcase1")
                                 .caseNumber("1234567")
                                 .caseType("testType1")
                                 .nameOfCourt("testcourt1")
                                 .build());

        Family family = Family.builder()
            .whoApplicationIsFor(ApplicationCoverEnum.applicantOnly)
            .protectedChildren(null)
            .anyOngoingCourtProceedings(false)
            .ongoingCourtProceedings(courtProceedings)
            .build();

        CourtNavRelationShipToRespondent relationShipToRespondent = CourtNavRelationShipToRespondent.builder()
            .relationshipDescription(ApplicantRelationshipDescriptionEnum.noneOfAbove)
            .ceremonyDate(null)
            .relationshipEndDate(null)
            .relationshipStartDate(null)
            .respondentsRelationshipToApplicant(ApplicantRelationshipOptionsEnum.cousin)
            .relationshipToApplicantOther(null)
            .anyChildren(false)
            .build();

        List<BehaviourTowardsApplicantEnum> behaviourTowardsApplicantEnum = new ArrayList<>();
        behaviourTowardsApplicantEnum.add(BehaviourTowardsApplicantEnum.comingNearHome);

        List<BehaviourTowardsChildrenEnum> behaviourTowardsChildrenEnum = new ArrayList<>();
        behaviourTowardsChildrenEnum.add(BehaviourTowardsChildrenEnum.beingViolentOrThreatening);

        CourtNavRespondentBehaviour respondentBehaviour = CourtNavRespondentBehaviour.builder()
            .applyingForMonMolestationOrder(true)
            .stopBehaviourAnythingElse("abc")
            .stopBehaviourTowardsApplicant(behaviourTowardsApplicantEnum)
            .stopBehaviourTowardsChildren(behaviourTowardsChildrenEnum)
            .build();

        TheHome home = TheHome.builder()
            .applyingForOccupationOrder(false)
            .build();

        List<ConsentEnum> consentEnum = new ArrayList<>();
        consentEnum.add(ConsentEnum.applicantConfirm);

        CourtNavStmtOfTruth stmtOfTruth = CourtNavStmtOfTruth.builder()
            .declaration(consentEnum)
            .signature("appl sign")
            .signatureDate(CourtNavDate.builder()
                               .day(10)
                               .month(6)
                               .year(2022)
                               .build())
            .signatureFullName("Applicant Courtnav")
            .representativeFirmName("courtnav_application")
            .representativePositionHeld("courtnav_application")
            .build();

        List<SpecialMeasuresEnum> specialMeasuresEnum = new ArrayList<>();
        specialMeasuresEnum.add(SpecialMeasuresEnum.separateWaitingRoom);

        GoingToCourt goingToCourt = GoingToCourt.builder()
            .isInterpreterRequired(false)
            .interpreterLanguage(null)
            .interpreterDialect(null)
            .anyDisabilityNeeds(false)
            .disabilityNeedsDetails(null)
            .anySpecialMeasures(specialMeasuresEnum)
            .courtSpecialRequirements("test special court")
            .build();

        CourtNavMetaData courtNavMetaData = CourtNavMetaData.builder()
            .courtNavApproved(true)
            .caseOrigin("courtnav")
            .numberOfAttachments(4)
            .build();

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(beforeStart)
                       .situation(situation)
                       .applicantDetails(applicantsDetails)
                       .respondentDetails(respondentDetails)
                       .family(family)
                       .relationshipWithRespondent(relationShipToRespondent)
                       .respondentBehaviour(respondentBehaviour)
                       .theHome(home)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
            .dateSubmittedAndTime(DateTimeFormatter.ofPattern("d MMM yyyy, hh:mm:ssa").format(zonedDateTime).toUpperCase())
            .applicantsFL401(PartyDetails.builder()
                                 .address(applicantsDetails.getApplicantAddress())
                                 .build())
            .build();
        String email = "dacourt@test.com";
        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();
        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));
        caseData = caseData.toBuilder()
            .courtName("testcourt")
            .build();

        caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor(), caseData.getTypeOfApplicationOrders().getOrderType());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedWithoutNoticeReason());

    }


}
