package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.UpdateCaseData;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildApplicantDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildRespondentDetailsElements;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocuments;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StatementOfService;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WITHDRAWN_STATE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT_WITH_HWF;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;



@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseService {
    public static final String LINK_CASE = "linkCase";
    public static final String INVALID = "Invalid";
    public static final String VALID = "Valid";
    public static final String LINKED = "Linked";
    public static final String YES = "Yes";
    public static final String CASE_INVITES = "caseInvites";
    public static final String CASE_STATUS = "caseStatus";
    public static final String WITHDRAW_APPLICATION_DATA = "withDrawApplicationData";
    private final CoreCaseDataApi coreCaseDataApi;

    private final CaseRepository caseRepository;
    private final IdamClient idamClient;
    private final ObjectMapper objectMapper;
    private final SystemUserService systemUserService;
    private final CaseDataMapper caseDataMapper;
    private final CcdCoreCaseDataService coreCaseDataService;
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;
    private final CaseSummaryTabService caseSummaryTab;
    private final RoleAssignmentService roleAssignmentService;
    private final UserService userService;
    private static final String INVALID_CLIENT = "Invalid Client";

    public CaseDetails updateCase(CaseData caseData, String authToken, String s2sToken,
                                  String caseId, String eventId, String accessCode) throws JsonProcessingException {
        if (LINK_CASE.equalsIgnoreCase(eventId) && null != accessCode) {
            linkCitizenToCase(authToken, s2sToken, caseId, accessCode);
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
                .buildUpdatedCaseData(caseData);
            updatedCaseData = updatedCaseData.toBuilder()
                .userInfo(wrapElements(userInfo))
                .courtName(C100_DEFAULT_COURT_NAME)
                .taskListVersion(TASK_LIST_VERSION_V2)
                .build();
            return caseRepository.updateCase(authToken, caseId, updatedCaseData, CaseEvent.fromValue(eventId));
        }
        if (CITIZEN_CASE_UPDATE.getValue().equalsIgnoreCase(eventId)
            && isEmpty(caseData.getApplicantCaseName())) {
            caseData = caseData.toBuilder()
                .applicantCaseName(buildApplicantAndRespondentForCaseName(caseData))
                .build();
        }

        return caseRepository.updateCase(authToken, caseId, caseData, CaseEvent.fromValue(eventId));
    }

    public CaseDetails updateCaseDetails(String authToken,
                                         String caseId, String eventId, UpdateCaseData updateCaseData) {

        CaseDetails caseDetails = caseRepository.getCase(authToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        PartyDetails partyDetails = updateCaseData.getPartyDetails();
        PartyEnum partyType = updateCaseData.getPartyType();
        if (CaseEvent.CITIZEN_STATEMENT_OF_SERVICE.getValue().equalsIgnoreCase(eventId)) {
            eventId = CaseEvent.CITIZEN_INTERNAL_CASE_UPDATE.getValue();
            handleCitizenStatementOfService(caseData, partyDetails, partyType);
        }
        if (null != partyDetails.getUser()) {
            if (C100_CASE_TYPE.equalsIgnoreCase(updateCaseData.getCaseTypeOfApplication())) {
                caseData = updatingPartyDetailsCa(caseData, partyDetails, partyType);
            } else {
                caseData = getFlCaseData(caseData, partyDetails, partyType);
            }
            caseData = generateAnswersForNoc(caseData);
            return caseRepository.updateCase(authToken, caseId, caseData, CaseEvent.fromValue(eventId));
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    public String buildApplicantAndRespondentForCaseName(CaseData caseData) throws JsonProcessingException {
        C100RebuildData c100RebuildData = caseData.getC100RebuildData();
        ObjectMapper mapper = new ObjectMapper();
        C100RebuildApplicantDetailsElements c100RebuildApplicantDetailsElements = null;
        C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements = null;
        if (null != c100RebuildData) {
            if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildApplicantDetails())) {
                c100RebuildApplicantDetailsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildApplicantDetails(), C100RebuildApplicantDetailsElements.class);
            }

            if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildRespondentDetails())) {
                c100RebuildRespondentDetailsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildRespondentDetails(), C100RebuildRespondentDetailsElements.class);
            }
        }
        return buildCaseName(c100RebuildApplicantDetailsElements, c100RebuildRespondentDetailsElements);
    }


    private String buildCaseName(C100RebuildApplicantDetailsElements c100RebuildApplicantDetailsElements,
                                 C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements) {
        String caseName = null;
        if (null != c100RebuildApplicantDetailsElements
            && null != c100RebuildRespondentDetailsElements.getRespondentDetails()) {
            caseName = c100RebuildApplicantDetailsElements.getApplicants().get(0).getApplicantLastName() + " V "
                + c100RebuildRespondentDetailsElements.getRespondentDetails().get(0).getLastName();
        }

        return caseName;
    }

    private CaseData handleCitizenStatementOfService(CaseData caseData,PartyDetails partyDetails, PartyEnum partyType) {
        CitizenSos citizenSos = partyDetails.getCitizenSosObject();
        StmtOfServiceAddRecipient sosObject = StmtOfServiceAddRecipient.builder()
            .citizenPartiesServedList(getPartyNames(citizenSos.getPartiesServed(), caseData, partyType))
            .citizenPartiesServedDate(citizenSos.getPartiesServedDate())
            .citizenSosDocs(getSosDocs(
                citizenSos.getCitizenSosDocs(),
                caseData.getDocumentManagementDetails().getCitizenUploadQuarantineDocsList()
            ))
            .build();
        if (caseData.getStatementOfService().getStmtOfServiceAddRecipient() != null) {
            List<Element<StmtOfServiceAddRecipient>> sosList = caseData.getStatementOfService().getStmtOfServiceAddRecipient();
            List<Element<StmtOfServiceAddRecipient>> mutableList = new ArrayList<>(sosList);
            mutableList.add(element(sosObject));
            caseData.setStatementOfService(StatementOfService.builder()
                                               .stmtOfServiceAddRecipient(mutableList)
                                               .build());
        } else {
            caseData
                .setStatementOfService(StatementOfService.builder()
                                           .stmtOfServiceAddRecipient(List.of(element(sosObject)))
                                           .build());
        }
        return caseData;
    }

    private String getPartyNames(String parties, CaseData caseData, PartyEnum partyType) {
        List<String> servedParties = new ArrayList<>();
        if (parties != null) {
            for (String partyId : parties.split(",")) {
                if (PartyEnum.applicant.equals(partyType)) {
                    servedParties.add(getPartyNameById(partyId, caseData.getApplicants()));
                } else if (PartyEnum.respondent.equals(partyType)) {
                    servedParties.add(getPartyNameById(partyId, caseData.getRespondents()));
                }
            }
        }
        return servedParties.toString();
    }

    private String getPartyNameById(String partyId, List<Element<PartyDetails>> parties) {
        for (Element<PartyDetails> party : parties) {
            if (party.getId().toString().equalsIgnoreCase(partyId)) {
                return party.getValue().getLabelForDynamicList();
            }
        }
        return "";
    }

    private List<Document> getSosDocs(List<String> docIdList, List<Element<UploadedDocuments>> citizenDocs) {
        List<Document> docs = new ArrayList<>();
        if (citizenDocs != null) {
            citizenDocs.forEach(doc -> {
                if (docIdList.contains(doc.getId().toString())) {
                    docs.add(doc.getValue().getCitizenDocument());
                }
            });
        }
        return docs;
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

    private static CaseData updatingPartyDetailsCa(CaseData caseData, PartyDetails partyDetails, PartyEnum partyType) {
        if (PartyEnum.applicant.equals(partyType)) {
            List<Element<PartyDetails>> applicants = new ArrayList<>(caseData.getApplicants());
            applicants.stream()
                .filter(party -> Objects.equals(
                    party.getValue().getUser().getIdamId(),
                    partyDetails.getUser().getIdamId()
                ))
                .findFirst()
                .ifPresent(party ->
                               applicants.set(applicants.indexOf(party), element(party.getId(), partyDetails))
                );
            caseData = caseData.toBuilder().applicants(applicants).build();
        } else if (PartyEnum.respondent.equals(partyType)) {
            List<Element<PartyDetails>> respondents = new ArrayList<>(caseData.getRespondents());
            respondents.stream()
                .filter(party -> Objects.equals(
                    party.getValue().getUser().getIdamId(),
                    partyDetails.getUser().getIdamId()
                ))
                .findFirst()
                .ifPresent(party ->
                               respondents.set(respondents.indexOf(party), element(party.getId(), partyDetails))
                );
            caseData = caseData.toBuilder().respondents(respondents).build();
        }
        return caseData;
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
            .toList();
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

    public void linkCitizenToCase(String authorisation, String s2sToken, String caseId, String accessCode) {
        String anonymousUserToken = systemUserService.getSysUserToken();
        CaseData currentCaseData = objectMapper.convertValue(
            coreCaseDataApi.getCase(anonymousUserToken, s2sToken, caseId).getData(),
            CaseData.class
        );

        if (VALID.equalsIgnoreCase(findAccessCodeStatus(accessCode, currentCaseData))) {
            UUID partyId = null;
            YesOrNo isApplicant = YesOrNo.Yes;

            String systemUpdateUserId = systemUserService.getUserId(anonymousUserToken);
            EventRequestData eventRequestData = coreCaseDataService.eventRequest(
                CaseEvent.LINK_CITIZEN,
                systemUpdateUserId
            );
            StartEventResponse startEventResponse =
                coreCaseDataService.startUpdate(
                    anonymousUserToken,
                    eventRequestData,
                    caseId,
                    true
                );

            CaseData caseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(
                startEventResponse,
                objectMapper
            );
            Map<String, Object> caseDataUpdated = new HashMap<>();
            UserDetails userDetails = idamClient.getUserDetails(authorisation);
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

            processUserDetailsForCase(userId, emailId, caseData, partyId, isApplicant, caseDataUpdated);
            caseRepository.linkDefendant(
                authorisation,
                anonymousUserToken,
                caseId,
                eventRequestData,
                startEventResponse,
                caseDataUpdated
            );
        }
    }

    private void processUserDetailsForCase(String userId, String emailId, CaseData caseData, UUID partyId,
                                           YesOrNo isApplicant, Map<String, Object> caseDataUpdated) {
        //Assumption is for C100 case PartyDetails will be part of list
        // and will always contain the partyId
        // whereas FL401 will have only one party details without any partyId
        if (partyId != null) {
            getValuesFromPartyDetails(caseData, partyId, isApplicant, userId, emailId, caseDataUpdated);
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
    }

    private void getValuesFromPartyDetails(CaseData caseData, UUID partyId, YesOrNo isApplicant, String userId,
                                           String emailId, Map<String, Object> caseDataUpdated) {
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
            .toList();

        if (!matchingCaseInvite.isEmpty()) {
            accessCodeStatus = VALID;
            for (CaseInvite caseInvite : matchingCaseInvite) {
                if (YES.equals(caseInvite.getHasLinked())) {
                    accessCodeStatus = LINKED;
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

    public CaseDetails withdrawCase(CaseData oldCaseData, String caseId, String authToken) {
        CaseEvent caseEvent = CaseEvent.CITIZEN_CASE_WITHDRAW;
        UserDetails userDetails = idamClient.getUserDetails(authToken);
        EventRequestData eventRequestData = coreCaseDataService.eventRequest(caseEvent, userDetails.getId());
        StartEventResponse startEventResponse =
            coreCaseDataService.startUpdate(
                authToken,
                eventRequestData,
                caseId,
                false
            );
        Map<String, Object> updatedCaseData = startEventResponse.getCaseDetails().getData();

        WithdrawApplication withDrawApplicationData = oldCaseData.getWithDrawApplicationData();
        Optional<YesOrNo> withdrawApplication = ofNullable(withDrawApplicationData.getWithDrawApplication());
        if ((withdrawApplication.isPresent() && Yes.equals(withdrawApplication.get()))) {
            updatedCaseData.put(WITHDRAW_APPLICATION_DATA, withDrawApplicationData);
            updatedCaseData.put(STATE, WITHDRAWN_STATE);
            updatedCaseData.put(CASE_STATUS, CaseStatus.builder().state(State.CASE_WITHDRAWN.getLabel()).build());
        }
        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContent(
            startEventResponse,
            updatedCaseData
        );
        return coreCaseDataService.submitUpdate(
            authToken,
            eventRequestData,
            caseDataContent,
            caseId,
            false
        );
    }

    public Map<String, String> fetchIdamAmRoles(String authorisation, String emailId) {
        return roleAssignmentService.fetchIdamAmRoles(authorisation, emailId);
    }

    public CitizenDocumentsManagement getAllCitizenDocumentsOrders(String authToken,
                                                                   CaseData caseData) {
        UserDetails userDetails = userService.getUserDetails(authToken);

        return CitizenDocumentsManagement.builder()
            .citizenDocuments(getCitizenDocuments(userDetails, caseData))
            .citizenOrders(getCitizenOrders(userDetails, caseData))
            .citizenApplicationPacks(getCitizenApplicationPacks(userDetails, caseData))
            .build();
    }

    private List<CitizenDocuments> getCitizenApplicationPacks(UserDetails userDetails,
                                                              CaseData caseData) {
        List<CitizenDocuments> citizenDocuments = new ArrayList<>();
        String partyId = findPartyId(caseData, userDetails);
        if (partyId != null) {
            citizenDocuments.addAll(fetchSoaPacksForParty(caseData, partyId));
        }
        return citizenDocuments;

    }

    private List<CitizenDocuments> fetchSoaPacksForParty(CaseData caseData, String partyId) {
        List<CitizenDocuments> citizenDocuments = new ArrayList<>();
        LocalDateTime uploadedDateTime = null;
        if (null != caseData.getFinalServedApplicationDetailsList()) {
            Optional<EmailNotificationDetails> emailNotificationDetails = caseData.getFinalServedApplicationDetailsList().stream()
                .map(Element::getValue)
                .filter(e -> e.getModeOfService().equals("By email"))
                .map(ServedApplicationDetails::getEmailNotificationDetails)
                .flatMap(Collection::stream)
                .toList()
                .stream().map(
                    Element::getValue
                )
                .filter(emailNotificationDetails1 ->
                            emailNotificationDetails1.getPartyIds() != null && emailNotificationDetails1.getPartyIds().contains(
                                partyId))
                .sorted(comparing(EmailNotificationDetails::getTimeStamp).reversed())
                .findFirst();
            if (emailNotificationDetails.isPresent()) {
                EmailNotificationDetails emailNotificationDetails1 = emailNotificationDetails.get();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
                uploadedDateTime = LocalDateTime.parse(
                    emailNotificationDetails1.getTimeStamp(),
                    formatter
                );
                citizenDocuments.add(
                    CitizenDocuments.builder()
                        .partyId(emailNotificationDetails1.getPartyIds())
                        .servedParty(emailNotificationDetails1.getServedParty())
                        .uploadedDate(uploadedDateTime)
                        .applicantSoaPack(
                            emailNotificationDetails1.getDocs().stream()
                                .map(Element::getValue)
                                .collect(Collectors.toList())
                        )
                        .respondentSoaPack(caseData.getServiceOfApplication().getUnServedRespondentPack().getPackDocument().stream()
                                               .map(Element::getValue)
                                               .collect(Collectors.toList()))

                        .build()
                );
            }

            Optional<BulkPrintDetails> bulkPrintNotificationDetails = caseData.getFinalServedApplicationDetailsList().stream()
                .map(Element::getValue)
                .filter(e -> e.getModeOfService().equals("By post"))
                .map(ServedApplicationDetails::getBulkPrintDetails)
                .flatMap(Collection::stream)
                .toList()
                .stream().map(
                    Element::getValue
                )
                .filter(emailNotificationDetails1 ->
                            emailNotificationDetails1.getPartyIds() != null
                                && emailNotificationDetails1.getPartyIds().contains(partyId))
                .sorted(comparing(BulkPrintDetails::getTimeStamp).reversed())
                .findFirst();

            if (bulkPrintNotificationDetails.isPresent()) {
                BulkPrintDetails bulkPrintNotificationDetails1 = bulkPrintNotificationDetails.get();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(
                    bulkPrintNotificationDetails1.getTimeStamp(),
                    formatter
                );
                if (uploadedDateTime.isBefore(dateTime)) {
                    citizenDocuments = new ArrayList<>();
                    citizenDocuments.add(
                        CitizenDocuments.builder()
                            .partyId(bulkPrintNotificationDetails1.getPartyIds())
                            .servedParty(bulkPrintNotificationDetails1.getServedParty())
                            .uploadedDate(dateTime)
                            .applicantSoaPack(
                                bulkPrintNotificationDetails1.getPrintDocs().stream()
                                    .map(Element::getValue)
                                    .collect(Collectors.toList())
                            )
                            .respondentSoaPack(caseData.getServiceOfApplication().getUnServedApplicantPack().getPackDocument().stream()
                                                   .map(Element::getValue)
                                                   .collect(Collectors.toList()))

                            .build()
                    );
                }

            }

        }
        return citizenDocuments;
    }

    private List<CitizenDocuments> getCitizenDocuments(UserDetails userDetails,
                                                       CaseData caseData) {
        List<CitizenDocuments> citizenDocuments = new ArrayList<>();
        List<String> unReturnedCategoriesForUI = List.of(
            "safeguardingLetter",
            "section37Report",
            "section7Report",
            "16aRiskAssessment",
            "guardianReport",
            "specialGuardianshipReport",
            "otherDocs",
            "sec37Report",
            "localAuthorityOtherDoc",
            "emailsToCourtToRequestHearingsAdjourned",
            "publicFundingCertificates",
            "noticesOfActingDischarge",
            "requestForFASFormsToBeChanged",
            "witnessAvailability",
            "lettersOfComplaint",
            "SPIPReferralRequests",
            "homeOfficeDWPResponses",
            "internalCorrespondence",
            "importantInfoAboutAddressAndContact",
            "privacyNotice",
            "specialMeasures",
            "anyOtherDoc",
            "noticeOfHearing",
            "caseSummary"
        );

        if (null != caseData.getReviewDocuments()) {
            //add solicitor uploaded docs
            citizenDocuments.addAll(addCitizenDocuments(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab()));
            //add cafacss uploaded docs
            citizenDocuments.addAll(addCitizenDocuments(caseData.getReviewDocuments().getCafcassUploadDocListDocTab()));
            //add court staff uploaded docs
            citizenDocuments.addAll(addCitizenDocuments(caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab()));
            //add citizen uploaded docs
            citizenDocuments.addAll(addCitizenDocuments(caseData.getReviewDocuments().getCitizenUploadedDocListDocTab()));

            citizenDocuments = citizenDocuments.stream().filter(citizenDocuments1 -> !unReturnedCategoriesForUI.contains(
                    citizenDocuments1.getCategoryId()))
                .collect(Collectors.toList());

            //confidential docs uploaded by citizen
            citizenDocuments.addAll(filterCitizenUploadedDocuments(
                caseData.getReviewDocuments().getConfidentialDocuments(),
                userDetails
            ));
            //restricted docs uploaded by citizen
            citizenDocuments.addAll(filterCitizenUploadedDocuments(
                caseData.getReviewDocuments().getRestrictedDocuments(),
                userDetails
            ));
        }

        //add citizen uploaded docs pending review
        if (null != caseData.getDocumentManagementDetails()) {
            citizenDocuments.addAll(filterCitizenUploadedDocuments(
                caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList(),
                userDetails
            ));
        }

        Collections.sort(citizenDocuments, comparing(CitizenDocuments::getUploadedDate).reversed());

        return citizenDocuments;
    }

    private List<CitizenDocuments> filterCitizenUploadedDocuments(List<Element<QuarantineLegalDoc>> quarantineDocsList,
                                                                  UserDetails userDetails) {
        return nullSafeCollection(quarantineDocsList).stream()
            .map(Element::getValue)
            .filter(qDoc -> CITIZEN.equalsIgnoreCase(qDoc.getUploaderRole()))
            .filter(qDoc -> null != userDetails
                && userDetails.getId().equalsIgnoreCase(qDoc.getUploadedByIdamId()))
            .map(this::createCitizenDocument)
            .toList();
    }

    private List<CitizenDocuments> addCitizenDocuments(List<Element<QuarantineLegalDoc>> quarantineDocsList) {
        return nullSafeCollection(quarantineDocsList).stream()
                                      .map(Element::getValue)
                                      .map(this::createCitizenDocument)
                                      .toList();
    }

    private CitizenDocuments createCitizenDocument(QuarantineLegalDoc quarantineDoc) {
        Document existingDocument = null;
        // If the quarantine doc is from Quarantine List then send the citizen document object
        if (quarantineDoc.getCitizenQuarantineDocument() != null && quarantineDoc.getCitizenQuarantineDocument().getDocumentUrl() != null) {
            existingDocument = quarantineDoc.getCitizenQuarantineDocument();
        } else {
            String attributeName = DocumentUtils.populateAttributeNameFromCategoryId(
                quarantineDoc.getCategoryId(),
                null
            );
            existingDocument = objectMapper.convertValue(
                objectMapper.convertValue(quarantineDoc, Map.class).get(attributeName),
                Document.class
            );
        }
        return CitizenDocuments.builder()
            .partyId(quarantineDoc.getUploadedByIdamId())
            .partyType(quarantineDoc.getDocumentParty())
            .categoryId(quarantineDoc.getCategoryId())
            .uploadedBy(quarantineDoc.getUploadedBy())
            .uploadedDate(quarantineDoc.getDocumentUploadedDate())
            .document(existingDocument)
            .build();
    }

    private List<CitizenDocuments> getCitizenOrders(UserDetails userDetails, CaseData caseData) {

        String partyId = findPartyId(caseData, userDetails);
        log.info("*** partyId from idamId {}", partyId);
        return new ArrayList<>(getCitizenOrdersForParty(caseData, partyId));
    }

    private List<CitizenDocuments> getCitizenOrdersForParty(CaseData caseData,
                                                            String partyId) {
        return nullSafeCollection(caseData.getOrderCollection()).stream()
            .map(Element::getValue)
            .filter(order -> isOrderServedForParty(order, partyId))
            .map(this::createCitizenOrder)
            .toList();
    }

    private CitizenDocuments createCitizenOrder(OrderDetails order) {
        return CitizenDocuments.builder()
            .partyType(order.getOrderType())
            //.categoryId(quarantineDoc.getCategoryId())
            .uploadedBy(order.getOtherDetails().getCreatedBy())
            .uploadedDate(order.getDateCreated())
            .document(order.getOrderDocument())
            .documentWelsh(order.getOrderDocumentWelsh())
            .build();
    }

    private boolean isOrderServedForParty(OrderDetails order,
                                          String partyId) {
        return nullSafeCollection(order.getServeOrderDetails().getServedParties()).stream()
            .map(Element::getValue)
            .anyMatch(servedParty -> servedParty.getPartyId().equalsIgnoreCase(partyId));
    }

    private String findPartyId(CaseData caseData,
                               UserDetails userDetails) {
        log.info("*** Inside find partyId method ***");
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("*** C100 case type");
            Optional<Element<PartyDetails>> applicantOptional = getParty(caseData.getApplicants(), userDetails);
            if (applicantOptional.isPresent()) {
                return String.valueOf(applicantOptional.get().getId());
            }

            Optional<Element<PartyDetails>> respondentOptional = getParty(caseData.getRespondents(), userDetails);
            if (respondentOptional.isPresent()) {
                return String.valueOf(respondentOptional.get().getId());
            }

        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("*** FL401 case type");
            if (null != caseData.getApplicantsFL401().getUser()
                && userDetails.getId().equalsIgnoreCase(caseData.getApplicantsFL401().getUser().getIdamId())) {
                return String.valueOf(caseData.getApplicantsFL401().getPartyId());
            }
            if (null != caseData.getRespondentsFL401().getUser()
                && userDetails.getId().equalsIgnoreCase(caseData.getRespondentsFL401().getUser().getIdamId())) {
                return String.valueOf(caseData.getRespondentsFL401().getPartyId());
            }
        }

        return null;
    }

    private Optional<Element<PartyDetails>> getParty(List<Element<PartyDetails>> parties,
                                                       UserDetails userDetails) {
        return nullSafeCollection(parties).stream()
            .filter(element -> null != element.getValue().getUser()
                && userDetails.getId().equalsIgnoreCase(element.getValue().getUser().getIdamId()))
            .findFirst();
    }
}
