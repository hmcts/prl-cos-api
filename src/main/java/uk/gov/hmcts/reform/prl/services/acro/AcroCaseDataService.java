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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CANCELLED;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AcroCaseDataService {

    public static final String NON_MOLESTATION_ORDER_FL_404_A = "Non-molestation order (FL404A)";
    public static final String NON_MOLESTATION = "nonMolestation";
    public static final String FINAL = "Final";
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    @Value("${acro.search-case-type-id}")
    private String searchCaseTypeId;
    private final SystemUserService systemUserService;
    private final HearingService hearingService;
    private final AcroDatesService acroDatesService;

    public AcroResponse getCaseData(String authorisation) throws IOException {

        AcroResponse acroResponse = AcroResponse.builder().cases(new ArrayList<>()).build();

        try {

            ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            objectMapper.registerModule(new ParameterNamesModule());
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            LocalDateTime startDateForSearch = acroDatesService.getStartDateForSearch();
            LocalDateTime endDateForSearch = acroDatesService.getEndDateForSearch();
            QueryParam ccdQueryParam = buildCcdQueryParam(startDateForSearch, endDateForSearch);
             String searchString = objectMapper.writeValueAsString(ccdQueryParam);
            //String searchString = buildRawJsonQuery(startDateForSearch, endDateForSearch, objectMapper);
            log.info("Search string: {}", searchString);
            String userToken = systemUserService.getSysUserToken();
            final String s2sToken = authTokenGenerator.generate();
            log.info("Invoking search cases");
            SearchResult searchResult = coreCaseDataApi.searchCases(
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

                List<AcroCaseDetail> validCases = acroResponse.getCases().stream()
                    .filter(c -> isValidForCaseTypeOfApplication(
                        c.getCaseData())).toList();
                AcroResponse filteredResponse = acroResponse.toBuilder().cases(validCases).build();
                AcroResponse updatedAcroData = extractCaseDetailsForAllCases(
                    authorisation,
                    s2sToken,
                    filteredResponse,
                    startDateForSearch,
                    endDateForSearch
                );
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

    private boolean isValidForCaseTypeOfApplication(AcroCaseData caseData) {
        String caseTypeOfApplication = caseData.getCaseTypeOfApplication();
        return (caseTypeOfApplication.equals("C100")
            && caseData.getApplicants() != null
            && caseData.getApplicants().size() == 1
            && caseData.getRespondents() != null
            && caseData.getRespondents().size() == 1)
            || caseTypeOfApplication.equals(
            "FL401");
    }

    private QueryParam buildCcdQueryParam(LocalDateTime startDateForSearch, LocalDateTime endDateForSearch) {
        LastModified lastModified = LastModified.builder()
            .gte(String.valueOf(startDateForSearch))
            .lte(String.valueOf(endDateForSearch))
            .build();

        List<Should> mustQuery = populateMustQuery(lastModified);
        Range range = Range.builder().lastModified(lastModified).build();
        Bool bool = Bool.builder()
            .filter(Filter.builder().range(range).build())
            .must(Must.builder()
                      .stateFilter(StateFilter.builder().should(mustQuery).minimumShouldMatch(2).build())
                      .build())
            .build();
        Query query = Query.builder().bool(bool).build();
        return QueryParam.builder().query(query).dataToReturn(fetchFieldsRequiredForAcro()).build();
    }

    private String buildRawJsonQuery(LocalDateTime startDateForSearch, LocalDateTime endDateForSearch, ObjectMapper objectMapper) {
        try {
            return """
                {
                    "query": {
                        "bool": {
                            "filter": {
                                "range": {
                                    "last_modified": {
                                        "gte": "2025-08-18T00:59:59",
                                        "lte": "2025-08-27T21:00"
                                    }
                                }
                            },
                            "must": [
                                {
                                    "term": {
                                        "data.orderCollection.value.orderTypeId.keyword": "Non-molestation order (FL404A)"
                                    }
                                },
                                {
                                    "range": {
                                        "data.orderCollection.value.dateCreated": {
                                            "gte": "2025-08-18T00:59:59",
                                            "lte": "2025-08-27T21:00"
                                        }
                                    }
                                }
                            ]
                        }
                    },
                    "_source": [
                        "data.caseTypeOfApplication",
                        "data.courtName",
                        "data.courtEpimsId",
                        "data.courtTypeId",
                        "data.applicantsFL401",
                        "data.applicants",
                        "data.respondentsFL401",
                        "data.respondents",
                        "data.applicantsConfidentialDetails",
                        "data.orderCollection",
                        "data.caseManagementLocation",
                        "data.stmtOfServiceForOrder"
                    ]
                }
                """;
        } catch (Exception e) {
            log.error("Error building JSON query: {}", e.getMessage());
            throw new RuntimeException("Failed to build search query", e);
        }
    }

    private List<Should> populateMustQuery(LastModified dateCreatedRange) {
        List<Should> should = new ArrayList<>();
        //should.add(Should.builder().match(Match.builder().orderType(NON_MOLESTATION).build()).build());
        should.add(Should.builder().match(Match.builder().orderTypeId(NON_MOLESTATION_ORDER_FL_404_A).build()).build());
        should.add(Should.builder().range(Range.builder().dateCreated(dateCreatedRange).build()).build());
        //should.add(Should.builder().match(Match.builder().typeOfOrder(FINAL).build()).build());

        return should;
    }

    private List<String> fetchFieldsRequiredForAcro() {
        return List.of(
            "data.caseTypeOfApplication",
            "data.courtName",
            "data.courtEpimsId",
            "data.courtTypeId",
            "data.applicantsFL401",
            "data.applicants",
            "data.respondentsFL401",
            "data.respondents",
            "data.applicantsConfidentialDetails",
            "data.orderCollection",
            "data.caseManagementLocation",
            "data.stmtOfServiceForOrder"
        );
    }

    private AcroResponse extractCaseDetailsForAllCases(
        String authorisation, String s2sToken, AcroResponse acroResponse,
        LocalDateTime startDateForSearch, LocalDateTime endDateForSearch) {
        AcroResponse extractedAcroResponse = AcroResponse.builder()
            .cases(new ArrayList<>())
            .build();
        Map<String, String> caseIdWithRegionIdMap = new HashMap<>();
        for (AcroCaseDetail caseDetails : acroResponse.getCases()) {
            updateCourtEpmisId(caseDetails, caseIdWithRegionIdMap, extractedAcroResponse);
            extractOrderSpecificForSearchCrieteria(caseDetails, startDateForSearch, endDateForSearch);
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

    private void updateCourtEpmisId(AcroCaseDetail caseDetails,
                                    Map<String, String> caseIdWithRegionIdMap,
                                    AcroResponse filteredAcroResponse) {
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

    private void extractOrderSpecificForSearchCrieteria(AcroCaseDetail acroCaseDetail,
                                                        LocalDateTime startDateForSearch,
                                                        LocalDateTime endDateForSearch) {
        AcroCaseData caseData = acroCaseDetail.getCaseData();
        caseData.getOrderCollection().stream().map(Element::getValue)
            .filter(o -> o.getOrderType().equals(NON_MOLESTATION)
                && o.getOrderTypeId().equals(NON_MOLESTATION_ORDER_FL_404_A)
                && o.getTypeOfOrder().equals(FINAL)
                && o.getDateCreated().isAfter(startDateForSearch)
                && o.getDateCreated().isBefore(endDateForSearch)
            )
            .forEach(order -> caseData.getFl404Orders().add(order));
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

    private boolean checkIfHearingCancelledBeforeListing(CaseHearing caseHearing) {
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
                    listOfHearingDetails.stream()
                        .filter(h -> h.getCaseRef().equals(String.valueOf(acroCaseDetail.getId())))
                        .findFirst().orElse(null);

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
