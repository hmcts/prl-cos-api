package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.CaseFlag;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;

@FeignClient(name = "common-ref-data-api", url = "${commonData.api.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface CommonDataRefApi {

    @GetMapping(value = "refdata/commondata/lov/categories/{categoryId}", consumes = "application/json")
    CommonDataResponse getAllCategoryValuesByCategoryId(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @PathVariable("categoryId") String categoryId,
        @RequestParam("serviceId") String serviceId,
        @RequestParam("isChildRequired") String isChildRequired

    );


    @GetMapping(
        value = "refdata/commondata/caseflags/service-id={service-id}",
        consumes = "application/json"
    )
    CaseFlag    retrieveCaseFlagsByServiceId(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @PathVariable(value = "service-id") String serviceId,
        @RequestParam(value = "flag-type", required = false) String flagType
    );
}


