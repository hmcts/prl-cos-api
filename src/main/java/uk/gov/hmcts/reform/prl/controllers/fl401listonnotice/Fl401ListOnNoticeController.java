package uk.gov.hmcts.reform.prl.controllers.fl401listonnotice;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_LIST_ON_NOTICE_FL404B_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_LISTONNOTICE_HEARINGDETAILS;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class Fl401ListOnNoticeController extends AbstractCallbackController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    HearingDataService hearingDataService;

    @Autowired
    RefDataUserService refDataUserService;

    @Autowired
    AllocatedJudgeService allocatedJudgeService;

    @Autowired
    private DocumentGenService documentGenService;

    @Autowired
    @Qualifier("caseSummaryTab")
    private CaseSummaryTabService caseSummaryTabService;

    public static final String CONFIRMATION_HEADER = "# Listing directions sent";

    public static final String CONFIRMATION_BODY_PREFIX = "### What happens next \n\n "
        + "<ul><li>Listing directions  have been sent as a task to their local court listing.</li> "
        + "<li>Listing directions have been saved in the notes tab and are available to view at any time.</li></ul>";


    @PostMapping(path = "/pre-populate-screen-and-hearing-data", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate Hearing page details")
    public AboutToStartOrSubmitCallbackResponse prePopulateHearingPageDataForFl401ListOnNotice(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        String caseId = String.valueOf(callbackRequest.getCaseDetails().getId());
        log.info("Inside Prepopulate prePopulateHearingPageDataForFl401ListOnNotice for the case id {}", caseId);
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        List<Element<HearingData>> existingFl401ListOnNoticeHearingDetails = caseData.getFl401ListOnNotice().getFl401ListOnNoticeHearingDetails();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            hearingDataService.populateHearingDynamicLists(authorisation, caseId, caseData);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        String isCaseWithOutNotice = String.valueOf(Yes.equals(caseData.getOrderWithoutGivingNoticeToRespondent()
                                                    .getOrderWithoutGivingNotice())
            ? Yes : No);
        caseDataUpdated.put("isFl401CaseCreatedForWithOutNotice", isCaseWithOutNotice);
        log.info("Check case is created without Notice::====: {}",caseDataUpdated.get("isFl401CaseCreatedForWithOutNotice"));

        if (caseDataUpdated.containsKey(FL401_LISTONNOTICE_HEARINGDETAILS)) {
            caseDataUpdated.put(
                FL401_LISTONNOTICE_HEARINGDETAILS,
                hearingDataService.getHearingData(existingFl401ListOnNoticeHearingDetails,hearingDataPrePopulatedDynamicLists,caseData));
        } else {
            caseDataUpdated.put(
                FL401_LISTONNOTICE_HEARINGDETAILS,
                ElementUtils.wrapElements(hearingDataService.generateHearingData(hearingDataPrePopulatedDynamicLists,caseData)));

        }
        List<DynamicListElement> linkedCasesList = hearingDataService.getLinkedCases(authorisation, caseData);
        caseDataUpdated.put(
            "linkedCaCasesList",
            hearingDataService.getDynamicList(linkedCasesList));
        log.info("Linked CA cases List:::: {}",caseDataUpdated.get("linkedCaCasesList"));

        List<DynamicListElement> legalAdviserList = refDataUserService.getLegalAdvisorList();
        caseDataUpdated.put("legalAdviserList", DynamicList.builder().value(DynamicListElement.EMPTY).listItems(legalAdviserList)
            .build());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/fl401ListOnNotice-document-generation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "List Without Notice submission flow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List Without notice submission is success"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse generateFl404bDocumentGeneration(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        log.info("Without Notice Submission flow - case id : {}", callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        Document document = documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            DA_LIST_ON_NOTICE_FL404B_DOCUMENT,
            false
        );
        caseDataUpdated.put("fl401ListOnNoticeDocument", document);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/fl401-list-on-notice/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "List Without Notice submission flow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List Without notice submission is success"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse fl401ListOnNoticeSubmission(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Without Notice Submission flow - case id : {}", callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        Object listWithoutNoticeHeardetailsObj = caseDataUpdated.get(FL401_LISTONNOTICE_HEARINGDETAILS);
        hearingDataService.nullifyUnncessaryFieldsPopulated(listWithoutNoticeHeardetailsObj);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        AllocatedJudge allocatedJudge = allocatedJudgeService.getAllocatedJudgeDetails(caseDataUpdated,
                                                                                       caseData.getLegalAdviserList(), refDataUserService);
        caseData = caseData.toBuilder().allocatedJudge(allocatedJudge).build();
        caseDataUpdated.putAll(caseSummaryTabService.updateTab(caseData));
        caseDataUpdated.put(FL401_LISTONNOTICE_HEARINGDETAILS, hearingDataService
            .getHearingData(caseData.getFl401ListOnNotice().getFl401ListOnNoticeHearingDetails(),null,caseData));
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
