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
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.PaymentStatus;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Bool;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Filter;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.LastModified;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Match;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Must;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Query;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Range;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Should;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.StateFilter;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentGroupReferenceStatusResponse;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;
import static uk.gov.hmcts.reform.prl.enums.CaseCreatedBy.CITIZEN;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.HWF_PROCESS_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HwfProcessingCheckPaymentStatusService {

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final PaymentRequestService paymentRequestService;
    private final AllTabServiceImpl allTabService;

    private final ObjectMapper objectMapper;



    public void checkPaymentStatus() {
        long startTime = System.currentTimeMillis();
        //Fetch all C100 pending cases with Help with fees
        List<CaseDetails> caseDetailsList = retrieveCasesWithHelpWithFeesInPendingState();
        if (isNotEmpty(caseDetailsList)) {
            caseDetailsList.forEach(caseDetails -> {
                CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                StartAllTabsUpdateDataContent startAllTabsUpdateDataContent;
                Map<String, Object> caseDataUpdated = new HashMap<>();
                startAllTabsUpdateDataContent
                    = allTabService.getStartUpdateForSpecificEvent(
                    caseDetails.getId().toString(),
                    HWF_PROCESS_CASE_UPDATE.getValue()
                );
                PaymentGroupReferenceStatusResponse paymentGroupReferenceStatusResponse = paymentRequestService.fetchPaymentGroupReferenceStatus(
                    startAllTabsUpdateDataContent.authorisation(),
                    caseData.getPaymentServiceRequestReferenceNumber()
                );

                if (PaymentStatus.PAID.getDisplayedValue().equals(paymentGroupReferenceStatusResponse.getServiceRequestStatus())) {
                    caseDataUpdated.put(STATE_FIELD, State.SUBMITTED_PAID);
                }
                //Save case data
                allTabService.submitAllTabsUpdate(
                    startAllTabsUpdateDataContent.authorisation(),
                    caseDetails.getId().toString(),
                    startAllTabsUpdateDataContent.startEventResponse(),
                    startAllTabsUpdateDataContent.eventRequestData(),
                    caseDataUpdated
                );

            });
        }
        log.info(
            "*** Total time taken to run HWF processing check payment status task - {}s ***",
            TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)
        );
    }

    public List<CaseDetails> retrieveCasesWithHelpWithFeesInPendingState() {

        SearchResultResponse response = SearchResultResponse.builder()
            .cases(new ArrayList<>()).build();

        QueryParam ccdQueryParam = buildCcdQueryParam();

        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String searchString = objectMapper.writeValueAsString(ccdQueryParam);

            String userToken = systemUserService.getSysUserToken();
            final String s2sToken = authTokenGenerator.generate();
            SearchResult searchResult = coreCaseDataApi.searchCases(
                userToken,
                s2sToken,
                CASE_TYPE,
                searchString
            );

            response = objectMapper.convertValue(
                searchResult,
                SearchResultResponse.class
            );
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
        //C100 citizen cases with help with fess
        List<Should> shoulds = List.of(
            Should.builder()
                .match(Match.builder()
                           .caseTypeOfApplication(C100_CASE_TYPE)
                           .build())
                .build(),
            Should.builder()
                .match(Match.builder()
                           .caseCreatedBy(CITIZEN.toString())
                           .build())
                .build(),
            Should.builder()
                .match(Match.builder()
                           .helpWithFees(Yes.getDisplayedValue())
                           .build())
                .build()
        );

        //Hearing state
        StateFilter stateFilter = StateFilter.builder()
            .should(List.of(Should.builder().match(Match.builder()
                                                       .state(State.SUBMITTED_NOT_PAID.getValue())
                                                       .build())
                                .build()))
            .build();
        Must mustFilter = Must.builder().stateFilter(stateFilter).build();

        LastModified lastModified = LastModified.builder().gte(LocalDateTime.now().minusDays(3).toString()).build();
        Range range = Range.builder().lastModified(lastModified).build();
        Filter filter = Filter.builder().range(range).build();

        Bool finalFilter = Bool.builder()
            .filter(filter)
            .should(shoulds)
            .minimumShouldMatch(2)
            .must(mustFilter)
            .build();

        return QueryParam.builder()
            .query(Query.builder().bool(finalFilter).build())
            .build();
    }
}
