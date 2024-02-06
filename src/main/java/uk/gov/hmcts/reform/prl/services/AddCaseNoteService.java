package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.time.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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

    public static final String ADD_CASE_NOTE_HEADER_1_TEXT = "addCaseNoteHeaderCaseNameText";
    public static final String SUBJECT = "subject";
    public static final String CASE_NOTE = "caseNote";
    public static final String CASE_NAME = "Case Name: ";
    private final Time dateTime;

    public List<Element<CaseNoteDetails>> addCaseNoteDetails(CaseData caseData,  UserDetails userDetails) {
        CaseNoteDetails currentCaseNoteDetails = getCurrentCaseNoteDetails(caseData, userDetails);
        return getCaseNoteDetails(caseData, currentCaseNoteDetails);
    }

    public List<Element<CaseNoteDetails>> getCaseNoteDetails(CaseData caseData, CaseNoteDetails currentCaseNoteDetails) {
        List<Element<CaseNoteDetails>> caseNotesCollection = null;
        if (currentCaseNoteDetails != null) {
            Element<CaseNoteDetails>  caseNoteDetails = element(currentCaseNoteDetails);
            if (caseData.getCaseNotes() != null) {
                caseNotesCollection = caseData.getCaseNotes();
                caseNotesCollection.add(caseNoteDetails);
            } else {
                caseNotesCollection = new ArrayList<>();
                caseNotesCollection.add(caseNoteDetails);
            }
            caseNotesCollection.sort(Comparator.comparing(m -> m.getValue().getDateCreated(), Comparator.reverseOrder()));
        }
        return caseNotesCollection;
    }

    private CaseNoteDetails getCurrentCaseNoteDetails(CaseData caseData, UserDetails userDetails) {
        return CaseNoteDetails.builder()
            .subject(caseData.getSubject())
            .caseNote(caseData.getCaseNote())
            .user(userDetails.getFullName())
            .dateAdded(LocalDate.now().toString())
            .dateCreated(LocalDateTime.now())
            .build();
    }

    public CaseNoteDetails getCurrentCaseNoteDetails(String subject, String caseNote, UserDetails userDetails) {
        return CaseNoteDetails.builder()
            .subject(subject)
            .caseNote(caseNote)
            .user(userDetails.getFullName())
            .dateAdded(LocalDate.now().toString())
            .dateCreated(LocalDateTime.now())
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
