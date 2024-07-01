package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.ApplicationStatus;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.PaymentStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Payment;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Bool;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Filter;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.LastModified;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Match;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Query;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Range;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Should;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestReferenceStatusResponse;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_ADDTIONAL_APPLICATION_BUNDLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.C100_HWF_PROCESS_AWP_STATUS_UPDATE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C100AwpProcessHwfPaymentService {

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final PaymentRequestService paymentRequestService;
    private final AllTabServiceImpl allTabService;

    private final ObjectMapper objectMapper;


    public void checkHwfPaymentStatusAndUpdateApplicationStatus() {
        long startTime = System.currentTimeMillis();
        log.info("inside checkHwfPaymentStatus");
        //Fetch all C100 pending cases with Help with fees
        List<CaseDetails> caseDetailsList = retrieveCasesWithAwpHelpWithFeesInPendingState();
        if (isNotEmpty(caseDetailsList)) {
            caseDetailsList.forEach(caseDetails -> {
                log.info("caseDetails caseId - " + caseDetails.getId());
                CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                List<UUID> processedApplicationIds = new ArrayList<>();
                for (Element<AdditionalApplicationsBundle> additionalApplicationsBundleElement : caseData.getAdditionalApplicationsBundle()) {
                    Payment payment = additionalApplicationsBundleElement.getValue().getPayment();
                    if (ObjectUtils.isNotEmpty(payment)
                        && PaymentStatus.HWF.getDisplayedValue().equalsIgnoreCase(payment.getStatus())) {
                        ServiceRequestReferenceStatusResponse serviceRequestReferenceStatusResponse =
                            paymentRequestService.fetchServiceRequestReferenceStatus(
                                systemUserService.getSysUserToken(),
                                payment.getPaymentServiceRequestReferenceNumber()
                            );
                        if (PaymentStatus.PAID.getDisplayedValue().equals(serviceRequestReferenceStatusResponse.getServiceRequestStatus())) {
                            processedApplicationIds.add(additionalApplicationsBundleElement.getId());
                        }
                    }
                }

                if (isNotEmpty(processedApplicationIds)) {
                    Map<String, Object> caseDataUpdated = new HashMap<>();
                    for (UUID processedApplicationId : processedApplicationIds) {
                        caseData.getAdditionalApplicationsBundle().stream().filter(additionalApplicationsBundleElement ->
                                                                                       additionalApplicationsBundleElement.getId().equals(
                                                                                           processedApplicationId))
                            .findFirst().ifPresent(additionalApplicationsBundleElement -> {
                                AdditionalApplicationsBundle additionalApplicationsBundle = additionalApplicationsBundleElement.getValue()
                                    .toBuilder()
                                    .payment(additionalApplicationsBundleElement.getValue().getPayment().toBuilder()
                                                 .status(PaymentStatus.PAID.getDisplayedValue())
                                                 .build()
                                    )
                                    .c2DocumentBundle(ObjectUtils.isNotEmpty(additionalApplicationsBundleElement.getValue().getC2DocumentBundle())
                                                          ? additionalApplicationsBundleElement.getValue().getC2DocumentBundle().toBuilder()
                                        .applicationStatus(ApplicationStatus.SUBMITTED.getDisplayedValue())
                                        .build() : additionalApplicationsBundleElement.getValue().getC2DocumentBundle())
                                    .otherApplicationsBundle(ObjectUtils.isNotEmpty(additionalApplicationsBundleElement
                                                                                        .getValue().getOtherApplicationsBundle())
                                                                 ? additionalApplicationsBundleElement.getValue()
                                        .getOtherApplicationsBundle().toBuilder()
                                        .applicationStatus(ApplicationStatus.SUBMITTED.getDisplayedValue())
                                        .build() : additionalApplicationsBundleElement.getValue().getOtherApplicationsBundle())
                                    .build();
                                caseData.getAdditionalApplicationsBundle().set(
                                    caseData.getAdditionalApplicationsBundle().indexOf(
                                        additionalApplicationsBundleElement),
                                    element(
                                        additionalApplicationsBundleElement.getId(),
                                        additionalApplicationsBundle
                                    )
                                );
                            });
                    }

                    StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
                        = allTabService.getStartUpdateForSpecificEvent(
                        caseDetails.getId().toString(),
                        C100_HWF_PROCESS_AWP_STATUS_UPDATE.getValue()
                    );

                    caseDataUpdated.put(AWP_ADDTIONAL_APPLICATION_BUNDLE, caseData.getAdditionalApplicationsBundle());
                    caseDataUpdated.put("c100HwfRequestedForAdditionalApplications", YesOrNo.No);

                    //Save case data
                    allTabService.submitAllTabsUpdate(
                        startAllTabsUpdateDataContent.authorisation(),
                        caseDetails.getId().toString(),
                        startAllTabsUpdateDataContent.startEventResponse(),
                        startAllTabsUpdateDataContent.eventRequestData(),
                        caseDataUpdated
                    );

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

    public List<CaseDetails> retrieveCasesWithAwpHelpWithFeesInPendingState() {

        SearchResultResponse searchResultResponse = SearchResultResponse.builder()
            .cases(new ArrayList<>()).build();

        QueryParam ccdQueryParam = buildCcdQueryParam();

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
            log.error("Exception happened in parsing query param ", e);
        }

        if (null != searchResultResponse) {
            log.info("Total no. of cases retrieved {}", searchResultResponse.getTotal());
            return searchResultResponse.getCases();
        }
        return Collections.emptyList();
    }

    private QueryParam buildCcdQueryParam() {
        //C100 citizen cases with help with fess
        List<Should> shoulds = List.of(
            Should.builder()
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
                           .c100HwfRequestedForAdditionalApplications("Yes")
                           .build())
                .build()
        );

        LastModified lastModified = LastModified.builder().gte(LocalDateTime.now().minusDays(3).toString()).build();
        Range range = Range.builder().lastModified(lastModified).build();
        Filter filter = Filter.builder().range(range).build();

        Bool finalFilter = Bool.builder()
            .should(shoulds)
            .minimumShouldMatch(3)
            .filter(filter)
            .build();

        return QueryParam.builder()
            .query(Query.builder().bool(finalFilter).build())
            .build();
    }
}
