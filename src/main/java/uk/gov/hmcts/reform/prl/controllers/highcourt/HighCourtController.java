package uk.gov.hmcts.reform.prl.controllers.highcourt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.highcourt.HighCourtService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@RequestMapping("/high-court-case")
public class HighCourtController  extends AbstractCallbackController {

    private final AuthorisationService authorisationService;
    private final HighCourtService highCourtService;

    @Autowired
    public HighCourtController(ObjectMapper objectMapper, EventService eventPublisher,
                               AuthorisationService authorisationService, HighCourtService highCourtService) {
        super(objectMapper, eventPublisher);
        this.authorisationService = authorisationService;
        this.highCourtService = highCourtService;
    }

    @PostMapping(path = "/submittedHighCourt", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Submitted request for setting high court value")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Inside /submittedHighCourt for case {}", callbackRequest.getCaseDetails().getId());
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            final CaseDetails caseDetails = callbackRequest.getCaseDetails();
            Object highCourtCase = caseDetails.getData().get("isHighCourtCase");
            if (highCourtCase != null) {
                highCourtService.setCaseAccess(caseDetails);
            }
            return ok(SubmittedCallbackResponse.builder().build());
        }  else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

}
