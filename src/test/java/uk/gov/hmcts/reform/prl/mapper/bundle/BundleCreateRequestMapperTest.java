package uk.gov.hmcts.reform.prl.mapper.bundle;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingInformation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ANY_OTHER_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_LA_OTHER_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CANCELLED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_SUMMARY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DNA_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRUG_AND_ALCOHOL_TESTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.GUARDIAN_REPORT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LETTERS_FROM_SCHOOL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MAGISTRATES_FACTS_AND_REASONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MAIL_SCREENSHOTS_MEDIA_FILES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_RECORDS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MIAM_CERTIFICATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PATERNITY_TEST_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POLICE_DISCLOSURES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POLICE_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PREVIOUS_ORDERS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT_APPLCATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT_C1A_APPLCATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESULTS_OF_HAIR_STRAND_BLOOD_TESTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SAFEGUARDING_LETTER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SECTION_37_REPORT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SECTION_7_REPORT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SIXTEENA_RISK_ASSESSMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SPECIAL_GUARDIANSHIP_REPORT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TRANSCRIPTS_OF_JUDGEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class BundleCreateRequestMapperTest {
    @InjectMocks
    private BundleCreateRequestMapper bundleCreateRequestMapper;

    @Test
    public void testBundleCreateRequestMapper() {
        List<FurtherEvidence> furtherEvidences = new ArrayList<>();
        furtherEvidences.add(FurtherEvidence.builder().typeOfDocumentFurtherEvidence(FurtherEvidenceDocumentType.miamCertificate)
            .documentFurtherEvidence(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Sample1.pdf").build())
            .restrictCheckboxFurtherEvidence(new ArrayList<>()).build());

        furtherEvidences.add(FurtherEvidence.builder().typeOfDocumentFurtherEvidence(FurtherEvidenceDocumentType.previousOrders)
            .documentFurtherEvidence(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Sample1.pdf").build())
            .restrictCheckboxFurtherEvidence(new ArrayList<>()).build());

        List<OtherDocuments> otherDocuments = new ArrayList<>();
        otherDocuments.add(OtherDocuments.builder().documentName("Application docu")
            .documentOther(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Sample2.pdf").build()).documentTypeOther(
                DocTypeOtherDocumentsEnum.applicantStatement).restrictCheckboxOtherDocuments(new ArrayList<>()).build());

        List<OrderDetails> orders = new ArrayList<>();
        orders.add(OrderDetails.builder().orderType("orders")
            .orderDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Order.pdf").build()).build());

        List<ResponseDocuments> citizenC7uploadedDocs = new ArrayList<>();
        citizenC7uploadedDocs.add(ResponseDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("C7Document.pdf").build()).build());

        List<UploadedDocuments> uploadedDocuments = new ArrayList<>();
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("PositionStatement.pdf").build())
            .documentType(YOUR_POSITION_STATEMENTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("PositionStatement.pdf").build())
            .documentType(YOUR_POSITION_STATEMENTS).isApplicant("Yes").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("WitnessStatement.pdf").build())
            .documentType(YOUR_WITNESS_STATEMENTS).isApplicant("Yes").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("WitnessStatement.pdf").build())
            .documentType(YOUR_WITNESS_STATEMENTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("LettersFromSchool.pdf").build())
            .documentType(LETTERS_FROM_SCHOOL).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("LettersFromSchool.pdf").build())
            .documentType(LETTERS_FROM_SCHOOL).isApplicant("Yes").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("LettersFromSchool.pdf").build())
            .documentType(EXPERT_REPORTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("CAFCASSReports.pdf").build())
            .documentType(CAFCASS_REPORTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("OtherWitnessDocuments.pdf").build())
            .documentType(OTHER_WITNESS_STATEMENTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("MedicalRecords.pdf").build())
            .documentType(MEDICAL_RECORDS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("MedicalReports.pdf").build())
            .documentType(MEDICAL_REPORTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("PaternityReports.pdf").build())
            .documentType(PATERNITY_TEST_REPORTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("DrugAndAlchol.pdf").build())
            .documentType(DRUG_AND_ALCOHOL_TESTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("PoliceReports.pdf").build())
            .documentType(POLICE_REPORTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("MediaScreenshots.pdf").build())
            .documentType(MAIL_SCREENSHOTS_MEDIA_FILES).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("MediaScreenshots.pdf").build())
            .documentType(MAIL_SCREENSHOTS_MEDIA_FILES).isApplicant("Yes").build());
        List<Document> fl401UploadWitnessDocuments = new ArrayList<>();
        fl401UploadWitnessDocuments.add(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("witnessDoc.pdf").build());

        List<Document> fl401UploadSupportingDocuments = new ArrayList<>();
        fl401UploadSupportingDocuments.add(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("supportingDoc.pdf")
            .build());

        List<ManageDocuments> otherManageDocuments = new ArrayList<>();
        otherManageDocuments.add(ManageDocuments.builder()
                                     .document(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("position.pdf")
                                                   .build())
                                     .documentCategories(DynamicList.builder().value(DynamicListElement.defaultListItem("Position statements"))
                                                             .build())
                                     .documentParty(DocumentPartyEnum.APPLICANT)
                                    .build());
        otherManageDocuments.add(ManageDocuments.builder()
                                     .document(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("witness.pdf")
                                                   .build())
                                     .documentCategories(DynamicList.builder()
                                                             .value(DynamicListElement.defaultListItem("Your witness statements")).build())
                                     .documentParty(DocumentPartyEnum.APPLICANT).build());
        otherManageDocuments.add(ManageDocuments.builder()
                                     .document(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("otherwitness.pdf")
                                                   .build())
                                     .documentCategories(DynamicList.builder()
                                                             .value(DynamicListElement.defaultListItem("Other witness Statements")).build())
                                     .documentParty(DocumentPartyEnum.APPLICANT)
                                     .build());
        otherManageDocuments.add(ManageDocuments.builder()
                                     .document(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("otherwitness.pdf")
                                                   .build())
                                     .documentCategories(DynamicList.builder().value(DynamicListElement.defaultListItem("Medical reports"))
                                                             .build())
                                     .documentParty(DocumentPartyEnum.APPLICANT)
                                     .build());
        otherManageDocuments.add(ManageDocuments.builder()
                                     .document(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("otherwitness.pdf")
                                                   .build())
                                     .documentCategories(DynamicList.builder().value(DynamicListElement.defaultListItem("Medical Records"))
                                                             .build())
                                     .documentParty(DocumentPartyEnum.APPLICANT)
                                     .build());
        otherManageDocuments.add(ManageDocuments.builder()
                                     .document(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("otherwitness.pdf")
                                                   .build())
                                     .documentCategories(DynamicList.builder().value(DynamicListElement.defaultListItem("Paternity test reports"))
                                                             .build())
                                     .documentParty(DocumentPartyEnum.APPLICANT)
                                     .build());
        otherManageDocuments.add(ManageDocuments.builder()
                                     .document(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("otherwitness.pdf")
                                                   .build())
                                     .documentCategories(DynamicList.builder()
                                                             .value(DynamicListElement.defaultListItem("Drug and alcohol test (toxicology)"))
                                                             .build())
                                     .documentParty(DocumentPartyEnum.APPLICANT)
                                     .build());
        otherManageDocuments.add(ManageDocuments.builder()
                                     .document(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("otherwitness.pdf")
                                                   .build())
                                     .documentCategories(DynamicList.builder().value(DynamicListElement.defaultListItem("Police report"))
                                                             .build())
                                     .documentParty(DocumentPartyEnum.APPLICANT)
                                     .build());
        otherManageDocuments.add(ManageDocuments.builder()
                                     .document(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("otherwitness.pdf")
                                                   .build())
                                     .documentCategories(DynamicList.builder().value(DynamicListElement.defaultListItem("Cafcass reports"))
                                                             .build())
                                     .documentParty(DocumentPartyEnum.APPLICANT)
                                     .build());
        otherManageDocuments.add(ManageDocuments.builder()
                                     .document(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("otherwitness.pdf")
                                                   .build())
                                     .documentCategories(DynamicList.builder().value(DynamicListElement.defaultListItem("Expert reports"))
                                                             .build())
                                     .documentParty(DocumentPartyEnum.APPLICANT)
                                     .build());
        otherManageDocuments.add(ManageDocuments.builder()
                                     .document(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("otherwitness.pdf")
                                                   .build())
                                     .documentCategories(DynamicList.builder().value(DynamicListElement.defaultListItem("Applicant's statements"))
                                                             .build())
                                     .documentParty(DocumentPartyEnum.APPLICANT)
                                     .build());

        QuarantineLegalDoc respStatement = QuarantineLegalDoc.builder()
            .respondentStatementsDocument(Document.builder().documentFileName("respondentStatements").build())
            .documentParty("Respondent").categoryName("Respondent's statements").build();
        List<Element<QuarantineLegalDoc>> courtStaffDoc = new ArrayList<>();
        courtStaffDoc.add(element(respStatement));

        QuarantineLegalDoc applStatement = QuarantineLegalDoc.builder()
            .applicantStatementsDocument(Document.builder().documentFileName("applicantStatements").build())
            .documentParty("Applicant").categoryName("Applicant's statements").build();
        courtStaffDoc.add(element(applStatement));

        QuarantineLegalDoc policeReport = QuarantineLegalDoc.builder()
            .policeReportDocument(Document.builder().documentFileName("policeReport").build())
            .documentParty("Applicant").categoryName("Police report").build();
        courtStaffDoc.add(element(policeReport));

        QuarantineLegalDoc drugTest = QuarantineLegalDoc.builder()
            .drugAndAlcoholTestDocument(Document.builder().documentFileName("drugTest").build())
            .documentParty("Applicant").categoryName("Drug and alcohol test (toxicology)").build();
        courtStaffDoc.add(element(drugTest));

        QuarantineLegalDoc medicalRecords = QuarantineLegalDoc.builder()
            .medicalRecordsDocument(Document.builder().documentFileName("medicalRecords").build())
            .documentParty("Applicant").categoryName("Medical Records").build();
        courtStaffDoc.add(element(medicalRecords));

        QuarantineLegalDoc medicalReports = QuarantineLegalDoc.builder()
            .medicalReportsDocument(Document.builder().documentFileName("medicalReports").build())
            .documentParty("Applicant").categoryName("Medical reports").build();
        courtStaffDoc.add(element(medicalReports));

        QuarantineLegalDoc witnessStmnts = QuarantineLegalDoc.builder()
            .otherWitnessStatementsDocument(Document.builder().documentFileName("witnessStmnts").build())
            .documentParty("Applicant").categoryName("Other witness Statements").build();
        courtStaffDoc.add(element(witnessStmnts));

        QuarantineLegalDoc positionStmnts = QuarantineLegalDoc.builder()
            .positionStatementsDocument(Document.builder().documentFileName("positionStmnts").build())
            .documentParty("Applicant").categoryName("Position statements").build();
        courtStaffDoc.add(element(positionStmnts));

        QuarantineLegalDoc dnaReports = QuarantineLegalDoc.builder()
            .dnaReportsExpertReportDocument(Document.builder().documentFileName("DNAReports").build())
            .documentParty("Applicant").categoryName(DNA_REPORTS).build();
        courtStaffDoc.add(element(dnaReports));

        QuarantineLegalDoc resultsOfHairStrandBloodTests = QuarantineLegalDoc.builder()
            .resultsOfHairStrandBloodTestsDocument(Document.builder().documentFileName("resultsOfHairStrandBloodTests").build())
            .documentParty("Applicant").categoryName(RESULTS_OF_HAIR_STRAND_BLOOD_TESTS).build();
        courtStaffDoc.add(element(resultsOfHairStrandBloodTests));

        QuarantineLegalDoc policeDisclosures = QuarantineLegalDoc.builder()
            .policeDisclosuresDocument(Document.builder().documentFileName("policeDisclosures").build())
            .documentParty("Applicant").categoryName(POLICE_DISCLOSURES).build();
        courtStaffDoc.add(element(policeDisclosures));

        QuarantineLegalDoc applicantC1AResponse = QuarantineLegalDoc.builder()
            .applicantC1AResponseDocument(Document.builder().documentFileName("applicantC1AResponse").build())
            .documentParty("Applicant").categoryName(APPLICANT_C1A_RESPONSE).build();
        courtStaffDoc.add(element(applicantC1AResponse));

        QuarantineLegalDoc caseSummary = QuarantineLegalDoc.builder()
            .caseSummaryDocument(Document.builder().documentFileName("caseSummary").build())
            .documentParty("Court").categoryName(CASE_SUMMARY).build();
        courtStaffDoc.add(element(caseSummary));

        QuarantineLegalDoc transcriptsOfJudgements = QuarantineLegalDoc.builder()
            .transcriptsOfJudgementsDocument(Document.builder().documentFileName("transcriptsOfJudgements").build())
            .documentParty("Court").categoryName(TRANSCRIPTS_OF_JUDGEMENTS).build();
        courtStaffDoc.add(element(transcriptsOfJudgements));

        QuarantineLegalDoc magistratesFactsAndReasons = QuarantineLegalDoc.builder()
            .magistratesFactsAndReasonsDocument(Document.builder().documentFileName("magistratesFactsAndReasons").build())
            .documentParty("Court").categoryName(MAGISTRATES_FACTS_AND_REASONS).build();
        courtStaffDoc.add(element(magistratesFactsAndReasons));

        QuarantineLegalDoc safeGuardingLetter = QuarantineLegalDoc.builder()
            .safeguardingLetterDocument(Document.builder().documentFileName("safeGuardingLetter").build())
            .documentParty("Cafcass Cymru").categoryName(SAFEGUARDING_LETTER).build();
        courtStaffDoc.add(element(safeGuardingLetter));

        QuarantineLegalDoc section7Report = QuarantineLegalDoc.builder()
            .section7ReportDocument(Document.builder().documentFileName("section7Report").build())
            .documentParty("Cafcass Cymru").categoryName(SECTION_7_REPORT).build();
        courtStaffDoc.add(element(section7Report));

        QuarantineLegalDoc sixteenARiskAssessment = QuarantineLegalDoc.builder()
            .sixteenARiskAssessmentDocument(Document.builder().documentFileName("16ARiskAssessment").build())
            .documentParty("Cafcass Cymru").categoryName(SIXTEENA_RISK_ASSESSMENT).build();
        courtStaffDoc.add(element(sixteenARiskAssessment));

        QuarantineLegalDoc guardianReport = QuarantineLegalDoc.builder()
            .guardianReportDocument(Document.builder().documentFileName("guardianReport").build())
            .documentParty("Cafcass Cymru").categoryName(GUARDIAN_REPORT).build();
        courtStaffDoc.add(element(guardianReport));

        QuarantineLegalDoc specialGuardianshipReport = QuarantineLegalDoc.builder()
            .specialGuardianshipReportDocument(Document.builder().documentFileName("specialGuardianshipReport").build())
            .documentParty("Cafcass Cymru").categoryName(SPECIAL_GUARDIANSHIP_REPORT).build();
        courtStaffDoc.add(element(specialGuardianshipReport));

        QuarantineLegalDoc section37Report = QuarantineLegalDoc.builder()
            .section37ReportDocument(Document.builder().documentFileName("section37Report").build())
            .documentParty("Cafcass Cymru").categoryName(SECTION_37_REPORT).build();
        courtStaffDoc.add(element(section37Report));

        QuarantineLegalDoc cafcassOtherDocuments = QuarantineLegalDoc.builder()
            .otherDocsDocument(Document.builder().documentFileName("otherDocuments").build())
            .documentParty("Cafcass Cymru").categoryName(CAFCASS_LA_OTHER_DOCUMENTS).build();
        courtStaffDoc.add(element(cafcassOtherDocuments));

        QuarantineLegalDoc laSection37Report = QuarantineLegalDoc.builder()
            .sec37ReportDocument(Document.builder().documentFileName("laSection37Report").build())
            .documentParty("Local Authority").categoryName(SECTION_37_REPORT).build();
        courtStaffDoc.add(element(laSection37Report));

        QuarantineLegalDoc laOtherDocuments = QuarantineLegalDoc.builder()
            .localAuthorityOtherDocDocument(Document.builder().documentFileName("laOtherDocuments").build())
            .documentParty("Cafcass Cymru").categoryName(CAFCASS_LA_OTHER_DOCUMENTS).build();
        courtStaffDoc.add(element(laOtherDocuments));

        QuarantineLegalDoc applicantPreviousOrders = QuarantineLegalDoc.builder()
            .previousOrdersSubmittedWithApplicationDocument(Document.builder().documentFileName(
                "applicantPreviousOrders").build())
            .documentParty("Applicant").categoryName(PREVIOUS_ORDERS).build();
        courtStaffDoc.add(element(applicantPreviousOrders));

        QuarantineLegalDoc respondentPreviousOrders = QuarantineLegalDoc.builder()
            .ordersFromOtherProceedingsDocument(Document.builder().documentFileName("respondentPreviousOrders").build())
            .documentParty("Respondent").categoryName(PREVIOUS_ORDERS).build();
        courtStaffDoc.add(element(respondentPreviousOrders));

        QuarantineLegalDoc anyOtherDocuments = QuarantineLegalDoc.builder()
            .anyOtherDocDocument(Document.builder().documentFileName("anyOtherDocuments").build())
            .documentParty("Court").categoryName(ANY_OTHER_DOCUMENTS).build();
        courtStaffDoc.add(element(anyOtherDocuments));

        QuarantineLegalDoc respondentApplication = QuarantineLegalDoc.builder()
            .respondentApplicationDocument(Document.builder().documentFileName("respondentApplication").build())
            .categoryName(RESPONDENT_APPLCATION).build();
        courtStaffDoc.add(element(respondentApplication));

        QuarantineLegalDoc respondentC1aApplication = QuarantineLegalDoc.builder()
            .respondentC1AApplicationDocument(Document.builder().documentFileName("respondentC1aApplication").build())
            .categoryName(RESPONDENT_C1A_APPLCATION).build();
        courtStaffDoc.add(element(respondentC1aApplication));

        QuarantineLegalDoc respondentC1aResponse = QuarantineLegalDoc.builder()
            .respondentC1AResponseDocument(Document.builder().documentFileName("respondentC1aResponse").build())
            .categoryName(RESPONDENT_C1A_RESPONSE).build();
        courtStaffDoc.add(element(respondentC1aResponse));

        QuarantineLegalDoc miamCertificate = QuarantineLegalDoc.builder()
            .miamCertificateDocument(Document.builder().documentFileName("miamCertificate").build())
            .categoryName(MIAM_CERTIFICATE).build();
        courtStaffDoc.add(element(miamCertificate));

        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .languagePreferenceWelsh(No)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .finalDocument(Document.builder().documentFileName("C100AppDoc").documentUrl("Url").build())
            .c1ADocument(Document.builder().documentFileName("c1ADocument").documentUrl("Url").build())
            .otherDocuments(ElementUtils.wrapElements(otherDocuments))
            .documentManagementDetails(DocumentManagementDetails
                                           .builder()
                                           .manageDocuments(ElementUtils.wrapElements(otherManageDocuments))
                                           .build())
            .furtherEvidences(ElementUtils.wrapElements(furtherEvidences))
            .orderCollection(ElementUtils.wrapElements(orders))
            .bundleInformation(BundlingInformation.builder().build())
            .citizenResponseC7DocumentList(ElementUtils.wrapElements(citizenC7uploadedDocs))
            .citizenUploadedDocumentList(ElementUtils.wrapElements(uploadedDocuments))
            .miamDetails(MiamDetails.builder()
                .miamCertificationDocumentUpload(Document.builder().documentFileName("maimCertDoc1").documentUrl("Url").build())
                .miamCertificationDocumentUpload1(Document.builder().documentFileName("maimCertDoc2").documentUrl("Url").build())
                .build())
            .applicantName("ApplicantFirstNameAndLastName")
            .fl401UploadWitnessDocuments(ElementUtils.wrapElements(fl401UploadWitnessDocuments))
            .fl401UploadSupportDocuments(ElementUtils.wrapElements(fl401UploadSupportingDocuments))
            .reviewDocuments(ReviewDocuments.builder().courtStaffUploadDocListDocTab(courtStaffDoc).build())
            .build();

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(c100CaseData,"eventI",
            Hearings.hearingsWith().build(),"sample.yaml");
        assertNotNull(bundleCreateRequest);
    }

    @Test
    public void testBundleCreateRequestMapperForEmptyDetails() {
        List<HearingDaySchedule> hearingDaySchedules = new ArrayList<>();
        hearingDaySchedules.add(HearingDaySchedule.hearingDayScheduleWith().hearingJudgeId("123").hearingJudgeName("hearingJudgeName")
            .hearingVenueId("venueId").hearingVenueAddress("venueAddress")
            .hearingStartDateTime(LocalDateTime.now()).build());
        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith().hmcStatus(LISTED).hearingDaySchedule(hearingDaySchedules).build());

        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .languagePreferenceWelsh(Yes)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.DECISION_OUTCOME)
            .finalDocument(Document.builder().documentFileName("C100AppDoc").documentUrl("Url").build())
            .c1ADocument(Document.builder().documentFileName("c1ADocument").documentUrl("Url").build())
            .bundleInformation(BundlingInformation.builder().build())
            .finalWelshDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("finalWelshDoc.pdf").build())
            .c1AWelshDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("C1AWelshDoc.pdf").build())
            .reviewDocuments(ReviewDocuments.builder().build())
            .build();

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(c100CaseData,"eventI",
            Hearings.hearingsWith().caseHearings(caseHearings).build(), "sample.yaml");
        assertNotNull(bundleCreateRequest);
    }

    @Test
    public void testBundleCreateRequestMapperForVenueAddressDetails() {
        List<HearingDaySchedule> hearingDaySchedules = new ArrayList<>();
        hearingDaySchedules.add(HearingDaySchedule.hearingDayScheduleWith().hearingJudgeId("123").hearingJudgeName("hearingJudgeName")
            .hearingVenueName("venueName").hearingVenueId("venueId").hearingVenueAddress("venueAddress")
            .hearingStartDateTime(LocalDateTime.now()).build());
        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith().hmcStatus(LISTED).hearingDaySchedule(hearingDaySchedules).build());

        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .languagePreferenceWelsh(Yes)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.DECISION_OUTCOME)
            .finalDocument(Document.builder().documentFileName("C100AppDoc").documentUrl("Url").build())
            .c1ADocument(Document.builder().documentFileName("c1ADocument").documentUrl("Url").build())
            .bundleInformation(BundlingInformation.builder().build())
            .finalWelshDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("finalWelshDoc.pdf").build())
            .c1AWelshDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("C1AWelshDoc.pdf").build())
            .reviewDocuments(ReviewDocuments.builder().build())
            .build();

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(c100CaseData,"eventI",
            Hearings.hearingsWith().caseHearings(caseHearings).build(), "sample.yaml");
        assertNotNull(bundleCreateRequest);
        assertEquals("venueName" + "\n" + "venueAddress",
            bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingVenueAddress());
    }

    @Test
    public void testBundleCreateRequestMapperWhenNoHearingScheduleDetails() {
        List<HearingDaySchedule> hearingDaySchedules = new ArrayList<>();
        hearingDaySchedules.add(HearingDaySchedule.hearingDayScheduleWith().build());
        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith().hmcStatus(LISTED).hearingDaySchedule(hearingDaySchedules).build());

        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .languagePreferenceWelsh(Yes)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.DECISION_OUTCOME)
            .finalDocument(Document.builder().documentFileName("C100AppDoc").documentUrl("Url").build())
            .c1ADocument(Document.builder().documentFileName("c1ADocument").documentUrl("Url").build())
            .reviewDocuments(ReviewDocuments.builder().build())
            .bundleInformation(BundlingInformation.builder().build())
            .reviewDocuments(ReviewDocuments.builder().build())
            .build();

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(c100CaseData,"eventI",
            Hearings.hearingsWith().caseHearings(caseHearings).build(), "sample.yaml");
        assertNotNull(bundleCreateRequest);
        Assert.assertEquals("",bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime());
        Assert.assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingJudgeName());
        Assert.assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingVenueAddress());
    }

    @Test
    public void testBundleCreateRequestMapperWhenNoHearingDetails() {
        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.DECISION_OUTCOME)
            .bundleInformation(BundlingInformation.builder().build())
            .finalWelshDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("finalWelshDoc.pdf").build())
            .c1AWelshDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("C1AWelshDoc.pdf").build())
            .reviewDocuments(ReviewDocuments.builder().build())
            .build();

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(c100CaseData,"eventI",
            null, "sample.yaml");
        assertNotNull(bundleCreateRequest);
        Assert.assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime());
        Assert.assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingJudgeName());
        Assert.assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingVenueAddress());
    }

    @Test
    public void testBundleCreateRequestMapperWhenNoCaseHearings() {
        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .languagePreferenceWelsh(Yes)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.DECISION_OUTCOME)
            .finalDocument(Document.builder().documentFileName("C100AppDoc").documentUrl("Url").build())
            .c1ADocument(Document.builder().documentFileName("c1ADocument").documentUrl("Url").build())
            .bundleInformation(BundlingInformation.builder().build())
            .finalWelshDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("finalWelshDoc.pdf").build())
            .c1AWelshDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("C1AWelshDoc.pdf").build())
            .reviewDocuments(ReviewDocuments.builder().build())
            .build();

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(c100CaseData,"eventI",
            Hearings.hearingsWith().build(), "sample.yaml");
        assertNotNull(bundleCreateRequest);
        Assert.assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime());
        Assert.assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingJudgeName());
        Assert.assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingVenueAddress());
    }

    @Test
    public void testBundleCreateRequestMapperWhenHmcStatusAsCancelled() {
        List<HearingDaySchedule> hearingDaySchedules = new ArrayList<>();
        hearingDaySchedules.add(HearingDaySchedule.hearingDayScheduleWith().build());
        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith().hmcStatus(CANCELLED).hearingDaySchedule(hearingDaySchedules).build());

        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .languagePreferenceWelsh(Yes)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.DECISION_OUTCOME)
            .finalDocument(Document.builder().documentFileName("C100AppDoc").documentUrl("Url").build())
            .c1ADocument(Document.builder().documentFileName("c1ADocument").documentUrl("Url").build())
            .bundleInformation(BundlingInformation.builder().build())
            .finalWelshDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("finalWelshDoc.pdf").build())
            .c1AWelshDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("C1AWelshDoc.pdf").build())
            .reviewDocuments(ReviewDocuments.builder().build())
            .build();

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(c100CaseData,"eventI",
            Hearings.hearingsWith().caseHearings(caseHearings).build(), "sample.yaml");
        assertNotNull(bundleCreateRequest);
        Assert.assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime());
        Assert.assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingJudgeName());
        Assert.assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingVenueAddress());
    }

}
