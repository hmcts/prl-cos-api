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

import java.util.List;
import java.util.Map;


@Service("cafcassHearingService")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingService {

    @Value("#{'${cafcaas.hearingStatus}'.split(',')}")
    private List<String> hearingStatusList;

    private Hearings hearingDetails;

    private List<Hearings> listOfHearingDetails;

    private final AuthTokenGenerator authTokenGenerator;

    private final HearingApiClient hearingApiClient;

    public Hearings getHearings(String userToken, String caseReferenceNumber) {
        try {
            hearingDetails = hearingApiClient.getHearingDetails(userToken, authTokenGenerator.generate(), caseReferenceNumber);
            filterHearings();
        } catch (Exception e) {
            log.error("Error in getHearings", e);
        }
        return hearingDetails;
    }

    public List<Hearings> getHearingsForAllCases(String userToken, Map<String,String> caseIdWithRegionIdMap) {
        try {
            listOfHearingDetails = hearingApiClient.getHearingDetailsForAllCaseIds(userToken, authTokenGenerator.generate(), caseIdWithRegionIdMap);
        } catch (Exception e) {
            log.error("Error while getHearingsForAllCases {}",e);
        }
        return listOfHearingDetails;
    }

    private void filterHearings() {

        if (hearingDetails != null && hearingDetails.getCaseHearings() != null)  {

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
                hearingDetails = null;
            }
        }
    }
}
