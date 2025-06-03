package uk.gov.hmcts.reform.prl.mapper.courtnav;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBailConditionDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicationCoverEnum;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.CourtSealFinderService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Component
@RequiredArgsConstructor
public class FL401ApplicationMapper {

    public static final String COURTNAV_DUMMY_BASE_LOCATION_ID = "234946";

    private final CourtFinderService courtFinderService;
    private final LaunchDarklyClient launchDarklyClient;
    private final LocationRefDataService locationRefDataService;
    private final CourtSealFinderService courtSealFinderService;
    private final CourtNavApplicantMapper courtNavApplicantMapper;
    private final CourtNavRespondentMapper courtNavRespondentMapper;
    private final CourtNavHomeMapper courtNavHomeMapper;
    private final ApplicantChildMapper applicantChildMapper;
    private final InterpreterNeedsMapper interpreterNeedsMapper;
    private final OrderWithoutNoticeMapper orderWithoutNoticeMapper;
    private final ApplicantRelationshipMapper applicantRelationshipMapper;
    private final RespondentBehaviourMapper respondentBehaviourMapper;
    private final StatementOfTruthMapper statementOfTruthMapper;
    private final AttendHearingMapper attendHearingMapper;

    private Court court = null;

    public CaseData mapCourtNavData(CourtNavFl401 courtNavCaseData, String authorization) throws NotFoundException {
        CaseData caseData = CaseData.builder()
            .isCourtNavCase(Yes)
            .state(State.SUBMITTED_PAID)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .caseOrigin(courtNavCaseData.getMetaData().getCaseOrigin())
            .courtNavApproved(courtNavCaseData.getMetaData().isCourtNavApproved() ? Yes : No)
            .hasDraftOrder(courtNavCaseData.getMetaData().isHasDraftOrder() ? Yes : No)
            .numberOfAttachments(String.valueOf(courtNavCaseData.getMetaData().getNumberOfAttachments()))
            .specialCourtName(courtNavCaseData.getMetaData().getCourtSpecialRequirements())
            .applicantAge(ApplicantAge.getValue(String.valueOf(courtNavCaseData.getFl401().getBeforeStart().getApplicantHowOld())))
            .applicantCaseName(getCaseName(courtNavCaseData))
            .typeOfApplicationOrders(TypeOfApplicationOrders.builder()
                                         .orderType(courtNavCaseData.getFl401().getSituation().getOrdersAppliedFor())
                                         .build())
            .orderWithoutGivingNoticeToRespondent(orderWithoutNoticeMapper.map(courtNavCaseData))
            .reasonForOrderWithoutGivingNotice(orderWithoutNoticeMapper.mapReasonForWithoutNotice(courtNavCaseData))
            .anyOtherDtailsForWithoutNoticeOrder(orderWithoutNoticeMapper.mapOtherDetails(courtNavCaseData))
            .bailDetails(getRespondentBailConditionDetails(courtNavCaseData))
            .applicantsFL401(courtNavApplicantMapper.map(courtNavCaseData.getFl401().getApplicantDetails()))
            .respondentsFL401(courtNavRespondentMapper.map(courtNavCaseData.getFl401().getCourtNavRespondent()))
            .applicantFamilyDetails(ApplicantFamilyDetails.builder()
                                        .doesApplicantHaveChildren(courtNavCaseData.getFl401().getFamily()
                                                                       .getWhoApplicationIsFor()
                                                                       .equals(ApplicationCoverEnum.applicantOnly)
                                                                       ? No : Yes)
                                        .build())
            .applicantChildDetails(!courtNavCaseData.getFl401().getFamily()
                .getWhoApplicationIsFor().equals(ApplicationCoverEnum.applicantOnly)
                                       ? applicantChildMapper.map(
                                           courtNavCaseData.getFl401().getFamily().getProtectedChildren()) : null)
            .respondentBehaviourData(respondentBehaviourMapper.map(courtNavCaseData))
            .respondentRelationObject(applicantRelationshipMapper.mapRelationType(courtNavCaseData))
            .respondentRelationDateInfoObject(applicantRelationshipMapper.mapRelationDates(courtNavCaseData))
            .respondentRelationOptions(applicantRelationshipMapper.mapRelationOptions(courtNavCaseData))
            .home(courtNavCaseData.getFl401().getCourtNavHome() != null
                      ? courtNavHomeMapper.map(courtNavCaseData.getFl401().getCourtNavHome())
                      : null)
            .fl401StmtOfTruth(statementOfTruthMapper.map(courtNavCaseData))
            .attendHearing(attendHearingMapper.map(courtNavCaseData)
                               .toBuilder()
                               .interpreterNeeds(interpreterNeedsMapper.map(courtNavCaseData))
                               .build())
            .fl401OtherProceedingDetails(getFl401OtherProceedingDetails(courtNavCaseData))
            .build();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
            .caseSubmittedTimeStamp(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime))
            .daApplicantContactInstructions(CaseUtils.getContactInstructions(caseData.getApplicantsFL401()))
            //PRL-6951 - Fix to display case type, applicant name, respondent name in case list table(XUI)
            .selectedCaseTypeID(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantName(caseData.getApplicantsFL401().getLabelForDynamicList())
            .respondentName(caseData.getRespondentsFL401().getLabelForDynamicList())
            .build();

        caseData = populateCourtDetailsForCourtNavCase(authorization, caseData,
                                                        courtNavCaseData.getMetaData().getCourtSpecialRequirements());
        caseData = caseData.setDateSubmittedDate();

        return caseData;

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
        if (courtVenue.isPresent()) {
            caseData = caseData.toBuilder()
                .courtName(courtVenue.get().getCourtName())
                .caseManagementLocation(CaseManagementLocation.builder()
                                            .region(courtVenue.get().getRegionId())
                                            .baseLocation(epimsId)
                                            .regionName(courtVenue.get().getRegion())
                                            .baseLocationName(courtVenue.get().getCourtName()).build())
                .isCafcass(CaseUtils.cafcassFlag(courtVenue.get().getRegionId()))
                .courtId(epimsId)
                .courtSeal(courtSealFinderService.getCourtSeal(courtVenue.get().getRegionId()))
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

    private String getCaseName(CourtNavFl401 courtNavCaseData) {

        String applicantName = courtNavCaseData.getFl401().getApplicantDetails().getFirstName() + " "
            + courtNavCaseData.getFl401().getApplicantDetails().getLastName();

        String respondentName = courtNavCaseData.getFl401().getCourtNavRespondent().getFirstName() + " "
            + courtNavCaseData.getFl401().getCourtNavRespondent().getLastName();

        return applicantName + " & " + respondentName;
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

}
