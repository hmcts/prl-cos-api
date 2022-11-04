package uk.gov.hmcts.reform.prl.services.cafcass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.Cafcass.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.cafcass.Hearing.Hearings;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingService {

    Hearings hearingDetails = null;

    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    HearingApiClient hearingApiClient;

    public Hearings getHearings(String userToken, String caseReferenceNumber, String referenceNumber){
        try {
            hearingDetails = hearingApiClient.getHearingDetails(userToken, authTokenGenerator.generate(), caseReferenceNumber);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return hearingDetails;
    }
}
