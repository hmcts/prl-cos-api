package uk.gov.hmcts.reform.prl.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenCoreCaseDataService;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CcdCaseApi {
    @Autowired
    AuthTokenGenerator authTokenGenerator;
    @Autowired
    CaseAccessApi caseAccessApi;

    @Autowired
    CitizenCoreCaseDataService citizenCoreCaseDataService;

    @Autowired
    IdamClient idamClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(CcdCaseApi.class);

    public void linkCitizenToCase(String authorisation, String anonymousUserToken, String caseId,
                                  CaseData caseData, StartEventResponse startEventResponse, Map<String, Object> caseDataUpdated) {
        linkToCase(authorisation, anonymousUserToken, caseId, caseData, startEventResponse, caseDataUpdated);
    }

    private void linkToCase(String authorisation, String anonymousUserToken, String caseId,
                            CaseData caseData, StartEventResponse startEventResponse, Map<String, Object> caseDataUpdated) {
        UserDetails userDetails = idamClient.getUserDetails(authorisation);
        LOGGER.info("linkToCase  Linking the case {} ", caseId);
        LOGGER.debug("Granting access to case {} for citizen {}", caseId, userDetails.getId());
        this.grantAccessToCase(userDetails.getId(), anonymousUserToken, caseId);
        this.linkCitizen(anonymousUserToken, caseId, caseData, startEventResponse, caseDataUpdated);
        LOGGER.info("case is now linked {}", caseId);
    }

    private void grantAccessToCase(String citizenId, String anonymousUserToken, String caseId) {
        caseAccessApi.grantAccessToCase(
            anonymousUserToken,
            authTokenGenerator.generate(),
            idamClient.getUserDetails(anonymousUserToken).getId(),
            PrlAppsConstants.JURISDICTION,
            PrlAppsConstants.CASE_TYPE,
            caseId,
            new UserId(citizenId)
        );
    }

    private CaseDetails linkCitizen(
        String anonymousUserToken,
        String caseId,
        CaseData caseData,
        StartEventResponse startEventResponse, Map<String, Object> caseDataUpdated) {
        LOGGER.info("updateCitizenIdAndEmail {}", caseId);
        return citizenCoreCaseDataService.linkDefendant(
            anonymousUserToken,
            Long.valueOf(caseId),
            caseData,
            CaseEvent.LINK_CITIZEN,
            startEventResponse,
            caseDataUpdated
        );
    }

    public CaseDetails updateCase(String authorisation, String caseId, CaseData caseData, CaseEvent caseEvent) {
        return citizenCoreCaseDataService.updateCase(
            authorisation,
            Long.valueOf(caseId),
            caseData,
            caseEvent
        );
    }

    public CaseDetails createCase(String authorisation, CaseData caseData) {
        return citizenCoreCaseDataService.createCase(
            authorisation,
            caseData
        );
    }

    public CaseDetails getCase(String authorisation, String caseId) {
        return citizenCoreCaseDataService.getCase(
            authorisation,
            caseId
        );
    }
}
