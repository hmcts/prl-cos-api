package uk.gov.hmcts.reform.prl.services.hearings;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Qualifier("hearingService")
public class HearingService {

    private Hearings hearingDetails;

    private final AuthTokenGenerator authTokenGenerator;

    private final HearingApiClient hearingApiClient;

    public Hearings getHearings(String userToken, String caseReferenceNumber) {

        try {
            hearingDetails = hearingApiClient.getHearingDetails(userToken, authTokenGenerator.generate(), caseReferenceNumber);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return hearingDetails;
    }


}