package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;


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

    @Test
    public void testAddCaseNoteDetails() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .subject("newsubject")
            .caseNote("newcasenote")
            .build();

        UserDetails userDetails = UserDetails.builder()
            .forename("forename1")
            .surname("surname1")
            .id("userid1234")
            .email("test@gmail.com")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated = objectMapper.convertValue(caseData, caseDataUpdated.getClass());
        caseDataUpdated.put("caseNotes", addCaseNoteService.addCaseNoteDetails(caseData, userDetails));

        List<Element<CaseNoteDetails>> list1 = getCaseNotesFromCase(caseDataUpdated);

        assertEquals("newsubject",list1.get(0).getValue().getSubject());
        assertEquals("newcasenote",list1.get(0).getValue().getCaseNote());
        assertEquals("forename1 surname1",list1.get(0).getValue().getUser());
    }

    @Test
    public void testAddCaseNoteDetailsUpdated() {
        CaseNoteDetails caseNoteDetails = CaseNoteDetails.builder()
            .subject("subject1234")
            .caseNote("casenote1234")
            .user("user1234")
            .dateAdded("26 Apr 2022")
            .build();

        Element<CaseNoteDetails> wrappedCaseNotes = Element.<CaseNoteDetails>builder().value(caseNoteDetails).build();
        List<Element<CaseNoteDetails>> listOfCaseNotes = new ArrayList<>();
        listOfCaseNotes.add(wrappedCaseNotes);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .subject("newsubject1234")
            .caseNote("newcasenote1234")
            .caseNotes(listOfCaseNotes)
            .build();

        UserDetails userDetails = UserDetails.builder()
            .forename("forename1")
            .surname("surname1")
            .id("userid1234")
            .email("test@gmail.com")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated = objectMapper.convertValue(caseData, caseDataUpdated.getClass());
        caseDataUpdated.put("caseNotes", addCaseNoteService.addCaseNoteDetails(caseData, userDetails));

        List<Element<CaseNoteDetails>> list1 = getCaseNotesFromCase(caseDataUpdated);

        assertEquals("newsubject1234",list1.get(1).getValue().getSubject());
        assertEquals("newcasenote1234",list1.get(1).getValue().getCaseNote());
        assertEquals("forename1 surname1",list1.get(1).getValue().getUser());
    }

    @Nullable
    private List<Element<CaseNoteDetails>> getCaseNotesFromCase(Map<String, Object> caseDataUpdated) {
        List<Element<CaseNoteDetails>> list1;
        list1 = (List<Element<CaseNoteDetails>>) caseDataUpdated.compute("caseNotes", (key, value) -> {
            if (value != null) {
                return value;
            } else {
                return null;
            }
        });
        return list1;
    }
}
