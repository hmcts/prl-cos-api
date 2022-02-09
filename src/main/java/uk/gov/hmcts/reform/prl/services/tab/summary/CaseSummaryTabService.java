package uk.gov.hmcts.reform.prl.services.tab.summary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.TabService;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.AllegationOfHarmGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.AllocatedJudgeDetailsGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.CaseStatusGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.ConfidentialDetailsGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.DateOfSubmissionGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.FieldGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.OrderAppliedForGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.OtherProceedingsGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.SpecialArrangementsGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.UrgencyGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Qualifier("caseSummaryTab")
public class CaseSummaryTabService implements TabService {

    @Autowired
    AllocatedJudgeDetailsGenerator allocatedJudgeDetailsGenerator;

    @Autowired
    CaseStatusGenerator caseStatusGenerator;

    @Autowired
    ConfidentialDetailsGenerator confidentialDetailsGenerator;

    @Autowired
    OrderAppliedForGenerator orderAppliedForGenerator;

    @Autowired
    OtherProceedingsGenerator otherProceedingsGenerator;

    @Autowired
    SpecialArrangementsGenerator specialArrangementsGenerator;

    @Autowired
    UrgencyGenerator urgencyGenerator;

    @Autowired
    AllegationOfHarmGenerator allegationOfHarmGenerator;

    @Autowired
    DateOfSubmissionGenerator dateOfSubmissionGenerator;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public Map<String, Object> updateTab(CaseData caseData) {
        Map<String, Object> summaryTabFields = getGenerators().stream()
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
    public List<FieldGenerator> getGenerators() {
        return List.of(allocatedJudgeDetailsGenerator,
                       caseStatusGenerator, confidentialDetailsGenerator,
                       orderAppliedForGenerator,
                       specialArrangementsGenerator, urgencyGenerator, allegationOfHarmGenerator,dateOfSubmissionGenerator);
    }
}
