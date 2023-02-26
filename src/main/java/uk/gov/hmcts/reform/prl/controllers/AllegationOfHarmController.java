package uk.gov.hmcts.reform.prl.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AllegationOfHarmController {

    private final ObjectMapper objectMapper;

    @PostMapping(path = "/pre-populate-child-data", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Copy manage docs for tabs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse prePopulateChildData(@RequestHeader(HttpHeaders.AUTHORIZATION)
                                                                     @Parameter(hidden = true) String authorisation,
                                                                     @RequestBody uk.gov.hmcts.reform
                                                                             .ccd.client.model.CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        caseData.getNewChildDetails().forEach(eachChild -> {
            listItems.add(DynamicMultiselectListElement.builder().code(eachChild.getId().toString())
                    .label(eachChild.getValue().getFirstName() + " " + eachChild.getValue().getLastName()).build());

        });

        caseDataUpdated.put("childPsychologicalAbuse", Map.of("whichChildrenAreRisk", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build()));
        caseDataUpdated.put("childPhysicalAbuse", Map.of("whichChildrenAreRisk", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build()));
        caseDataUpdated.put("childFinancialAbuse", Map.of("whichChildrenAreRisk", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build()));
        caseDataUpdated.put("childSexualAbuse", Map.of("whichChildrenAreRisk", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build()));
        caseDataUpdated.put("childEmotionalAbuse", Map.of("whichChildrenAreRisk", DynamicMultiSelectList.builder()
                .listItems(listItems)
                .build()));

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
