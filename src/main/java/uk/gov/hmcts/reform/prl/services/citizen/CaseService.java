package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildApplicantDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildRespondentDetailsElements;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.caseflags.request.CitizenPartyFlagsRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.FlagDetailRequest;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetailsMeta;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT_WITH_HWF;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getPartyDetailsMeta;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseService {
    public static final String YES = "Yes";
    private final CoreCaseDataApi coreCaseDataApi;
    private final CaseRepository caseRepository;
    private final IdamClient idamClient;
    private final ObjectMapper objectMapper;
    private final CaseDataMapper caseDataMapper;
    private final RoleAssignmentService roleAssignmentService;
    private final CcdCoreCaseDataService ccdCoreCaseDataService;
    private final HearingService hearingService;

    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;

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

    public List<CaseData> retrieveCases(String authToken, String s2sToken) {

        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("sortDirection", "desc");
        searchCriteria.put("page", "1");

        return searchCasesLinkedToUser(authToken, s2sToken, searchCriteria);
    }

    private List<CaseData> searchCasesLinkedToUser(String authToken, String s2sToken,
                                                   Map<String, String> searchCriteria) {

        UserDetails userDetails = idamClient.getUserDetails(authToken);
        List<CaseDetails> caseDetails = new ArrayList<>(performSearch(authToken, userDetails, searchCriteria, s2sToken));
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

    public CaseDataWithHearingResponse getCaseWithHearing(String authorisation, String caseId, String hearingNeeded) {
        CaseDataWithHearingResponse caseDataWithHearingResponse = CaseDataWithHearingResponse.builder().build();
        CaseDetails caseDetails = ccdCoreCaseDataService.findCaseById(authorisation, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        caseData = caseData.toBuilder().noOfDaysRemainingToSubmitCase(
            CaseUtils.getRemainingDaysSubmitCase(caseData)).build();
        caseDataWithHearingResponse = caseDataWithHearingResponse.toBuilder().caseData(caseData).build();
        if ("Yes".equalsIgnoreCase(hearingNeeded)) {
            caseDataWithHearingResponse =
                caseDataWithHearingResponse.toBuilder().hearings(
                    hearingService.getHearings(authorisation, caseId)).build();
        }
        return caseDataWithHearingResponse;
    }

    public Flags getPartyCaseFlags(String authToken, String caseId, String partyId) {
        log.info("Inside getPartyCaseFlags caseId {}", caseId);
        log.info("Inside getPartyCaseFlags partyId {}", partyId);
        CaseDetails caseDetails = getCase(authToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        Optional<PartyDetailsMeta> partyDetailsMeta = getPartyDetailsMeta(
            partyId,
            caseData.getCaseTypeOfApplication(),
            caseData
        );

        if (partyDetailsMeta.isPresent()) {
            log.info("Party details meta {}", partyDetailsMeta.get().getPartyDetails());
            log.info(
                "Party details meta labelForDynamicList {}",
                partyDetailsMeta.get().getPartyDetails().getLabelForDynamicList()
            );
        }

        if (partyDetailsMeta.isPresent()
            && partyDetailsMeta.get().getPartyDetails() != null
            && !StringUtils.isEmpty(partyDetailsMeta.get().getPartyDetails().getLabelForDynamicList())) {
            log.info("Party details meta is valid ");
            Optional<String> partyExternalCaseFlagField = getPartyExternalCaseFlagField(
                caseData.getCaseTypeOfApplication(),
                partyDetailsMeta.get().getPartyType(),
                partyDetailsMeta.get().getPartyIndex()
            );

            if (partyExternalCaseFlagField.isPresent()) {
                log.info("partyExternalCaseFlagField is present:: {}", partyExternalCaseFlagField.get());
                log.info("data is present as well:: {}", caseDetails.getData().get(partyExternalCaseFlagField.get()));
                log.info(" validating the data with entire case details:: {}", caseDetails.getData());
                return objectMapper.convertValue(
                    caseDetails.getData().get(partyExternalCaseFlagField.get()),
                    Flags.class
                );
            }
        }

        return null;
    }

    public ResponseEntity<Object> updateCitizenRAflags(
        String caseId, String eventId, String authToken, CitizenPartyFlagsRequest citizenPartyFlagsRequest) {
        log.info("Inside updateCitizenRAflags caseId {}", caseId);
        log.info("Inside updateCitizenRAflags eventId {}", eventId);
        log.info("Inside updateCitizenRAflags citizenPartyFlagsRequest {}", citizenPartyFlagsRequest);

        if (StringUtils.isEmpty(citizenPartyFlagsRequest.getPartyIdamId()) || ObjectUtils.isEmpty(
            citizenPartyFlagsRequest.getPartyExternalFlags())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("bad request");
        }

        UserDetails userDetails = idamClient.getUserDetails(authToken);
        CaseEvent caseEvent = CaseEvent.fromValue(eventId);
        EventRequestData eventRequestData = ccdCoreCaseDataService.eventRequest(
            caseEvent,
            userDetails.getId()
        );

        StartEventResponse startEventResponse =
            ccdCoreCaseDataService.startUpdate(
                authToken,
                eventRequestData,
                caseId,
                false
            );

        CaseData caseData = CaseUtils.getCaseData(startEventResponse.getCaseDetails(), objectMapper);
        Optional<PartyDetailsMeta> partyDetailsMeta = getPartyDetailsMeta(
            citizenPartyFlagsRequest.getPartyIdamId(),
            caseData.getCaseTypeOfApplication(),
            caseData
        );

        if (partyDetailsMeta.isEmpty()
            || null == partyDetailsMeta.get().getPartyDetails()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("party details not found");
        }

        Map<String, Object> updatedCaseData = startEventResponse.getCaseDetails().getData();
        Optional<String> partyExternalCaseFlagField = getPartyExternalCaseFlagField(
            caseData.getCaseTypeOfApplication(),
            partyDetailsMeta.get().getPartyType(),
            partyDetailsMeta.get().getPartyIndex()
        );

        if (partyExternalCaseFlagField.isEmpty() || !updatedCaseData.containsKey(partyExternalCaseFlagField.get()) || ObjectUtils.isEmpty(
            updatedCaseData.get(
                partyExternalCaseFlagField.get()))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("party external flag details not found");
        }
        try {
            log.info("partyExternalCaseFlagField ===>" + objectMapper.writeValueAsString(partyExternalCaseFlagField.get()));
        } catch (JsonProcessingException e) {
            log.info("error");
        }

        Flags flags = objectMapper.convertValue(
            updatedCaseData.get(partyExternalCaseFlagField.get()),
            Flags.class
        );
        try {
            log.info("Existing external Party flags  ===>" + objectMapper.writeValueAsString(flags));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        flags = flags.toBuilder()
            .details(convertFlags(citizenPartyFlagsRequest.getPartyExternalFlags().getDetails()))
            .build();
        try {
            log.info("Updated external Party flags  ===>" + objectMapper.writeValueAsString(flags));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        Map<String, Object> externalCaseFlagMap = new HashMap<>();
        externalCaseFlagMap.put(partyExternalCaseFlagField.get(), flags);

        CaseDataContent caseDataContent = ccdCoreCaseDataService.createCaseDataContent(
            startEventResponse,
            externalCaseFlagMap
        );

        try {
            log.info("Case data content is  ===>" + objectMapper.writeValueAsString(caseDataContent));
        } catch (JsonProcessingException e) {
            log.info("error");
        }

        ccdCoreCaseDataService.submitUpdate(
            authToken,
            eventRequestData,
            caseDataContent,
            caseId,
            false
        );
        return ResponseEntity.status(HttpStatus.OK).body("party flags updated");
    }

    private List<Element<FlagDetail>> convertFlags(List<Element<FlagDetailRequest>> details) {
        List<Element<FlagDetail>> flagDetails = new ArrayList<>();

        for (Element<FlagDetailRequest> detail : details) {
            FlagDetail flagDetail = FlagDetail.builder().name(detail.getValue().getName())
                .name_cy(detail.getValue().getName_cy())
                .subTypeValue(detail.getValue().getSubTypeValue())
                .subTypeValue_cy(detail.getValue().getSubTypeValue_cy())
                .subTypeKey(detail.getValue().getSubTypeKey())
                .otherDescription(detail.getValue().getOtherDescription())
                .otherDescription_cy(detail.getValue().getOtherDescription_cy())
                .flagComment(detail.getValue().getFlagComment())
                .flagComment_cy(detail.getValue().getFlagComment_cy())
                .flagUpdateComment(detail.getValue().getFlagUpdateComment())
                .dateTimeCreated(detail.getValue().getDateTimeCreated())
                .dateTimeModified(detail.getValue().getDateTimeModified())
                .path(detail.getValue().getPath())
                .hearingRelevant(detail.getValue().getHearingRelevant())
                .flagCode(detail.getValue().getFlagCode())
                .status(detail.getValue().getStatus())
                .availableExternally(detail.getValue().getAvailableExternally())
                .build();

            if (null != detail.getId()) {
                flagDetails.add(element(detail.getId(), flagDetail));
            } else {
                flagDetails.add(element(flagDetail));
            }
        }

        return flagDetails;
    }

    public Optional<String> getPartyExternalCaseFlagField(String caseType, PartyEnum partyType, Integer partyIndex) {

        Optional<String> partyExternalCaseFlagField = Optional.empty();
        boolean isC100Case = C100_CASE_TYPE.equalsIgnoreCase(caseType);

        if (PartyEnum.applicant == partyType) {
            partyExternalCaseFlagField
                = Optional.ofNullable(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
                caseType,
                isC100Case ? PartyRole.Representing.CAAPPLICANT : PartyRole.Representing.DAAPPLICANT,
                partyIndex
            ));
        } else if (PartyEnum.respondent == partyType) {
            partyExternalCaseFlagField
                = Optional.ofNullable(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
                caseType,
                isC100Case ? PartyRole.Representing.CARESPONDENT : PartyRole.Representing.DARESPONDENT,
                partyIndex
            ));
        }

        return partyExternalCaseFlagField;
    }

    public Map<String, String> fetchIdamAmRoles(String authorisation, String emailId) {
        return roleAssignmentService.fetchIdamAmRoles(authorisation, emailId);
    }
}
