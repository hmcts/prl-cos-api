package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.services.ExampleService;
import uk.gov.hmcts.reform.prl.workflows.ApplicationConsiderationTimetableValidationWorkflow;
import uk.gov.hmcts.reform.prl.workflows.ValidateMiamApplicationOrExemptionWorkflow;

import java.util.ArrayList;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class CallbackController {

    private final ApplicationConsiderationTimetableValidationWorkflow applicationConsiderationTimetableValidationWorkflow;
    private final ExampleService exampleService;
    private final ValidateMiamApplicationOrExemptionWorkflow validateMiamApplicationOrExemptionWorkflow;
    private final ObjectMapper objectMapper;


    /**
     * It's just an example - to be removed when there are real tasks sending emails.
     */
    @PostMapping(path = "/send-email", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to send email")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CallbackResponse> sendEmail(
        @RequestBody @ApiParam("CaseData") CallbackRequest request
    ) throws WorkflowException {
        return ok(
            CallbackResponse.builder()
                .data(exampleService.executeExampleWorkflow(request.getCaseDetails()))
                .build()
        );
    }

    @PostMapping(path = "/validate-application-consideration-timetable", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to validate application consideration timetable. Returns error messages if validation fails.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<uk.gov.hmcts.reform.ccd.client.model.CallbackResponse> validateApplicationConsiderationTimetable(
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws WorkflowException {
        WorkflowResult workflowResult = applicationConsiderationTimetableValidationWorkflow.run(callbackRequest);

        return ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .errors(workflowResult.getErrors())
                .build()
        );
    }

    @PostMapping(path = "/validate-miam-application-or-exemption", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to confirm that a MIAM has been attended or applicant is exempt. Returns error message if confirmation fails")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<uk.gov.hmcts.reform.ccd.client.model.CallbackResponse> validateMiamApplicationOrExemption(
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws WorkflowException {
        WorkflowResult workflowResult = validateMiamApplicationOrExemptionWorkflow.run(callbackRequest);

        return ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .errors(workflowResult.getErrors())
                .build()

        );
    }

    @PostMapping(path = "/behaviour-collection", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Expands collection of behaviours to display field on page loading")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<uk.gov.hmcts.reform.ccd.client.model.CallbackResponse> expandBehaviourCollection(
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws WorkflowException {

        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        caseData.put("behaviours", new ArrayList<>());
        return ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData).build()

        );
    }

    @PostMapping(path = "/test", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to confirm that a MIAM has been attended or applicant is exempt. Returns error message if confirmation fails")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<uk.gov.hmcts.reform.ccd.client.model.CallbackResponse> testDataModel(
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws WorkflowException, JsonProcessingException {

        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);

        String jsonCaseData = objectMapper.writeValueAsString(caseData);

        System.out.println(jsonCaseData);

        return ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .build()
        );
    }



}
