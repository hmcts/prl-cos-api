package uk.gov.hmcts.reform.prl.services.acro;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroResponse;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CaseManagementLocation;
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
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CANCELLED;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CoreCaseDataService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    @Value("${cafcaas.search-case-type-id}")
    private String cafCassSearchCaseTypeId;
    private final SystemUserService systemUserService;
    private final HearingService hearingService;

    public AcroResponse getCaseData(String authorisation) throws IOException {

        AcroResponse acroResponse = AcroResponse.builder().cases(new ArrayList<>()).build();

        try {

            ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            objectMapper.registerModule(new ParameterNamesModule());
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            QueryParam ccdQueryParam = buildCcdQueryParam();
            String searchString = objectMapper.writeValueAsString(ccdQueryParam);
            log.info("Search string: {}", searchString);
            String userToken = systemUserService.getSysUserToken();
            final String s2sToken = authTokenGenerator.generate();
            log.info("Invoking search cases");
            SearchResult searchResult = coreCaseDataApi.searchCases(
                userToken,
                searchString,
                s2sToken,
                cafCassSearchCaseTypeId
            );
            acroResponse = objectMapper.convertValue(
                searchResult,
                AcroResponse.class
            );
            if (acroResponse.getCases() != null && !acroResponse.getCases().isEmpty()) {
                log.info("CCD Search Result Size --> {}", acroResponse.getTotal());
                log.info("After applying filter Result Size --> {}", acroResponse.getTotal());
                AcroResponse updatecroData = updateDetailsForAllCases(authorisation, s2sToken, acroResponse);
                for (AcroCaseDetail acroCase : updatecroData.getCases()) {
                    log.info(
                        "Found case with id {} and courtName {} ",
                        acroCase.getId(), acroCase.getCaseData().getCourtName()
                    );
                }
                return AcroResponse.builder()
                    .cases(updatecroData.getCases())
                    .total(updatecroData.getCases().size())
                    .build();
            }
        } catch (Exception e) {
            log.error("Error in search cases {}", e.getMessage());
            throw e;
        }
        return acroResponse;
    }

    private QueryParam buildCcdQueryParam() {
        LastModified lastModified = LastModified.builder().gte(String.valueOf(LocalDateTime.of(
                LocalDate.now(ZoneId.systemDefault()).minusDays(1L), LocalTime.of(20, 59, 59))))
            .lte(String.valueOf(LocalDateTime.of(LocalDate.now(ZoneId.systemDefault()), LocalTime.of(21, 0, 0))))
            .build();

        List<Should> mustQuery = populateMustQuery(lastModified);
        Range range = Range.builder().lastModified(lastModified).build();
        StateFilter stateFilter = StateFilter.builder().should(mustQuery).build();
        Filter filter = Filter.builder().range(range).build();
        Must must = Must.builder().stateFilter(stateFilter).build();
        Bool bool = Bool.builder().filter(filter).must(must).build();
        Query query = Query.builder().bool(bool).build();
        return QueryParam.builder().query(query).dataToReturn(fetchFieldsRequiredForAcro()).build();
    }

    private List<Should> populateMustQuery(LastModified dateCreatedRange) {
        List<Should> should = new ArrayList<>();
        should.add(Should.builder().match(Match.builder().caseTypeOfApplication("FL401").build()).build());
        should.add(Should.builder().match(Match.builder().orderType("nonMolestation").build()).build());
        should.add(Should.builder().match(Match.builder().orderTypeId("Non-molestation order (FL404A)").build()).build());
        should.add(Should.builder().range(Range.builder().dateCreated(dateCreatedRange).build()).build());
        should.add(Should.builder().match(Match.builder().typeOfOrder("Final").build()).build());

        return should;
    }

    private List<String> fetchFieldsRequiredForAcro() {
        return List.of(
            "data.courtName",
            "data.courtEpimsId",
            "data.courtTypeId",
            "data.applicantsFL401",
            "data.respondentsFL401",
            "data.applicantsConfidentialDetails",
            "data.orderCollection"
        );
    }

    private AcroResponse updateDetailsForAllCases(String authorisation, String s2sToken, AcroResponse acroResponse) {
        AcroResponse filteredAcroResponse = AcroResponse.builder()
            .cases(new ArrayList<>())
            .build();
        Map<String, String> caseIdWithRegionIdMap = new HashMap<>();
        for (AcroCaseDetail caseDetails : acroResponse.getCases()) {
            updateCourtEpmisId(caseDetails, caseIdWithRegionIdMap, filteredAcroResponse);
            updateOrderExpiryDate(caseDetails);
        }
        List<Hearings> listOfHearingDetails = hearingService.getHearingsForAllCases(
            authorisation,
            caseIdWithRegionIdMap
        );

        filterCancelledHearingsBeforeListing(listOfHearingDetails);

        updateHearingData(acroResponse, listOfHearingDetails);

        return filteredAcroResponse;
    }

    private static void updateCourtEpmisId(AcroCaseDetail caseDetails, Map<String, String> caseIdWithRegionIdMap, AcroResponse filteredAcroResponse) {
        CaseManagementLocation caseManagementLocation = caseDetails.getCaseData().getCaseManagementLocation();
        if (caseManagementLocation != null) {
            if (caseManagementLocation.getRegionId() != null
                && Integer.parseInt(caseManagementLocation.getRegionId()) < 7) {
                caseIdWithRegionIdMap.put(
                    caseDetails.getId().toString(), caseManagementLocation.getRegionId()
                        + "-" + caseManagementLocation.getBaseLocationId()
                );
                caseDetails.getCaseData().setCourtEpimsId(caseManagementLocation.getBaseLocationId());
                filteredAcroResponse.getCases().add(caseDetails);
            } else if (caseManagementLocation.getRegion() != null && Integer.parseInt(caseManagementLocation.getRegion()) < 7) {
                caseIdWithRegionIdMap.put(
                    String.valueOf(caseDetails.getId()),
                    caseManagementLocation.getRegion() + "-" + caseManagementLocation.getBaseLocation()
                );
                caseDetails.getCaseData().setCourtEpimsId(caseManagementLocation.getBaseLocation());
                filteredAcroResponse.getCases().add(caseDetails);
            }
        }
    }

    private void updateOrderExpiryDate(AcroCaseDetail acroCaseDetail) {
        Optional<OrderDetails> orderDetails = acroCaseDetail.getCaseData().getOrderCollection().stream().map(Element::getValue)
            .filter(o -> o.getOrderType().equals("nonMolestation")
                && o.getOrderTypeId().equals("Non-molestation order (FL404A)")
                && o.getTypeOfOrder().equals("Final"))
            .findFirst();

        if (orderDetails.isPresent()) {
            OrderDetails order = orderDetails.get();
            acroCaseDetail.getCaseData().setFl404order(order);
            LocalDateTime fl404bDateOrderEnd = order.getFl404CustomFields().getFl404bDateOrderEnd();
            if (fl404bDateOrderEnd != null) {
                acroCaseDetail.getCaseData().setOrderExpiryDate(fl404bDateOrderEnd);
            }

        }
    }

    public void filterCancelledHearingsBeforeListing(List<Hearings> listOfHearingDetails) {
        if (null != listOfHearingDetails && !listOfHearingDetails.isEmpty()) {
            for (Hearings hearings : listOfHearingDetails) {
                List<CaseHearing> filteredCaseHearings = new ArrayList<>();
                hearings.getCaseHearings().forEach(caseHearing -> {
                    if (!checkIfHearingCancelledBeforeListing(caseHearing)) {
                        filteredCaseHearings.add(caseHearing);
                    }
                });
                hearings.setCaseHearings(filteredCaseHearings);
            }
        }
    }

    private static boolean checkIfHearingCancelledBeforeListing(CaseHearing caseHearing) {
        boolean hearingCancelledBeforeListing = false;
        if (CANCELLED.equals(caseHearing.getHmcStatus())
            && null != caseHearing.getHearingDaySchedule()) {
            for (HearingDaySchedule hearingDaySchedule : caseHearing.getHearingDaySchedule()) {
                if (ObjectUtils.isEmpty(hearingDaySchedule.getHearingStartDateTime())
                    && ObjectUtils.isEmpty(hearingDaySchedule.getHearingEndDateTime())) {
                    hearingCancelledBeforeListing = true;
                    break;
                }
            }
        }
        return hearingCancelledBeforeListing;
    }

    private void updateHearingData(AcroResponse acroResponse, List<Hearings> listOfHearingDetails) {
        if (null != listOfHearingDetails && !listOfHearingDetails.isEmpty()) {
            for (AcroCaseDetail acroCaseDetail : acroResponse.getCases()) {
                Hearings filteredHearing =
                    listOfHearingDetails.stream().filter(hearings -> hearings.getCaseRef().equals(String.valueOf(
                        acroCaseDetail.getId()))).findFirst().orElse(null);

                if (filteredHearing != null && CollectionUtils.isNotEmpty(filteredHearing.getCaseHearings())) {
                    acroCaseDetail.getCaseData().setHearingData(filteredHearing);
                    acroCaseDetail.getCaseData().setCourtName(filteredHearing.getCourtName());
                    acroCaseDetail.getCaseData().setCourtTypeId(filteredHearing.getCourtTypeId());
                    filteredHearing.setCourtName(null);
                    filteredHearing.setCourtTypeId(null);
                    filteredHearing.getCaseHearings().forEach(
                        caseHearing -> {
                            if (CollectionUtils.isNotEmpty(caseHearing.getHearingDaySchedule())) {
                                caseHearing.getHearingDaySchedule().forEach(
                                    hearingDaySchedule -> {
                                        hearingDaySchedule.setEpimsId(hearingDaySchedule.getHearingVenueId());
                                        hearingDaySchedule.setHearingVenueId(null);
                                    }
                                );
                            }
                        });
                }
            }
        }
    }
}
