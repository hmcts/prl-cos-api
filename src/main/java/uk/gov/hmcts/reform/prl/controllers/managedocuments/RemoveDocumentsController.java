package uk.gov.hmcts.reform.prl.controllers.managedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.RemovableDocument;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.managedocuments.RemoveDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@RequestMapping("/remove-documents")
@SecurityRequirement(name = "Bearer Authentication")
public class RemoveDocumentsController extends AbstractCallbackController {

    private final RemoveDocumentsService removeDocumentsService;
    private final AuthorisationService authorisationService;
    private final UserService userService;
    private final AllTabServiceImpl tabService;

    @Autowired
    protected RemoveDocumentsController(ObjectMapper objectMapper,
                                        EventService eventPublisher,
                                        RemoveDocumentsService removeDocumentsService,
                                        UserService userService,
                                        AuthorisationService authorisationService,
                                        AllTabServiceImpl tabService) {
        super(objectMapper, eventPublisher);
        this.removeDocumentsService = removeDocumentsService;
        this.userService = userService;
        this.authorisationService = authorisationService;
        this.tabService = tabService;
    }

    @PostMapping("/about-to-start")
    public CallbackResponse handleAboutToStart(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        if (!authorisationService.isAuthorized(authorisation, s2sToken)) {
            throw new RuntimeException(INVALID_CLIENT);
        }
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        caseData = removeDocumentsService.populateRemovalList(caseData);
        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }

    @PostMapping(path = "/confirm-removals",
        consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public CallbackResponse confirmRemovals(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        if (!authorisationService.isAuthorized(authorisation, s2sToken)) {
            throw new RuntimeException(INVALID_CLIENT);
        }
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        CaseData old = getCaseData(callbackRequest.getCaseDetailsBefore());

        // add list of documents we've identified as being removed
        caseData = caseData.toBuilder()
            .documentsToBeRemoved(
                removeDocumentsService.getConfirmationTextForDocsBeingRemoved(caseData, old)
            )
            .build();

        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }

    @PostMapping(path = "/about-to-submit",
        consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public AboutToStartOrSubmitCallbackResponse aboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        if (!authorisationService.isAuthorized(authorisation, s2sToken)) {
            throw new RuntimeException(INVALID_CLIENT);
        }
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        CaseData old = getCaseData(callbackRequest.getCaseDetailsBefore());

        List<Element<RemovableDocument>> docsToRemove =
            removeDocumentsService.getDocsBeingRemoved(caseData, old);
        Map<String, Object> updatedCaseData =
            removeDocumentsService.removeDocuments(caseData, docsToRemove);
        Map<String, Object> allData = new HashMap<>(callbackRequest.getCaseDetails().getData());
        allData.putAll(updatedCaseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(allData)
            .build();
    }

    @PostMapping(path = "/submitted", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public CallbackResponse submitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        if (!authorisationService.isAuthorized(authorisation, s2sToken)) {
            throw new RuntimeException(INVALID_CLIENT);
        }

        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        CaseData old = getCaseData(callbackRequest.getCaseDetailsBefore());

        removeDocumentsService.deleteDocumentsInCdam(caseData, old);

        return CallbackResponse.builder().build();
    }
}
