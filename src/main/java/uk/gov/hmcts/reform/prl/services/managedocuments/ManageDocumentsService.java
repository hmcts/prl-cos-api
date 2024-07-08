package uk.gov.hmcts.reform.prl.services.managedocuments;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.amroles.InternalCaseworkerAmRolesEnum;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BULK_SCAN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONFIDENTIAL_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INTERNAL_CORRESPONDENCE_CATEGORY_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INTERNAL_CORRESPONDENCE_LABEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_ADVISER_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_PROFESSIONAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESTRICTED_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MULTIPART_FILE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY;
import static uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc.quarantineCategoriesToRemove;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsService {
    public static final String UNEXPECTED_USER_ROLE = "Unexpected user role : ";
    public static final String MANAGE_DOCUMENTS_RESTRICTED_FLAG = "manageDocumentsRestrictedFlag";
    public static final String FM5_ERROR = "The statement of position on non-court dispute resolution "
        + "(form FM5) cannot contain confidential information or be restricted.";
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final CaseDocumentClient caseDocumentClient;
    private final SystemUserService systemUserService;
    private final AllTabServiceImpl allTabService;
    private final LaunchDarklyClient launchDarklyClient;
    private final RoleAssignmentApi roleAssignmentApi;

    public static final String CONFIDENTIAL = "Confidential_";

    public static final String MANAGE_DOCUMENTS_TRIGGERED_BY = "manageDocumentsTriggeredBy";
    public static final String DETAILS_ERROR_MESSAGE
        = "You must give a reason why the document should be restricted";

    public CaseData populateDocumentCategories(String authorization, CaseData caseData) {
        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentCategories(getCategoriesSubcategories(authorization, String.valueOf(caseData.getId())))
            .build();

        return caseData.toBuilder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .isC8DocumentPresent(CaseUtils.isC8PresentCheckDraftAndFinal(caseData) ? "Yes" : "No")
                                           .manageDocuments(Arrays.asList(element(manageDocuments)))
                                           .build())
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
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<Element<ManageDocuments>> manageDocuments = caseData.getDocumentManagementDetails().getManageDocuments();

        for (Element<ManageDocuments> element : manageDocuments) {
            boolean restricted = element.getValue().getIsRestricted().equals(YesOrNo.Yes);
            boolean confidential = element.getValue().getIsConfidential().equals(YesOrNo.Yes);
            boolean restrictedReasonEmpty = element.getValue().getRestrictedDetails() == null
                || element.getValue().getRestrictedDetails().isEmpty();

            if (SOLICITOR.equals(userRole) && restricted && restrictedReasonEmpty) {
                errorList.add(DETAILS_ERROR_MESSAGE);
            }

            if ("fm5Statements".equalsIgnoreCase(element.getValue().getDocumentCategories().getValue().getCode())
                && (restricted || confidential)) {
                errorList.add(FM5_ERROR);
            }
        }

        return errorList;
    }

    public Map<String, Object> copyDocument(CallbackRequest callbackRequest, String authorization) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        UserDetails userDetails = userService.getUserDetails(authorization);
        final String[] surname = {null};
        userDetails.getSurname().ifPresent(snm -> surname[0] = snm);
        UserDetails updatedUserDetails = UserDetails.builder()
            .email(userDetails.getEmail())
            .id(userDetails.getId())
            .surname(surname[0])
            .forename(userDetails.getForename())
            .roles(getLoggedInUserType(authorization))
            .build();
        transformAndMoveDocument(
            caseData,
            caseDataUpdated,
            updatedUserDetails
        );
        caseDataUpdated.remove("manageDocuments");
        return caseDataUpdated;
    }

    private void transformAndMoveDocument(CaseData caseData, Map<String, Object> caseDataUpdated,
                                          UserDetails userDetails) {

        String userRole = CaseUtils.getUserRole(userDetails);
        List<Element<ManageDocuments>> manageDocuments = caseData.getDocumentManagementDetails().getManageDocuments();
        boolean isWaTaskSetForFirstDocumentIteration = false;
        for (Element<ManageDocuments> element : manageDocuments) {
            CaseData updatedCaseData = objectMapper.convertValue(caseDataUpdated, CaseData.class);
            ManageDocuments manageDocument = element.getValue();
            QuarantineLegalDoc quarantineLegalDoc = covertManageDocToQuarantineDoc(manageDocument, userDetails);

            if (userRole.equals(COURT_ADMIN) || DocumentPartyEnum.COURT.equals(manageDocument.getDocumentParty())
                || getRestrictedOrConfidentialKey(quarantineLegalDoc) == null
            ) {
                moveDocumentsToRespectiveCategoriesNew(
                    quarantineLegalDoc,
                    userDetails,
                    updatedCaseData,
                    caseDataUpdated,
                    userRole
                );
            } else {
                if (!isWaTaskSetForFirstDocumentIteration) {
                    isWaTaskSetForFirstDocumentIteration = true;
                    setFlagsForWaTask(updatedCaseData, caseDataUpdated, userRole, quarantineLegalDoc);
                }
                moveDocumentsToQuarantineTab(quarantineLegalDoc, updatedCaseData, caseDataUpdated, userRole);
            }
        }
    }

    public void moveDocumentsToRespectiveCategoriesNew(QuarantineLegalDoc quarantineLegalDoc, UserDetails userDetails,
                                                       CaseData caseData, Map<String, Object> caseDataUpdated, String userRole) {
        String restrictedKey = getRestrictedOrConfidentialKey(quarantineLegalDoc);

        if (restrictedKey != null) {
            //This will be executed only during review documents
            if (!userRole.equals(COURT_ADMIN)
                && !DocumentPartyEnum.COURT.getDisplayedValue().equals(quarantineLegalDoc.getDocumentParty())) {
                String loggedInUserType = DocumentUtils.getLoggedInUserType(userDetails);
                Document document = getQuarantineDocumentForUploader(loggedInUserType, quarantineLegalDoc);
                Document updatedConfidentialDocument = downloadAndDeleteDocument(document, systemUserService.getSysUserToken());
                quarantineLegalDoc = setQuarantineDocumentForUploader(
                    ManageDocuments.builder()
                        .document(updatedConfidentialDocument)
                        .build(),
                    loggedInUserType,
                    quarantineLegalDoc
                );
            }
            if (quarantineLegalDoc != null) {
                QuarantineLegalDoc finalConfidentialDocument = convertQuarantineDocumentToRightCategoryDocument(
                    quarantineLegalDoc,
                    userDetails
                );
                //This will be executed only during manage documents
                if (userRole.equals(COURT_ADMIN) || DocumentPartyEnum.COURT.getDisplayedValue().equals(
                    quarantineLegalDoc.getDocumentParty())) {
                    finalConfidentialDocument = finalConfidentialDocument.toBuilder()
                        .hasTheConfidentialDocumentBeenRenamed(YesOrNo.No)
                        .build();
                }

                moveToConfidentialOrRestricted(
                    caseDataUpdated,
                    CONFIDENTIAL_DOCUMENTS.equals(restrictedKey)
                        ? caseData.getReviewDocuments().getConfidentialDocuments()
                        : caseData.getReviewDocuments().getRestrictedDocuments(),
                    finalConfidentialDocument,
                    restrictedKey
                );
            }
        } else {
            // Remove these attributes for Non Confidential documents
            quarantineLegalDoc = quarantineLegalDoc.toBuilder()
                .isConfidential(null)
                .isRestricted(null)
                .restrictedDetails(null)
                .build();

            QuarantineLegalDoc finalConfidentialDocument = convertQuarantineDocumentToRightCategoryDocument(
                quarantineLegalDoc,
                userDetails
            );
            List<Element<QuarantineLegalDoc>> existingCaseDocuments = getQuarantineDocs(caseData, userRole, true);
            existingCaseDocuments.add(element(finalConfidentialDocument));
            updateQuarantineDocs(caseDataUpdated, existingCaseDocuments, userRole, true);
        }
    }

    private QuarantineLegalDoc convertQuarantineDocumentToRightCategoryDocument(QuarantineLegalDoc quarantineLegalDoc, UserDetails userDetails) {
        String loggedInUserType = DocumentUtils.getLoggedInUserType(userDetails);

        Document document = getQuarantineDocumentForUploader(loggedInUserType, quarantineLegalDoc);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(DocumentUtils.populateAttributeNameFromCategoryId(quarantineLegalDoc.getCategoryId(), loggedInUserType), document);
        objectMapper.registerModule(new ParameterNamesModule());
        QuarantineLegalDoc finalQuarantineDocument = objectMapper.convertValue(hashMap, QuarantineLegalDoc.class);
        return finalQuarantineDocument.toBuilder()
            .documentParty(quarantineLegalDoc.getDocumentParty())
            .documentUploadedDate(quarantineLegalDoc.getDocumentUploadedDate())
            .categoryId(quarantineLegalDoc.getCategoryId())
            .categoryName(quarantineLegalDoc.getCategoryName())
            //PRL-4320 - Manage documents redesign
            .isConfidential(quarantineLegalDoc.getIsConfidential())
            .isRestricted(quarantineLegalDoc.getIsRestricted())
            .restrictedDetails(quarantineLegalDoc.getRestrictedDetails())
            .uploadedBy(quarantineLegalDoc.getUploadedBy())
            .uploadedByIdamId(quarantineLegalDoc.getUploadedByIdamId())
            .uploaderRole(quarantineLegalDoc.getUploaderRole())
            //PRL-5006 - bulk scan fields
            .fileName(quarantineLegalDoc.getFileName())
            .controlNumber(quarantineLegalDoc.getControlNumber())
            .type(quarantineLegalDoc.getType())
            .subtype(quarantineLegalDoc.getSubtype())
            .exceptionRecordReference(quarantineLegalDoc.getExceptionRecordReference())
            .scannedDate(quarantineLegalDoc.getScannedDate())
            .deliveryDate(quarantineLegalDoc.getDeliveryDate())
            .build();

    }

    private QuarantineLegalDoc covertManageDocToQuarantineDoc(ManageDocuments manageDocument, UserDetails userDetails) {
        boolean isCourtPartySelected = DocumentPartyEnum.COURT.equals(manageDocument.getDocumentParty());

        String loggedInUserType = DocumentUtils.getLoggedInUserType(userDetails);
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder()
            .documentParty(manageDocument.getDocumentParty().getDisplayedValue())
            .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
            .categoryId(isCourtPartySelected ? INTERNAL_CORRESPONDENCE_CATEGORY_ID : manageDocument.getDocumentCategories().getValueCode())
            .categoryName(isCourtPartySelected ? INTERNAL_CORRESPONDENCE_LABEL : manageDocument.getDocumentCategories().getValueLabel())
            //PRL-4320 - Manage documents redesign
            .isConfidential(manageDocument.getIsConfidential())
            .isRestricted(manageDocument.getIsRestricted())
            .restrictedDetails(manageDocument.getRestrictedDetails())
            .uploadedBy(userDetails.getFullName())
            .uploadedByIdamId(userDetails.getId())
            .uploaderRole(loggedInUserType)
            .build();
        return setQuarantineDocumentForUploader(manageDocument, loggedInUserType, quarantineLegalDoc);
    }

    public void setFlagsForWaTask(CaseData caseData, Map<String, Object> caseDataUpdated, String userRole, QuarantineLegalDoc quarantineLegalDoc) {
        //Setting this flag for WA task
        if (userRole.equals(CITIZEN) || quarantineLegalDoc.getIsConfidential() != null || quarantineLegalDoc.getIsRestricted() != null) {
            caseDataUpdated.put(MANAGE_DOCUMENTS_RESTRICTED_FLAG, "True");
        } else {
            caseDataUpdated.remove(MANAGE_DOCUMENTS_RESTRICTED_FLAG);
        }
        if (CollectionUtils.isNotEmpty(caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList())
            || CollectionUtils.isNotEmpty(caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList())
            || CollectionUtils.isNotEmpty(caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList())
            || CollectionUtils.isNotEmpty(caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList())
            || (CollectionUtils.isNotEmpty(caseData.getScannedDocuments())
            && caseData.getScannedDocuments().size() > 1)) {
            caseDataUpdated.remove(MANAGE_DOCUMENTS_TRIGGERED_BY);
        } else {
            updateCaseDataUpdatedByRole(caseDataUpdated, userRole);
        }
    }

    public void moveDocumentsToQuarantineTab(QuarantineLegalDoc quarantineLegalDoc,
                                             CaseData caseData,
                                             Map<String, Object> caseDataUpdated,
                                             String userRole) {
        List<Element<QuarantineLegalDoc>> existingQuarantineDocuments = getQuarantineDocs(caseData, userRole, false);
        existingQuarantineDocuments.add(element(quarantineLegalDoc));
        updateQuarantineDocs(caseDataUpdated, existingQuarantineDocuments, userRole, false);
    }

    /**
     * Based on user input documents will be moved either to confidential or restricted documents.
     * ifConfidential && isRestricted - RESTRICTED
     * !ifConfidential && isRestricted - RESTRICTED
     * ifConfidential && !isRestricted - CONFIDENTIAL
     */
    public String getRestrictedOrConfidentialKey(QuarantineLegalDoc quarantineLegalDoc) {
        if (quarantineLegalDoc.getIsConfidential() != null && (!(YesOrNo.No.equals(quarantineLegalDoc.getIsConfidential())
            && YesOrNo.No.equals(quarantineLegalDoc.getIsRestricted())))) {
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


    public Document downloadAndDeleteDocument(Document document, String systemAuthorisation) {
        try {
            if (!document.getDocumentFileName().startsWith(CONFIDENTIAL)) {
                UUID documentId = UUID.fromString(DocumentUtils.getDocumentId(document.getDocumentUrl()));
                Document newUploadedDocument = getNewUploadedDocument(
                    document,
                    documentId
                );
                if (null != newUploadedDocument) {
                    caseDocumentClient.deleteDocument(systemAuthorisation,
                                                      authTokenGenerator.generate(),
                                                      documentId, true
                    );
                    return newUploadedDocument;
                }

            } else {
                throw new IllegalStateException("Failed to move document to confidential tab please retry");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to move document to confidential tab please retry", e);
        }
        return document;
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

    private Document getNewUploadedDocument(Document document,
                                            UUID documentId) {
        byte[] docData;
        Document newUploadedDocument = null;
        try {
            String sysUserToken = systemUserService.getSysUserToken();
            String serviceToken = authTokenGenerator.generate();
            Resource resource = caseDocumentClient.getDocumentBinary(sysUserToken, serviceToken,
                                                                     documentId
            ).getBody();
            docData = IOUtils.toByteArray(resource.getInputStream());
            UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
                sysUserToken,
                serviceToken,
                CASE_TYPE,
                JURISDICTION,
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
            log.error("Failed to upload new document {}", ex.getMessage(), ex);
        }
        return newUploadedDocument;
    }

    public Document getQuarantineDocumentForUploader(String uploadedBy,
                                                     QuarantineLegalDoc quarantineLegalDoc) {
        return switch (uploadedBy) {
            case LEGAL_PROFESSIONAL -> quarantineLegalDoc.getDocument();
            case CAFCASS -> quarantineLegalDoc.getCafcassQuarantineDocument();
            case COURT_STAFF -> quarantineLegalDoc.getCourtStaffQuarantineDocument();
            case BULK_SCAN -> quarantineLegalDoc.getUrl();
            case CITIZEN -> quarantineLegalDoc.getCitizenQuarantineDocument();
            default -> null;
        };
    }

    private QuarantineLegalDoc setQuarantineDocumentForUploader(ManageDocuments manageDocument, String uploadedBy,
                                                                QuarantineLegalDoc quarantineLegalDoc) {
        return switch (uploadedBy) {
            case LEGAL_PROFESSIONAL -> quarantineLegalDoc.toBuilder().document(manageDocument.getDocument()).build();
            case CAFCASS ->
                quarantineLegalDoc.toBuilder().cafcassQuarantineDocument(manageDocument.getDocument()).build();
            case COURT_STAFF ->
                quarantineLegalDoc.toBuilder().courtStaffQuarantineDocument(manageDocument.getDocument()).build();
            case BULK_SCAN -> quarantineLegalDoc.toBuilder().url(manageDocument.getDocument()).build();
            case CITIZEN -> quarantineLegalDoc.toBuilder().citizenQuarantineDocument(manageDocument.getDocument()).build();
            default -> null;
        };
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
        return userDetails.getRoles().stream().anyMatch(COURT_STAFF::contains);
    }

    public boolean isCourtSelectedInDocumentParty(CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        return caseData.getDocumentManagementDetails().getManageDocuments().stream()
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
        } else if (CITIZEN.equals(userRole)) {
            caseDataUpdated.put(MANAGE_DOCUMENTS_TRIGGERED_BY, "CITIZEN");
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
            case SOLICITOR ->
                caseDataUpdated.put(isDocumentTab ? "legalProfUploadDocListDocTab" : "legalProfQuarantineDocsList",
                                    quarantineDocs);
            case CAFCASS ->
                caseDataUpdated.put(isDocumentTab ? "cafcassUploadDocListDocTab" : "cafcassQuarantineDocsList",
                                    quarantineDocs);
            case COURT_STAFF ->
                caseDataUpdated.put(isDocumentTab ? "courtStaffUploadDocListDocTab" : "courtStaffQuarantineDocsList",
                                    quarantineDocs);
            case COURT_ADMIN -> {
                if (isDocumentTab) {
                    caseDataUpdated.put("courtStaffUploadDocListDocTab", quarantineDocs);
                }
            }
            case BULK_SCAN -> {
                if (isDocumentTab) {
                    caseDataUpdated.put("bulkScannedDocListDocTab", quarantineDocs);
                }
            }
            case CITIZEN ->
                caseDataUpdated.put(isDocumentTab ? "citizenUploadedDocListDocTab" : "citizenQuarantineDocsList",
                                    quarantineDocs);
            default -> throw new IllegalStateException(UNEXPECTED_USER_ROLE + userRole);
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
                    caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList()
            );
            case CAFCASS -> getQuarantineOrUploadDocsBasedOnDocumentTab(
                    isDocumentTab,
                    caseData.getReviewDocuments().getCafcassUploadDocListDocTab(),
                    caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList()
            );
            case COURT_STAFF -> getQuarantineOrUploadDocsBasedOnDocumentTab(
                    isDocumentTab,
                    caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab(),
                    caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList()
            );
            case COURT_ADMIN -> getQuarantineOrUploadDocsBasedOnDocumentTab(
                    isDocumentTab,
                    caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab(),
                    caseData.getReviewDocuments().getCourtStaffUploadDocListConfTab()//not in use
            );
            case BULK_SCAN -> getQuarantineOrUploadDocsBasedOnDocumentTab(
                isDocumentTab,
                caseData.getReviewDocuments().getBulkScannedDocListDocTab(),
                caseData.getReviewDocuments().getBulkScannedDocListConfTab()//not in use
            );
            case CITIZEN -> getQuarantineOrUploadDocsBasedOnDocumentTab(
                isDocumentTab,
                caseData.getReviewDocuments().getCitizenUploadedDocListDocTab(),
                caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList()
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

    public void appendConfidentialDocumentNameForCourtAdminAndUpdate(CallbackRequest callbackRequest, String authorisation) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
                = allTabService.getStartAllTabsUpdate(String.valueOf(callbackRequest.getCaseDetails().getId()));
        Map<String, Object> updatedCaseDataMap
                = appendConfidentialDocumentNameForCourtAdmin(authorisation,
                startAllTabsUpdateDataContent.caseDataMap(),
                startAllTabsUpdateDataContent.caseData());
        //update all tabs
        allTabService.submitAllTabsUpdate(startAllTabsUpdateDataContent.authorisation(),
                String.valueOf(callbackRequest.getCaseDetails().getId()),
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                updatedCaseDataMap);
    }

    public Map<String, Object> appendConfidentialDocumentNameForCourtAdmin(String authorization, Map<String, Object> caseDataMap, CaseData caseData) {
        List<String> userRole = getLoggedInUserType(authorization);
        if (userRole.contains(COURT_ADMIN) || userRole.contains(COURT_STAFF)) {
            if (CollectionUtils.isNotEmpty(caseData.getReviewDocuments().getConfidentialDocuments())) {
                List<Element<QuarantineLegalDoc>> confidentialDocuments = renameConfidentialDocumentForCourtAdmin(
                    caseData.getReviewDocuments().getConfidentialDocuments());
                caseDataMap.put("confidentialDocuments", confidentialDocuments);
            }
            if (CollectionUtils.isNotEmpty(caseData.getReviewDocuments().getRestrictedDocuments())) {
                List<Element<QuarantineLegalDoc>> restrictedDocuments = renameConfidentialDocumentForCourtAdmin(
                    caseData.getReviewDocuments().getRestrictedDocuments());
                caseDataMap.put("restrictedDocuments", restrictedDocuments);
            }
        }
        caseDataMap.remove("manageDocuments");
        return caseDataMap;
    }

    private List<Element<QuarantineLegalDoc>> renameConfidentialDocumentForCourtAdmin(List<Element<QuarantineLegalDoc>> confidentialDocuments) {
        List<Element<QuarantineLegalDoc>> confidentialTabDocuments = new ArrayList<>();
        final @NotNull @Valid QuarantineLegalDoc[] quarantineLegalDoc = new QuarantineLegalDoc[1];
        confidentialDocuments.parallelStream().forEach(
            element -> {
                if (YesOrNo.No.equals(element.getValue().getHasTheConfidentialDocumentBeenRenamed())) {
                    quarantineLegalDoc[0] = element.getValue();

                    String attributeName = DocumentUtils.populateAttributeNameFromCategoryId(quarantineLegalDoc[0].getCategoryId(), null);
                    Document existingDocument = objectMapper.convertValue(
                        objectMapper.convertValue(quarantineLegalDoc[0], Map.class).get(attributeName),
                        Document.class
                    );
                    QuarantineLegalDoc updatedQuarantineLegalDocumentObject = quarantineLegalDoc[0];

                    Document renamedDocument = downloadAndDeleteDocument(existingDocument, systemUserService.getSysUserToken());
                    Map tempQuarantineObjectMap =
                        objectMapper.convertValue(quarantineLegalDoc[0], Map.class);
                    tempQuarantineObjectMap.put(
                        attributeName,
                        renamedDocument
                    );
                    tempQuarantineObjectMap.put("hasTheConfidentialDocumentBeenRenamed", YesOrNo.Yes);
                    updatedQuarantineLegalDocumentObject = objectMapper.convertValue(
                        tempQuarantineObjectMap,
                        QuarantineLegalDoc.class
                    );
                    confidentialTabDocuments.add(element(element.getId(), updatedQuarantineLegalDocumentObject));
                } else {
                    confidentialTabDocuments.add(element);
                }
            }
        );
        return confidentialTabDocuments.stream().sorted(Comparator.comparing(
            m -> m.getValue().getDocumentUploadedDate(),
            Comparator.reverseOrder()
        )).toList();
    }

    public List<String> getLoggedInUserType(String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        List<String> roles = userDetails.getRoles();
        List<String> loggedInUserType = new ArrayList<>();
        if (launchDarklyClient.isFeatureEnabled(ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY)) {
            //This would check for roles from AM for Judge/Legal advisor/Court admin
            //if it doesn't find then it will check for idam roles for rest of the users
            RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentApi.getRoleAssignments(
                authorisation,
                authTokenGenerator.generate(),
                null,
                userDetails.getId()
            );
            if (roles.contains(Roles.SOLICITOR.getValue())) {
                loggedInUserType.add(LEGAL_PROFESSIONAL);
                loggedInUserType.add(SOLICITOR_ROLE);
            } else if (roles.contains(Roles.CITIZEN.getValue())) {
                loggedInUserType.add(CITIZEN_ROLE);
            } else if (roleAssignmentServiceResponse != null) {
                List<String> amRoles = roleAssignmentServiceResponse.getRoleAssignmentResponse()
                    .stream()
                    .map(role -> role.getRoleName()).toList();
                if (amRoles.stream().anyMatch(InternalCaseworkerAmRolesEnum.JUDGE.getRoles()::contains)) {
                    loggedInUserType.add(COURT_STAFF);
                    loggedInUserType.add(JUDGE_ROLE);
                } else if (amRoles.stream().anyMatch(InternalCaseworkerAmRolesEnum.LEGAL_ADVISER.getRoles()::contains)) {
                    loggedInUserType.add(COURT_STAFF);
                    loggedInUserType.add(LEGAL_ADVISER_ROLE);
                } else if (amRoles.stream().anyMatch(InternalCaseworkerAmRolesEnum.COURT_ADMIN.getRoles()::contains)) {
                    loggedInUserType.add(COURT_STAFF);
                    loggedInUserType.add(COURT_ADMIN_ROLE);
                } else if (amRoles.stream().anyMatch(InternalCaseworkerAmRolesEnum.CAFCASS_CYMRU.getRoles()::contains)) {
                    loggedInUserType.add(UserRoles.CAFCASS.name());
                }
            } else if (roles.contains(Roles.BULK_SCAN.getValue())) {
                loggedInUserType.add(BULK_SCAN);
            }
        } else {
            checkExistingIdamRoleConfig(roles, loggedInUserType);
        }
        return loggedInUserType;
    }

    private static void checkExistingIdamRoleConfig(List<String> roles, List<String> loggedInUserType) {
        if (roles.contains(Roles.JUDGE.getValue())) {
            loggedInUserType.add(COURT_STAFF);
            loggedInUserType.add(JUDGE_ROLE);
        } else if (roles.contains(Roles.LEGAL_ADVISER.getValue())) {
            loggedInUserType.add(COURT_STAFF);
            loggedInUserType.add(LEGAL_ADVISER_ROLE);
        } else if (roles.contains(Roles.COURT_ADMIN.getValue())) {
            loggedInUserType.add(COURT_STAFF);
            loggedInUserType.add(COURT_ADMIN_ROLE);
        } else if (roles.contains(Roles.SOLICITOR.getValue())) {
            loggedInUserType.add(LEGAL_PROFESSIONAL);
            loggedInUserType.add(SOLICITOR_ROLE);
        } else if (roles.contains(Roles.CITIZEN.getValue())) {
            loggedInUserType.add(CITIZEN_ROLE);
        } else if (roles.contains(Roles.BULK_SCAN.getValue())) {
            loggedInUserType.add(BULK_SCAN);
        } else {
            loggedInUserType.add(UserRoles.CAFCASS.name());
        }
    }
}
