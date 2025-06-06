package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ApplicantsDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.BeforeStart;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ChildAtAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavHome;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavMetaData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavRelationShipToRespondent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavRespondent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavRespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavStatementOfTruth;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.Family;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.GoingToCourt;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ProtectedChild;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.Situation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ContractEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreferredContactEnum;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum.noneOfTheAbove;
import static uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum.cousin;
import static uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_8;
import static uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum.nonMolestationOrder;
import static uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum.occupationOrder;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.ReasonForOrderWithoutGivingNoticeEnum.harmToApplicantOrChild;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge.eighteenOrOlder;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantRelationshipDescriptionEnum.formerlyMarriedOrCivil;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantRelationshipDescriptionEnum.noneOfAbove;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicationCoverEnum.applicantAndChildren;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicationCoverEnum.applicantOnly;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsApplicantEnum.comingNearHome;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum.beingViolentOrThreatening;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ConsentEnum.applicantConfirm;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.CurrentResidentAtAddressEnum.other;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.FamilyHomeOutcomeEnum.respondentToPayRentMortgage;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.LivingSituationOutcomeEnum.stayInHome;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreviousOrIntendedResidentAtAddressEnum.applicant;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SpecialMeasuresEnum.separateWaitingRoom;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum.riskOfSignificantHarm;

@SpringBootTest
class FL401ApplicationMapperTest {

    @Autowired
    private FL401ApplicationMapper fl401ApplicationMapper;

    private CourtNavFl401 courtNavFl401;
    private Situation situationWithOnlyNonMolestationOrder;
    private Situation situationWithOnlyOccupationOrder;
    private ApplicantsDetails applicantsDetails;
    private CourtNavHome home;

    @BeforeEach
    void setUp() {
        CourtNavAddress applicantAddress = buildApplicantAddress();
        situationWithOnlyNonMolestationOrder = buildSituation(List.of(nonMolestationOrder));
        situationWithOnlyOccupationOrder = buildSituation(List.of(occupationOrder));
        applicantsDetails = buildApplicantsDetails(applicantAddress);
        home = buildCourtNavHome(applicantAddress);
        courtNavFl401 = buildCourtNavFl401(applicantAddress);
    }

