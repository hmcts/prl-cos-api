package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class AddCaseNoteServiceTest {

    @InjectMocks
    private AddCaseNoteService addCaseNoteService;

    @InjectMocks
    private ObjectMapper objectMapper;

    @Test
    void testPopulateHeader() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case Header Text")
            .build();

        Map<String, Object> responseMap = addCaseNoteService.populateHeader(caseData);
        assertEquals("Case Name: Test Case Header Text", responseMap.get("addCaseNoteHeaderCaseNameText"));
    }

    @Test
    void testClearFields() {
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

        assertNull(caseDataUpdated.get("subject"));
        assertNull(caseDataUpdated.get("caseNote"));
    }

    @Test
    void testAddCaseNotes() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case Header Text")
            .subject("testSubject1")
            .caseNote("testCaseNote1")
            .build();

        UserDetails userDetails = UserDetails.builder().forename("forename").surname("surname").build();

        List<Element<CaseNoteDetails>> result = addCaseNoteService.addCaseNoteDetails(caseData, userDetails);

        assertEquals(1, result.size());
        assertEquals("testSubject1", result.getFirst().getValue().getSubject());
        assertEquals("testCaseNote1", result.getFirst().getValue().getCaseNote());
        assertEquals("forename surname", result.getFirst().getValue().getUser());
    }

    @Test
    void testAddCaseNotesWithExistingCaseNotes() {
        List<Element<CaseNoteDetails>> caseNotes = new ArrayList<>();
        caseNotes.add(ElementUtils.element(CaseNoteDetails.builder().dateCreated(LocalDateTime.now()).build()));
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case Header Text")
            .subject("testSubject1")
            .caseNotes(caseNotes)
            .build();

        UserDetails userDetails = UserDetails.builder().forename("forename").surname("surname").build();

        List<Element<CaseNoteDetails>> result = addCaseNoteService.addCaseNoteDetails(caseData, userDetails);

        assertEquals(2, result.size());
        assertEquals("testSubject1", result.getFirst().getValue().getSubject());
        assertEquals("forename surname", result.getFirst().getValue().getUser());
    }

    @Test
    void testGetCurrentCaseNoteDetails() {
        UserDetails userDetails = UserDetails.builder().forename("forename").surname("surname").build();
        CaseNoteDetails result = addCaseNoteService.getCurrentCaseNoteDetails(
            "testSubject1",
            "testCaseNote1",
            userDetails
        );
        assertEquals("testSubject1", result.getSubject());
        assertEquals("testCaseNote1", result.getCaseNote());
    }
}
