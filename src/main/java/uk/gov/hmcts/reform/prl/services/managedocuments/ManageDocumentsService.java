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
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
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
    @Autowired
    private final CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private final AuthTokenGenerator authTokenGenerator;

    private final ObjectMapper objectMapper;

    @Autowired
    private final UserService userService;

    private final Time dateTime;

    public static final String MANAGE_DOCUMENTS_TRIGGERED_BY = "manageDocumentsTriggeredBy";
    public static final String DETAILS_ERROR_MESSAGE
        = "You must give a reason why the document should be restricted";
    private final Date localZoneDate = Date.from(ZonedDateTime.now(ZoneId.of(LONDON_TIME_ZONE)).toInstant());

    public CaseData populateDocumentCategories(String authorization, CaseData caseData) {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentCategories(getCategoriesSubcategories(authorization, String.valueOf(caseData.getId())))
            .documentRelatedToCaseLabel("Confirm the document is related to " + caseData.getApplicantCaseName())
            .build();

        return caseData.toBuilder()
            //.isC8DocumentPresent(CaseUtils.isC8Present(caseData))
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

    public List<String> precheckDocumentField(CallbackRequest callbackRequest) {

        List<String> errorList = new ArrayList<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        List<Element<ManageDocuments>> manageDocuments = caseData.getManageDocuments();
        for (Element<ManageDocuments> element : manageDocuments) {
            boolean restricted = element.getValue().getIsRestricted().getDisplayedValue().equals(YesOrNo.Yes);
            boolean restricted1 = element.getValue().getIsRestricted().equals(YesOrNo.Yes);
            boolean restrictedReasonEmpty = (element.getValue().getRestrictedDetails() == null
                || element.getValue().getRestrictedDetails().isEmpty()) ? true : false;
            log.info("restricted", restricted);
            log.info("restricted1", restricted1);
            log.info("restrictedReasonEmpty", restrictedReasonEmpty);
            if (restricted && restrictedReasonEmpty) {
                errorList.add(DETAILS_ERROR_MESSAGE);
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
                if (addToQuarantineDocsOrTabDocumentsAndReturnConfidFlag(
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
                                                                         List<Element<QuarantineLegalDoc>> tabDocuments,
                                                                         UserDetails userDetails) {

        ManageDocuments manageDocument = element.getValue();
        boolean confidentialityFlag = false;
        // if restricted or confidential then add to quarantine docs list
        if (restricted.test(element)) {
            QuarantineLegalDoc quarantineLegalDoc = getQuarantineDocument(manageDocument, userRole);
            quarantineLegalDoc = DocumentUtils.addQuarantineFields(quarantineLegalDoc, manageDocument, userDetails);
            confidentialityFlag = true;
            quarantineDocs.add(element(quarantineLegalDoc));
        } else {
            final String categoryId = manageDocument.getDocumentCategories().getValueCode();
            QuarantineLegalDoc quarantineUploadDoc = DocumentUtils
                .getQuarantineUploadDocument(categoryId,
                                             manageDocument.getDocument().toBuilder()
                                                 .documentCreatedOn(localZoneDate).build()
                );
            quarantineUploadDoc = DocumentUtils.addQuarantineFields(quarantineUploadDoc, manageDocument, userDetails);

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
}