    @Test
    void shouldMapSituationToOrderTypesAndWithoutNoticeReasonDetails() {
        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyNonMolestationOrder)
                       .courtNavHome(CourtNavHome.builder().applyingForOccupationOrder(false).build())
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(
            List.of(nonMolestationOrder),
            caseData.getTypeOfApplicationOrders().getOrderType()
        );
        assertEquals(
            List.of(harmToApplicantOrChild),
            caseData.getReasonForOrderWithoutGivingNotice().getReasonForOrderWithoutGivingNotice()
        );
        assertEquals(
            "test1",
            caseData.getReasonForOrderWithoutGivingNotice().getFutherDetails()
        );
    }

    @Test
    void shouldMapOccupationOrderAndPreserveWithoutNoticeReason() {
        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyOccupationOrder)
                       .courtNavHome(home)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(List.of(FL401OrderTypeEnum.occupationOrder),
                     caseData.getTypeOfApplicationOrders().getOrderType());
        assertEquals(
            List.of(harmToApplicantOrChild),
            caseData.getReasonForOrderWithoutGivingNotice().getReasonForOrderWithoutGivingNotice()
        );
    }

    @Test
    void shouldMapMetadataFieldsWhenCourtNavApprovedIsFalse() {
        CourtNavMetaData overriddenMetaData = CourtNavMetaData.builder()
            .courtNavApproved(false)
            .caseOrigin("courtnav")
            .numberOfAttachments(4)
            .hasDraftOrder(true)
            .courtSpecialRequirements("test")
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyOccupationOrder)
                       .courtNavHome(home)
                       .build())
            .metaData(overriddenMetaData)
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(No, caseData.getCourtNavApproved());
        assertEquals(Yes, caseData.getHasDraftOrder());
        assertEquals("courtnav", caseData.getCaseOrigin());
        assertEquals("test", caseData.getSpecialCourtName());
        assertEquals("4", caseData.getNumberOfAttachments());
    }

    @Test
    void shouldMapOrdersAppliedWithoutNoticeAndBailConditionFlagsAsFalse() {
        Situation updatedSituation = situationWithOnlyNonMolestationOrder.toBuilder()
            .ordersAppliedWithoutNotice(false)
            .bailConditionsOnRespondent(false)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(updatedSituation)
                       .courtNavHome(home)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertNull(caseData.getReasonForOrderWithoutGivingNotice());
        assertEquals(YesNoDontKnow.no, caseData.getBailDetails().getIsRespondentAlreadyInBailCondition());
        assertNull(caseData.getBailDetails().getBailConditionEndDate());
    }

    @Test
    void shouldMapFamilyAsApplicantOnlyWithNoChildrenOrOngoingProceedings() {
        Family updatedFamily = Family.builder()
            .whoApplicationIsFor(applicantOnly)
            .protectedChildren(null)
            .anyOngoingCourtProceedings(false)
            .ongoingCourtProceedings(null)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .family(updatedFamily)
                       .courtNavHome(home)
                       .situation(situationWithOnlyNonMolestationOrder)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(No, caseData.getApplicantFamilyDetails().getDoesApplicantHaveChildren());
        assertNull(caseData.getApplicantChildDetails());
        assertEquals(YesNoDontKnow.no, caseData.getFl401OtherProceedingDetails().getHasPrevOrOngoingOtherProceeding());
        assertNull(caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings());
    }

    @Test
    void shouldMapRelationshipDescriptionAsNoneOfTheAbove() {
        CourtNavRelationShipToRespondent updatedRelationship = CourtNavRelationShipToRespondent.builder()
            .relationshipDescription(noneOfAbove)
            .ceremonyDate(null)
            .relationshipEndDate(null)
            .relationshipStartDate(null)
            .respondentsRelationshipToApplicant(cousin)
            .respondentsRelationshipToApplicantOther(null)
            .anyChildren(false)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .relationshipWithRespondent(updatedRelationship)
                       .courtNavHome(home)
                       .situation(situationWithOnlyNonMolestationOrder)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(
            noneOfTheAbove,
            caseData.getRespondentRelationObject().getApplicantRelationship()
        );

        assertEquals(
            cousin,
            caseData.getRespondentRelationOptions().getApplicantRelationshipOptions()
        );

        assertNull(caseData.getRespondentRelationDateInfoObject());
    }

    @Test
    void shouldMapInterpreterAndDisabilityNeedsFromGoingToCourt() {
        GoingToCourt customGoingToCourt = GoingToCourt.builder()
            .isInterpreterRequired(true)
            .interpreterLanguage("language")
            .interpreterDialect("dialect")
            .anyDisabilityNeeds(true)
            .disabilityNeedsDetails("disability details")
            .anySpecialMeasures(List.of(separateWaitingRoom))
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyNonMolestationOrder)
                       .courtNavHome(home)
                       .goingToCourt(customGoingToCourt)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        AttendHearing hearing = caseData.getAttendHearing();

        assertEquals(Yes, hearing.getIsInterpreterNeeded());
        assertEquals(Yes, hearing.getIsDisabilityPresent());
        assertEquals("disability details", hearing.getAdjustmentsRequired());

        assertEquals(1, hearing.getInterpreterNeeds().size());
        assertEquals("language - dialect", hearing.getInterpreterNeeds().getFirst().getValue().getLanguage());

        assertEquals("A separate waiting room in the court building", hearing.getSpecialArrangementsRequired());
    }

    @Test
    void shouldMapOngoingCourtProceedingsWhenPresent() {
        List<CourtProceedings> courtProceedings = List.of(
            CourtProceedings.builder()
                .caseDetails("testcase1")
                .caseNumber("1234567")
                .caseType("testType1")
                .nameOfCourt("testcourt1")
                .build()
        );

        ProtectedChild child = ProtectedChild.builder()
            .fullName("child1")
            .dateOfBirth(new CourtNavDate(10, 9, 2016))
            .parentalResponsibility(true)
            .relationship("mother")
            .respondentRelationship("uncle")
            .build();

        Family family = Family.builder()
            .whoApplicationIsFor(applicantAndChildren)
            .protectedChildren(List.of(child))
            .anyOngoingCourtProceedings(true)
            .ongoingCourtProceedings(courtProceedings)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .family(family)
                       .courtNavHome(home)
                       .situation(situationWithOnlyNonMolestationOrder)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(YesNoDontKnow.yes,
                     caseData.getFl401OtherProceedingDetails().getHasPrevOrOngoingOtherProceeding());

        assertThat(caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings())
            .hasSize(1);

        FL401Proceedings proceeding = caseData.getFl401OtherProceedingDetails()
            .getFl401OtherProceedings().getFirst().getValue();

        assertEquals("testcourt1", proceeding.getNameOfCourt());
        assertEquals("1234567", proceeding.getCaseNumber());
        assertEquals("testType1", proceeding.getTypeOfCase());
        assertEquals("testcase1", proceeding.getAnyOtherDetails());
    }

    @Test
    void shouldMapHomeDetailsCorrectlyWhenAllFlagsAreFalse() {
        CourtNavHome updatedHome = home.toBuilder()
            .propertyIsRented(false)
            .propertyHasMortgage(false)
            .haveHomeRights(false)
            .propertySpeciallyAdapted(false)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyOccupationOrder)
                       .courtNavHome(updatedHome)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(No, caseData.getHome().getIsPropertyRented());
        assertEquals(No, caseData.getHome().getIsThereMortgageOnProperty());
        assertEquals(No, caseData.getHome().getDoesApplicantHaveHomeRights());
        assertEquals(No, caseData.getHome().getIsPropertyAdapted());

        assertNull(caseData.getHome().getLandlords());
        assertNull(caseData.getHome().getMortgages());
        assertNull(caseData.getHome().getHowIsThePropertyAdapted());
    }

    @Test
    void shouldNotThrowWhenFamilyHomeListIsEmpty() {
        home = home.toBuilder()
            .wantToHappenWithFamilyHome(Collections.emptyList())
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyOccupationOrder)
                       .courtNavHome(home)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertNotNull(caseData.getHome().getLivingSituation());
    }

    @Test
    void shouldMarkApplicantContactDetailsAsNotConfidentialWhenShared() {
        applicantsDetails.setGender(female);
        applicantsDetails.setShareContactDetailsWithRespondent(true);
        applicantsDetails.setAddress(CourtNavAddress.builder()
                                         .addressLine1("55 Test Street")
                                         .postTown("Town")
                                         .postCode("LU1 5ET")
                                         .build());

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .applicantDetails(applicantsDetails)
                       .situation(situationWithOnlyOccupationOrder)
                       .courtNavHome(home)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(No, caseData.getApplicantsFL401().getIsAddressConfidential());
        assertEquals(No, caseData.getApplicantsFL401().getIsEmailAddressConfidential());
        assertEquals(No, caseData.getApplicantsFL401().getIsPhoneNumberConfidential());
    }

    @Test
    void shouldSetRespondentContactFlagsToNoWhenInfoIsMissing() {
        CourtNavRespondent courtNavRespondent = CourtNavRespondent.builder()
            .firstName("resp test")
            .lastName("fl401")
            .dateOfBirth(null)
            .email(null)
            .address(null)
            .respondentLivesWithApplicant(false)
            .phoneNumber(null)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .courtNavRespondent(courtNavRespondent)
                       .situation(situationWithOnlyOccupationOrder)
                       .courtNavHome(home)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(No, caseData.getRespondentsFL401().getCanYouProvideEmailAddress());
        assertEquals(No, caseData.getRespondentsFL401().getCanYouProvidePhoneNumber());
        assertEquals(No, caseData.getRespondentsFL401().getIsCurrentAddressKnown());

        assertNull(caseData.getRespondentsFL401().getEmail());
        assertNull(caseData.getRespondentsFL401().getPhoneNumber());
        assertNull(caseData.getRespondentsFL401().getAddress());
    }

    @Test
    void shouldMapRelationshipEndDateWhenPresent() {
        CourtNavRelationShipToRespondent relationShipToRespondent = CourtNavRelationShipToRespondent.builder()
            .relationshipDescription(formerlyMarriedOrCivil)
            .relationshipStartDate(new CourtNavDate(10, 9, 1998))
            .ceremonyDate(new CourtNavDate(10, 9, 1999))
            .relationshipEndDate(new CourtNavDate(10, 9, 2011))
            .respondentsRelationshipToApplicant(null)
            .respondentsRelationshipToApplicantOther(null)
            .anyChildren(false)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyOccupationOrder)
                       .relationshipWithRespondent(relationShipToRespondent)
                       .courtNavHome(home)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(
            LocalDate.parse(relationShipToRespondent.getRelationshipEndDate().mergeDate()),
            caseData.getRespondentRelationDateInfoObject()
                .getRelationStartAndEndComplexType()
                .getRelationshipDateComplexEndDate()
        );
    }

    @Test
    void testCourtnavFamilyParentalResponsibilityAsFalse() {
        ProtectedChild child = ProtectedChild.builder()
            .fullName("child1")
            .dateOfBirth(new CourtNavDate(10, 9, 2016))
            .parentalResponsibility(false)
            .relationship("mother")
            .respondentRelationship("uncle")
            .build();

        Family family = Family.builder()
            .whoApplicationIsFor(applicantAndChildren)
            .protectedChildren(List.of(child))
            .anyOngoingCourtProceedings(false)
            .ongoingCourtProceedings(null)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .family(family)
                       .situation(situationWithOnlyOccupationOrder)
                       .courtNavHome(home)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertNotNull(caseData.getApplicantChildDetails());
    }

    @Test
    void shouldNotMapBehaviourTowardsApplicantWhenNull() {
        CourtNavRespondentBehaviour respondentBehaviour = CourtNavRespondentBehaviour.builder()
            .applyingForMonMolestationOrder(true)
            .stopBehaviourAnythingElse("abc")
            .stopBehaviourTowardsApplicant(null)
            .stopBehaviourTowardsChildren(List.of(beingViolentOrThreatening))
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .respondentBehaviour(respondentBehaviour)
                       .courtNavHome(home)
                       .situation(situationWithOnlyNonMolestationOrder)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertNull(caseData.getRespondentBehaviourData().getApplicantWantToStopFromRespondentDoing());
        assertEquals(
            List.of(applicantStopFromRespondentDoingToChildEnum_Value_1),
            caseData.getRespondentBehaviourData().getApplicantWantToStopFromRespondentDoingToChild()
        );
    }

    @Test
    void shouldNotMapBehaviourTowardsChildrenWhenNull() {
        CourtNavRespondentBehaviour respondentBehaviour = CourtNavRespondentBehaviour.builder()
            .applyingForMonMolestationOrder(true)
            .stopBehaviourAnythingElse("abc")
            .stopBehaviourTowardsApplicant(List.of(comingNearHome))
            .stopBehaviourTowardsChildren(null)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .respondentBehaviour(respondentBehaviour)
                       .courtNavHome(home)
                       .situation(situationWithOnlyNonMolestationOrder)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(
            List.of(applicantStopFromRespondentEnum_Value_8),
            caseData.getRespondentBehaviourData().getApplicantWantToStopFromRespondentDoing()
        );
        assertNull(caseData.getRespondentBehaviourData().getApplicantWantToStopFromRespondentDoingToChild());
    }

    @Test
    void shouldMapApplicantContactInstructionsFromCourtNavDetails() {
        applicantsDetails.setApplicantContactInstructions("Test");

        courtNavFl401 = courtNavFl401.toBuilder()
            .metaData(CourtNavMetaData.builder()
                          .courtSpecialRequirements(null)
                          .build())
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyNonMolestationOrder)
                       .courtNavHome(CourtNavHome.builder().applyingForOccupationOrder(false).build())
                       .applicantDetails(applicantsDetails)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals("Test", caseData.getDaApplicantContactInstructions());
    }

    private CourtNavAddress buildApplicantAddress() {
        return CourtNavAddress.builder()
            .addressLine1("55 Test Street")
            .postTown("Town")
            .postCode("LU1 5ET")
            .build();
    }

    private Situation buildSituation(List<FL401OrderTypeEnum> orders) {
        return situationBuilder()
            .ordersAppliedFor(orders)
            .build();
    }

    private ApplicantsDetails buildApplicantsDetails(CourtNavAddress address) {
        return ApplicantsDetails.builder()
            .firstName("courtnav Applicant")
            .lastName("test")
            .dateOfBirth(new CourtNavDate(10, 9, 1992))
            .gender(female)
            .shareContactDetailsWithRespondent(false)
            .email("test@courtNav.com")
            .phoneNumber("12345678907")
            .hasLegalRepresentative(false)
            .applicantPreferredContact(List.of(PreferredContactEnum.email))
            .address(address)
            .build();
    }

    private CourtNavHome buildCourtNavHome(CourtNavAddress address) {
        return CourtNavHome.builder()
            .applyingForOccupationOrder(true)
            .occupationOrderAddress(address)
            .currentlyLivesAtAddress(List.of(other))
            .currentlyLivesAtAddressOther("test")
            .previouslyLivedAtAddress(applicant)
            .intendedToLiveAtAddress(applicant)
            .childrenApplicantResponsibility(List.of(new ChildAtAddress("test child", 3)))
            .childrenSharedResponsibility(List.of(new ChildAtAddress("test child", 5)))
            .propertySpeciallyAdapted(true)
            .propertyHasMortgage(true)
            .namedOnMortgage(List.of(ContractEnum.other))
            .namedOnMortgageOther("test")
            .mortgageNumber("2345678")
            .mortgageLenderName("test mort")
            .mortgageLenderAddress(CourtNavAddress.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .propertyIsRented(true)
            .namedOnRentalAgreement(List.of(ContractEnum.other))
            .namedOnRentalAgreementOther("test")
            .landlordName("landlord")
            .landlordAddress(CourtNavAddress.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .haveHomeRights(true)
            .wantToHappenWithLivingSituation(List.of(stayInHome))
            .wantToHappenWithFamilyHome(List.of(respondentToPayRentMortgage))
            .anythingElseForCourtToConsider("test court details")
            .build();
    }

    private Situation.SituationBuilder situationBuilder() {
        return Situation.builder()
            .ordersAppliedWithoutNotice(true)
            .additionalDetailsForCourt("test details")
            .bailConditionsOnRespondent(true)
            .ordersAppliedWithoutNoticeReasonDetails("test1")
            .bailConditionsEndDate(new CourtNavDate(8, 9, 1996))
            .ordersAppliedWithoutNoticeReason(List.of(riskOfSignificantHarm));
    }

    private BeforeStart buildBeforeStart() {
        return new BeforeStart(eighteenOrOlder);
    }

    private CourtNavRespondent buildRespondent(CourtNavAddress address) {
        return CourtNavRespondent.builder()
            .firstName("resp test")
            .lastName("fl401")
            .dateOfBirth(new CourtNavDate(10, 9, 1989))
            .email("test@resp.com")
            .address(address)
            .respondentLivesWithApplicant(true)
            .phoneNumber("12345670987")
            .build();
    }

    private CourtNavRelationShipToRespondent buildRelationship() {
        return CourtNavRelationShipToRespondent.builder()
            .relationshipDescription(formerlyMarriedOrCivil)
            .ceremonyDate(new CourtNavDate(10, 9, 1999))
            .relationshipEndDate(null)
            .relationshipStartDate(new CourtNavDate(10, 9, 1998))
            .respondentsRelationshipToApplicant(null)
            .respondentsRelationshipToApplicantOther(null)
            .anyChildren(false)
            .build();
    }

    private CourtNavRespondentBehaviour buildRespondentBehaviour() {
        return CourtNavRespondentBehaviour.builder()
            .applyingForMonMolestationOrder(true)
            .stopBehaviourAnythingElse("abc")
            .stopBehaviourTowardsApplicant(List.of(comingNearHome))
            .stopBehaviourTowardsChildren(List.of(beingViolentOrThreatening))
            .build();
    }

    private CourtNavStatementOfTruth buildStatementOfTruth() {
        return CourtNavStatementOfTruth.builder()
            .declaration(List.of(applicantConfirm))
            .signature("appl sign")
            .signatureDate(new CourtNavDate(10, 6, 2022))
            .signatureFullName("Applicant Courtnav")
            .representativeFirmName("courtnav_application")
            .representativePositionHeld("courtnav_application")
            .build();
    }

    private GoingToCourt buildGoingToCourt() {
        return GoingToCourt.builder()
            .isInterpreterRequired(false)
            .interpreterLanguage(null)
            .interpreterDialect(null)
            .anyDisabilityNeeds(false)
            .disabilityNeedsDetails(null)
            .anySpecialMeasures(List.of(separateWaitingRoom))
            .build();
    }

    private CourtNavMetaData buildMetaData() {
        return CourtNavMetaData.builder()
            .courtNavApproved(true)
            .caseOrigin("courtnav")
            .numberOfAttachments(4)
            .courtSpecialRequirements("test special court")
            .hasDraftOrder(false)
            .build();
    }

    private Family buildFamily() {
        return Family.builder()
            .whoApplicationIsFor(applicantAndChildren)
            .protectedChildren(List.of(
                ProtectedChild.builder()
                    .fullName("child1")
                    .dateOfBirth(new CourtNavDate(10, 9, 2016))
                    .parentalResponsibility(true)
                    .relationship("mother")
                    .respondentRelationship("uncle")
                    .build()))
            .anyOngoingCourtProceedings(false)
            .ongoingCourtProceedings(List.of(
                CourtProceedings.builder()
                    .caseDetails("testcase1")
                    .caseNumber("1234567")
                    .caseType("testType1")
                    .nameOfCourt("testcourt1")
                    .build()))
            .build();
    }

    private CourtNavFl401 buildCourtNavFl401(CourtNavAddress applicantAddress) {
        return CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(buildBeforeStart())
                       .applicantDetails(applicantsDetails)
                       .courtNavRespondent(buildRespondent(applicantAddress))
                       .family(buildFamily())
                       .relationshipWithRespondent(buildRelationship())
                       .respondentBehaviour(buildRespondentBehaviour())
                       .statementOfTruth(buildStatementOfTruth())
                       .goingToCourt(buildGoingToCourt())
                       .build())
            .metaData(buildMetaData())
            .build();
    }
}
