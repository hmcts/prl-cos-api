package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LinkCitizenCaseService {

    private final AllTabServiceImpl allTabService;
    private final SystemUserService systemUserService;
    private final ObjectMapper objectMapper;
    private final CcdCoreCaseDataService ccdCoreCaseDataService;
    private final IdamClient idamClient;
    private final CaseAccessApi caseAccessApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final LaunchDarklyClient launchDarklyClient;

    public static final String INVALID = "Invalid";
    public static final String VALID = "Valid";
    public static final String LINKED = "Linked";
    public static final String YES = "Yes";
    public static final String CASE_INVITES = "caseInvites";
    public static final String CITIZEN_ALLOW_DA_JOURNEY = "citizen-allow-da-journey";

    public Optional<CaseDetails> linkCitizenToCase(String authorisation, String caseId, String accessCode) {
        Optional<CaseDetails> caseDetails = Optional.empty();
        CaseData dbCaseData = findAndGetCase(caseId);

        if (VALID.equalsIgnoreCase(findAccessCodeStatus(accessCode, dbCaseData))) {
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
                = allTabService.getStartUpdateForSpecificEvent(caseId, CaseEvent.LINK_CITIZEN.getValue());

            CaseData caseData = startAllTabsUpdateDataContent.caseData();
            UserDetails userDetails = idamClient.getUserDetails(authorisation);
            Map<String, Object> caseDataUpdated = getCaseDataMapToLinkCitizen(accessCode, caseData, userDetails);

            caseAccessApi.grantAccessToCase(
                startAllTabsUpdateDataContent.authorisation(),
                authTokenGenerator.generate(),
                idamClient.getUserDetails(startAllTabsUpdateDataContent.authorisation()).getId(),
                PrlAppsConstants.JURISDICTION,
                PrlAppsConstants.CASE_TYPE,
                caseId,
                new UserId(userDetails.getId())
            );

            caseDetails = Optional.ofNullable(allTabService.submitAllTabsUpdate(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                caseDataUpdated
            ));
        }
        return caseDetails;
    }

    private CaseData findAndGetCase(String caseId) {
        String anonymousUserToken = systemUserService.getSysUserToken();
        return objectMapper.convertValue(
            ccdCoreCaseDataService.findCaseById(anonymousUserToken, caseId).getData(),
            CaseData.class
        );
    }

    public Map<String, Object> getCaseDataMapToLinkCitizen(String accessCode,
                                                           CaseData caseData,
                                                           UserDetails userDetails) {
        UUID partyId = null;
        YesOrNo isApplicant = YesOrNo.Yes;
        Map<String, Object> caseDataUpdated = new HashMap<>();

        String userId = userDetails.getId();
        String emailId = userDetails.getEmail();

        for (Element<CaseInvite> invite : caseData.getCaseInvites()) {
            if (accessCode.equals(invite.getValue().getAccessCode())) {
                partyId = invite.getValue().getPartyId();
                isApplicant = invite.getValue().getIsApplicant();
                invite.getValue().setHasLinked(YES);
                invite.getValue().setInvitedUserId(userId);
            }
        }
        caseDataUpdated.put(CASE_INVITES, caseData.getCaseInvites());

        caseDataUpdated.putAll(processUserDetailsForCase(userId, emailId, caseData, partyId, isApplicant));

        return caseDataUpdated;
    }

    private Map<String, Object> processUserDetailsForCase(String userId, String emailId, CaseData caseData, UUID partyId,
                                                          YesOrNo isApplicant) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            caseDataUpdated.putAll(getValuesFromPartyDetails(caseData, partyId, isApplicant, userId, emailId));
        } else {
            if (YesOrNo.Yes.equals(isApplicant)) {
                User user = caseData.getApplicantsFL401().getUser().toBuilder().email(emailId)
                    .idamId(userId).build();
                caseData.getApplicantsFL401().setUser(user);
                caseDataUpdated.put(FL401_APPLICANTS, caseData.getApplicantsFL401());
            } else {
                User user = caseData.getRespondentsFL401().getUser().toBuilder().email(emailId)
                    .idamId(userId).build();
                caseData.getRespondentsFL401().setUser(user);
                caseDataUpdated.put(FL401_RESPONDENTS, caseData.getRespondentsFL401());

            }
        }
        return caseDataUpdated;
    }

    private Map<String, Object> getValuesFromPartyDetails(CaseData caseData, UUID partyId, YesOrNo isApplicant, String userId,
                                                          String emailId) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        if (YesOrNo.Yes.equals(isApplicant)) {
            for (Element<PartyDetails> partyDetails : caseData.getApplicants()) {
                if (partyId.equals(partyDetails.getId())) {
                    User user = partyDetails.getValue().getUser().toBuilder().email(emailId)
                        .idamId(userId).build();
                    partyDetails.getValue().setUser(user);
                }
            }

            caseDataUpdated.put(C100_APPLICANTS, caseData.getApplicants());
        } else {
            for (Element<PartyDetails> partyDetails : caseData.getRespondents()) {
                if (partyId.equals(partyDetails.getId())) {
                    User user = partyDetails.getValue().getUser().toBuilder().email(emailId)
                        .idamId(userId).build();
                    partyDetails.getValue().setUser(user);
                }
            }
            caseDataUpdated.put(C100_RESPONDENTS, caseData.getRespondents());
        }
        return caseDataUpdated;
    }

    private String findAccessCodeStatus(String accessCode, CaseData caseData) {
        String accessCodeStatus = INVALID;
        if (null == caseData.getCaseInvites() || caseData.getCaseInvites().isEmpty()
            || (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            && !launchDarklyClient.isFeatureEnabled(CITIZEN_ALLOW_DA_JOURNEY))) {
            return accessCodeStatus;
        }
        List<CaseInvite> matchingCaseInvite = caseData.getCaseInvites()
            .stream()
            .map(Element::getValue)
            .filter(x -> accessCode.equals(x.getAccessCode()))
            .toList();

        if (!matchingCaseInvite.isEmpty()) {
            accessCodeStatus = VALID;
            for (CaseInvite caseInvite : matchingCaseInvite) {
                if (YES.equals(caseInvite.getHasLinked())) {
                    accessCodeStatus = LINKED;
                    break;
                }
            }
        }
        return accessCodeStatus;
    }

    public String validateAccessCode(String caseId, String accessCode) {
        CaseData caseData = findAndGetCase(caseId);
        if (null == caseData) {
            return INVALID;
        }
        return findAccessCodeStatus(accessCode, caseData);
    }
}
