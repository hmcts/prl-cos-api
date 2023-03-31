package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.services.AddCafcassOfficerService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AddCafcassOfficerController {

    private final AddCafcassOfficerService addCafcassOfficerService;

    @PostMapping(path = "/add-cafcass-officer/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to add Cafcass officer details")
    public AboutToStartOrSubmitCallbackResponse updateChildDetailsWithCafcassOfficer(@RequestBody CallbackRequest callbackRequest) {
        return AboutToStartOrSubmitCallbackResponse.builder().data(addCafcassOfficerService.populateCafcassOfficerDetails(
            callbackRequest)).build();
    }

}
