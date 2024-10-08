package uk.gov.hmcts.reform.prl.mapper.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.prl.enums.bundle.BundlingDocGroupEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.reverse;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ANY_OTHER_DOC;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CASE_SUMMARY;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.DNA_REPORTS_EXPERT_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.DRUG_AND_ALCOHOL_TEST;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.GUARDIAN_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.LA_OTHER_DOCS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MAGISTRATES_FACTS_AND_REASONS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MEDICAL_RECORDS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MEDICAL_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MIAM_CERTIFICATE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ORDERS_FROM_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.OTHER_DOCS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.OTHER_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.POLICE_DISCLOSURES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.POLICE_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION;
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
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.TRANSCRIPTS_OF_JUDGEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
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
                            ? getBundleDateTime(hearingDaySchedules.get(0).getHearingStartDateTime()) : BLANK_STRING)
                        .hearingJudgeName(hearingDaySchedules.get(0).getHearingJudgeName()).build();
                }
            }
        }
        return BundleHearingInfo.builder().build();
    }

    public static String getBundleDateTime(LocalDateTime bundleDateTime) {
        StringBuilder newBundleDateTime = new StringBuilder();
        LocalDateTime ldt = CaseUtils.convertUtcToBst(bundleDateTime);

        return newBundleDateTime
            .append(bundleDateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)))
            .append(EMPTY_SPACE_STRING)
            .append(CaseUtils.convertLocalDateTimeToAmOrPmTime(ldt))
            .toString();
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

        FL401OtherProceedingDetails fl401OtherProceedingDetails = caseData.getFl401OtherProceedingDetails();
        if (fl401OtherProceedingDetails != null) {
            List<Element<BundlingRequestDocument>> fl401ApplicantOtherProceedingsDocs = mapFl401OtherProceedings(
                caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings());
            if (!fl401ApplicantOtherProceedingsDocs.isEmpty()) {
                allOtherDocuments.addAll(fl401ApplicantOtherProceedingsDocs);
            }
        }

        List<Element<BundlingRequestDocument>> c100ApplicantOtherProceedingsDocs = mapC100OtherProceedings(caseData.getExistingProceedingsWithDoc());
        if (!c100ApplicantOtherProceedingsDocs.isEmpty()) {
            allOtherDocuments.addAll(c100ApplicantOtherProceedingsDocs);
        }

        //SNI-4260 fix
        //Updated to retrieve otherDocuments according to the new manageDocuments event
        List<Element<BundlingRequestDocument>> otherDocuments = mapOtherDocumentsFromCaseData(caseData);
        if (null != otherDocuments && !otherDocuments.isEmpty()) {
            allOtherDocuments.addAll(otherDocuments);
        }

        List<Element<BundlingRequestDocument>> miamCertAndPreviousOrdersUploadedByCourtAdmin =
            mapApplicationsFromFurtherEvidences(caseData.getFurtherEvidences());
        if (!miamCertAndPreviousOrdersUploadedByCourtAdmin.isEmpty()) {
            allOtherDocuments.addAll(miamCertAndPreviousOrdersUploadedByCourtAdmin);
        }

        List<Element<BundlingRequestDocument>> miamDocuments = mapMiamDetails(caseData.getMiamDetails());
        if (null != miamDocuments && !miamDocuments.isEmpty()) {
            allOtherDocuments.addAll(miamDocuments);
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

    private List<Element<BundlingRequestDocument>> mapFl401OtherProceedings(List<Element<FL401Proceedings>> fl401OtherProceedingDocuments) {
        List<Element<BundlingRequestDocument>> fl401OtherProceedingDocs = new ArrayList<>();
        ElementUtils.unwrapElements(fl401OtherProceedingDocuments)
            .forEach(otherProceeding ->
                         fl401OtherProceedingDocs.add(ElementUtils.element(
                             mapBundlingRequestDocument(
                                 otherProceeding.getUploadRelevantOrder(),
                                 BundlingDocGroupEnum.applicantPreviousOrdersSubmittedWithApplication
                             ))));
        return fl401OtherProceedingDocs;
    }

    private List<Element<BundlingRequestDocument>> mapC100OtherProceedings(List<Element<ProceedingDetails>> c100OtherProceedingDocuments) {
        List<Element<BundlingRequestDocument>> c100OtherProceedingDocs = new ArrayList<>();
        ElementUtils.unwrapElements(c100OtherProceedingDocuments)
            .forEach(otherProceeding ->
                         c100OtherProceedingDocs.add(ElementUtils.element(
                             mapBundlingRequestDocument(
                                 otherProceeding.getUploadRelevantOrder(),
                                 BundlingDocGroupEnum.applicantPreviousOrdersSubmittedWithApplication
                             ))));
        return c100OtherProceedingDocs;
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

        if (null != caseData.getFinalDocument()) {
            applications.add(mapBundlingRequestDocument(caseData.getFinalDocument(), BundlingDocGroupEnum.applicantApplication));
        }
        if (null != caseData.getFinalWelshDocument()) {
            applications.add(mapBundlingRequestDocument(caseData.getFinalWelshDocument(), BundlingDocGroupEnum.applicantApplication));
        }

        if (null != caseData.getC1ADocument()) {
            applications.add(mapBundlingRequestDocument(caseData.getC1ADocument(), BundlingDocGroupEnum.applicantC1AApplication));
        }
        if (null != caseData.getC1AWelshDocument()) {
            applications.add(mapBundlingRequestDocument(caseData.getC1AWelshDocument(), BundlingDocGroupEnum.applicantC1AApplication));
        }

        List<BundlingRequestDocument> citizenUploadedC7Documents = mapC7DocumentsFromCaseData(caseData.getCitizenResponseC7DocumentList());
        if (!citizenUploadedC7Documents.isEmpty()) {
            applications.addAll(citizenUploadedC7Documents);
        }
        return ElementUtils.wrapElements(applications);
    }

    private List<Element<BundlingRequestDocument>> mapMiamDetails(MiamDetails miamDetails) {
        List<BundlingRequestDocument> miamBundlingDocuments = new ArrayList<>();
        if (null != miamDetails) {
            Document miamCertificateUpload = miamDetails.getMiamCertificationDocumentUpload();
            if (null != miamCertificateUpload) {
                miamBundlingDocuments.add(mapBundlingRequestDocument(miamCertificateUpload, BundlingDocGroupEnum.applicantMiamCertificate));
            }
            Document miamCertificateUpload1 = miamDetails.getMiamCertificationDocumentUpload1();
            if (null != miamCertificateUpload1) {
                miamBundlingDocuments.add(mapBundlingRequestDocument(miamCertificateUpload1, BundlingDocGroupEnum.applicantMiamCertificate));
            }
        }
        return ElementUtils.wrapElements(miamBundlingDocuments);
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

    private List<Element<BundlingRequestDocument>> mapApplicationsFromFurtherEvidences(List<Element<FurtherEvidence>> furtherEvidencesFromCaseData) {
        List<BundlingRequestDocument> applications = new ArrayList<>();
        Optional<List<Element<FurtherEvidence>>> existingFurtherEvidences = ofNullable(furtherEvidencesFromCaseData);
        if (existingFurtherEvidences.isEmpty()) {
            return ElementUtils.wrapElements(applications);
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
        return ElementUtils.wrapElements(applications);
    }

    private List<Element<BundlingRequestDocument>> mapOrdersFromCaseData(List<Element<OrderDetails>> ordersFromCaseData) {
        List<BundlingRequestDocument> orders = new ArrayList<>();
        Optional<List<Element<OrderDetails>>> existingOrders = ofNullable(ordersFromCaseData);
        if (existingOrders.isEmpty()) {
            return new ArrayList<>();
        }

        reverse(ordersFromCaseData);
        ordersFromCaseData.forEach(orderDetailsElement -> {
            OrderDetails orderDetails = orderDetailsElement.getValue();
            Document document = orderDetails.getOrderDocument();
            orders.add(BundlingRequestDocument.builder().documentGroup(BundlingDocGroupEnum.ordersSubmittedWithApplication)
                .documentFileName(document.getDocumentFileName()).documentLink(document).build());
            Document welshDocument = orderDetails.getOrderDocumentWelsh();
            if (welshDocument != null) {
                orders.add(BundlingRequestDocument.builder().documentGroup(BundlingDocGroupEnum.ordersSubmittedWithApplication)
                               .documentFileName(welshDocument.getDocumentFileName()).documentLink(welshDocument).build());
            }
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

        if (null != caseData.getReviewDocuments().getCitizenUploadedDocListDocTab()
            && !caseData.getReviewDocuments().getCitizenUploadedDocListDocTab().isEmpty()) {
            List<Element<QuarantineLegalDoc>> citizenUploadedDocuments = caseData.getReviewDocuments().getCitizenUploadedDocListDocTab();
            allDocuments.addAll(citizenUploadedDocuments);
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



    private BundlingRequestDocument mapBundlingRequestDocumentForOtherDocs(QuarantineLegalDoc doc) {
        HashMap<String, BundlingRequestDocument> bundleMap = new HashMap<>();
        log.info("****** In BundleCreateRequestMapper method getDocumentGroup");

        bundleMap.put(
            POSITION_STATEMENTS,
            Objects.nonNull(doc.getPositionStatementsDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getPositionStatementsDocument())
                .documentFileName(doc.getPositionStatementsDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.positionStatements).build() : null
        );
        bundleMap.put(
            OTHER_WITNESS_STATEMENTS,
            Objects.nonNull(doc.getOtherWitnessStatementsDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getOtherWitnessStatementsDocument())
                .documentFileName(doc.getOtherWitnessStatementsDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.otherWitnessStatements).build() : null
        );
        bundleMap.put(
            MEDICAL_REPORTS,
            Objects.nonNull(doc.getMedicalReportsDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getMedicalReportsDocument())
                .documentFileName(doc.getMedicalReportsDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.medicalReports).build() : null
        );
        bundleMap.put(
            MEDICAL_RECORDS,
            Objects.nonNull(doc.getMedicalRecordsDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getMedicalRecordsDocument())
                .documentFileName(doc.getMedicalRecordsDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.medicalRecords).build() : null
        );
        bundleMap.put(
            DRUG_AND_ALCOHOL_TEST,
            Objects.nonNull(doc.getDrugAndAlcoholTestDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getDrugAndAlcoholTestDocument())
                .documentFileName(doc.getDrugAndAlcoholTestDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.reportsForDrugAndAlcoholTest).build() : null
        );
        bundleMap.put(POLICE_REPORT, Objects.nonNull(doc.getPoliceReportDocument()) ? BundlingRequestDocument.builder()
            .documentLink(doc.getPoliceReportDocument())
            .documentFileName(doc.getPoliceReportDocument().getDocumentFileName())
            .documentGroup(BundlingDocGroupEnum.policeReport).build() : null);
        bundleMap.put(
            DNA_REPORTS_EXPERT_REPORT,
            Objects.nonNull(doc.getDnaReportsExpertReportDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getDnaReportsExpertReportDocument())
                .documentFileName(doc.getDnaReportsExpertReportDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.dnaReports).build() : null
        );
        bundleMap.put(
            RESULTS_OF_HAIR_STRAND_BLOOD_TESTS,
            Objects.nonNull(doc.getResultsOfHairStrandBloodTestsDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getResultsOfHairStrandBloodTestsDocument())
                .documentFileName(doc.getResultsOfHairStrandBloodTestsDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.resultsOfHairStrandBloodTests).build() : null
        );
        bundleMap.put(
            POLICE_DISCLOSURES,
            Objects.nonNull(doc.getPoliceDisclosuresDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getPoliceDisclosuresDocument())
                .documentFileName(doc.getPoliceDisclosuresDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.policeDisclosures).build() : null
        );
        bundleMap.put(
            APPLICANT_STATEMENTS,
            Objects.nonNull(doc.getApplicantStatementsDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getApplicantStatementsDocument())
                .documentFileName(doc.getApplicantStatementsDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.applicantWitnessStatements).build() : null
        );
        bundleMap.put(
            RESPONDENT_STATEMENTS,
            Objects.nonNull(doc.getRespondentStatementsDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getRespondentStatementsDocument())
                .documentFileName(doc.getRespondentStatementsDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.respondentWitnessStatements).build() : null
        );
        bundleMap.put(
            APPLICANT_C1A_RESPONSE,
            Objects.nonNull(doc.getApplicantC1AResponseDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getApplicantC1AResponseDocument())
                .documentFileName(doc.getApplicantC1AResponseDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.applicantC1AResponse).build() : null
        );
        bundleMap.put(
            RESPONDENT_APPLICATION,
            Objects.nonNull(doc.getRespondentApplicationDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getRespondentApplicationDocument())
                .documentFileName(doc.getRespondentApplicationDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.respondentApplication).build() : null
        );
        bundleMap.put(
            RESPONDENT_C1A_APPLICATION,
            Objects.nonNull(doc.getRespondentC1AApplicationDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getRespondentC1AApplicationDocument())
                .documentFileName(doc.getRespondentC1AApplicationDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.respondentC1AApplication).build() : null
        );
        bundleMap.put(
            RESPONDENT_C1A_RESPONSE,
            Objects.nonNull(doc.getRespondentC1AResponseDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getRespondentC1AResponseDocument())
                .documentFileName(doc.getRespondentC1AResponseDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.respondentC1AResponse).build() : null
        );
        bundleMap.put(CASE_SUMMARY, Objects.nonNull(doc.getCaseSummaryDocument()) ? BundlingRequestDocument.builder()
            .documentLink(doc.getCaseSummaryDocument())
            .documentFileName(doc.getCaseSummaryDocument().getDocumentFileName())
            .documentGroup(BundlingDocGroupEnum.caseSummary).build() : null);
        bundleMap.put(
            TRANSCRIPTS_OF_JUDGEMENTS,
            Objects.nonNull(doc.getTranscriptsOfJudgementsDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getTranscriptsOfJudgementsDocument())
                .documentFileName(doc.getTranscriptsOfJudgementsDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.transcriptsOfJudgements).build() : null
        );
        bundleMap.put(
            MAGISTRATES_FACTS_AND_REASONS,
            Objects.nonNull(doc.getMagistratesFactsAndReasonsDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getMagistratesFactsAndReasonsDocument())
                .documentFileName(doc.getMagistratesFactsAndReasonsDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.magistrateFactAndReasons).build() : null
        );
        bundleMap.put(
            SAFEGUARDING_LETTER,
            Objects.nonNull(doc.getSafeguardingLetterDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getSafeguardingLetterDocument())
                .documentFileName(doc.getSafeguardingLetterDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.safeguardingLetter).build() : null
        );
        bundleMap.put(
            SECTION7_REPORT,
            Objects.nonNull(doc.getSection7ReportDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getSection7ReportDocument())
                .documentFileName(doc.getSection7ReportDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.section7Report).build() : null
        );
        bundleMap.put(
            SIXTEEN_A_RISK_ASSESSMENT,
            Objects.nonNull(doc.getSixteenARiskAssessmentDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getSixteenARiskAssessmentDocument())
                .documentFileName(doc.getSixteenARiskAssessmentDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.sixteenARiskAssessment).build() : null
        );
        bundleMap.put(
            GUARDIAN_REPORT,
            Objects.nonNull(doc.getGuardianReportDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getGuardianReportDocument())
                .documentFileName(doc.getGuardianReportDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.guardianReport).build() : null
        );
        bundleMap.put(
            SPECIAL_GUARDIANSHIP_REPORT,
            Objects.nonNull(doc.getSpecialGuardianshipReportDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getSpecialGuardianshipReportDocument())
                .documentFileName(doc.getSpecialGuardianshipReportDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.specialGuardianshipReport).build() : null
        );
        bundleMap.put(
            SECTION_37_REPORT,
            Objects.nonNull(doc.getSection37ReportDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getSection37ReportDocument())
                .documentFileName(doc.getSection37ReportDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.cafcassSection37Report).build() : null
        );
        bundleMap.put(OTHER_DOCS, Objects.nonNull(doc.getOtherDocsDocument()) ? BundlingRequestDocument.builder()
            .documentLink(doc.getOtherDocsDocument())
            .documentFileName(doc.getOtherDocsDocument().getDocumentFileName())
            .documentGroup(BundlingDocGroupEnum.cafcassOtherDocuments).build() : null);
        bundleMap.put(SEC37_REPORT, Objects.nonNull(doc.getSec37ReportDocument()) ? BundlingRequestDocument.builder()
            .documentLink(doc.getSec37ReportDocument())
            .documentFileName(doc.getSec37ReportDocument().getDocumentFileName())
            .documentGroup(BundlingDocGroupEnum.laSection37Report).build() : null);
        bundleMap.put(
            LA_OTHER_DOCS,
            Objects.nonNull(doc.getLocalAuthorityOtherDocDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getLocalAuthorityOtherDocDocument())
                .documentFileName(doc.getLocalAuthorityOtherDocDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.laOtherDocuments).build() : null
        );
        bundleMap.put(
            MIAM_CERTIFICATE,
            Objects.nonNull(doc.getMiamCertificateDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getMiamCertificateDocument())
                .documentFileName(doc.getMiamCertificateDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.applicantMiamCertificate).build() : null
        );
        bundleMap.put(
            PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION,
            Objects.nonNull(doc.getPreviousOrdersSubmittedWithApplicationDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getPreviousOrdersSubmittedWithApplicationDocument())
                .documentFileName(doc.getPreviousOrdersSubmittedWithApplicationDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.applicantPreviousOrdersSubmittedWithApplication).build() : null
        );
        bundleMap.put(
            ORDERS_FROM_OTHER_PROCEEDINGS,
            Objects.nonNull(doc.getOrdersFromOtherProceedingsDocument()) ? BundlingRequestDocument.builder()
                .documentLink(doc.getOrdersFromOtherProceedingsDocument())
                .documentFileName(doc.getOrdersFromOtherProceedingsDocument().getDocumentFileName())
                .documentGroup(BundlingDocGroupEnum.respondentPreviousOrdersSubmittedWithApplication).build() : null
        );
        bundleMap.put(ANY_OTHER_DOC, Objects.nonNull(doc.getAnyOtherDocDocument()) ? BundlingRequestDocument.builder()
            .documentLink(doc.getAnyOtherDocDocument())
            .documentFileName(doc.getAnyOtherDocDocument().getDocumentFileName())
            .documentGroup(BundlingDocGroupEnum.anyOtherDocuments).build() : null);

        return bundleMap.get(doc.getCategoryId());
    }
}
