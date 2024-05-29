package uk.gov.hmcts.reform.prl.courtnav.mappers;

import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ApplicantsDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.BeforeStart;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ChildAtAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavMetaData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavRelationShipToRespondent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavRespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavStmtOfTruth;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtnavAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.Family;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.GoingToCourt;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ProtectedChild;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.RespondentDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.Situation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.TheHome;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantGenderEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantRelationshipDescriptionEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicationCoverEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsApplicantEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ConsentEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ContractEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.CurrentResidentAtAddressEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.FamilyHomeOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.LivingSituationOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreviousOrIntendedResidentAtAddressEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SpecialMeasuresEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    private Situation situation;
    private Situation situation1;
    private BeforeStart beforeStart;
    private ApplicantsDetails applicantsDetails;
    private RespondentDetails respondentDetails;
    private CourtNavRelationShipToRespondent relationShipToRespondent;
    private Family family;
    private TheHome home;
    private TheHome home1;
    private CourtNavRespondentBehaviour respondentBehaviour;
    private CourtNavStmtOfTruth stmtOfTruth;
    private GoingToCourt goingToCourt;
    private CourtNavMetaData courtNavMetaData;
    @Mock
    private LaunchDarklyClient launchDarklyClient;
    @Mock
    private LocationRefDataService locationRefDataService;

    @Before
    public void setUp() {

        court = Court.builder()
            .courtName("testcourt")
            .build();

        beforeStart = BeforeStart.builder()
            .applicantHowOld(ApplicantAge.eighteenOrOlder)
            .build();

        List<FL401OrderTypeEnum> fl401OrderTypeEnum = new ArrayList<>();

        fl401OrderTypeEnum.add(FL401OrderTypeEnum.nonMolestationOrder);

        List<FL401OrderTypeEnum> fl401OrderTypeEnum1 = new ArrayList<>();

        fl401OrderTypeEnum1.add(FL401OrderTypeEnum.occupationOrder);

        List<WithoutNoticeReasonEnum> withoutNoticeReasonEnum = new ArrayList<>();

        withoutNoticeReasonEnum.add(WithoutNoticeReasonEnum.riskOfSignificantHarm);

        situation = Situation.builder()
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

        situation1 = Situation.builder()
            .ordersAppliedFor(fl401OrderTypeEnum1)
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

        applicantsDetails = ApplicantsDetails.builder()
            .applicantFirstName("courtnav Applicant")
            .applicantLastName("test")
            .applicantDateOfBirth(CourtNavDate.builder()
                                      .day(10)
                                      .month(9)
                                      .year(1992)
                                      .build())
            .applicantGender(ApplicantGenderEnum.female)
            .shareContactDetailsWithRespondent(false)
            .applicantEmailAddress("test@courtNav.com")
            .applicantPhoneNumber("12345678907")
            .applicantHasLegalRepresentative(false)
            .applicantAddress(CourtnavAddress.builder()
                                  .addressLine1("55 Test Street")
                                  .postTown("Town")
                                  .postCode("LU1 5ET")
                                  .build())
            .build();

        respondentDetails = RespondentDetails.builder()
            .respondentFirstName("resp test")
            .respondentLastName("fl401")
            .respondentDateOfBirth(CourtNavDate.builder()
                                       .day(10)
                                       .month(9)
                                       .year(1989)
                                       .build())
            .respondentEmailAddress("test@resp.com")
            .respondentAddress(CourtnavAddress.builder()
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

        List<ProtectedChild> protectedChildren = new ArrayList<>();

        ProtectedChild child = ProtectedChild.builder()
            .fullName("child1")
            .dateOfBirth(CourtNavDate.builder()
                             .day(10)
                             .month(9)
                             .year(2016)
                             .build())
            .parentalResponsibility(true)
            .relationship("mother")
            .respondentRelationship("uncle")
            .build();
        protectedChildren.add(child);

        family = Family.builder()
            .whoApplicationIsFor(ApplicationCoverEnum.applicantAndChildren)
            .protectedChildren(protectedChildren)
            .anyOngoingCourtProceedings(false)
            .ongoingCourtProceedings(courtProceedings)
            .build();

        relationShipToRespondent = CourtNavRelationShipToRespondent.builder()
            .relationshipDescription(ApplicantRelationshipDescriptionEnum.formerlyMarriedOrCivil)
            .ceremonyDate(CourtNavDate.builder()
                              .day(10)
                              .month(9)
                              .year(1999)
                              .build())
            .relationshipEndDate(null)
            .relationshipStartDate(CourtNavDate.builder()
                                       .day(10)
                                       .month(9)
                                       .year(1998)
                                       .build())
            .respondentsRelationshipToApplicant(null)
            .respondentsRelationshipToApplicantOther(null)
            .anyChildren(false)
            .build();

        List<BehaviourTowardsApplicantEnum> behaviourTowardsApplicantEnum = new ArrayList<>();
        behaviourTowardsApplicantEnum.add(BehaviourTowardsApplicantEnum.comingNearHome);

        List<BehaviourTowardsChildrenEnum> behaviourTowardsChildrenEnum = new ArrayList<>();
        behaviourTowardsChildrenEnum.add(BehaviourTowardsChildrenEnum.beingViolentOrThreatening);

        respondentBehaviour = CourtNavRespondentBehaviour.builder()
            .applyingForMonMolestationOrder(true)
            .stopBehaviourAnythingElse("abc")
            .stopBehaviourTowardsApplicant(behaviourTowardsApplicantEnum)
            .stopBehaviourTowardsChildren(behaviourTowardsChildrenEnum)
            .build();

        home = TheHome.builder()
            .applyingForOccupationOrder(false)
            .build();

        List<CurrentResidentAtAddressEnum> currentResidentAtAddressEnum = new ArrayList<>();
        currentResidentAtAddressEnum.add(CurrentResidentAtAddressEnum.other);

        ChildAtAddress childAtAddress = ChildAtAddress.builder()
            .age(3)
            .fullName("test child")
            .build();

        List<ContractEnum> contractEnum = new ArrayList<>();
        contractEnum.add(ContractEnum.other);

        List<ChildAtAddress> children = new ArrayList<>();
        children.add(childAtAddress);
        List<LivingSituationOutcomeEnum> livingSituationOutcomeEnum = new ArrayList<>();
        livingSituationOutcomeEnum.add(LivingSituationOutcomeEnum.stayInHome);

        ChildAtAddress childAtAddress1 = ChildAtAddress.builder()
            .age(5)
            .fullName("test child")
            .build();

        List<ChildAtAddress> children1 = new ArrayList<>();
        children.add(childAtAddress1);

        List<FamilyHomeOutcomeEnum> familyHomeOutcomeEnum = new ArrayList<>();
        familyHomeOutcomeEnum.add(FamilyHomeOutcomeEnum.respondentToPayRentMortgage);

        home1 = TheHome.builder()
            .applyingForOccupationOrder(true)
            .occupationOrderAddress(CourtnavAddress.builder()
                                        .addressLine1("55 Test Street")
                                        .postTown("Town")
                                        .postCode("N12 3BH")
                                        .build())
            .currentlyLivesAtAddress(currentResidentAtAddressEnum)
            .currentlyLivesAtAddressOther("test")
            .previouslyLivedAtAddress(PreviousOrIntendedResidentAtAddressEnum.applicant)
            .intendedToLiveAtAddress(PreviousOrIntendedResidentAtAddressEnum.applicant)
            .childrenApplicantResponsibility(children)
            .childrenSharedResponsibility(children1)
            .propertySpeciallyAdapted(true)
            .propertyHasMortgage(true)
            .namedOnMortgage(contractEnum)
            .namedOnMortgageOther("test")
            .mortgageNumber("2345678")
            .mortgageLenderName("test mort")
            .mortgageLenderAddress(CourtnavAddress.builder()
                                       .addressLine1("ABC").postCode("AB1 2MN").build())
            .propertyIsRented(true)
            .namedOnRentalAgreement(contractEnum)
            .namedOnRentalAgreementOther("test")
            .landlordName("landlord")
            .landlordAddress(CourtnavAddress.builder()
                                 .addressLine1("ABC").postCode("AB1 2MN").build())
            .haveHomeRights(true)
            .wantToHappenWithLivingSituation(livingSituationOutcomeEnum)
            .wantToHappenWithFamilyHome(familyHomeOutcomeEnum)
            .anythingElseForCourtToConsider("test court details")
            .build();

        List<ConsentEnum> consentEnum = new ArrayList<>();
        consentEnum.add(ConsentEnum.applicantConfirm);

        stmtOfTruth = CourtNavStmtOfTruth.builder()
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

        goingToCourt = GoingToCourt.builder()
            .isInterpreterRequired(false)
            .interpreterLanguage(null)
            .interpreterDialect(null)
            .anyDisabilityNeeds(false)
            .disabilityNeedsDetails(null)
            .anySpecialMeasures(specialMeasuresEnum)
            .build();

        courtNavMetaData = CourtNavMetaData.builder()
            .courtNavApproved(true)
            .caseOrigin("courtnav")
            .numberOfAttachments(4)
            .courtSpecialRequirements("test special court")
            .hasDraftOrder(false)
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


        CaseData caseData = CaseData.builder()
            .applicantAge(ApplicantAge.eighteenOrOlder)
            .build();

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(BeforeStart.builder()
                                        .applicantHowOld(ApplicantAge.eighteenOrOlder)
                                        .build())
                       .build())
            .build();

        assertEquals(courtNavFl401.getFl401().getBeforeStart().getApplicantHowOld(), caseData.getApplicantAge());
        assertNotNull(courtNavFl401.getFl401().getBeforeStart().getApplicantHowOld());

    }

    @Test
    public void testCourtnavCaseDataWithCourtNavFL401Details() throws NotFoundException {

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
        String email = "dacourt@test.com";
        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();
        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        assertEquals(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor(), caseData.getTypeOfApplicationOrders().getOrderType());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedWithoutNoticeReason());

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

    }

    @Test
    public void testCourtnavCaseDataWithCourtNavFL401DetailsWithOccupationalOrder() throws NotFoundException {

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(beforeStart)
                       .situation(situation1)
                       .applicantDetails(applicantsDetails)
                       .respondentDetails(respondentDetails)
                       .family(family)
                       .relationshipWithRespondent(relationShipToRespondent)
                       .respondentBehaviour(respondentBehaviour)
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String email = "dacourt@test.com";
        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();
        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        assertEquals(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor(), caseData.getTypeOfApplicationOrders().getOrderType());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedWithoutNoticeReason());

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

    }

    @Test
    public void testCourtnavMetaDataCourtnavApprovedAsFalse() throws NotFoundException {

        courtNavMetaData = CourtNavMetaData.builder()
            .courtNavApproved(false)
            .caseOrigin("courtnav")
            .numberOfAttachments(4)
            .hasDraftOrder(true)
            .courtSpecialRequirements("test")
            .build();

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(beforeStart)
                       .situation(situation1)
                       .applicantDetails(applicantsDetails)
                       .respondentDetails(respondentDetails)
                       .family(family)
                       .relationshipWithRespondent(relationShipToRespondent)
                       .respondentBehaviour(respondentBehaviour)
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CaseData caseData = CaseData.builder()
            .caseOrigin("courtnav")
            .courtNavApproved(YesOrNo.No)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertFalse(courtNavFl401.getMetaData().isCourtNavApproved());
        assertTrue(courtNavFl401.getMetaData().isHasDraftOrder());
        assertNotNull(courtNavFl401.getMetaData().getCaseOrigin());

    }

    @Test
    public void testCourtnavOrdersAppliedWithoutNoticeAsFalse() throws NotFoundException {

        situation = situation.toBuilder()
            .ordersAppliedWithoutNotice(false)
            .bailConditionsOnRespondent(false)
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
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CaseData caseData = CaseData.builder()
            .caseOrigin("courtnav")
            .courtNavApproved(YesOrNo.No)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertFalse(courtNavFl401.getFl401().getSituation().isOrdersAppliedWithoutNotice());
        assertFalse(courtNavFl401.getFl401().getSituation().isBailConditionsOnRespondent());

    }

    @Test
    public void testCourtnavFamilyAsApplicant() throws NotFoundException {

        family = Family.builder()
            .whoApplicationIsFor(ApplicationCoverEnum.applicantOnly)
            .protectedChildren(null)
            .anyOngoingCourtProceedings(false)
            .ongoingCourtProceedings(null)
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
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CaseData caseData = CaseData.builder()
            .caseOrigin("courtnav")
            .courtNavApproved(YesOrNo.No)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertEquals("Applicant Only", courtNavFl401.getFl401().getFamily().getWhoApplicationIsFor().getDisplayedValue());

    }

    @Test
    public void testCourtnavRelationShipDescriptionAsNoneOfTheAbove() throws NotFoundException {

        relationShipToRespondent = CourtNavRelationShipToRespondent.builder()
            .relationshipDescription(ApplicantRelationshipDescriptionEnum.noneOfAbove)
            .ceremonyDate(null)
            .relationshipEndDate(null)
            .relationshipStartDate(null)
            .respondentsRelationshipToApplicant(ApplicantRelationshipOptionsEnum.cousin)
            .respondentsRelationshipToApplicantOther(null)
            .anyChildren(false)
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
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CaseData caseData = CaseData.builder()
            .caseOrigin("courtnav")
            .courtNavApproved(YesOrNo.No)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertEquals(
            "None of the above",
            courtNavFl401.getFl401().getRelationshipWithRespondent().getRelationshipDescription()
                .getDisplayedValue());

    }

    @Test
    public void testCourtnavGoingToCourtInterpreterNeeds() throws NotFoundException {

        List<SpecialMeasuresEnum> specialMeasuresEnum = new ArrayList<>();
        specialMeasuresEnum.add(SpecialMeasuresEnum.separateWaitingRoom);

        goingToCourt = GoingToCourt.builder()
            .isInterpreterRequired(true)
            .interpreterLanguage("test")
            .interpreterDialect("test")
            .anyDisabilityNeeds(true)
            .disabilityNeedsDetails("test")
            .anySpecialMeasures(specialMeasuresEnum)
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
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CaseData caseData = CaseData.builder()
            .caseOrigin("courtnav")
            .courtNavApproved(YesOrNo.No)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertEquals(YesOrNo.Yes, caseData1.getAttendHearing().getIsInterpreterNeeded());
        assertEquals(YesOrNo.Yes, caseData1.getAttendHearing().getIsDisabilityPresent());
    }

    @Test
    public void testCourtnavGoingToCourtWithNoSpecialMeasures() throws NotFoundException {

        List<SpecialMeasuresEnum> specialMeasuresEnum = new ArrayList<>();
        specialMeasuresEnum.add(SpecialMeasuresEnum.separateWaitingRoom);

        goingToCourt = GoingToCourt.builder()
            .isInterpreterRequired(true)
            .interpreterLanguage("test")
            .interpreterDialect("test")
            .anyDisabilityNeeds(true)
            .disabilityNeedsDetails("test")
            .anySpecialMeasures(null)
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
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CaseData caseData = CaseData.builder()
            .caseOrigin("courtnav")
            .courtNavApproved(YesOrNo.No)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertEquals(YesOrNo.Yes, caseData1.getAttendHearing().getIsInterpreterNeeded());
        assertEquals(YesOrNo.Yes, caseData1.getAttendHearing().getIsDisabilityPresent());
        assertNull(courtNavFl401.getFl401().getGoingToCourt().getAnySpecialMeasures());
        assertNull(caseData1.getAttendHearing().getSpecialArrangementsRequired());
    }

    @Test
    public void testCourtnavOngoingCourtProceedings() throws NotFoundException {

        List<CourtProceedings> courtProceedings = new ArrayList<>();
        courtProceedings.add(CourtProceedings.builder()
                                 .caseDetails("testcase1")
                                 .caseNumber("1234567")
                                 .caseType("testType1")
                                 .nameOfCourt("testcourt1")
                                 .build());

        List<ProtectedChild> protectedChildren = new ArrayList<>();

        ProtectedChild child = ProtectedChild.builder()
            .fullName("child1")
            .dateOfBirth(CourtNavDate.builder()
                             .day(10)
                             .month(9)
                             .year(2016)
                             .build())
            .parentalResponsibility(true)
            .relationship("mother")
            .respondentRelationship("uncle")
            .build();
        protectedChildren.add(child);

        family = Family.builder()
            .whoApplicationIsFor(ApplicationCoverEnum.applicantAndChildren)
            .protectedChildren(protectedChildren)
            .anyOngoingCourtProceedings(true)
            .ongoingCourtProceedings(courtProceedings)
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
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CaseData caseData = CaseData.builder()
            .caseOrigin("courtnav")
            .courtNavApproved(YesOrNo.No)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertEquals(
            YesNoDontKnow.yes,
            caseData1.getFl401OtherProceedingDetails().getHasPrevOrOngoingOtherProceeding()
        );
        assertNotNull(caseData1.getFl401OtherProceedingDetails().getFl401OtherProceedings());

    }

    @Test
    public void testCourtnavHomeChildrenIsNull() throws NotFoundException {

        home1 = home1.toBuilder()
            .childrenApplicantResponsibility(null)
            .childrenSharedResponsibility(null)
            .build();

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(beforeStart)
                       .situation(situation1)
                       .applicantDetails(applicantsDetails)
                       .respondentDetails(respondentDetails)
                       .family(family)
                       .relationshipWithRespondent(relationShipToRespondent)
                       .respondentBehaviour(respondentBehaviour)
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CaseData caseData = CaseData.builder()
            .caseOrigin("courtnav")
            .courtNavApproved(YesOrNo.No)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertEquals(YesOrNo.No, caseData1.getHome().getDoAnyChildrenLiveAtAddress());

    }

    @Test
    public void testCourtnavHomePreviouslyLivedAtThisAddressAsNeither() throws NotFoundException {

        home1 = home1.toBuilder()
            .previouslyLivedAtAddress(null)
            .intendedToLiveAtAddress(null)
            .build();

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(beforeStart)
                       .situation(situation1)
                       .applicantDetails(applicantsDetails)
                       .respondentDetails(respondentDetails)
                       .family(family)
                       .relationshipWithRespondent(relationShipToRespondent)
                       .respondentBehaviour(respondentBehaviour)
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CaseData caseData = CaseData.builder()
            .caseOrigin("courtnav")
            .courtNavApproved(YesOrNo.No)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertNull(caseData1.getHome().getEverLivedAtTheAddress());
        assertNull(caseData1.getHome().getIntendToLiveAtTheAddress());

    }

    @Test
    public void testCourtnavHomeLivingStituationAsNull() throws NotFoundException {

        home1 = home1.toBuilder()
            .wantToHappenWithLivingSituation(null)
            .wantToHappenWithFamilyHome(null)
            .build();

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(beforeStart)
                       .situation(situation1)
                       .applicantDetails(applicantsDetails)
                       .respondentDetails(respondentDetails)
                       .family(family)
                       .relationshipWithRespondent(relationShipToRespondent)
                       .respondentBehaviour(respondentBehaviour)
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CaseData caseData = CaseData.builder()
            .caseOrigin("courtnav")
            .courtNavApproved(YesOrNo.No)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertNull(caseData1.getHome().getLivingSituation());
        assertNull(caseData1.getHome().getFamilyHome());

    }

    @Test
    public void testCourtnavHomeNameOnRentalAgreementAsNull() throws NotFoundException {

        home1 = home1.toBuilder()
            .propertyIsRented(false)
            .namedOnRentalAgreement(null)
            .propertyHasMortgage(false)
            .namedOnMortgage(null)
            .build();

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(beforeStart)
                       .situation(situation1)
                       .applicantDetails(applicantsDetails)
                       .respondentDetails(respondentDetails)
                       .family(family)
                       .relationshipWithRespondent(relationShipToRespondent)
                       .respondentBehaviour(respondentBehaviour)
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertNull(caseData1.getHome().getLandlords());
        assertNull(caseData1.getHome().getMortgages());

    }

    @Test
    public void testCourtnavHomeMortagageAndRentDetailsAsFalse() throws NotFoundException {

        home1 = home1.toBuilder()
            .propertyIsRented(false)
            .propertyHasMortgage(false)
            .haveHomeRights(false)
            .propertySpeciallyAdapted(false)
            .build();

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(beforeStart)
                       .situation(situation1)
                       .applicantDetails(applicantsDetails)
                       .respondentDetails(respondentDetails)
                       .family(family)
                       .relationshipWithRespondent(relationShipToRespondent)
                       .respondentBehaviour(respondentBehaviour)
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertEquals(YesOrNo.No, caseData1.getHome().getIsPropertyRented());
        assertNull(caseData1.getHome().getHowIsThePropertyAdapted());
        assertEquals(YesOrNo.No, caseData1.getHome().getIsThereMortgageOnProperty());
        assertNull(caseData1.getHome().getMortgages());
        assertEquals(YesOrNo.No, caseData1.getHome().getIsPropertyAdapted());
        assertEquals(YesOrNo.No, caseData1.getHome().getDoesApplicantHaveHomeRights());
        assertNull(caseData1.getHome().getLandlords());

    }

    @Test
    public void testCourtnavApplicantDetailsHasNoConfidentialInfo() throws NotFoundException {

        applicantsDetails = applicantsDetails.toBuilder()
            .applicantGender(ApplicantGenderEnum.female)
            .shareContactDetailsWithRespondent(true)
            .applicantAddress(CourtnavAddress.builder()
                                  .addressLine1("55 Test Street")
                                  .postTown("Town")
                                  .postCode("LU1 5ET")
                                  .build())
            .build();

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(beforeStart)
                       .situation(situation1)
                       .applicantDetails(applicantsDetails)
                       .respondentDetails(respondentDetails)
                       .family(family)
                       .relationshipWithRespondent(relationShipToRespondent)
                       .respondentBehaviour(respondentBehaviour)
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertEquals(YesOrNo.No, caseData1.getApplicantsFL401().getIsAddressConfidential());
        assertEquals(YesOrNo.No, caseData1.getApplicantsFL401().getIsEmailAddressConfidential());
        assertEquals(YesOrNo.No, caseData1.getApplicantsFL401().getIsPhoneNumberConfidential());

    }

    @Test
    public void testCourtnavRespondentDetailsHasNullInfo() throws NotFoundException {

        respondentDetails = RespondentDetails.builder()
            .respondentFirstName("resp test")
            .respondentLastName("fl401")
            .respondentDateOfBirth(null)
            .respondentEmailAddress(null)
            .respondentAddress(null)
            .respondentLivesWithApplicant(false)
            .respondentPhoneNumber(null)
            .build();

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(beforeStart)
                       .situation(situation1)
                       .applicantDetails(applicantsDetails)
                       .respondentDetails(respondentDetails)
                       .family(family)
                       .relationshipWithRespondent(relationShipToRespondent)
                       .respondentBehaviour(respondentBehaviour)
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertEquals(YesOrNo.No, caseData1.getRespondentsFL401().getCanYouProvideEmailAddress());
        assertEquals(YesOrNo.No, caseData1.getRespondentsFL401().getCanYouProvidePhoneNumber());
        assertEquals(YesOrNo.No, caseData1.getRespondentsFL401().getIsCurrentAddressKnown());

    }

    @Test
    public void testCourtnavRelationShiptoRespondentHasRelationEndDate() throws NotFoundException {

        relationShipToRespondent = CourtNavRelationShipToRespondent.builder()
            .relationshipDescription(ApplicantRelationshipDescriptionEnum.formerlyMarriedOrCivil)
            .ceremonyDate(CourtNavDate.builder()
                              .day(10)
                              .month(9)
                              .year(1999)
                              .build())
            .relationshipEndDate(CourtNavDate.builder()
                                     .day(10)
                                     .month(9)
                                     .year(2011)
                                     .build())
            .relationshipStartDate(CourtNavDate.builder()
                                       .day(10)
                                       .month(9)
                                       .year(1998)
                                       .build())
            .respondentsRelationshipToApplicant(null)
            .respondentsRelationshipToApplicantOther(null)
            .anyChildren(false)
            .build();

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(beforeStart)
                       .situation(situation1)
                       .applicantDetails(applicantsDetails)
                       .respondentDetails(respondentDetails)
                       .family(family)
                       .relationshipWithRespondent(relationShipToRespondent)
                       .respondentBehaviour(respondentBehaviour)
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertEquals(
            LocalDate.parse(courtNavFl401.getFl401().getRelationshipWithRespondent().getRelationshipEndDate().mergeDate()),
            caseData1.getRespondentRelationDateInfoObject().getRelationStartAndEndComplexType().getRelationshipDateComplexEndDate());

    }

    @Test
    public void testCourtNavFamilyParentalResponsibilityAsFalse() throws NotFoundException {

        List<ProtectedChild> protectedChildren = new ArrayList<>();

        ProtectedChild child = ProtectedChild.builder()
            .fullName("child1")
            .dateOfBirth(CourtNavDate.builder()
                             .day(10)
                             .month(9)
                             .year(2016)
                             .build())
            .parentalResponsibility(false)
            .relationship("mother")
            .respondentRelationship("uncle")
            .build();
        protectedChildren.add(child);

        family = Family.builder()
            .whoApplicationIsFor(ApplicationCoverEnum.applicantAndChildren)
            .protectedChildren(protectedChildren)
            .anyOngoingCourtProceedings(false)
            .ongoingCourtProceedings(null)
            .build();

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(beforeStart)
                       .situation(situation1)
                       .applicantDetails(applicantsDetails)
                       .respondentDetails(respondentDetails)
                       .family(family)
                       .relationshipWithRespondent(relationShipToRespondent)
                       .respondentBehaviour(respondentBehaviour)
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));


        assertNotNull(caseData1.getApplicantChildDetails());
    }

    @Test
    public void testCourtnavRespondentBehaviourTowardsApplicantAsNull() throws NotFoundException {

        List<BehaviourTowardsChildrenEnum> behaviourTowardsChildrenEnum = new ArrayList<>();
        behaviourTowardsChildrenEnum.add(BehaviourTowardsChildrenEnum.beingViolentOrThreatening);

        respondentBehaviour = CourtNavRespondentBehaviour.builder()
            .applyingForMonMolestationOrder(true)
            .stopBehaviourAnythingElse("abc")
            .stopBehaviourTowardsApplicant(null)
            .stopBehaviourTowardsChildren(behaviourTowardsChildrenEnum)
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
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");
        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertNull(courtNavFl401.getFl401().getRespondentBehaviour().getStopBehaviourTowardsApplicant());
        assertNull(caseData1.getRespondentBehaviourData().getApplicantWantToStopFromRespondentDoing());

    }

    @Test
    public void testCourtNavRespondentBehaviourTowardsChildrenAsNull() throws NotFoundException {

        List<BehaviourTowardsApplicantEnum> behaviourTowardsApplicantEnum = new ArrayList<>();
        behaviourTowardsApplicantEnum.add(BehaviourTowardsApplicantEnum.comingNearHome);

        respondentBehaviour = CourtNavRespondentBehaviour.builder()
            .applyingForMonMolestationOrder(true)
            .stopBehaviourAnythingElse("abc")
            .stopBehaviourTowardsApplicant(behaviourTowardsApplicantEnum)
            .stopBehaviourTowardsChildren(null)
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
                       .theHome(home1)
                       .statementOfTruth(stmtOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();

        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();

        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));

        assertNull(courtNavFl401.getFl401().getRespondentBehaviour().getStopBehaviourTowardsChildren());
        assertNull(caseData1.getRespondentBehaviourData().getApplicantWantToStopFromRespondentDoingToChild());

    }

    @Test
    public void testCourtNavCaseDataWhenPopulateDefaultCaseFlagIsOn() throws NotFoundException {

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
        when(launchDarklyClient.isFeatureEnabled(anyString())).thenReturn(true);
        when(locationRefDataService.getCourtDetailsFromEpimmsId("234946","Bearer:test"))
            .thenReturn(Optional.of(CourtVenue.builder()
                                        .courtName("Swansea Family court")
                                        .region("Wales")
                                        .regionId("7")
                                        .build()));
        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        assertEquals(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor(), caseData.getTypeOfApplicationOrders().getOrderType());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedWithoutNoticeReason());
        assertEquals("Swansea Family court", caseData.getCourtName());
        assertEquals("234946", caseData.getCourtId());

    }

    @Test
    public void testCourtNavCaseDataWhenCourtDetailFoundForEpmsId() throws NotFoundException {

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
        when(launchDarklyClient.isFeatureEnabled(anyString())).thenReturn(false);
        when(locationRefDataService.getCourtDetailsFromEpimmsId(anyString(),anyString()))
            .thenReturn(Optional.of(CourtVenue.builder()
                                        .courtName("Swansea Family court")
                                        .region("Wales")
                                        .regionId("7")
                                        .build()));
        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        assertEquals(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor(), caseData.getTypeOfApplicationOrders().getOrderType());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedWithoutNoticeReason());
        assertEquals("Swansea Family court", caseData.getCourtName());
    }

    @Test
    public void testCourtNavCaseDataWhenCourtDetailFoundForEpmsIdAndFlagIsOn() throws NotFoundException {

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
        when(launchDarklyClient.isFeatureEnabled(anyString())).thenReturn(true);
        when(locationRefDataService.getCourtDetailsFromEpimmsId(anyString(),anyString()))
            .thenReturn(Optional.of(CourtVenue.builder()
                                        .courtName("Swansea Family court")
                                        .region("Wales")
                                        .regionId("7")
                                        .build()));
        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        assertEquals(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor(), caseData.getTypeOfApplicationOrders().getOrderType());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedWithoutNoticeReason());
        assertEquals("Swansea Family court", caseData.getCourtName());
    }

    @Test
    public void testCourtNavCaseDataWhenPopulateDefaultCaseFlagIsOff() throws NotFoundException {

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
            .metaData(courtNavMetaData.toBuilder()
                          .courtSpecialRequirements(null)
                          .build())
            .build();
        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("test court address")
            .description("court desc")
            .explanation("court explanation")
            .build();
        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(courtEmailAddress));
        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401,"Bearer:test");

        assertEquals(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor(), caseData.getTypeOfApplicationOrders().getOrderType());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedWithoutNoticeReason());
        verify(courtFinderService, times(1)).getNearestFamilyCourt(Mockito.any(CaseData.class));
    }
}
