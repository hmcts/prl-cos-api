package uk.gov.hmcts.reform.prl.courtnav.mappers;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
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
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.InterpreterNeed;
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
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ApplicantsDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ChildAtAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ProtectedChild;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.RespondentDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsApplicantEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ContractEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.CurrentResidentAtAddressEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.FamilyHomeOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.LivingSituationOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SpecialMeasuresEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FL401ApplicationMapper {

    private final CourtFinderService courtFinderService;
    private Court court = null;

    public CaseData mapCourtNavData(CourtNavCaseData courtNavCaseData) throws NotFoundException {

        CaseData caseData = null;
        caseData =  CaseData.builder()
            .applicantAge(courtNavCaseData.getApplicantHowOld())
            .applicantCaseName(courtNavCaseData.getCourtNavCaseName())
            .typeOfApplicationOrders(TypeOfApplicationOrders.builder()
                                         .orderType(courtNavCaseData.getOrdersAppliedFor())
                                         .build())
            .orderWithoutGivingNoticeToRespondent(WithoutNoticeOrderDetails.builder()
                                                      .orderWithoutGivingNotice(courtNavCaseData.isOrdersAppliedWithoutNotice()
                                                      ? YesOrNo.Yes : YesOrNo.No)
                                                      .build())
            .reasonForOrderWithoutGivingNotice(!courtNavCaseData.isOrdersAppliedWithoutNotice() ? null : (ReasonForWithoutNoticeOrder.builder()
                .reasonForOrderWithoutGivingNotice(courtNavCaseData.getOrdersAppliedWithoutNoticeReason().stream()
                                                       .map(WithoutNoticeReasonEnum::getDisplayedValue)
                                                       .map(ReasonForOrderWithoutGivingNoticeEnum::getDisplayedValueFromEnumString)
                                                       .collect(Collectors.toList()))
                                                   .futherDetails(courtNavCaseData.getOrdersAppliedWithoutNoticeReasonDetails())
                .build()))
            .bailDetails(RespondentBailConditionDetails.builder()
                             .isRespondentAlreadyInBailCondition(courtNavCaseData.isBailConditionsOnRespondent()
                             ? YesNoDontKnow.yes : YesNoDontKnow.no)
                             .bailConditionEndDate(courtNavCaseData.isBailConditionsOnRespondent()
                                                       ? courtNavCaseData.getBailConditionsEndDate() : null)
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
            .respondentBehaviourData(courtNavCaseData.getOrdersAppliedFor().contains(FL401OrderTypeEnum.occupationOrder)
                                     ? (RespondentBehaviour.builder()
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
                         .build()) : null)
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                          .applicantRelationship(ApplicantRelationshipEnum
                                                                     .getDisplayedValueFromEnumString(courtNavCaseData.getRelationshipDescription()
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
            .home(courtNavCaseData.getOrdersAppliedFor().contains(FL401OrderTypeEnum.occupationOrder) ? mapHomeDetails(courtNavCaseData) : null)
            .fl401StmtOfTruth(StatementOfTruth.builder()
                                  .applicantConsent(courtNavCaseData.getDeclaration())
                                  .signature(courtNavCaseData.getSignature())
                                  .fullname(courtNavCaseData.getSignatureFullName())
                                  .date(courtNavCaseData.getSignatureDate())
                                  .nameOfFirm(courtNavCaseData.getRepresentativeFirmName())
                                  .signOnBehalf(courtNavCaseData.getRepresentativePositionHeld())
                                  .build())
            .isInterpreterNeeded(courtNavCaseData.isInterpreterRequired() ? YesOrNo.Yes : YesOrNo.No)
            .interpreterNeeds(interpreterLanguageDetails(courtNavCaseData))
            .isDisabilityPresent(courtNavCaseData.isAnyDisabilityNeeds() ? YesOrNo.Yes : YesOrNo.No)
            .adjustmentsRequired(courtNavCaseData.isAnyDisabilityNeeds()
                ? courtNavCaseData.getDisabilityNeedsDetails() : null)
            .isSpecialArrangementsRequired(!courtNavCaseData.getAnySpecialMeasures().isEmpty()
                                               ? YesOrNo.Yes : YesOrNo.No)
            .specialArrangementsRequired(!courtNavCaseData.getAnySpecialMeasures().isEmpty()
                                             ? (courtNavCaseData.getAnySpecialMeasures()
                                                .stream()
                                                .map(SpecialMeasuresEnum::getDisplayedValue)
                                                .collect(Collectors.joining(","))) : null)
            .specialCourtName(courtNavCaseData.getCourtSpecialRequirements())
            .fl401OtherProceedingDetails(FL401OtherProceedingDetails.builder()
                                             .hasPrevOrOngoingOtherProceeding(courtNavCaseData.isAnyOngoingCourtProceedings()
                                                 ? YesNoDontKnow.yes : YesNoDontKnow.no)
                                             .fl401OtherProceedings(courtNavCaseData.isAnyOngoingCourtProceedings()
                                                                        ? getOngoingProceedings(courtNavCaseData.getOngoingCourtProceedings()) : null)
                                             .build())
            .build();

        caseData.toBuilder()
            .courtName(getCourtName(caseData))
            .courtEmailAddress(getCourtEmailAddress(court))
            .build();
        return caseData;

    }

    private String getCourtName(CaseData caseData) throws NotFoundException {
        caseData = caseData.toBuilder().caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE).build();
        court = courtFinderService.getNearestFamilyCourt(caseData);
        return court.getCourtName();
    }

    private String getCourtEmailAddress(Court court1) {

        Optional<CourtEmailAddress> courtEmailAddress = courtFinderService.getEmailAddress(court1);
        return String.valueOf(courtEmailAddress);
    }

    private List<Element<FL401Proceedings>> getOngoingProceedings(List<Element<CourtProceedings>> ongoingCourtProceedings) {

        List<FL401Proceedings> proceedingsList = new ArrayList<>();
        for (Element<CourtProceedings> courtProceedingsElement : ongoingCourtProceedings) {

            CourtProceedings proceedings = courtProceedingsElement.getValue();
            proceedingsList.add(FL401Proceedings.builder()
                                    .nameOfCourt(proceedings.getNameOfCourt())
                                    .caseNumber(proceedings.getCaseNumber())
                                    .typeOfCase(proceedings.getCaseType())
                                    .anyOtherDetails(proceedings.getCaseDetails())
                                    .build());
        }
        return ElementUtils.wrapElements(proceedingsList);

    }

    private List<Element<InterpreterNeed>> interpreterLanguageDetails(CourtNavCaseData courtNavCaseData) {

        InterpreterNeed interpreterNeed = InterpreterNeed.builder()
            .language(courtNavCaseData.getInterpreterLanguage())
            .otherAssistance(courtNavCaseData.getInterpreterDialect())
            .build();

        return ElementUtils.wrapElements(interpreterNeed);
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
            .isPropertyAdapted(courtNavCaseData.isPropertySpeciallyAdapted() ? YesOrNo.Yes : YesOrNo.No)
            .howIsThePropertyAdapted(courtNavCaseData.getPropertySpeciallyAdaptedDetails())
            .isThereMortgageOnProperty(courtNavCaseData.isPropertyHasMortgage() ? YesOrNo.Yes : YesOrNo.No)
            .mortgages(courtNavCaseData.isPropertyHasMortgage() ? (Mortgage.builder()
                           .mortgageNamedAfter(courtNavCaseData.getNamedOnMortgage()
                                                   .stream()
                                                   .map(ContractEnum::getDisplayedValue)
                                                   .map(MortgageNamedAfterEnum::getDisplayedValueFromEnumString)
                                                   .collect(Collectors.toList()))
                           .textAreaSomethingElse(courtNavCaseData.getNamedOnMortgageOther())
                           .mortgageLenderName(courtNavCaseData.getMortgageLenderName())
                           .mortgageNumber(courtNavCaseData.getMortgageNumber())
                           .address(courtNavCaseData.getLandlordAddress())
                           .build()) : null)
            .isPropertyRented(courtNavCaseData.isPropertyIsRented() ? YesOrNo.Yes : YesOrNo.No)
            .landlords(courtNavCaseData.isPropertyIsRented() ? (Landlord.builder()
                           .mortgageNamedAfterList(courtNavCaseData.getNamedOnRentalAgreement()
                                                       .stream()
                                                       .map(ContractEnum::getDisplayedValue)
                                                       .map(MortgageNamedAfterEnum::getDisplayedValueFromEnumString)
                                                       .collect(Collectors.toList()))
                           .textAreaSomethingElse(courtNavCaseData.getNamedOnRentalAgreementOther())
                           .landlordName(courtNavCaseData.getLandlordName())
                           .address(courtNavCaseData.getLandlordAddress())
                           .build()) : null)
            .doesApplicantHaveHomeRights(courtNavCaseData.isHaveHomeRights() ? YesOrNo.Yes : YesOrNo.No)
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

        List<ApplicantChild> childList = new ArrayList<>();
        for (Element<ProtectedChild> protectedChild : protectedChildren) {

            ProtectedChild value = protectedChild.getValue();
            childList.add(ApplicantChild.builder()
                .fullName(value.getFullName())
                .dateOfBirth(value.getDateOfBirth())
                .applicantChildRelationship(value.getRelationship())
                .applicantRespondentShareParental(value.isParentalResponsibility() ? YesOrNo.Yes : YesOrNo.No)
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
            .respondentLivedWithApplicant(respondent.isRespondentLivesWithApplicant() ? YesOrNo.Yes : YesOrNo.No)
            .build();
    }

    private PartyDetails mapApplicant(ApplicantsDetails applicant) {

        return PartyDetails.builder()
            .firstName(applicant.getApplicantFirstName())
            .lastName(applicant.getApplicantLastName())
            .previousName(applicant.getApplicantOtherNames())
            .dateOfBirth(applicant.getApplicantDateOfBirth())
            .gender(Gender.getDisplayedValueFromEnumString(applicant.getApplicantGender().getDisplayedValue()))
            .otherGender((!applicant.getApplicantGender().getDisplayedValue().equals("Male")
                         || !applicant.getApplicantGender().getDisplayedValue().equals("Female"))
                             ? applicant.getApplicantGender().getDisplayedValue() : null)
            .address(applicant.getApplicantAddress())
            .isAddressConfidential(!applicant.isShareContactDetailsWithRespondent() ? YesOrNo.Yes : YesOrNo.No)
            .canYouProvideEmailAddress(YesOrNo.valueOf(null != applicant.getApplicantEmailAddress() ? "Yes" : "No"))
            .email(applicant.getApplicantEmailAddress())
            .isEmailAddressConfidential(!applicant.isShareContactDetailsWithRespondent() ? YesOrNo.Yes : YesOrNo.No)
            .phoneNumber(applicant.getApplicantPhoneNumber())
            .isPhoneNumberConfidential(!applicant.isShareContactDetailsWithRespondent() ? YesOrNo.Yes : YesOrNo.No)
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
