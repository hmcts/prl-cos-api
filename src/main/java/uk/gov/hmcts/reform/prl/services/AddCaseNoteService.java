package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

/**
 * This class is added only as a java service example. It can be deleted when more services is added.
 */
@Component
@RequiredArgsConstructor
public class AddCaseNoteService {

    public static final String ADD_CASE_NOTE_HEADER_1_TEXT = "addCaseNoteHeader1Text";
    public static final String SUBJECT = "subject";
    public static final String CASE_NOTE = "caseNote";
    public static final String CASE_NAME = "Case Name: ";

    public List<Element<CaseNoteDetails>> addCaseNoteDetails(CaseData caseData,  UserDetails userDetails) {
        List<Element<CaseNoteDetails>> caseNotesCollection = null;

        CaseNoteDetails currentCaseNoteDetails = getCurrentCaseNoteDetails(caseData, userDetails);
        if(currentCaseNoteDetails != null){
            Element<CaseNoteDetails>  caseNoteDetails = element(currentCaseNoteDetails);
            if (caseData.getCaseNotes() != null) {
                caseNotesCollection = caseData.getCaseNotes();
                caseNotesCollection.add(caseNoteDetails);
            } else {
                caseNotesCollection = new ArrayList<>();
                caseNotesCollection.add(caseNoteDetails);
            }
        }

        return caseNotesCollection;
    }

    private CaseNoteDetails getCurrentCaseNoteDetails(CaseData caseData, UserDetails userDetails){

        return CaseNoteDetails.builder()
            .subject(caseData.getSubject())
            .caseNote(caseData.getCaseNote())
            .user(userDetails.getFullName())
            .dateAdded(LocalDate.now().toString())
            .build();
    }

    public void clearFields(Map<String, Object> caseDataUpdated) {
        caseDataUpdated.put(SUBJECT, null);
        caseDataUpdated.put(CASE_NOTE, null);
    }

    public Map<String, Object> populateHeader(CaseData caseData) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(ADD_CASE_NOTE_HEADER_1_TEXT, getHeaderInfo(caseData));
        return headerMap;
    }

    private String getHeaderInfo(CaseData caseData) {
        StringBuilder headerInfo = new StringBuilder();
        headerInfo.append(CASE_NAME + caseData.getApplicantCaseName());
        return headerInfo.toString();
    }
}
