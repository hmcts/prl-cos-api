package uk.gov.hmcts.reform.prl.courtnav.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum;
import uk.gov.hmcts.reform.prl.enums.FamilyHomeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LivingSituationEnum;
import uk.gov.hmcts.reform.prl.enums.MortgageNamedAfterEnum;
import uk.gov.hmcts.reform.prl.enums.PeopleLivingAtThisAddressEnum;
import uk.gov.hmcts.reform.prl.enums.ReasonForOrderWithoutGivingNoticeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoBothEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.Landlord;
import uk.gov.hmcts.reform.prl.models.complextypes.Mortgage;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDetailsOfWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ReasonForWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.RelationshipDateComplex;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBailConditionDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationDateInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationObjectType;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationOptionsInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.StatementOfTruth;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ApplicantsDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ChildAtAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ProtectedChild;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.RespondentDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsApplicantEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ContractEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.CurrentResidentAtAddressEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.FamilyHomeOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.LivingSituationOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;


@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FL401ApplicationMapper {

    public CaseData mapCourtNavData(CourtNavCaseData courtNavCaseData) {

        return CaseData.builder()
            .applicantAge(courtNavCaseData.getApplicantHowOld())
            .applicantCaseName(courtNavCaseData.getCourtNavCaseName())
            .typeOfApplicationOrders(TypeOfApplicationOrders.builder()
                                         .orderType(courtNavCaseData.getOrdersAppliedFor())
                                         .build())
            .orderWithoutGivingNoticeToRespondent(WithoutNoticeOrderDetails.builder()
                                                      .orderWithoutGivingNotice(courtNavCaseData.getOrdersAppliedWithoutNotice())
                                                      .build())
            .reasonForOrderWithoutGivingNotice(!courtNavCaseData.getOrdersAppliedWithoutNotice().getDisplayedValue().equals(
                "Yes") ? null : (ReasonForWithoutNoticeOrder.builder()
                .reasonForOrderWithoutGivingNotice(courtNavCaseData.getOrdersAppliedWithoutNoticeReason().stream()
                                                       .map(WithoutNoticeReasonEnum::getDisplayedValue)
                                                       .map(ReasonForOrderWithoutGivingNoticeEnum::getDisplayedValueFromEnumString)
                                                       .collect(Collectors.toList()))
                                                   .futherDetails(courtNavCaseData.getOrdersAppliedWithoutNoticeReasonDetails())
                .build()))
            .bailDetails(RespondentBailConditionDetails.builder()
                             .isRespondentAlreadyInBailCondition(YesNoDontKnow.valueOf(courtNavCaseData
                                                                                           .getBailConditionsOnRespondent().getDisplayedValue()))
                             .bailConditionEndDate(courtNavCaseData.getBailConditionsEndDate())
                             .build())
            .anyOtherDtailsForWithoutNoticeOrder(OtherDetailsOfWithoutNoticeOrder.builder()
                                                     .otherDetails(courtNavCaseData.getAdditionalDetailsForCourt())
                                                     .build())
            .applicantsFL401(mapApplicant(courtNavCaseData.getApplicantDetails()))
            .respondentsFL401(mapRespondent(courtNavCaseData.getRespondentDetails()))
            .applicantFamilyDetails(ApplicantFamilyDetails.builder()
                                        .doesApplicantHaveChildren(courtNavCaseData.getWhoApplicationIsFor())
                                        .build())
            .applicantChildDetails(courtNavCaseData.getWhoApplicationIsFor().getDisplayedValue().equals("Yes")
                                       ? mapProtectedChild(courtNavCaseData.getProtectedChildren()) : null)
            .respondentBehaviourData(RespondentBehaviour.builder()
                         .applicantWantToStopFromRespondentDoing(courtNavCaseData
                                                     .getStopBehaviourTowardsApplicant()
                                                     .stream()
                                                     .map(BehaviourTowardsApplicantEnum::getDisplayedValue)
                                                     .map(ApplicantStopFromRespondentDoingEnum::getDisplayedValueFromEnumString)
                                                     .collect(Collectors.toList()))
                         .applicantWantToStopFromRespondentDoingToChild(courtNavCaseData
                                                    .getStopBehaviourTowardsChildren()
                                                    .stream()
                                                    .map(BehaviourTowardsChildrenEnum::getDisplayedValue)
                                                    .map(ApplicantStopFromRespondentDoingToChildEnum::getDisplayedValueFromEnumString)
                                                    .collect(Collectors.toList()))
                         .otherReasonApplicantWantToStopFromRespondentDoing(courtNavCaseData.getStopBehaviourAnythingElse())
                         .build())
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                          .applicantRelationship(ApplicantRelationshipEnum
                                                                     .valueOf(courtNavCaseData.getRelationshipDescription()
                                                                                  .getDisplayedValue()))
                                          .build())
            .respondentRelationDateInfoObject((!courtNavCaseData.getRelationshipDescription()
                .getDisplayedValue()
                .equalsIgnoreCase("noneOfAbove"))
                      ? (RespondentRelationDateInfo.builder()
                          .relationStartAndEndComplexType(RelationshipDateComplex.builder()
                                      .relationshipDateComplexStartDate(courtNavCaseData.getRelationshipStartDate())
                                      .relationshipDateComplexEndDate(courtNavCaseData.getRelationshipEndDate())
                                      .build())
                          .applicantRelationshipDate(courtNavCaseData.getCeremonyDate())
                          .build()) : null)
            .respondentRelationOptions((courtNavCaseData.getRelationshipDescription().getDisplayedValue().equalsIgnoreCase("noneOfAbove"))
                           ? (RespondentRelationOptionsInfo.builder()
                               .applicantRelationshipOptions(courtNavCaseData.getRespondentsRelationshipToApplicant())
                               .relationOptionsOther(courtNavCaseData.getRelationshipToApplicantOther())
                                 .build()) : null)
            .home(mapHomeDetails(courtNavCaseData))
            .fl401StmtOfTruth(StatementOfTruth.builder()
                                  .applicantConsent(courtNavCaseData.getDeclaration())
                                  .signature(courtNavCaseData.getSignature())
                                  .fullname(courtNavCaseData.getSignatureFullName())
                                  .date(courtNavCaseData.getSignatureDate())
                                  .nameOfFirm(courtNavCaseData.getRepresentativeFirmName())
                                  .signOnBehalf(courtNavCaseData.getRepresentativePositionHeld())
                                  .build())
            .build();

    }

    private Home mapHomeDetails(CourtNavCaseData courtNavCaseData) {

        return Home.builder()
            .address(courtNavCaseData.getOccupationOrderAddress())
            .peopleLivingAtThisAddress(courtNavCaseData.getCurrentlyLivesAtAddress()
                                           .stream()
                                           .map(CurrentResidentAtAddressEnum::getDisplayedValue)
                                           .map(PeopleLivingAtThisAddressEnum::getDisplayedValueFromEnumString)
                                           .collect(Collectors.toList()))
            .textAreaSomethingElse(courtNavCaseData.getCurrentlyLivesAtAddressOther())
            .everLivedAtTheAddress(YesNoBothEnum.valueOf(courtNavCaseData.getPreviouslyLivedAtAddress().getDisplayedValue()))
            .intendToLiveAtTheAddress(YesNoBothEnum.valueOf(courtNavCaseData.getIntendedToLiveAtAddress().getDisplayedValue()))
            .doAnyChildrenLiveAtAddress(YesOrNo.valueOf(null != courtNavCaseData.getChildrenApplicantResponsibility() ? "Yes" : "No"))
            .children(mapHomeChildren(courtNavCaseData.getChildrenApplicantResponsibility()))
            .isPropertyAdapted(courtNavCaseData.getPropertySpeciallyAdapted())
            .howIsThePropertyAdapted(courtNavCaseData.getPropertySpeciallyAdaptedDetails())
            .isThereMortgageOnProperty(courtNavCaseData.getPropertyHasMortgage())
            .mortgages(Mortgage.builder()
                           .mortgageNamedAfter(courtNavCaseData.getNamedOnMortgage()
                                                   .stream()
                                                   .map(ContractEnum::getDisplayedValue)
                                                   .map(MortgageNamedAfterEnum::getDisplayedValueFromEnumString)
                                                   .collect(Collectors.toList()))
                           .textAreaSomethingElse(courtNavCaseData.getNamedOnMortgageOther())
                           .mortgageLenderName(courtNavCaseData.getMortgageLenderName())
                           .mortgageNumber(courtNavCaseData.getMortgageNumber())
                           .address(courtNavCaseData.getLandlordAddress())
                           .build())
            .isPropertyRented(courtNavCaseData.getPropertyIsRented())
            .landlords(Landlord.builder()
                           .mortgageNamedAfterList(courtNavCaseData.getNamedOnRentalAgreement()
                                                       .stream()
                                                       .map(ContractEnum::getDisplayedValue)
                                                       .map(MortgageNamedAfterEnum::getDisplayedValueFromEnumString)
                                                       .collect(Collectors.toList()))
                           .textAreaSomethingElse(courtNavCaseData.getNamedOnRentalAgreementOther())
                           .landlordName(courtNavCaseData.getLandlordName())
                           .address(courtNavCaseData.getLandlordAddress())
                           .build())
            .doesApplicantHaveHomeRights(courtNavCaseData.getHaveHomeRights())
            .livingSituation(courtNavCaseData.getWantToHappenWithLivingSituation()
                                 .stream()
                                 .map(LivingSituationOutcomeEnum::getDisplayedValue)
                                 .map(LivingSituationEnum::getDisplayedValueFromEnumString)
                                 .collect(Collectors.toList()))
            .familyHome(courtNavCaseData.getWantToHappenWithFamilyHome()
                            .stream()
                            .map(FamilyHomeOutcomeEnum::getDisplayedValue)
                            .map(FamilyHomeEnum::getDisplayedValueFromEnumString)
                            .collect(Collectors.toList()))

            .build();


    }

    private List<Element<ChildrenLiveAtAddress>> mapHomeChildren(List<Element<ChildAtAddress>> childrenApplicantResponsibility) {
        Optional<List<Element<ChildAtAddress>>> childElementsCheck = ofNullable(childrenApplicantResponsibility);

        List<ChildrenLiveAtAddress> childList = new ArrayList<>();
        for (Element<ChildAtAddress> child : childrenApplicantResponsibility) {

            ChildAtAddress value = child.getValue();
            childList.add(ChildrenLiveAtAddress.builder()
                              .keepChildrenInfoConfidential(YesOrNo.No)
                              .childFullName(value.getFullName())
                              .childsAge(String.valueOf(value.getAge()))
                              .isRespondentResponsibleForChild(YesOrNo.No)
                              .build());
        }
        return ElementUtils.wrapElements(childList);
    }

    private List<Element<ApplicantChild>> mapProtectedChild(List<Element<ProtectedChild>> protectedChildren) {
        Optional<List<Element<ProtectedChild>>> childElementsCheck = ofNullable(protectedChildren);

        List<ApplicantChild> childList = new ArrayList<>();
        for (Element<ProtectedChild> protectedChild : protectedChildren) {

            ProtectedChild value = protectedChild.getValue();
            childList.add(ApplicantChild.builder()
                .fullName(value.getFullName())
                .dateOfBirth(value.getDateOfBirth())
                .applicantChildRelationship(value.getRelationship())
                .applicantRespondentShareParental(value.getParentalResponsibility())
                .respondentChildRelationship(value.getRespondentRelationship())
                .build());
        }
        return ElementUtils.wrapElements(childList);
    }

    private PartyDetails mapRespondent(RespondentDetails respondent) {
        return PartyDetails.builder()
            .firstName(respondent.getRespondentFirstName())
            .lastName(respondent.getRespondentLastName())
            .previousName(respondent.getRespondentOtherNames())
            .dateOfBirth(respondent.getRespondentDateOfBirth())
            .isDateOfBirthKnown(YesOrNo.valueOf(null != respondent.getRespondentDateOfBirth() ? "Yes" : "No"))
            .email(respondent.getRespondentEmailAddress())
            .canYouProvideEmailAddress(YesOrNo.valueOf(null != respondent.getRespondentEmailAddress() ? "Yes" : "No"))
            .phoneNumber(respondent.getRespondentPhoneNumber())
            .canYouProvidePhoneNumber(YesOrNo.valueOf(null != respondent.getRespondentPhoneNumber() ? "Yes" : "No"))
            .address(respondent.getRespondentAddress())
            .isCurrentAddressKnown(YesOrNo.valueOf(null != respondent.getRespondentAddress() ? "Yes" : "No"))
            .respondentLivedWithApplicant(respondent.getRespondentLivesWithApplicant())
            .build();
    }

    private PartyDetails mapApplicant(ApplicantsDetails applicant) {

        return PartyDetails.builder()
            .firstName(applicant.getApplicantFirstName())
            .lastName(applicant.getApplicantLastName())
            .previousName(applicant.getApplicantOtherNames())
            .dateOfBirth(applicant.getApplicantDateOfBirth())
            .gender(Gender.valueOf(applicant.getApplicantGender().getDisplayedValue()))
            .otherGender((!applicant.getApplicantGender().getDisplayedValue().equals("Male")
                         || !applicant.getApplicantGender().getDisplayedValue().equals("Female"))
                             ? applicant.getApplicantGender().getDisplayedValue() : null)
            .address(applicant.getApplicantAddress())
            .isAddressConfidential(YesOrNo.valueOf(applicant.getShareContactDetailsWithRespondent()
                                                       .getDisplayedValue().equals("No") ? "Yes" : "No"))
            .canYouProvideEmailAddress(YesOrNo.valueOf(null != applicant.getApplicantEmailAddress() ? "Yes" : "No"))
            .email(applicant.getApplicantEmailAddress())
            .isEmailAddressConfidential(YesOrNo.valueOf(applicant.getShareContactDetailsWithRespondent()
                                                            .getDisplayedValue().equals("No") ? "Yes" : "No"))
            .phoneNumber(applicant.getApplicantPhoneNumber())
            .isPhoneNumberConfidential(YesOrNo.valueOf(applicant.getShareContactDetailsWithRespondent()
                                                           .getDisplayedValue().equals("No") ? "Yes" : "No"))
            .applicantPreferredContact(applicant.getApplicantPreferredContact())
            .applicantContactInstructions(applicant.getApplicantContactInstructions())
            .representativeFirstName(applicant.getLegalRepresentativeFirstName())
            .representativeLastName(applicant.getLegalRepresentativeLastName())
            .solicitorTelephone(applicant.getLegalRepresentativePhone())
            .solicitorReference(applicant.getLegalRepresentativeReference())
            .solicitorEmail(applicant.getLegalRepresentativeEmail())
            .solicitorAddress(applicant.getLegalRepresentativeAddress())
            .dxNumber(applicant.getLegalRepresentativeDx())
            .build();
    }

}
