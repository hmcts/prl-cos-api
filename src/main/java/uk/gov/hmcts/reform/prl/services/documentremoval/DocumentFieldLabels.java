package uk.gov.hmcts.reform.prl.services.documentremoval;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DocumentFieldLabels {
    private static final Map<String, String> FIELD_LABELS;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("sodAdditionalDocumentsList", "Upload additional documents");
        map.put("contactOrderDocumentsUploaded", "Contact order");
        map.put("c8FormDocumentsUploaded", "C8 form");
        map.put("otherDocumentsUploaded", "Other documents");
        map.put("legalProfQuarantineDocsList", "Legal professional uploaded documents");
        map.put("citizenUploadQuarantineDocsList", "Citizen uploaded documents");
        map.put("citizenQuarantineDocsList", "Citizen uploaded quarantine documents");
        map.put("cafcassQuarantineDocsList", "Cafcass uploaded documents");
        map.put("courtStaffQuarantineDocsList", "Court staff uploaded documents");
        map.put("tempQuarantineDocumentList", "Temparary QuarantineList");
        map.put("courtNavQuarantineDocumentList", "CourtNav uploaded documents");
        map.put("pd36qLetter", "PD36ZE letter");
        map.put("noticeOfSafetySupportLetter", "Upload notice of safety letter");
        map.put("specialArrangementsLetter", "Special arrangements letter");
        map.put("additionalDocuments", "Additional documents");
        map.put("additionalDocumentsList", "Upload additional documents");
        map.put("finalServedApplicationDetailsList", "Print and email notifications");
        map.put("soaDocumentDynamicListForLa", "Document");
        map.put("refugeDocuments", "Refuge documents");
        map.put("historicalRefugeDocuments", "Historical refuge documents");
        map.put("c1ADocument", "C1A Document");
        map.put("c1AWelshDocument", "C1A Document (Welsh)");
        map.put("c1ADraftDocument", "C1A Draft Document");
        map.put("c1AWelshDraftDocument", "C1A Draft Document (Welsh)");
        map.put("draftOrderCollection", "Draft orders");
        map.put("courtNavUploadedDocs", "CourtNav uploaded documents");
        map.put("listWithoutNoticeHearingDetails", "Hearing");
        map.put("dioPartiesRaisedAbuseCollection", "Name of party raising domestic abuse issue");
        map.put("dioDisclosureOfPapersCaseNumbers", " ");
        map.put("dioApplicationToApplyPermission", "Edit your selection");
        map.put("dioDisclosureOtherDetails", "Give details");
        map.put("sdoFurtherDirectionDetails", "Add a new direction");
        map.put("sdoWitnessStatementsCheckDetails", " ");
        map.put("sdoInstructionsFilingDetails", " ");
        map.put("sdoMedicalDiscApplicantName", "Name of applicant");
        map.put("sdoMedicalDiscRespondentName", "Name of respondent");
        map.put("sdoMedicalDiscFilingDetails", " ");
        map.put("sdoGpApplicantName", "Name of applicant");
        map.put("sdoGpRespondentName", "Name of respondent");
        map.put("sdoLetterFromGpDetails", " ");
        map.put("sdoLsApplicantName", "Name of applicant");
        map.put("sdoLsRespondentName", "Name of respondent");
        map.put("sdoLetterFromSchoolDetails", " ");
        map.put("sdoScheduleOfAllegationsDetails", " ");
        map.put("sdoDisClosureProceedingDetails", " ");
        map.put("sdoPartyToProvideDetails", " ");
        map.put("sdoNewPartnersToCafcassDetails", " ");
        map.put("sdoSection7CheckDetails", " ");
        map.put("sdoFactFindingOtherDetails", " ");
        map.put("sdoInterpreterOtherDetails", " ");
        map.put("sdoCafcassFileAndServeDetails", " ");
        map.put("safeguardingCafcassCymruDetails", " ");
        map.put("sdoPositionStatementOtherDetails", " ");
        map.put("sdoMiamOtherDetails", " ");
        map.put("sdoLocalAuthorityDetails", "Give details");
        map.put("sdoTransferCourtDetails", " ");
        map.put("sdoCrossExaminationCourtDetails", " ");
        map.put("c8Document", "C8 Document");
        map.put("c8WelshDocument", "C8 Document (Welsh)");
        map.put("c8WelshDraftDocument", "C8 Draft Document (Welsh)");
        map.put("c8DraftDocument", "C8 Draft Document");
        map.put("c8ArchivedDocument", "C8 Archived Document");
        // ...existing code...
        FIELD_LABELS = Collections.unmodifiableMap(map);
    }

    public static String getLabelForField(String fieldName) {
        // First, try exact match
        if (FIELD_LABELS.containsKey(fieldName)) {
            return FIELD_LABELS.get(fieldName);
        }
        // If not found, try to find a key contained within the fieldName
        for (String key : FIELD_LABELS.keySet()) {
            if (fieldName != null && fieldName.contains(key)) {
                return FIELD_LABELS.get(key);
            }
        }
        // Fallback to returning the fieldName itself
        return fieldName;
    }
}
