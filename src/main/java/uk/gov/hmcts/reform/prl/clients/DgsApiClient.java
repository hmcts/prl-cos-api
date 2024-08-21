package uk.gov.hmcts.reform.prl.clients;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.dto.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;

@FeignClient(
    name = "prl-dgs-api",
    url = "${prl-dgs-api.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class
)
public interface DgsApiClient {
    @PostMapping(value = "/version/1/generatePDF", consumes = "application/json")
    GeneratedDocumentInfo generateDocument(
        @RequestHeader("Authorization") String authorization,
        @RequestBody GenerateDocumentRequest documentRequest
    );

    @PostMapping(value = "/version/1/convertDocToPdf/{fileName}", consumes = "application/json")
    GeneratedDocumentInfo convertDocToPdf(
        @PathVariable("fileName") String fileName,
        @RequestHeader("Authorization") String authorization,
        @RequestBody GenerateDocumentRequest documentRequest
    );

    @PostMapping(value = "/version/1/downloadDocument/{documentBinaryUrl}", consumes = "application/json")
    ResponseEntity<byte[]> downloadDocument(
        @PathVariable("documentBinaryUrl") String documentBinaryUrl,
        @RequestHeader("Authorization") String authorization
    );
}
