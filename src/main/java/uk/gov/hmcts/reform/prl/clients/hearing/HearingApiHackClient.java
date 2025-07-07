package uk.gov.hmcts.reform.prl.clients.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = "hearing.hack.enabled", havingValue = "true")
public class HearingApiHackClient implements HearingApiClient {
    private static final DateTimeFormatter dateTimeFormatter = ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public Hearings getHearingDetails(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("caseReference") String caseReference
    ) throws RuntimeException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        LocalDateTime firstHearingDate = LocalDateTime.now().plusDays(1);
        LocalDateTime secondHearingDate = firstHearingDate.plusDays(7);

        String hearingPayload = null;

        Resource resource = new ClassPathResource("/hearingHackResponse.json");
        try (InputStream inputStream = resource.getInputStream()) {
            hearingPayload = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            String updatedHearingPayload = StringUtils.replaceEach(
                hearingPayload,
                new String[]{
                    "<caseRef>",
                    "<firstHearingStartDate>",
                    "<firstHearingEndDate>",
                    "<firstNextHearingDate>",
                    "<secondHearingStartDate>",
                    "<secondHearingEndDate>",
                    "<secondNextHearingDate>"
                },
                new String[]{
                    caseReference,
                    dateTimeFormatter.format(firstHearingDate),
                    dateTimeFormatter.format(firstHearingDate.plusDays(2)),
                    dateTimeFormatter.format(firstHearingDate.plusDays(5)),
                    dateTimeFormatter.format(secondHearingDate),
                    dateTimeFormatter.format(secondHearingDate.plusDays(2)),
                    dateTimeFormatter.format(secondHearingDate.plusDays(5))
                }
            );
            log.info("Hearing hack response: {}", updatedHearingPayload);
            return mapper.readValue(updatedHearingPayload, Hearings.class);
        } catch (IOException e) {
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
