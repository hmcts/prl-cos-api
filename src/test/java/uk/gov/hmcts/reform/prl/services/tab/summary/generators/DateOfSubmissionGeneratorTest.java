package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.DateOfSubmission;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.DateOfSubmissionGenerator;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DateOfSubmissionGeneratorTest {

    private final DateOfSubmissionGenerator generator = new DateOfSubmissionGenerator();

    @Test
    public void testGenerate() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String format = DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime);
        String isoDateToSpecificFormat = CommonUtils.getIsoDateToSpecificFormat(format, "dd-MM-yyyy");
        String dateWithoutDash = isoDateToSpecificFormat.replaceAll("-", " ");
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .dateSubmitted(format)
                                                             .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .dateOfSubmission(DateOfSubmission.builder()
                                                                    .dateOfSubmission(dateWithoutDash
                                                                       )
                                                                    .build())
                                                               .build());

    }
}
