package uk.gov.hmcts.reform.prl.controllers.fl401listonnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
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
        String isCaseWithOutNotice = Yes.equals(caseData.getOrderWithoutGivingNoticeToRespondent()
                                                    .getOrderWithoutGivingNotice())
            ? Yes.getDisplayedValue() : No.getDisplayedValue();
        caseDataUpdated.put("isFl401CaseCreatedForWithOutNotice", isCaseWithOutNotice);
        if (caseDataUpdated.containsKey("fl401ListOnNoticeHearingDetails")) {
            caseDataUpdated.put(
                "fl401ListOnNoticeHearingDetails",
                hearingDataService.getHearingData(existingFl401ListOnNoticeHearingDetails,hearingDataPrePopulatedDynamicLists,caseData));
        } else {
            caseDataUpdated.put(
                "fl401ListOnNoticeHearingDetails",
                ElementUtils.wrapElements(hearingDataService.generateHearingData(hearingDataPrePopulatedDynamicLists,caseData)));

        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

}
