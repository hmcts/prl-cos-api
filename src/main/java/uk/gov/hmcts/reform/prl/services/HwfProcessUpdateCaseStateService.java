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
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.PaymentStatus;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.DateOfSubmission;
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
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_OF_SUBMISSION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EUROPE_LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.HWF_PROCESS_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.PROCESS_PAYMENT_FOR_BULKSCAN;

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
        log.info("inside checkHwfPaymentStatus");
        //Fetch all C100 pending cases with Help with fees
        List<CaseDetails> caseDetailsList = retrieveCasesWithHelpWithFeesInPendingState(false);
        List<CaseDetails> bulkScanCaseDetailsList = retrieveCasesWithHelpWithFeesInPendingState(true);
        log.info("Case details {}", caseDetailsList);
        log.info("Bulk scan case details {}", bulkScanCaseDetailsList);
        checkAndProcessHwfAndUpdateCase(caseDetailsList);
        checkAndProcessHwfAndUpdateCase(bulkScanCaseDetailsList);
        long startTime = System.currentTimeMillis();
        log.info(
            "*** Total time taken to run HWF processing check payment status task - {}s ***",
            TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)
        );
    }

    private void checkAndProcessHwfAndUpdateCase(List<CaseDetails> caseDetailsList) {
        if (isNotEmpty(caseDetailsList)) {
            caseDetailsList.forEach(caseDetails -> {
                log.info("checking payment for caseId - " + caseDetails.getId());
                CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                if (StringUtils.isNotEmpty(caseData.getHelpWithFeesNumber())
                    && StringUtils.isNotEmpty(caseData.getPaymentServiceRequestReferenceNumber())) {
                    String event = CaseCreatedBy.BULK_SCAN.equals(caseData.getCaseCreatedBy())
                        ? PROCESS_PAYMENT_FOR_BULKSCAN.getValue() : HWF_PROCESS_CASE_UPDATE.getValue();
                    log.info("Processing help with fees with event - " + event);
                    updateCaseStateAndSubmitevent(event, caseDetails, caseData);
                }
                if (CaseCreatedBy.BULK_SCAN.equals(caseData.getCaseCreatedBy()) && StringUtils.isEmpty(caseData.getHelpWithFeesNumber())) {
                    updateCaseStateAndSubmitevent(PROCESS_PAYMENT_FOR_BULKSCAN.getValue(), caseDetails, caseData);
                }
            });
        } else {
            log.info("Retrieve Cases With HelpWithFees In Pending State is empty");
        }
    }

    private void updateCaseStateAndSubmitevent(String event, CaseDetails caseDetails, CaseData caseData) {
        log.info("Going to check service request payment status");
        log.info("PaymentServiceRequestReferenceNumber - " + caseData.getPaymentServiceRequestReferenceNumber());
        if (StringUtils.isNotEmpty(caseData.getPaymentServiceRequestReferenceNumber())) {
            ServiceRequestReferenceStatusResponse serviceRequestReferenceStatusResponse =
                paymentRequestService.fetchServiceRequestReferenceStatus(
                    systemUserService.getSysUserToken(),
                    caseData.getPaymentServiceRequestReferenceNumber());
            log.info("PaymentGroupReferenceStatusResponse - " + serviceRequestReferenceStatusResponse.getServiceRequestStatus());
            if (PaymentStatus.PAID.getDisplayedValue().equals(serviceRequestReferenceStatusResponse.getServiceRequestStatus())) {
                StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
                    = allTabService.getStartUpdateForSpecificEvent(caseDetails.getId().toString(), event);
                Map<String, Object> caseDataUpdated = startAllTabsUpdateDataContent.caseDataMap();
                if (caseDataUpdated.get(DATE_SUBMITTED_FIELD) == null) {
                    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
                    log.info("DateTimeFormatter Date is {} ", DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));
                    caseDataUpdated.put(DATE_SUBMITTED_FIELD, DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));
                    caseDataUpdated.put(
                        DATE_OF_SUBMISSION,
                        DateOfSubmission.builder().dateOfSubmission(CommonUtils.getIsoDateToSpecificFormat(
                            DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime),
                            CommonUtils.DATE_OF_SUBMISSION_FORMAT
                        ).replace("-", " ")).build()
                    );
                }
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
    }

    public List<CaseDetails> retrieveCasesWithHelpWithFeesInPendingState(boolean isBulkScan) {

        SearchResultResponse searchResultResponse = SearchResultResponse.builder()
            .cases(new ArrayList<>()).build();
        QueryParam ccdQueryParam;
        if (isBulkScan) {
            ccdQueryParam = buildCcdQueryParamForBulkScan();
        } else {
            ccdQueryParam = buildCcdQueryParam();
        }
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String searchString = objectMapper.writeValueAsString(ccdQueryParam);
            log.info("searchString " + searchString);
            String sysUserToken = systemUserService.getSysUserToken();
            final String s2sToken = authTokenGenerator.generate();
            SearchResult searchCases = coreCaseDataApi.searchCases(
                sysUserToken,
                s2sToken,
                CASE_TYPE,
                searchString
            );

            searchResultResponse = objectMapper.convertValue(
                searchCases,
                SearchResultResponse.class
            );
        } catch (JsonProcessingException e) {
            log.error("Exception happened in parsing query param {}", e.getMessage());
        }

        if (null != searchResultResponse) {
            log.info("Total no. of cases retrieved {}", searchResultResponse.getTotal());
            return searchResultResponse.getCases();
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

        Bool finalFilter = Bool.builder()
            .should(shoulds)
            .minimumShouldMatch(3)
            .filter(getFilter())
            .must(getMustFilter())
            .build();

        return getQueryParam(finalFilter);
    }

    private QueryParam buildCcdQueryParamForBulkScan() {
        //Bulk scan c100 cases
        List<Should> shoulds = List.of(Should.builder()
                                           .match(Match.builder()
                                                      .caseTypeOfApplication("C100")
                                                      .build())
                                           .build(),
                                       Should.builder()
                                           .match(Match.builder()
                                                      .caseCreatedBy("BULK_SCAN")
                                                      .build())
                                           .build());

        Bool finalFilter = Bool.builder()
            .should(shoulds)
            .minimumShouldMatch(2)
            .filter(getFilter())
            .must(getMustFilter())
            .build();

        return getQueryParam(finalFilter);
    }

    private QueryParam getQueryParam(Bool finalFilter) {
        return QueryParam.builder()
            .query(Query.builder().bool(finalFilter).build())
            .size("100")
            .dataToReturn(List.of(
                "data.caseCreatedBy",
                "data.helpWithFeesNumber",
                "data.paymentServiceRequestReferenceNumber"
            ))
            .build();
    }

    private Must getMustFilter() {
        StateFilter stateFilter = StateFilter.builder()
            .should(List.of(Should.builder().match(Match.builder()
                                                       .state(State.SUBMITTED_NOT_PAID.getValue())
                                                       .build())
                                .build()))
            .build();
        return Must.builder().stateFilter(stateFilter).build();
    }

    private Filter getFilter() {
        // Below filter added to reduce the case count, expectation is case payments are processed within 2 days (SLA)
        LastModified lastModified = LastModified.builder().gte(LocalDateTime.now().minusDays(5).toString()).build();
        Range range = Range.builder().lastModified(lastModified).build();
        return Filter.builder().range(range).build();
    }
}
