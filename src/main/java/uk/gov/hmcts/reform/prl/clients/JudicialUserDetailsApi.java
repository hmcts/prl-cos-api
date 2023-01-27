package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersAPIRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersAPIResponse;

@FeignClient(name = "judicial-ref-data-api", url = "${judicialUsers.api.url}",configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface JudicialUserDetailsApi {
    @PostMapping(value = "/refdata/judicial/users", consumes = "application/json")
    JudicialUsersAPIResponse getAllJudicialUserDetails(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestBody JudicialUsersAPIRequest judicialUsersApiRequest
    );
}
