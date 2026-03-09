package uk.gov.hmcts.reform.prl.controllers.documentremoval;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.exception.InvalidClientException;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.documentremoval.DocumentRemovalService;

import java.io.IOException;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@RestController
@RequestMapping("/document-removal")
@RequiredArgsConstructor
@Slf4j
public class DocumentRemovalController {

    private final AuthorisationService authorisationService;
    private final DocumentRemovalService documentRedactionService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse aboutToStart(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        if (!authorisationService.isAuthorized(authorisation, s2sToken)) {
            throw (new InvalidClientException(INVALID_CLIENT));
        }

        log.info("Document removal about to start callback received for case id: {}",
                 callbackRequest.getCaseDetails().getId());

        Map<String, Object> data = documentRedactionService.getDocumentsToKeepCollection(callbackRequest.getCaseDetails());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public AboutToStartOrSubmitCallbackResponse aboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws IOException {

        if (!authorisationService.isAuthorized(authorisation, s2sToken)) {
            throw (new InvalidClientException(INVALID_CLIENT));
        }

        log.info("Document removal about to submit callback received for case id: {}",
                 callbackRequest.getCaseDetails().getId());

        Map<String, Object> updatedCaseData = documentRedactionService.removeDocuments(callbackRequest.getCaseDetails());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData)
            .build();
    }
}
