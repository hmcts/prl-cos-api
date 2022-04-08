package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

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
}
