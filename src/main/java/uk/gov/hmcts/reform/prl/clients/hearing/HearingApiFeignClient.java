package uk.gov.hmcts.reform.prl.clients.hearing;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingResponse;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Primary
@ConditionalOnProperty(name = "hearing.hack.enabled", havingValue = "false", matchIfMissing = true)
public class HearingApiFeignClient implements HearingApiClient {

    private final HearingApiFeignClient hearingApiFeignClient;

    @Override
    public Hearings getHearingDetails(String authorisation,
                                      String serviceAuthorization,
                                      String caseReference) {
        return hearingApiFeignClient.getHearingDetails(
            authorisation,
            serviceAuthorization,
            caseReference);
    }

    @Override
    public List<CaseLinkedData> getCaseLinkedData(String authorisation,
                                                  String serviceAuthorization,
                                                  CaseLinkedRequest caseLinkedRequest) {
        return hearingApiFeignClient.getCaseLinkedData(
            authorisation,
            serviceAuthorization,
            caseLinkedRequest);
    }

    @Override

    public NextHearingDetails getNextHearingDate(String authorisation,
                                                 String serviceAuthorization,
                                                 String caseReference) {
        return hearingApiFeignClient.getNextHearingDate(
            authorisation,
            serviceAuthorization,
            caseReference);
    }

    @Override
    public Hearings getFutureHearings(String authorisation,
                                      String serviceAuthorization,
                                      String caseReference) {
        return hearingApiFeignClient.getFutureHearings(
            authorisation,
            serviceAuthorization,
            caseReference);
    }

    @Override
    public List<Hearings> getHearingsByListOfCaseIds(String authorisation,
                                                     String serviceAuthorization,
                                                     Map<String, String> caseIdWithRegionIdMap) {
        return hearingApiFeignClient.getHearingsByListOfCaseIds(
            authorisation,
            serviceAuthorization,
            caseIdWithRegionIdMap);
    }

    @Override
    public List<Hearings> getHearingsForAllCaseIdsWithCourtVenue(String authorisation,
                                                                 String serviceAuthorization,
                                                                 List<String> caseIds) {
        return hearingApiFeignClient.getHearingsForAllCaseIdsWithCourtVenue(
            authorisation,
            serviceAuthorization,
            caseIds);
    }

    @Override
    public Map<String, List<String>> getListedHearingsForAllCaseIdsOnCurrentDate(String authorisation,
                                                                                 String serviceAuthorization,
                                                                                 List<String> caseIds) {
        return hearingApiFeignClient.getListedHearingsForAllCaseIdsOnCurrentDate(
            authorisation,
            serviceAuthorization,
            caseIds);
    }

    @Override
    public ResponseEntity<AutomatedHearingResponse> createAutomatedHearing(String authorisation,
                                                                           String serviceAuthorization,
                                                                           AutomatedHearingCaseData automatedHearingCaseData) {
        return hearingApiFeignClient.createAutomatedHearing(
            authorisation,
            serviceAuthorization,
            automatedHearingCaseData);
    }
}
