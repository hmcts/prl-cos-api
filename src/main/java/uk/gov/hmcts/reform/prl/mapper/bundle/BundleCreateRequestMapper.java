package uk.gov.hmcts.reform.prl.mapper.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.bundle.BundlingDocGroupEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleHearingInfo;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingCaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ANY_OTHER_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANTS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_STATMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_LA_OTHER_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_SUMMARY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DNA_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRUG_AND_ALCOHOL_TESTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRUG_AND_ALCOHOL_TESTS_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.GUARDIAN_REPORT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LETTERS_FROM_SCHOOL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MAGISTRATES_FACTS_AND_REASONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_RECORDS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_RECORDS_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MIAM_CERTIFICATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_WITNESS_STATEMENTS_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PATERNITY_TEST_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POLICE_DISCLOSURES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POLICE_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POLICE_REPORT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PREVIOUS_ORDERS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENTS_STATEMENTS;
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
import static uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts.restrictToGroup;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BundleCreateRequestMapper {
    public BundleCreateRequest mapCaseDataToBundleCreateRequest(CaseData caseData, String eventId, Hearings hearingDetails,
                                                                String bundleConfigFileName) {
        BundleCreateRequest bundleCreateRequest = BundleCreateRequest.builder()
            .caseDetails(BundlingCaseDetails.builder()
                .bundleName(caseData.getApplicantName())
                .caseData(mapCaseData(caseData,hearingDetails,
                    bundleConfigFileName))
                .build())
            .caseTypeId(CASE_TYPE).jurisdictionId(JURISDICTION).eventId(eventId).build();
        log.info("*** create bundle request mapped for the case id  : {}", caseData.getId());
        return bundleCreateRequest;
    }

    private BundlingCaseData mapCaseData(CaseData caseData, Hearings hearingDetails, String bundleConfigFileName) {
        return BundlingCaseData.builder().id(String.valueOf(caseData.getId())).bundleConfiguration(
                bundleConfigFileName)
            .data(BundlingData.builder().caseNumber(String.valueOf(caseData.getId())).applicantCaseName(caseData.getApplicantCaseName())
                .hearingDetails(mapHearingDetails(hearingDetails))
                .applications(mapApplicationsFromCaseData(caseData))
                .orders(mapOrdersFromCaseData(caseData.getOrderCollection()))
                .allOtherDocuments(mapAllOtherDocuments(caseData)).build()).build();
    }

    private BundleHearingInfo mapHearingDetails(Hearings hearingDetails) {
        if (null != hearingDetails && null != hearingDetails.getCaseHearings()) {
            List<CaseHearing> listedCaseHearings = hearingDetails.getCaseHearings().stream()
                .filter(caseHearing -> LISTED.equalsIgnoreCase(caseHearing.getHmcStatus())).toList();
            if (null != listedCaseHearings && !listedCaseHearings.isEmpty()) {
                List<HearingDaySchedule> hearingDaySchedules = listedCaseHearings.get(0).getHearingDaySchedule();
                if (null != hearingDaySchedules && !hearingDaySchedules.isEmpty()) {
                    return BundleHearingInfo.builder().hearingVenueAddress(getHearingVenueAddress(hearingDaySchedules.get(0)))
                        .hearingDateAndTime(null != hearingDaySchedules.get(0).getHearingStartDateTime()
                            ? hearingDaySchedules.get(0).getHearingStartDateTime().toString() : BLANK_STRING)
                        .hearingJudgeName(hearingDaySchedules.get(0).getHearingJudgeName()).build();
                }
            }
        }
        return BundleHearingInfo.builder().build();
    }

    private String getHearingVenueAddress(HearingDaySchedule hearingDaySchedule) {
        return null != hearingDaySchedule.getHearingVenueName()
            ? hearingDaySchedule.getHearingVenueName() + "\n" +  hearingDaySchedule.getHearingVenueAddress()
            : hearingDaySchedule.getHearingVenueAddress();
    }

    private List<Element<BundlingRequestDocument>> mapAllOtherDocuments(CaseData caseData) {

        List<Element<BundlingRequestDocument>> allOtherDocuments = new ArrayList<>();

        List<Element<BundlingRequestDocument>> fl401SupportingDocs = mapFl401SupportingDocs(caseData.getFl401UploadSupportDocuments());
        if (!fl401SupportingDocs.isEmpty()) {
            allOtherDocuments.addAll(fl401SupportingDocs);
        }
        List<Element<BundlingRequestDocument>> fl401WitnessDocs = mapFl401WitnessDocs(caseData.getFl401UploadWitnessDocuments());
        if (!fl401WitnessDocs.isEmpty()) {
            allOtherDocuments.addAll(fl401WitnessDocs);
        }
        List<Element<BundlingRequestDocument>> citizenUploadedDocuments =
            mapBundlingDocsFromCitizenUploadedDocs(caseData.getReviewDocuments().getCitizenUploadedDocListDocTab());
        if (null != citizenUploadedDocuments && !citizenUploadedDocuments.isEmpty()) {
            allOtherDocuments.addAll(citizenUploadedDocuments);
        }
        //SNI-4260 fix
        //Updated to retrieve otherDocuments according to the new manageDocuments event
        List<Element<BundlingRequestDocument>> otherDocuments = mapOtherDocumentsFromCaseData(caseData);
        if (null != otherDocuments && !otherDocuments.isEmpty()) {
            allOtherDocuments.addAll(otherDocuments);
        }
        return allOtherDocuments;

    }


    private List<Element<BundlingRequestDocument>> mapFl401WitnessDocs(List<Element<Document>> fl401UploadWitnessDocuments) {
        List<Element<BundlingRequestDocument>> fl401WitnessDocs = new ArrayList<>();
        Optional<List<Element<Document>>> existingfl401WitnessDocs = ofNullable(fl401UploadWitnessDocuments);
        if (existingfl401WitnessDocs.isEmpty()) {
            return fl401WitnessDocs;
        }
        ElementUtils.unwrapElements(fl401UploadWitnessDocuments).forEach(witnessDocs ->
            fl401WitnessDocs.add(ElementUtils.element(mapBundlingRequestDocument(witnessDocs,
                BundlingDocGroupEnum.applicantWitnessStatements))));
        return fl401WitnessDocs;
    }

    private List<Element<BundlingRequestDocument>> mapFl401SupportingDocs(List<Element<Document>> fl401UploadSupportDocuments) {
        List<Element<BundlingRequestDocument>> fl401SupportingDocs = new ArrayList<>();
        Optional<List<Element<Document>>> existingfl401SupportingDocs = ofNullable(fl401UploadSupportDocuments);
        if (existingfl401SupportingDocs.isEmpty()) {
            return fl401SupportingDocs;
        }
        ElementUtils.unwrapElements(fl401UploadSupportDocuments).forEach(supportDocs ->
            fl401SupportingDocs.add(ElementUtils.element(mapBundlingRequestDocument(supportDocs,
                BundlingDocGroupEnum.applicantStatementSupportingEvidence))));
        return fl401SupportingDocs;
    }

    private List<Element<BundlingRequestDocument>> mapApplicationsFromCaseData(CaseData caseData) {
        List<BundlingRequestDocument> applications = new ArrayList<>();

        if (YesOrNo.Yes.equals(caseData.getLanguagePreferenceWelsh())) {
            if (null != caseData.getFinalWelshDocument()) {
                applications.add(mapBundlingRequestDocument(caseData.getFinalWelshDocument(), BundlingDocGroupEnum.applicantApplication));
            }
            if (null != caseData.getC1AWelshDocument()) {
                applications.add(mapBundlingRequestDocument(caseData.getC1AWelshDocument(), BundlingDocGroupEnum.applicantC1AApplication));
            }
        } else {
            if (null != caseData.getFinalDocument()) {
                applications.add(mapBundlingRequestDocument(caseData.getFinalDocument(), BundlingDocGroupEnum.applicantApplication));
            }
            if (null != caseData.getC1ADocument()) {
                applications.add(mapBundlingRequestDocument(caseData.getC1ADocument(), BundlingDocGroupEnum.applicantC1AApplication));
            }
        }
        List<BundlingRequestDocument> miamCertAndPreviousOrdersUploadedByCourtAdmin =
            mapApplicationsFromFurtherEvidences(caseData.getFurtherEvidences());
        if (!miamCertAndPreviousOrdersUploadedByCourtAdmin.isEmpty()) {
            applications.addAll(miamCertAndPreviousOrdersUploadedByCourtAdmin);
        }
        mapMiamDetails(caseData.getMiamDetails(),applications);
        List<BundlingRequestDocument> citizenUploadedC7Documents = mapC7DocumentsFromCaseData(caseData.getCitizenResponseC7DocumentList());
        if (!citizenUploadedC7Documents.isEmpty()) {
            applications.addAll(citizenUploadedC7Documents);
        }
        return ElementUtils.wrapElements(applications);
    }

    private void mapMiamDetails(MiamDetails miamDetails, List<BundlingRequestDocument> applications) {
        if (null != miamDetails) {
            Document miamCertificateUpload = miamDetails.getMiamCertificationDocumentUpload();
            if (null != miamCertificateUpload) {
                applications.add(mapBundlingRequestDocument(miamCertificateUpload, BundlingDocGroupEnum.applicantMiamCertificate));
            }
            Document miamCertificateUpload1 = miamDetails.getMiamCertificationDocumentUpload1();
            if (null != miamCertificateUpload1) {
                applications.add(mapBundlingRequestDocument(miamCertificateUpload1, BundlingDocGroupEnum.applicantMiamCertificate));
            }
        }
    }

    private List<BundlingRequestDocument> mapC7DocumentsFromCaseData(List<Element<ResponseDocuments>> citizenResponseC7DocumentList) {
        List<BundlingRequestDocument> applications = new ArrayList<>();
        Optional<List<Element<ResponseDocuments>>> uploadedC7CitizenDocs = ofNullable(citizenResponseC7DocumentList);
        if (uploadedC7CitizenDocs.isEmpty()) {
            return applications;
        }
        ElementUtils.unwrapElements(citizenResponseC7DocumentList).forEach(c7CitizenResponseDocument ->
            applications.add(mapBundlingRequestDocument(c7CitizenResponseDocument.getCitizenDocument(),
                BundlingDocGroupEnum.c7Documents)));
        return applications;
    }

    private BundlingRequestDocument mapBundlingRequestDocument(Document document, BundlingDocGroupEnum applicationsDocGroup) {
        return (null != document) ? BundlingRequestDocument.builder().documentLink(document).documentFileName(document.getDocumentFileName())
            .documentGroup(applicationsDocGroup).build() : BundlingRequestDocument.builder().build();
    }

    private List<BundlingRequestDocument> mapApplicationsFromFurtherEvidences(List<Element<FurtherEvidence>> furtherEvidencesFromCaseData) {
        List<BundlingRequestDocument> applications = new ArrayList<>();
        Optional<List<Element<FurtherEvidence>>> existingFurtherEvidences = ofNullable(furtherEvidencesFromCaseData);
        if (existingFurtherEvidences.isEmpty()) {
            return applications;
        }
        ElementUtils.unwrapElements(furtherEvidencesFromCaseData).forEach(furtherEvidence -> {
            if (!furtherEvidence.getRestrictCheckboxFurtherEvidence().contains(restrictToGroup)) {
                if (FurtherEvidenceDocumentType.miamCertificate.equals(furtherEvidence.getTypeOfDocumentFurtherEvidence())) {
                    applications.add(mapBundlingRequestDocument(furtherEvidence.getDocumentFurtherEvidence(),
                        BundlingDocGroupEnum.applicantMiamCertificate));
                } else if (FurtherEvidenceDocumentType.previousOrders.equals(furtherEvidence.getTypeOfDocumentFurtherEvidence())) {
                    applications.add(mapBundlingRequestDocument(furtherEvidence.getDocumentFurtherEvidence(),
                        BundlingDocGroupEnum.applicantPreviousOrdersSubmittedWithApplication));
                }
            }
        });
        return applications;
    }

    private List<Element<BundlingRequestDocument>> mapOrdersFromCaseData(List<Element<OrderDetails>> ordersFromCaseData) {
        List<BundlingRequestDocument> orders = new ArrayList<>();
        Optional<List<Element<OrderDetails>>> existingOrders = ofNullable(ordersFromCaseData);
        if (existingOrders.isEmpty()) {
            return new ArrayList<>();
        }
        ordersFromCaseData.forEach(orderDetailsElement -> {
            OrderDetails orderDetails = orderDetailsElement.getValue();
            Document document = orderDetails.getOrderDocument();
            orders.add(BundlingRequestDocument.builder().documentGroup(BundlingDocGroupEnum.ordersSubmittedWithApplication)
                .documentFileName(document.getDocumentFileName()).documentLink(document).build());
        });
        return ElementUtils.wrapElements(orders);
    }

    //SNI-4260 fix
    //Updated to retrieve otherDocuments according to the new manageDocuments event
    private List<Element<BundlingRequestDocument>> mapOtherDocumentsFromCaseData(
        CaseData caseData) {
        List<Element<QuarantineLegalDoc>>  allDocuments = new ArrayList<>();
        if (null != caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab()
            && !caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab().isEmpty()) {
            List<Element<QuarantineLegalDoc>> courtStaffUploadDocList = caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab();
            allDocuments.addAll(courtStaffUploadDocList);
        }
        if (null != caseData.getReviewDocuments().getCafcassUploadDocListDocTab()
            && !caseData.getReviewDocuments().getCafcassUploadDocListDocTab().isEmpty()) {
            List<Element<QuarantineLegalDoc>> cafcassUploadDocList = caseData.getReviewDocuments().getCafcassUploadDocListDocTab();
            allDocuments.addAll(cafcassUploadDocList);
        }
        if (null != caseData.getReviewDocuments().getLegalProfUploadDocListDocTab()
            && !caseData.getReviewDocuments().getLegalProfUploadDocListDocTab().isEmpty()) {
            List<Element<QuarantineLegalDoc>> legalProfUploadDocList = caseData.getReviewDocuments().getLegalProfUploadDocListDocTab();
            allDocuments.addAll(legalProfUploadDocList);
        }
        List<BundlingRequestDocument> otherBundlingDocuments = new ArrayList<>();
        List<QuarantineLegalDoc> allDocs = ElementUtils.unwrapElements(allDocuments);
        for (QuarantineLegalDoc doc : allDocs) {
            BundlingRequestDocument otherDoc = mapBundlingRequestDocumentForOtherDocs(doc);
            if (null != otherDoc) {
                otherBundlingDocuments.add(otherDoc);
            }
        }
        return ElementUtils.wrapElements(otherBundlingDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapBundlingDocsFromCitizenUploadedDocs(List<Element<QuarantineLegalDoc>>
                                                                                              citizenQuarantineDocumentList) {
        List<BundlingRequestDocument> bundlingCitizenDocuments = new ArrayList<>();
        Optional<List<Element<QuarantineLegalDoc>>> citizenQuarantineDocuments = ofNullable(citizenQuarantineDocumentList);
        if (citizenQuarantineDocuments.isEmpty()) {
            return new ArrayList<>();
        }
        citizenQuarantineDocumentList.forEach(citizenQuarantineDocumentElement -> {
            QuarantineLegalDoc quarantineLegalDoc = citizenQuarantineDocumentElement.getValue();
            //FIX TO FETCH DOCUMENT LATER
            Document uploadedDocument = quarantineLegalDoc.getCitizenQuarantineDocument();
            bundlingCitizenDocuments.add(BundlingRequestDocument.builder()
                //FIX TO FETCH CATEGORY FROM DocumentCategory
                //.documentGroup(getDocumentGroupForCitizen(uploadedDocuments.getIsApplicant(), uploadedDocuments.getDocumentType()))
                .documentFileName(uploadedDocument.getDocumentFileName())
                .documentLink(uploadedDocument).build());

        });
        return ElementUtils.wrapElements(bundlingCitizenDocuments);
    }

    private BundlingDocGroupEnum getDocumentGroupForCitizen(String isApplicant, String docType) {
        BundlingDocGroupEnum bundlingDocGroupEnum = BundlingDocGroupEnum.notRequiredGroup;
        switch (docType) {
            case YOUR_POSITION_STATEMENTS:
                bundlingDocGroupEnum =  BundlingDocGroupEnum.positionStatements;
                break;
            case YOUR_WITNESS_STATEMENTS:
                bundlingDocGroupEnum = PrlAppsConstants.NO.equals(isApplicant) ? BundlingDocGroupEnum.respondentWitnessStatements :
                    BundlingDocGroupEnum.applicantWitnessStatements;
                break;
            case LETTERS_FROM_SCHOOL:
                bundlingDocGroupEnum = PrlAppsConstants.NO.equals(isApplicant) ? BundlingDocGroupEnum.respondentLettersFromSchool :
                    BundlingDocGroupEnum.applicantLettersFromSchool;
                break;
            case OTHER_WITNESS_STATEMENTS:
                bundlingDocGroupEnum =  BundlingDocGroupEnum.otherWitnessStatements;
                break;
            case MEDICAL_REPORTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.expertMedicalReports;
                break;
            case MEDICAL_RECORDS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.expertMedicalRecords;
                break;
            case PATERNITY_TEST_REPORTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.dnaReports;
                break;
            case DRUG_AND_ALCOHOL_TESTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.reportsForDrugAndAlcoholTest;
                break;
            case POLICE_REPORTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.policeReports;
                break;
            case CAFCASS_REPORTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.cafcassReportsUploadedByCourtAdmin;
                break;
            case EXPERT_REPORTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.expertReportsUploadedByCourtAdmin;
                break;
            case APPLICANT_STATMENT:
                bundlingDocGroupEnum = BundlingDocGroupEnum.applicantStatementDocsUploadedByCourtAdmin;
                break;
            default:
                break;
        }
        return bundlingDocGroupEnum;
    }

    private BundlingRequestDocument mapBundlingRequestDocumentForOtherDocs(QuarantineLegalDoc doc) {
        BundlingRequestDocument bundlingRequestDocument = null;
        log.info("****** In BundleCreateRequestMapper method getDocumentGroup");
        String docType = doc.getCategoryName();
        switch (docType) {
            case POSITION_STATEMENTS:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getPositionStatementsDocument())
                    .documentFileName(doc.getPositionStatementsDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.positionStatements).build();
                break;
            case OTHER_WITNESS_STATEMENTS_DOCUMENT:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getOtherWitnessStatementsDocument())
                    .documentFileName(doc.getOtherWitnessStatementsDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.otherWitnessStatements).build();
                break;
            case MEDICAL_REPORTS:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getMedicalReportsDocument())
                    .documentFileName(doc.getMedicalReportsDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.expertMedicalReports).build();
                break;
            case MEDICAL_RECORDS_DOCUMENT:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getMedicalRecordsDocument())
                    .documentFileName(doc.getMedicalRecordsDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.expertMedicalRecords).build();
                break;
            case DRUG_AND_ALCOHOL_TESTS_DOCUMENT:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getDrugAndAlcoholTestDocument())
                    .documentFileName(doc.getDrugAndAlcoholTestDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.reportsForDrugAndAlcoholTest).build();
                break;
            case POLICE_REPORT_DOCUMENT:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getPoliceReportDocument())
                    .documentFileName(doc.getPoliceReportDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.policeReports).build();
                break;
            case DNA_REPORTS:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getDnaReportsExpertReportDocument())
                    .documentFileName(doc.getDnaReportsExpertReportDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.dnaReports).build();
                break;
            case RESULTS_OF_HAIR_STRAND_BLOOD_TESTS:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getResultsOfHairStrandBloodTestsDocument())
                    .documentFileName(doc.getResultsOfHairStrandBloodTestsDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.resultsOfHairStrandBloodTests).build();
                break;
            case POLICE_DISCLOSURES:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getPoliceDisclosuresDocument())
                    .documentFileName(doc.getPoliceDisclosuresDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.policeDisclosures).build();
                break;
            case APPLICANTS_STATEMENTS:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getApplicantStatementsDocument())
                    .documentFileName(doc.getApplicantStatementsDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.applicantWitnessStatements).build();
                break;
            case RESPONDENTS_STATEMENTS:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getRespondentStatementsDocument())
                    .documentFileName(doc.getRespondentStatementsDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.respondentWitnessStatements).build();
                break;
            case APPLICANT_C1A_RESPONSE:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getApplicantC1AResponseDocument())
                    .documentFileName(doc.getApplicantC1AResponseDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.applicantC1AResponse).build();
                break;
            case RESPONDENT_APPLCATION:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getRespondentApplicationDocument())
                    .documentFileName(doc.getRespondentApplicationDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.respondentApplication).build();
                break;
            case RESPONDENT_C1A_APPLCATION:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getRespondentC1AApplicationDocument())
                    .documentFileName(doc.getRespondentC1AApplicationDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.respondentC1AApplication).build();
                break;
            case RESPONDENT_C1A_RESPONSE:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getRespondentC1AResponseDocument())
                    .documentFileName(doc.getRespondentC1AResponseDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.respondentC1AResponse).build();
                break;
            case CASE_SUMMARY:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getCaseSummaryDocument())
                    .documentFileName(doc.getCaseSummaryDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.caseSummary).build();
                break;
            case TRANSCRIPTS_OF_JUDGEMENTS:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getTranscriptsOfJudgementsDocument())
                    .documentFileName(doc.getTranscriptsOfJudgementsDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.transcriptsOfJudgements).build();
                break;
            case MAGISTRATES_FACTS_AND_REASONS:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getMagistratesFactsAndReasonsDocument())
                    .documentFileName(doc.getMagistratesFactsAndReasonsDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.magistratesFactsAndReasons).build();
                break;
            case SAFEGUARDING_LETTER:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getSafeguardingLetterDocument())
                    .documentFileName(doc.getSafeguardingLetterDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.safeguardingLetter).build();
                break;
            case SECTION_7_REPORT:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getSection7ReportDocument())
                    .documentFileName(doc.getSection7ReportDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.section7Report).build();
                break;
            case SIXTEENA_RISK_ASSESSMENT:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getSixteenARiskAssessmentDocument())
                    .documentFileName(doc.getSixteenARiskAssessmentDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.sixteenARiskAssessment).build();
                break;
            case GUARDIAN_REPORT:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getGuardianReportDocument())
                    .documentFileName(doc.getGuardianReportDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.guardianReport).build();
                break;
            case SPECIAL_GUARDIANSHIP_REPORT:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getSpecialGuardianshipReportDocument())
                    .documentFileName(doc.getSpecialGuardianshipReportDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.specialGuardianshipReport).build();
                break;
            case SECTION_37_REPORT:
                if (doc.getSection37ReportDocument() != null) {
                    bundlingRequestDocument = BundlingRequestDocument.builder()
                        .documentLink(doc.getSection37ReportDocument())
                        .documentFileName(doc.getSection37ReportDocument().getDocumentFileName())
                        .documentGroup(BundlingDocGroupEnum.cafcassSection37Report).build();
                } else {
                    bundlingRequestDocument = BundlingRequestDocument.builder()
                        .documentLink(doc.getSec37ReportDocument())
                        .documentFileName(doc.getSec37ReportDocument().getDocumentFileName())
                        .documentGroup(BundlingDocGroupEnum.laSection37Report).build();
                }
                break;
            case CAFCASS_LA_OTHER_DOCUMENTS:
                if (doc.getOtherDocsDocument() != null) {
                    bundlingRequestDocument = BundlingRequestDocument.builder()
                        .documentLink(doc.getOtherDocsDocument())
                        .documentFileName(doc.getOtherDocsDocument().getDocumentFileName())
                        .documentGroup(BundlingDocGroupEnum.cafcassOtherDocuments).build();
                } else {
                    bundlingRequestDocument = BundlingRequestDocument.builder()
                        .documentLink(doc.getLocalAuthorityOtherDocDocument())
                        .documentFileName(doc.getLocalAuthorityOtherDocDocument().getDocumentFileName())
                        .documentGroup(BundlingDocGroupEnum.laOtherDocuments).build();
                }
                break;
            case MIAM_CERTIFICATE:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getMiamCertificateDocument())
                    .documentFileName(doc.getMiamCertificateDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.applicantMiamCertificate).build();
                break;
            case PREVIOUS_ORDERS:
                if (doc.getPreviousOrdersSubmittedWithApplicationDocument() != null) {
                    bundlingRequestDocument = BundlingRequestDocument.builder()
                        .documentLink(doc.getPreviousOrdersSubmittedWithApplicationDocument())
                        .documentFileName(doc.getPreviousOrdersSubmittedWithApplicationDocument().getDocumentFileName())
                        .documentGroup(BundlingDocGroupEnum.applicantPreviousOrdersSubmittedWithApplication).build();
                } else {
                    bundlingRequestDocument = BundlingRequestDocument.builder()
                        .documentLink(doc.getOrdersFromOtherProceedingsDocument())
                        .documentFileName(doc.getOrdersFromOtherProceedingsDocument().getDocumentFileName())
                        .documentGroup(BundlingDocGroupEnum.respondentPreviousOrdersSubmittedWithApplication).build();
                }
                break;
            case ANY_OTHER_DOCUMENTS:
                bundlingRequestDocument = BundlingRequestDocument.builder()
                    .documentLink(doc.getAnyOtherDocDocument())
                    .documentFileName(doc.getAnyOtherDocDocument().getDocumentFileName())
                    .documentGroup(BundlingDocGroupEnum.anyOtherDocuments).build();
                break;
            default:
                break;
        }
        return bundlingRequestDocument;
    }
}
