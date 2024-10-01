package uk.gov.hmcts.reform.prl.clients.ccd;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.extendedcasedetails.ExtendedCaseDetails;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;


@FeignClient(
    name = "core-case-data-api",
    primary = false,
    url = "${core_case_data.api.url}"
)
public interface ExtendedCaseDataApi {
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String EXPERIMENTAL = "experimental=true";

    @GetMapping(
        path = "/cases/{cid}",
        headers = EXPERIMENTAL
    )
    ExtendedCaseDetails getExtendedCaseDetails(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("cid") String caseId
    );
}
