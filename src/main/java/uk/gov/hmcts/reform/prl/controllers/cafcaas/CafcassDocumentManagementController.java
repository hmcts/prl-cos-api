package uk.gov.hmcts.reform.prl.controllers.cafcaas;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassDocumentManagementService;

import java.util.UUID;

@RestController
@RequestMapping("/cases/documents/{documentId}/binary")
public class CafcassDocumentManagementController {
    @Autowired
    CafcassDocumentManagementService cafcassDocumentManagementService;

    @RequestMapping(
            value = "/download",
            method = RequestMethod.GET,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(description = "Call CDAM to download document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uploaded Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request while downloading the document"),
            @ApiResponse(responseCode = "401", description = "Provided Authorization token is missing or invalid"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> downloadDocument(@RequestHeader("authorisation") String authorisation,
                                              @RequestHeader("serviceAuthorisation") String serviceAuthorisation,
                                              @PathVariable UUID documentId) {

        return ResponseEntity.ok(cafcassDocumentManagementService.downloadDocument(authorisation, serviceAuthorisation, documentId));
    }
}
