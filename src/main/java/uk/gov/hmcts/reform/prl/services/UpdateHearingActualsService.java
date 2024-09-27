package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Bool;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Match;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Must;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Query;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Should;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.StateFilter;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateHearingActualsService {

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final HearingApiClient hearingApiClient;
    private final AllTabServiceImpl allTabService;

    private final ObjectMapper objectMapper;


    public void updateHearingActuals() {

        //Fetch all cases in Hearing state pending fm5 reminder notifications
        List<CaseDetails> caseDetailsList = retrieveCasesInHearingState();
        if (isNotEmpty(caseDetailsList)) {
            createUpdateHearingActualWaTask(
                caseDetailsList,
                fetchAndFilterHearingsForTodaysDate(getListOfCaseidsForHearings(
                    caseDetailsList))
            );
        }
    }

    private Map<String, String> fetchAndFilterHearingsForTodaysDate(List<String> listOfCaseidsForHearings) {
        List<Hearings> hearingsList = hearingApiClient.getHearingsForAllCaseIdsWithCourtVenue(
            systemUserService.getSysUserToken(),
            authTokenGenerator.generate(),
            listOfCaseidsForHearings
        );

        return filterCaseIdAndHearingsForTodaysDate(hearingsList);
    }

    private void createUpdateHearingActualWaTask(List<CaseDetails> caseDetailsList,
                                                 Map<String,
                                                     String> caseIds) {
        caseIds.forEach((caseId, hearingId) -> caseDetailsList.stream().filter(caseDetails -> String.valueOf(caseDetails.getId()).equals(
            caseId)).map(caseDetails -> {
            CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

            if (!checkIfHearingIdIsMappedInOrders(caseData, hearingId)) {
                StartAllTabsUpdateDataContent startAllTabsUpdateDataContent;
                startAllTabsUpdateDataContent = allTabService.getStartAllTabsUpdate(caseId);
                Map<String, Object> caseDataUpdated = new HashMap<>();
                caseDataUpdated.put("enableUpdateHearingActualTask", "true");
                allTabService.submitAllTabsUpdate(
                    startAllTabsUpdateDataContent.authorisation(),
                        caseId,
                        startAllTabsUpdateDataContent.startEventResponse(),
                        startAllTabsUpdateDataContent.eventRequestData(),
                        caseDataUpdated
                    );
                }
                return null;
            }
        ));
    }

    private boolean checkIfHearingIdIsMappedInOrders(CaseData caseData, String hearingId) {
        if (!checkIfHearingIdIsMappedinDraftOrder(caseData, hearingId)) {
            return !checkIfHearingIdIsMappedinSavedServedOrder(caseData, hearingId);
        }
        return true;
    }

    private boolean checkIfHearingIdIsMappedinDraftOrder(CaseData caseData, String hearingId) {
        return caseData.getDraftOrderCollection()
            .stream()
            .map(Element::getValue)
            .anyMatch(draftOrderElement -> draftOrderElement.getManageOrderHearingDetails()
                .stream()
                .map(Element::getValue)
                .anyMatch(hearingData -> hearingData.getConfirmedHearingDates() != null
                    && hearingData.getConfirmedHearingDates().getValue().getCode().equals(hearingId)));
    }

    private boolean checkIfHearingIdIsMappedinSavedServedOrder(CaseData caseData, String hearingId) {
        return caseData.getOrderCollection()
            .stream()
            .map(Element::getValue)
            .anyMatch(orderElement -> orderElement.getManageOrderHearingDetails()
                .stream().map(Element::getValue)
                .anyMatch(hearingData -> hearingData.getConfirmedHearingDates() != null
                    && hearingData.getConfirmedHearingDates().getValue().getCode().equals(hearingId)));
    }

    private Map<String, String> filterCaseIdAndHearingsForTodaysDate(List<Hearings> hearingsForAllCaseIds) {

        Map<String, String> caseIdHearingIdMapping = new HashMap<>();
        hearingsForAllCaseIds.stream().forEach(hearings -> {
            List<Long> filteredHearingIds = hearings.getCaseHearings()
                .stream().filter(caseHearing -> caseHearing.getHmcStatus().equals(LISTED))
                .filter(caseHearing -> caseHearing.getHearingDaySchedule().stream().
                    anyMatch(
                        hearingDaySchedule -> hearingDaySchedule.getHearingStartDateTime().toLocalDate().equals(
                            LocalDate.now())
                    )).map(CaseHearing::getHearingID).toList();

            caseIdHearingIdMapping.put(hearings.getCaseRef(), String.valueOf(filteredHearingIds.get(0)));

        });
        return caseIdHearingIdMapping;
    }

    private List<String> getListOfCaseidsForHearings(List<CaseDetails> caseDetailsList) {
        return caseDetailsList.stream().map(CaseDetails::getId).map(String::valueOf).toList();

    }


    public List<CaseDetails> retrieveCasesInHearingState() {

        SearchResultResponse response = SearchResultResponse.builder().cases(new ArrayList<>()).build();

        QueryParam ccdQueryParam = buildCcdQueryParam();

        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String searchString = objectMapper.writeValueAsString(ccdQueryParam);

            String userToken = systemUserService.getSysUserToken();
            final String s2sToken = authTokenGenerator.generate();
            SearchResult searchResult = coreCaseDataApi.searchCases(userToken, s2sToken, CASE_TYPE, searchString);

            response = objectMapper.convertValue(searchResult, SearchResultResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Exception happened in parsing query param ", e);
        }

        if (null != response) {
            log.info("Total no. of cases retrieved {}", response.getTotal());
            return response.getCases();
        }
        return Collections.emptyList();
    }


    private QueryParam buildCcdQueryParam() {
        //C100 cases where fm5 reminders are not sent already
        List<Should> shoulds = List.of(
            Should.builder().match(Match.builder().caseTypeOfApplication("C100").build()).build(),
            Should.builder().match(Match.builder().caseTypeOfApplication("FL401").build()).build()
        );

        //Hearing state
        StateFilter stateFilter = StateFilter.builder().should(List.of(
            Should.builder().match(Match.builder().state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue()).build()).build(),
            Should.builder().match(Match.builder().state(State.DECISION_OUTCOME.getValue()).build()).build()
        )).build();
        Must mustFilter = Must.builder().stateFilter(stateFilter).build();

        Bool finalFilter = Bool.builder().should(shoulds).minimumShouldMatch(2).must(mustFilter).build();

        return QueryParam.builder().query(Query.builder().bool(finalFilter).build()).build();
    }

}
