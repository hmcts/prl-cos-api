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
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicRadioList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGCHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TELEPHONESUBCHANNELS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.VIDEOSUBCHANNELS;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class ListWithoutNoticeController extends AbstractCallbackController {

    @Autowired
    RefDataUserService refDataUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    HearingDataService hearingDataService;

    @Autowired
    LocationRefDataService locationRefDataService;

    private DynamicList retrievedHearingTypes = null;

    private DynamicList retrievedHearingDates = null;

    private DynamicList retrievedHearingChannels = null;

    private DynamicRadioList retrievedRadioHearingChannels = null;

    private DynamicList retrievedVideoSubChannels = null;

    private DynamicList retrievedTelephoneSubChannels = null;

    private DynamicList retrievedCourtLocations = null;


    @PostMapping(path = "/pre-populate-hearingPage-Data", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate Hearing page details")
    public AboutToStartOrSubmitCallbackResponse prePopulateHearingPageData(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        log.info("Inside Prepopulate prePopulateHearingPageData for the case id {}", callbackRequest.getCaseDetails().getId());
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        List<Element<HearingData>> existingListWithoutNoticeHearingDetails = caseData.getListWithoutNoticeHearingDetails();
        if (null == retrievedHearingTypes) {
            retrievedHearingTypes = DynamicList.builder()
                .value(DynamicListElement.EMPTY)
                .listItems(hearingDataService.prePopulateHearingType(authorisation)).build();
        }
        if (null == retrievedHearingDates) {
            retrievedHearingDates = DynamicList.builder()
                .value(DynamicListElement.EMPTY)
                .listItems(hearingDataService.getHearingStartDate(authorisation,caseData)).build();
        }
        if (null == retrievedHearingChannels) {
            Map<String,List<DynamicListElement>> populateHearingChannel =
                hearingDataService.prePopulateHearingChannel(authorisation);
            retrievedHearingChannels = DynamicList.builder()
                .value(DynamicListElement.EMPTY)
                .listItems(populateHearingChannel.get(HEARINGCHANNEL)).build();
            retrievedRadioHearingChannels = DynamicRadioList.builder()
                .value(DynamicListElement.EMPTY)
                .listItems(populateHearingChannel.get(HEARINGCHANNEL)).build();
            retrievedVideoSubChannels = DynamicList.builder()
                .value(DynamicListElement.EMPTY)
                .listItems(populateHearingChannel.get(VIDEOSUBCHANNELS)).build();
            retrievedTelephoneSubChannels = DynamicList.builder()
                .value(DynamicListElement.EMPTY)
                .listItems(populateHearingChannel.get(TELEPHONESUBCHANNELS)).build();
        }
        if (null == retrievedCourtLocations) {
            retrievedCourtLocations = DynamicList.builder()
                .value(DynamicListElement.EMPTY)
                .listItems(locationRefDataService.getCourtLocations(authorisation)).build();
        }
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (caseDataUpdated.containsKey("listWithoutNoticeHearingDetails")) {
            caseDataUpdated.put("listWithoutNoticeHearingDetails",
                                hearingDataService.mapHearingData(existingListWithoutNoticeHearingDetails,
                                                                  retrievedHearingTypes,retrievedHearingDates,
                                                                  retrievedHearingChannels,retrievedRadioHearingChannels,
                                                                  retrievedVideoSubChannels,retrievedTelephoneSubChannels,
                                                                  retrievedCourtLocations));
        } else {
            caseDataUpdated.put("listWithoutNoticeHearingDetails",
                ElementUtils.wrapElements(HearingData.builder()
                    .hearingTypes(retrievedHearingTypes)
                    .confirmedHearingDates(retrievedHearingDates)
                                              .hearingChannels(retrievedHearingChannels)
                                              .hearingVideoChannels(retrievedVideoSubChannels)
                                              .hearingTelephoneChannels(retrievedTelephoneSubChannels)
                                              .courtList(retrievedCourtLocations)
                                              .hearingChannelDynamicRadioList(retrievedRadioHearingChannels)
                    .build()));

        }

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
        log.info("Without Notice Submission flow - case id : {}", callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );

        caseDataUpdated.put("listWithoutNoticeHearingDetails",hearingDataService
            .mapHearingData(caseData.getListWithoutNoticeHearingDetails(),null,null,null,
                            null,null,null,null));

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


}
