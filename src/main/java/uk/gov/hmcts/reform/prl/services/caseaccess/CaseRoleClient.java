package uk.gov.hmcts.reform.prl.services.caseaccess;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesRequest;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesResponse;
import uk.gov.hmcts.reform.prl.models.caseaccess.RemoveUserRolesRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "case-role-client", url = "${core_case_data.api.url}")
public interface CaseRoleClient {
    static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @DeleteMapping(
        value = "/case-users",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void removeCaseRoles(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody final RemoveUserRolesRequest request
    );

    @PostMapping(
        value = "/case-users/search",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    FindUserCaseRolesResponse findUserCaseRoles(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody final FindUserCaseRolesRequest request
    );
}
