package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;

import java.util.List;

@FeignClient(name = "judicial-ref-data-api", url = "${judicialUsers.api.url}",configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface JudicialUserDetailsApi {
    @PostMapping(value = "/refdata/judicial/users", consumes = "application/json")
    List<JudicialUsersApiResponse> getAllJudicialUserDetails(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestBody JudicialUsersApiRequest judicialUsersApiRequest
    );
}
