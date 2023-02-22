package uk.gov.hmcts.reform.prl.controllers.listwithoutnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.HearingUtils;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class ListWithoutNoticeController {


    @Autowired
    RefDataUserService refDataUserService;

    @Autowired
    HearingUtils hearingUtils;

    @Autowired
    ObjectMapper objectMapper;

    @PostMapping(path = "/pre-populate-hearingPage-Data", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate Hearing type details")
    public AboutToStartOrSubmitCallbackResponse prePopulateHearingPageData(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        log.info("Inside Prepopulate prePopulateHearingPageData for the case id {}",callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class)
            .toBuilder()
            .id(callbackRequest.getCaseDetails().getId())
            .build();

        caseDataUpdated.put("listWithoutNoticeHearingDetails",
                            ElementUtils.wrapElements(HearingData.builder()
                                                          .hearingTypes(DynamicList.builder()
                                                                            .value(DynamicListElement.EMPTY)
                                                                            .listItems(prePopulateHearingType(authorisation)).build())
                                                          .confirmedHearingDates(DynamicList.builder()
                                                                             .value(DynamicListElement.EMPTY)
                                                                             .listItems(prePopulateConfirmedHearingDate(authorisation,caseData))
                                                                             .build()).build()));

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    private List<DynamicListElement> prePopulateHearingType(String authorisation) {
        List<DynamicListElement> listOfHearingType = refDataUserService.retrieveCategoryValues(authorisation, HEARINGTYPE);
        if (null != listOfHearingType.get(0).getCode()) {
            listOfHearingType.add(DynamicListElement.builder().code("Other").label("Other").build());
        }
        return listOfHearingType;

    }

    private List<DynamicListElement> prePopulateConfirmedHearingDate(String authorisation, CaseData caseData) {
        List<DynamicListElement> hearingStartDate = hearingUtils.getHearingStartDate(authorisation, caseData);
        log.info("Prepopulate confirmedHearingDate {} for the case reference number {} ",hearingStartDate,String.valueOf(caseData.getId()));
        return hearingStartDate;
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
