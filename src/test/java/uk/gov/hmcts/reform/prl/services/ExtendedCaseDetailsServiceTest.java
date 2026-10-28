package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtendedCaseDetailsServiceTest {
    @InjectMocks
    ExtendedCaseDetailsService extendedCaseDetailsService;
    @Mock
    FormattedTitleDisplayService formattedTitleDisplayService;

    @Test
    void shouldSetExtendedDetails() {
        HashMap caseDataUpdated = new HashMap();
        String appCaseName = "appCaseName";
        String caseNameHmctsInternal = "caseNameHmctsInternal";
        String familymanCaseNumber = "familymanCaseNumber";
        Long id = 1234567890123456L;
        LocalDate issueDate = LocalDate.of(2026, 10, 31);
        CaseData caseData = CaseData.builder().applicantCaseName(appCaseName)
            .caseNameHmctsInternal(caseNameHmctsInternal)
            .familymanCaseNumber(familymanCaseNumber)
            .id(id)
            .issueDate(issueDate)
            .build();

        String expectedTitle = "some new title";
        when(formattedTitleDisplayService.getFormattedTitleDisplay(
            appCaseName,
            caseNameHmctsInternal,
            familymanCaseNumber,
            id,
            issueDate
        )).thenReturn(expectedTitle);

        extendedCaseDetailsService.setExtendedDetails(caseData, caseDataUpdated);
        assertThat(caseDataUpdated.get("INJECTED_DATA")).isNotNull();
        assertThat(((HashMap) caseDataUpdated.get("INJECTED_DATA")).get("extendedCaseDetailsTitle")).isEqualTo(
            expectedTitle);


    }
}
