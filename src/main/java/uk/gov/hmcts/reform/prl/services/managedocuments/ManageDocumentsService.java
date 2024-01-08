package uk.gov.hmcts.reform.prl.services.managedocuments;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
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
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BULK_SCAN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONFIDENTIAL_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INTERNAL_CORRESPONDENCE_LABEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESTRICTED_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MULTIPART_FILE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc.quarantineCategoriesToRemove;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsService {
    public static final String UNEXPECTED_USER_ROLE = "Unexpected user role : ";
    public static final String MANAGE_DOCUMENTS_RESTRICTED_FLAG = "manageDocumentsRestrictedFlag";
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final CaseDocumentClient caseDocumentClient;
    private final SystemUserService systemUserService;

    public static final String CONFIDENTIAL = "Confidential_";

    public static final String MANAGE_DOCUMENTS_TRIGGERED_BY = "manageDocumentsTriggeredBy";
    public static final String DETAILS_ERROR_MESSAGE
        = "You must give a reason why the document should be restricted";
    private final Date localZoneDate = Date.from(ZonedDateTime.now(ZoneId.of(LONDON_TIME_ZONE)).toInstant());

    public CaseData populateDocumentCategories(String authorization, CaseData caseData) {
        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentCategories(getCategoriesSubcategories(authorization, String.valueOf(caseData.getId())))
            .build();

        return caseData.toBuilder()
            .isC8DocumentPresent(CaseUtils.isC8Present(caseData) ? "Yes" : "No")
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
                    .toList();

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

    public List<String> validateRestrictedReason(CallbackRequest callbackRequest,
                                                 UserDetails userDetails) {
        List<String> errorList = new ArrayList<>();
        String userRole = CaseUtils.getUserRole(userDetails);
        if (SOLICITOR.equals(userRole)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

            List<Element<ManageDocuments>> manageDocuments = caseData.getManageDocuments();
            for (Element<ManageDocuments> element : manageDocuments) {
                boolean restricted = element.getValue().getIsRestricted().equals(YesOrNo.Yes);
                boolean restrictedReasonEmpty = element.getValue().getRestrictedDetails() == null
                    || element.getValue().getRestrictedDetails().isEmpty();
                if (restricted && restrictedReasonEmpty) {
                    errorList.add(DETAILS_ERROR_MESSAGE);
                }
            }
        }
        return errorList;
    }

    public Map<String, Object> copyDocumentNew(CallbackRequest callbackRequest, String authorization) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        UserDetails userDetails = userService.getUserDetails(authorization);
        transformManageDocumentsToQuarantineList(
            authorization,
            caseData,
            caseDataUpdated,
            userDetails
        );
        caseDataUpdated.remove("manageDocuments");
        return caseDataUpdated;
    }

    private void transformManageDocumentsToQuarantineList(String authorization, CaseData caseData, Map<String, Object> caseDataUpdated,
                                                          UserDetails userDetails) {

        String userRole = CaseUtils.getUserRole(userDetails);
        List<Element<ManageDocuments>> manageDocuments = caseData.getManageDocuments();
        for (Element<ManageDocuments> element : manageDocuments) {
            ManageDocuments manageDocument = element.getValue();
            String categoryId = formulateCategoryId(manageDocument);
            log.info("Get Category ID - {}", categoryId);

            boolean confidentialOrRestrictedFlag = YesOrNo.Yes.equals(manageDocument.getIsConfidential())
                || YesOrNo.Yes.equals(manageDocument.getIsRestricted());
            QuarantineLegalDoc quarantineLegalDoc = DocumentUtils.addQuarantineFieldsWithConfidentialFlag(
                categoryId,
                manageDocument.getDocument().toBuilder()
                    .documentCreatedOn(localZoneDate).build(),
                objectMapper,
                manageDocument,
                userDetails,
                confidentialOrRestrictedFlag
            );
            log.info(
                "QuarantineLegalDoc quarantineLegalDoc = DocumentUtils.addQuarantineFieldsWithConfidentialFlag -->{}",
                quarantineLegalDoc
            );
            if (userRole.equals(COURT_ADMIN) || !confidentialOrRestrictedFlag) {
                moveDocumentsToRespectiveCategories(
                    authorization,
                    quarantineLegalDoc,
                    caseData,
                    caseDataUpdated,
                    userRole
                );
            } else {
                moveDocumentsToQuarantineTab(quarantineLegalDoc, caseData, caseDataUpdated, userRole);
                setFlagsForWaTask(caseData, caseDataUpdated, userRole, quarantineLegalDoc);

            }
        }
    }

    private void setFlagsForWaTask(CaseData caseData, Map<String, Object> caseDataUpdated, String userRole, QuarantineLegalDoc quarantineLegalDoc) {
        //Setting this flag for WA task
        if (quarantineLegalDoc.getIsConfidential() != null) {
            caseDataUpdated.put(MANAGE_DOCUMENTS_RESTRICTED_FLAG, "True");
        } else {
            caseDataUpdated.remove(MANAGE_DOCUMENTS_RESTRICTED_FLAG);
        }
        if (CollectionUtils.isNotEmpty(caseData.getCourtStaffQuarantineDocsList())
            || CollectionUtils.isNotEmpty(caseData.getCafcassQuarantineDocsList())
            || CollectionUtils.isNotEmpty(caseData.getLegalProfQuarantineDocsList())) {
            updateCaseDataUpdatedByRole(caseDataUpdated, userRole);
        } else {
            caseDataUpdated.remove(MANAGE_DOCUMENTS_TRIGGERED_BY);
        }
    }

    public void moveDocumentsToQuarantineTab(
        QuarantineLegalDoc quarantineLegalDoc,
        CaseData caseData,
        Map<String, Object> caseDataUpdated,
        String userRole) {
        List<Element<QuarantineLegalDoc>> existingQuarantineDocuments = getQuarantineDocs(caseData, userRole, false);
        existingQuarantineDocuments.add(element(quarantineLegalDoc));
        updateQuarantineDocs(caseDataUpdated, existingQuarantineDocuments, userRole, false);
    }

    // Pass Authorisation only when called via Manage document service
    public void moveDocumentsToRespectiveCategories(
        String authorization,
        QuarantineLegalDoc quarantineLegalDoc,
        CaseData caseData,
        Map<String, Object> caseDataUpdated,
        String userRole) {

        String restrcitedKey = getRestrictedOrConfidentialKey(quarantineLegalDoc);
        if (restrcitedKey != null) {
            quarantineLegalDoc = downloadAndDeleteDocumentNew(
                authorization,
                quarantineLegalDoc
            );
            moveToConfidentialOrRestricted(
                caseDataUpdated,
                CONFIDENTIAL_DOCUMENTS.equals(restrcitedKey)
                    ? caseData.getReviewDocuments().getConfidentialDocuments()
                    : caseData.getReviewDocuments().getRestrictedDocuments(),
                quarantineLegalDoc,
                restrcitedKey
            );
        } else {
            List<Element<QuarantineLegalDoc>> existingCaseDocuments = getQuarantineDocs(caseData, userRole, true);
            existingCaseDocuments.add(element(quarantineLegalDoc));
            updateQuarantineDocs(caseDataUpdated, existingCaseDocuments, userRole, true);
        }
    }

    /**
     * Based on user input documents will be moved either to confidential or restricted documents.
     * ifConfidential && isRestricted - RESTRICTED
     * !ifConfidential && isRestricted - RESTRICTED
     * ifConfidential && !isRestricted - CONFIDENTIAL
     */
    public String getRestrictedOrConfidentialKey(QuarantineLegalDoc quarantineLegalDoc) {
        if (quarantineLegalDoc.getIsConfidential() != null) {
            if (YesOrNo.Yes.equals(quarantineLegalDoc.getIsConfidential())
                && YesOrNo.No.equals(quarantineLegalDoc.getIsRestricted())) {
                return CONFIDENTIAL_DOCUMENTS;
            } else {
                return RESTRICTED_DOCUMENTS;
            }
        }
        return null;
    }

    public void moveToConfidentialOrRestricted(Map<String, Object> caseDataUpdated,
                                               List<Element<QuarantineLegalDoc>> confidentialOrRestrictedDocuments,
                                               QuarantineLegalDoc uploadDoc,
                                               String confidentialOrRestrictedKey) {
        if (null != confidentialOrRestrictedDocuments) {
            confidentialOrRestrictedDocuments.add(element(uploadDoc));
            confidentialOrRestrictedDocuments.sort(Comparator.comparing(
                doc -> doc.getValue().getDocumentUploadedDate(),
                Comparator.reverseOrder()
            ));
            caseDataUpdated.put(confidentialOrRestrictedKey, confidentialOrRestrictedDocuments);
        } else {
            caseDataUpdated.put(confidentialOrRestrictedKey, List.of(element(uploadDoc)));
        }
    }


    private QuarantineLegalDoc downloadAndDeleteDocumentNew(
        String authorization,
        QuarantineLegalDoc quarantineLegalDoc) {
        try {
            Document document = getDocumentFromQuarantineObject(quarantineLegalDoc);
            UUID documentId = UUID.fromString(DocumentUtils.getDocumentId(document.getDocumentUrl()));
            log.info(" DocumentId found {}", documentId);
            Document newUploadedDocument = getNewUploadedDocument(
                authorization,
                document,
                documentId
            );

            log.info("document uploaded {}", newUploadedDocument);
            if (null != newUploadedDocument) {
                caseDocumentClient.deleteDocument(systemUserService.getSysUserToken(),
                                                  authTokenGenerator.generate(),
                                                  documentId, true
                );
                log.info("deleted document {}", documentId);

                QuarantineLegalDoc newQuarantineLegalDoc = DocumentUtils.getQuarantineUploadDocument(
                    quarantineLegalDoc.getCategoryId(),
                    newUploadedDocument,
                    objectMapper
                );
                return addQuarantineDocumentFields(newQuarantineLegalDoc, quarantineLegalDoc);


            } else {
                throw new IllegalStateException("Failed to move document to confidential tab please retry");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to move document to confidential tab please retry", e);
        }
    }

    public QuarantineLegalDoc addQuarantineDocumentFields(QuarantineLegalDoc legalProfUploadDoc,
                                                           QuarantineLegalDoc quarantineLegalDoc) {

        return legalProfUploadDoc.toBuilder()
            .documentParty(quarantineLegalDoc.getDocumentParty())
            .documentUploadedDate(quarantineLegalDoc.getDocumentUploadedDate())
            .notes(quarantineLegalDoc.getNotes())
            .categoryId(quarantineLegalDoc.getCategoryId())
            .categoryName(quarantineLegalDoc.getCategoryName())
            .fileName(quarantineLegalDoc.getFileName())
            .controlNumber(quarantineLegalDoc.getControlNumber())
            .type(quarantineLegalDoc.getType())
            .subtype(quarantineLegalDoc.getSubtype())
            .exceptionRecordReference(quarantineLegalDoc.getExceptionRecordReference())
            .url(legalProfUploadDoc.getConfidentialDocument() == null ? quarantineLegalDoc.getUrl() : null)
            .scannedDate(quarantineLegalDoc.getScannedDate())
            .deliveryDate(quarantineLegalDoc.getDeliveryDate())
            //PRL-4320 - Manage documents redesign
            .isConfidential(quarantineLegalDoc.getIsConfidential())
            .isRestricted(quarantineLegalDoc.getIsRestricted())
            .notes(quarantineLegalDoc.getRestrictedDetails())
            .uploadedBy(quarantineLegalDoc.getUploadedBy())
            .uploadedByIdamId(quarantineLegalDoc.getUploadedByIdamId())
            .build();
    }

    private Document getNewUploadedDocument(String authorization, Document document,
                                            UUID documentId) {
        byte[] docData;
        Document newUploadedDocument = null;
        try {
            String sysUserToken = null != authorization ? authorization : systemUserService.getSysUserToken();
            String serviceToken = authTokenGenerator.generate();
            Resource resource = caseDocumentClient.getDocumentBinary(sysUserToken, serviceToken,
                                                                     documentId
            ).getBody();
            docData = IOUtils.toByteArray(resource.getInputStream());
            UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
                sysUserToken,
                serviceToken,
                PrlAppsConstants.CASE_TYPE,
                PrlAppsConstants.JURISDICTION,
                List.of(
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        CONFIDENTIAL + document.getDocumentFileName(),
                        APPLICATION_PDF_VALUE,
                        docData
                    ))
            );
            newUploadedDocument = Document.buildFromDocument(uploadResponse.getDocuments().get(0));
        } catch (Exception ex) {
            log.error("Failed to upload new document {}", ex.getMessage());
        }
        return newUploadedDocument;
    }

    private Document getQuarantineDocumentForUploader(String uploadedBy,
                                                      QuarantineLegalDoc quarantineLegalDoc) {
        return switch (uploadedBy) {
            case SOLICITOR -> quarantineLegalDoc.getDocument();
            case CAFCASS -> quarantineLegalDoc.getCafcassQuarantineDocument();
            case COURT_STAFF -> quarantineLegalDoc.getCourtStaffQuarantineDocument();
            case BULK_SCAN -> quarantineLegalDoc.getUrl();
            default -> null;
        };
    }

    public Document getDocumentFromQuarantineObject(QuarantineLegalDoc quarantineLegalDoc) {

        String attributeName = quarantineLegalDoc.getCategoryId() + "Document";
        return objectMapper.convertValue(objectMapper.convertValue(quarantineLegalDoc, Map.class).get(attributeName), Document.class);
    }

    private String formulateCategoryId(ManageDocuments manageDocument) {
        if (DocumentPartyEnum.COURT.equals(manageDocument.getDocumentParty())) {
            return INTERNAL_CORRESPONDENCE_LABEL;
        }
        return manageDocument.getDocumentCategories().getValueCode();

    }

    public List<String> validateCourtUser(CallbackRequest callbackRequest,
                                          UserDetails userDetails) {
        if (isCourtSelectedInDocumentParty(callbackRequest)
            && !checkIfUserIsCourtStaff(userDetails)) {
            return List.of("Only court admin/Judge can select the value 'court' for 'submitting on behalf of'");
        }
        return Collections.emptyList();
    }

    public boolean checkIfUserIsCourtStaff(UserDetails userDetails) {
        return userDetails.getRoles().stream().anyMatch(ROLES::contains);
    }

    public boolean isCourtSelectedInDocumentParty(CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        return caseData.getManageDocuments().stream()
            .anyMatch(element -> DocumentPartyEnum.COURT.equals(element.getValue().getDocumentParty()));
    }

    private void updateCaseDataUpdatedByRole(Map<String,Object> caseDataUpdated,
                                             String userRole) {

        if (SOLICITOR.equals(userRole)) {
            caseDataUpdated.put(MANAGE_DOCUMENTS_TRIGGERED_BY, "SOLICITOR");
        } else if (CAFCASS.equals(userRole)) {
            caseDataUpdated.put(MANAGE_DOCUMENTS_TRIGGERED_BY, "CAFCASS");
        } else if (COURT_STAFF.equals(userRole)) {
            caseDataUpdated.put(MANAGE_DOCUMENTS_TRIGGERED_BY, "STAFF");
        }
    }

    private void updateQuarantineDocs(Map<String, Object> caseDataUpdated,
                                      List<Element<QuarantineLegalDoc>> quarantineDocs,
                                      String userRole,
                                      boolean isDocumentTab) {
        if (CommonUtils.isEmpty(userRole)) {
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

            case COURT_ADMIN:
                if (isDocumentTab) {
                    caseDataUpdated.put("courtStaffUploadDocListDocTab", quarantineDocs);
                } else {
                    caseDataUpdated.put("courtStaffUploadDocListConfTab", quarantineDocs);
                }
                break;

            default:
                throw new IllegalStateException(UNEXPECTED_USER_ROLE + userRole);

        }
    }

    private List<Element<QuarantineLegalDoc>> getQuarantineDocs(CaseData caseData,
                                                                String userRole,
                                                                boolean isDocumentTab) {
        if (CommonUtils.isEmpty(userRole)) {
            throw new IllegalStateException(UNEXPECTED_USER_ROLE + userRole);
        }

        return switch (userRole) {
            case SOLICITOR -> getQuarantineOrUploadDocsBasedOnDocumentTab(
                    isDocumentTab,
                    caseData.getReviewDocuments().getLegalProfUploadDocListDocTab(),
                    caseData.getLegalProfQuarantineDocsList()
            );
            case CAFCASS -> getQuarantineOrUploadDocsBasedOnDocumentTab(
                    isDocumentTab,
                    caseData.getReviewDocuments().getCafcassUploadDocListDocTab(),
                    caseData.getCafcassQuarantineDocsList()
            );
            case COURT_STAFF -> getQuarantineOrUploadDocsBasedOnDocumentTab(
                    isDocumentTab,
                    caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab(),
                    caseData.getCourtStaffQuarantineDocsList()
            );
            case COURT_ADMIN -> getQuarantineOrUploadDocsBasedOnDocumentTab(
                    isDocumentTab,
                    caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab(),
                    caseData.getReviewDocuments().getCourtStaffUploadDocListConfTab()
            );
            default -> throw new IllegalStateException(UNEXPECTED_USER_ROLE + userRole);
        };
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
}
