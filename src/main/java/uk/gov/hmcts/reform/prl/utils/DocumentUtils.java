package uk.gov.hmcts.reform.prl.utils;

import uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;

import java.time.LocalDateTime;

public class DocumentUtils {

    public static GeneratedDocumentInfo toGeneratedDocumentInfo(Document document) {
        return GeneratedDocumentInfo.builder()
            .url(document.getDocumentUrl())
            .binaryUrl(document.getDocumentBinaryUrl())
            .hashToken(document.getDocumentHash())
            .build();
    }

    public static Document toCoverLetterDocument(GeneratedDocumentInfo generatedDocumentInfo) {
        if (null != generatedDocumentInfo) {
            return Document.builder().documentUrl(generatedDocumentInfo.getUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentFileName("coverletter.pdf")
                .build();
        }
        return null;
    }


    public static Document toDocument(GeneratedDocumentInfo generateDocument) {
        if (null != generateDocument) {
            return Document.builder().documentUrl(generateDocument.getUrl())
                .documentHash(generateDocument.getHashToken())
                .documentBinaryUrl(generateDocument.getBinaryUrl())
                .documentFileName(generateDocument.getDocName())
                .build();
        }
        return null;
    }

    public static QuarantineLegalDoc getQuarantineUploadDocument(String categoryId,
                                                                 Document document) {

        return QuarantineLegalDoc.builder()
            .applicantApplicationDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.APPLICANT_APPLICATION,
                categoryId,
                document
            ))
            .applicantC1AApplicationDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.APPLICANT_C1A_APPLICATION,
                categoryId,
                document
            ))
            .applicantC1AResponseDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.APPLICANT_C1A_RESPONSE,
                categoryId,
                document
            ))
            .applicationsWithinProceedingsDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.APPLICATIONS_WITHIN_PROCEEDINGS,
                categoryId,
                document
            ))
            .miamCertificateDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.MIAM_CERTIFICATE,
                categoryId,
                document
            ))
            .previousOrdersSubmittedWithApplicationDocument(
                getDocumentByCategoryId(
                    ManageDocumentsCategoryConstants.PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION,
                    categoryId,
                    document
                ))
            .respondentApplicationDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.RESPONDENT_APPLICATION,
                categoryId,
                document
            ))
            .respondentC1AApplicationDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.RESPONDENT_C1A_APPLICATION,
                categoryId,
                document
            ))
            .respondentC1AResponseDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.RESPONDENT_C1A_RESPONSE,
                categoryId,
                document
            ))
            .applicationsFromOtherProceedingsDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.APPLICATIONS_FROM_OTHER_PROCEEDINGS,
                categoryId,
                document
            ))
            .noticeOfHearingDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.NOTICE_OF_HEARING,
                categoryId,
                document
            ))
            .courtBundleDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.COURT_BUNDLE,
                categoryId,
                document
            ))
            .safeguardingLetterDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.SAFEGUARDING_LETTER,
                categoryId,
                document
            ))
            .section7ReportDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.SECTION7_REPORT,
                categoryId,
                document
            ))
            .section37ReportDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.SECTION_37_REPORT,
                categoryId,
                document
            ))
            .sixteenARiskAssessmentDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.SIXTEEN_A_RISK_ASSESSMENT,
                categoryId,
                document
            ))
            .guardianReportDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.GUARDIAN_REPORT,
                categoryId,
                document
            ))
            .specialGuardianshipReportDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.SPECIAL_GUARDIANSHIP_REPORT,
                categoryId,
                document
            ))
            .otherDocsDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.OTHER_DOCS,
                categoryId,
                document
            ))
            .confidentialDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.CONFIDENTIAL,
                categoryId,
                document
            ))
            .emailsToCourtToRequestHearingsAdjournedDocument(
                getDocumentByCategoryId(
                    ManageDocumentsCategoryConstants.EMAILS_TO_COURT_TO_REQUEST_HEARINGS_ADJOURNED,
                    categoryId,
                    document
                ))
            .publicFundingCertificatesDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.PUBLIC_FUNDING_CERTIFICATES,
                categoryId,
                document
            ))
            .noticesOfActingDischargeDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.NOTICES_OF_ACTING_DISCHARGE,
                categoryId,
                document
            ))
            .requestForFasFormsToBeChangedDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.REQUEST_FOR_FAS_FORMS_TO_BE_CHANGED,
                categoryId,
                document
            ))
            .witnessAvailabilityDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.WITNESS_AVAILABILITY,
                categoryId,
                document
            ))
            .lettersOfComplaintDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.LETTERS_OF_COMPLAINTS,
                categoryId,
                document
            ))
            .spipReferralRequestsDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.SPIP_REFERRAL_REQUESTS,
                categoryId,
                document
            ))
            .homeOfficeDwpResponsesDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.HOME_OFFICE_DWP_RESPONSES,
                categoryId,
                document
            ))
            .medicalReportsDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.MEDICAL_REPORTS,
                categoryId,
                document
            ))
            .dnaReportsExpertReportDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.DNA_REPORTS_EXPERT_REPORT,
                categoryId,
                document
            ))
            .resultsOfHairStrandBloodTestsDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.RESULTS_OF_HAIR_STRAND_BLOOD_TESTS,
                categoryId,
                document
            ))
            .policeDisclosuresDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.POLICE_DISCLOSURES,
                categoryId,
                document
            ))
            .medicalRecordsDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.MEDICAL_RECORDS,
                categoryId,
                document
            ))
            .drugAndAlcoholTestDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.DRUG_AND_ALCOHOL_TEST,
                categoryId,
                document
            ))
            .policeReportDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.POLICE_REPORT,
                categoryId,
                document
            ))
            .sec37ReportDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.SEC37_REPORT,
                categoryId,
                document
            ))
            .ordersSubmittedWithApplicationDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.ORDERS_SUBMITTED_WITH_APPLICATION,
                categoryId,
                document
            ))
            .approvedOrdersDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.APPROVED_ORDERS,
                categoryId,
                document
            ))
            .standardDirectionsOrderDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.STANDARD_DIRECTIONS_ORDER,
                categoryId,
                document
            ))
            .transcriptsOfJudgementsDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.TRANSCRIPTS_OF_JUDGEMENTS,
                categoryId,
                document
            ))
            .magistratesFactsAndReasonsDocument(
                getDocumentByCategoryId(
                    ManageDocumentsCategoryConstants.MAGISTRATES_FACTS_AND_REASONS,
                    categoryId,
                    document
                ))
            .judgeNotesFromHearingDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.JUDGE_NOTES_FROM_HEARING,
                categoryId,
                document
            ))
            .importantInfoAboutAddressAndContactDocument(
                getDocumentByCategoryId(
                    ManageDocumentsCategoryConstants.IMPORTANT_INFO_ABOUT_ADDRESS_AND_CONTACT,
                    categoryId,
                    document
                ))
            .dnaReportsOtherDocsDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.DNA_REPORTS_OTHER_DOCS,
                categoryId,
                document
            ))
            .privacyNoticeDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.PRIVACY_NOTICE,
                categoryId,
                document
            ))
            .specialMeasuresDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.SPECIAL_MEASURES,
                categoryId,
                document
            ))
            .anyOtherDocDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.ANY_OTHER_DOC,
                categoryId,
                document
            ))
            .positionStatementsDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.POSITION_STATEMENTS,
                categoryId,
                document
            ))
            .applicantStatementsDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.APPLICANT_STATEMENTS,
                categoryId,
                document
            ))
            .respondentStatementsDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.RESPONDENT_STATEMENTS,
                categoryId,
                document
            ))
            .otherWitnessStatementsDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.OTHER_WITNESS_STATEMENTS,
                categoryId,
                document
            ))
            .caseSummaryDocument(getDocumentByCategoryId(
                ManageDocumentsCategoryConstants.CASE_SUMMARY,
                categoryId,
                document
            ))
            .build();
    }

    private static Document getDocumentByCategoryId(String categoryConstant,
                                                    String categoryId,
                                                    Document document) {
        return categoryConstant.equalsIgnoreCase(categoryId) ? document : null;
    }

    public static QuarantineLegalDoc addQuarantineFields(QuarantineLegalDoc quarantineLegalDoc,
                                                         ManageDocuments manageDocument) {
        return quarantineLegalDoc.toBuilder()
            .documentParty(manageDocument.getDocumentParty().getDisplayedValue())
            .documentUploadedDate(LocalDateTime.now())
            .restrictCheckboxCorrespondence(manageDocument.getDocumentRestrictCheckbox())
            .notes(manageDocument.getDocumentDetails())
            .categoryId(manageDocument.getDocumentCategories().getValueCode())
            .categoryName(manageDocument.getDocumentCategories().getValueLabel())
            .build();
    }
}
