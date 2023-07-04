package uk.gov.hmcts.reform.prl.services.hearings;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingService {

    private List<CaseLinkedData> caseLinkedData;

    private final AuthTokenGenerator authTokenGenerator;

    private final HearingApiClient hearingApiClient;

    public Hearings getHearings(String userToken, String caseReferenceNumber) {

        try {
            return hearingApiClient.getHearingDetails(userToken, authTokenGenerator.generate(), caseReferenceNumber);

        } catch (Exception e) {
            log.error("Error in getting hearings ", e);
        }
        return null;
    }


    public List<CaseLinkedData> getCaseLinkedData(String userToken, CaseLinkedRequest caseLinkedRequest) {

        try {
            caseLinkedData = hearingApiClient.getCaseLinkedData(userToken, authTokenGenerator.generate(), caseLinkedRequest);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return caseLinkedData;
    }


    public NextHearingDetails getNextHearingDate(String userToken, String caseReferenceNumber) {

        try {
            return hearingApiClient.getNextHearingDate(userToken, authTokenGenerator.generate(), caseReferenceNumber);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public Hearings getFutureHearings(String userToken, String caseReferenceNumber) {

        try {
            return hearingApiClient.getFutureHearings(userToken, authTokenGenerator.generate(), caseReferenceNumber);
        } catch (Exception e) {
            log.error("Error in getFutureHearings ----> {}", e);
        }
        return null;
    }

}
