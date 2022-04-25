package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
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

    public List<Element<CaseNoteDetails>> addCaseNoteDetails(CaseData caseData) {
        Element<CaseNoteDetails> caseNoteDetails = null;
        CaseNoteDetails currentOrderDetails = getCurrentOrderDetails(caseData);
        if(currentOrderDetails != null){
            caseNoteDetails = element(currentOrderDetails);
        }

        List<Element<CaseNoteDetails>> caseNotesCollection;

        if (caseData.getCaseNotes() != null) {
            caseNotesCollection = caseData.getCaseNotes();
            caseNotesCollection.add(caseNoteDetails);
        } else {
            caseNotesCollection = new ArrayList<>();
            caseNotesCollection.add(caseNoteDetails);
            //caseData.setCaseNotes(caseNotesCollection);
        }
        return caseNotesCollection;
    }

    private CaseNoteDetails getCurrentOrderDetails(CaseData caseData){
        String subject = caseData.getSubject();
        String caseNote = caseData.getCaseNote();
        if(subject == null && caseNote == null){
            return null;
        }
        return CaseNoteDetails.builder().subject(subject).caseNote(caseNote).build();
    }

    public void clearFields(Map<String, Object> caseDataUpdated) {
        caseDataUpdated.put("subject", null);
        caseDataUpdated.put("caseNote", null);
    }

    public Map<String, Object> populateHeader(CaseData caseData) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("addCaseNoteHeader1Text", getHeaderInfo(caseData));
        return headerMap;
    }

    private String getHeaderInfo(CaseData caseData) {
        StringBuilder headerInfo = new StringBuilder();
        headerInfo.append("Case Name: " + caseData.getApplicantCaseName());
        headerInfo.append("\n\n");
        return headerInfo.toString();
    }
}
