package uk.gov.hmcts.reform.prl.services.hearings;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingService {

    private Hearings hearingDetails;

    private final AuthTokenGenerator authTokenGenerator;

    private  HearingApiClient hearingApiClient;

    public Hearings getHearings(String userToken, String caseReferenceNumber) {

        try {
            hearingDetails = hearingApiClient.getHearingDetails(userToken, authTokenGenerator.generate(), caseReferenceNumber);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return hearingDetails;
    }

    public NextHearingDetails getNextHearingDate(String userToken, String caseReferenceNumber) {

        try {
            return hearingApiClient.getNextHearingDate(userToken, authTokenGenerator.generate(), caseReferenceNumber);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

}
