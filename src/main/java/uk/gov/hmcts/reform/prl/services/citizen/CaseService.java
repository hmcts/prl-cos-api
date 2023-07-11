package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.UpdateCaseData;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT_WITH_HWF;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseService {

    public static final String LINK_CASE = "linkCase";
    public static final String INVALID = "Invalid";
    @Autowired
    private final CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private final CaseRepository caseRepository;

    private final IdamClient idamClient;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final SystemUserService systemUserService;

    @Autowired
    private final CaseDataMapper caseDataMapper;

    private final CcdCoreCaseDataService coreCaseDataService;

    private final NoticeOfChangePartiesService noticeOfChangePartiesService;
    private static final String INVALID_CLIENT = "Invalid Client";

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

            CaseData updatedCaseData = caseDataMapper
                .buildUpdatedCaseData(caseData.toBuilder().userInfo(wrapElements(userInfo))
                                          .courtName(C100_DEFAULT_COURT_NAME)
                                          .build());
            return caseRepository.updateCase(authToken, caseId, updatedCaseData, CaseEvent.fromValue(eventId));
        }

        return caseRepository.updateCase(authToken, caseId, caseData, CaseEvent.fromValue(eventId));
    }

    public CaseDetails updateCaseDetails(String authToken,
                                         String caseId, String eventId, UpdateCaseData updateCaseData) {

        CaseDetails caseDetails = caseRepository.getCase(authToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        PartyDetails partyDetails = updateCaseData.getPartyDetails();
        PartyEnum partyType = updateCaseData.getPartyType();
        if (null != partyDetails.getUser()) {
            if (C100_CASE_TYPE.equalsIgnoreCase(updateCaseData.getCaseTypeOfApplication())) {
                if (CaseEvent.CITIZEN_STATEMENT_OF_SERVICE.getValue().equalsIgnoreCase(eventId)) {
                    eventId = CaseEvent.CITIZEN_INTERNAL_CASE_UPDATE.getValue();
                    handleCitizenStatementOfService(caseData, partyDetails, partyType);
                } else {
                    updatingPartyDetailsCa(caseData, partyDetails, partyType);
                }
            } else {
                caseData = getFlCaseData(caseData, partyDetails, partyType);
            }
            caseData = generateAnswersForNoc(caseData);
            return caseRepository.updateCase(authToken, caseId, caseData, CaseEvent.fromValue(eventId));
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private void handleCitizenStatementOfService(CaseData caseData, PartyDetails partyDetails, PartyEnum partyType) {
        if (PartyEnum.applicant.equals(partyType)) {
            List<Element<PartyDetails>> applicants = caseData.getApplicants();
            applicants.stream()
                .filter(party -> Objects.equals(party.getValue().getUser().getIdamId(), partyDetails.getUser().getIdamId()))
                .findFirst()
                .ifPresent(party ->
                               applicants.set(applicants.indexOf(party), element(party.getId(), partyDetails))
                );
        } else if (PartyEnum.respondent.equals(partyType)) {
            List<Element<PartyDetails>> respondents = caseData.getRespondents();
            respondents.stream()
                .filter(party -> Objects.equals(party.getValue().getUser().getIdamId(), partyDetails.getUser().getIdamId()))
                .findFirst()
                .ifPresent(party ->
                               respondents.set(respondents.indexOf(party), element(party.getId(), partyDetails))
                );
        }
    }
    private CaseData generateAnswersForNoc(CaseData caseData) {
        Map<String, Object> caseDataMap = caseData.toMap(objectMapper);
        if (isNotEmpty(caseDataMap)) {
            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                caseDataMap.putAll(noticeOfChangePartiesService.generate(caseData, CARESPONDENT));
                caseDataMap.putAll(noticeOfChangePartiesService.generate(caseData, CAAPPLICANT));
            } else {
                caseDataMap.putAll(noticeOfChangePartiesService.generate(caseData, DARESPONDENT));
                caseDataMap.putAll(noticeOfChangePartiesService.generate(caseData, DAAPPLICANT));
            }
        }
        caseData = objectMapper.convertValue(caseDataMap, CaseData.class);
        return caseData;
    }

    private static CaseData getFlCaseData(CaseData caseData, PartyDetails partyDetails, PartyEnum partyType) {
        if (PartyEnum.applicant.equals(partyType)) {
            if (partyDetails.getUser().getIdamId().equalsIgnoreCase(caseData.getApplicantsFL401().getUser().getIdamId())) {
                caseData = caseData.toBuilder().applicantsFL401(partyDetails).build();
            }
        } else {
            if (partyDetails.getUser().getIdamId().equalsIgnoreCase(caseData.getRespondentsFL401().getUser().getIdamId())) {
                caseData = caseData.toBuilder().respondentsFL401(partyDetails).build();
            }
        }
        return caseData;
    }

    private static void updatingPartyDetailsCa(CaseData caseData, PartyDetails partyDetails, PartyEnum partyType) {
        log.info("** PartyDetails ** {}", partyDetails);
        if (PartyEnum.applicant.equals(partyType)) {
            List<Element<PartyDetails>> applicants = caseData.getApplicants();
            applicants.stream()
                .filter(party -> Objects.equals(party.getValue().getUser().getIdamId(), partyDetails.getUser().getIdamId()))
                .findFirst()
                .ifPresent(party ->
                    applicants.set(applicants.indexOf(party), element(party.getId(), partyDetails))
                );
        } else if (PartyEnum.respondent.equals(partyType)) {
            List<Element<PartyDetails>> respondents = caseData.getRespondents();
            respondents.stream()
                .filter(party -> Objects.equals(party.getValue().getUser().getIdamId(), partyDetails.getUser().getIdamId()))
                .findFirst()
                .ifPresent(party ->
                    respondents.set(respondents.indexOf(party), element(party.getId(), partyDetails))
                );
        }
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

        CaseData currentCaseData = objectMapper.convertValue(
            coreCaseDataApi.getCase(anonymousUserToken, s2sToken, caseId).getData(),
            CaseData.class
        );
        log.info("caseId {}", caseId);
        if ("Valid".equalsIgnoreCase(findAccessCodeStatus(accessCode, currentCaseData))) {
            UUID partyId = null;
            YesOrNo isApplicant = YesOrNo.Yes;

            String systemAuthorisation = systemUserService.getSysUserToken();
            String systemUpdateUserId = systemUserService.getUserId(systemAuthorisation);
            EventRequestData eventRequestData = coreCaseDataService.eventRequest(
                CaseEvent.LINK_CITIZEN,
                systemUpdateUserId
            );
            StartEventResponse startEventResponse =
                coreCaseDataService.startUpdate(
                    systemAuthorisation,
                    eventRequestData,
                    caseId,
                    true
                );

            CaseData caseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(
                startEventResponse,
                objectMapper
            );

            for (Element<CaseInvite> invite : caseData.getCaseInvites()) {
                if (accessCode.equals(invite.getValue().getAccessCode())) {
                    partyId = invite.getValue().getPartyId();
                    isApplicant = invite.getValue().getIsApplicant();
                    invite.getValue().setHasLinked("Yes");
                    invite.getValue().setInvitedUserId(userId);
                }
            }

            processUserDetailsForCase(userId, emailId, caseData, partyId, isApplicant);

            caseRepository.linkDefendant(authorisation, anonymousUserToken, caseId, caseData, startEventResponse);
        }
    }

    private void processUserDetailsForCase(String userId, String emailId, CaseData caseData, UUID partyId,
                                           YesOrNo isApplicant) {
        //Assumption is for C100 case PartyDetails will be part of list
        // and will always contain the partyId
        // whereas FL401 will have only one party details without any partyId
        if (partyId != null) {
            getValuesFromPartyDetails(caseData, partyId, isApplicant, userId, emailId);
        } else {
            if (YesOrNo.Yes.equals(isApplicant)) {
                User user = caseData.getApplicantsFL401().getUser().toBuilder().email(emailId)
                    .idamId(userId).build();
                caseData.getApplicantsFL401().setUser(user);
            } else {
                User user = caseData.getRespondentsFL401().getUser().toBuilder().email(emailId)
                    .idamId(userId).build();
                caseData.getRespondentsFL401().setUser(user);
            }
        }
    }

    private void getValuesFromPartyDetails(CaseData caseData, UUID partyId, YesOrNo isApplicant, String userId, String emailId) {
        if (YesOrNo.Yes.equals(isApplicant)) {
            for (Element<PartyDetails> partyDetails : caseData.getApplicants()) {
                if (partyId.equals(partyDetails.getId())) {
                    User user = partyDetails.getValue().getUser().toBuilder().email(emailId)
                        .idamId(userId).build();
                    partyDetails.getValue().setUser(user);
                }
            }
        } else {
            for (Element<PartyDetails> partyDetails : caseData.getRespondents()) {
                if (partyId.equals(partyDetails.getId())) {
                    User user = partyDetails.getValue().getUser().toBuilder().email(emailId)
                        .idamId(userId).build();
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
        if (null == caseData) {
            return INVALID;
        }
        return findAccessCodeStatus(accessCode, caseData);
    }

    private String findAccessCodeStatus(String accessCode, CaseData caseData) {
        String accessCodeStatus = INVALID;
        if (null == caseData.getCaseInvites() || caseData.getCaseInvites().isEmpty()) {
            return accessCodeStatus;
        }
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

    public CaseDetails withdrawCase(CaseData caseData, String caseId, String authToken) {

        WithdrawApplication withDrawApplicationData = caseData.getWithDrawApplicationData();
        Optional<YesOrNo> withdrawApplication = ofNullable(withDrawApplicationData.getWithDrawApplication());
        CaseDetails caseDetails = getCase(authToken, caseId);
        CaseData updatedCaseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder().id(caseDetails.getId()).build();

        if ((withdrawApplication.isPresent() && Yes.equals(withdrawApplication.get()))) {
            updatedCaseData = updatedCaseData.toBuilder()
                .withDrawApplicationData(withDrawApplicationData)
                .build();
        }

        return caseRepository.updateCase(authToken, caseId, updatedCaseData, CaseEvent.CITIZEN_CASE_WITHDRAW);
    }

}
