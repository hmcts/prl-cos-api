package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AddCaseNoteServiceTest {

    @InjectMocks
    private AddCaseNoteService addCaseNoteService;

    @InjectMocks
    private ObjectMapper objectMapper;

    @Test
    public void testPopulateHeader() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case Header Text")
            .build();

        Map<String, Object> responseMap = addCaseNoteService.populateHeader(caseData);
        assertEquals("Case Name: Test Case Header Text", responseMap.get("addCaseNoteHeader1Text"));
    }

    @Test
    public void testClearFields() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case Header Text")
            .subject("testSubject1")
            .caseNote("testCaseNote1")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated = objectMapper.convertValue(caseData, caseDataUpdated.getClass());

        addCaseNoteService.clearFields(caseDataUpdated);

        assertEquals(null, caseDataUpdated.get("subject"));
        assertEquals(null, caseDataUpdated.get("caseNote"));
    }

}
