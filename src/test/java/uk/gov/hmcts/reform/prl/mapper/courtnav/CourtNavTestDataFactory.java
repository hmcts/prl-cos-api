package uk.gov.hmcts.reform.prl.mapper.courtnav;

import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.BeforeStart;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ChildAtAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavApplicant;
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

import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge.eighteenOrOlder;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantRelationshipDescriptionEnum.formerlyMarriedOrCivil;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicationCoverEnum.applicantAndChildren;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsApplicantEnum.comingNearHome;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum.beingViolentOrThreatening;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ConsentEnum.applicantConfirm;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.CurrentResidentAtAddressEnum.other;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.FamilyHomeOutcomeEnum.respondentToPayRentMortgage;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.LivingSituationOutcomeEnum.stayInHome;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreviousOrIntendedResidentAtAddressEnum.applicant;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SpecialMeasuresEnum.separateWaitingRoom;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum.riskOfSignificantHarm;

final class CourtNavTestDataFactory {

    private CourtNavTestDataFactory() {
        // utility class
    }

    static CourtNavAddress buildApplicantAddress() {
        return CourtNavAddress.builder()
            .addressLine1("Address Line 1")
            .postTown("Town")
            .postCode("Postcode")
            .build();
    }

    static Situation buildSituation(List<FL401OrderTypeEnum> orders) {
        return situationBuilder()
            .ordersAppliedFor(orders)
            .build();
    }

    static CourtNavApplicant buildApplicantsDetails(CourtNavAddress address) {
        return CourtNavApplicant.builder()
            .firstName("applicant_first_name")
            .lastName("applicant_last_name")
            .dateOfBirth(new CourtNavDate(10, 9, 1992))
            .gender(female)
            .shareContactDetailsWithRespondent(false)
            .email("email@exmaple.com")
            .phoneNumber("12345678907")
            .hasLegalRepresentative(false)
            .applicantPreferredContact(List.of(PreferredContactEnum.email))
            .address(address)
            .build();
    }

    static CourtNavHome buildCourtNavHome(CourtNavAddress address) {
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

    static Situation.SituationBuilder situationBuilder() {
        return Situation.builder()
            .ordersAppliedWithoutNotice(true)
            .additionalDetailsForCourt("test details")
            .bailConditionsOnRespondent(true)
            .ordersAppliedWithoutNoticeReasonDetails("test1")
            .bailConditionsEndDate(new CourtNavDate(8, 9, 1996))
            .ordersAppliedWithoutNoticeReason(List.of(riskOfSignificantHarm));
    }

    static BeforeStart buildBeforeStart() {
        return new BeforeStart(eighteenOrOlder);
    }

    static CourtNavRespondent buildRespondent(CourtNavAddress address) {
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

    static CourtNavRelationShipToRespondent buildRelationship() {
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

    static CourtNavRespondentBehaviour buildRespondentBehaviour() {
        return CourtNavRespondentBehaviour.builder()
            .applyingForMonMolestationOrder(true)
            .stopBehaviourAnythingElse("abc")
            .stopBehaviourTowardsApplicant(List.of(comingNearHome))
            .stopBehaviourTowardsChildren(List.of(beingViolentOrThreatening))
            .build();
    }

    static CourtNavStatementOfTruth buildStatementOfTruth() {
        return CourtNavStatementOfTruth.builder()
            .applicantConsent(List.of(applicantConfirm))
            .signature("appl sign")
            .date(new CourtNavDate(10, 6, 2022))
            .fullname("Applicant Courtnav")
            .nameOfFirm("courtnav_application")
            .signOnBehalf("courtnav_application")
            .build();
    }

    static GoingToCourt buildGoingToCourt() {
        return GoingToCourt.builder()
            .isInterpreterRequired(false)
            .interpreterLanguage(null)
            .interpreterDialect(null)
            .anyDisabilityNeeds(false)
            .disabilityNeedsDetails(null)
            .anySpecialMeasures(List.of(separateWaitingRoom))
            .build();
    }

    static CourtNavMetaData buildMetaData() {
        return CourtNavMetaData.builder()
            .courtNavApproved(true)
            .caseOrigin("courtnav")
            .numberOfAttachments(4)
            .courtSpecialRequirements("test special court")
            .hasDraftOrder(false)
            .build();
    }

    static Family buildFamily() {
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
                    .caseDetails("case-details")
                    .caseNumber("1234567")
                    .caseType("case-type")
                    .nameOfCourt("court")
                    .build()))
            .build();
    }

    static CourtNavFl401 buildCourtNavFl401(CourtNavApplicant applicant, CourtNavAddress applicantAddress) {
        return CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .beforeStart(buildBeforeStart())
                       .courtNavApplicant(applicant)
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
