package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.DateOfSubmission;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.DateOfSubmissionGenerator;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DateOfSubmissionGeneratorTest {

    private final DateOfSubmissionGenerator generator = new DateOfSubmissionGenerator();

    @Test
    public void testGenerate() {
        LocalDateTime now = LocalDateTime.now();
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .dateSubmitted("2022-02-10")
                                                             .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .dateOfSubmission(DateOfSubmission.builder()
                                                                    .dateOfSubmission(
                                                                        CommonUtils.formatLocalDateTime(now)).build())
                                                               .build());

    }
}
