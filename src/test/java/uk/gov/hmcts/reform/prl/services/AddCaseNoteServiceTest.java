package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
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
        assertEquals("Case Name: Test Case Header Text", responseMap.get("addCaseNoteHeaderCaseNameText"));
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

    @Test
    public void testaddCaseNotes() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case Header Text")
            .subject("testSubject1")
            .caseNote("testCaseNote1")
            .build();

        UserDetails userDetails = UserDetails.builder().forename("forename").surname("surname").build();

        List<Element<CaseNoteDetails>> result = addCaseNoteService.addCaseNoteDetails(caseData, userDetails);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("testSubject1", result.get(0).getValue().getSubject());
        Assertions.assertEquals("testCaseNote1", result.get(0).getValue().getCaseNote());
        Assertions.assertEquals("forename surname", result.get(0).getValue().getUser());
    }

    @Test
    public void testaddCaseNotesWithExistingCaseNotes() {
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

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("testSubject1", result.get(0).getValue().getSubject());
        Assertions.assertEquals("forename surname", result.get(0).getValue().getUser());
    }

    @Test
    public void testGetCurrentCaseNoteDetails() {
        UserDetails userDetails = UserDetails.builder().forename("forename").surname("surname").build();
        CaseNoteDetails result = addCaseNoteService.getCurrentCaseNoteDetails(
            "testSubject1",
            "testCaseNote1",
            userDetails
        );
        Assertions.assertEquals("testSubject1", result.getSubject());
        Assertions.assertEquals("testCaseNote1", result.getCaseNote());
    }

}
