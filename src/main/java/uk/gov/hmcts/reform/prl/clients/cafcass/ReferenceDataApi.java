package uk.gov.hmcts.reform.prl.clients.cafcass;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.refdata.Categories;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(
        name = "ref-data",
        url = "${refdata.api.url}",
        configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface ReferenceDataApi {
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String REF_DATA_CATEGORY_ENDPOINT = "/refdata/commondata/lov/categories";

    @GetMapping(
            value = REF_DATA_CATEGORY_ENDPOINT + "/{categoryId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    Categories retrieveListOfValuesByCategoryId(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @PathVariable("categoryId") String categoryId,
            @RequestParam(name = "serviceId") String serviceId);
}
