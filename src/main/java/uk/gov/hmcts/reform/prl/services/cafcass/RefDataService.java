package uk.gov.hmcts.reform.prl.services.cafcass;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.cafcass.ReferenceDataApi;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.refdata.Categories;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.refdata.Category;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RefDataService {

    private final ReferenceDataApi referenceDataApiefDataApi;

    public Map<String, String> getRefDataCategoryValueMap(
        String authorization, String serviceAuthorization, String serviceCode, String categoryId) {
        // Call hearing api to get hmc status value
        final Categories categoriesByCategoryId =
            referenceDataApiefDataApi.retrieveListOfValuesByCategoryId(
                authorization,
                serviceAuthorization,
                categoryId,
                serviceCode
            );

        return categoriesByCategoryId.getListOfCategory().stream()
            .collect(Collectors.toMap(Category::getKey, Category::getValueEn));
    }
}
