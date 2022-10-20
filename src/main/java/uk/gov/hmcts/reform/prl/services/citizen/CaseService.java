package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessService;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@Slf4j
@Service
public class CaseService {

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    CaseDetailsConverter caseDetailsConverter;

    @Autowired
    CaseRepository caseRepository;

    @Autowired
    IdamClient idamClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SystemUserService systemUserService;

    @Autowired
    CitizenCoreCaseDataService citizenCoreCaseDataService;

    @Autowired
    AssignCaseAccessService assignCaseAccessService;

    public CaseDetails getCase(String authToken, String s2sToken, String caseId) {
        return coreCaseDataApi.getCase(authToken, s2sToken, caseId);

    }

    public CaseDetails updateCase(CaseData caseData, String authToken, String s2sToken, String caseId, String eventId,
                                  String accessCode) {

        log.info("Inside CaseService::updateCase");
        CaseDetails caseDetails = null;
        if ("linkCase".equalsIgnoreCase(eventId)) {
            this.linkCitizenToCase(authToken, s2sToken, accessCode, caseId);
            caseDetails = coreCaseDataApi.getCase(authToken, s2sToken, caseId);

        } else {

            caseData = caseData.toBuilder()
                .manageOrders(null)
                .allegationOfHarm(null)
                .serviceOfApplicationUploadDocs(null)
                .build();

            caseDetails = this.updateCase(caseData, authToken, s2sToken, caseId, eventId);
        }

        return caseDetails;
    }

    public CaseDetails updateCase(CaseData caseData, String authToken, String s2sToken, String caseId, String eventId) {
        log.info("Inside CaseService::updateCase for citizen");

        String userId = systemUserService.getUserId(authToken);

        return coreCaseDataApi.submitEventForCitizen(
            authToken,
            s2sToken,
            userId,
            PrlAppsConstants.JURISDICTION,
            CASE_TYPE,
            String.valueOf(caseId),
            true,
            getCaseDataContent(authToken, caseData, s2sToken, userId,
                               String.valueOf(caseId), eventId
            )
        );

    }

    private CaseDataContent getCaseDataContent(String authorization, CaseData caseData, String s2sToken,
                                               String userId, String caseId, String eventId) {
        CaseDataContent.CaseDataContentBuilder builder = CaseDataContent.builder().data(caseData);
        builder.event(Event.builder().id(eventId).build())
            .eventToken(getEventTokenForUpdate(authorization, userId, eventId,
                                               caseId, s2sToken
            ));


        return builder.build();
    }

    public String getEventTokenForUpdate(String authorization, String userId, String eventId, String caseId,
                                         String s2sToken) {
        StartEventResponse res = coreCaseDataApi.startEventForCitizen(
            authorization,
            s2sToken,
            userId,
            JURISDICTION,
            CASE_TYPE,
            caseId,
            eventId
        );

        //This has to be removed
        log.info("Response of update event token: " + res.getToken());

        return nonNull(res) ? res.getToken() : null;
    }

    // end of copy

    public List<CaseData> retrieveCases(String authToken, String s2sToken, String role, String userId) {
        Map<String, String> searchCriteria = new HashMap<>();

        searchCriteria.put("sortDirection", "desc");
        searchCriteria.put("page", "1");

        return searchCasesWith(authToken, s2sToken, searchCriteria);
    }

    public List<CaseData> retrieveCases(String authToken, String s2sToken) {
        Map<String, String> searchCriteria = new HashMap<>();

        searchCriteria.put("sortDirection", "desc");
        searchCriteria.put("page", "1");

        return searchCasesLinkedToCitizen(authToken, s2sToken, searchCriteria);
    }

    private List<CaseData> searchCasesWith(String authToken, String s2sToken, Map<String, String> searchCriteria) {

        UserDetails userDetails = idamClient.getUserDetails(authToken);
        List<CaseDetails> caseDetails = new ArrayList<>();
        caseDetails.addAll(performSearch(authToken, userDetails, searchCriteria, s2sToken));
        return caseDetails
            .stream()
            .map(caseDetail -> CaseUtils.getCaseData(caseDetail, objectMapper))
            .collect(Collectors.toList());
    }

    private List<CaseData> searchCasesLinkedToCitizen(String authToken, String s2sToken,
                                                      Map<String, String> searchCriteria) {

        UserDetails userDetails = idamClient.getUserDetails(authToken);
        List<CaseDetails> caseDetails = new ArrayList<>();
        caseDetails.addAll(performSearch(authToken, userDetails, searchCriteria, s2sToken));
        return caseDetails
            .stream()
            .map(caseDetail -> CaseUtils.getCaseData(caseDetail, objectMapper))
            .collect(Collectors.toList());
    }

    private List<CaseDetails> performSearch(String authToken, UserDetails user, Map<String, String> searchCriteria,
                                            String serviceAuthToken) {
        List<CaseDetails> result;

        result = coreCaseDataApi.searchForCitizen(
            authToken,
            serviceAuthToken,
            user.getId(),
            JURISDICTION,
            CASE_TYPE,
            searchCriteria
        );

        return result;
    }

