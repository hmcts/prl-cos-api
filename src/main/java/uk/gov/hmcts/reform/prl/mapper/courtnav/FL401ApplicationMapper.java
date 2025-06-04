package uk.gov.hmcts.reform.prl.mapper.courtnav;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
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
import java.util.ArrayList;
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

        caseData = caseData.setDateSubmittedDate();

        return caseData;
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
