package uk.gov.hmcts.reform.prl.controllers.cafcaas;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassCdamService;

import java.util.UUID;

@RestController
@RequestMapping("/cases")
public class CafcassDocumentManagementController {
    @Autowired
    CafcassCdamService cafcassCdamService;

    @GetMapping(path = "/documents/{documentId}/binary")
    @Operation(description = "Call CDAM to download document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Downloaded Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request while downloading the document"),
            @ApiResponse(responseCode = "401", description = "Provided Authorization token is missing or invalid"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Resource> downloadDocument(@RequestHeader("authorisation") String authorisation,
                                                     @RequestHeader("serviceAuthorisation") String serviceAuthorisation,
                                                     @PathVariable UUID documentId) {

        return cafcassCdamService.getDocument(authorisation, serviceAuthorisation, documentId);
    }
}
