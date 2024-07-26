package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuarantineLegalDoc {
    private final String documentName;
    private final String notes;
    private final Document document;
    private final String documentType;
    private final String categoryId;
    private final String categoryName;
    private final List<RestrictToCafcassHmcts> restrictCheckboxCorrespondence;
    private final String documentParty;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime documentUploadedDate;
    //PRL-3564 manage documents categories to fields
    private final Document applicantApplicationDocument;
    private final Document applicantC1AApplicationDocument;
    private final Document applicantC1AResponseDocument;
    private final Document applicationsWithinProceedingsDocument;
    private final Document miamCertificateDocument;
    private final Document previousOrdersSubmittedWithApplicationDocument;
    private final Document fm5StatementsDocument;
    private final Document respondentApplicationDocument;
    private final Document ordersFromOtherProceedingsDocument;
    private final Document respondentC1AApplicationDocument;
    private final Document respondentC1AResponseDocument;
    private final Document applicationsFromOtherProceedingsDocument;
    private final Document noticeOfHearingDocument;
    private final Document courtBundleDocument;
    private final Document safeguardingLetterDocument;
    private final Document section7ReportDocument;
    private final Document section37ReportDocument;
    private final Document sixteenARiskAssessmentDocument; // 16aRiskAssessment
    private final Document guardianReportDocument;
    private final Document specialGuardianshipReportDocument;
    private final Document otherDocsDocument;
    private final Document confidentialDocument;
    private final Document emailsToCourtToRequestHearingsAdjournedDocument;
    private final Document publicFundingCertificatesDocument;
    private final Document noticesOfActingDischargeDocument;
    private final Document requestForFasFormsToBeChangedDocument; // requestForFASFormsToBeChanged
    private final Document witnessAvailabilityDocument;
    private final Document lettersOfComplaintDocument;
    private final Document spipReferralRequestsDocument; // SPIPReferralRequests
    private final Document homeOfficeDwpResponsesDocument; // homeOfficeDWPResponses
    private final Document internalCorrespondenceDocument;
    private final Document medicalReportsDocument;
    private final Document dnaReportsExpertReportDocument; // DNAReports_expertReport
    private final Document resultsOfHairStrandBloodTestsDocument;
    private final Document policeDisclosuresDocument;
    private final Document medicalRecordsDocument;
    private final Document drugAndAlcoholTestDocument; // drugAndAlcoholTest(toxicology)
    private final Document policeReportDocument;
    private final Document sec37ReportDocument;
    private final Document ordersSubmittedWithApplicationDocument;
    private final Document approvedOrdersDocument;
    private final Document standardDirectionsOrderDocument;
    private final Document transcriptsOfJudgementsDocument;
    private final Document magistratesFactsAndReasonsDocument;
    private final Document judgeNotesFromHearingDocument;
    private final Document importantInfoAboutAddressAndContactDocument;
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
    private final Document localAuthorityOtherDocDocument;
    private final Document pathfinderDocument;
    private final Document draftOrdersDocument;
    private final Document courtNavQuarantineDocument;

    // Adding Bulk scan attributes
    public final String fileName;
    public final String controlNumber;
    public final String type;
    public final String subtype;
    public final String exceptionRecordReference;
    public final Document url;
    public final LocalDateTime scannedDate;
    public final LocalDateTime deliveryDate;

    //PRL-4320 - manage docs redesign
    private final YesOrNo isConfidential;
    private final YesOrNo isRestricted;
    private final String restrictedDetails;
    private final String uploadedBy;
    private final String uploadedByIdamId;
    private final String uploaderRole;
    private final YesOrNo hasTheConfidentialDocumentBeenRenamed;

    // These fields are neeeded when Respondent solicitor uploads response on behalf of party
    private final String solicitorRepresentedPartyName;
    private final String solicitorRepresentedPartyId;
    private final String documentLanguage;

    //PRL-4306- Added confidential category in the exclusion list
    public static String[] quarantineCategoriesToRemove() {
        return new String [] {
            "citizenQuarantine", "legalProfQuarantine", "cafcassQuarantine", "courtStaffQuarantine", "confidential",
            "applicationsWithinProceedings", "applicationsFromOtherProceedings", "courtnavQuarantine"
        };
    }
}
