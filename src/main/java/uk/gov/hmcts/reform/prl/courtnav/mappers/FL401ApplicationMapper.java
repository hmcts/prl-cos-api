package uk.gov.hmcts.reform.prl.courtnav.mappers;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants;
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
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoBothEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
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
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ApplicantsDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ChildAtAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtnavAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.Family;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ProtectedChild;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.RespondentDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.TheHome;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicationCoverEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsApplicantEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ConsentEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ContractEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.CurrentResidentAtAddressEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.FamilyHomeOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.LivingSituationOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SpecialMeasuresEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FL401ApplicationMapper {

    public static final String COURTNAV_DUMMY_BASE_LOCATION_ID = "234946";

    private final CourtFinderService courtFinderService;
    private final LaunchDarklyClient launchDarklyClient;
    private final LocationRefDataService locationRefDataService;

    private Court court = null;

    public CaseData mapCourtNavData(CourtNavFl401 courtNavCaseData, String authorization) throws NotFoundException {
        CaseData caseData = null;
        caseData = CaseData.builder()
            .isCourtNavCase(YesOrNo.Yes)
            .state(State.SUBMITTED_PAID)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .caseOrigin(courtNavCaseData.getMetaData().getCaseOrigin())
            .courtNavApproved(courtNavCaseData.getMetaData().isCourtNavApproved() ? YesOrNo.Yes : YesOrNo.No)
            .hasDraftOrder(courtNavCaseData.getMetaData().isHasDraftOrder() ? YesOrNo.Yes : YesOrNo.No)
            .numberOfAttachments(String.valueOf(courtNavCaseData.getMetaData().getNumberOfAttachments()))
            .specialCourtName(courtNavCaseData.getMetaData().getCourtSpecialRequirements())
            .applicantAge(ApplicantAge.getValue(String.valueOf(courtNavCaseData.getFl401().getBeforeStart().getApplicantHowOld())))
            .applicantCaseName(getCaseName(courtNavCaseData))
            .typeOfApplicationOrders(TypeOfApplicationOrders.builder()
                                         .orderType(courtNavCaseData.getFl401().getSituation().getOrdersAppliedFor())
                                         .build())
            .applicantChildDetails(getApplicantChildDetails(courtNavCaseData.getFl401().getFamily()))
            .orderWithoutGivingNoticeToRespondent(WithoutNoticeOrderDetails.builder()
                                                      .orderWithoutGivingNotice(courtNavCaseData
                                                                                    .getFl401()
                                                                                    .getSituation()
                                                                                    .isOrdersAppliedWithoutNotice()
                                                                                    ? YesOrNo.Yes : YesOrNo.No)
                                                      .build())
            .reasonForOrderWithoutGivingNotice(!courtNavCaseData.getFl401().getSituation().isOrdersAppliedWithoutNotice() ? null
                                                   : (ReasonForWithoutNoticeOrder.builder()
                .reasonForOrderWithoutGivingNotice(getReasonForWithOutOrderNotice(courtNavCaseData))
                .futherDetails(courtNavCaseData.getFl401().getSituation().getOrdersAppliedWithoutNoticeReasonDetails())
                .build()))
            .bailDetails(getRespondentBailConditionDetails(courtNavCaseData))
            .anyOtherDtailsForWithoutNoticeOrder(OtherDetailsOfWithoutNoticeOrder.builder()
                                                     .otherDetails(courtNavCaseData.getFl401().getSituation().getAdditionalDetailsForCourt())
                                                     .build())
            .applicantsFL401(mapApplicant(courtNavCaseData.getFl401().getApplicantDetails()))
            .respondentsFL401(mapRespondent(courtNavCaseData.getFl401().getRespondentDetails()))
            .applicantFamilyDetails(ApplicantFamilyDetails.builder()
                                        .doesApplicantHaveChildren(courtNavCaseData.getFl401().getFamily()
                                                                       .getWhoApplicationIsFor()
                                                                       .equals(ApplicationCoverEnum.applicantOnly)
                                                                       ? YesOrNo.No : YesOrNo.Yes)
                                        .build())
            .applicantChildDetails(!courtNavCaseData.getFl401().getFamily()
                .getWhoApplicationIsFor()
                .equals(ApplicationCoverEnum.applicantOnly)
                                       ? mapProtectedChild(courtNavCaseData.getFl401()
                                                               .getFamily().getProtectedChildren()) : null)
            .respondentBehaviourData(courtNavCaseData.getFl401().getSituation()
                                         .getOrdersAppliedFor().contains(FL401OrderTypeEnum.nonMolestationOrder)
                                         ? (RespondentBehaviour.builder()
                .applicantWantToStopFromRespondentDoing(getApplicantBehaviourList(courtNavCaseData))
                .applicantWantToStopFromRespondentDoingToChild(getChildrenBehaviourList(courtNavCaseData))
                .otherReasonApplicantWantToStopFromRespondentDoing(courtNavCaseData.getFl401()
                                                                       .getRespondentBehaviour().getStopBehaviourAnythingElse())
                .build()) : null)
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                          .applicantRelationship(ApplicantRelationshipEnum
                                                                     .getDisplayedValueFromEnumString(
                                                                         courtNavCaseData.getFl401()
                                                                             .getRelationshipWithRespondent()
                                                                             .getRelationshipDescription()
                                                                             .getId()))
                                          .build())
            .respondentRelationDateInfoObject((!courtNavCaseData.getFl401().getRelationshipWithRespondent()
                .getRelationshipDescription().getId()
                .equalsIgnoreCase("noneOfAbove"))
                                                  ? (RespondentRelationDateInfo.builder()
                .relationStartAndEndComplexType(RelationshipDateComplex.builder()
                                                    .relationshipDateComplexStartDate(LocalDate.parse(courtNavCaseData
                                                                                                          .getFl401()
                                                                                                          .getRelationshipWithRespondent()
                                                                                                          .getRelationshipStartDate()
                                                                                                          .mergeDate()))
                                                    .relationshipDateComplexEndDate(getRelationShipEndDate(courtNavCaseData))
                                                    .build())
                .applicantRelationshipDate(getRelationShipCeremonyDate(courtNavCaseData))
                .build()) : null)
            .respondentRelationOptions((courtNavCaseData
                .getFl401()
                .getRelationshipWithRespondent()
                .getRelationshipDescription().getId().equalsIgnoreCase(
                    "noneOfAbove"))
                                           ? (RespondentRelationOptionsInfo.builder()
                .applicantRelationshipOptions(courtNavCaseData.getFl401()
                                                  .getRelationshipWithRespondent().getRespondentsRelationshipToApplicant())
                .relationOptionsOther(courtNavCaseData.getFl401()
                                          .getRelationshipWithRespondent().getRespondentsRelationshipToApplicantOther())
                .build()) : null)
            .home(courtNavCaseData.getFl401().getSituation()
                      .getOrdersAppliedFor().contains(FL401OrderTypeEnum.occupationOrder)
                      ? mapHomeDetails(courtNavCaseData) : null)
            .fl401StmtOfTruth(StatementOfTruth.builder()
                                  .applicantConsent(courtNavCaseData.getFl401()
                                                        .getStatementOfTruth()
                                                        .getDeclaration().stream()
                                                        .map(ConsentEnum::getId)
                                                        .map(FL401Consent::getDisplayedValueFromEnumString)
                                                        .toList())
                                  .signature(courtNavCaseData.getFl401().getStatementOfTruth().getSignature())
                                  .fullname(courtNavCaseData.getFl401().getStatementOfTruth().getSignatureFullName())
                                  .date(LocalDate.parse(courtNavCaseData.getFl401().getStatementOfTruth().getSignatureDate().mergeDate()))
                                  .nameOfFirm(courtNavCaseData.getFl401().getStatementOfTruth().getRepresentativeFirmName())
                                  .signOnBehalf(courtNavCaseData.getFl401().getStatementOfTruth().getRepresentativePositionHeld())
                                  .build())
            .attendHearing(AttendHearing.builder()
                               .isInterpreterNeeded(Boolean.TRUE.equals(courtNavCaseData.getFl401().getGoingToCourt().getIsInterpreterRequired())
                                                        ? YesOrNo.Yes : YesOrNo.No)
                               .interpreterNeeds(interpreterLanguageDetails(courtNavCaseData))
                               .isDisabilityPresent(courtNavCaseData.getFl401().getGoingToCourt().isAnyDisabilityNeeds() ? YesOrNo.Yes : YesOrNo.No)
                               .adjustmentsRequired(courtNavCaseData.getFl401().getGoingToCourt().isAnyDisabilityNeeds()
                                                        ? courtNavCaseData.getFl401().getGoingToCourt().getDisabilityNeedsDetails() : null)
                               .isSpecialArrangementsRequired(null != courtNavCaseData.getFl401().getGoingToCourt().getAnySpecialMeasures()
                                                                  ? YesOrNo.Yes : YesOrNo.No)
                               .specialArrangementsRequired(null != courtNavCaseData.getFl401().getGoingToCourt().getAnySpecialMeasures()
                                                                ? (courtNavCaseData.getFl401().getGoingToCourt().getAnySpecialMeasures()
                                   .stream()
                                   .map(SpecialMeasuresEnum::getDisplayedValue)
                                   .collect(Collectors.joining(","))) : null)
                               .build())
            .fl401OtherProceedingDetails(getFl401OtherProceedingDetails(courtNavCaseData))
            .build();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
            .caseSubmittedTimeStamp(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime))
            .build();

        caseData = populateCourtDetailsForCourtNavCase(authorization, caseData,
                                                        courtNavCaseData.getMetaData().getCourtSpecialRequirements());
        caseData = caseData.setDateSubmittedDate();

        return caseData;

    }

    private List<Element<ApplicantChild>> getApplicantChildDetails(Family family) {
        if (!ObjectUtils.isEmpty(family) && !ObjectUtils.isEmpty(family.getProtectedChildren())) {
            List<Element<ApplicantChild>> childList = new ArrayList<>();
            family.getProtectedChildren().forEach(child ->
                childList.add(element(ApplicantChild.builder()
                    .fullName(child.getFullName())
                    .dateOfBirth(LocalDate.parse(child.getDateOfBirth().mergeDate()))
                    .respondentChildRelationship(child.getRespondentRelationship())
                    .applicantChildRelationship(child.getRelationship())
                    .applicantRespondentShareParental(child.isParentalResponsibility() ? YesOrNo.Yes : YesOrNo.No)
                    .build()))
            );
            return childList;
        }
        return Collections.emptyList();
    }

    private CaseData populateCourtDetailsForCourtNavCase(String authorization, CaseData caseData,
                                                          String epimsId) throws NotFoundException {
        Optional<CourtVenue> courtVenue = Optional.empty();
        //1. get court details from provided epimsId request
        if (!StringUtils.isEmpty(epimsId)) {
            courtVenue = getCourtVenue(authorization, epimsId);
        }
        //2. if not found check launch-darkly flag and populate default Swansea court Id.
        if (launchDarklyClient.isFeatureEnabled(PrlLaunchDarklyFlagConstants.COURTNAV_SWANSEA_COURT_MAPPING)
            && courtVenue.isEmpty()) {
            epimsId = COURTNAV_DUMMY_BASE_LOCATION_ID;
            courtVenue = getCourtVenue(authorization, epimsId);
        }
        //3. if court details found then populate court information and case management location.
        if (!courtVenue.isEmpty()) {
            caseData = caseData.toBuilder()
                .courtName(courtVenue.get().getCourtName())
                .caseManagementLocation(CaseManagementLocation.builder()
                                            .region(courtVenue.get().getRegionId())
                                            .baseLocation(epimsId)
                                            .regionName(courtVenue.get().getRegion())
                                            .baseLocationName(courtVenue.get().getCourtName()).build())
                .isCafcass(CaseUtils.cafcassFlag(courtVenue.get().getRegionId()))
                .courtId(epimsId)
                .build();
        } else {
            // 4. populate court details from fact-finder Api.
            caseData = caseData.toBuilder()
                .courtName(getCourtName(caseData))
                .courtEmailAddress(getCourtEmailAddress(court))
                .build();
        }
        return caseData;
    }

    private Optional<CourtVenue> getCourtVenue(String authToken, String epmsId) {
        Optional<CourtVenue> courtVenue;
        courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId(
            epmsId,
            authToken
        );
        return courtVenue;
    }

    private RespondentBailConditionDetails getRespondentBailConditionDetails(CourtNavFl401 courtNavCaseData) {
        return RespondentBailConditionDetails.builder()
            .isRespondentAlreadyInBailCondition(courtNavCaseData
                                                    .getFl401()
                                                    .getSituation().isBailConditionsOnRespondent()
                                                    ? YesNoDontKnow.yes : YesNoDontKnow.no)
            .bailConditionEndDate(courtNavCaseData.getFl401().getSituation().isBailConditionsOnRespondent()
                                      ? LocalDate.parse(courtNavCaseData
                                                            .getFl401()
                                                            .getSituation()
                                                            .getBailConditionsEndDate()
                                                            .mergeDate()) : null)
            .build();
    }

    private FL401OtherProceedingDetails getFl401OtherProceedingDetails(CourtNavFl401 courtNavCaseData) {
        return FL401OtherProceedingDetails.builder()
            .hasPrevOrOngoingOtherProceeding(courtNavCaseData.getFl401().getFamily().isAnyOngoingCourtProceedings()
                                                 ? YesNoDontKnow.yes : YesNoDontKnow.no)
            .fl401OtherProceedings(courtNavCaseData.getFl401().getFamily().isAnyOngoingCourtProceedings()
                                       ? getOngoingProceedings(courtNavCaseData.getFl401()
                                                                   .getFamily().getOngoingCourtProceedings()) : null)
            .build();
    }

    private List<ApplicantStopFromRespondentDoingToChildEnum> getChildrenBehaviourList(CourtNavFl401 courtNavCaseData) {

        return (null !=  courtNavCaseData.getFl401()
            .getRespondentBehaviour().getStopBehaviourTowardsChildren())
            ? getBehaviourTowardsChildren(courtNavCaseData) : null;
    }

    private List<ApplicantStopFromRespondentDoingEnum> getApplicantBehaviourList(CourtNavFl401 courtNavCaseData) {

        return (null !=  courtNavCaseData.getFl401()
            .getRespondentBehaviour().getStopBehaviourTowardsApplicant())
            ? getBehaviourTowardsApplicant(courtNavCaseData) : null;
    }


    private LocalDate getRelationShipCeremonyDate(CourtNavFl401 courtNavCaseData) {
        LocalDate cermonyDate = null;

        if (null != courtNavCaseData.getFl401().getRelationshipWithRespondent().getCeremonyDate()) {
            cermonyDate = LocalDate.parse(courtNavCaseData
                                              .getFl401()
                                              .getRelationshipWithRespondent()
                                              .getCeremonyDate().mergeDate());
        }
        return cermonyDate;
    }


    private LocalDate getRelationShipEndDate(CourtNavFl401 courtNavCaseData) {
        LocalDate endDate = null;

        if (null != courtNavCaseData.getFl401().getRelationshipWithRespondent().getRelationshipEndDate()) {
            endDate = LocalDate.parse(courtNavCaseData
                                          .getFl401()
                                          .getRelationshipWithRespondent()
                                          .getRelationshipEndDate()
                                          .mergeDate());
        }
        return endDate;
    }

    private String getCaseName(CourtNavFl401 courtNavCaseData) {

        String applicantName = courtNavCaseData.getFl401().getApplicantDetails().getApplicantFirstName() + " "
            + courtNavCaseData.getFl401().getApplicantDetails().getApplicantLastName();

        String respondentName = courtNavCaseData.getFl401().getRespondentDetails().getRespondentFirstName() + " "
            + courtNavCaseData.getFl401().getRespondentDetails().getRespondentLastName();

        return applicantName + " & " + respondentName;
    }

    private List<ApplicantStopFromRespondentDoingToChildEnum> getBehaviourTowardsChildren(CourtNavFl401 courtNavCaseData) {

        List<BehaviourTowardsChildrenEnum> behaviourTowardsChildrenList = courtNavCaseData
            .getFl401()
            .getRespondentBehaviour()
            .getStopBehaviourTowardsChildren();
        List<ApplicantStopFromRespondentDoingToChildEnum> applicantStopFromRespondentDoingToChildList = new ArrayList<>();
        for (BehaviourTowardsChildrenEnum behaviourTowardsChildren : behaviourTowardsChildrenList) {

            applicantStopFromRespondentDoingToChildList.add(ApplicantStopFromRespondentDoingToChildEnum
                                                                .getDisplayedValueFromEnumString(String.valueOf(
                                                                    behaviourTowardsChildren)));

        }
        return applicantStopFromRespondentDoingToChildList;

    }

    private List<ApplicantStopFromRespondentDoingEnum> getBehaviourTowardsApplicant(CourtNavFl401 courtNavCaseData) {

        List<BehaviourTowardsApplicantEnum> behaviourTowardsApplicantList = courtNavCaseData.getFl401()
            .getRespondentBehaviour().getStopBehaviourTowardsApplicant();
        List<ApplicantStopFromRespondentDoingEnum> applicantStopFromRespondentDoingList = new ArrayList<>();
        for (BehaviourTowardsApplicantEnum behaviourTowardsApplicant : behaviourTowardsApplicantList) {

            applicantStopFromRespondentDoingList.add(ApplicantStopFromRespondentDoingEnum
                                                         .getDisplayedValueFromEnumString(String.valueOf(
                                                             behaviourTowardsApplicant)));

        }
        return applicantStopFromRespondentDoingList;
    }

    private List<ReasonForOrderWithoutGivingNoticeEnum> getReasonForWithOutOrderNotice(CourtNavFl401 courtNavCaseData) {

        List<WithoutNoticeReasonEnum> withoutOrderReasonList = courtNavCaseData.getFl401()
            .getSituation().getOrdersAppliedWithoutNoticeReason();
        List<ReasonForOrderWithoutGivingNoticeEnum> reasonForOrderWithoutGivingNoticeList = new ArrayList<>();
        for (WithoutNoticeReasonEnum withoutOrderReason : withoutOrderReasonList) {
            reasonForOrderWithoutGivingNoticeList.add(ReasonForOrderWithoutGivingNoticeEnum
                                                          .getDisplayedValueFromEnumString(String.valueOf(
                                                              withoutOrderReason)));
        }
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

    private List<Element<FL401Proceedings>> getOngoingProceedings(List<CourtProceedings> ongoingCourtProceedings) {

        List<Element<FL401Proceedings>> fl401ProceedingList = new ArrayList<>();
        for (CourtProceedings courtProceedings : ongoingCourtProceedings) {
            FL401Proceedings f = FL401Proceedings.builder()
                .nameOfCourt(courtProceedings.getNameOfCourt())
                .caseNumber(courtProceedings.getCaseNumber())
                .typeOfCase(courtProceedings.getCaseType())
                .anyOtherDetails(courtProceedings.getCaseDetails())
                .build();
            fl401ProceedingList.add(element(f));
        }
        return fl401ProceedingList;

    }

    private List<Element<InterpreterNeed>> interpreterLanguageDetails(CourtNavFl401 courtNavCaseData) {

        InterpreterNeed interpreterNeed = InterpreterNeed.builder()
            .language(courtNavCaseData.getFl401().getGoingToCourt().getInterpreterLanguage())
            .otherAssistance(courtNavCaseData.getFl401().getGoingToCourt().getInterpreterDialect())
            .build();

        return List.of(
            ElementUtils.element(interpreterNeed));
    }

    private Home mapHomeDetails(CourtNavFl401 courtNavCaseData) {

        return Home.builder()
            .address(getAddress(courtNavCaseData.getFl401().getTheHome().getOccupationOrderAddress()))
            .peopleLivingAtThisAddress(getPeopleLivingAtThisAddress(courtNavCaseData))
            .textAreaSomethingElse(courtNavCaseData.getFl401().getTheHome().getCurrentlyLivesAtAddressOther())
            .everLivedAtTheAddress(null != courtNavCaseData.getFl401()
                .getTheHome()
                .getPreviouslyLivedAtAddress() ? YesNoBothEnum
                .getDisplayedValueFromEnumString(courtNavCaseData.getFl401()
                                                     .getTheHome()
                                                     .getPreviouslyLivedAtAddress().getId()) : null)
            .intendToLiveAtTheAddress(null != courtNavCaseData.getFl401()
                .getTheHome()
                .getIntendedToLiveAtAddress() ? YesNoBothEnum
                .getDisplayedValueFromEnumString(courtNavCaseData.getFl401()
                                                     .getTheHome()
                                                     .getIntendedToLiveAtAddress().getId()) : null)
            .doAnyChildrenLiveAtAddress(getAnyChildrenLivedAtAddress(courtNavCaseData))
            .children(getChildrenDetails(courtNavCaseData)
                          ? mapHomeChildren(courtNavCaseData.getFl401()
                                              .getTheHome()) : null)
            .isPropertyAdapted(courtNavCaseData.getFl401()
                                   .getTheHome().isPropertySpeciallyAdapted() ? YesOrNo.Yes : YesOrNo.No)
            .howIsThePropertyAdapted(courtNavCaseData.getFl401()
                                         .getTheHome().getPropertySpeciallyAdaptedDetails())
            .isThereMortgageOnProperty(courtNavCaseData.getFl401()
                                           .getTheHome().isPropertyHasMortgage() ? YesOrNo.Yes : YesOrNo.No)
            .mortgages(getMortgageMappingDetails(courtNavCaseData))
            .isPropertyRented(courtNavCaseData.getFl401().getTheHome().isPropertyIsRented() ? YesOrNo.Yes : YesOrNo.No)
            .landlords(getLandlordMappingDetails(courtNavCaseData))
            .doesApplicantHaveHomeRights(courtNavCaseData.getFl401().getTheHome().isHaveHomeRights() ? YesOrNo.Yes : YesOrNo.No)
            .livingSituation((null != courtNavCaseData.getFl401()
                .getTheHome().getWantToHappenWithLivingSituation())
                                 ? getLivingSituationDetails(courtNavCaseData) : null)
            .familyHome((null != courtNavCaseData.getFl401()
                .getTheHome().getWantToHappenWithFamilyHome())
                            ? getFamilyHomeDetails(courtNavCaseData) : null)
            .furtherInformation(courtNavCaseData.getFl401().getTheHome().getAnythingElseForCourtToConsider())
            .build();

    }

    private Landlord getLandlordMappingDetails(CourtNavFl401 courtNavCaseData) {
        Landlord landlord = null;
        if (courtNavCaseData.getFl401().getTheHome().isPropertyIsRented()) {
            landlord = Landlord.builder()
                .mortgageNamedAfterList((null != courtNavCaseData.getFl401()
                    .getTheHome().getNamedOnRentalAgreement())
                                            ? getLandlordDetails(courtNavCaseData) : null)
                .textAreaSomethingElse(courtNavCaseData.getFl401().getTheHome().getNamedOnRentalAgreementOther())
                .landlordName(courtNavCaseData.getFl401().getTheHome().getLandlordName())
                .address(getAddress(courtNavCaseData.getFl401().getTheHome().getLandlordAddress()))
                .build();
        }
        return landlord;
    }

    private Mortgage getMortgageMappingDetails(CourtNavFl401 courtNavCaseData) {
        Mortgage mortgage = null;

        if (courtNavCaseData.getFl401().getTheHome().isPropertyHasMortgage()) {
            mortgage = Mortgage.builder()
                .mortgageNamedAfter((null != courtNavCaseData.getFl401()
                    .getTheHome().getNamedOnMortgage())
                                        ? getMortageDetails(courtNavCaseData) : null)
                .textAreaSomethingElse(courtNavCaseData.getFl401().getTheHome().getNamedOnMortgageOther())
                .mortgageLenderName(courtNavCaseData.getFl401().getTheHome().getMortgageLenderName())
                .mortgageNumber(courtNavCaseData.getFl401().getTheHome().getMortgageNumber())
                .address(getAddress(courtNavCaseData.getFl401().getTheHome().getLandlordAddress()))
                .build();
        }
        return mortgage;

    }

    private boolean getChildrenDetails(CourtNavFl401 courtNavCaseData) {

        return (null != courtNavCaseData.getFl401().getTheHome().getChildrenApplicantResponsibility()
            || null != courtNavCaseData.getFl401().getTheHome().getChildrenSharedResponsibility());

    }

    private YesOrNo getAnyChildrenLivedAtAddress(CourtNavFl401 courtNavCaseData) {
        return (null != courtNavCaseData.getFl401().getTheHome()
            .getChildrenApplicantResponsibility())
            || (null != courtNavCaseData.getFl401().getTheHome()
            .getChildrenSharedResponsibility())
            ? YesOrNo.Yes : YesOrNo.No;
    }

    private Address getAddress(CourtnavAddress courtnavAddress) {

        Address address = null;
        if (null != courtnavAddress) {
            address = Address.builder()
                .addressLine1(courtnavAddress.getAddressLine1())
                .addressLine2(courtnavAddress.getAddressLine2())
                .addressLine3(courtnavAddress.getAddressLine3())
                .postTown(courtnavAddress.getPostTown())
                .postCode(courtnavAddress.getPostCode())
                .county(courtnavAddress.getCounty())
                .country(courtnavAddress.getCountry())
                .build();
        }
        return address;
    }

    private List<FamilyHomeEnum> getFamilyHomeDetails(CourtNavFl401 courtNavCaseData) {

        List<FamilyHomeOutcomeEnum> familyHomeList = courtNavCaseData.getFl401().getTheHome().getWantToHappenWithFamilyHome();
        List<FamilyHomeEnum> familyHomeEnumList = new ArrayList<>();
        for (FamilyHomeOutcomeEnum familyHome : familyHomeList) {
            familyHomeEnumList.add(FamilyHomeEnum
                                       .getDisplayedValueFromEnumString(String.valueOf(familyHome)));
        }
        return familyHomeEnumList;
    }


    private List<LivingSituationEnum> getLivingSituationDetails(CourtNavFl401 courtNavCaseData) {

        List<LivingSituationOutcomeEnum> livingSituationOutcomeList = courtNavCaseData.getFl401().getTheHome().getWantToHappenWithLivingSituation();
        List<LivingSituationEnum> livingSituationList = new ArrayList<>();
        for (LivingSituationOutcomeEnum livingSituation : livingSituationOutcomeList) {
            livingSituationList.add(LivingSituationEnum
                                        .getDisplayedValueFromEnumString(String.valueOf(livingSituation)));
        }
        return livingSituationList;

    }

    private List<MortgageNamedAfterEnum> getLandlordDetails(CourtNavFl401 courtNavCaseData) {

        List<ContractEnum> contractList = courtNavCaseData.getFl401().getTheHome().getNamedOnRentalAgreement();
        List<MortgageNamedAfterEnum> mortagageNameList = new ArrayList<>();
        for (ContractEnum contract : contractList) {
            mortagageNameList.add(MortgageNamedAfterEnum
                                      .getDisplayedValueFromEnumString(String.valueOf(contract)));
        }
        return mortagageNameList;
    }

    private List<MortgageNamedAfterEnum> getMortageDetails(CourtNavFl401 courtNavCaseData) {

        List<ContractEnum> contractList = courtNavCaseData.getFl401().getTheHome().getNamedOnMortgage();
        List<MortgageNamedAfterEnum> mortagageNameList = new ArrayList<>();
        for (ContractEnum contract : contractList) {
            mortagageNameList.add(MortgageNamedAfterEnum
                                      .getDisplayedValueFromEnumString(String.valueOf(contract)));
        }
        return mortagageNameList;
    }

    private List<PeopleLivingAtThisAddressEnum> getPeopleLivingAtThisAddress(CourtNavFl401 courtNavCaseData) {

        List<CurrentResidentAtAddressEnum> currentlyLivesAtAddressList = courtNavCaseData.getFl401()
            .getTheHome().getCurrentlyLivesAtAddress();
        List<PeopleLivingAtThisAddressEnum> peopleLivingAtThisAddressList = new ArrayList<>();
        for (CurrentResidentAtAddressEnum currentlyLivesAtAddress : currentlyLivesAtAddressList) {

            peopleLivingAtThisAddressList.add(PeopleLivingAtThisAddressEnum
                                                  .getDisplayedValueFromEnumString(String.valueOf(
                                                      currentlyLivesAtAddress)));
        }
        return peopleLivingAtThisAddressList;
    }

    private List<Element<ChildrenLiveAtAddress>> mapHomeChildren(TheHome theHome) {

        List<Element<ChildrenLiveAtAddress>> childList = new ArrayList<>();

        List<ChildAtAddress> childrenApplicantResponsibility = theHome.getChildrenApplicantResponsibility();
        List<ChildAtAddress> childrenSharedResponsibility = theHome.getChildrenSharedResponsibility();

        if (null != childrenSharedResponsibility) {
            for (ChildAtAddress child : childrenSharedResponsibility) {
                ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
                    .keepChildrenInfoConfidential(YesOrNo.No)
                    .childFullName(child.getFullName())
                    .childsAge(String.valueOf(child.getAge()))
                    .isRespondentResponsibleForChild(YesOrNo.Yes)
                    .build();
                childList.add(element(childrenLiveAtAddress));
            }
        }

        if (null != childrenApplicantResponsibility) {
            for (ChildAtAddress child : childrenApplicantResponsibility) {
                ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
                    .keepChildrenInfoConfidential(YesOrNo.No)
                    .childFullName(child.getFullName())
                    .childsAge(String.valueOf(child.getAge()))
                    .isRespondentResponsibleForChild(YesOrNo.No)
                    .build();
                childList.add(element(childrenLiveAtAddress));
            }
        }
        return childList;
    }

    private List<Element<ApplicantChild>> mapProtectedChild(List<ProtectedChild> protectedChildren) {

        List<Element<ApplicantChild>> applicantChild = new ArrayList<>();
        for (ProtectedChild protectedChild : protectedChildren) {
            ApplicantChild a = ApplicantChild.builder()
                .fullName(protectedChild.getFullName())
                .dateOfBirth(LocalDate.parse(protectedChild.getDateOfBirth().mergeDate()))
                .applicantChildRelationship(protectedChild.getRelationship())
                .applicantRespondentShareParental(protectedChild.isParentalResponsibility() ? YesOrNo.Yes : YesOrNo.No)
                .respondentChildRelationship(protectedChild.getRespondentRelationship())
                .build();
            applicantChild.add(element(a));
        }

        return applicantChild;
    }

    private PartyDetails mapRespondent(RespondentDetails respondent) {
        return PartyDetails.builder()
            .firstName(respondent.getRespondentFirstName())
            .lastName(respondent.getRespondentLastName())
            .previousName(respondent.getRespondentOtherNames())
            .dateOfBirth(null != respondent.getRespondentDateOfBirth()
                        ? LocalDate.parse(respondent.getRespondentDateOfBirth().mergeDate()) : null)
            .isDateOfBirthKnown(YesOrNo.valueOf(null != respondent.getRespondentDateOfBirth() ? "Yes" : "No"))
            .email(respondent.getRespondentEmailAddress())
            .canYouProvideEmailAddress(YesOrNo.valueOf(null != respondent.getRespondentEmailAddress() ? "Yes" : "No"))
            .phoneNumber(respondent.getRespondentPhoneNumber())
            .canYouProvidePhoneNumber(YesOrNo.valueOf(null != respondent.getRespondentPhoneNumber() ? "Yes" : "No"))
            .address(null != respondent.getRespondentAddress()
                         ? getAddress(respondent.getRespondentAddress()) : null)
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
            .dateOfBirth(LocalDate.parse(applicant.getApplicantDateOfBirth().mergeDate()))
            .gender(Gender.getDisplayedValueFromEnumString(applicant.getApplicantGender().getId()))
            .otherGender(applicant.getApplicantGender().getDisplayedValue().equals("other")
                             ? applicant.getApplicantGenderOther() : null)
            .address(null != applicant.getApplicantAddress()
                         ? getAddress(applicant.getApplicantAddress()) : null)
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
            .solicitorOrg(Organisation.builder()
                              .organisationName(applicant.getLegalRepresentativeFirm())
                              .build())
            .solicitorEmail(applicant.getLegalRepresentativeEmail())
            .solicitorAddress(null != applicant.getLegalRepresentativeAddress()
                                  ? getAddress(applicant.getLegalRepresentativeAddress()) : null)
            .dxNumber(applicant.getLegalRepresentativeDx())
            .build();
    }
}
