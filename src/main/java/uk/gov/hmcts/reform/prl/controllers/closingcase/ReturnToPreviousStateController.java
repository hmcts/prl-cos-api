package uk.gov.hmcts.reform.prl.controllers.closingcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class ReturnToPreviousStateController extends AbstractCallbackController {
    private final AuthorisationService authorisationService;
    private final AllTabServiceImpl tabService;

    @Autowired
    public ReturnToPreviousStateController(ObjectMapper objectMapper, EventService eventPublisher,
                                           AuthorisationService authorisationService, AllTabServiceImpl tabService) {
        super(objectMapper, eventPublisher);
        this.authorisationService = authorisationService;
        this.tabService = tabService;
    }


    @PostMapping("/returnToPreviousState")
    public void handleSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        tabService.updateAllTabsIncludingConfTab(String.valueOf(callbackRequest.getCaseDetails().getId()));
    }
}