    private CaseDetails updateCaseDetails(CaseData caseData, String authToken, String s2sToken, String caseId,
                                          String eventId, UserDetails userDetails) {
        Map<String, Object> caseDataMap = caseData.toMap(objectMapper);
        Iterables.removeIf(caseDataMap.values(), Objects::isNull);
        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(
            authToken,
            s2sToken,
            userDetails.getId(),
            JURISDICTION,
            CASE_TYPE,
            caseId,
            eventId
        );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(caseDataMap)
            .build();

        return coreCaseDataApi.submitEventForCitizen(
            authToken,
            s2sToken,
            userDetails.getId(),
            JURISDICTION,
            CASE_TYPE,
            caseId,
            true,
            caseDataContent
        );
    }

    public void linkCitizenToCase(String authorisation, String s2sToken, String accessCode, String caseId) {
        UserDetails userDetails = idamClient.getUserDetails(authorisation);
        String anonymousUserToken = systemUserService.getSysUserToken();
        String userId = userDetails.getId();
        String emailId = userDetails.getEmail();

        CaseData caseData = objectMapper.convertValue(
            coreCaseDataApi.getCase(anonymousUserToken, s2sToken, caseId).getData(),
            CaseData.class
        );

        if ("Valid".equalsIgnoreCase(findAccessCodeStatus(accessCode, caseData))) {
            UUID partyId = null;
            YesOrNo isApplicant = YesOrNo.Yes;

            for (Element<CaseInvite> invite : caseData.getCaseInvites()) {
                if (accessCode.equals(invite.getValue().getAccessCode())) {
                    partyId = invite.getValue().getPartyId();
                    isApplicant = invite.getValue().getIsApplicant();
                    invite.getValue().setHasLinked("Yes");
                    invite.getValue().setInvitedUserId(userId);
                }
            }
            processUserDetailsForCase(userId, emailId, caseData, partyId, isApplicant);

            caseRepository.linkDefendant(authorisation, anonymousUserToken, s2sToken, caseId, caseData);
        }
    }

    private void processUserDetailsForCase(String userId, String emailId, CaseData caseData, UUID partyId,
                                           YesOrNo isApplicant) {
        //Assumption is for C100 case PartyDetails will be part of list
        // and will always contain the partyId
        // whereas FL401 will have only one party details without any partyId
        User user = User.builder().email(emailId)
            .idamId(userId).build();
        if (partyId != null) {
            if (YesOrNo.Yes.equals(isApplicant)) {
                for (Element<PartyDetails> partyDetails : caseData.getApplicants()) {
                    if (partyId.equals(partyDetails.getId())) {
                        partyDetails.getValue().setUser(user);
                    }
                }
            } else {
                for (Element<PartyDetails> partyDetails : caseData.getRespondents()) {
                    if (partyId.equals(partyDetails.getId())) {
                        partyDetails.getValue().setUser(user);
                    }
                }
            }
        } else {
            if (YesOrNo.Yes.equals(isApplicant)) {
                caseData.getApplicantsFL401().setUser(user);
            } else {
                caseData.getRespondentsFL401().setUser(user);
            }
        }
    }

    public String validateAccessCode(String authorisation, String s2sToken, String caseId, String accessCode) {
        CaseData caseData = objectMapper.convertValue(
            coreCaseDataApi.getCase(authorisation, s2sToken, caseId).getData(),
            CaseData.class
        );
        return findAccessCodeStatus(accessCode, caseData);
    }

    private String findAccessCodeStatus(String accessCode, CaseData caseData) {
        String accessCodeStatus = "Invalid";
        List<CaseInvite> matchingCaseInvite = caseData.getCaseInvites()
            .stream()
            .map(Element::getValue)
            .filter(x -> accessCode.equals(x.getAccessCode()))
            .collect(Collectors.toList());

        if (matchingCaseInvite.size() > 0) {
            accessCodeStatus = "Valid";
            for (CaseInvite caseInvite : matchingCaseInvite) {
                if ("Yes".equals(caseInvite.getHasLinked())) {
                    accessCodeStatus = "Linked";
                }
            }
        }
        return accessCodeStatus;
    }

    public CaseDetails createCase(CaseData caseData, String authToken, String s2sToken) {
        UserDetails userDetails = idamClient.getUserDetails(authToken);

        return createCase(caseData, authToken, s2sToken, userDetails);
    }

    private CaseDetails createCase(CaseData caseData, String authToken, String s2sToken, UserDetails userDetails) {
        return coreCaseDataApi.submitForCitizen(
            authToken,
            s2sToken,
            userDetails.getId(),
            JURISDICTION,
            CASE_TYPE,
            true,
            getCaseDataContentForCreateCase(authToken, s2sToken, caseData, userDetails.getId())
        );
    }

    private CaseDataContent getCaseDataContentForCreateCase(String authorization, String s2sToken, CaseData caseData,
                                                            String userId) {
        Map<String, Object> caseDataMap = caseData.toMap(objectMapper);
        Iterables.removeIf(caseDataMap.values(), Objects::isNull);
        return CaseDataContent.builder()
            .data(caseDataMap)
            .event(Event.builder().id(PrlAppsConstants.CITIZEN_PRL_CREATE_EVENT).build())
            .eventToken(getEventToken(authorization, s2sToken, userId, PrlAppsConstants.CITIZEN_PRL_CREATE_EVENT))
            .build();
    }

    public String getEventToken(String authorization, String s2sToken, String userId, String eventId) {
        StartEventResponse res = coreCaseDataApi.startForCitizen(
            authorization,
            s2sToken,
            userId,
            JURISDICTION,
            CASE_TYPE,
            eventId
        );

        return nonNull(res) ? res.getToken() : null;
    }
}
