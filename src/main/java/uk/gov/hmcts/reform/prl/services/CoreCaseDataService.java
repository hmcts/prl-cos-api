package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CoreCaseDataService {

    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final SystemUserService systemUserService;


    public void triggerEvent(String jurisdiction,
                             String caseType,
                             Long caseId,
                             String eventName,
                             Map<String, Object> eventData) {

        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            jurisdiction,
            caseType,
            caseId.toString(),
            eventName
        );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(eventData)
            .build();

        coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            jurisdiction,
            caseType,
            caseId.toString(),
            true,
            caseDataContent
        );
    }

    public DynamicList getCategoriesAndDocuments(String authorisation, String caseReference) {
        CategoriesAndDocuments categoriesAndDocuments = coreCaseDataApi.getCategoriesAndDocuments(
            authorisation,
            authTokenGenerator.generate(),
            caseReference
        );
        return createDynamicList(categoriesAndDocuments);

    }

    private DynamicList createDynamicList(CategoriesAndDocuments categoriesAndDocuments) {

        List<Category> parentCategories = categoriesAndDocuments.getCategories().stream()
            .sorted(Comparator.comparing(Category::getCategoryName))
            .collect(Collectors.toList());

        List<DynamicListElement> dynamicListElementList = new ArrayList<>();
        String parentString = null;
        dynamicListElementList = createDynamicListFromSubCategories(parentCategories, dynamicListElementList,
                                                                    parentString, null
        );
        System.out.println("Done");

        return DynamicList.builder().value(DynamicListElement.EMPTY)
            .listItems(dynamicListElementList).build();
    }

    private List<DynamicListElement> createDynamicListFromSubCategories(List<Category> categoryList,
                                                                        List<DynamicListElement> dynamicListElementList,
                                                                        final String parentLabelString,
                                                                        final String parentCodeString) {
        categoryList.stream().forEach(category -> {
            if (parentLabelString == null) {
                if (category.getDocuments() != null) {
                    category.getDocuments().stream().forEach(document -> {
                        dynamicListElementList.add(
                            DynamicListElement.builder().code(category.getCategoryId() + "___" + document.getDocumentURL())
                                .label(category.getCategoryName() + " --- " + document.getDocumentFilename()).build()
                        );
                    });
                }
                if (category.getSubCategories() != null) {
                    createDynamicListFromSubCategories(
                        category.getSubCategories(),
                        dynamicListElementList,
                        category.getCategoryName(),
                        category.getCategoryId()
                    );
                }
            } else {
                if (category.getDocuments() != null) {
                    category.getDocuments().stream().forEach(document -> {
                        dynamicListElementList.add(
                            DynamicListElement.builder()
                                .code(parentCodeString + " -> " + category.getCategoryId() + "___" + document.getDocumentURL())
                                .label(parentLabelString + " -> " + category.getCategoryName() + " --- "
                                           + document.getDocumentFilename()).build()
                        );
                    });
                }
                if (category.getSubCategories() != null) {
                    createDynamicListFromSubCategories(category.getSubCategories(), dynamicListElementList,
                                                       parentLabelString + " -> " + category.getCategoryName(),
                                                       parentCodeString + " -> " + category.getCategoryId()
                    );
                }
            }


        });
        return dynamicListElementList;
    }


    private List<DynamicListElement> getDisplayEntry(Category category) {
        List<String> key = null;
        String value = null;
        List<DynamicListElement> dynamicListElementList = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        category.getDocuments().stream().forEach(document -> {
            keys.add(category.getCategoryId() + "_" + document.getDocumentURL());
            dynamicListElementList.add(
                DynamicListElement.builder().code(category.getCategoryId() + "_" + document.getDocumentURL())
                    .label(category.getCategoryName() + "-" + document.getDocumentFilename()).build()
            );
        });
        // String key = category.getCategoryId() +"_"+category.getDocuments();
        return dynamicListElementList;
        // return DynamicListElement.builder().code(key).label(value).build();
    }
}
