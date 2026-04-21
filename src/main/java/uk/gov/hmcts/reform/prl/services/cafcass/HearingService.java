package uk.gov.hmcts.reform.prl.services.cafcass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.cafcass.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service("cafcassHearingService")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingService {

    @Value("#{'${cafcaas.hearingStatus}'.split(',')}")
    private List<String> hearingStatusList;

    private final AuthTokenGenerator authTokenGenerator;

    private final HearingApiClient hearingApiClient;

    public Hearings getHearings(String userToken, String caseReferenceNumber) {
        Hearings hearingDetails = null;
        try {
            hearingDetails = hearingApiClient.getHearingDetails(
                userToken,
                authTokenGenerator.generate(),
                caseReferenceNumber
            );
            hearingDetails = filterHearings(hearingDetails);
        } catch (Exception e) {
            log.error("Error in getHearings", e.getMessage());
        }
        return hearingDetails;
    }

    public List<Hearings> getHearingsForAllCases(String userToken, Map<String, String> caseIdWithRegionIdMap) {
        List<Hearings> listOfHearingDetails = null;
        try {
            listOfHearingDetails = hearingApiClient.getHearingDetailsForAllCaseIds(
                userToken,
                authTokenGenerator.generate(),
                caseIdWithRegionIdMap
            );
        } catch (Exception e) {
            log.error("Error while getHearingsForAllCases {}", e.getMessage());
            return Collections.emptyList();
        }
        return listOfHearingDetails;
    }

    private Hearings filterHearings(Hearings hearingDetails) {

        if (hearingDetails != null && hearingDetails.getCaseHearings() != null) {

            final List<CaseHearing> caseHearings = hearingDetails.getCaseHearings();

            final List<String> hearingStatuses = hearingStatusList.stream().map(String::trim).toList();

            final List<CaseHearing> hearings = caseHearings.stream()
                .filter(hearing ->
                            hearingStatuses.stream().anyMatch(hearingStatus -> hearingStatus.equals(
                                hearing.getHmcStatus()))
                )
                .toList();


            // if we find any hearing after filteration, change hmc status to null as it's not required in response.
            if (hearings != null && !hearings.isEmpty()) {
                hearingDetails.setCaseHearings(hearings);
                log.debug("Hearings filtered based on Listed hearing");
            } else {
                return null;
            }
        }
        return hearingDetails;
    }
}
