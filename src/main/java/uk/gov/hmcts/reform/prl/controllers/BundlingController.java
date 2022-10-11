package uk.gov.hmcts.reform.prl.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/bundle")
public class BundlingController extends AbstractCallbackController {

    @PostMapping(path = "/createBundle", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Creating bundle. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bundle Created Successfully ."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse createBundle(@RequestHeader("authorization") @Parameter(hidden = true)
                                                             String authorization,
                                                             @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("caseBundles", "[{\"value\":{\"id\":\"e0aecb42-1c9e-4bde-844e-1bebf8fe5fa5\",\"title\":\"PRLBundle\","
            + "\"description\":null,\"eligibleForStitching\":\"no\",\"eligibleForCloning\":\"no\",\"stitchedDocument\":null,"
            + "\"documents\":[],\"folders\":[{\"value\":{\"name\":\"MainApplicationDocuments\",\"documents\":[{\"value\":{\"name\":\"ConsentOrder\",\"description\":null,\"sortIndex\":0,\"sourceDocument\":{\"document_url\":\"http://dm-store-aat.service.core-compute-aat.internal/documents/b6f5b557-8c31-4bfe-8b2c-8fa456e76b66\",\"document_filename\":\"SampleDoc1.pdf\",\"document_binary_url\":\"http://dm-store-aat.service.core-compute-aat.internal/documents/b6f5b557-8c31-4bfe-8b2c-8fa456e76b66/binary\",\"document_hash\":null}}},{\"value\":{\"name\":\"miamCertificate\",\"description\":null,\"sortIndex\":0,\"sourceDocument\":{\"document_url\":\"http://dm-store-aat.service.core-compute-aat.internal/documents/42b2d014-337e-447e-bd11-00b10e75091e\",\"document_filename\":\"SampleDoc2.pdf\",\"document_binary_url\":\"http://dm-store-aat.service.core-compute-aat.internal/documents/42b2d014-337e-447e-bd11-00b10e75091e/binary\",\"document_hash\":null}}},{\"value\":{\"name\":\"previousOrders\",\"description\":null,\"sortIndex\":0,\"sourceDocument\":{\"document_url\":\"http://dm-store-aat.service.core-compute-aat.internal/documents/91db0bb9-2a9c-4ce9-9ae6-49c08a9b3fb2\",\"document_filename\":\"Sampledoc3WithWord.docx\",\"document_binary_url\":\"http://dm-store-aat.service.core-compute-aat.internal/documents/91db0bb9-2a9c-4ce9-9ae6-49c08a9b3fb2/binary\",\"document_hash\":null}}}],\"sortIndex\":0}},{\"value\":{\"name\":\"OtherDocuments\",\"documents\":[{\"value\":{\"name\":\"Applicationdocu\",\"description\":null,\"sortIndex\":0,\"sourceDocument\":{\"document_url\":\"http://dm-store-aat.service.core-compute-aat.internal/documents/08b3fcd9-4ead-4eeb-8f13-6d99095d9c07\",\"document_filename\":\"Sampledoc3WithWord.docx\",\"document_binary_url\":\"http://dm-store-aat.service.core-compute-aat.internal/documents/08b3fcd9-4ead-4eeb-8f13-6d99095d9c07/binary\",\"document_hash\":null}}}],\"sortIndex\":1}}],\"fileName\":\"-PRLBundle.pdf\",\"fileNameIdentifier\":\"/case_details/id\",\"coverpageTemplate\":\"\",\"hasTableOfContents\":\"Yes\",\"hasCoversheets\":\"No\",\"hasFolderCoversheets\":\"Yes\",\"stitchStatus\":\"NEW\",\"paginationStyle\":null,\"pageNumberFormat\":\"numberOfPages\",\"stitchingFailureMessage\":null}}]");
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();

    }
}
