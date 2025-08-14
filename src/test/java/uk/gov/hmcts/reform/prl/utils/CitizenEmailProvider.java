package uk.gov.hmcts.reform.prl.utils;

import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmail;

import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_PETITIONER_NAME;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_RESPONDENT_NAME;

@NoArgsConstructor
public class CitizenEmailProvider {

    public static CitizenEmail empty() {
        return builder().build();
    }

    public static CitizenEmail full() {
        return of(TEST_CASE_ID, TEST_PETITIONER_NAME, TEST_RESPONDENT_NAME);
    }

    public static CitizenEmail of(String caseId) {
        return builder().caseReference(caseId).build();
    }

    public static CitizenEmail of(String caseId, String petitionerName, String respondentName) {
        return builder()
            .caseReference(caseId)
            .petitionerName(petitionerName)
            .respondentName(respondentName)
            .build();
    }

    private static CitizenEmail.CitizenEmailBuilder builder() {
        return CitizenEmail.builder();
    }
}
