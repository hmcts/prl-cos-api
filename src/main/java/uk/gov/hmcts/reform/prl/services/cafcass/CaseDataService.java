package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.ApplicantDetails;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseData;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Document;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Element;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments;
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
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseDataService {
    @Value("${cafcaas.search-case-type-id}")
    private String cafCassSearchCaseTypeId;

    @Value("${ccd.elastic-search-api.result-size}")
    private String ccdElasticSearchApiResultSize;

    @Value("${ccd.elastic-search-api.boost}")
    private String ccdElasticSearchApiBoost;

    @Value("#{'${cafcaas.caseState}'.split(',')}")
    private List<String> caseStateList;

    @Value("#{'${cafcaas.caseTypeOfApplicationList}'.split(',')}")
    private List<String> caseTypeList;

    @Value("${refdata.category-id}")
    private String categoryId;

    private final HearingService hearingService;

    private final CafcassCcdDataStoreService cafcassCcdDataStoreService;

    private final CafCassFilter cafCassFilter;

    private final AuthTokenGenerator authTokenGenerator;

    private final SystemUserService systemUserService;

    private final RefDataService refDataService;

    private final OrganisationService organisationService;

    public CafCassResponse getCaseData(String authorisation, String startDate, String endDate) throws IOException {

        log.info("Search API start date - {}, end date - {}", startDate, endDate);

        CafCassResponse cafCassResponse = CafCassResponse.builder().cases(new ArrayList<>()).build();

        try {
            if (caseTypeList != null && !caseTypeList.isEmpty()) {
                caseTypeList = caseTypeList.stream().map(String::trim).toList();

                ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                objectMapper.registerModule(new ParameterNamesModule());
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

                QueryParam ccdQueryParam = buildCcdQueryParam(startDate, endDate);
                String searchString = objectMapper.writeValueAsString(ccdQueryParam);

                String userToken = systemUserService.getSysUserToken();
                final String s2sToken = authTokenGenerator.generate();
                SearchResult searchResult = cafcassCcdDataStoreService.searchCases(
                    userToken,
                    searchString,
                    s2sToken,
                    cafCassSearchCaseTypeId
                );

                cafCassResponse = objectMapper.convertValue(
                    searchResult,
                    CafCassResponse.class
                );

                if (cafCassResponse.getCases() != null && !cafCassResponse.getCases().isEmpty()) {

                    log.info("CCD Search Result Size --> {}", cafCassResponse.getTotal());
                    cafCassFilter.filter(cafCassResponse);
                    log.info("After applying filter Result Size --> {}", cafCassResponse.getTotal());
                    CafCassResponse filteredCafcassData = getHearingDetailsForAllCases(authorisation, cafCassResponse);
                    updateHearingResponse(authorisation, s2sToken, filteredCafcassData);
                    updateSolicitorAddressForParties(filteredCafcassData);
                    return CafCassResponse.builder()
                        .cases(filteredCafcassData.getCases())
                        .total(filteredCafcassData.getCases().size())
                        .build();
                }
            }
        } catch (Exception e) {
            log.error("Error in search cases {}", e);
            throw e;
        }
        return cafCassResponse;
    }

    private void updateSolicitorAddressForParties(CafCassResponse filteredCafcassData) {
        Map<String, Address> orgIdToAddressMap = new HashMap<>();
        List<String> orgIdListForAllCases = new ArrayList<>();
        filteredCafcassData.getCases().stream().forEach(
            caseDetail -> {
                CafCassCaseData cafCassCaseData = caseDetail.getCaseData();
                orgIdListForAllCases.addAll(cafCassCaseData.getApplicants().stream()
                    .filter(party -> party.getValue().getSolicitorOrg() != null)
                    .map(partyDetail -> partyDetail.getValue().getSolicitorOrg().getOrganisationID())
                    .collect(Collectors.toList()));
                orgIdListForAllCases.addAll(cafCassCaseData.getRespondents().stream()
                                                   .filter(party -> party.getValue().getSolicitorOrg() != null)
                                                   .map(partyDetail -> partyDetail.getValue().getSolicitorOrg().getOrganisationID())
                                                   .toList());
            });
        orgIdListForAllCases.stream().distinct()
            .forEach(orgId ->
                         orgIdToAddressMap.put(
                             orgId,
                             organisationService.getOrganisationDetails(
                                     systemUserService.getSysUserToken(),
                                     orgId
                                 )
                                 .getContactInformation().get(0).toAddress()
                         ));


        filteredCafcassData.getCases().stream().forEach(
            caseDetail -> {
                CafCassCaseData cafCassCaseData = caseDetail.getCaseData();
                cafCassCaseData = cafCassCaseData.toBuilder()
                    .applicants(cafCassCaseData.getApplicants().stream()
                                     .map(updatedParty -> {
                                         if (updatedParty.getValue().getSolicitorOrg() == null) {
                                             return updatedParty;
                                         }
                                         Address address = orgIdToAddressMap.get(updatedParty.getValue().getSolicitorOrg().getOrganisationID());
                                         return Element.<ApplicantDetails>builder().id(updatedParty.getId())
                                             .value(updatedParty.getValue().toBuilder()
                                                        .solicitorAddress(
                                                            address != null
                                                                ? uk.gov.hmcts.reform.prl.models.dto.cafcass.Address.builder()
                                                                .addressLine1(address.getAddressLine1())
                                                                .addressLine2(address.getAddressLine2())
                                                                .addressLine3(address.getAddressLine3())
                                                                .county(address.getCounty())
                                                                .country(address.getCountry())
                                                                .postTown(address.getPostTown())
                                                                .postCode(address.getPostCode())
                                                                .build() : null
                                                        )
                                                        .build()).build();
                                     })
                                     .toList())
                    .respondents(cafCassCaseData.getRespondents().stream()
                                     .map(updatedParty -> {
                                         if (updatedParty.getValue().getSolicitorOrg() == null) {
                                             return updatedParty;
                                         }
                                         Address address = orgIdToAddressMap.get(updatedParty.getValue().getSolicitorOrg().getOrganisationID());
                                         return Element.<ApplicantDetails>builder().id(updatedParty.getId())
                                             .value(updatedParty.getValue().toBuilder()
                                                        .solicitorAddress(
                                                            address != null
                                                                ? uk.gov.hmcts.reform.prl.models.dto.cafcass.Address.builder()
                                                                .addressLine1(address.getAddressLine1())
                                                                .addressLine2(address.getAddressLine2())
                                                                .addressLine3(address.getAddressLine3())
                                                                .county(address.getCounty())
                                                                .country(address.getCountry())
                                                                .postTown(address.getPostTown())
                                                                .postCode(address.getPostCode())
                                                                .build() : null
                                                        )
                                                        .build()).build();
                                     })
                                     .toList())
                    .build();
                caseDetail.setCaseData(cafCassCaseData);
            });

    }

    private QueryParam buildCcdQueryParam(String startDate, String endDate) {

        // set or condition for caseTypeofApplication (e.g. something like -
        // caseTypeofApplication = C100 or caseTypeofApplication - FL401
        List<Should> applicationTypes = populateCaseTypeOfApplicationForSearchQuery();

        List<Should> shoulds = populateStatesForQuery();

        LastModified lastModified = LastModified.builder().gte(startDate).lte(endDate).boost(ccdElasticSearchApiBoost)
            .build();
        Range range = Range.builder().lastModified(lastModified).build();

        StateFilter stateFilter = StateFilter.builder().should(shoulds).build();
        Filter filter = Filter.builder().range(range).build();
        Must must = Must.builder().stateFilter(stateFilter).build();
        Bool bool = Bool.builder().filter(filter).should(applicationTypes).minimumShouldMatch(1).must(must).build();
        Query query = Query.builder().bool(bool).build();
        return QueryParam.builder().query(query).size(ccdElasticSearchApiResultSize).build();
    }

    private List<Should> populateStatesForQuery() {
        caseStateList = caseStateList.stream().map(String::trim).toList();

        List<Should> shoulds = new ArrayList<>();
        if (caseStateList != null && !caseStateList.isEmpty()) {
            for (String caseState : caseStateList) {
                shoulds.add(Should.builder().match(Match.builder().state(caseState).build()).build());
            }
        }
        return shoulds;
    }

    private List<Should> populateCaseTypeOfApplicationForSearchQuery() {

        List<Should> shoulds = new ArrayList<>();
        for (String caseType : caseTypeList) {
            shoulds.add(Should.builder().match(Match.builder().caseTypeOfApplication(caseType).build()).build());
        }
        return shoulds;
    }

    private CafCassResponse getHearingDetailsForAllCases(String authorisation, CafCassResponse cafCassResponse) {
        CafCassResponse filteredCafcassResponse = CafCassResponse.builder()
            .cases(new ArrayList<>())
            .build();
        Map<String, String> caseIdWithRegionIdMap = new HashMap<>();
        for (CafCassCaseDetail caseDetails : cafCassResponse.getCases()) {
            CaseManagementLocation caseManagementLocation = caseDetails.getCaseData().getCaseManagementLocation();
            if (caseManagementLocation != null) {
                if (caseManagementLocation.getRegionId() != null
                    && Integer.parseInt(caseManagementLocation.getRegionId()) < 7) {
                    caseIdWithRegionIdMap.put(caseDetails.getId().toString(), caseManagementLocation.getRegionId()
                        + "-" + caseManagementLocation.getBaseLocationId());
                    caseDetails.getCaseData().setCourtEpimsId(caseManagementLocation.getBaseLocationId());
                    filteredCafcassResponse.getCases().add(caseDetails);
                } else if (caseManagementLocation.getRegion() != null && Integer.parseInt(caseManagementLocation.getRegion()) < 7) {
                    caseIdWithRegionIdMap.put(
                        String.valueOf(caseDetails.getId()),
                        caseManagementLocation.getRegion() + "-" + caseManagementLocation.getBaseLocation()
                    );
                    caseDetails.getCaseData().setCourtEpimsId(caseManagementLocation.getBaseLocation());
                    caseDetails.getCaseData().setOtherDocuments(updateOtherDocuments(caseDetails.getCaseData()));
                    caseDetails.getCaseData().setCafcassUploadedDocs(null);
                    filteredCafcassResponse.getCases().add(caseDetails);
                }
            }
        }
        List<Hearings> listOfHearingDetails = hearingService.getHearingsForAllCases(
            authorisation,
            caseIdWithRegionIdMap
        );

        updateHearingDataCafcass(filteredCafcassResponse, listOfHearingDetails);
        return filteredCafcassResponse;
    }

    private List<Element<OtherDocuments>> updateOtherDocuments(CafCassCaseData caseData) {
        List<Element<OtherDocuments>> otherDocsList = CollectionUtils.isNotEmpty(caseData.getOtherDocuments())
            ? caseData.getOtherDocuments() : new ArrayList<>();
        final @NotNull @Valid UploadedDocuments[] uploadedDocs = new UploadedDocuments[1];
        if (caseData.getCafcassUploadedDocs() != null) {
            caseData.getCafcassUploadedDocs().stream().forEach(
                uploadedDocumentsElement -> {
                    uploadedDocs[0] = uploadedDocumentsElement.getValue();
                    otherDocsList.add(Element.<OtherDocuments>builder()
                                          .id(uploadedDocumentsElement.getId())
                                          .value(OtherDocuments.builder()
                                                     .documentOther(Document.buildFromPrlDocument(uploadedDocs[0].getCafcassDocument()))
                                                     .documentName(uploadedDocs[0].getCafcassDocument().getDocumentFileName())
                                                     .documentTypeOther(DocTypeOtherDocumentsEnum.cafcassReports)
                                                     .build())
                                          .build());
                }
            );

        }
        return otherDocsList;
    }

    private void updateHearingDataCafcass(CafCassResponse filteredCafcassResponse, List<Hearings> listOfHearingDetails) {
        if (null != listOfHearingDetails && !listOfHearingDetails.isEmpty()) {
            for (CafCassCaseDetail cafCassCaseDetail : filteredCafcassResponse.getCases()) {
                Hearings filteredHearing =
                    listOfHearingDetails.stream().filter(hearings -> hearings.getCaseRef().equals(String.valueOf(
                        cafCassCaseDetail.getId()))).findFirst().orElse(null);
                if (filteredHearing != null) {
                    cafCassCaseDetail.getCaseData().setHearingData(filteredHearing);
                    cafCassCaseDetail.getCaseData().setCourtName(filteredHearing.getCourtName());
                    cafCassCaseDetail.getCaseData().setCourtTypeId(filteredHearing.getCourtTypeId());
                    filteredHearing.setCourtName(null);
                    filteredHearing.setCourtTypeId(null);
                    filteredHearing.getCaseHearings().forEach(
                        caseHearing -> caseHearing.getHearingDaySchedule().forEach(
                                hearingDaySchedule -> {
                                    hearingDaySchedule.setEpimsId(hearingDaySchedule.getHearingVenueId());
                                    hearingDaySchedule.setHearingVenueId(null);
                                }
                            )
                    );
                }
            }
        }
    }

    private void updateHearingResponse(String authorisation, String s2sToken, CafCassResponse cafCassResponse) {

        Map<String, String> refDataCategoryValueMap = null;

        for (CafCassCaseDetail cafCassCaseDetail : cafCassResponse.getCases()) {
            final Hearings hearingData = cafCassCaseDetail.getCaseData().getHearingData();
            if (null != hearingData) {

                if (refDataCategoryValueMap == null) {
                    refDataCategoryValueMap = refDataService.getRefDataCategoryValueMap(
                        authorisation,
                        s2sToken,
                        hearingData.getHmctsServiceCode(),
                        categoryId
                    );
                }

                for (CaseHearing caseHearing : hearingData.getCaseHearings()) {
                    caseHearing.setHearingTypeValue(refDataCategoryValueMap.get(caseHearing.getHearingType()));
                }
            }
        }
    }
}
