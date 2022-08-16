package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DraftOrderController {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${document.templates.common.prl_draft_an_order_template}")
    protected String draftAnOrder;

    @Value("${document.templates.common.prl_draft_an_order_filename}")
    protected String draftAnOrderFile;

    @Autowired
    private final UserService userService;

    @Autowired
    private final DocumentLanguageService documentLanguageService;

    @Autowired
    private DgsService documentGenService;

    @PostMapping(path = "/draft-order/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public AboutToStartOrSubmitCallbackResponse draftOrder(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        GeneratedDocumentInfo generatedDocumentInfo = documentGenService.generateDocument(authorisation,caseDetails,draftAnOrder);
        Document document = Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName(draftAnOrderFile).build();
        caseDataUpdated.put("previewDraftAnOrderDocument",document);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
