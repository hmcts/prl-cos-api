package uk.gov.hmcts.reform.prl.services.cafcass;

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
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
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
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CANCELLED;

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

    private final CoreCaseDataApi coreCaseDataApi;

    @Value("#{'${cafcaas.excludedDocumentCategories}'.split(',')}")
    private List<String> excludedDocumentCategoryList;

    @Value("#{'${cafcaas.excludedDocuments}'.split(',')}")
    private List<String> excludedDocumentList;

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
                log.info("Query params : {}", ccdQueryParam);
                String searchString = objectMapper.writeValueAsString(ccdQueryParam);

                String userToken = systemUserService.getSysUserToken();
                final String s2sToken = authTokenGenerator.generate();
                SearchResult searchResult = cafcassCcdDataStoreService.searchCases(
                    userToken,
                    searchString,
                    s2sToken,
                    cafCassSearchCaseTypeId
                );
                log.info("Search result response  {}", searchResult);
                cafCassResponse = objectMapper.convertValue(
                    searchResult,
                    CafCassResponse.class
                );
                log.info("Cafcass response  {}", cafCassResponse);
                if (cafCassResponse.getCases() != null && !cafCassResponse.getCases().isEmpty()) {
                    log.info("CCD Search Result Size --> {}", cafCassResponse.getTotal());
                    addSpecificDocumentsFromCaseFileViewBasedOnCategories(cafCassResponse);
                    cafCassFilter.filter(cafCassResponse);
                    log.info("After applying filter Result Size --> {}", cafCassResponse.getTotal());
                    CafCassResponse filteredCafcassData = getHearingDetailsForAllCases(authorisation, cafCassResponse);
                    updateHearingResponse(authorisation, s2sToken, filteredCafcassData);
                    return CafCassResponse.builder()
                        .cases(filteredCafcassData.getCases())
                        .total(filteredCafcassData.getCases().size())
                        .build();
                }
            }
        } catch (Exception e) {
            log.error("Error in search cases {}", e.getMessage());
            throw e;
        }
        return cafCassResponse;
    }

    private void addSpecificDocumentsFromCaseFileViewBasedOnCategories(CafCassResponse cafCassResponse) {

        String systemAuthorisation = systemUserService.getSysUserToken();
        String serviceAuthorisation = authTokenGenerator.generate();
        cafCassResponse.getCases().forEach(cafCassCaseDetail -> {
            CategoriesAndDocuments categoriesAndDocuments = coreCaseDataApi.getCategoriesAndDocuments(
                systemAuthorisation,
                serviceAuthorisation,
                String.valueOf(cafCassCaseDetail.getId())
            );
            List<Element<uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments>> otherDocsList = new ArrayList<>();

            List<Category> parentCategories = categoriesAndDocuments.getCategories().stream()
                .sorted(Comparator.comparing(Category::getCategoryName))
                .toList();
            parseCategoryAndCreateList(parentCategories, otherDocsList);
            CafCassCaseData caseData = cafCassCaseDetail.getCaseData();
            final CafCassCaseData cafCassCaseData = caseData.toBuilder()
                .otherDocuments(otherDocsList)
                .build();
            cafCassCaseDetail.setCaseData(cafCassCaseData);
        });


    }

    private void parseCategoryAndCreateList(List<Category> parentCategories,
                                            List<Element<uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments>> otherDocsList) {
        parentCategories.forEach(category -> {
            if (CollectionUtils.isEmpty(excludedDocumentCategoryList) || !excludedDocumentCategoryList.contains(
                category.getCategoryId())) {
                if (category.getSubCategories() != null) {
                    parseCategoryAndCreateList(category.getSubCategories(), otherDocsList);
                }
                parseCfvDocuments(otherDocsList, category);
            }
        });
    }

    private void parseCfvDocuments(List<Element<OtherDocuments>> otherDocsList, Category category) {
        category.getDocuments().forEach(document -> {
            if (CollectionUtils.isEmpty(excludedDocumentList)
                || !checkIfDocumentsNeedToExclude(excludedDocumentList, document.getDocumentFilename())) {
                try {
                    otherDocsList.add(Element.<OtherDocuments>builder().id(
                        UUID.randomUUID()).value(OtherDocuments.builder().documentOther(
                        buildFromCfvDocument(document)).documentName(document.getDocumentFilename()).documentTypeOther(
                        DocTypeOtherDocumentsEnum.getValue(category.getCategoryId())).build()).build());
                } catch (MalformedURLException e) {
                    log.error("Error in populating otherDocsList for CAFCASS {}", e.getMessage());
                }
            }
        });
    }

    public boolean checkIfDocumentsNeedToExclude(List<String> excludedDocumentList, String documentFilename) {
        boolean isExcluded = false;
        for (String excludedDocumentName : excludedDocumentList) {
            if (documentFilename.contains(excludedDocumentName)) {
                isExcluded = true;
            }
        }
        return isExcluded;
    }

    public Document buildFromCfvDocument(uk.gov.hmcts.reform.ccd.client.model.Document cfvDocument) throws MalformedURLException {
        URL url = new URL(cfvDocument.getDocumentURL());
        return Document.builder()
            .documentUrl(CafCassCaseData.getDocumentId(url))
            .documentFileName(cfvDocument.getDocumentFilename())
            .build();
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
        Bool bool = Bool.builder().filter(filter).should(applicationTypes).minimumShouldMatch(2).must(must).build();
        Query query = Query.builder().bool(bool).build();
        return QueryParam.builder().query(query).size(ccdElasticSearchApiResultSize)
            .dataToReturn(fetchFieldsRequiredForCafcass()).build();
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
            shoulds.add(Should.builder().match(Match.builder().cafcassServedOptions(YesOrNo.Yes).build()).build());
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
                    caseDetails.getCaseData().setCafcassUploadedDocs(null);
                    filteredCafcassResponse.getCases().add(caseDetails);
                }
            }
        }
        List<Hearings> listOfHearingDetails = hearingService.getHearingsForAllCases(
            authorisation,
            caseIdWithRegionIdMap
        );

        //PRL-6431
        filterCancelledHearingsBeforeListing(listOfHearingDetails);
        updateHearingDataCafcass(filteredCafcassResponse, listOfHearingDetails);
        return filteredCafcassResponse;
    }

    public void filterCancelledHearingsBeforeListing(List<Hearings> listOfHearingDetails) {
        List<CaseHearing> filteredCaseHearings = new ArrayList<>();
        if (null != listOfHearingDetails && !listOfHearingDetails.isEmpty()) {
            for (Hearings hearings : listOfHearingDetails) {
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

    private List<String> fetchFieldsRequiredForCafcass() {
        return List.of(
            "submitAndPayDownloadApplicationLink",
            "c8Document",
            "c1ADocument",
            "familymanCaseNumber",
            "dateSubmitted",
            "issueDate",
            "caseTypeOfApplication",
            "draftConsentOrderFile",
            "confidentialDetails",
            "isInterpreterNeeded",
            "interpreterNeeds",
            "childrenKnownToLocalAuthority",
            "otherDocuments",
            "finalDocument",
            "ordersApplyingFor",
            "children",
            "miamCertificationDocumentUpload1",
            "miamStatus",
            "miamExemptionsTable",
            "claimingExemptionMiam",
            "applicantAttendedMiam",
            "familyMediatorMiam",
            "otherProceedingsMiam",
            "applicantConsentMiam",
            "mediatorRegistrationNumber",
            "familyMediatorServiceName",
            "soleTraderName",
            "mpuChildInvolvedInMiam",
            "mpuApplicantAttendedMiam",
            "mpuClaimingExemptionMiam",
            "mpuExemptionReasons",
            "miamDomesticAbuseEvidences",
            "mpuDomesticAbuseEvidences",
            "mpuIsDomesticAbuseEvidenceProvided",
            "mpuDomesticAbuseEvidenceDocument",
            "mpuNoDomesticAbuseEvidenceReason",
            "mpuUrgencyReason",
            "miamUrgencyReason",
            "mpuPreviousMiamAttendanceReason",
            "miamPreviousAttendanceReason",
            "mpuDocFromDisputeResolutionProvider",
            "mpuTypeOfPreviousMiamAttendanceEvidence",
            "miamTypeOfPreviousAttendanceEvidence",
            "mpuCertificateByMediator",
            "mpuMediatorDetails",
            "mpuOtherExemptionReasons",
            "miamOtherExemptionReasons",
            "mpuApplicantUnableToAttendMiamReason1",
            "mpuApplicantUnableToAttendMiamReason2",
            "miamCertificationDocumentUpload",
            "mpuChildProtectionConcernReason",
            "miamChildProtectionConcernReason",
            "miamTable",
            "summaryTabForOrderAppliedFor",
            "partyIdAndPartyTypeMap",
            "applicants",
            "respondents",
            "applicantsConfidentialDetails",
            "applicantSolicitorEmailAddress",
            "solicitorName",
            "courtEpimsId",
            "courtTypeId",
            "courtName",
            "otherPeopleInTheCaseTable",
            "ordersNonMolestationDocument",
            "hearingOutComeDocument",
            "manageOrderCollection",
            "hearingData",
            "orderCollection",
            "caseManagementLocation",
            "otherPartyInTheCaseRevised",
            "newChildDetails",
            "childAndApplicantRelations",
            "childAndRespondentRelations",
            "childAndOtherPeopleRelations",
            "cafcassUploadedDocs"
        );
    }
}
