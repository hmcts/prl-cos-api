package uk.gov.hmcts.reform.prl.services.courtnav;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseCreationService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamClient idamClient;
    private final AuthTokenGenerator authTokenGenerator;

    public CaseDetails createCourtNavCase(String authToken, CaseData testInput) {
        log.info("Roles of the calling user",idamClient.getUserInfo(authToken).getRoles());
        log.info("Name of the calling user",idamClient.getUserInfo(authToken).getName());
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("applicantCaseName",testInput.getApplicantCaseName());
        StartEventResponse startEventResponse =
            coreCaseDataApi.startForCaseworker(
                authToken,
                authTokenGenerator.generate(),
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
            .data(inputMap).build();

        return coreCaseDataApi.submitForCaseworker(
            authToken,
            authTokenGenerator.generate(),
            idamClient.getUserInfo(authToken).getUid(),
            PrlAppsConstants.JURISDICTION,
            PrlAppsConstants.CASE_TYPE,
            true,
            caseDataContent
        );
    }
}

