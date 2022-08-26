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
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenCoreCaseDataService;

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
    @Autowired
    SystemUserService systemUserService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CcdCaseApi.class);

    public void linkCitizenToCase(String authorisation, String caseId, CaseData caseData) {
        String anonymousUserToken = systemUserService.getSysUserToken();
        linkToCase(authorisation, anonymousUserToken, caseId, caseData);
    }

    private void linkToCase(String authorisation, String anonymousUserToken, String caseId, CaseData caseData) {
        UserDetails userDetails = idamClient.getUserDetails(authorisation);
        LOGGER.info("<--linkToCase-> Linking the case " + caseId);
        LOGGER.debug("Granting access to case {} for citizen {}", caseId, userDetails.getId());
        this.grantAccessToCase(userDetails.getId(), anonymousUserToken, caseId);

        // LOGGER.debug("Revoking access to case {} ", caseId);
        // this.revokeAccessToCase(userDetails, anonymousUserToken, caseId);
        this.linkCitizen(authorisation, userDetails, caseId, caseData);
        LOGGER.info("case is now linked " + caseId);
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

    private void revokeAccessToCase(UserDetails userDetails, String anonymousUserToken, String caseId) {
        LOGGER.debug("Revoking access to case {}", caseId);
        caseAccessApi.revokeAccessToCase(
            anonymousUserToken,
            authTokenGenerator.generate(),
            idamClient.getUserDetails(anonymousUserToken).getId(),
            PrlAppsConstants.JURISDICTION,
            PrlAppsConstants.CASE_TYPE,
            caseId,
            userDetails.getId()
        );
    }

    private CaseDetails linkCitizen(
        String authorisation,
        UserDetails citizenUser,
        String caseId,
        CaseData caseData
    ) {
        LOGGER.info("<----updateCitizenIdAndEmail---->", caseId);
        return citizenCoreCaseDataService.linkDefendant(
            authorisation,
            Long.valueOf(caseId),
            caseData,
            CaseEvent.LINK_CITIZEN
        );
    }
}
