package uk.gov.hmcts.reform.prl.services.caseaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.models.caseaccess.AssignCaseAccessRequest;
import uk.gov.hmcts.reform.prl.services.UserService;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;


@Service
@Slf4j
@RequiredArgsConstructor
public class AssignCaseAccessService {

    private final CcdDataStoreService ccdDataStoreService;
    private final AuthTokenGenerator authTokenGenerator;
    private final AssignCaseAccessClient assignCaseAccessClient;
    private final UserService userService;
    private final LaunchDarklyClient launchDarklyClient;


    public void assignCaseAccess(String caseId, String authorisation) {

        if (launchDarklyClient.isFeatureEnabled("share-a-case")) {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            String userId = userDetails.getId();

            log.info("CaseId: {} of type {} assigning case access to user {}", caseId, CASE_TYPE, userId);

            String serviceToken = authTokenGenerator.generate();
            assignCaseAccessClient.assignCaseAccess(
                authorisation,
                serviceToken,
                true,
                buildAssignCaseAccessRequest(caseId, userId, CASE_TYPE)
            );
            ccdDataStoreService.removeCreatorRole(caseId, authorisation);

            log.info("CaseId: {} assigned case access to user {}", caseId, userId);
        }
    }

    private AssignCaseAccessRequest buildAssignCaseAccessRequest(String caseId, String userId, String caseTypeId) {

        return AssignCaseAccessRequest
            .builder()
            .caseId(caseId)
            .assigneeId(userId)
            .caseTypeId(caseTypeId)
            .build();
    }


}
