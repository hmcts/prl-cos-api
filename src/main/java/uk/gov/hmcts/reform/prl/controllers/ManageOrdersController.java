package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;
import javax.ws.rs.core.HttpHeaders;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequiredArgsConstructor
public class ManageOrdersController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private final UserService userService;

    @Autowired
    private ManageOrderService manageOrderService;

    @Autowired
    private final DgsService dgsService;

    @Autowired
    private final DocumentLanguageService documentLanguageService;

    @Autowired
    private ManageOrderEmailService manageOrderEmailService;

    @Value("${document.templates.common.prl_c21_template}")
    protected String c21Template;

    @Value("${document.templates.common.prl_c21_filename}")
    protected String c21File;

    @Value("${document.templates.common.prl_c21_draft_template}")
    protected String c21TDraftTemplate;

    @Value("${document.templates.common.prl_c21_draft_filename}")
    protected String c21DraftFile;

    @Value("${document.templates.common.prl_c21_welsh_template}")
    protected String c21WelshTemplate;

    @Value("${document.templates.common.prl_c21_welsh_filename}")
    protected String c21WelshFile;

    @PostMapping(path = "/populate-preview-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to show preview order in next screen for upload order")
    public CallbackResponse populatePreviewOrderWhenOrderUploaded(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        CaseData caseData1 = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData1);
        CaseData caseData;
        if (caseData1.getCreateSelectOrderOptions() != null
            && PrlAppsConstants.C42_CREATE_ORDER.equalsIgnoreCase(
            caseData1.getCreateSelectOrderOptions().getDisplayedValue())) {
            caseData = getCaseData(authorisation, caseData1,c21DraftFile,c21TDraftTemplate);
        } else {
            caseData = objectMapper.convertValue(
                CaseData.builder()
                    .previewOrderDoc(caseData1.getAppointmentOfGuardian())
                    .build(),
                CaseData.class
            );
        }

        return CallbackResponse.builder()
            .data(caseData)
            .build();

    }

    private CaseData getCaseData(String authorisation, CaseData caseData1,String fileName,String templateName)
        throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
            authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData1).build(),
                templateName
            );

        CaseData caseData = objectMapper.convertValue(
            CaseData.builder()
                .previewOrderDoc(Document.builder()
                                     .documentUrl(generatedDocumentInfo.getUrl())
                                     .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                     .documentHash(generatedDocumentInfo.getHashToken())
                                     .documentFileName(fileName)
                                     .build()),
            CaseData.class
        );
        return caseData;
    }

    @PostMapping(path = "/fetch-child-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to fetch child details ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Child details are fetched"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public CallbackResponse fetchChildDetails(
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        CaseData caseDataInput = manageOrderService.getUpdatedCaseData(caseData);

        return CallbackResponse.builder()
            .data(caseDataInput)
            .build();
    }




    @PostMapping(path = "/case-order-email-notification", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Send Email Notification on Case order")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse sendEmailNotificationOnClosingOrder(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        manageOrderEmailService.sendEmail(caseDetails);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();


    }

}
