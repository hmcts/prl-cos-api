package uk.gov.hmcts.reform.prl.services.managedocuments;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ANY_OTHER_DOC;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICATIONS_FROM_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICATIONS_WITHIN_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPROVED_ORDERS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CASE_SUMMARY;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CITIZEN_QUARANTINE;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts.restrictToGroup;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsService {

    @Autowired
    private final CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private final AuthTokenGenerator authTokenGenerator;

    private final ObjectMapper objectMapper;

    @Autowired
    private final UserService userService;

    public CaseData populateDocumentCategories(String authorization, CaseData caseData) {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentCategories(getCategoriesSubcategories(authorization, String.valueOf(caseData.getId())))
            .build();

        return caseData.toBuilder()
            .manageDocuments(Arrays.asList(element(manageDocuments)))
            .build();
    }

    private DynamicList getCategoriesSubcategories(String authorisation, String caseReference) {
        try {
            CategoriesAndDocuments categoriesAndDocuments = coreCaseDataApi.getCategoriesAndDocuments(
                authorisation,
                authTokenGenerator.generate(),
                caseReference
            );
            if (null != categoriesAndDocuments) {
                List<Category> parentCategories = nullSafeCollection(categoriesAndDocuments.getCategories())
                    .stream()
                    .sorted(Comparator.comparing(Category::getCategoryName))
                    .collect(Collectors.toList());

                List<DynamicListElement> dynamicListElementList = new ArrayList<>();
                CaseUtils.createCategorySubCategoryDynamicList(parentCategories, dynamicListElementList);

                return DynamicList.builder().value(DynamicListElement.EMPTY)
                    .listItems(dynamicListElementList).build();
            }
        } catch (Exception e) {
            log.error("Error in getCategoriesAndDocuments method", e);
        }
        return DynamicList.builder()
            .value(DynamicListElement.EMPTY).build();
    }

    public Map<String, Object> copyDocument(CallbackRequest callbackRequest, String authorization) {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        List<Element<ManageDocuments>> manageDocuments = caseData.getManageDocuments();
        String userRole = CaseUtils.getUserRole(userService.getUserDetails(authorization));

        if (manageDocuments != null && !manageDocuments.isEmpty()) {
            List<Element<QuarantineLegalDoc>> quarantineDocs = getQuarantineDocs(caseData, userRole, false);
            List<Element<QuarantineLegalDoc>> tabDocuments = getQuarantineDocs(caseData, userRole, true);

            log.info("*** manageDocuments List *** {}", manageDocuments);
            log.info("*** quarantineDocs -> before *** {}", quarantineDocs);
            log.info("*** legalProfUploadDocListDocTab -> before *** {}", tabDocuments);

            Predicate<Element<ManageDocuments>> restricted = manageDocumentsElement -> manageDocumentsElement.getValue()
                .getDocumentRestrictCheckbox().contains(restrictToGroup);

            for (Element<ManageDocuments> element : manageDocuments) {
                ManageDocuments manageDocument = element.getValue();
                // if restricted then add to quarantine docs list
                if (restricted.test(element)) {
                    QuarantineLegalDoc quarantineLegalDoc = getQuarantineLegalDocument(manageDocument, userRole);
                    quarantineDocs.add(element(quarantineLegalDoc));
                } else {
                    QuarantineLegalDoc legalProfUploadDoc = getLegalProfUploadDocument(manageDocument);
                    tabDocuments.add(element(legalProfUploadDoc));
                }
            }

            log.info("quarantineDocs List ---> after {}", quarantineDocs);
            log.info("legalProfUploadDocListDocTab List ---> after {}", tabDocuments);

            if (!quarantineDocs.isEmpty()) {
                if (CAFCASS.equals(userRole)) {
                    caseDataUpdated.put("cafcassQuarantineDocsList", quarantineDocs);
                } else {
                    caseDataUpdated.put("legalProfQuarantineDocsList", quarantineDocs);
                }
            }
            if (!tabDocuments.isEmpty()) {
                if (CAFCASS.equals(userRole)) {
                    caseDataUpdated.put("cafcassUploadDocListDocTab", tabDocuments);
                } else {
                    caseDataUpdated.put("legalProfUploadDocListDocTab", tabDocuments);
                }
            }
        }
        //remove manageDocuments from caseData
        caseDataUpdated.remove("manageDocuments");
        return caseDataUpdated;
    }

    private List<Element<QuarantineLegalDoc>> getQuarantineDocs(CaseData caseData,
                                                               String userRole,
                                                                boolean isDocumentTab) {
        if (StringUtils.isEmpty(userRole)) {
            //return new ArrayList<>();
            throw new IllegalStateException("Unexpected role : " + userRole);
        }

        switch (userRole) {
            case SOLICITOR:
                if (isDocumentTab) {
                    return !isEmpty(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab())
                        ? caseData.getReviewDocuments().getLegalProfUploadDocListDocTab() : new ArrayList<>();
                } else {
                    return !isEmpty(caseData.getLegalProfQuarantineDocsList())
                        ? caseData.getLegalProfQuarantineDocsList() : new ArrayList<>();
                }

            case CAFCASS:
                if (isDocumentTab) {
                    return !isEmpty(caseData.getReviewDocuments().getCafcassUploadDocListDocTab())
                        ? caseData.getReviewDocuments().getCafcassUploadDocListDocTab() : new ArrayList<>();
                } else {
                    return !isEmpty(caseData.getCafcassQuarantineDocsList())
                        ? caseData.getCafcassQuarantineDocsList() : new ArrayList<>();
                }

            default:
                //return new ArrayList<>();
                throw new IllegalStateException("Unexpected role : " + userRole);
        }
    }

    private QuarantineLegalDoc getQuarantineLegalDocument(ManageDocuments manageDocument, String userRole) {
        return QuarantineLegalDoc.builder()
            .document(SOLICITOR.equals(userRole) ? manageDocument.getDocument() : null)
            .cafcassQuarantineDocument(CAFCASS.equals(userRole) ? manageDocument.getDocument() : null)
            .documentParty(manageDocument.getDocumentParty().getDisplayedValue())
            .restrictCheckboxCorrespondence(manageDocument.getDocumentRestrictCheckbox())
            .notes(manageDocument.getDocumentDetails())
            .category(manageDocument.getDocumentCategories().getValueCode())
            .build();
    }

    private QuarantineLegalDoc getLegalProfUploadDocument(ManageDocuments manageDocument) {
        final String categoryId = manageDocument.getDocumentCategories().getValueCode();

        return QuarantineLegalDoc.builder()
            .applicantApplicationDocument(getDocumentByCategoryId(APPLICANT_APPLICATION, categoryId, manageDocument))
            .applicantC1AApplicationDocument(getDocumentByCategoryId(APPLICANT_C1A_APPLICATION, categoryId, manageDocument))
            .applicantC1AResponseDocument(getDocumentByCategoryId(APPLICANT_C1A_RESPONSE, categoryId, manageDocument))
            .applicationsWithinProceedingsDocument(getDocumentByCategoryId(APPLICATIONS_WITHIN_PROCEEDINGS, categoryId, manageDocument))
            .miamCertificateDocument(getDocumentByCategoryId(MIAM_CERTIFICATE, categoryId, manageDocument))
            .previousOrdersSubmittedWithApplicationDocument(
                getDocumentByCategoryId(PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION, categoryId, manageDocument))
            .respondentApplicationDocument(getDocumentByCategoryId(RESPONDENT_APPLICATION, categoryId, manageDocument))
            .respondentC1AApplicationDocument(getDocumentByCategoryId(RESPONDENT_C1A_APPLICATION, categoryId, manageDocument))
            .respondentC1AResponseDocument(getDocumentByCategoryId(RESPONDENT_C1A_RESPONSE, categoryId, manageDocument))
            .applicationsFromOtherProceedingsDocument(getDocumentByCategoryId(APPLICATIONS_FROM_OTHER_PROCEEDINGS, categoryId, manageDocument))
            .noticeOfHearingDocument(getDocumentByCategoryId(NOTICE_OF_HEARING, categoryId, manageDocument))
            .courtBundleDocument(getDocumentByCategoryId(COURT_BUNDLE, categoryId, manageDocument))
            .safeguardingLetterDocument(getDocumentByCategoryId(SAFEGUARDING_LETTER, categoryId, manageDocument))
            .section7ReportDocument(getDocumentByCategoryId(SECTION7_REPORT, categoryId, manageDocument))
            .section37ReportDocument(getDocumentByCategoryId(SECTION_37_REPORT, categoryId, manageDocument))
            .sixteenARiskAssessmentDocument(getDocumentByCategoryId(SIXTEEN_A_RISK_ASSESSMENT, categoryId, manageDocument))
            .guardianReportDocument(getDocumentByCategoryId(GUARDIAN_REPORT, categoryId, manageDocument))
            .specialGuardianshipReportDocument(getDocumentByCategoryId(SPECIAL_GUARDIANSHIP_REPORT, categoryId, manageDocument))
            .otherDocsDocument(getDocumentByCategoryId(OTHER_DOCS, categoryId, manageDocument))
            .confidentialDocument(getDocumentByCategoryId(CONFIDENTIAL, categoryId, manageDocument))
            .emailsToCourtToRequestHearingsAdjournedDocument(
                getDocumentByCategoryId(EMAILS_TO_COURT_TO_REQUEST_HEARINGS_ADJOURNED, categoryId, manageDocument))
            .publicFundingCertificatesDocument(getDocumentByCategoryId(PUBLIC_FUNDING_CERTIFICATES, categoryId, manageDocument))
            .noticesOfActingDischargeDocument(getDocumentByCategoryId(NOTICES_OF_ACTING_DISCHARGE, categoryId, manageDocument))
            .requestForFasFormsToBeChangedDocument(getDocumentByCategoryId(REQUEST_FOR_FAS_FORMS_TO_BE_CHANGED, categoryId, manageDocument))
            .witnessAvailabilityDocument(getDocumentByCategoryId(WITNESS_AVAILABILITY, categoryId, manageDocument))
            .lettersOfComplaintDocument(getDocumentByCategoryId(LETTERS_OF_COMPLAINTS, categoryId, manageDocument))
            .spipReferralRequestsDocument(getDocumentByCategoryId(SPIP_REFERRAL_REQUESTS, categoryId, manageDocument))
            .homeOfficeDwpResponsesDocument(getDocumentByCategoryId(HOME_OFFICE_DWP_RESPONSES, categoryId, manageDocument))
            .medicalReportsDocument(getDocumentByCategoryId(MEDICAL_REPORTS, categoryId, manageDocument))
            .dnaReportsExpertReportDocument(getDocumentByCategoryId(DNA_REPORTS_EXPERT_REPORT, categoryId, manageDocument))
            .resultsOfHairStrandBloodTestsDocument(getDocumentByCategoryId(RESULTS_OF_HAIR_STRAND_BLOOD_TESTS, categoryId, manageDocument))
            .policeDisclosuresDocument(getDocumentByCategoryId(POLICE_DISCLOSURES, categoryId, manageDocument))
            .medicalRecordsDocument(getDocumentByCategoryId(MEDICAL_RECORDS, categoryId, manageDocument))
            .drugAndAlcoholTestDocument(getDocumentByCategoryId(DRUG_AND_ALCOHOL_TEST, categoryId, manageDocument))
            .policeReportDocument(getDocumentByCategoryId(POLICE_REPORT, categoryId, manageDocument))
            .sec37ReportDocument(getDocumentByCategoryId(SEC37_REPORT, categoryId, manageDocument))
            .ordersSubmittedWithApplicationDocument(getDocumentByCategoryId(ORDERS_SUBMITTED_WITH_APPLICATION, categoryId, manageDocument))
            .approvedOrdersDocument(getDocumentByCategoryId(APPROVED_ORDERS, categoryId, manageDocument))
            .standardDirectionsOrderDocument(getDocumentByCategoryId(STANDARD_DIRECTIONS_ORDER, categoryId, manageDocument))
            .transcriptsOfJudgementsDocument(getDocumentByCategoryId(TRANSCRIPTS_OF_JUDGEMENTS, categoryId, manageDocument))
            .magistratesFactsAndReasonsDocument(
                getDocumentByCategoryId(MAGISTRATES_FACTS_AND_REASONS, categoryId, manageDocument))
            .judgeNotesFromHearingDocument(getDocumentByCategoryId(JUDGE_NOTES_FROM_HEARING, categoryId, manageDocument))
            .importantInfoAboutAddressAndContactDocument(
                getDocumentByCategoryId(IMPORTANT_INFO_ABOUT_ADDRESS_AND_CONTACT, categoryId, manageDocument))
            .dnaReportsOtherDocsDocument(getDocumentByCategoryId(DNA_REPORTS_OTHER_DOCS, categoryId, manageDocument))
            .privacyNoticeDocument(getDocumentByCategoryId(PRIVACY_NOTICE, categoryId, manageDocument))
            .specialMeasuresDocument(getDocumentByCategoryId(SPECIAL_MEASURES, categoryId, manageDocument))
            .anyOtherDocDocument(getDocumentByCategoryId(ANY_OTHER_DOC, categoryId, manageDocument))
            .positionStatementsDocument(getDocumentByCategoryId(POSITION_STATEMENTS, categoryId, manageDocument))
            .citizenQuarantineDocument(getDocumentByCategoryId(CITIZEN_QUARANTINE, categoryId, manageDocument))
            .applicantStatementsDocument(getDocumentByCategoryId(APPLICANT_STATEMENTS, categoryId, manageDocument))
            .respondentStatementsDocument(getDocumentByCategoryId(RESPONDENT_STATEMENTS, categoryId, manageDocument))
            .otherWitnessStatementsDocument(getDocumentByCategoryId(OTHER_WITNESS_STATEMENTS, categoryId, manageDocument))
            .caseSummaryDocument(getDocumentByCategoryId(CASE_SUMMARY, categoryId, manageDocument))
            .documentParty(manageDocument.getDocumentParty().getDisplayedValue())
            .notes(manageDocument.getDocumentDetails())
            .build();
    }

    private Document getDocumentByCategoryId(String categoryConstant,
                                             String categoryId,
                                             ManageDocuments manageDocuments) {
        return categoryConstant.equalsIgnoreCase(categoryId) ? manageDocuments.getDocument() : null;
    }
}
