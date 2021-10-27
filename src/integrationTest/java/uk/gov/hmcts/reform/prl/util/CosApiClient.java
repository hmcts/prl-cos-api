package uk.gov.hmcts.reform.prl.util;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;

@FeignClient(name = "case-orchestration-api", url = "${case.orchestration.service.base.uri}")
public interface CosApiClient {

    @ApiOperation("Root endpoint returning welcome text")
    @GetMapping("/")
    String welcome();

    @ApiOperation("Retrieve service's swagger specs")
    @GetMapping("/v2/api-docs")
    byte[] apiDocs();

    @ApiOperation("Temporary endpoint for testing gov UK notifications integration")
    @PostMapping("/send-email")
    void sendEmail(CallbackRequest callbackRequest);
}
