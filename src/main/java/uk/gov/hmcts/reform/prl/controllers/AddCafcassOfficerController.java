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
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer.ChildAndCafcassOfficer;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AddCafcassOfficerController {

    private final ObjectMapper objectMapper;

    @PostMapping(path = "/add-cafcass-officer/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Generate applicants")
    public AboutToStartOrSubmitCallbackResponse prePopulateChildDetails(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = null;
        if (caseData.getChildren() != null && !caseData.getChildren().isEmpty()) {
            childAndCafcassOfficers = new ArrayList<>();
            for (Element<Child> childElement : caseData.getChildren()) {
                ChildAndCafcassOfficer childAndCafcassOfficer = ChildAndCafcassOfficer.builder()
                    .childId(childElement.getId().toString())
                    .childName(childElement.getValue().getFirstName() + " " + childElement.getValue().getLastName()).build();
                childAndCafcassOfficers.add(element(childAndCafcassOfficer));
            }
        }
        caseDataUpdated.put("childAndCafcassOfficers", childAndCafcassOfficers);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/add-cafcass-officer/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Generate applicants")
    public AboutToStartOrSubmitCallbackResponse populateChildDetailsWithCafcassOfficer(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = caseData.getChildAndCafcassOfficers();

        for (Element<ChildAndCafcassOfficer> cafcassOfficer : childAndCafcassOfficers) {
            List<Element<Child>> childern = caseData.getChildren();
            childern.stream()
                .filter(child -> Objects.equals(child.getId(), cafcassOfficer.getValue().getChildId()))
                .findFirst()
                .ifPresent(child -> {
                    Child amendedChild = child.getValue().toBuilder()
                        .cafcassOfficerName(cafcassOfficer.getValue().getCafcassOfficerName())
                        .cafcassOfficerEmailAddress(cafcassOfficer.getValue().getCafcassOfficerEmailAddress())
                        .cafcassOfficerPhoneNo(cafcassOfficer.getValue().getCafcassOfficerPhoneNo())
                        .build();

                    childern.set(childern.indexOf(child), element(child.getId(), amendedChild));
                });
        }
        log.info("Children ------>" + caseData.getChildren());

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
