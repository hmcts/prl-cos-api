package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PENDING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WITHDRAWN_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WITHDRAW_REQUEST_FIELD;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT_WITH_HWF;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;


@Slf4j
@Service
public class CaseService {

    public static final String LINK_CASE = "linkCase";
    public static final String CITIZEN_INTERNAL_CASE_UPDATE = "citizen-internal-case-update";
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

    @Autowired
    AllTabServiceImpl allTabsService;

    @Autowired
    CourtFinderService courtLocatorService;

    @Autowired
    CaseEventService caseEventService;

    public CaseDetails updateCase(CaseData caseData, String authToken, String s2sToken,
                                  String caseId, String eventId, String accessCode) throws JsonProcessingException {

        if (LINK_CASE.equalsIgnoreCase(eventId) && null != accessCode) {
            linkCitizenToCase(authToken, s2sToken, accessCode, caseId);
            return caseRepository.getCase(authToken, caseId);
        }
        if (CITIZEN_CASE_SUBMIT.getValue().equalsIgnoreCase(eventId)
            || CITIZEN_CASE_SUBMIT_WITH_HWF.getValue().equalsIgnoreCase(eventId)) {
            UserDetails userDetails = idamClient.getUserDetails(authToken);
            UserInfo userInfo = UserInfo
                .builder()
                .idamId(userDetails.getId())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname().orElse(null))
                .emailAddress(userDetails.getEmail())
                .build();

            Court closestChildArrangementsCourt = buildCourt(caseData);

            CaseData updatedCaseData = caseDataMapper
                .buildUpdatedCaseData(caseData.toBuilder().userInfo(wrapElements(userInfo))
                                          .courtName((closestChildArrangementsCourt != null)
                                                         ? closestChildArrangementsCourt.getCourtName() : "No Court Fetched")
                                          .build());
            return caseRepository.updateCase(authToken, caseId, updatedCaseData, CaseEvent.fromValue(eventId));
        }
        return caseRepository.updateCase(authToken, caseId, caseData, CaseEvent.fromValue(eventId));
    }

    private Court buildCourt(CaseData caseData) {
        try {
            return courtLocatorService.getNearestFamilyCourt(caseData);
        } catch (NotFoundException e) {
            log.error("Cannot find court");
        }
        return null;
    }

    public List<CaseData> retrieveCases(String authToken, String s2sToken) {
        Map<String, String> searchCriteria = new HashMap<>();

        searchCriteria.put("sortDirection", "desc");
        searchCriteria.put("page", "1");

        return searchCasesLinkedToUser(authToken, s2sToken, searchCriteria);
    }

    private List<CaseData> searchCasesLinkedToUser(String authToken, String s2sToken,
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
        log.info("caseId {}", caseId);
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
            getValuesFromPartyDetails(caseData, partyId, isApplicant, user);
        } else {
            if (YesOrNo.Yes.equals(isApplicant)) {
                caseData.getApplicantsFL401().setUser(user);
            } else {
                caseData.getRespondentsFL401().setUser(user);
            }
        }
    }

    private void getValuesFromPartyDetails(CaseData caseData, UUID partyId, YesOrNo isApplicant, User user) {
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

        if (!matchingCaseInvite.isEmpty()) {
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

    public CaseDetails withdrawCase(CaseData caseData, String caseId, String authToken, String s2sToken) throws JsonProcessingException {

        List<CaseEventDetail> eventsForCase = caseEventService.findEventsForCase(caseId);
        log.info("eventsForCase " + eventsForCase);
        Optional<String> previousState = eventsForCase.stream().map(CaseEventDetail::getStateId)
            .filter(CaseUtils::getPreviousState).findFirst();
        log.info("previousState " + previousState);
        WithdrawApplication withDrawApplicationData = caseData.getWithDrawApplicationData();
        log.info("withDrawApplicationData " + withDrawApplicationData);
        Optional<YesOrNo> withdrawApplication = ofNullable(withDrawApplicationData.getWithDrawApplication());
        Map<String, Object> caseDetailsMap = getCase(authToken, caseId).getData();
        log.info("caseDetailsMap " + caseDetailsMap);
        CaseData updatedCaseData = CaseUtils.getCaseData(CaseDetails.builder().data(caseDetailsMap).build(), objectMapper);
        log.info("updatedCaseData " + updatedCaseData);
        if ((withdrawApplication.isPresent() && Yes.equals(withdrawApplication.get()))) {
            if (previousState.isPresent()
                && !CaseUtils.WITHDRAW_STATE_LIST.contains(previousState.get())) {
                caseDetailsMap.put(WITHDRAW_REQUEST_FIELD, PENDING);
                log.info("Case is updated as WithdrawRequestSent");
                //REVIEW IF ANY EMAILS TO SEND
                //sendWithdrawEmails(caseData, userDetails, caseDetails);
            } else {
                if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                    citizenEmailService.sendCitizenCaseWithdrawalEmail(authToken, caseId, updatedCaseData);
                    // Refreshing the page in the same event. Hence no external event call needed.
                    // Getting the tab fields and add it to the casedetails..
                    Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);
                    caseDetailsMap.putAll(allTabsFields);
                }
                caseDetailsMap.put(STATE_FIELD, WITHDRAWN_STATE);
            }
        }
        log.info("case withdrawn, updating case");
        return updateCase(updatedCaseData, authToken, s2sToken, caseId, CaseEvent.CITIZEN_CASE_UPDATE.getValue(), null);

    }

}
