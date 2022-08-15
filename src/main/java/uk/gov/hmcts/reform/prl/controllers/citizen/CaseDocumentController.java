package uk.gov.hmcts.reform.prl.controllers.citizen;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
public class CaseDocumentController {

    @Autowired
    private DocumentGenService documentGenService;

    @PostMapping(path = "/generate-citizen-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Create citizen documents")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application Submitted."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public String generateCitizenDocument(@RequestHeader("Authorization")
                                                             String authorisation,
                                                             @RequestBody CaseData caseData) throws Exception {
        return documentGenService.generateCitizenDocuments(authorisation, caseData);
    }

}

