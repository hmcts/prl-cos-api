package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    CaseAccessApi caseAccessApi;


    @Autowired
    IdamClient idamClient;

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    SystemUserService systemUserService;

    @Autowired
    ObjectMapper objectMapper;

    public CaseDetails updateCase(CaseData caseData, String authToken, String s2sToken, String caseId, String eventId) {

        UserDetails userDetails = idamClient.getUserDetails(authToken);

        return updateCaseDetails(caseData, authToken, s2sToken, caseId, eventId, userDetails);

    }

    public List<CaseData> retrieveCases(String authToken, String s2sToken, String role, String userId) {
        Map<String, String> searchCriteria = new HashMap<>();

        searchCriteria.put("sortDirection", "desc");
        searchCriteria.put("page", "1");

        CaseData caseData1 = CaseData.builder().applicantCaseName("John Smith").id(1234567891).state(State.CASE_ISSUE).caseTypeOfApplication("C100").build();
        CaseData caseData2 = CaseData.builder().applicantCaseName("Robert Walker").id(1234567892).state(State.GATEKEEPING).caseTypeOfApplication("C100").build();
        CaseData caseData3 = CaseData.builder().applicantCaseName("Elon Musk").id(1234567893).state(State.CASE_HEARING).caseTypeOfApplication("C100").build();
        CaseData caseData4 = CaseData.builder().applicantCaseName("Warren Buffet").id(1234567894).state(State.GATEKEEPING).caseTypeOfApplication("FL401").build();
        CaseData caseData5 = CaseData.builder().applicantCaseName("Jonny Wilko").id(1234567895).state(State.ALL_FINAL_ORDERS_ISSUED)
            .caseTypeOfApplication("FL401").build();
        List<CaseData> caseDataList = new ArrayList<>();
        caseDataList.add(caseData1);
        caseDataList.add(caseData2);
        caseDataList.add(caseData3);
        caseDataList.add(caseData4);
        caseDataList.add(caseData5);
        //return searchCasesWith(authToken, s2sToken, searchCriteria);
        return caseDataList;
    }

    private List<CaseData> searchCasesWith(String authToken, String s2sToken, Map<String, String> searchCriteria) {

        UserDetails userDetails = idamClient.getUserDetails(authToken);
        List<CaseDetails> caseDetails = new ArrayList<>();
        caseDetails.addAll(performSearch(authToken, userDetails, searchCriteria, s2sToken));
        return caseDetails
            .stream()
            .map(caseDetailsConverter::extractCase)
            .collect(Collectors.toList());
    }

    private List<CaseDetails> performSearch(String authToken, UserDetails user, Map<String, String> searchCriteria, String serviceAuthToken) {
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
        log.info("Input casedata, applicantcaseName :::: {}", caseData.getApplicantCaseName());
        Map<String, Object> caseDataMap = caseData.toMap(objectMapper);
        Iterables.removeIf(caseDataMap.values(), Objects::isNull);
        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
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

        return coreCaseDataApi.submitEventForCaseWorker(
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
        String userId = idamClient.getUserDetails(authorisation).getId();
        String emailId = idamClient.getUserDetails(authorisation).getEmail();
        this.grantAccessToCase(systemUserService.getSysUserToken(), caseId, userId);
        this.updateCitizenIdAndEmail(authorisation, caseId, userId, emailId, s2sToken, "applicantsDetails");

    }

    private void grantAccessToCase(String sysUserToken, String caseId, String userId) {
        String sysUserId = idamClient.getUserDetails(sysUserToken).getId();
        caseAccessApi.grantAccessToCase(
            sysUserToken,
            authTokenGenerator.generate(),
            sysUserId,
            JURISDICTION,
            CASE_TYPE,
            caseId,
            new UserId(userId)
        );
    }

    @SuppressWarnings(value = "squid:S1172")
    private CaseDetails updateCitizenIdAndEmail(
        String authorisation,
        String caseId,
        String citizenId,
        String citizenEmail,
        String s2sToken,
        String eventId
    ) {
        try {
            UserDetails userDetails = idamClient.getUserDetails(authorisation);
            EventRequestData eventRequestData = eventRequest(
                CaseEventDetail.builder().id("applicantsDetails")
                    .eventName("applicantsDetails").build(),
                userDetails.getId(),
                authorisation
            );

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                s2sToken,
                eventRequestData,
                Long.valueOf(caseId)
            );

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                           .id(startEventResponse.getEventId())
                           .build())
                .build();

            return submitUpdate(
                authorisation,
                s2sToken,
                eventRequestData,
                caseDataContent,
                Long.valueOf(caseId)
            );
        } catch (Exception exception) {
            throw new RuntimeException();
        }
    }

    private StartEventResponse startUpdate(
        String authorisation,
        String s2sToken,
        EventRequestData eventRequestData,
        Long caseId
    ) {
        return coreCaseDataApi.startEventForCitizen(
            authorisation,
            s2sToken,
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            caseId.toString(),
            eventRequestData.getEventId()
        );
    }

    private CaseDetails submitUpdate(
        String authorisation,
        String s2sToken,
        EventRequestData eventRequestData,
        CaseDataContent caseDataContent,
        Long caseId
    ) {
        return coreCaseDataApi.submitEventForCitizen(
            authorisation,
            s2sToken,
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            caseId.toString(),
            eventRequestData.isIgnoreWarning(),
            caseDataContent
        );
    }

    private EventRequestData eventRequest(CaseEventDetail caseEvent, String userId, String authorisation) {
        return EventRequestData.builder()
            .userToken(authorisation)
            .userId(userId)
            .jurisdictionId(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .eventId(caseEvent.getEventName())
            .ignoreWarning(true)
            .build();
    }

    public String validateAccessCode(String userToken, String s2sToken, String caseId, String accessCode) {
        log.info("validateAccessCode");
        log.info("parameters are :" + caseId + " and " + accessCode);
        String accessCodeStatus = "Invalid";
        CaseData caseData = objectMapper.convertValue(
            coreCaseDataApi.getCase(userToken, s2sToken, caseId).getData(),
            CaseData.class
        );

        log.info("caseData testing::" + caseData);

        List<CaseInvite> matchingCaseInvite = caseData.getRespondentCaseInvites()
            .stream()
            .map(Element::getValue)
            .filter(x -> accessCode.equals(x.getAccessCode()))
            .collect(Collectors.toList());

        log.info("matchingCaseInvite testing::" + matchingCaseInvite);
        if (matchingCaseInvite.size() > 0) {
            accessCodeStatus = "Valid";
            for (CaseInvite caseInvite : matchingCaseInvite) {
                if ("Yes".equals(caseInvite.getHasLinked())) {
                    accessCodeStatus = "Linked";
                }
            }
        }
        log.info("accessCodeStatus" + accessCodeStatus);
        return accessCodeStatus;
    }
}
