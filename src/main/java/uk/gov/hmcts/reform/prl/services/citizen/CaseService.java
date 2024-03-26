package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.UpdateCaseData;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildApplicantDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildRespondentDetailsElements;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StatementOfService;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT_WITH_HWF;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
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
    public static final String YES = "Yes";
    public static final String CASE_STATUS = "caseStatus";
    private final CoreCaseDataApi coreCaseDataApi;
    private final CaseRepository caseRepository;
    private final IdamClient idamClient;
    private final ObjectMapper objectMapper;
    private final CaseDataMapper caseDataMapper;
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;
    private final RoleAssignmentService roleAssignmentService;
    private static final String INVALID_CLIENT = "Invalid Client";

    public CaseDetails updateCase(CaseData caseData, String authToken,
                                  String caseId, String eventId) throws JsonProcessingException {
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
                                          .taskListVersion(TASK_LIST_VERSION_V2)
                                          .build());
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

    public CaseDetails getCase(String authToken, String caseId) {
        return caseRepository.getCase(authToken, caseId);
    }

    public CaseDetails createCase(CaseData caseData, String authToken) {
        return caseRepository.createCase(authToken, caseData);
    }

    public Map<String, String> fetchIdamAmRoles(String authorisation, String emailId) {
        return roleAssignmentService.fetchIdamAmRoles(authorisation, emailId);
    }
}
