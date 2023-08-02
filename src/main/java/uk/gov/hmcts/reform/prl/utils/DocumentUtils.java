package uk.gov.hmcts.reform.prl.utils;

import org.apache.commons.io.IOUtils;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ANY_OTHER_DOC;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICATIONS_FROM_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICATIONS_WITHIN_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPROVED_ORDERS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CASE_SUMMARY;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CONFIDENTIAL;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.COURT_BUNDLE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.DNA_REPORTS_EXPERT_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.DNA_REPORTS_OTHER_DOCS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.DRUG_AND_ALCOHOL_TEST;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.EMAILS_TO_COURT_TO_REQUEST_HEARINGS_ADJOURNED;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.GUARDIAN_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.HOME_OFFICE_DWP_RESPONSES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.IMPORTANT_INFO_ABOUT_ADDRESS_AND_CONTACT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.JUDGE_NOTES_FROM_HEARING;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.LETTERS_OF_COMPLAINTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MAGISTRATES_FACTS_AND_REASONS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MEDICAL_RECORDS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MEDICAL_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MIAM_CERTIFICATE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.NOTICES_OF_ACTING_DISCHARGE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ORDERS_SUBMITTED_WITH_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.OTHER_DOCS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.OTHER_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.POLICE_DISCLOSURES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.POLICE_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PRIVACY_NOTICE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PUBLIC_FUNDING_CERTIFICATES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.REQUEST_FOR_FAS_FORMS_TO_BE_CHANGED;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESULTS_OF_HAIR_STRAND_BLOOD_TESTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SAFEGUARDING_LETTER;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SEC37_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SECTION7_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SECTION_37_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SIXTEEN_A_RISK_ASSESSMENT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SPECIAL_GUARDIANSHIP_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SPIP_REFERRAL_REQUESTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.STANDARD_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.TRANSCRIPTS_OF_JUDGEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.WITNESS_AVAILABILITY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;


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

    public static Document toPrlDocument(uk.gov.hmcts.reform.ccd.document.am.model.Document document) {
        if (null != document) {
            return Document.builder()
                .documentUrl(document.links.self.href)
                .documentBinaryUrl(document.links.binary.href)
                .documentHash(document.hashToken)
                .documentFileName(document.originalDocumentName).build();
        }
        return null;
    }

    public static byte[] readBytes(String resourcePath) {
        try (InputStream inputStream = ResourceReader.class.getResourceAsStream(resourcePath)) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Unable to read resource: " + resourcePath, e);
        }
    }

    public static QuarantineLegalDoc getQuarantineUploadDocument(String categoryId,
                                                                 Document document) {

        return QuarantineLegalDoc.builder()
            .applicantApplicationDocument(getDocumentByCategoryId(APPLICANT_APPLICATION, categoryId, document))
            .applicantC1AApplicationDocument(getDocumentByCategoryId(APPLICANT_C1A_APPLICATION, categoryId, document))
            .applicantC1AResponseDocument(getDocumentByCategoryId(APPLICANT_C1A_RESPONSE, categoryId, document))
            .applicationsWithinProceedingsDocument(getDocumentByCategoryId(APPLICATIONS_WITHIN_PROCEEDINGS, categoryId, document))
            .miamCertificateDocument(getDocumentByCategoryId(MIAM_CERTIFICATE, categoryId, document))
            .previousOrdersSubmittedWithApplicationDocument(
                getDocumentByCategoryId(PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION, categoryId, document))
            .respondentApplicationDocument(getDocumentByCategoryId(RESPONDENT_APPLICATION, categoryId, document))
            .respondentC1AApplicationDocument(getDocumentByCategoryId(RESPONDENT_C1A_APPLICATION, categoryId, document))
            .respondentC1AResponseDocument(getDocumentByCategoryId(RESPONDENT_C1A_RESPONSE, categoryId, document))
            .applicationsFromOtherProceedingsDocument(getDocumentByCategoryId(APPLICATIONS_FROM_OTHER_PROCEEDINGS, categoryId, document))
            .noticeOfHearingDocument(getDocumentByCategoryId(NOTICE_OF_HEARING, categoryId, document))
            .courtBundleDocument(getDocumentByCategoryId(COURT_BUNDLE, categoryId, document))
            .safeguardingLetterDocument(getDocumentByCategoryId(SAFEGUARDING_LETTER, categoryId, document))
            .section7ReportDocument(getDocumentByCategoryId(SECTION7_REPORT, categoryId, document))
            .section37ReportDocument(getDocumentByCategoryId(SECTION_37_REPORT, categoryId, document))
            .sixteenARiskAssessmentDocument(getDocumentByCategoryId(SIXTEEN_A_RISK_ASSESSMENT, categoryId, document))
            .guardianReportDocument(getDocumentByCategoryId(GUARDIAN_REPORT, categoryId, document))
            .specialGuardianshipReportDocument(getDocumentByCategoryId(SPECIAL_GUARDIANSHIP_REPORT, categoryId, document))
            .otherDocsDocument(getDocumentByCategoryId(OTHER_DOCS, categoryId, document))
            .confidentialDocument(getDocumentByCategoryId(CONFIDENTIAL, categoryId, document))
            .emailsToCourtToRequestHearingsAdjournedDocument(
                getDocumentByCategoryId(EMAILS_TO_COURT_TO_REQUEST_HEARINGS_ADJOURNED, categoryId, document))
            .publicFundingCertificatesDocument(getDocumentByCategoryId(PUBLIC_FUNDING_CERTIFICATES, categoryId, document))
            .noticesOfActingDischargeDocument(getDocumentByCategoryId(NOTICES_OF_ACTING_DISCHARGE, categoryId, document))
            .requestForFasFormsToBeChangedDocument(getDocumentByCategoryId(REQUEST_FOR_FAS_FORMS_TO_BE_CHANGED, categoryId, document))
            .witnessAvailabilityDocument(getDocumentByCategoryId(WITNESS_AVAILABILITY, categoryId, document))
            .lettersOfComplaintDocument(getDocumentByCategoryId(LETTERS_OF_COMPLAINTS, categoryId, document))
            .spipReferralRequestsDocument(getDocumentByCategoryId(SPIP_REFERRAL_REQUESTS, categoryId, document))
            .homeOfficeDwpResponsesDocument(getDocumentByCategoryId(HOME_OFFICE_DWP_RESPONSES, categoryId, document))
            .medicalReportsDocument(getDocumentByCategoryId(MEDICAL_REPORTS, categoryId, document))
            .dnaReportsExpertReportDocument(getDocumentByCategoryId(DNA_REPORTS_EXPERT_REPORT, categoryId, document))
            .resultsOfHairStrandBloodTestsDocument(getDocumentByCategoryId(RESULTS_OF_HAIR_STRAND_BLOOD_TESTS, categoryId, document))
            .policeDisclosuresDocument(getDocumentByCategoryId(POLICE_DISCLOSURES, categoryId, document))
            .medicalRecordsDocument(getDocumentByCategoryId(MEDICAL_RECORDS, categoryId, document))
            .drugAndAlcoholTestDocument(getDocumentByCategoryId(DRUG_AND_ALCOHOL_TEST, categoryId, document))
            .policeReportDocument(getDocumentByCategoryId(POLICE_REPORT, categoryId, document))
            .sec37ReportDocument(getDocumentByCategoryId(SEC37_REPORT, categoryId, document))
            .ordersSubmittedWithApplicationDocument(getDocumentByCategoryId(ORDERS_SUBMITTED_WITH_APPLICATION, categoryId, document))
            .approvedOrdersDocument(getDocumentByCategoryId(APPROVED_ORDERS, categoryId, document))
            .standardDirectionsOrderDocument(getDocumentByCategoryId(STANDARD_DIRECTIONS_ORDER, categoryId, document))
            .transcriptsOfJudgementsDocument(getDocumentByCategoryId(TRANSCRIPTS_OF_JUDGEMENTS, categoryId, document))
            .magistratesFactsAndReasonsDocument(
                getDocumentByCategoryId(MAGISTRATES_FACTS_AND_REASONS, categoryId, document))
            .judgeNotesFromHearingDocument(getDocumentByCategoryId(JUDGE_NOTES_FROM_HEARING, categoryId, document))
            .importantInfoAboutAddressAndContactDocument(
                getDocumentByCategoryId(IMPORTANT_INFO_ABOUT_ADDRESS_AND_CONTACT, categoryId, document))
            .dnaReportsOtherDocsDocument(getDocumentByCategoryId(DNA_REPORTS_OTHER_DOCS, categoryId, document))
            .privacyNoticeDocument(getDocumentByCategoryId(PRIVACY_NOTICE, categoryId, document))
            .specialMeasuresDocument(getDocumentByCategoryId(SPECIAL_MEASURES, categoryId, document))
            .anyOtherDocDocument(getDocumentByCategoryId(ANY_OTHER_DOC, categoryId, document))
            .positionStatementsDocument(getDocumentByCategoryId(POSITION_STATEMENTS, categoryId, document))
            .applicantStatementsDocument(getDocumentByCategoryId(APPLICANT_STATEMENTS, categoryId, document))
            .respondentStatementsDocument(getDocumentByCategoryId(RESPONDENT_STATEMENTS, categoryId, document))
            .otherWitnessStatementsDocument(getDocumentByCategoryId(OTHER_WITNESS_STATEMENTS, categoryId, document))
            .caseSummaryDocument(getDocumentByCategoryId(CASE_SUMMARY, categoryId, document))
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
            .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
            .restrictCheckboxCorrespondence(manageDocument.getDocumentRestrictCheckbox())
            .notes(manageDocument.getDocumentDetails())
            .categoryId(manageDocument.getDocumentCategories().getValueCode())
            .categoryName(manageDocument.getDocumentCategories().getValueLabel())
            .build();
    }

    public static List<Element<QuarantineLegalDoc>> getExistingCitizenQuarantineDocuments(CaseData caseData) {
        if (isNotEmpty(caseData.getCitizenQuarantineDocsList())) {
            return caseData.getCitizenQuarantineDocsList();
        }
        return new ArrayList<>();
    }

    public static QuarantineLegalDoc addCitizenQuarantineFields(QuarantineLegalDoc quarantineLegalDoc,
                                                                String documentParty,
                                                                String categoryId,
                                                                String categoryName,
                                                                String notes,
                                                                ServedParties servedParties) {
        return quarantineLegalDoc.toBuilder()
            .documentParty(documentParty)
            .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
            .categoryId(categoryId)
            .categoryName(categoryName)
            .notes(notes)
            .partyDetails(servedParties)
            .build();
    }
}
