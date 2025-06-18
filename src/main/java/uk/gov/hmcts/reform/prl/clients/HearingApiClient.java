package uk.gov.hmcts.reform.prl.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingResponse;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

import java.util.List;
import java.util.Map;

@FeignClient(
    name = "hearing-api",
    url = "${fis_hearing.api.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class
)
public interface HearingApiClient {

    String hearingPayload = """
        {
          "hmctsServiceCode" : "ABA5",
          "caseRef" : "<caseRef>",
          "caseHearings" : [ {
            "hearingID" : "999999",
            "hearingRequestDateTime" : null,
            "hearingType" : "ABA5-FFH",
            "hmcStatus" : "LISTED",
            "lastResponseReceivedDateTime" : null,
            "requestVersion" : null,
            "hearingListingStatus" : null,
            "listAssistCaseStatus" : null,
            "hearingDaySchedule" : [ {
              "hearingStartDateTime" : "2025-07-18T14:23:45.123",
              "hearingEndDateTime" : null,
              "listAssistSessionId" : null,
              "hearingVenueId" : null,
              "hearingVenueName" : null,
              "hearingVenueLocationCode" : null,
              "hearingVenueAddress" : null,
              "hearingRoomId" : null,
              "hearingJudgeId" : null,
              "hearingJudgeName" : null,
              "panelMemberIds" : null,
              "attendees" : null
            } ],
            "hearingGroupRequestId" : null,
            "hearingIsLinkedFlag" : null,
            "hearingTypeValue" : null,
            "nextHearingDate" : "2025-07-18T14:23:45.123",
            "urgentFlag" : false
          } ]
        }
        """;

    @GetMapping(path = "/hearings")
    Hearings getHearingDetails(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("caseReference") String caseReference
    );

    //TODO: DO NOT COMMIT
    @SuppressWarnings("squid:S00112")
    default Hearings getHearingDetailsHacked(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("caseReference") String caseReference
    ) throws RuntimeException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        String updatedHearingPayload = hearingPayload.replace("<caseRef>", caseReference);

        try {
            return mapper.readValue(updatedHearingPayload, Hearings.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping(value = "/serviceLinkedCases", consumes = "application/json")
    List<CaseLinkedData> getCaseLinkedData(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestBody CaseLinkedRequest caseLinkedRequest
    );

    @GetMapping(path = "/getNextHearingDate")
    NextHearingDetails getNextHearingDate(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("caseReference") String caseReference
    );

    @GetMapping(path = "/getFutureHearings")
    Hearings getFutureHearings(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("caseReference") String caseReference
    );

    @PostMapping(path = "/hearings-by-list-of-case-ids")
    List<Hearings> getHearingsByListOfCaseIds(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestBody Map<String,String> caseIdWithRegionIdMap
    );

    @PostMapping(path = "/hearings-by-list-of-caseids-without-venue")
    List<Hearings> getHearingsForAllCaseIdsWithCourtVenue(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestBody List<String> caseIds
    );

    @PostMapping(path = "/hearings-listed-for-today-by-list-of-caseids")
    Map<String, List<String>> getListedHearingsForAllCaseIdsOnCurrentDate(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestBody List<String> caseIds
    );

    @PostMapping(value = "/automated-hearing", consumes = "application/json")
    ResponseEntity<AutomatedHearingResponse> createAutomatedHearing(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestBody AutomatedHearingCaseData automatedHearingCaseData
    );
}
