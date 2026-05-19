package uk.gov.hmcts.reform.prl.services.renamedocument;

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
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.sendandreply.SendAndReplyService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CHILD_IMPACT_REPORT_1_LA;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CHILD_IMPACT_REPORT_2_LA;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CIR_EXTENSION_REQUEST_LA;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CIR_TRANSFER_REQUEST_LA;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.LOCAL_AUTHORITY_INVOLVEMENT_LA;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SECTION_47_LA;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SECTION_7_ADDENDUM_REPORT_LA;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SECTION_7_REPORT_LA;
import static uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc.quarantineCategoriesToRemove;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RenameDocumentService {

    private final ObjectMapper objectMapper;
    private final SendAndReplyService sendAndReplyService;
    private final UserService userService;
    private final RoleAssignmentService roleAssignmentService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    private static final List<String> EXCLUDED_LA_DOCS_LIST_FOR_ADMIN = List.of(
        CHILD_IMPACT_REPORT_1_LA,
        CHILD_IMPACT_REPORT_2_LA,
        SECTION_7_REPORT_LA,
        SECTION_7_ADDENDUM_REPORT_LA,
        LOCAL_AUTHORITY_INVOLVEMENT_LA,
        SECTION_47_LA,
        CIR_EXTENSION_REQUEST_LA,
        CIR_TRANSFER_REQUEST_LA
    );

    public Map<String, Object> handleAboutToStart(String authorisation,
                                                  CallbackRequest callbackRequest) {
        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        DynamicList documentsList = sendAndReplyService.getCategoriesAndDocuments(authorisation, String.valueOf(caseData.getId()));
        caseDataMap.put("renameDocumentsList", List.of(element(documentsList)));

        UserDetails userDetails = userService.getUserDetails(authorisation);
        boolean isUserRoleLA = isUserAllocatedRoleForCaseLA(String.valueOf(caseData.getId()), userDetails.getId());

        DynamicList categoriesAndDocumentsList = getCategoriesSubcategories(authorisation, String.valueOf(caseData.getId()), isUserRoleLA);
        caseDataMap.put("categoryDocumentsList", List.of(element(categoriesAndDocumentsList)));

        return caseDataMap;
    }

    public Map<String, Object> handleAboutToSubmit(String authorisation, CallbackRequest callbackRequest) {
        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        return caseDataMap;
    }

    private DynamicList getCategoriesSubcategories(String authorisation, String caseReference, boolean isUserRoleLA) {
        try {
            CategoriesAndDocuments categoriesAndDocuments = coreCaseDataApi.getCategoriesAndDocuments(
                authorisation,
                authTokenGenerator.generate(),
                caseReference
            );
            if (null != categoriesAndDocuments) {
                List<Category> parentCategories = nullSafeCollection(categoriesAndDocuments.getCategories())
                    .stream()
                    .filter(category -> !isUserRoleLA || category.getCategoryId().equals("localAuthorityDocuments"))
                    .sorted(Comparator.comparing(Category::getCategoryName))
                    .toList();

                List<String> docsToExclude = new ArrayList<>(List.of(quarantineCategoriesToRemove()));
                if (!isUserRoleLA) {
                    docsToExclude.addAll(EXCLUDED_LA_DOCS_LIST_FOR_ADMIN);
                }

                List<DynamicListElement> dynamicListElementList = new ArrayList<>();
                CaseUtils.createCategorySubCategoryDynamicList(
                    parentCategories,
                    dynamicListElementList,
                    docsToExclude
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

    private boolean isUserAllocatedRoleForCaseLA(String caseId, String idamId) {
        return roleAssignmentService.isUserAllocatedRoleForCase(caseId, idamId, Roles.LOCAL_AUTHORITY_STAFF.getValue())
            || roleAssignmentService.isUserAllocatedRoleForCase(caseId, idamId, Roles.LOCAL_AUTHORITY_SOLICITOR.getValue());
    }

}
