package uk.gov.hmcts.reform.prl.mapper.citizen;

import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.UrgentHearingReasonEnum;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildUrgencyElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper.COMMA_SEPARATOR;

public class CaseDataUrgencyElementsMapper {

    private CaseDataUrgencyElementsMapper() {
    }

    private static final String CASE_URGENCY_TIME = "Case Urgency Time - ";
    private static final String CASE_URGENCY_REASONS = " Case Urgency Reasons - ";

    public static void updateUrgencyElementsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                      C100RebuildUrgencyElements c100RebuildUrgencyElements) {
        caseDataBuilder
                .isCaseUrgent(c100RebuildUrgencyElements.getUrgentHearingRequired())
                .caseUrgencyTimeAndReason(buildCaseUrgencyTimeAndReason(c100RebuildUrgencyElements))
                .areRespondentsAwareOfProceedings(c100RebuildUrgencyElements.getHearingWithNext48HrsDetails())
                .effortsMadeWithRespondents(c100RebuildUrgencyElements.getHearingWithNext48HrsMsg());
    }

    private static String buildCaseUrgencyTimeAndReason(C100RebuildUrgencyElements c100RebuildUrgencyElements) {
        if (YesOrNo.Yes.equals(c100RebuildUrgencyElements.getUrgentHearingRequired())) {
            List<String> riskList = nonNull(c100RebuildUrgencyElements.getReasonOfUrgentHearing())
                ? List.of(c100RebuildUrgencyElements.getReasonOfUrgentHearing()) : Collections.emptyList();

            List<String> riskList1 = riskList.stream().map(value -> UrgentHearingReasonEnum.valueOf(value)
                .getDisplayedValue()).collect(Collectors.toList());

            if (isNotEmpty(c100RebuildUrgencyElements.getOtherRiskDetails())) {
                riskList1.add(c100RebuildUrgencyElements.getOtherRiskDetails());
            }
            return CASE_URGENCY_TIME + c100RebuildUrgencyElements.getTimeOfHearingDetails()
                + CASE_URGENCY_REASONS + String.join(COMMA_SEPARATOR, riskList1);
        }
        return null;
    }
}
