package uk.gov.hmcts.reform.prl.controllers.managedocuments;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/manage-documents")
@SecurityRequirement(name = "Bearer Authentication")
public class ManageDocumentsController extends AbstractCallbackController {

    @Autowired
    private ManageDocumentsService manageDocumentsService;

    @PostMapping("/about-to-start")
    public CallbackResponse handleAboutToStart(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {

        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        //PRL-3562 - populate document categories
        caseData = manageDocumentsService.populateDocumentCategories(authorisation, caseData);

        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }
}
