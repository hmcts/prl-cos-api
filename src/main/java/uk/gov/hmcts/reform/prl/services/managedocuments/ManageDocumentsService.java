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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

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
                if (userRole.equals(SOLICITOR)) {
                    caseDataUpdated.put("manageDocumentsTriggeredBy", "SOLICITOR");
                } else if (userRole.equals(CAFCASS)) {
                    caseDataUpdated.put("manageDocumentsTriggeredBy", "CAFCASS");
                }
            } else {
                caseDataUpdated.put("manageDocumentsTriggeredBy", "NOTREQUIRED");
            }
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
                    QuarantineLegalDoc quarantineLegalDoc = getQuarantineDocument(manageDocument, userRole);
                    quarantineLegalDoc = DocumentUtils.addQuarantineFields(quarantineLegalDoc, manageDocument);

                    quarantineDocs.add(element(quarantineLegalDoc));
                } else {
                    final String categoryId = manageDocument.getDocumentCategories().getValueCode();
                    QuarantineLegalDoc quarantineUploadDoc = DocumentUtils
                        .getQuarantineUploadDocument(
                            categoryId,
                            manageDocument.getDocument().toBuilder()
                                .documentCreatedOn(new Date()).build()
                        );
                    quarantineUploadDoc = DocumentUtils.addQuarantineFields(quarantineUploadDoc, manageDocument);

                    tabDocuments.add(element(quarantineUploadDoc));
                }
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

            case COURT_STAFF:
                if (isDocumentTab) {
                    return !isEmpty(caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab())
                        ? caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab() : new ArrayList<>();
                } else {
                    return !isEmpty(caseData.getCourtStaffQuarantineDocsList())
                        ? caseData.getCourtStaffQuarantineDocsList() : new ArrayList<>();
                }

            default:
                throw new IllegalStateException(UNEXPECTED_USER_ROLE + userRole);
        }
    }

    private QuarantineLegalDoc getQuarantineDocument(ManageDocuments manageDocument, String userRole) {
        return QuarantineLegalDoc.builder()
            .document(SOLICITOR.equals(userRole) || COURT_STAFF.equals(userRole)
                          ? manageDocument.getDocument().toBuilder().documentCreatedOn(new Date()).build()
                          : null)
            .cafcassQuarantineDocument(CAFCASS.equals(userRole)
                                           ? manageDocument.getDocument().toBuilder().documentCreatedOn(new Date()).build()
                                           : null)
            .courtStaffQuarantineDocument(COURT_STAFF.equals(userRole)
                                              ? manageDocument.getDocument().toBuilder().documentCreatedOn(new Date()).build()
                                              : null)
            .build();
    }
}
