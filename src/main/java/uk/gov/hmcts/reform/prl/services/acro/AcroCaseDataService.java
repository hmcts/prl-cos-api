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
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;
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
public class AcroCaseDataService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final AcroCaseSearchService acroCaseSearchService;
    private final AuthTokenGenerator authTokenGenerator;
    @Value("${cafcaas.search-case-type-id}")
    private String searchCaseTypeId;
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
            String userToken = systemUserService.getSysUserToken();
            final String s2sToken = authTokenGenerator.generate();
            log.info("Invoking search cases");
            SearchResult searchResult = acroCaseSearchService.searchCases(
                userToken,
                searchString,
                s2sToken,
                searchCaseTypeId
            );
            acroResponse = objectMapper.convertValue(
                searchResult,
                AcroResponse.class
            );
            if (acroResponse.getCases() != null && !acroResponse.getCases().isEmpty()) {
                log.info("CCD Search Result Size --> {}", acroResponse.getTotal());
                log.info("Extracting data needed for ACRO --> {}", acroResponse.getTotal());
                AcroResponse updatedAcroData = extractCaseDetailsForAllCases(authorisation, s2sToken, acroResponse);
                for (AcroCaseDetail acroCase : updatedAcroData.getCases()) {
                    log.info(
                        "Found case with id {} and courtName {} ",
                        acroCase.getId(), acroCase.getCaseData().getCourtName()
                    );
                }
                return AcroResponse.builder()
                    .cases(updatedAcroData.getCases())
                    .total(updatedAcroData.getCases().size())
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
        Bool bool = Bool.builder().filter(filter).must(must).minimumShouldMatch(2).build();
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
            "data.orderCollection",
            "data.caseManagementLocation",
            "currentHearingId"
        );
    }

    private AcroResponse extractCaseDetailsForAllCases(String authorisation, String s2sToken, AcroResponse acroResponse) {
        AcroResponse extractedAcroResponse = AcroResponse.builder()
            .cases(new ArrayList<>())
            .build();
        Map<String, String> caseIdWithRegionIdMap = new HashMap<>();
        for (AcroCaseDetail caseDetails : acroResponse.getCases()) {
            updateCourtEpmisId(caseDetails, caseIdWithRegionIdMap, extractedAcroResponse);
            extractOrderSpecificData(caseDetails);
            extractedAcroResponse.getCases().add(caseDetails);
        }
        List<Hearings> listOfHearingDetails = hearingService.getHearingsForAllCases(
            authorisation,
            caseIdWithRegionIdMap
        );

        filterCancelledHearingsBeforeListing(listOfHearingDetails);

        extractHearingData(acroResponse, listOfHearingDetails);

        return extractedAcroResponse;
    }

    private static void updateCourtEpmisId(AcroCaseDetail caseDetails, Map<String, String> caseIdWithRegionIdMap, AcroResponse filteredAcroResponse) {
        CaseManagementLocation caseManagementLocation = caseDetails.getCaseData().getCaseManagementLocation();
        if (caseManagementLocation != null) {
            if (caseManagementLocation.getRegionId() != null) {
                caseIdWithRegionIdMap.put(
                    caseDetails.getId().toString(), caseManagementLocation.getRegionId()
                        + "-" + caseManagementLocation.getBaseLocationId()
                );
                caseDetails.getCaseData().setCourtEpimsId(caseManagementLocation.getBaseLocationId());
                filteredAcroResponse.getCases().add(caseDetails);
            } else if (caseManagementLocation.getRegion() != null) {
                caseIdWithRegionIdMap.put(
                    String.valueOf(caseDetails.getId()),
                    caseManagementLocation.getRegion() + "-" + caseManagementLocation.getBaseLocation()
                );
                caseDetails.getCaseData().setCourtEpimsId(caseManagementLocation.getBaseLocation());
                filteredAcroResponse.getCases().add(caseDetails);
            }
        }
    }

    private void extractOrderSpecificData(AcroCaseDetail acroCaseDetail) {
        AcroCaseData caseData = acroCaseDetail.getCaseData();
        Optional<OrderDetails> orderDetails = caseData.getOrderCollection().stream().map(Element::getValue)
            .filter(o -> o.getOrderType().equals("nonMolestation")
                && o.getOrderTypeId().equals("Non-molestation order (FL404A)")
                && o.getTypeOfOrder().equals("Final"))
            .findFirst();

        if (orderDetails.isPresent()) {
            OrderDetails order = orderDetails.get();
            caseData.getFl404Orders().add(order);
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

    private void extractHearingData(AcroResponse acroResponse, List<Hearings> listOfHearingDetails) {
        if (null != listOfHearingDetails && !listOfHearingDetails.isEmpty()) {
            for (AcroCaseDetail acroCaseDetail : acroResponse.getCases()) {
                Hearings filteredHearing =
                    listOfHearingDetails.stream().filter(hearings -> hearings.getCaseRef().equals(String.valueOf(
                        acroCaseDetail.getId()))).findFirst().orElse(null);

                if (filteredHearing != null && CollectionUtils.isNotEmpty(filteredHearing.getCaseHearings())) {
                    acroCaseDetail.getCaseData().setCaseHearings(filteredHearing.getCaseHearings());
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
