package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
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
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassCaseDataService {

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

    private final ObjectMapper objMapper;

    private final FeatureToggleService featureToggleService;

    private final CafcassCaseDataHelper cafcassCaseDataHelper;

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
                String searchString = objectMapper.writeValueAsString(ccdQueryParam);
                log.info("Search string - {}", searchString);
                String userToken = systemUserService.getSysUserToken();
                final String s2sToken = authTokenGenerator.generate();
                log.info("Invoking search cases");
                SearchResult searchResult = cafcassCcdDataStoreService.searchCases(
                    userToken,
                    searchString,
                    s2sToken,
                    cafCassSearchCaseTypeId
                );
                if (searchResult != null) {
                    List<CafCassCaseDetail> cafCassCaseDetails = new ArrayList<>();
                    searchResult.getCases().forEach(caseData -> {
                        try {
                            CafCassCaseDetail cafCassCaseDetail = objectMapper.convertValue(caseData, CafCassCaseDetail.class);
                            cafCassCaseDetails.add(cafCassCaseDetail);
                        } catch (Exception e) {
                            log.error("Error while converting result case to Cafcass casedetails {} with exception", caseData.getId(), e);
                        }
                    });
                    cafCassResponse.setCases(cafCassCaseDetails);
                    cafCassResponse.setTotal(cafCassCaseDetails.size());
                }

                if (cafCassResponse.getCases() != null && !cafCassResponse.getCases().isEmpty()) {
                    log.info("CCD Search Result Size --> {} and Cafcass Response Size --> {}", searchResult.getTotal(),
                             cafCassResponse.getTotal());
                    cafCassFilter.filter(cafCassResponse);
                    log.info("After applying filter Result Size --> {}", cafCassResponse.getTotal());
                    CafCassResponse filteredCafcassData = getHearingDetailsForAllCases(authorisation, cafCassResponse);
                    updateHearingResponse(authorisation, s2sToken, filteredCafcassData);
                    addSpecificDocumentsFromCaseFileViewBasedOnCategories(filteredCafcassData);
                    filteredCafcassData = removeUnnecessaryFieldsFromResponse(filteredCafcassData);
                    removeRedactedDocumentsFromResponse(filteredCafcassData);
                    for (CafCassCaseDetail cafcassCase : filteredCafcassData.getCases()) {
                        log.info("Found case with id {} and courtName {} ",
                                 cafcassCase.getId(), cafcassCase.getCaseData().getCourtName()
                        );
                    }
                    return CafCassResponse.builder()
                        .cases(filteredCafcassData.getCases())
                        .total(filteredCafcassData.getCases().size())
                        .build();
                }
            }
        } catch (Exception e) {
            log.error("Error in search cases", e);
            throw e;
        }
        return cafCassResponse;
    }

    private CafCassResponse removeUnnecessaryFieldsFromResponse(CafCassResponse filteredCafcassData) {
        return cafcassCaseDataHelper.removeUnnecessaryFieldsFromResponse(filteredCafcassData);
    }

    private void removeRedactedDocumentsFromResponse(CafCassResponse filteredCafcassData) {
        cafcassCaseDataHelper.removeRedactedDocumentsFromResponse(filteredCafcassData);
    }

    private void addSpecificDocumentsFromCaseFileViewBasedOnCategories(CafCassResponse cafCassResponse) {
        cafcassCaseDataHelper.addSpecificDocumentsFromCaseFileViewBasedOnCategories(cafCassResponse);
    }

    public boolean checkIfDocumentsNeedToExclude(List<String> excludedDocumentList, String documentFilename) {
        return cafcassCaseDataHelper.checkIfDocumentsNeedToExclude(excludedDocumentList, documentFilename);
    }

    private QueryParam buildCcdQueryParam(String startDate, String endDate) {

        // set or condition for caseTypeofApplication (e.g. something like -
        // caseTypeofApplication = C100 or caseTypeofApplication - FL401
        List<Should> applicationTypes = populateCaseTypeOfApplicationForSearchQuery();

        List<Should> shoulds = populateStatesForQuery();

        LastModified lastModified = LastModified.builder().gte(startDate).lte(endDate).boost(ccdElasticSearchApiBoost)
            .build();

        Range range = Range.builder().lastModified(lastModified).build();
        if (featureToggleService.isCafcassDateTimeFeatureEnabled()) {
            range = Range.builder().cafcassDateTime(lastModified).build();
        }

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
        return cafcassCaseDataHelper.getHearingDetailsForAllCases(authorisation, cafCassResponse);
    }

    public void filterCancelledHearingsBeforeListing(List<Hearings> listOfHearingDetails) {
        cafcassCaseDataHelper.filterCancelledHearingsBeforeListing(listOfHearingDetails);
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
            "data.submitAndPayDownloadApplicationLink",
            "data.c8Document",
            "data.c1ADocument",
            "data.familymanCaseNumber",
            "data.dateSubmitted",
            "data.issueDate",
            "data.caseTypeOfApplication",
            "data.draftConsentOrderFile",
            "data.confidentialDetails",
            "data.isInterpreterNeeded",
            "data.interpreterNeeds",
            "data.childrenKnownToLocalAuthority",
            "data.otherDocuments",
            "data.finalDocument",
            "data.ordersApplyingFor",
            "data.children",
            "data.miamStatus",
            "data.miamExemptionsTable",
            "data.claimingExemptionMiam",
            "data.applicantAttendedMiam",
            "data.familyMediatorMiam",
            "data.otherProceedingsMiam",
            "data.applicantConsentMiam",
            "data.mediatorRegistrationNumber",
            "data.familyMediatorServiceName",
            "data.soleTraderName",
            "data.mpuChildInvolvedInMiam",
            "data.mpuApplicantAttendedMiam",
            "data.mpuClaimingExemptionMiam",
            "data.mpuExemptionReasons",
            "data.miamDomesticAbuseEvidences",
            "data.mpuDomesticAbuseEvidences",
            "data.mpuIsDomesticAbuseEvidenceProvided",
            "data.mpuDomesticAbuseEvidenceDocument",
            "data.mpuNoDomesticAbuseEvidenceReason",
            "data.mpuUrgencyReason",
            "data.miamUrgencyReason",
            "data.mpuPreviousMiamAttendanceReason",
            "data.miamPreviousAttendanceReason",
            "data.mpuDocFromDisputeResolutionProvider",
            "data.mpuTypeOfPreviousMiamAttendanceEvidence",
            "data.miamTypeOfPreviousAttendanceEvidence",
            "data.mpuCertificateByMediator",
            "data.mpuMediatorDetails",
            "data.mpuOtherExemptionReasons",
            "data.miamOtherExemptionReasons",
            "data.mpuApplicantUnableToAttendMiamReason1",
            "data.mpuApplicantUnableToAttendMiamReason2",
            "data.miamCertificationDocumentUpload",
            "data.mpuChildProtectionConcernReason",
            "data.miamChildProtectionConcernReason",
            "data.miamTable",
            "data.summaryTabForOrderAppliedFor",
            "data.partyIdAndPartyTypeMap",
            "data.applicants",
            "data.respondents",
            "data.applicantsConfidentialDetails",
            "data.applicantSolicitorEmailAddress",
            "data.solicitorName",
            "data.courtEpimsId",
            "data.courtTypeId",
            "data.courtName",
            "data.otherPeopleInTheCaseTable",
            "data.ordersNonMolestationDocument",
            "data.hearingOutComeDocument",
            "data.manageOrderCollection",
            "data.hearingData",
            "data.orderCollection",
            "data.caseManagementLocation",
            "data.otherPartyInTheCaseRevised",
            "data.newChildDetails",
            "data.childAndApplicantRelations",
            "data.childAndRespondentRelations",
            "data.childAndOtherPeopleRelations",
            "data.cafcassUploadedDocs",
            "data.courtStaffUploadDocListDocTab",
            "data.legalProfUploadDocListDocTab",
            "data.bulkScannedDocListDocTab",
            "data.cafcassUploadDocListDocTab",
            "data.citizenUploadedDocListDocTab",
            "data.localAuthorityUploadDocListDocTab",
            "data.restrictedDocuments",
            "data.confidentialDocuments",
            "data.respondentAc8Documents",
            "data.respondentBc8Documents",
            "data.respondentCc8Documents",
            "data.respondentDc8Documents",
            "data.respondentEc8Documents",
            "data.otherPartyC8Documents",
            "data.c8FormDocumentsUploaded",
            "data.bundleInformation",
            "data.otherDocumentsUploaded",
            "data.uploadOrderDoc",
            "data.specialArrangementsLetter",
            "data.additionalDocuments",
            "data.additionalDocumentsList",
            "data.stmtOfServiceAddRecipient",
            "data.stmtOfServiceForOrder",
            "data.stmtOfServiceForApplication",
            "data.finalServedApplicationDetailsList",
            "data.additionalOrderDocuments"
        );
    }
}
