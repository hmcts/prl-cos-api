package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class QuarantineLegalDoc {
    private final String documentName;
    private final String notes;
    private final Document document;
    private final String documentType;
    private final String categoryId;
    private final String categoryName;
    private final List<RestrictToCafcassHmcts> restrictCheckboxCorrespondence;
    private final String documentParty;
    private final LocalDateTime documentUploadedDate;
    //PRL-3564 manage documents categories to fields
    private final Document applicantApplicationDocument;
    private final Document applicantC1AApplicationDocument;
    private final Document applicantC1AResponseDocument;
    private final Document applicationsWithinProceedingsDocument;
    private final Document miamCertificateDocument;
    private final Document previousOrdersSubmittedWithApplicationDocument;
    private final Document respondentApplicationDocument;
    private final Document respondentC1AApplicationDocument;
    private final Document respondentC1AResponseDocument;
    private final Document applicationsFromOtherProceedingsDocument;
    private final Document noticeOfHearingDocument;
    private final Document courtBundleDocument;
    private final Document safeguardingLetterDocument;
    private final Document section7ReportDocument;
    private final Document section37ReportDocument;
    private final Document sixteenARiskAssessmentDocument;
    private final Document guardianReportDocument;
    private final Document specialGuardianshipReportDocument;
    private final Document otherDocsDocument;
    private final Document confidentialDocument;
    private final Document emailsToCourtToRequestHearingsAdjournedDocument;
    private final Document publicFundingCertificatesDocument;
    private final Document noticesOfActingDischargeDocument;
    private final Document requestForFasFormsToBeChangedDocument;
    private final Document witnessAvailabilityDocument;
    private final Document lettersOfComplaintDocument;
    private final Document spipReferralRequestsDocument;
    private final Document homeOfficeDwpResponsesDocument;
    private final Document medicalReportsDocument;
    private final Document dnaReportsExpertReportDocument;
    private final Document resultsOfHairStrandBloodTestsDocument;
    private final Document policeDisclosuresDocument;
    private final Document medicalRecordsDocument;
    private final Document drugAndAlcoholTestDocument;
    private final Document policeReportDocument;
    private final Document sec37ReportDocument;
    private final Document ordersSubmittedWithApplicationDocument;
    private final Document approvedOrdersDocument;
    private final Document standardDirectionsOrderDocument;
    private final Document transcriptsOfJudgementsDocument;
    private final Document magistratesFactsAndReasonsDocument;
    private final Document judgeNotesFromHearingDocument;
    private final Document importantInfoAboutAddressAndContactDocument;
    private final Document dnaReportsOtherDocsDocument;
    private final Document privacyNoticeDocument;
    private final Document specialMeasuresDocument;
    private final Document anyOtherDocDocument;
    private final Document positionStatementsDocument;
    private final Document citizenQuarantineDocument;
    private final Document applicantStatementsDocument;
    private final Document respondentStatementsDocument;
    private final Document otherWitnessStatementsDocument;
    private final Document caseSummaryDocument;
    private final Document legalProfQuarantineDocument;
    private final Document cafcassQuarantineDocument;
    private final Document courtStaffQuarantineDocument;

    public static String[] quarantineCategoriesToRemove() {
        return new String [] {
            "citizenQuarantine", "legalProfQuarantine", "cafcassQuarantine", "courtStaffQuarantine"
        };
    }
}
