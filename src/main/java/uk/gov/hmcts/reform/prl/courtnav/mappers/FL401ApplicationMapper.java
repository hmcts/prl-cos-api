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
import uk.gov.hmcts.reform.prl.enums.FL401Consent;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsApplicantEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ConsentEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ContractEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.CurrentResidentAtAddressEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.FamilyHomeOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.LivingSituationOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SignatureEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SpecialMeasuresEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

        CaseData caseData = CaseData.builder()
            .applicantAge(ApplicantAge.getValue(String.valueOf(courtNavCaseData.getApplicantHowOld())))
            .applicantCaseName(getCaseName(courtNavCaseData))
            .typeOfApplicationOrders(TypeOfApplicationOrders.builder()
                                         .orderType(courtNavCaseData.getOrdersAppliedFor())
                                         .build())
            .orderWithoutGivingNoticeToRespondent(WithoutNoticeOrderDetails.builder()
                                                      .orderWithoutGivingNotice(courtNavCaseData.isOrdersAppliedWithoutNotice()
                                                                                    ? YesOrNo.Yes : YesOrNo.No)
                                                      .build())
            .reasonForOrderWithoutGivingNotice(!courtNavCaseData.isOrdersAppliedWithoutNotice() ? null : (ReasonForWithoutNoticeOrder.builder()
                .reasonForOrderWithoutGivingNotice(getReasonForWithOutOrderNotice(courtNavCaseData))
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
            .respondentBehaviourData(courtNavCaseData.getOrdersAppliedFor().contains(FL401OrderTypeEnum.nonMolestationOrder)
                                         ? (RespondentBehaviour.builder()
                .applicantWantToStopFromRespondentDoing(getBehaviourTowardsApplicant(courtNavCaseData))
                .applicantWantToStopFromRespondentDoingToChild(getBehaviourTowardsChildren(courtNavCaseData))
                .otherReasonApplicantWantToStopFromRespondentDoing(courtNavCaseData.getStopBehaviourAnythingElse())
                .build()) : null)
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                          .applicantRelationship(ApplicantRelationshipEnum
                                                                     .getDisplayedValueFromEnumString(courtNavCaseData.getRelationshipDescription()
                                                                                                          .getId()))
                                          .build())
            .respondentRelationDateInfoObject((!courtNavCaseData.getRelationshipDescription().getId()
                .equalsIgnoreCase("noneOfAbove"))
                                                  ? (RespondentRelationDateInfo.builder()
                .relationStartAndEndComplexType(RelationshipDateComplex.builder()
                                                    .relationshipDateComplexStartDate(courtNavCaseData.getRelationshipStartDate())
                                                    .relationshipDateComplexEndDate(courtNavCaseData.getRelationshipEndDate())
                                                    .build())
                .applicantRelationshipDate(courtNavCaseData.getCeremonyDate())
                .build()) : null)
            .respondentRelationOptions((courtNavCaseData.getRelationshipDescription().getId().equalsIgnoreCase(
                "noneOfAbove"))
                                           ? (RespondentRelationOptionsInfo.builder()
                .applicantRelationshipOptions(courtNavCaseData.getRespondentsRelationshipToApplicant())
                .relationOptionsOther(courtNavCaseData.getRelationshipToApplicantOther())
                .build()) : null)
            .home(courtNavCaseData.getOrdersAppliedFor().contains(FL401OrderTypeEnum.occupationOrder) ? mapHomeDetails(
                courtNavCaseData) : null)
            .fl401StmtOfTruth(StatementOfTruth.builder()
                                  .applicantConsent(courtNavCaseData.getDeclaration().stream()
                                                        .map(ConsentEnum::getId)
                                                        .map(FL401Consent::getDisplayedValueFromEnumString)
                                                        .collect(Collectors.toList()))
                                  .signature(courtNavCaseData.getSignature())
                                  .signatureType(SignatureEnum.getValue(String.valueOf(courtNavCaseData.getSignatureType())))
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
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
            .dateSubmittedAndTime(DateTimeFormatter.ofPattern("d MMM yyyy, hh:mm:ssa").format(zonedDateTime).toUpperCase())
            .build();

        caseData = caseData.toBuilder()
            .courtName(getCourtName(caseData))
            .courtEmailAddress(getCourtEmailAddress(court))
            .build();

        caseData = caseData.setDateSubmittedDate();

        return caseData;

    }

    private String getCaseName(CourtNavCaseData courtNavCaseData) {

        String applicantName = courtNavCaseData.getApplicantDetails().getApplicantFirstName() + " "
            + courtNavCaseData.getApplicantDetails().getApplicantLastName();

        String respondentName = courtNavCaseData.getRespondentDetails().getRespondentFirstName() + " "
            + courtNavCaseData.getRespondentDetails().getRespondentLastName();

        return applicantName + " & " + respondentName;
    }

    private List<ApplicantStopFromRespondentDoingToChildEnum> getBehaviourTowardsChildren(CourtNavCaseData courtNavCaseData) {

        List<BehaviourTowardsChildrenEnum> behaviourTowardsChildrenList = courtNavCaseData.getStopBehaviourTowardsChildren();
        List<ApplicantStopFromRespondentDoingToChildEnum> applicantStopFromRespondentDoingToChildList = new ArrayList<>();
        for (BehaviourTowardsChildrenEnum behaviourTowardsChildren : behaviourTowardsChildrenList) {

            applicantStopFromRespondentDoingToChildList.add(ApplicantStopFromRespondentDoingToChildEnum
                                                                .getDisplayedValueFromEnumString(String.valueOf(
                                                                    behaviourTowardsChildren)));

        }
        return applicantStopFromRespondentDoingToChildList;

    }

    private List<ApplicantStopFromRespondentDoingEnum> getBehaviourTowardsApplicant(CourtNavCaseData courtNavCaseData) {

        List<BehaviourTowardsApplicantEnum> behaviourTowardsApplicantList = courtNavCaseData.getStopBehaviourTowardsApplicant();
        List<ApplicantStopFromRespondentDoingEnum> applicantStopFromRespondentDoingList = new ArrayList<>();
        for (BehaviourTowardsApplicantEnum behaviourTowardsApplicant : behaviourTowardsApplicantList) {

            applicantStopFromRespondentDoingList.add(ApplicantStopFromRespondentDoingEnum
                                                         .getDisplayedValueFromEnumString(String.valueOf(
                                                             behaviourTowardsApplicant)));

        }
        return applicantStopFromRespondentDoingList;
    }

    private List<ReasonForOrderWithoutGivingNoticeEnum> getReasonForWithOutOrderNotice(CourtNavCaseData courtNavCaseData) {

        List<WithoutNoticeReasonEnum> withoutOrderReasonList = courtNavCaseData.getOrdersAppliedWithoutNoticeReason();
        log.info("cournav without order reson list: = {}", courtNavCaseData.getOrdersAppliedWithoutNoticeReason());
        List<ReasonForOrderWithoutGivingNoticeEnum> reasonForOrderWithoutGivingNoticeList = new ArrayList<>();
        for (WithoutNoticeReasonEnum withoutOrderReason : withoutOrderReasonList) {
            log.info("Actual reason: {}", withoutOrderReason);
            reasonForOrderWithoutGivingNoticeList.add(ReasonForOrderWithoutGivingNoticeEnum
                                                          .getDisplayedValueFromEnumString(String.valueOf(
                                                              withoutOrderReason)));
        }
        log.info("Convert enum values: {}", reasonForOrderWithoutGivingNoticeList);
        return reasonForOrderWithoutGivingNoticeList;
    }

    private String getCourtName(CaseData caseData) throws NotFoundException {
        court = courtFinderService.getNearestFamilyCourt(caseData);
        return court.getCourtName();
    }

    private String getCourtEmailAddress(Court court1) {

        Optional<CourtEmailAddress> courtEmailAddress = courtFinderService.getEmailAddress(court1);
        return String.valueOf(courtEmailAddress);
    }

    private List<Element<FL401Proceedings>> getOngoingProceedings(List<Element<CourtProceedings>> ongoingCourtProceedings) {

        List<Element<FL401Proceedings>> proceedingsList = new ArrayList<>();
        for (Element<CourtProceedings> courtProceedingsElement : ongoingCourtProceedings) {

            CourtProceedings proceedings = courtProceedingsElement.getValue();
            FL401Proceedings fl401Proceedings = FL401Proceedings.builder()
                .nameOfCourt(proceedings.getNameOfCourt())
                .caseNumber(proceedings.getCaseNumber())
                .typeOfCase(proceedings.getCaseType())
                .anyOtherDetails(proceedings.getCaseDetails())
                .build();
            proceedingsList.add(ElementUtils.element(fl401Proceedings));
        }
        return proceedingsList;

    }

    private List<Element<InterpreterNeed>> interpreterLanguageDetails(CourtNavCaseData courtNavCaseData) {

        InterpreterNeed interpreterNeed = InterpreterNeed.builder()
            .language(courtNavCaseData.getInterpreterLanguage())
            .otherAssistance(courtNavCaseData.getInterpreterDialect())
            .build();

        List<Element<InterpreterNeed>> interpreterNeedElement1 = List.of(
            ElementUtils.element(interpreterNeed));
        return interpreterNeedElement1;
    }

    private Home mapHomeDetails(CourtNavCaseData courtNavCaseData) {

        return Home.builder()
            .address(courtNavCaseData.getOccupationOrderAddress())
            .peopleLivingAtThisAddress(getPeopleLivingAtThisAddress(courtNavCaseData))
            .textAreaSomethingElse(courtNavCaseData.getCurrentlyLivesAtAddressOther())
            .everLivedAtTheAddress(YesNoBothEnum.valueOf(courtNavCaseData.getPreviouslyLivedAtAddress().getDisplayedValue()))
            .intendToLiveAtTheAddress(YesNoBothEnum.valueOf(courtNavCaseData.getIntendedToLiveAtAddress().getDisplayedValue()))
            .doAnyChildrenLiveAtAddress(YesOrNo.valueOf(null != courtNavCaseData.getChildrenApplicantResponsibility() ? "Yes" : "No"))
            .children(mapHomeChildren(courtNavCaseData.getChildrenApplicantResponsibility()))
            .isPropertyAdapted(courtNavCaseData.isPropertySpeciallyAdapted() ? YesOrNo.Yes : YesOrNo.No)
            .howIsThePropertyAdapted(courtNavCaseData.getPropertySpeciallyAdaptedDetails())
            .isThereMortgageOnProperty(courtNavCaseData.isPropertyHasMortgage() ? YesOrNo.Yes : YesOrNo.No)
            .mortgages(courtNavCaseData.isPropertyHasMortgage() ? (Mortgage.builder()
                .mortgageNamedAfter(getMortageDetails(courtNavCaseData))
                .textAreaSomethingElse(courtNavCaseData.getNamedOnMortgageOther())
                .mortgageLenderName(courtNavCaseData.getMortgageLenderName())
                .mortgageNumber(courtNavCaseData.getMortgageNumber())
                .address(courtNavCaseData.getLandlordAddress())
                .build()) : null)
            .isPropertyRented(courtNavCaseData.isPropertyIsRented() ? YesOrNo.Yes : YesOrNo.No)
            .landlords(courtNavCaseData.isPropertyIsRented() ? (Landlord.builder()
                .mortgageNamedAfterList(getLandlordDetails(courtNavCaseData))
                .textAreaSomethingElse(courtNavCaseData.getNamedOnRentalAgreementOther())
                .landlordName(courtNavCaseData.getLandlordName())
                .address(courtNavCaseData.getLandlordAddress())
                .build()) : null)
            .doesApplicantHaveHomeRights(courtNavCaseData.isHaveHomeRights() ? YesOrNo.Yes : YesOrNo.No)
            .livingSituation(getLivingSituationDetails(courtNavCaseData))
            .familyHome(getFamilyHomeDetails(courtNavCaseData))
            .build();

    }

    private List<FamilyHomeEnum> getFamilyHomeDetails(CourtNavCaseData courtNavCaseData) {

        List<FamilyHomeOutcomeEnum> familyHomeList = courtNavCaseData.getWantToHappenWithFamilyHome();
        List<FamilyHomeEnum> familyHomeEnumList = new ArrayList<>();
        for (FamilyHomeOutcomeEnum familyHome : familyHomeList) {
            familyHomeEnumList.add(FamilyHomeEnum
                                       .getDisplayedValueFromEnumString(String.valueOf(familyHome)));
        }
        return familyHomeEnumList;
    }


    private List<LivingSituationEnum> getLivingSituationDetails(CourtNavCaseData courtNavCaseData) {

        List<LivingSituationOutcomeEnum> livingSituationOutcomeList = courtNavCaseData.getWantToHappenWithLivingSituation();
        List<LivingSituationEnum> livingSituationList = new ArrayList<>();
        for (LivingSituationOutcomeEnum livingSituation : livingSituationOutcomeList) {
            livingSituationList.add(LivingSituationEnum
                                        .getDisplayedValueFromEnumString(String.valueOf(livingSituation)));
        }
        return livingSituationList;

    }

    private List<MortgageNamedAfterEnum> getLandlordDetails(CourtNavCaseData courtNavCaseData) {

        List<ContractEnum> contractList = courtNavCaseData.getNamedOnRentalAgreement();
        List<MortgageNamedAfterEnum> mortagageNameList = new ArrayList<>();
        for (ContractEnum contract : contractList) {
            mortagageNameList.add(MortgageNamedAfterEnum
                                      .getDisplayedValueFromEnumString(String.valueOf(contract)));
        }
        return mortagageNameList;
    }

    private List<MortgageNamedAfterEnum> getMortageDetails(CourtNavCaseData courtNavCaseData) {

        List<ContractEnum> contractList = courtNavCaseData.getNamedOnMortgage();
        List<MortgageNamedAfterEnum> mortagageNameList = new ArrayList<>();
        for (ContractEnum contract : contractList) {
            mortagageNameList.add(MortgageNamedAfterEnum
                                      .getDisplayedValueFromEnumString(String.valueOf(contract)));
        }
        return mortagageNameList;
    }

    private List<PeopleLivingAtThisAddressEnum> getPeopleLivingAtThisAddress(CourtNavCaseData courtNavCaseData) {

        List<CurrentResidentAtAddressEnum> currentlyLivesAtAddressList = courtNavCaseData.getCurrentlyLivesAtAddress();
        List<PeopleLivingAtThisAddressEnum> peopleLivingAtThisAddressList = new ArrayList<>();
        for (CurrentResidentAtAddressEnum currentlyLivesAtAddress : currentlyLivesAtAddressList) {

            peopleLivingAtThisAddressList.add(PeopleLivingAtThisAddressEnum
                                                  .getDisplayedValueFromEnumString(String.valueOf(
                                                      currentlyLivesAtAddress)));
        }
        return peopleLivingAtThisAddressList;
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

        List<Element<ApplicantChild>> childList = new ArrayList<>();
        for (Element<ProtectedChild> protectedChild : protectedChildren) {

            ProtectedChild value = protectedChild.getValue();
            ApplicantChild applicantChild = ApplicantChild.builder()
                .fullName(value.getFullName())
                .dateOfBirth(value.getDateOfBirth())
                .applicantChildRelationship(value.getRelationship())
                .applicantRespondentShareParental(value.isParentalResponsibility() ? YesOrNo.Yes : YesOrNo.No)
                .respondentChildRelationship(value.getRespondentRelationship())
                .build();
            childList.add(ElementUtils.element(applicantChild));
        }

        return childList;
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
            .applicantContactInstructions(null)
            .applicantPreferredContact(null)
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
