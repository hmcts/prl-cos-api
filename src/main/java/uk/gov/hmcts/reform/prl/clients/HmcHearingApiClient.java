package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingResponse;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Direct HMC hearing Feign client, ported from fis-hmc-api's HearingApiClient.
 * Replaces the previous indirection through fis-hmc-api's /hearings endpoints by
 * calling the HMC hearing component directly.
 */
@FeignClient(
    name = "hmc-hearing-feign-api",
    url = "${hearing_component.api.feign-url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class
)
public interface HmcHearingApiClient {

    String AUTHORIZATION = "Authorization";
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String ROLE_ASSIGNMENT_URL = "Role-Assignment-Url";
    String DATA_STORE_URL = "Data-Store-Url";
    String HMCTS_DEPLOYMENT_ID = "hmctsDeploymentId";

    @GetMapping(path = "/hearings/{caseReference}")
    Hearings getHearingDetails(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @PathVariable("caseReference") String caseReference
    );

    @GetMapping(path = "/hearings")
    List<Hearings> getListOfHearingDetails(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @RequestParam("ccdCaseRefs") List<String> ccdCaseRefs,
        @RequestParam("caseTypeId") String caseTypeId
    );

    @PostMapping(path = "/hearing")
    @Retryable({RuntimeException.class, TimeoutException.class})
    ResponseEntity<AutomatedHearingResponse> createHearingDetails(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @RequestBody AutomatedHearingCaseData hearingRequest
    );
}

