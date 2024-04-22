package uk.gov.hmcts.reform.prl.util;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;

@FeignClient(name = "case-orchestration-api", url = "${case.orchestration.service.base.uri}")
public interface CosApiClient {

    @Operation(description = "Root endpoint returning welcome text")
    @GetMapping("/")
    String welcome();

    @Operation(description = "Retrieve service's swagger specs")
    @GetMapping("/v3/api-docs")
    byte[] apiDocs();

    @Operation(description = "Temporary endpoint for testing gov UK notifications integration")
    @PostMapping("/send-email")
    void sendEmail(CallbackRequest callbackRequest);
}
