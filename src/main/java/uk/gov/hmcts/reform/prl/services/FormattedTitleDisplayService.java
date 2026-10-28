package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FormattedTitleDisplayService {
    private final FeatureToggleService featureToggleService;

    public static final String FORMATTED_TITLE_DISPLAY_BEFORE_RELEASE =
        "## %s\n ## FamilyMan ID: %s\n ## Casenumber: %s";
    public static final String FORMATTED_TITLE_DISPLAY_AFTER_RELEASE =
        "## %s\n ## Casenumber: %s";

    public String getFormattedTitleDisplay(String applicantCaseName, String caseNameHmctsInternal,
                                           String familymanCaseNumber, Long caseRef,
                                           LocalDate issueDate) {

        if (featureToggleService.isCaseIssuedAfterReleaseDate(issueDate)) {
            return String.format(FORMATTED_TITLE_DISPLAY_AFTER_RELEASE, caseNameHmctsInternal, caseRef);
        } else {
            return String.format(
                FORMATTED_TITLE_DISPLAY_BEFORE_RELEASE,
                applicantCaseName,
                familymanCaseNumber,
                caseRef
            );
        }
    }
}
