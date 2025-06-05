package uk.gov.hmcts.reform.prl.mapper.courtnav;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBailConditionDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicationCoverEnum;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Component
@RequiredArgsConstructor
public class FL401ApplicationMapper {

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

    public CaseData mapCourtNavData(CourtNavFl401 courtNavCaseData) {
        CaseData caseData = CaseData.builder()
            .isCourtNavCase(Yes)
            .state(State.SUBMITTED_PAID)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .caseOrigin(courtNavCaseData.getMetaData().getCaseOrigin())
            .courtNavApproved(courtNavCaseData.getMetaData().isCourtNavApproved() ? Yes : No)
            .hasDraftOrder(courtNavCaseData.getMetaData().isHasDraftOrder() ? Yes : No)
            .numberOfAttachments(String.valueOf(courtNavCaseData.getMetaData().getNumberOfAttachments()))
            .specialCourtName(courtNavCaseData.getMetaData().getCourtSpecialRequirements())
            .applicantAge(buildApplicantAge(courtNavCaseData))
            .applicantCaseName(buildCaseName(courtNavCaseData))
            .typeOfApplicationOrders(buildTypeOfApplicationOrders(courtNavCaseData))
            .orderWithoutGivingNoticeToRespondent(orderWithoutNoticeMapper.map(courtNavCaseData))
            .reasonForOrderWithoutGivingNotice(orderWithoutNoticeMapper.mapReasonForWithoutNotice(courtNavCaseData))
            .anyOtherDtailsForWithoutNoticeOrder(orderWithoutNoticeMapper.mapOtherDetails(courtNavCaseData))
            .bailDetails(buildBailConditionDetails(courtNavCaseData))
            .applicantsFL401(courtNavApplicantMapper.map(courtNavCaseData.getFl401().getApplicantDetails()))
            .respondentsFL401(courtNavRespondentMapper.map(courtNavCaseData.getFl401().getCourtNavRespondent()))
            .applicantFamilyDetails(buildApplicantFamilyDetails(courtNavCaseData))
            .applicantChildDetails(buildApplicantChildDetails(courtNavCaseData))
            .respondentBehaviourData(respondentBehaviourMapper.map(courtNavCaseData))
            .respondentRelationObject(applicantRelationshipMapper.mapRelationType(courtNavCaseData))
            .respondentRelationDateInfoObject(applicantRelationshipMapper.mapRelationDates(courtNavCaseData))
            .respondentRelationOptions(applicantRelationshipMapper.mapRelationOptions(courtNavCaseData))
            .home(mapHomeIfPresent(courtNavCaseData))
            .fl401StmtOfTruth(statementOfTruthMapper.map(courtNavCaseData))
            .attendHearing(attendHearingMapper.map(courtNavCaseData)
                               .toBuilder()
                               .interpreterNeeds(interpreterNeedsMapper.map(courtNavCaseData))
                               .build())
            .fl401OtherProceedingDetails(buildOtherProceedings(courtNavCaseData))
            .build();

        return enrichWithCaseMetadata(caseData);
    }

    private ApplicantAge buildApplicantAge(CourtNavFl401 source) {
        return ApplicantAge.getValue(String.valueOf(
            source.getFl401().getBeforeStart().getApplicantHowOld()
        ));
    }

    private String buildCaseName(CourtNavFl401 source) {
        String applicantName = source.getFl401().getApplicantDetails().getFirstName() + " " +
            source.getFl401().getApplicantDetails().getLastName();
        String respondentName = source.getFl401().getCourtNavRespondent().getFirstName() + " " +
            source.getFl401().getCourtNavRespondent().getLastName();
        return applicantName + " & " + respondentName;
    }

    private TypeOfApplicationOrders buildTypeOfApplicationOrders(CourtNavFl401 source) {
        return TypeOfApplicationOrders.builder()
            .orderType(source.getFl401().getSituation().getOrdersAppliedFor())
            .build();
    }

    private ApplicantFamilyDetails buildApplicantFamilyDetails(CourtNavFl401 source) {
        boolean hasChildren = !ApplicationCoverEnum.applicantOnly.equals(
            source.getFl401().getFamily().getWhoApplicationIsFor()
        );
        return ApplicantFamilyDetails.builder()
            .doesApplicantHaveChildren(hasChildren ? Yes : No)
            .build();
    }

    private List<Element<ApplicantChild>> buildApplicantChildDetails(CourtNavFl401 source) {
        return !ApplicationCoverEnum.applicantOnly.equals(source.getFl401().getFamily().getWhoApplicationIsFor())
            ? applicantChildMapper.map(source.getFl401().getFamily().getProtectedChildren())
            : null;
    }

    private RespondentBailConditionDetails buildBailConditionDetails(CourtNavFl401 source) {
        boolean hasConditions = source.getFl401().getSituation().isBailConditionsOnRespondent();
        return RespondentBailConditionDetails.builder()
            .isRespondentAlreadyInBailCondition(hasConditions ? YesNoDontKnow.yes : YesNoDontKnow.no)
            .bailConditionEndDate(hasConditions
                                      ? LocalDate.parse(source.getFl401().getSituation().getBailConditionsEndDate().mergeDate())
                                      : null)
            .build();
    }

    private Home mapHomeIfPresent(CourtNavFl401 source) {
        return source.getFl401().getCourtNavHome() != null
            ? courtNavHomeMapper.map(source.getFl401().getCourtNavHome())
            : null;
    }

    private FL401OtherProceedingDetails buildOtherProceedings(CourtNavFl401 source) {
        boolean hasProceedings = source.getFl401().getFamily().isAnyOngoingCourtProceedings();
        return FL401OtherProceedingDetails.builder()
            .hasPrevOrOngoingOtherProceeding(hasProceedings ? YesNoDontKnow.yes : YesNoDontKnow.no)
            .fl401OtherProceedings(hasProceedings
                                       ? mapProceedings(source.getFl401().getFamily().getOngoingCourtProceedings())
                                       : null)
            .build();
    }

    private List<Element<FL401Proceedings>> mapProceedings(List<CourtProceedings> proceedings) {
        return proceedings.stream()
            .map(p -> element(FL401Proceedings.builder()
                                  .nameOfCourt(p.getNameOfCourt())
                                  .caseNumber(p.getCaseNumber())
                                  .typeOfCase(p.getCaseType())
                                  .anyOtherDetails(p.getCaseDetails())
                                  .build()))
            .toList();
    }

    private CaseData enrichWithCaseMetadata(CaseData caseData) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        return caseData.toBuilder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
            .caseSubmittedTimeStamp(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime))
            .daApplicantContactInstructions(CaseUtils.getContactInstructions(caseData.getApplicantsFL401()))
            .selectedCaseTypeID(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantName(caseData.getApplicantsFL401().getLabelForDynamicList())
            .respondentName(caseData.getRespondentsFL401().getLabelForDynamicList())
            .build()
            .setDateSubmittedDate();
    }
}
