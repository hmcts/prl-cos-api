package uk.gov.hmcts.reform.prl.services.taskmanagement;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.wa.CompletableTaskResponse;
import uk.gov.hmcts.reform.prl.models.wa.SearchEventAndCaseRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(
    name = "wa-task-management-api-client",
    url = "${wa-task-management.api.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class
)
public interface TaskManagementClient {

    @PostMapping(
        value = "/task/search-for-completable",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    CompletableTaskResponse searchForCompletable(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody final SearchEventAndCaseRequest searchEventAndCaseRequest
    );
}
