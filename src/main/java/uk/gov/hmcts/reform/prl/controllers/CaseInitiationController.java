package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.caseinitiation.CaseInitiationService;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Tag(name = "case-initiation-controller")
@RestController
@RequestMapping("/case-initiation")
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class CaseInitiationController extends AbstractCallbackController {
    private final CaseInitiationService caseInitiationService;
    private final AuthorisationService authorisationService;

    @Autowired
    public CaseInitiationController(ObjectMapper objectMapper,
                                    EventService eventPublisher,
                                    CaseInitiationService caseInitiationService,
                                    AuthorisationService authorisationService) {
        super(objectMapper, eventPublisher);
        this.caseInitiationService = caseInitiationService;
        this.authorisationService = authorisationService;
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
                                @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
                                @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            caseInitiationService.handleCaseInitiation(
                authorisation,
                callbackRequest
            );
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
