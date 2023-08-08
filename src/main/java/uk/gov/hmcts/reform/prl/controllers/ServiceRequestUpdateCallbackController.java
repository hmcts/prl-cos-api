package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.RequestUpdateCallbackService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabsService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ServiceRequestUpdateCallbackController extends AbstractCallbackController {

    private final RequestUpdateCallbackService requestUpdateCallbackService;

    @Autowired
    @Qualifier("allTabsService")
    AllTabsService tabService;

    @Autowired
    private final AuthorisationService authorisationService;

    @PutMapping(path = "/service-request-update", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Ways to pay will call this API and send the status of payment with other details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public void serviceRequestUpdate(
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody ServiceRequestUpdateDto serviceRequestUpdateDto
    ) throws WorkflowException {
        if (Boolean.TRUE.equals(authorisationService.authoriseService(s2sToken))){
            try {
                requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);
            } catch (Exception ex) {
                log.error(
                    "Payment callback is unsuccessful for the CaseID: {}",
                    serviceRequestUpdateDto.getCcdCaseNumber()
                );
                throw new WorkflowException(ex.getMessage(), ex);
            }
        }else {
            throw (new RuntimeException(INVALID_CLIENT));
        }

        }
    }

