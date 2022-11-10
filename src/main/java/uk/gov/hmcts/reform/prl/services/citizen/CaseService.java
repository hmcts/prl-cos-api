package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataMapper;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;


@Slf4j
@Service
public class CaseService {

    public static final String LINK_CASE = "linkCase";
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
    CaseDataMapper caseDataMapper;

    @Autowired
    CitizenEmailService citizenEmailService;

    public CaseDetails updateCase(CaseData caseData, String authToken, String s2sToken,
                                  String caseId, String eventId, String accessCode) throws JsonProcessingException {

        if (LINK_CASE.equalsIgnoreCase(eventId) && null != accessCode) {
            linkCitizenToCase(authToken, s2sToken, accessCode, caseId);
            return caseRepository.getCase(authToken, caseId);
        }
        if (CITIZEN_CASE_SUBMIT.getValue().equalsIgnoreCase(eventId)) {
            citizenEmailService.sendCitizenCaseSubmissionEmail(authToken, caseData);
            CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData);
            return caseRepository.updateCase(authToken, caseId, updatedCaseData, CaseEvent.fromValue(eventId));
        }
        return caseRepository.updateCase(authToken, caseId, caseData, CaseEvent.fromValue(eventId));
    }

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

            caseRepository.linkDefendant(authorisation, anonymousUserToken, caseId, caseData);
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

    public CaseDetails getCase(String authToken, String caseId) {
        return caseRepository.getCase(authToken, caseId);
    }

    public CaseDetails createCase(CaseData caseData, String authToken) {
        return caseRepository.createCase(authToken, caseData);
    }

}
