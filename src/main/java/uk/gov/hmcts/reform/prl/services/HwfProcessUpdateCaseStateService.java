package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
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
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestReferenceStatusResponse;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.HWF_PROCESS_CASE_UPDATE;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HwfProcessUpdateCaseStateService {

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final PaymentRequestService paymentRequestService;
    private final AllTabServiceImpl allTabService;

    private final ObjectMapper objectMapper;


    public void checkHwfPaymentStatusAndUpdateCaseState() {
        long startTime = System.currentTimeMillis();
        log.info("inside checkHwfPaymentStatus");
        //Fetch all C100 pending cases with Help with fees
        List<CaseDetails> caseDetailsList = retrieveCasesWithHelpWithFeesInPendingState();
        if (isNotEmpty(caseDetailsList)) {
            caseDetailsList.forEach(caseDetails -> {
                log.info("caseDetails caseId - " + caseDetails.getId());
                CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                if (StringUtils.isNotEmpty(caseData.getHelpWithFeesNumber())
                    && StringUtils.isNotEmpty(caseData.getPaymentServiceRequestReferenceNumber())) {
                    log.info("Going to check service request payment status");
                    ServiceRequestReferenceStatusResponse serviceRequestReferenceStatusResponse =
                        paymentRequestService.fetchServiceRequestReferenceStatus(
                        systemUserService.getSysUserToken(),
                        caseData.getPaymentServiceRequestReferenceNumber()
                    );
                    log.info("PaymentGroupReferenceStatusResponse - " + serviceRequestReferenceStatusResponse.getServiceRequestStatus());
                    if (PaymentStatus.PAID.getDisplayedValue().equals(serviceRequestReferenceStatusResponse.getServiceRequestStatus())) {
                        Map<String, Object> caseDataUpdated = new HashMap<>();
                        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
                            = allTabService.getStartUpdateForSpecificEvent(
                            caseDetails.getId().toString(),
                            HWF_PROCESS_CASE_UPDATE.getValue()
                        );

                        caseDataUpdated.put("caseStatus", CaseStatus.builder().state(State.SUBMITTED_PAID.getLabel()).build());

                        //Save case data
                        allTabService.submitAllTabsUpdate(
                            startAllTabsUpdateDataContent.authorisation(),
                            caseDetails.getId().toString(),
                            startAllTabsUpdateDataContent.startEventResponse(),
                            startAllTabsUpdateDataContent.eventRequestData(),
                            caseDataUpdated
                        );
                    }
                }
            });

        } else {
            log.info("Retrieve Cases With HelpWithFees In Pending State is empty");
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
            log.info("searchString " + searchString);
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
        List<Should> shoulds = List.of(Should.builder()
                                           .match(Match.builder()
                                                      .caseTypeOfApplication("C100")
                                                      .build())
                                           .build(),
                                       Should.builder()
                                           .match(Match.builder()
                                                      .caseCreatedBy("CITIZEN")
                                                      .build())
                                           .build(),
                                       Should.builder()
                                           .match(Match.builder()
                                                      .helpWithFees("Yes")
                                                      .build())
                                           .build());
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
            .should(shoulds)
            .minimumShouldMatch(3)
            .filter(filter)
            .must(mustFilter)
            .build();

        return QueryParam.builder()
            .query(Query.builder().bool(finalFilter).build())
            .build();
    }
}
