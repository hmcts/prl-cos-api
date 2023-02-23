package uk.gov.hmcts.reform.prl.controllers.listwithoutnotice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class ListWithoutNoticeController extends AbstractCallbackController {

    @Autowired
    RefDataUserService refDataUserService;

    @PostMapping(path = "/pre-populate-hearingPage-Data", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate Hearing type details")
    public AboutToStartOrSubmitCallbackResponse prePopulateHearingPageData(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        log.info("Inside Prepopulate prePopulateHearingPageData for the case id {}", callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        List<Element<HearingData>> existingListWithoutNoticeHearingDetails = caseData.getListWithoutNoticeHearingDetails();
        if (caseDataUpdated.containsKey("listWithoutNoticeHearingDetails")) {
            caseDataUpdated.put("listWithoutNoticeHearingDetails",
                existingListWithoutNoticeHearingDetails);
        } else {
            caseDataUpdated.put("listWithoutNoticeHearingDetails",
                ElementUtils.wrapElements(HearingData.builder()
                    .hearingTypes(DynamicList.builder()
                        .value(DynamicListElement.EMPTY)
                        .listItems(prePopulateHearingType(authorisation)).build()).build()));
        }


        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    private List<DynamicListElement> prePopulateHearingType(String authorisation) {
        return refDataUserService.retrieveCategoryValues(authorisation, HEARINGTYPE);
    }


    @PostMapping(path = "/listWithoutNotice", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "List Without Notice")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List Without notice is successful"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse listWithoutNotice(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Without Notice Submission flow - case id : {}", callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

}
