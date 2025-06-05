package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantRelationshipDescriptionEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ContractEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreferredContactEnum;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum.nonMolestationOrder;
import static uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum.occupationOrder;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge.eighteenOrOlder;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantRelationshipDescriptionEnum.formerlyMarriedOrCivil;
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
        CourtNavAddress applicantAddress = CourtNavAddress.builder()
            .addressLine1("55 Test Street")
            .postTown("Town")
            .postCode("LU1 5ET")
            .build();

        situationWithOnlyNonMolestationOrder = situationBuilder()
            .ordersAppliedFor(List.of(nonMolestationOrder))
            .build();

        situationWithOnlyOccupationOrder = situationBuilder()
            .ordersAppliedFor(List.of(occupationOrder))
            .build();

        applicantsDetails = ApplicantsDetails.builder()
            .firstName("courtnav Applicant")
            .lastName("test")
            .dateOfBirth(new CourtNavDate(10, 9, 1992))
            .gender(female)
            .shareContactDetailsWithRespondent(false)
            .email("test@courtNav.com")
            .phoneNumber("12345678907")
            .hasLegalRepresentative(false)
            .applicantPreferredContact(List.of(PreferredContactEnum.email))
            .address(applicantAddress)
            .build();

        CourtNavRespondent courtNavRespondent = CourtNavRespondent.builder()
            .firstName("resp test")
            .lastName("fl401")
            .dateOfBirth(new CourtNavDate(10, 9, 1989))
            .email("test@resp.com")
            .address(applicantAddress)
            .respondentLivesWithApplicant(true)
            .phoneNumber("12345670987")
            .build();

        CourtNavRelationShipToRespondent relationShipToRespondent = CourtNavRelationShipToRespondent.builder()
            .relationshipDescription(formerlyMarriedOrCivil)
            .ceremonyDate(new CourtNavDate(10, 9, 1999))
            .relationshipEndDate(null)
            .relationshipStartDate(new CourtNavDate(10, 9, 1998))
            .respondentsRelationshipToApplicant(null)
            .respondentsRelationshipToApplicantOther(null)
            .anyChildren(false)
            .build();

        CourtNavRespondentBehaviour respondentBehaviour = CourtNavRespondentBehaviour.builder()
            .applyingForMonMolestationOrder(true)
            .stopBehaviourAnythingElse("abc")
            .stopBehaviourTowardsApplicant(List.of(comingNearHome))
            .stopBehaviourTowardsChildren(List.of(beingViolentOrThreatening))
            .build();

        home = CourtNavHome.builder()
            .applyingForOccupationOrder(true)
            .occupationOrderAddress(applicantAddress)
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

        CourtNavStatementOfTruth statementOfTruth = CourtNavStatementOfTruth.builder()
            .declaration(List.of(applicantConfirm))
            .signature("appl sign")
            .signatureDate(new CourtNavDate(10, 6, 2022))
            .signatureFullName("Applicant Courtnav")
            .representativeFirmName("courtnav_application")
            .representativePositionHeld("courtnav_application")
            .build();

        GoingToCourt goingToCourt = GoingToCourt.builder()
            .isInterpreterRequired(false)
            .interpreterLanguage(null)
            .interpreterDialect(null)
            .anyDisabilityNeeds(false)
            .disabilityNeedsDetails(null)
            .anySpecialMeasures(List.of(separateWaitingRoom))
            .build();

        CourtNavMetaData courtNavMetaData = CourtNavMetaData.builder()
            .courtNavApproved(true)
            .caseOrigin("courtnav")
            .numberOfAttachments(4)
            .courtSpecialRequirements("test special court")
            .hasDraftOrder(false)
            .build();

        Family family = Family.builder()
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

        courtNavFl401 = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(new BeforeStart(eighteenOrOlder))
                       .applicantDetails(applicantsDetails)
                       .courtNavRespondent(courtNavRespondent)
                       .family(family)
                       .relationshipWithRespondent(relationShipToRespondent)
                       .respondentBehaviour(respondentBehaviour)
                       .statementOfTruth(statementOfTruth)
                       .goingToCourt(goingToCourt)
                       .build())
            .metaData(courtNavMetaData)
            .build();
    }

    @Test
    void testCourtnavCaseDataWithCourtNavFL401Details() {
        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyNonMolestationOrder)
                       .courtNavHome(CourtNavHome.builder().applyingForOccupationOrder(false).build())
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(
            courtNavFl401.getFl401().getSituation().getOrdersAppliedFor(),
            caseData.getTypeOfApplicationOrders().getOrderType()
        );
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedWithoutNoticeReason());
    }

    @Test
    void testCourtnavCaseDataWithCourtNavFL401DetailsWithOccupationalOrder() {
        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyOccupationOrder)
                       .courtNavHome(home)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(
            courtNavFl401.getFl401().getSituation().getOrdersAppliedFor(),
            caseData.getTypeOfApplicationOrders().getOrderType()
        );
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedWithoutNoticeReason());
    }

    @Test
    void testCourtnavMetaDataApprovedAsFalse() {
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

        fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertFalse(courtNavFl401.getMetaData().isCourtNavApproved());
        assertTrue(courtNavFl401.getMetaData().isHasDraftOrder());
        assertNotNull(courtNavFl401.getMetaData().getCaseOrigin());
    }

    @Test
    void testCourtnavOrdersAppliedWithoutNoticeAsFalse() {
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

        fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertFalse(courtNavFl401.getFl401().getSituation().isOrdersAppliedWithoutNotice());
        assertFalse(courtNavFl401.getFl401().getSituation().isBailConditionsOnRespondent());
    }

    @Test
    void testCourtnavFamilyAsApplicant() {
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

        fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(
            "Applicant Only",
            courtNavFl401.getFl401().getFamily().getWhoApplicationIsFor().getDisplayedValue()
        );
    }

    @Test
    void testCourtnavRelationShipDescriptionAsNoneOfTheAbove() {
        CourtNavRelationShipToRespondent updatedRelationship = CourtNavRelationShipToRespondent.builder()
            .relationshipDescription(ApplicantRelationshipDescriptionEnum.noneOfAbove)
            .ceremonyDate(null)
            .relationshipEndDate(null)
            .relationshipStartDate(null)
            .respondentsRelationshipToApplicant(ApplicantRelationshipOptionsEnum.cousin)
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

        fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(
            "None of the above",
            courtNavFl401.getFl401().getRelationshipWithRespondent().getRelationshipDescription()
                .getDisplayedValue()
        );
    }

    @Test
    void testCourtnavGoingToCourtInterpreterNeeds() {
        GoingToCourt customGoingToCourt = GoingToCourt.builder()
            .isInterpreterRequired(true)
            .interpreterLanguage("test")
            .interpreterDialect("test")
            .anyDisabilityNeeds(true)
            .disabilityNeedsDetails("test")
            .anySpecialMeasures(List.of(separateWaitingRoom))
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .courtNavHome(home)
                       .situation(situationWithOnlyNonMolestationOrder)
                       .goingToCourt(customGoingToCourt.toBuilder().interpreterDialect(null).build())
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(YesOrNo.Yes, caseData.getAttendHearing().getIsInterpreterNeeded());
        assertEquals(YesOrNo.Yes, caseData.getAttendHearing().getIsDisabilityPresent());
    }

    @Test
    void testCourtnavGoingToCourtWithNoSpecialMeasures() {
        GoingToCourt noSpecialMeasures = GoingToCourt.builder()
            .isInterpreterRequired(true)
            .interpreterLanguage("test")
            .interpreterDialect("test")
            .anyDisabilityNeeds(true)
            .disabilityNeedsDetails("test")
            .anySpecialMeasures(null)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .goingToCourt(noSpecialMeasures)
                       .courtNavHome(home)
                       .situation(situationWithOnlyNonMolestationOrder)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(YesOrNo.Yes, caseData.getAttendHearing().getIsInterpreterNeeded());
        assertEquals(YesOrNo.Yes, caseData.getAttendHearing().getIsDisabilityPresent());
        assertNull(courtNavFl401.getFl401().getGoingToCourt().getAnySpecialMeasures());
        assertNull(caseData.getAttendHearing().getSpecialArrangementsRequired());
    }

    @Test
    void testCourtnavOngoingCourtProceedings() {
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
            .dateOfBirth(CourtNavDate.builder().day(10).month(9).year(2016).build())
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

        assertEquals(YesNoDontKnow.yes, caseData.getFl401OtherProceedingDetails().getHasPrevOrOngoingOtherProceeding());
        assertNotNull(caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings());
    }

    @Test
    void testCourtnavHomeChildrenIsNull() {
        home = home.toBuilder()
            .childrenApplicantResponsibility(null)
            .childrenSharedResponsibility(null)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyOccupationOrder)
                       .courtNavHome(home)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(YesOrNo.No, caseData.getHome().getDoAnyChildrenLiveAtAddress());
    }

    @Test
    void testCourtnavHomePreviouslyLivedAtThisAddressAsNeither() {
        home = home.toBuilder()
            .previouslyLivedAtAddress(null)
            .intendedToLiveAtAddress(null)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyOccupationOrder)
                       .courtNavHome(home)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertNull(caseData.getHome().getEverLivedAtTheAddress());
        assertNull(caseData.getHome().getIntendToLiveAtTheAddress());
    }

    @Test
    void testCourtnavHomeLivingSituationAsNull() {
        home = home.toBuilder()
            .wantToHappenWithLivingSituation(null)
            .wantToHappenWithFamilyHome(null)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyOccupationOrder)
                       .courtNavHome(home)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertNull(caseData.getHome().getLivingSituation());
        assertNull(caseData.getHome().getFamilyHome());
    }

    @Test
    void testCourtnavHomeNameOnRentalAgreementAsNull() {
        home = home.toBuilder()
            .propertyIsRented(false)
            .namedOnRentalAgreement(null)
            .propertyHasMortgage(false)
            .namedOnMortgage(null)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyOccupationOrder)
                       .courtNavHome(home)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertNull(caseData.getHome().getLandlords());
        assertNull(caseData.getHome().getMortgages());
    }

    @Test
    void testCourtnavHomeMortgageAndRentDetailsAsFalse() {

        home = home.toBuilder()
            .propertyIsRented(false)
            .propertyHasMortgage(false)
            .haveHomeRights(false)
            .propertySpeciallyAdapted(false)
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyOccupationOrder)
                       .courtNavHome(home)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(YesOrNo.No, caseData.getHome().getIsPropertyRented());
        assertNull(caseData.getHome().getHowIsThePropertyAdapted());
        assertEquals(YesOrNo.No, caseData.getHome().getIsThereMortgageOnProperty());
        assertNull(caseData.getHome().getMortgages());
        assertEquals(YesOrNo.No, caseData.getHome().getIsPropertyAdapted());
        assertEquals(YesOrNo.No, caseData.getHome().getDoesApplicantHaveHomeRights());
        assertNull(caseData.getHome().getLandlords());
    }

    @Test
    void testCourtnavFamilyHomeEmptyListDoesNotCauseNullPE() {

        home = home.toBuilder()
            .wantToHappenWithFamilyHome(Collections.emptyList())
            .build();

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyOccupationOrder)
                       .courtNavHome(home)
                       .build())
            .build();

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertNotNull(caseData1.getHome().getLivingSituation());
        assertDoesNotThrow(() -> fl401ApplicationMapper.mapCourtNavData(courtNavFl401));
    }

    @Test
    void testCourtnavApplicantDetailsHasNoConfidentialInfo() {

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

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(YesOrNo.No, caseData1.getApplicantsFL401().getIsAddressConfidential());
        assertEquals(YesOrNo.No, caseData1.getApplicantsFL401().getIsEmailAddressConfidential());
        assertEquals(YesOrNo.No, caseData1.getApplicantsFL401().getIsPhoneNumberConfidential());
    }

    @Test
    void testCourtnavRespondentDetailsHasNullInfo() {
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

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(YesOrNo.No, caseData1.getRespondentsFL401().getCanYouProvideEmailAddress());
        assertEquals(YesOrNo.No, caseData1.getRespondentsFL401().getCanYouProvidePhoneNumber());
        assertEquals(YesOrNo.No, caseData1.getRespondentsFL401().getIsCurrentAddressKnown());
    }

    @Test
    void testCourtnavRelationShipToRespondentHasRelationEndDate() {
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

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(
            LocalDate.parse(relationShipToRespondent.getRelationshipEndDate().mergeDate()),
            caseData1.getRespondentRelationDateInfoObject().getRelationStartAndEndComplexType().getRelationshipDateComplexEndDate()
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

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertNotNull(caseData1.getApplicantChildDetails());
    }

    @Test
    void testCourtnavRespondentBehaviourTowardsApplicantAsNull() {
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

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertNull(courtNavFl401.getFl401().getRespondentBehaviour().getStopBehaviourTowardsApplicant());
        assertNull(caseData1.getRespondentBehaviourData().getApplicantWantToStopFromRespondentDoing());
    }

    @Test
    void testCourtnavRespondentBehaviourTowardsChildrenAsNull() {
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

        CaseData caseData1 = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertNull(courtNavFl401.getFl401().getRespondentBehaviour().getStopBehaviourTowardsChildren());
        assertNull(caseData1.getRespondentBehaviourData().getApplicantWantToStopFromRespondentDoingToChild());
    }

    @Test
    void testCourtNavCaseDataWhenPopulateDefaultCaseFlagIsOff() {
        courtNavFl401 = courtNavFl401.toBuilder()
            .metaData(CourtNavMetaData.builder()
                          .courtSpecialRequirements(null)
                          .build())
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyNonMolestationOrder)
                       .courtNavHome(CourtNavHome.builder().applyingForOccupationOrder(false).build())
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor(), caseData.getTypeOfApplicationOrders().getOrderType());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedFor());
        assertNotNull(courtNavFl401.getFl401().getSituation().getOrdersAppliedWithoutNoticeReason());
    }

    @Test
    void testCourtNavContactInformation() {
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

    private Situation.SituationBuilder situationBuilder() {
        return Situation.builder()
            .ordersAppliedWithoutNotice(true)
            .additionalDetailsForCourt("test details")
            .bailConditionsOnRespondent(true)
            .ordersAppliedWithoutNoticeReasonDetails("test1")
            .bailConditionsEndDate(new CourtNavDate(8, 9, 1996))
            .ordersAppliedWithoutNoticeReason(List.of(riskOfSignificantHarm));
    }
}
