package uk.gov.hmcts.reform.prl.services.tab.summary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.TabService;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.AllegationOfHarmGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.AllegationOfHarmRevisedGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.AllocatedJudgeDetailsGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.CaseStatusGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.ConfidentialDetailsGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.DateOfSubmissionGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.FieldGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.OrderAppliedForGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.OtherProceedingsGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.SpecialArrangementsGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.TypeOfApplicationGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.UrgencyGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Qualifier("caseSummaryTab")
public class CaseSummaryTabService implements TabService {

    private final AllocatedJudgeDetailsGenerator allocatedJudgeDetailsGenerator;
    private final CaseStatusGenerator caseStatusGenerator;
    private final ConfidentialDetailsGenerator confidentialDetailsGenerator;
    private final OrderAppliedForGenerator orderAppliedForGenerator;
    private final OtherProceedingsGenerator otherProceedingsGenerator;
    private final SpecialArrangementsGenerator specialArrangementsGenerator;
    private final UrgencyGenerator urgencyGenerator;
    private final AllegationOfHarmGenerator allegationOfHarmGenerator;
    private final AllegationOfHarmRevisedGenerator allegationOfHarmRevisedGenerator;
    private final DateOfSubmissionGenerator dateOfSubmissionGenerator;
    private final ObjectMapper objectMapper;
    private final TypeOfApplicationGenerator typeOfApplicationGenerator;

    @Override
    public Map<String, Object> updateTab(CaseData caseData) {

        Map<String, Object> summaryTabFields = getGenerators(caseData).stream()
            .map(generator -> generator.generate(caseData))
            .flatMap(summary -> objectMapper.convertValue(
                    summary,
                    new TypeReference<Map<String, Object>>() {
                    }
                )
                .entrySet().stream())
            .collect(HashMap::new, (m, v) -> {
                Object value = m.getOrDefault(v.getKey(), null);
                if (Objects.isNull(value)) {
                    m.put(v.getKey(), v.getValue());
                }
            }, HashMap::putAll);

        // For Collection Fields, We should do manually since it should have element structure..
        CaseSummary caseSummary = otherProceedingsGenerator.generate(caseData);

        summaryTabFields.put("otherProceedingsForSummaryTab", otherProceedingsGenerator.getOtherProceedingsDetails(caseData));
        summaryTabFields.put("otherProceedingEmptyTable", caseSummary.getOtherProceedingEmptyTable());
        return summaryTabFields;
    }

    @Override
    public List<FieldGenerator> getGenerators(CaseData caseData) {
        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {

            return List.of(allocatedJudgeDetailsGenerator,
                    caseStatusGenerator, confidentialDetailsGenerator, urgencyGenerator, typeOfApplicationGenerator,
                    specialArrangementsGenerator, dateOfSubmissionGenerator);

        }

        return List.of(
                allocatedJudgeDetailsGenerator,
                caseStatusGenerator,
                confidentialDetailsGenerator,
                orderAppliedForGenerator,
                specialArrangementsGenerator,
                urgencyGenerator,
                TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
                        || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion()) ? allegationOfHarmRevisedGenerator
                        : allegationOfHarmGenerator,
                dateOfSubmissionGenerator
        );
    }

    @Override
    public void calEventToRefreshUI() {
        // no current implementation required.
    }
}
