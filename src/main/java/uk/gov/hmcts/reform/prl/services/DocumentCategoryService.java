package uk.gov.hmcts.reform.prl.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILD_IMPACT_REPORT_1_LA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILD_IMPACT_REPORT_2_LA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CIR_EXTENSION_REQUEST_LA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CIR_TRANSFER_REQUEST_LA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LOCAL_AUTHORITY_INVOLVEMENT_LA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SECTION_47_LA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SECTION_7_ADDENDUM_REPORT_LA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SECTION_7_REPORT_LA;
import static uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc.quarantineCategoriesToRemove;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentCategoryService {

    private static final List<String> EXCLUDED_LA_DOCS_LIST_FOR_ADMIN = Arrays.asList(
        CHILD_IMPACT_REPORT_1_LA,
        CHILD_IMPACT_REPORT_2_LA,
        SECTION_7_REPORT_LA,
        SECTION_7_ADDENDUM_REPORT_LA,
        LOCAL_AUTHORITY_INVOLVEMENT_LA,
        SECTION_47_LA,
        CIR_EXTENSION_REQUEST_LA,
        CIR_TRANSFER_REQUEST_LA
    );

    private final UserService userService;
    private final RoleAssignmentService roleAssignmentService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    public DynamicList retrieveDocumentCategories(String authorization, CaseData caseData) {
        boolean isUserRoleLA = isUserAllocatedRoleForCaseLA(authorization, caseData);
        return getCategoriesSubcategories(
            authorization,
            String.valueOf(caseData.getId()),
            isUserRoleLA
        );
    }

    public boolean isUserAllocatedRoleForCaseLA(String authorization, CaseData caseData) {
        UserDetails userDetails = userService.getUserDetails(authorization);
        String caseId = String.valueOf(caseData.getId());
        String idamId = userDetails.getId();
        return isUserAllocatedRoleForCaseLA(caseId, idamId);
    }



    public boolean isUserAllocatedRoleForCaseLA(String caseId, String idamId) {
        return roleAssignmentService.isUserAllocatedRoleForCase(caseId, idamId, Roles.LOCAL_AUTHORITY_STAFF.getValue())
            || roleAssignmentService.isUserAllocatedRoleForCase(caseId, idamId, Roles.LOCAL_AUTHORITY_SOLICITOR.getValue());
    }



    public DynamicList getCategoriesSubcategories(String authorisation, String caseReference, boolean isUserRoleLA) {
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
                docsToExclude.clear();

                return DynamicList.builder().value(DynamicListElement.EMPTY)
                    .listItems(dynamicListElementList).build();
            }
        } catch (Exception e) {
            log.error("Error in getCategoriesAndDocuments method", e);
        }
        return DynamicList.builder()
            .value(DynamicListElement.EMPTY).build();
    }


}
