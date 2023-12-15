package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;

@FeignClient(name = "amRoleAssignment",
        url = "${amRoleAssignment.api.url}",
        configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface RoleAssignmentApi {
}
