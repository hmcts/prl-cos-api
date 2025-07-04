package uk.gov.hmcts.reform.prl.clients.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingResponse;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static java.lang.ClassLoader.getSystemResource;
import static java.nio.file.Files.readString;

@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = "hearing.hack.enabled", havingValue = "true")
public class HearingApiHackClient implements HearingApiClient {

    public Hearings getHearingDetails(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("caseReference") String caseReference
    ) throws RuntimeException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        String hearingPayload = null;
        try {
            hearingPayload = readString(Paths.get(getSystemResource("hearingHackResponse.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String updatedHearingPayload = hearingPayload.replace("<caseRef>", caseReference);

        try {
            return mapper.readValue(updatedHearingPayload, Hearings.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CaseLinkedData> getCaseLinkedData(String authorisation,
                                                  String serviceAuthorization,
                                                  CaseLinkedRequest caseLinkedRequest) {
        throw new UnsupportedOperationException("Feign call not supported from hack api");
    }

    @Override
    public NextHearingDetails getNextHearingDate(String authorisation,
                                                 String serviceAuthorization,
                                                 String caseReference) {
        throw new UnsupportedOperationException("Feign call not supported from hack api");
    }

    @Override
    public Hearings getFutureHearings(String authorisation,
                                      String serviceAuthorization,
                                      String caseReference) {
        throw new UnsupportedOperationException("Feign call not supported from hack api");
    }

    @Override
    public List<Hearings> getHearingsByListOfCaseIds(String authorisation,
                                                     String serviceAuthorization,
                                                     Map<String, String> caseIdWithRegionIdMap) {
        throw new UnsupportedOperationException("Feign call not supported from hack api");
    }

    @Override
    public List<Hearings> getHearingsForAllCaseIdsWithCourtVenue(String authorisation,
                                                                 String serviceAuthorization,
                                                                 List<String> caseIds) {
        throw new UnsupportedOperationException("Feign call not supported from hack api");
    }

    @Override
    public Map<String, List<String>> getListedHearingsForAllCaseIdsOnCurrentDate(String authorisation,
                                                                                 String serviceAuthorization,
                                                                                 List<String> caseIds) {
        throw new UnsupportedOperationException("Feign call not supported from hack api");
    }

    @Override
    public ResponseEntity<AutomatedHearingResponse> createAutomatedHearing(String authorisation,
                                                                           String serviceAuthorization,
                                                                           AutomatedHearingCaseData automatedHearingCaseData) {
        throw new UnsupportedOperationException("Feign call not supported from hack api");
    }
}
