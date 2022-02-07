package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.RejectReasonEnum;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReturnApplicationReturnMessageController {

    @Autowired
    private UserService userService;
    private final ObjectMapper objectMapper;
    private final CaseWorkerEmailService caseWorkerEmailService;

    public boolean noRejectReasonSelected(CaseData caseData) {

        boolean noOptionSelected = true;

        boolean hasSelectedOption = allNonEmpty(caseData.getRejectReason());

        if (hasSelectedOption) {
            noOptionSelected = false;
        }

        return noOptionSelected;
    }

    public String getLegalFullName(CaseData caseData) {

        String legalName = "[Legal representative name]";

        Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());

        if (applicantsWrapped.isPresent() && applicantsWrapped.get().size() == 1) {
            List<PartyDetails> applicants = applicantsWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            String legalFirstName = applicants.get(0).getRepresentativeFirstName();
            String legalLastName = applicants.get(0).getRepresentativeLastName();

            legalName = legalFirstName + " " + legalLastName;

        } else {
            legalName = caseData.getSolicitorName();
        }
        return legalName;
    }

    @PostMapping(path = "/return-application-return-message", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to get return message of the return application ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback proceeded"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse returnApplicationReturnMessage(
        @RequestHeader("Authorization") String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws WorkflowException {

        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class)
            .toBuilder()
            .id(callbackRequest.getCaseDetails().getId())
            .build();

        UserDetails userDetails = userService.getUserDetails(authorisation);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        if (noRejectReasonSelected(caseData)) {
            log.info("There are no reject reason selected, therefore no return message is needed");
        } else {
            log.info("Preparing pre-filled text for return message");
            String caseName = caseData.getApplicantCaseName();
            String ccdId = String.valueOf(caseData.getId());


            String legalName = getLegalFullName(caseData);
            String caseWorkerName = userDetails.getFullName();

            List<RejectReasonEnum> listOfReasons = caseData.getRejectReason();

            StringBuilder returnMsgStr = new StringBuilder();

            returnMsgStr//.append("Subject line: Application returned: " + caseName + "\n")
                .append("Case name: " + caseName + "\n")
                .append("Reference code: " + ccdId + "\n\n")
                .append("Dear " + legalName + ",\n\n")
                .append("Thank you for your application."
                            + " Your application has been reviewed and is being returned for the following reasons:" + "\n\n");

            for (RejectReasonEnum reasonEnum : listOfReasons) {
                returnMsgStr.append(reasonEnum.getReturnMsgText());
            }

            returnMsgStr.append("Please resolve these issues and resubmit your application.\n\n")
                .append("Kind regards,\n")
                .append(caseWorkerName);

            caseDataUpdated.put("returnMessage", returnMsgStr.toString());
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/return-application-notification", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to send return application email notification")
    public AboutToStartOrSubmitCallbackResponse returnApplicationEmailNotification(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) throws Exception {

        caseWorkerEmailService.sendReturnApplicationEmailToSolicitor(callbackRequest.getCaseDetails());

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
