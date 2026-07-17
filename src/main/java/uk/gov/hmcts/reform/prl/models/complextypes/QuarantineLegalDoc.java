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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuarantineLegalDoc {
    @CCD(label = "Document name", searchable = false)
    private final String documentName;
    @CCD(label = "Notes", searchable = false)
    private final String notes;
    @CCD(label = "Document", categoryID = "legalProfQuarantine", searchable = false)
    private final Document document;
    @CCD(label = "Document ID", showCondition = "document = \"DO_NOT_SHOW\"", searchable = false)
    private final String originalDocumentId;
    @CCD(label = "Document type", searchable = false)
    private final String documentType;
    @CCD(label = "Category ID", showCondition = "categoryId=\"DO_NOT_SHOW\"", searchable = false)
    private final String categoryId;
    @CCD(label = "Document category", searchable = false)
    private final String categoryName;
    @CCD(label = "Restrict to group", searchable = false)
    private final List<RestrictToCafcassHmcts> restrictCheckboxCorrespondence;
    @CCD(label = "Submitted by", searchable = false)
    private final String documentParty;
    @CCD(label = "Submitted date", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime documentUploadedDate;
    //PRL-3564 manage documents categories to fields
    @CCD(label = "Document", categoryID = "applicantApplication", searchable = false)
    private final Document applicantApplicationDocument;
    @CCD(label = "Document", categoryID = "applicantC1AApplication", searchable = false)
    private final Document applicantC1AApplicationDocument;
    @CCD(label = "Document", categoryID = "applicantC1AResponse", searchable = false)
    private final Document applicantC1AResponseDocument;
    @CCD(label = "Document", categoryID = "applicationsWithinProceedings", searchable = false)
    private final Document applicationsWithinProceedingsDocument;
    @CCD(label = "Document", categoryID = "MIAMCertificate", searchable = false)
    private final Document miamCertificateDocument;
    @CCD(label = "Document", categoryID = "previousOrdersSubmittedWithApplication", searchable = false)
    private final Document previousOrdersSubmittedWithApplicationDocument;
    @CCD(label = "Document", categoryID = "fm5Statements", searchable = false)
    private final Document fm5StatementsDocument;
    @CCD(label = "Document", categoryID = "respondentApplication", searchable = false)
    private final Document respondentApplicationDocument;
    @CCD(label = "Document", categoryID = "ordersFromOtherProceedings", searchable = false)
    private final Document ordersFromOtherProceedingsDocument;
    @CCD(label = "Document", categoryID = "respondentC1AApplication", searchable = false)
    private final Document respondentC1AApplicationDocument;
    @CCD(label = "Document", categoryID = "respondentC1AResponse", searchable = false)
    private final Document respondentC1AResponseDocument;
    @CCD(label = "Document", categoryID = "applicationsFromOtherProceedings", searchable = false)
    private final Document applicationsFromOtherProceedingsDocument;
    @CCD(label = "Document", categoryID = "noticeOfHearing", searchable = false)
    private final Document noticeOfHearingDocument;
    @CCD(label = "Document", categoryID = "courtBundle", searchable = false)
    private final Document courtBundleDocument;
    @CCD(label = "Document", categoryID = "safeguardingLetter", searchable = false)
    private final Document safeguardingLetterDocument;
    @CCD(label = "Document", categoryID = "childImpactReport1", searchable = false)
    private final Document childImpactReport1Document;
    @CCD(label = "Document", categoryID = "childImpactReport2", searchable = false)
    private final Document childImpactReport2Document;
    @CCD(label = "Document", categoryID = "section7Report", searchable = false)
    private final Document section7ReportDocument;
    @CCD(label = "Document", categoryID = "section37Report", searchable = false)
    private final Document section37ReportDocument;
    @CCD(label = "Document", categoryID = "16aRiskAssessment", searchable = false)
    private final Document sixteenARiskAssessmentDocument; // 16aRiskAssessment
    @CCD(label = "Document", categoryID = "cirTransferRequest", searchable = false)
    private final Document cirTransferRequestDocument;
    @CCD(label = "Document", categoryID = "cirExtensionRequest", searchable = false)
    private final Document cirExtensionRequestDocument;
    @CCD(label = "Document", categoryID = "guardianReport", searchable = false)
    private final Document guardianReportDocument;
    @CCD(label = "Document", categoryID = "specialGuardianshipReport", searchable = false)
    private final Document specialGuardianshipReportDocument;
    @CCD(label = "Document", categoryID = "otherDocs", searchable = false)
    private final Document otherDocsDocument;
    @CCD(label = "Document", categoryID = "confidential", searchable = false)
    private final Document confidentialDocument;
    @CCD(label = "Document", categoryID = "emailsToCourtToRequestHearingsAdjourned", searchable = false)
    private final Document emailsToCourtToRequestHearingsAdjournedDocument;
    @CCD(label = "Document", categoryID = "publicFundingCertificates", searchable = false)
    private final Document publicFundingCertificatesDocument;
    @CCD(label = "Document", categoryID = "noticesOfActingDischarge", searchable = false)
    private final Document noticesOfActingDischargeDocument;
    @CCD(label = "Document", categoryID = "requestForFASFormsToBeChanged", searchable = false)
    private final Document requestForFasFormsToBeChangedDocument; // requestForFASFormsToBeChanged
    @CCD(label = "Document", categoryID = "witnessAvailability", searchable = false)
    private final Document witnessAvailabilityDocument;
    @CCD(label = "Document", categoryID = "lettersOfComplaint", searchable = false)
    private final Document lettersOfComplaintDocument;
    @CCD(label = "Document", categoryID = "SPIPReferralRequests", searchable = false)
    private final Document spipReferralRequestsDocument; // SPIPReferralRequests
    @CCD(label = "Document", categoryID = "homeOfficeDWPResponses", searchable = false)
    private final Document homeOfficeDwpResponsesDocument; // homeOfficeDWPResponses
    @CCD(label = "Document", categoryID = "internalCorrespondence", searchable = false)
    private final Document internalCorrespondenceDocument;
    @CCD(label = "Document", categoryID = "medicalReports", searchable = false)
    private final Document medicalReportsDocument;
    @CCD(label = "Document", categoryID = "DNAReports_expertReport", searchable = false)
    private final Document dnaReportsExpertReportDocument; // DNAReports_expertReport
    @CCD(label = "Document", categoryID = "resultsOfHairStrandBloodTests", searchable = false)
    private final Document resultsOfHairStrandBloodTestsDocument;
    @CCD(label = "Document", categoryID = "policeDisclosures", searchable = false)
    private final Document policeDisclosuresDocument;
    @CCD(label = "Document", categoryID = "medicalRecords", searchable = false)
    private final Document medicalRecordsDocument;
    @CCD(label = "Document", categoryID = "drugAndAlcoholTest(toxicology)", searchable = false)
    private final Document drugAndAlcoholTestDocument; // drugAndAlcoholTest(toxicology)
    @CCD(label = "Document", categoryID = "policeReport", searchable = false)
    private final Document policeReportDocument;
    @CCD(label = "Document", categoryID = "sec37Report", searchable = false)
    private final Document sec37ReportDocument;
    @CCD(label = "Document", categoryID = "childImpactReport1La", searchable = false)
    private final Document childImpactReport1LaDocument;
    @CCD(label = "Document", categoryID = "childImpactReport2La", searchable = false)
    private final Document childImpactReport2LaDocument;
    @CCD(label = "Document", categoryID = "section7ReportLa", searchable = false)
    private final Document section7ReportLaDocument;
    @CCD(label = "Document", categoryID = "section7AddendumReportLa", searchable = false)
    private final Document section7AddendumReportLaDocument;
    @CCD(label = "Document", categoryID = "localAuthorityInvolvementLa", searchable = false)
    private final Document localAuthorityInvolvementLaDocument;
    @CCD(label = "Document", categoryID = "section47La", searchable = false)
    private final Document section47LaDocument;
    @CCD(label = "Document", categoryID = "cirExtensionRequestLa", searchable = false)
    private final Document cirExtensionRequestLaDocument;
    @CCD(label = "Document", categoryID = "cirTransferRequestLa", searchable = false)
    private final Document cirTransferRequestLaDocument;
    @CCD(label = "Document", categoryID = "draftOrders", searchable = false)
    private final Document ordersSubmittedWithApplicationDocument;
    @CCD(label = "Document", categoryID = "approvedOrders", searchable = false)
    private final Document approvedOrdersDocument;
    @CCD(label = "Document", categoryID = "draftOrders", searchable = false)
    private final Document standardDirectionsOrderDocument;
    @CCD(label = "Document", categoryID = "transcriptsOfJudgements", searchable = false)
    private final Document transcriptsOfJudgementsDocument;
    @CCD(label = "Document", categoryID = "magistratesFactsAndReasons", searchable = false)
    private final Document magistratesFactsAndReasonsDocument;
    @CCD(label = "Document", categoryID = "judgeNotesFromHearing", searchable = false)
    private final Document judgeNotesFromHearingDocument;
    @CCD(label = "Document", categoryID = "importantInfoAboutAddressAndContact", searchable = false)
    private final Document importantInfoAboutAddressAndContactDocument;
    @CCD(label = "Document", categoryID = "privacyNotice", searchable = false)
    private final Document privacyNoticeDocument;
    @CCD(label = "Document", categoryID = "specialMeasures", searchable = false)
    private final Document specialMeasuresDocument;
    @CCD(label = "Document", categoryID = "anyOtherDoc", searchable = false)
    private final Document anyOtherDocDocument;
    @CCD(label = "Document", categoryID = "positionStatements", searchable = false)
    private final Document positionStatementsDocument;
    @CCD(label = "Document", categoryID = "citizenQuarantine", searchable = false)
    private final Document citizenQuarantineDocument;
    @CCD(label = "Document", categoryID = "applicantStatements", searchable = false)
    private final Document applicantStatementsDocument;
    @CCD(label = "Document", categoryID = "respondentStatements", searchable = false)
    private final Document respondentStatementsDocument;
    @CCD(label = "Document", categoryID = "otherWitnessStatements", searchable = false)
    private final Document otherWitnessStatementsDocument;
    @CCD(label = "Document", categoryID = "caseSummary", searchable = false)
    private final Document caseSummaryDocument;
    @CCD(label = "Document", categoryID = "legalProfQuarantine", searchable = false)
    private final Document legalProfQuarantineDocument;
    @CCD(label = "Document", categoryID = "cafcassQuarantine", searchable = false)
    private final Document cafcassQuarantineDocument;
    @CCD(label = "Document", categoryID = "localAuthorityQuarantine", searchable = false)
    private final Document localAuthorityQuarantineDocument;
    @CCD(label = "Document", categoryID = "courtStaffQuarantine", searchable = false)
    private final Document courtStaffQuarantineDocument;
    @CCD(label = "Document", categoryID = "localAuthorityOtherDoc", searchable = false)
    private final Document localAuthorityOtherDocDocument;
    @CCD(label = "Document", categoryID = "pathfinder", searchable = false)
    private final Document pathfinderDocument;
    @CCD(label = "Document", categoryID = "draftOrders", searchable = false)
    private final Document draftOrdersDocument;
    @CCD(label = "Document", categoryID = "courtnavQuarantine", searchable = false)
    private final Document courtNavQuarantineDocument;

    // Adding Bulk scan attributes
    @CCD(label = "File Name", searchable = false)
    public final String fileName;
    @CCD(label = "Document Control Number", searchable = false)
    public final String controlNumber;
    @CCD(label = "Document Type", searchable = false)
    public final String type;
    @CCD(label = "Document sub type", searchable = false)
    public final String subtype;
    @CCD(label = "Exception Record Reference", searchable = false)
    public final String exceptionRecordReference;
    @CCD(label = "Document", searchable = false)
    public final Document url;
    @CCD(label = "Scanned Date", searchable = false)
    public final LocalDateTime scannedDate;
    @CCD(label = "Delivery Date", searchable = false)
    public final LocalDateTime deliveryDate;

    //PRL-4320 - manage docs redesign
    @CCD(
            label = "Is confidential?",
            showCondition = "uploadedByIdamId=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isConfidential;
    @CCD(
            label = "Is restricted?",
            showCondition = "uploadedByIdamId=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isRestricted;
    @CCD(label = "Reason for restricted access", searchable = false)
    private final String restrictedDetails;
    @CCD(label = "Uploaded by", searchable = false)
    private final String uploadedBy;
    @CCD(label = "Uploaded by IdamId", showCondition = "uploadedByIdamId=\"DO_NOT_SHOW\"", searchable = false)
    private final String uploadedByIdamId;
    @CCD(label = "uploader user role", showCondition = "uploaderRole=\"DO_NOT_SHOW\"", searchable = false)
    private final String uploaderRole;
    @CCD(
            label = "uploader user role",
            showCondition = "hasTheConfidentialDocumentBeenRenamed=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo hasTheConfidentialDocumentBeenRenamed;

    // These fields are neeeded when Respondent solicitor uploads response on behalf of party
    @CCD(label = "solicitor represented party name", showCondition = "uploaderRole=\"DO_NOT_SHOW\"", searchable = false)
    private final String solicitorRepresentedPartyName;
    @CCD(label = "solicitor represented party id", showCondition = "uploaderRole=\"DO_NOT_SHOW\"", searchable = false)
    private final String solicitorRepresentedPartyId;
    @CCD(label = "Document language", showCondition = "uploaderRole=\"DO_NOT_SHOW\"", searchable = false)
    private final String documentLanguage;

    //PRL-4306- Added confidential category in the exclusion list
    public static String[] quarantineCategoriesToRemove() {
        return new String [] {
            "citizenQuarantine", "legalProfQuarantine", "cafcassQuarantine", "courtStaffQuarantine", "confidential",
            "applicationsWithinProceedings", "applicationsWithinProceedingsRes", "applicationsFromOtherProceedings",
            "courtnavQuarantine", "c8ArchivedDocuments", "bulkScanQuarantine", "draftOrders", "localAuthorityQuarantine"
        };
    }


    public static String[] allQuarantineCategoriesToRemove() {
        return new String [] {
            "citizenQuarantine", "legalProfQuarantine", "cafcassQuarantine", "courtStaffQuarantine",
            "courtnavQuarantine", "bulkScanQuarantine", "localAuthorityQuarantine"
        };
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Document", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.Document dnaReportsOtherDocsDocument;
  @CCD(label = " ", showCondition = "partyDetails=\"DO_NOT_SHOW\"", searchable = false)
  private ServedParties partyDetails;
  // ==== end synthesised definition-only fields ====
}
