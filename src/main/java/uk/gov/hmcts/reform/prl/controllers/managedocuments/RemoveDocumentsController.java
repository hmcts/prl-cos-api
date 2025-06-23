package uk.gov.hmcts.reform.prl.controllers.managedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.RemovableDocument;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.managedocuments.RemoveDocumentsService;

import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


@Slf4j
@RestController
@RequestMapping("/remove-documents")
@SecurityRequirement(name = "Bearer Authentication")
public class RemoveDocumentsController extends AbstractCallbackController {
    private final RemoveDocumentsService removeDocumentsService;
    private final UserService userService;

    @Autowired
    protected RemoveDocumentsController(ObjectMapper objectMapper, EventService eventPublisher,
                                        RemoveDocumentsService removeDocumentsService,
                                        UserService userService) {
        super(objectMapper, eventPublisher);
        this.removeDocumentsService = removeDocumentsService;
        this.userService = userService;
    }

    @PostMapping("/about-to-start")
    public CallbackResponse handleAboutToStart(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());

        caseData = removeDocumentsService.populateRemovalList(caseData);
        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }

    @PostMapping(path = "/confirm-removals", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public CallbackResponse confirmRemovals(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        CaseData old = getCaseData(callbackRequest.getCaseDetailsBefore());

        // add list of documents we've identified as being removed
        caseData = caseData.toBuilder()
            .documentsToBeRemoved(removeDocumentsService.docsBeingRemovedString(caseData, old))
            .build();

        return CallbackResponse.builder()
            .data(caseData).build();
    }

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public CallbackResponse aboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        CaseData old = getCaseData(callbackRequest.getCaseDetailsBefore());

        List<Element<RemovableDocument>> docsToRemove = removeDocumentsService.docsBeingRemoved(caseData, old);
        caseData = removeDocumentsService.removeDocuments(caseData, docsToRemove);
        // add list of documents we've identified as being removed
        return CallbackResponse.builder()
            .data(caseData).build();
    }

    @PostMapping(path = "/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public CallbackResponse submitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        CaseData old = getCaseData(callbackRequest.getCaseDetailsBefore());

        removeDocumentsService.deleteDocumentsInCdam(caseData, old);

        return CallbackResponse.builder().build();
    }

}
