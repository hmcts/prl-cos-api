package uk.gov.hmcts.reform.prl.controllers.listwithoutnotice;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTWITHOUTNOTICE_HEARINGDETAILS;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class ListWithoutNoticeController extends AbstractCallbackController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    HearingDataService hearingDataService;

    @Autowired
    LocationRefDataService locationRefDataService;

    @Autowired
    RefDataUserService refDataUserService;

    @Autowired
    AllocatedJudgeService allocatedJudgeService;

    @Autowired
    @Qualifier("caseSummaryTab")
    private CaseSummaryTabService caseSummaryTabService;
    public static final String CONFIRMATION_HEADER = "# Listing directions sent";

    public static final String CONFIRMATION_BODY_PREFIX = "### What happens next \n\n "
        + "<ul><li>Listing directions  have been sent as a task to their local court listing.</li> "
        + "<li>Listing directions have been saved in the notes tab and are available to view at any time.</li></ul>";


    @PostMapping(path = "/pre-populate-hearingPage-Data", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate Hearing page details")
    public AboutToStartOrSubmitCallbackResponse prePopulateHearingPageData(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        log.info("prePopulateHearingPageData new or existing callbackRequest : {}", callbackRequest);
        String caseReferenceNumber = String.valueOf(callbackRequest.getCaseDetails().getId());
        log.info("Inside Prepopulate prePopulateHearingPageData for the case id {}", caseReferenceNumber);
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        List<Element<HearingData>> existingListWithoutNoticeHearingDetails = caseData.getListWithoutNoticeHearingDetails();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            hearingDataService.populateHearingDynamicLists(authorisation, caseReferenceNumber, caseData);
        log.info("Prepoulated hearingDynamicLists {}", hearingDataPrePopulatedDynamicLists);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (caseDataUpdated.containsKey(LISTWITHOUTNOTICE_HEARINGDETAILS)) {
            log.info("Inside case data updated containd key check for the case id {}", caseReferenceNumber);
            caseDataUpdated.put(
                LISTWITHOUTNOTICE_HEARINGDETAILS,
                hearingDataService.getHearingData(existingListWithoutNoticeHearingDetails,hearingDataPrePopulatedDynamicLists,caseData));
            log.info("Inside controller after mapping hearing data  {}", caseDataUpdated.get(LISTWITHOUTNOTICE_HEARINGDETAILS));
        } else {
            caseDataUpdated.put(
                LISTWITHOUTNOTICE_HEARINGDETAILS,
                ElementUtils.wrapElements(hearingDataService.generateHearingData(hearingDataPrePopulatedDynamicLists,caseData)));

        }
        //populate legal advisor list
        List<DynamicListElement> legalAdviserList = refDataUserService.getLegalAdvisorList();
        caseDataUpdated.put("legalAdviserList", DynamicList.builder().value(DynamicListElement.EMPTY).listItems(legalAdviserList)
            .build());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/listWithoutNotice", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "List Without Notice submission flow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List Without notice submission is success"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse listWithoutNoticeSubmission(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("listWithoutNoticeSubmission  callbackRequest : {}", callbackRequest);
        log.info("Without Notice Submission flow - case id : {}", callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("Getting the listWithoutNoticeHearingDetails from caseDataUpdated ");
        Object listWithoutNoticeHeardetailsObj = caseDataUpdated.get(LISTWITHOUTNOTICE_HEARINGDETAILS);
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
        caseDataUpdated.put(LISTWITHOUTNOTICE_HEARINGDETAILS, hearingDataService
            .getHearingData(caseData.getListWithoutNoticeHearingDetails(),null,caseData));
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


    @PostMapping(path = "/listWithoutNotice-confirmation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to List without notice confirmation . Returns service request reference if "
        + "successful")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = uk.gov.hmcts.reform.ccd.client.model.CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<SubmittedCallbackResponse> ccdSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest callbackRequest
    ) {
        return ok(SubmittedCallbackResponse.builder().confirmationHeader(
            CONFIRMATION_HEADER).confirmationBody(
            CONFIRMATION_BODY_PREFIX
        ).build());
    }
}
