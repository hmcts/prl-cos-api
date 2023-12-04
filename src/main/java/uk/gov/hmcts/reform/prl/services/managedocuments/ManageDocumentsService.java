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
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ANY_OTHER_DOC;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ANY_OTHER_DOC_NAME;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MIAM_CERTIFICATE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MIAM_CERTIFICATE_NAME;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ORDERS_SUBMITTED_WITH_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ORDERS_SUBMITTED_WITH_APPLICATION_NAME;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.OTHER_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.OTHER_WITNESS_STATEMENTS_NAME;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts.restrictToGroup;
import static uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc.quarantineCategoriesToRemove;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsService {

    public static final String UNEXPECTED_USER_ROLE = "Unexpected user role : ";
    public static final String MANAGE_DOCUMENTS_RESTRICTED_FLAG = "manageDocumentsRestrictedFlag";
    @Autowired
    private final CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private final AuthTokenGenerator authTokenGenerator;

    private final ObjectMapper objectMapper;

    @Autowired
    private final UserService userService;

    private final Time dateTime;

    public static final String MANAGE_DOCUMENTS_TRIGGERED_BY = "manageDocumentsTriggeredBy";
    private final Date localZoneDate = Date.from(ZonedDateTime.now(ZoneId.of(LONDON_TIME_ZONE)).toInstant());

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
                CaseUtils.createCategorySubCategoryDynamicList(
                    parentCategories,
                    dynamicListElementList,
                    Arrays.asList(quarantineCategoriesToRemove())
                );

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

            if (quarantineDocs.isEmpty()) {
                updateCaseDataUpdatedByRole(caseDataUpdated, userRole);
            } else {
                caseDataUpdated.put(MANAGE_DOCUMENTS_TRIGGERED_BY, "NOTREQUIRED");
            }
            List<Element<QuarantineLegalDoc>> tabDocuments = getQuarantineDocs(caseData, userRole, true);
            log.info("*** manageDocuments List *** {}", manageDocuments);
            log.info("*** quarantineDocs -> before *** {}", quarantineDocs);
            log.info("*** legalProfUploadDocListDocTab -> before *** {}", tabDocuments);

            Predicate<Element<ManageDocuments>> restricted = manageDocumentsElement -> manageDocumentsElement.getValue()
                .getDocumentRestrictCheckbox().contains(restrictToGroup);

            boolean isRestrictedFlag = false;
            for (Element<ManageDocuments> element : manageDocuments) {
                if (addToQuarantineDocsOrTabDocumentsAndReturnConfidFlag(
                    element,
                    restricted,
                    userRole,
                    quarantineDocs,
                    tabDocuments
                )) {
                    isRestrictedFlag = true;
                }
            }
            //if any restricted docs
            if (isRestrictedFlag) {
                caseDataUpdated.put(MANAGE_DOCUMENTS_RESTRICTED_FLAG, "True");
            } else {
                caseDataUpdated.remove(MANAGE_DOCUMENTS_RESTRICTED_FLAG);
            }

            log.info("quarantineDocs List ---> after {}", quarantineDocs);
            log.info("legalProfUploadDocListDocTab List ---> after {}", tabDocuments);

            if (!quarantineDocs.isEmpty()) {
                updateQuarantineDocs(caseDataUpdated, quarantineDocs, userRole, false);
            }
            if (!tabDocuments.isEmpty()) {
                updateQuarantineDocs(caseDataUpdated, tabDocuments, userRole, true);
            }
        }
        //remove manageDocuments from caseData
        caseDataUpdated.remove("manageDocuments");

        return caseDataUpdated;
    }

    private void updateCaseDataUpdatedByRole(Map<String,Object> caseDataUpdated,String userRole) {

        if (SOLICITOR.equals(userRole)) {
            caseDataUpdated.put(MANAGE_DOCUMENTS_TRIGGERED_BY, "SOLICITOR");
        } else if (CAFCASS.equals(userRole)) {
            caseDataUpdated.put(MANAGE_DOCUMENTS_TRIGGERED_BY, "CAFCASS");
        } else if (COURT_STAFF.equals(userRole)) {
            caseDataUpdated.put(MANAGE_DOCUMENTS_TRIGGERED_BY, "STAFF");
        }
    }

    private boolean addToQuarantineDocsOrTabDocumentsAndReturnConfidFlag(Element<ManageDocuments> element,
                                                                         Predicate<Element<ManageDocuments>> restricted,
                                                                         String userRole,
                                                                         List<Element<QuarantineLegalDoc>> quarantineDocs,
                                                                         List<Element<QuarantineLegalDoc>> tabDocuments) {

        ManageDocuments manageDocument = element.getValue();
        boolean confidentialityFlag = false;
        // if restricted then add to quarantine docs list
        if (restricted.test(element)) {
            QuarantineLegalDoc quarantineLegalDoc = getQuarantineDocument(manageDocument, userRole);
            quarantineLegalDoc = DocumentUtils.addQuarantineFields(quarantineLegalDoc, manageDocument);
            confidentialityFlag = true;
            quarantineDocs.add(element(quarantineLegalDoc));
        } else {
            final String categoryId = manageDocument.getDocumentCategories().getValueCode();
            QuarantineLegalDoc quarantineUploadDoc = DocumentUtils
                .getQuarantineUploadDocument(
                    categoryId,
                    manageDocument.getDocument().toBuilder()
                        .documentCreatedOn(localZoneDate).build()
                );
            quarantineUploadDoc = DocumentUtils.addQuarantineFields(quarantineUploadDoc, manageDocument);

            tabDocuments.add(element(quarantineUploadDoc));
        }
        return confidentialityFlag;
    }


    private void updateQuarantineDocs(Map<String, Object> caseDataUpdated,
                                      List<Element<QuarantineLegalDoc>> quarantineDocs,
                                      String userRole,
                                      boolean isDocumentTab) {
        if (StringUtils.isEmpty(userRole)) {
            throw new IllegalStateException(UNEXPECTED_USER_ROLE + userRole);
        }

        switch (userRole) {
            case SOLICITOR:
                if (isDocumentTab) {
                    caseDataUpdated.put("legalProfUploadDocListDocTab", quarantineDocs);
                } else {
                    caseDataUpdated.put("legalProfQuarantineDocsList", quarantineDocs);
                }
                break;

            case CAFCASS:
                if (isDocumentTab) {
                    caseDataUpdated.put("cafcassUploadDocListDocTab", quarantineDocs);
                } else {
                    caseDataUpdated.put("cafcassQuarantineDocsList", quarantineDocs);
                }
                break;

            case COURT_STAFF:
                if (isDocumentTab) {
                    caseDataUpdated.put("courtStaffUploadDocListDocTab", quarantineDocs);
                } else {
                    caseDataUpdated.put("courtStaffQuarantineDocsList", quarantineDocs);
                }
                break;

            default:
                throw new IllegalStateException(UNEXPECTED_USER_ROLE + userRole);

        }
    }

    private List<Element<QuarantineLegalDoc>> getQuarantineDocs(CaseData caseData,
                                                                String userRole,
                                                                boolean isDocumentTab) {
        if (StringUtils.isEmpty(userRole)) {
            throw new IllegalStateException(UNEXPECTED_USER_ROLE + userRole);
        }

        switch (userRole) {
            case SOLICITOR:
                return getQuarantineOrUploadDocsBasedOnDocumentTab(
                    isDocumentTab,
                    caseData.getReviewDocuments().getLegalProfUploadDocListDocTab(),
                    caseData.getLegalProfQuarantineDocsList()
                );
            case CAFCASS:

                return getQuarantineOrUploadDocsBasedOnDocumentTab(
                    isDocumentTab,
                    caseData.getReviewDocuments().getCafcassUploadDocListDocTab(),
                    caseData.getCafcassQuarantineDocsList()
                );
            case COURT_STAFF:

                return getQuarantineOrUploadDocsBasedOnDocumentTab(
                    isDocumentTab,
                    caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab(),
                    caseData.getCourtStaffQuarantineDocsList()
                );
            default:
                throw new IllegalStateException(UNEXPECTED_USER_ROLE + userRole);
        }
    }

    private List<Element<QuarantineLegalDoc>> getQuarantineOrUploadDocsBasedOnDocumentTab(boolean isDocumentTab,
                                                                                          List<Element<QuarantineLegalDoc>> uploadDocListDocTab,
                                                                                          List<Element<QuarantineLegalDoc>> quarantineDocsList) {
        if (isDocumentTab) {
            return !isEmpty(uploadDocListDocTab) ? uploadDocListDocTab : new ArrayList<>();
        } else {
            return !isEmpty(quarantineDocsList) ? quarantineDocsList : new ArrayList<>();
        }
    }

    private QuarantineLegalDoc getQuarantineDocument(ManageDocuments manageDocument, String userRole) {
        return QuarantineLegalDoc.builder()
            .document(SOLICITOR.equals(userRole) ? manageDocument.getDocument().toBuilder()
                .documentCreatedOn(localZoneDate).build() : null)
            .cafcassQuarantineDocument(CAFCASS.equals(userRole) ? manageDocument.getDocument().toBuilder()
                .documentCreatedOn(localZoneDate).build() : null)
            .courtStaffQuarantineDocument(COURT_STAFF.equals(userRole) ? manageDocument.getDocument().toBuilder()
                .documentCreatedOn(localZoneDate).build() : null)
            .build();
    }

    public void createC100QuarantineDocuments(Map<String, Object> caseDataUpdated,
                                              CaseData caseData) {
        log.info("##################### C100 Quarantine documents #####################");
        List<Element<QuarantineLegalDoc>> quarantineDocs = new ArrayList<>();

        //MIAM certificate
        if (null != caseData.getMiamDetails()) {
            log.info("MiamCertDocUploadddddd()----> {}",caseData.getMiamDetails().getMiamCertificationDocumentUpload());
            if (null != caseData.getMiamDetails().getMiamCertificationDocumentUpload()) {
                QuarantineLegalDoc miamQuarantineDoc = QuarantineLegalDoc.builder()
                    .document(caseData.getMiamDetails().getMiamCertificationDocumentUpload().toBuilder()
                                  .documentCreatedOn(localZoneDate).build())
                    .index(1) //Added to filter docs for return case
                    .build();
                miamQuarantineDoc = DocumentUtils.addQuarantineFields(miamQuarantineDoc, MIAM_CERTIFICATE, MIAM_CERTIFICATE_NAME);
                quarantineDocs.add(element(miamQuarantineDoc));
                caseDataUpdated.remove("miamCertificationDocumentUpload");
            }
        }

        //Draft consent order
        if (null != caseData.getDraftConsentOrderFile()) {
            log.info("ConsentOrderUpload()----> {}",caseData.getDraftConsentOrderFile());
            if (null != caseData.getDraftConsentOrderFile()) {
                QuarantineLegalDoc consentOrderQuarantineDoc = QuarantineLegalDoc.builder()
                    .document(caseData.getDraftConsentOrderFile().toBuilder()
                                  .documentCreatedOn(localZoneDate).build())
                    .index(1) //Added to filter docs for return case
                    .build();
                consentOrderQuarantineDoc = DocumentUtils.addQuarantineFields(consentOrderQuarantineDoc,
                                                                              ORDERS_SUBMITTED_WITH_APPLICATION,
                                                                              ORDERS_SUBMITTED_WITH_APPLICATION_NAME);
                quarantineDocs.add(element(consentOrderQuarantineDoc));
                caseDataUpdated.remove("draftConsentOrderFile");
            }
        }

        //Other proceedings
        if (!isEmpty(caseData.getExistingProceedings())) {
            log.info("ExistingProceedings()----> {}", caseData.getExistingProceedings());
            for (int index = 0; index < caseData.getExistingProceedings().size(); index++) {
                ProceedingDetails otherProceeding = caseData.getExistingProceedings().get(index).getValue();
                if (null != otherProceeding
                    && null != otherProceeding.getUploadRelevantOrder()) {
                    QuarantineLegalDoc otherProceedingQuarantineDoc = QuarantineLegalDoc.builder()
                        .document(otherProceeding.getUploadRelevantOrder().toBuilder()
                                      .documentCreatedOn(localZoneDate).build())
                        .index(index + 1) //Index starts from 1 to filter out default 0
                        .build();
                    otherProceedingQuarantineDoc = DocumentUtils.addQuarantineFields(otherProceedingQuarantineDoc,
                                                                                     PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION,
                                                                                     PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION_NAME);
                    quarantineDocs.add(element(otherProceedingQuarantineDoc));
                    //remove original doc
                    otherProceeding.setUploadRelevantOrder(null);
                }
            }
            /*caseData.getExistingProceedings().stream()
                .map(Element::getValue)
                .forEach(otherProceeding -> {
                    if (null != otherProceeding.getUploadRelevantOrder()) {
                        QuarantineLegalDoc otherProceedingQuarantineDoc = QuarantineLegalDoc.builder()
                            .document(otherProceeding.getUploadRelevantOrder().toBuilder()
                                          .documentCreatedOn(localZoneDate).build())
                            .build();
                        otherProceedingQuarantineDoc = DocumentUtils.addQuarantineFields(otherProceedingQuarantineDoc,
                                                                                         PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION,
                                                                                         PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION_NAME);
                        quarantineDocs.add(element(otherProceedingQuarantineDoc));
                        otherProceeding.setUploadRelevantOrder(null);
                        otherProceeding.setIndex();
                    }
                });*/
            caseDataUpdated.put("existingProceedings", caseData.getExistingProceedings());
        }

        log.info("quarantineDocs()----> {}", quarantineDocs);
        if (!quarantineDocs.isEmpty()) {
            caseDataUpdated.put("legalProfQuarantineDocsList", quarantineDocs);
            caseDataUpdated.put(MANAGE_DOCUMENTS_RESTRICTED_FLAG, "True");
        }
        log.info("##################### C100 Quarantine documents #####################");
    }

    public void createFL401QuarantineDocuments(Map<String, Object> caseDataUpdated,
                                               CaseData caseData) {
        log.info("******************* FL401 Quarantine documents *******************");
        List<Element<QuarantineLegalDoc>> quarantineDocs = new ArrayList<>();

        //Other proceedings
        if (null != caseData.getFl401OtherProceedingDetails()
            && !isEmpty(caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings())) {
            log.info("ExistingProceedings()----> {}", caseData.getFl401OtherProceedingDetails());
            for (int index = 0; index < caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings().size(); index++) {
                FL401Proceedings otherProceeding = caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings().get(index).getValue();
                if (null != otherProceeding
                    && null != otherProceeding.getUploadRelevantOrder()) {
                    QuarantineLegalDoc otherProceedingQuarantineDoc = QuarantineLegalDoc.builder()
                        .document(otherProceeding.getUploadRelevantOrder().toBuilder()
                                      .documentCreatedOn(localZoneDate).build())
                        .index(index + 1)
                        .build();
                    otherProceedingQuarantineDoc = DocumentUtils.addQuarantineFields(otherProceedingQuarantineDoc,
                                                                                     PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION,
                                                                                     PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION_NAME);
                    quarantineDocs.add(element(otherProceedingQuarantineDoc));
                    //remove original doc
                    otherProceeding.setUploadRelevantOrder(null);
                }
            }
            /*caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings().stream()
                .map(Element::getValue)
                .forEach(otherProceeding -> {
                    if (null != otherProceeding.getUploadRelevantOrder()) {
                        QuarantineLegalDoc otherProceedingQuarantineDoc = QuarantineLegalDoc.builder()
                            .document(otherProceeding.getUploadRelevantOrder().toBuilder()
                                          .documentCreatedOn(localZoneDate).build())
                            .build();
                        otherProceedingQuarantineDoc = DocumentUtils.addQuarantineFields(otherProceedingQuarantineDoc,
                                                                                         PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION,
                                                                                         PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION_NAME);
                        quarantineDocs.add(element(otherProceedingQuarantineDoc));
                        otherProceeding.setUploadRelevantOrder(null);
                    }
                });*/
            caseDataUpdated.put("fl401OtherProceedingDetails", caseData.getFl401OtherProceedingDetails());
        }

        //Witness statements
        if (!isEmpty(caseData.getFl401UploadWitnessDocuments())) {
            log.info("Witness statements()----> {}",caseData.getFl401UploadWitnessDocuments());
            caseData.getFl401UploadWitnessDocuments().stream()
                .map(Element::getValue)
                .forEach(document -> {
                    if (null != document) {
                        QuarantineLegalDoc witnessQuarantineDoc = QuarantineLegalDoc.builder()
                            .document(document.toBuilder()
                                          .documentCreatedOn(localZoneDate).build())
                            .index(1) //Added to filter docs for return case
                            .build();
                        witnessQuarantineDoc = DocumentUtils.addQuarantineFields(witnessQuarantineDoc,
                                                                                 OTHER_WITNESS_STATEMENTS,
                                                                                 OTHER_WITNESS_STATEMENTS_NAME);
                        quarantineDocs.add(element(witnessQuarantineDoc));
                    }
                });
            caseDataUpdated.remove("fl401UploadWitnessDocuments");
        }

        //Supporting documents
        if (!isEmpty(caseData.getFl401UploadSupportDocuments())) {
            log.info("Supporting documents()----> {}",caseData.getFl401UploadSupportDocuments());
            caseData.getFl401UploadSupportDocuments().stream()
                .map(Element::getValue)
                .forEach(document -> {
                    if (null != document) {
                        QuarantineLegalDoc supportingQuarantineDoc = QuarantineLegalDoc.builder()
                            .document(document.toBuilder()
                                          .documentCreatedOn(localZoneDate).build())
                            .index(1) //Added to filter docs for return case
                            .build();
                        supportingQuarantineDoc = DocumentUtils.addQuarantineFields(supportingQuarantineDoc,
                                                                                    ANY_OTHER_DOC,
                                                                                    ANY_OTHER_DOC_NAME);
                        quarantineDocs.add(element(supportingQuarantineDoc));
                    }
                });
            caseDataUpdated.remove("fl401UploadSupportDocuments");
        }

        log.info("quarantineDocs()----> {}", quarantineDocs);
        if (!quarantineDocs.isEmpty()) {
            caseDataUpdated.put("legalProfQuarantineDocsList", quarantineDocs);
            caseDataUpdated.put(MANAGE_DOCUMENTS_RESTRICTED_FLAG, "True");
        }
        log.info("******************* FL401 Quarantine documents *******************");
    }

    public void removeQuarantineDocsAndMoveToOriginal(Map<String, Object> caseDataUpdated,
                                                      CaseData caseData) {
        //Return case - move quarantine docs back to original
        log.info("Remove quarantine docs for {}", caseData.getCaseTypeOfApplication());
        switch (caseData.getCaseTypeOfApplication()) {
            case C100_CASE_TYPE -> removeC100QuarantineDocs(caseDataUpdated, caseData);
            case FL401_CASE_TYPE -> removeFL401QuarantineDocs(caseDataUpdated, caseData);

            default -> log.error("Invalid case type {}", caseData.getCaseTypeOfApplication());
        }
    }

    private void removeC100QuarantineDocs(Map<String, Object> caseDataUpdated,
                                          CaseData caseData) {
        if (!isEmpty(caseData.getLegalProfQuarantineDocsList())) {
            //MIAM
            Element<QuarantineLegalDoc> miamQuarantineDoc = getQuarantineDocByCategory(caseData, MIAM_CERTIFICATE);
            if (null != miamQuarantineDoc) {
                log.info("Miam quarantine doc {}", miamQuarantineDoc);
                caseDataUpdated.put("miamCertificationDocumentUpload", miamQuarantineDoc.getValue().getDocument());
                caseData.getLegalProfQuarantineDocsList().remove(miamQuarantineDoc);
            }

            //Consent order
            Element<QuarantineLegalDoc> consentQuarantineDoc = getQuarantineDocByCategory(caseData, ORDERS_SUBMITTED_WITH_APPLICATION);
            if (null != consentQuarantineDoc) {
                log.info("Consent order quarantine doc {}", consentQuarantineDoc);
                caseDataUpdated.put("draftConsentOrderFile", consentQuarantineDoc.getValue().getDocument());
                caseData.getLegalProfQuarantineDocsList().remove(consentQuarantineDoc);
            }

            //Other proceedings
            if (!isEmpty(caseData.getExistingProceedings())) {
                List<Element<QuarantineLegalDoc>> otherProceedingQuarantineDocs =
                    getQuarantineDocsByCategory(caseData,
                                                PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION);
                log.info("otherProceedingQuarantineDocs {}", otherProceedingQuarantineDocs);
                if (!isEmpty(otherProceedingQuarantineDocs)) {
                    otherProceedingQuarantineDocs
                        .forEach(quarantineLegalDocElement -> {
                            QuarantineLegalDoc quarantineDoc = quarantineLegalDocElement.getValue();
                            //Set documents
                            caseData.getExistingProceedings()
                                .get(quarantineDoc.getIndex() - 1)
                                .getValue()
                                .setUploadRelevantOrder(quarantineDoc.getDocument());

                            caseData.getLegalProfQuarantineDocsList().remove(quarantineLegalDocElement);
                        });
                    caseDataUpdated.put("existingProceedings", caseData.getExistingProceedings());
                }
            }

            caseDataUpdated.put("legalProfQuarantineDocsList", caseData.getLegalProfQuarantineDocsList());
        }
    }

    private void removeFL401QuarantineDocs(Map<String, Object> caseDataUpdated,
                                          CaseData caseData) {
        if (!isEmpty(caseData.getLegalProfQuarantineDocsList())) {
            //Other proceedings
            if (null != caseData.getFl401OtherProceedingDetails()
                && !isEmpty(caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings())) {
                List<Element<QuarantineLegalDoc>> otherProceedingQuarantineDocs =
                    getQuarantineDocsByCategory(caseData,
                                                PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION);
                if (!isEmpty(otherProceedingQuarantineDocs)) {
                    otherProceedingQuarantineDocs
                        .forEach(quarantineLegalDocElement -> {
                            QuarantineLegalDoc quarantineDoc = quarantineLegalDocElement.getValue();
                            //Set documents
                            caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings()
                                .get(quarantineDoc.getIndex() - 1)
                                .getValue()
                                .setUploadRelevantOrder(quarantineDoc.getDocument());

                            caseData.getLegalProfQuarantineDocsList().remove(quarantineLegalDocElement);
                        });
                    caseDataUpdated.put("fl401OtherProceedingDetails", caseData.getFl401OtherProceedingDetails());
                }
            }

            //Witness statements
            List<Element<QuarantineLegalDoc>> witnessQuarantineDocs = getQuarantineDocsByCategory(caseData,
                                                                                                  OTHER_WITNESS_STATEMENTS);
            if (!isEmpty(witnessQuarantineDocs)) {
                List<Element<Document>> fl401UploadWitnessDocuments = new ArrayList<>();
                witnessQuarantineDocs
                    .forEach(quarantineLegalDocElement -> {
                        QuarantineLegalDoc quarantineDoc = quarantineLegalDocElement.getValue();
                        fl401UploadWitnessDocuments.add(element(quarantineDoc.getDocument()));
                        caseData.getLegalProfQuarantineDocsList().remove(quarantineLegalDocElement);
                    });
                caseDataUpdated.put("fl401UploadWitnessDocuments", fl401UploadWitnessDocuments);
            }

            //Supporting docs
            List<Element<QuarantineLegalDoc>> supportingQuarantineDocs = getQuarantineDocsByCategory(caseData,
                                                                                                     ANY_OTHER_DOC);
            if (!isEmpty(supportingQuarantineDocs)) {
                List<Element<Document>> fl401UploadSupportDocuments = new ArrayList<>();
                supportingQuarantineDocs
                    .forEach(quarantineLegalDocElement -> {
                        QuarantineLegalDoc quarantineDoc = quarantineLegalDocElement.getValue();
                        fl401UploadSupportDocuments.add(element(quarantineDoc.getDocument()));
                        caseData.getLegalProfQuarantineDocsList().remove(quarantineLegalDocElement);
                    });
                caseDataUpdated.put("fl401UploadSupportDocuments", fl401UploadSupportDocuments);
            }

            caseDataUpdated.put("legalProfQuarantineDocsList", caseData.getLegalProfQuarantineDocsList());
        }
    }

    private Element<QuarantineLegalDoc> getQuarantineDocByCategory(CaseData caseData,
                                                         String categoryId) {
        if (!isEmpty(caseData.getLegalProfQuarantineDocsList())) {
            return caseData.getLegalProfQuarantineDocsList().stream()
                .filter(quarantineDoc -> quarantineDoc.getValue().getIndex() > 0
                    && categoryId.equals(quarantineDoc.getValue().getCategoryId()))
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    private List<Element<QuarantineLegalDoc>> getQuarantineDocsByCategory(CaseData caseData,
                                                                String categoryId) {
        if (!isEmpty(caseData.getLegalProfQuarantineDocsList())) {
            return caseData.getLegalProfQuarantineDocsList().stream()
                .filter(quarantineDoc -> quarantineDoc.getValue().getIndex() > 0
                    && categoryId.equals(quarantineDoc.getValue().getCategoryId()))
                .toList();
        }
        return Collections.emptyList();
    }
}
