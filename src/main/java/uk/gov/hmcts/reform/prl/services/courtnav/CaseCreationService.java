package uk.gov.hmcts.reform.prl.services.courtnav;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseCreationService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamClient idamClient;

    public CaseDetails createCourtNavCase(String authToken, CaseData testInput, String serviceAuthorization) {
        StartEventResponse startEventResponse =
            coreCaseDataApi.startForCaseworker(
                authToken,
                serviceAuthorization,
                idamClient.getUserInfo(authToken).getUid(),
                PrlAppsConstants.JURISDICTION,
                PrlAppsConstants.CASE_TYPE,
                "courtnav-case-creation"
        );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(testInput).build();

        return coreCaseDataApi.submitForCaseworker(
            authToken,
            serviceAuthorization,
            idamClient.getUserInfo(authToken).getUid(),
            PrlAppsConstants.JURISDICTION,
            PrlAppsConstants.CASE_TYPE,
            true,
            caseDataContent
        );
    }
}

