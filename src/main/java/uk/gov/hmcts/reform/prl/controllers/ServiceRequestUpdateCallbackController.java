package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.RequestUpdateCallbackService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabsService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
public class ServiceRequestUpdateCallbackController extends AbstractCallbackController {
    private static final String BEARER = "Bearer ";
    private final RequestUpdateCallbackService requestUpdateCallbackService;
    @Qualifier("allTabsService")
    private final AllTabsService tabService;
    private final AuthorisationService authorisationService;
    private final LaunchDarklyClient launchDarklyClient;

    @Autowired
    protected ServiceRequestUpdateCallbackController(ObjectMapper objectMapper,
                                                     EventService eventPublisher,
                                                     RequestUpdateCallbackService requestUpdateCallbackService,
                                                     AllTabsService tabService,
                                                     AuthorisationService authorisationService,
                                                     LaunchDarklyClient launchDarklyClient) {
        super(objectMapper, eventPublisher);
        this.requestUpdateCallbackService = requestUpdateCallbackService;
        this.tabService = tabService;
        this.authorisationService = authorisationService;
        this.launchDarklyClient = launchDarklyClient;

    }

    @PutMapping(path = "/service-request-update", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Ways to pay will call this API and send the status of payment with other details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public void serviceRequestUpdate(
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String serviceAuthToken,
        @RequestBody ServiceRequestUpdateDto serviceRequestUpdateDto
    ) throws WorkflowException {
        try {
            if (launchDarklyClient.isFeatureEnabled("payment-app-s2sToken")) {
                serviceAuthToken = serviceAuthToken.startsWith(BEARER) ? serviceAuthToken : BEARER.concat(
                    serviceAuthToken);
                if (Boolean.FALSE.equals(authorisationService.authoriseService(serviceAuthToken))) {
                    log.info("s2s token from payment service validation is unsuccessful");
                    throw (new RuntimeException(INVALID_CLIENT));
                }
            }
            requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);
        } catch (Exception ex) {
            log.error(
                "Payment callback is unsuccessful for the CaseID: {}",
                serviceRequestUpdateDto.getCcdCaseNumber()
            );
            throw new WorkflowException(ex.getMessage(), ex);
        }
    }
}

