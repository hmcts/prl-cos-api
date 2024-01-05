package uk.gov.hmcts.reform.prl.services.managedocuments;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.reviewdocument.ReviewDocumentService;
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
import java.util.function.Predicate;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;
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
    private final ReviewDocumentService reviewDocumentService;
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


    public Map<String, Object> copyDocument(CallbackRequest callbackRequest, String authorization) {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        List<Element<ManageDocuments>> manageDocuments = caseData.getManageDocuments();
        UserDetails userDetails = userService.getUserDetails(authorization);
        String userRole = CaseUtils.getUserRole(userDetails);

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

            //PRL-4320 - Updated for when documents need to put into quarantine
            Predicate<Element<ManageDocuments>> restricted = manageDocumentsElement ->
                YesOrNo.Yes.equals(manageDocumentsElement.getValue().getIsConfidential())
                    || YesOrNo.Yes.equals(manageDocumentsElement.getValue().getIsRestricted());

            boolean isRestrictedFlag = false;
            for (Element<ManageDocuments> element : manageDocuments) {
                //Move documents either to restricted/confidential or case documents if uploaded by court admin
                if (COURT_ADMIN.equals(userRole)) {
                    moveToConfidentialTabOrCaseDocumentsTab(caseDataUpdated,
                                                            caseData,
                                                            element,
                                                            restricted,
                                                            userDetails,
                                                            tabDocuments);

                } else if (addToQuarantineDocsOrTabDocumentsAndReturnConfigFlag(
                    element,
                    restricted,
                    userRole,
                    quarantineDocs,
                    tabDocuments,
                    userDetails
                )) {
                    isRestrictedFlag = true;
                }
            }
            //if any restricted docs
            updateRestrictedFlag(caseDataUpdated, isRestrictedFlag);

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

    private void moveToConfidentialTabOrCaseDocumentsTab(Map<String, Object> caseDataUpdated,
                                                         CaseData caseData,
                                                         Element<ManageDocuments> element,
                                                         Predicate<Element<ManageDocuments>> restricted,
                                                         UserDetails userDetails,
                                                         List<Element<QuarantineLegalDoc>> tabDocuments) {
        ManageDocuments manageDocument = element.getValue();
        if (restricted.test(element)) {
            reviewDocumentService.moveToConfidentialOrRestricted(caseDataUpdated,
                                                                 caseData,
                                                                 getTempQuarantineLegalDocForReview(manageDocument, userDetails),
                                                                 COURT_ADMIN);
        } else {
            //move documents to case documents
            getQuarantineLegalDocAndAddToCaseDocuments(
                manageDocument.getDocumentCategories().getValueCode(),
                manageDocument,
                userDetails,
                tabDocuments
            );
        }
    }

    private QuarantineLegalDoc getTempQuarantineLegalDocForReview(ManageDocuments manageDocument,
                                                                  UserDetails userDetails) {
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder()
            .courtStaffQuarantineDocument(manageDocument.getDocument().toBuilder()
                                              .documentCreatedOn(localZoneDate).build())
            .build();
        return DocumentUtils.addQuarantineFields(quarantineLegalDoc, manageDocument, userDetails);
    }

    private void updateRestrictedFlag(Map<String, Object> caseDataUpdated, boolean isRestrictedFlag) {
        if (isRestrictedFlag) {
            caseDataUpdated.put(MANAGE_DOCUMENTS_RESTRICTED_FLAG, "True");
        } else {
            caseDataUpdated.remove(MANAGE_DOCUMENTS_RESTRICTED_FLAG);
        }
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

    private boolean addToQuarantineDocsOrTabDocumentsAndReturnConfigFlag(Element<ManageDocuments> element,
                                                                         Predicate<Element<ManageDocuments>> restricted,
                                                                         String userRole,
                                                                         List<Element<QuarantineLegalDoc>> quarantineDocs,
                                                                         List<Element<QuarantineLegalDoc>> tabDocuments,
                                                                         UserDetails userDetails) {

        ManageDocuments manageDocument = element.getValue();
        boolean confidentialityFlag = false;

        //if DocumentParty is selected as COURT - move documents directly into case documents under internalCorrespondence category
        if (DocumentPartyEnum.COURT.equals(manageDocument.getDocumentParty())) {
            getQuarantineLegalDocAndAddToCaseDocuments(ManageDocumentsCategoryConstants.INTERNAL_CORRESPONDENCE,
                                                       manageDocument,
                                                       userDetails,
                                                       tabDocuments);

        } else if (restricted.test(element)) {

            // if restricted or confidential then add to quarantine docs list
            QuarantineLegalDoc quarantineLegalDoc = getQuarantineDocument(manageDocument, userRole);
            quarantineLegalDoc = DocumentUtils.addQuarantineFields(quarantineLegalDoc, manageDocument, userDetails);

            confidentialityFlag = true;
            quarantineDocs.add(element(quarantineLegalDoc));

        } else {
            //move documents to case documents if neither restricted nor confidential
            getQuarantineLegalDocAndAddToCaseDocuments(manageDocument.getDocumentCategories().getValueCode(),
                                                       manageDocument,
                                                       userDetails,
                                                       tabDocuments);

        }
        return confidentialityFlag;
    }

    private void getQuarantineLegalDocAndAddToCaseDocuments(String categoryId,
                                                           ManageDocuments manageDocument,
                                                           UserDetails userDetails,
                                                           List<Element<QuarantineLegalDoc>> tabDocuments) {
        QuarantineLegalDoc quarantineUploadDoc = DocumentUtils
            .getQuarantineUploadDocument(categoryId,
                                         manageDocument.getDocument().toBuilder().documentCreatedOn(localZoneDate).build(),
                                         objectMapper);
        quarantineUploadDoc = DocumentUtils.addQuarantineFields(quarantineUploadDoc, manageDocument, userDetails);

        tabDocuments.add(element(quarantineUploadDoc));
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
                    caseDataUpdated.put("courtStaffUploadDocListConfTab", quarantineDocs);//TO BE DELETED
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
                    caseData.getReviewDocuments().getCourtStaffUploadDocListConfTab()//TO BE DELETED
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

    private QuarantineLegalDoc getQuarantineDocument(ManageDocuments manageDocument, String userRole) {
        return QuarantineLegalDoc.builder()
            .document(SOLICITOR.equals(userRole) ? manageDocument.getDocument().toBuilder()
                .documentCreatedOn(localZoneDate).build() : null)
            .cafcassQuarantineDocument(CAFCASS.equals(userRole) ? manageDocument.getDocument().toBuilder()
                .documentCreatedOn(localZoneDate).build() : null)
            .courtStaffQuarantineDocument((COURT_STAFF.equals(userRole)) ? manageDocument.getDocument().toBuilder()
                .documentCreatedOn(localZoneDate).build() : null)
            .build();
    }
}
