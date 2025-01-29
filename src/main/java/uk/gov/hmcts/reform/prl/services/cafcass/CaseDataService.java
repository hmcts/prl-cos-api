package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.micrometer.common.util.StringUtils;
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
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResponseToAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.ApplicantDetails;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseData;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Document;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Element;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.manageorder.CaseOrder;
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
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CANCELLED;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseDataService {
    public static final String CONFIDENTIAL = "confidential";
    public static final String ANY_OTHER_DOC = "anyOtherDoc";
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
                String userToken = systemUserService.getSysUserToken();
                final String s2sToken = authTokenGenerator.generate();
                log.info("Invoking search cases");
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
                    addSpecificDocumentsFromCaseFileViewBasedOnCategories(filteredCafcassData);
                    filteredCafcassData = removeUnnecessaryFieldsFromResponse(filteredCafcassData);
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

    private CafCassResponse removeUnnecessaryFieldsFromResponse(CafCassResponse filteredCafcassData) {
        filteredCafcassData.getCases().forEach(cafCassCaseDetail -> {
            CafCassCaseData caseData = cafCassCaseDetail.getCaseData();
            caseData = caseData.toBuilder()
                .applicants(removeResponse(caseData.getApplicants()))
                .respondents(removeResponse(caseData.getRespondents()))
                .orderCollection(removeServeOrderDetails(caseData.getOrderCollection()))
                .build();

            cafCassCaseDetail.setCaseData(caseData);
        });

        return  filteredCafcassData;
    }

    private List<Element<CaseOrder>> removeServeOrderDetails(List<Element<CaseOrder>> orderCollection) {
        if (!CollectionUtils.isEmpty(orderCollection)) {
            orderCollection.forEach(order -> {
                if (null != order.getValue() && null != order.getValue().getServeOrderDetails()) {
                    order.getValue().setServeOrderDetails(null);
                }
            });
        }

        return orderCollection;
    }

    private List<Element<ApplicantDetails>> removeResponse(List<Element<ApplicantDetails>> partyDetails) {
        partyDetails.forEach(partyDetail -> {
            if (null != partyDetail.getValue() && null != partyDetail.getValue().getResponse()) {
                partyDetail.getValue().setResponse(null);
            }
        });

        return partyDetails;
    }

    private void addSpecificDocumentsFromCaseFileViewBasedOnCategories(CafCassResponse cafCassResponse) {
        cafCassResponse.getCases().parallelStream().forEach(cafCassCaseDetail -> {
            List<Element<uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments>> otherDocsList = new ArrayList<>();
            CafCassCaseData caseData = cafCassCaseDetail.getCaseData();
            populateReviewDocuments(otherDocsList, caseData);
            populateRespondentC1AResponseDoc(caseData.getRespondents(), otherDocsList);
            populateConfidentialDoc(caseData, otherDocsList);
            populateBundleDoc(caseData, otherDocsList);
            populateAnyOtherDoc(caseData, otherDocsList);

            List<Element<ApplicantDetails>> respondents = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(caseData.getRespondents())) {
                caseData.getRespondents().parallelStream().forEach(applicantDetailsElement -> {
                    ApplicantDetails applicantDetails = applicantDetailsElement.getValue().toBuilder().response(null).build();
                    respondents.add(Element.<ApplicantDetails>builder().id(applicantDetailsElement.getId()).value(
                        applicantDetails).build());
                });
            }

            //Setting the new fields value to null once documents are processed
            final CafCassCaseData cafCassCaseData = caseData.toBuilder()
                .otherDocuments(otherDocsList)
                .legalProfUploadDocListDocTab(null)
                .bulkScannedDocListDocTab(null)
                .cafcassUploadDocListDocTab(null)
                .courtStaffUploadDocListDocTab(null)
                .citizenUploadedDocListDocTab(null)
                .restrictedDocuments(null)
                .confidentialDocuments(null)
                .respondentAc8Documents(null)
                .respondentBc8Documents(null)
                .respondentCc8Documents(null)
                .respondentDc8Documents(null)
                .respondentEc8Documents(null)
                .c8FormDocumentsUploaded(null)
                .bundleInformation(null)
                .otherDocumentsUploaded(null)
                .uploadOrderDoc(null)
                .specialArrangementsLetter(null)
                .additionalDocuments(null)
                .additionalDocumentsList(null)
                .stmtOfServiceAddRecipient(null)
                .stmtOfServiceForOrder(null)
                .stmtOfServiceForApplication(null)
                .respondents(respondents)
                .build();
            cafCassCaseDetail.setCaseData(cafCassCaseData);
        });


    }

    private void populateAnyOtherDoc(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        if (CollectionUtils.isNotEmpty(caseData.getOtherDocumentsUploaded())) {
            caseData.getOtherDocumentsUploaded().parallelStream().forEach(document -> addInOtherDocuments(
                ANY_OTHER_DOC,
                document,
                otherDocsList
            ));
        }
        if (null != caseData.getUploadOrderDoc()) {
            addInOtherDocuments(ANY_OTHER_DOC, caseData.getUploadOrderDoc(), otherDocsList);
        }
        populateServiceOfApplicationUploadDocs(caseData, otherDocsList);
        populateStatementOfServiceDocs(caseData, otherDocsList);


    }

    private void populateStatementOfServiceDocs(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        if (CollectionUtils.isNotEmpty(caseData.getStmtOfServiceForOrder())) {
            caseData.getStmtOfServiceForOrder().parallelStream().forEach(
                stmtOfServiceAddRecipientElement -> addInOtherDocuments(
                    ANY_OTHER_DOC,
                    stmtOfServiceAddRecipientElement.getValue().getStmtOfServiceDocument(),
                    otherDocsList
                ));
        }
        if (CollectionUtils.isNotEmpty(caseData.getStmtOfServiceForApplication())) {
            caseData.getStmtOfServiceForApplication().parallelStream().forEach(
                stmtOfServiceAddRecipientElement -> addInOtherDocuments(
                    ANY_OTHER_DOC,
                    stmtOfServiceAddRecipientElement.getValue().getStmtOfServiceDocument(),
                    otherDocsList
                ));
        }
        if (CollectionUtils.isNotEmpty(caseData.getStmtOfServiceAddRecipient())) {
            caseData.getStmtOfServiceAddRecipient().parallelStream().forEach(
                stmtOfServiceAddRecipientElement -> addInOtherDocuments(
                    ANY_OTHER_DOC,
                    stmtOfServiceAddRecipientElement.getValue().getStmtOfServiceDocument(),
                    otherDocsList
                ));
        }
    }

    private void populateServiceOfApplicationUploadDocs(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        if (null != caseData.getSpecialArrangementsLetter()) {
            addInOtherDocuments(
                ANY_OTHER_DOC,
                caseData.getSpecialArrangementsLetter(),
                otherDocsList
            );
        }
        if (null != caseData.getAdditionalDocuments()) {
            addInOtherDocuments(ANY_OTHER_DOC, caseData.getAdditionalDocuments(),
                otherDocsList
            );
        }
        if (CollectionUtils.isNotEmpty(caseData.getAdditionalDocumentsList())) {
            caseData.getAdditionalDocumentsList().parallelStream().forEach(
                documentElement -> addInOtherDocuments(
                    ANY_OTHER_DOC,
                    documentElement.getValue(),
                    otherDocsList
                ));
        }
    }

    private void populateBundleDoc(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        if (null != caseData.getBundleInformation()
            && null != caseData.getBundleInformation().getCaseBundles()
            && CollectionUtils.isNotEmpty(caseData.getBundleInformation().getCaseBundles())) {
            caseData.getBundleInformation().getCaseBundles().parallelStream().forEach(bundle -> {
                uk.gov.hmcts.reform.prl.models.documents.Document document = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                    .documentFileName(bundle.getValue().getStitchedDocument().documentFilename)
                    .documentUrl(bundle.getValue().getStitchedDocument().getDocumentUrl())
                    .build();
                addInOtherDocuments("courtBundle", document, otherDocsList);
            });
        }
    }

    private void populateReviewDocuments(List<Element<OtherDocuments>> otherDocsList, CafCassCaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getCourtStaffUploadDocListDocTab())) {
            parseQuarantineLegalDocs(
                otherDocsList,
                caseData.getCourtStaffUploadDocListDocTab()
            );
        }
        if (CollectionUtils.isNotEmpty(caseData.getLegalProfUploadDocListDocTab())) {
            parseQuarantineLegalDocs(
                otherDocsList,
                caseData.getLegalProfUploadDocListDocTab()
            );
        }
        if (CollectionUtils.isNotEmpty(caseData.getCafcassUploadDocListDocTab())) {
            parseQuarantineLegalDocs(
                otherDocsList,
                caseData.getCafcassUploadDocListDocTab()
            );
        }
        if (CollectionUtils.isNotEmpty(caseData.getCitizenUploadedDocListDocTab())) {
            parseQuarantineLegalDocs(
                otherDocsList,
                caseData.getCitizenUploadedDocListDocTab()
            );
        }
        if (CollectionUtils.isNotEmpty(caseData.getConfidentialDocuments())) {
            parseQuarantineLegalDocs(otherDocsList, caseData.getConfidentialDocuments());
        }
        if (CollectionUtils.isNotEmpty(caseData.getBulkScannedDocListDocTab())) {
            parseQuarantineLegalDocs(otherDocsList, caseData.getBulkScannedDocListDocTab());
        }
        if (CollectionUtils.isNotEmpty(caseData.getRestrictedDocuments())) {
            parseQuarantineLegalDocs(otherDocsList, caseData.getRestrictedDocuments());
        }
    }

    private void populateConfidentialDoc(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        if (CollectionUtils.isNotEmpty(caseData.getRespondentAc8Documents())) {
            caseData.getRespondentAc8Documents().parallelStream().forEach(
                responseDocumentsElement ->
                    populateRespondentDocument(
                        responseDocumentsElement.getValue().getRespondentC8Document(),
                        responseDocumentsElement.getValue().getRespondentC8DocumentWelsh(),
                        CONFIDENTIAL,
                        otherDocsList
                    ));
        }
        if (CollectionUtils.isNotEmpty(caseData.getRespondentBc8Documents())) {
            caseData.getRespondentBc8Documents().parallelStream().forEach(
                responseDocumentsElement ->
                    populateRespondentDocument(
                        responseDocumentsElement.getValue().getRespondentC8Document(),
                        responseDocumentsElement.getValue().getRespondentC8DocumentWelsh(),
                        CONFIDENTIAL,
                        otherDocsList
                    ));
        }
        if (CollectionUtils.isNotEmpty(caseData.getRespondentCc8Documents())) {
            caseData.getRespondentCc8Documents().parallelStream().forEach(
                responseDocumentsElement ->
                    populateRespondentDocument(
                        responseDocumentsElement.getValue().getRespondentC8Document(),
                        responseDocumentsElement.getValue().getRespondentC8DocumentWelsh(),
                        CONFIDENTIAL,
                        otherDocsList
                    ));
        }
        if (CollectionUtils.isNotEmpty(caseData.getRespondentDc8Documents())) {
            caseData.getRespondentDc8Documents().parallelStream().forEach(
                responseDocumentsElement ->
                    populateRespondentDocument(
                        responseDocumentsElement.getValue().getRespondentC8Document(),
                        responseDocumentsElement.getValue().getRespondentC8DocumentWelsh(),
                        CONFIDENTIAL,
                        otherDocsList
                    ));
        }
        if (CollectionUtils.isNotEmpty(caseData.getRespondentEc8Documents())) {
            caseData.getRespondentEc8Documents().parallelStream().forEach(
                responseDocumentsElement ->
                    populateRespondentDocument(
                        responseDocumentsElement.getValue().getRespondentC8Document(),
                        responseDocumentsElement.getValue().getRespondentC8DocumentWelsh(),
                        CONFIDENTIAL,
                        otherDocsList
                    ));
        }
        if (CollectionUtils.isNotEmpty(caseData.getC8FormDocumentsUploaded())) {
            caseData.getC8FormDocumentsUploaded().parallelStream().forEach(c8FormDocumentsUploaded ->
                populateRespondentDocument(
                    c8FormDocumentsUploaded,
                    null,
                    CONFIDENTIAL,
                    otherDocsList
                ));
        }
    }

    private void populateRespondentDocument(uk.gov.hmcts.reform.prl.models.documents.Document responseDocumentEng,
                                            uk.gov.hmcts.reform.prl.models.documents.Document responseDocumentWelsh,
                                            String category,
                                            List<Element<OtherDocuments>> otherDocsList) {
        if (null != responseDocumentEng) {
            addInOtherDocuments(category, responseDocumentEng, otherDocsList);
        }
        if (null != responseDocumentWelsh) {
            addInOtherDocuments(category, responseDocumentWelsh, otherDocsList);
        }
    }

    private void populateRespondentC1AResponseDoc(List<Element<ApplicantDetails>> respondents, List<Element<OtherDocuments>> otherDocsList) {
        if (CollectionUtils.isNotEmpty(respondents)) {
            respondents.stream().forEach(respondent -> {
                if (null != respondent.getValue().getResponse() && null != respondent.getValue().getResponse().getResponseToAllegationsOfHarm()) {
                    ResponseToAllegationsOfHarm responseToAllegationsOfHarm = respondent.getValue().getResponse().getResponseToAllegationsOfHarm();
                    populateRespondentDocument(
                        responseToAllegationsOfHarm.getResponseToAllegationsOfHarmDocument(),
                        responseToAllegationsOfHarm.getResponseToAllegationsOfHarmWelshDocument(),
                        "respondentC1AResponse",
                        otherDocsList
                    );
                }
            });
        }
    }

    private void parseQuarantineLegalDocs(List<Element<OtherDocuments>> otherDocsList,
                                          List<uk.gov.hmcts.reform.prl.models.Element<QuarantineLegalDoc>> quarantineLegalDocs) {
        quarantineLegalDocs.parallelStream().forEach(quarantineLegalDocElement -> {
            uk.gov.hmcts.reform.prl.models.documents.Document document = null;
            if (!StringUtils.isEmpty(quarantineLegalDocElement.getValue().getCategoryId())) {
                String attributeName = DocumentUtils.populateAttributeNameFromCategoryId(
                    quarantineLegalDocElement.getValue().getCategoryId(),
                    null
                );
                document = objMapper.convertValue(
                    objMapper.convertValue(quarantineLegalDocElement.getValue(), Map.class).get(attributeName),
                    uk.gov.hmcts.reform.prl.models.documents.Document.class
                );
            }
            if (null != document) {
                log.info("Found document for category {}", quarantineLegalDocElement.getValue().getCategoryId());
                parseCategoryAndCreateList(
                    quarantineLegalDocElement.getValue().getCategoryId(),
                    document,
                    otherDocsList
                );
            }
        });
    }

    private void parseCategoryAndCreateList(String category,
                                            uk.gov.hmcts.reform.prl.models.documents.Document caseDocument,
                                            List<Element<uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments>> otherDocsList) {
        if ((CollectionUtils.isEmpty(excludedDocumentCategoryList) || !excludedDocumentCategoryList.contains(category))
            && (CollectionUtils.isEmpty(excludedDocumentList) || !checkIfDocumentsNeedToExclude(
            excludedDocumentList,
            caseDocument.getDocumentFileName()
        ))) {
            addInOtherDocuments(category, caseDocument, otherDocsList);

        }

    }

    private void addInOtherDocuments(String category,
                                     uk.gov.hmcts.reform.prl.models.documents.Document caseDocument,
                                     List<Element<OtherDocuments>> otherDocsList) {
        try {
            if (null != caseDocument) {
                otherDocsList.add(Element.<OtherDocuments>builder().id(
                    UUID.randomUUID()).value(OtherDocuments.builder().documentOther(
                    buildFromCaseDocument(caseDocument)).documentName(caseDocument.getDocumentFileName()).documentTypeOther(
                    DocTypeOtherDocumentsEnum.getValue(category)).build()).build());
            }
        } catch (MalformedURLException e) {
            log.error("Error in populating otherDocsList for CAFCASS {}", e.getMessage());
        }
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

    public Document buildFromCaseDocument(uk.gov.hmcts.reform.prl.models.documents.Document caseDocument) throws MalformedURLException {
        URL url = new URL(caseDocument.getDocumentUrl());
        return Document.builder()
            .documentId(CafCassCaseData.getDocumentId(url))
            .documentFileName(caseDocument.getDocumentFileName())
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
            "data.restrictedDocuments",
            "data.confidentialDocuments",
            "data.respondentAc8Documents",
            "data.respondentBc8Documents",
            "data.respondentCc8Documents",
            "data.respondentDc8Documents",
            "data.respondentEc8Documents",
            "data.c8FormDocumentsUploaded",
            "data.bundleInformation",
            "data.otherDocumentsUploaded",
            "data.uploadOrderDoc",
            "data.specialArrangementsLetter",
            "data.additionalDocuments",
            "data.additionalDocumentsList",
            "data.stmtOfServiceAddRecipient",
            "data.stmtOfServiceForOrder",
            "data.stmtOfServiceForApplication"
        );
    }
}
