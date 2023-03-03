package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer.ChildAndCafcassOfficer;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AddCafcassOfficerService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AddCafcassOfficerController {

    private final ObjectMapper objectMapper;
    private final AddCafcassOfficerService addCafcassOfficerService;

    @PostMapping(path = "/add-cafcass-officer/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to add child name for Cafcass officer")
    public AboutToStartOrSubmitCallbackResponse prePopulateChildDetails(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = addCafcassOfficerService.prePopulateChildName(caseData);
        log.info("childAndCafcassOfficers ==> " + childAndCafcassOfficers);
        caseDataUpdated.put("childAndCafcassOfficers", childAndCafcassOfficers);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/add-cafcass-officer/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to add Cafcass officer details")
    public AboutToStartOrSubmitCallbackResponse updateChildDetailsWithCafcassOfficer(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = caseData.getChildAndCafcassOfficers();
        for (Element<ChildAndCafcassOfficer> cafcassOfficer : childAndCafcassOfficers) {
            addCafcassOfficerService.populateCafcassOfficerDetails(caseData, caseDataUpdated, cafcassOfficer);
        }
        caseDataUpdated.put("childAndCafcassOfficers", childAndCafcassOfficers);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


}
