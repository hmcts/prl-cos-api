package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavApplicant;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavHome;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavMetaData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavRelationShipToRespondent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavRespondent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavRespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.Family;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.GoingToCourt;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ProtectedChild;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.Situation;

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
import static uk.gov.hmcts.reform.prl.mapper.courtnav.CourtNavTestDataFactory.buildApplicantAddress;
import static uk.gov.hmcts.reform.prl.mapper.courtnav.CourtNavTestDataFactory.buildApplicantsDetails;
import static uk.gov.hmcts.reform.prl.mapper.courtnav.CourtNavTestDataFactory.buildCourtNavFl401;
import static uk.gov.hmcts.reform.prl.mapper.courtnav.CourtNavTestDataFactory.buildCourtNavHome;
import static uk.gov.hmcts.reform.prl.mapper.courtnav.CourtNavTestDataFactory.buildSituation;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantRelationshipDescriptionEnum.formerlyMarriedOrCivil;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantRelationshipDescriptionEnum.noneOfAbove;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicationCoverEnum.applicantAndChildren;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicationCoverEnum.applicantOnly;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsApplicantEnum.comingNearHome;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum.beingViolentOrThreatening;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SpecialMeasuresEnum.separateWaitingRoom;

@SpringBootTest
@Import(FL401ApplicationMapper.class)
class FL401ApplicationMapperTest {

    @Autowired
    private FL401ApplicationMapper fl401ApplicationMapper;

    private CourtNavFl401 courtNavFl401;
    private Situation situationWithOnlyNonMolestationOrder;
    private Situation situationWithOnlyOccupationOrder;
    private CourtNavApplicant courtNavApplicant;
    private CourtNavHome home;

    @BeforeEach
    void setUp() {
        CourtNavAddress applicantAddress = buildApplicantAddress();
        situationWithOnlyNonMolestationOrder = buildSituation(List.of(nonMolestationOrder));
        situationWithOnlyOccupationOrder = buildSituation(List.of(occupationOrder));
        courtNavApplicant = buildApplicantsDetails(applicantAddress);
        home = buildCourtNavHome(applicantAddress);
        courtNavFl401 = buildCourtNavFl401(courtNavApplicant, applicantAddress);
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
                .caseDetails("case-details")
                .caseNumber("1234567")
                .caseType("case-type")
                .nameOfCourt("court")
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

        assertEquals("court", proceeding.getNameOfCourt());
        assertEquals("1234567", proceeding.getCaseNumber());
        assertEquals("case-type", proceeding.getTypeOfCase());
        assertEquals("case-details", proceeding.getAnyOtherDetails());
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
        courtNavApplicant.setGender(female);
        courtNavApplicant.setShareContactDetailsWithRespondent(true);
        courtNavApplicant.setAddress(CourtNavAddress.builder()
                                         .addressLine1("Address Line 1")
                                         .postTown("Town")
                                         .postCode("Postcode")
                                         .build());

        courtNavFl401 = courtNavFl401.toBuilder()
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .courtNavApplicant(courtNavApplicant)
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
        courtNavApplicant.setApplicantContactInstructions("Test");

        courtNavFl401 = courtNavFl401.toBuilder()
            .metaData(CourtNavMetaData.builder()
                          .courtSpecialRequirements(null)
                          .build())
            .fl401(courtNavFl401.getFl401().toBuilder()
                       .situation(situationWithOnlyNonMolestationOrder)
                       .courtNavHome(CourtNavHome.builder().applyingForOccupationOrder(false).build())
                       .courtNavApplicant(courtNavApplicant)
                       .build())
            .build();

        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(courtNavFl401);

        assertEquals("Test", caseData.getDaApplicantContactInstructions());
    }
}
