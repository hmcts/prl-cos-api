package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequiredArgsConstructor
public class ManageOrdersController {

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(path = "/populate-preview-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to show preview order in next screen for upload order")
    public CallbackResponse populatePreviewOrderWhenOrderUploaded(
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData1 = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        CaseData caseData = objectMapper.convertValue(
            CaseData.builder()
                .previewOrderDoc(caseData1.getAppointmentOfGuardian())
                .build(),
            CaseData.class
        );
        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }

    @PostMapping(path = "/fetch-child-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to fetch child details ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Child details are fetched"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public CallbackResponse fetchChildDetails(
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<Child> children = caseData.getChildren().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < children.size(); i++) {
            Child child = children.get(i);
            builder.append(String.format("Child %d: %s", i + 1, child.getFirstName() + child.getLastName()));
            builder.append("\n");
        }
        CaseData caseDataInput = CaseData.builder().childrenList(builder.toString()).build();
        return CallbackResponse.builder()
            .data(caseDataInput)
            .build();
    }
}
