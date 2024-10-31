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
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.Address;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private final OrganisationService organisationService;


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
                SearchResult searchResult = cafcassCcdDataStoreService.searchCases(
                    userToken,
                    searchString,
                    s2sToken,
                    cafCassSearchCaseTypeId
                );
                log.info("searchResult --> {}", searchResult);
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
                    addSpecificDocumentsFromCaseFileViewBasedOnCategories(filteredCafcassData);
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
        cafCassResponse.getCases().parallelStream().forEach(cafCassCaseDetail -> {
            log.info("processing cafCassResponse " + cafCassResponse);
            List<Element<uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments>> otherDocsList = new ArrayList<>();
            CafCassCaseData caseData = cafCassCaseDetail.getCaseData();
            populateReviewDocuments(otherDocsList, caseData);
            populateRespondentC1AResponseDoc(caseData.getRespondents(), otherDocsList);
            populateConfidentialDoc(caseData, otherDocsList);
            populateBundleDoc(caseData, otherDocsList);
            populateAnyOtherDoc(caseData, otherDocsList);

            List<Element<ApplicantDetails>> respondents = new ArrayList<>();
            caseData.getRespondents().parallelStream().forEach(applicantDetailsElement ->  {
                ApplicantDetails applicantDetails = applicantDetailsElement.getValue().toBuilder().response(null).build();
                respondents.add(Element.<ApplicantDetails>builder().id(applicantDetailsElement.getId()).value(applicantDetails).build());
            });

            final CafCassCaseData cafCassCaseData = caseData.toBuilder()
                .otherDocuments(otherDocsList)
                .reviewDocuments(null)
                .respondentC8Document(null)
                .c8FormDocumentsUploaded(null)
                .bundleInformation(null)
                .otherDocumentsUploaded(null)
                .uploadOrderDoc(null)
                .serviceOfApplicationUploadDocs(null)
                .statementOfService(null)
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
        if (null != caseData.getServiceOfApplicationUploadDocs()) {
            populateServiceOfApplicationUploadDocs(caseData, otherDocsList);
        }
        if (null != caseData.getStatementOfService()) {
            populateStatementOfServiceDocs(caseData, otherDocsList);
        }

    }

    private void populateStatementOfServiceDocs(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForOrder())) {
            caseData.getStatementOfService().getStmtOfServiceForOrder().parallelStream().forEach(
                stmtOfServiceAddRecipientElement -> addInOtherDocuments(
                    ANY_OTHER_DOC,
                    stmtOfServiceAddRecipientElement.getValue().getStmtOfServiceDocument(),
                    otherDocsList
                ));
        }
        if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForApplication())) {
            caseData.getStatementOfService().getStmtOfServiceForApplication().parallelStream().forEach(
                stmtOfServiceAddRecipientElement -> addInOtherDocuments(
                    ANY_OTHER_DOC,
                    stmtOfServiceAddRecipientElement.getValue().getStmtOfServiceDocument(),
                    otherDocsList
                ));
        }
        if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceAddRecipient())) {
            caseData.getStatementOfService().getStmtOfServiceAddRecipient().parallelStream().forEach(
                stmtOfServiceAddRecipientElement -> addInOtherDocuments(
                    ANY_OTHER_DOC,
                    stmtOfServiceAddRecipientElement.getValue().getStmtOfServiceDocument(),
                    otherDocsList
                ));
        }
    }

    private void populateServiceOfApplicationUploadDocs(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        if (null != caseData.getServiceOfApplicationUploadDocs().getSpecialArrangementsLetter()) {
            addInOtherDocuments(ANY_OTHER_DOC,
                                caseData.getServiceOfApplicationUploadDocs().getSpecialArrangementsLetter(),
                                otherDocsList
            );
        }
        if (null != caseData.getServiceOfApplicationUploadDocs().getAdditionalDocuments()) {
            addInOtherDocuments(ANY_OTHER_DOC, caseData.getServiceOfApplicationUploadDocs().getAdditionalDocuments(),
                                otherDocsList
            );
        }
        if (CollectionUtils.isNotEmpty(caseData.getServiceOfApplicationUploadDocs().getAdditionalDocumentsList())) {
            caseData.getServiceOfApplicationUploadDocs().getAdditionalDocumentsList().parallelStream().forEach(
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
        log.info("inside populateReviewDocuments");
        if (ObjectUtils.isNotEmpty(caseData.getReviewDocuments())) {
            if (CollectionUtils.isNotEmpty(caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab())) {
                parseQuarantineLegalDocs(
                    otherDocsList,
                    caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab()
                );
            }
            if (CollectionUtils.isNotEmpty(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab())) {
                parseQuarantineLegalDocs(
                    otherDocsList,
                    caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab()
                );
            }
            if (CollectionUtils.isNotEmpty(caseData.getReviewDocuments().getCafcassUploadDocListDocTab())) {
                parseQuarantineLegalDocs(
                    otherDocsList,
                    caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab()
                );
            }
            if (CollectionUtils.isNotEmpty(caseData.getReviewDocuments().getCitizenUploadedDocListDocTab())) {
                parseQuarantineLegalDocs(
                    otherDocsList,
                    caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab()
                );
            }
            if (CollectionUtils.isNotEmpty(caseData.getReviewDocuments().getConfidentialDocuments())) {
                parseQuarantineLegalDocs(otherDocsList, caseData.getReviewDocuments().getConfidentialDocuments());
            }
            if (CollectionUtils.isNotEmpty(caseData.getReviewDocuments().getBulkScannedDocListDocTab())) {
                parseQuarantineLegalDocs(otherDocsList, caseData.getReviewDocuments().getConfidentialDocuments());
            }
            if (CollectionUtils.isNotEmpty(caseData.getReviewDocuments().getRestrictedDocuments())) {
                parseQuarantineLegalDocs(otherDocsList, caseData.getReviewDocuments().getConfidentialDocuments());
            }
        }
    }

    private void populateConfidentialDoc(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        if (null != caseData.getRespondentC8Document()) {
            if (CollectionUtils.isNotEmpty(caseData.getRespondentC8Document().getRespondentAc8Documents())) {
                caseData.getRespondentC8Document().getRespondentAc8Documents().parallelStream().forEach(
                    responseDocumentsElement ->
                        populateRespondentDocument(
                            responseDocumentsElement.getValue().getRespondentC8Document(),
                            responseDocumentsElement.getValue().getRespondentC8DocumentWelsh(),
                            CONFIDENTIAL,
                            otherDocsList
                        ));
            }
            if (CollectionUtils.isNotEmpty(caseData.getRespondentC8Document().getRespondentBc8Documents())) {
                caseData.getRespondentC8Document().getRespondentBc8Documents().parallelStream().forEach(
                    responseDocumentsElement ->
                        populateRespondentDocument(
                            responseDocumentsElement.getValue().getRespondentC8Document(),
                            responseDocumentsElement.getValue().getRespondentC8DocumentWelsh(),
                            CONFIDENTIAL,
                            otherDocsList
                        ));
            }
            if (CollectionUtils.isNotEmpty(caseData.getRespondentC8Document().getRespondentCc8Documents())) {
                caseData.getRespondentC8Document().getRespondentCc8Documents().parallelStream().forEach(
                    responseDocumentsElement ->
                        populateRespondentDocument(
                            responseDocumentsElement.getValue().getRespondentC8Document(),
                            responseDocumentsElement.getValue().getRespondentC8DocumentWelsh(),
                            CONFIDENTIAL,
                            otherDocsList
                        ));
            }
            if (CollectionUtils.isNotEmpty(caseData.getRespondentC8Document().getRespondentDc8Documents())) {
                caseData.getRespondentC8Document().getRespondentDc8Documents().parallelStream().forEach(
                    responseDocumentsElement ->
                        populateRespondentDocument(
                            responseDocumentsElement.getValue().getRespondentC8Document(),
                            responseDocumentsElement.getValue().getRespondentC8DocumentWelsh(),
                            CONFIDENTIAL,
                            otherDocsList
                        ));
            }
            if (CollectionUtils.isNotEmpty(caseData.getRespondentC8Document().getRespondentEc8Documents())) {
                caseData.getRespondentC8Document().getRespondentEc8Documents().parallelStream().forEach(
                    responseDocumentsElement ->
                        populateRespondentDocument(
                            responseDocumentsElement.getValue().getRespondentC8Document(),
                            responseDocumentsElement.getValue().getRespondentC8DocumentWelsh(),
                            CONFIDENTIAL,
                            otherDocsList
                        ));
            }
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

    private void parseQuarantineLegalDocs(List<Element<OtherDocuments>> otherDocsList,
                                          List<uk.gov.hmcts.reform.prl.models.Element<QuarantineLegalDoc>> quarantineLegalDocs) {
        log.info("inside parseQuarantineLegalDocs");
        quarantineLegalDocs.parallelStream().forEach(quarantineLegalDocElement -> {
            if (!StringUtils.isEmpty(quarantineLegalDocElement.getValue().getCategoryId())) {
                uk.gov.hmcts.reform.prl.models.documents.Document document = getDocumentBasedOnCategories(
                    quarantineLegalDocElement.getValue());
                if (null != document) {
                    log.info("found document for category {}", quarantineLegalDocElement.getValue().getCategoryId());
                    parseCategoryAndCreateList(
                        quarantineLegalDocElement.getValue().getCategoryId(),
                        document,
                        otherDocsList
                    );
                }
            }
        });
    }

    private uk.gov.hmcts.reform.prl.models.documents.Document getDocumentBasedOnCategories(QuarantineLegalDoc quarantineLegalDoc) {
        switch (quarantineLegalDoc.getCategoryId()) {
            case "transcriptsOfJudgements":
                return quarantineLegalDoc.getTranscriptsOfJudgementsDocument();
            case "magistratesFactsAndReasons":
                return quarantineLegalDoc.getMagistratesFactsAndReasonsDocument();
            case "judgeNotesFromHearing":
                return quarantineLegalDoc.getJudgeNotesFromHearingDocument();
            case "positionStatements":
                return quarantineLegalDoc.getPositionStatementsDocument();
            case "fm5Statements":
                return quarantineLegalDoc.getFm5StatementsDocument();
            case "applicantApplication":
                return quarantineLegalDoc.getApplicantApplicationDocument();
            case "applicantC1AApplication":
                return quarantineLegalDoc.getApplicantC1AApplicationDocument();
            case "applicantC1AResponse":
                return quarantineLegalDoc.getApplicantC1AResponseDocument();
            case "applicationsWithinProceedings":
                return quarantineLegalDoc.getApplicationsWithinProceedingsDocument();
            case "MIAMCertificate":
                return quarantineLegalDoc.getMiamCertificateDocument();
            case "previousOrdersSubmittedWithApplication":
                return quarantineLegalDoc.getPreviousOrdersSubmittedWithApplicationDocument();
            case "respondentApplication":
                return quarantineLegalDoc.getRespondentApplicationDocument();
            case "respondentC1AApplication":
                return quarantineLegalDoc.getRespondentC1AApplicationDocument();
            case "respondentC1AResponse":
                return quarantineLegalDoc.getRespondentC1AResponseDocument();
            case "applicationsFromOtherProceedings":
                return quarantineLegalDoc.getApplicationsFromOtherProceedingsDocument();
            case "ordersFromOtherProceedings":
                return quarantineLegalDoc.getOrdersFromOtherProceedingsDocument();
            case "applicantStatements":
                return quarantineLegalDoc.getApplicantStatementsDocument();
            case "respondentStatements":
                return quarantineLegalDoc.getRespondentStatementsDocument();
            case "otherWitnessStatements":
                return quarantineLegalDoc.getOtherWitnessStatementsDocument();
            case "pathfinder":
                return quarantineLegalDoc.getPathfinderDocument();
            case "safeguardingLetter":
                return quarantineLegalDoc.getSafeguardingLetterDocument();
            case "section7Report":
                return quarantineLegalDoc.getSection7ReportDocument();
            case "section37Report":
                return quarantineLegalDoc.getSection37ReportDocument();
            case "16aRiskAssessment":
                return quarantineLegalDoc.getSixteenARiskAssessmentDocument();
            case "guardianReport":
                return quarantineLegalDoc.getGuardianReportDocument();
            case "specialGuardianshipReport":
                return quarantineLegalDoc.getSpecialGuardianshipReportDocument();
            case "otherDocs":
                return quarantineLegalDoc.getOtherDocsDocument();
            case "sec37Report":
                return quarantineLegalDoc.getSection37ReportDocument();
            case "localAuthorityOtherDoc":
                return quarantineLegalDoc.getLocalAuthorityOtherDocDocument();
            case "medicalReports":
                return quarantineLegalDoc.getMedicalRecordsDocument();
            case "DNAReports_expertReport":
                return quarantineLegalDoc.getDnaReportsExpertReportDocument();
            case "resultsOfHairStrandBloodTests":
                return quarantineLegalDoc.getResultsOfHairStrandBloodTestsDocument();
            case "policeDisclosures":
                return quarantineLegalDoc.getPoliceDisclosuresDocument();
            case "medicalRecords":
                return quarantineLegalDoc.getMedicalRecordsDocument();
            case "drugAndAlcoholTest(toxicology)":
                return quarantineLegalDoc.getDrugAndAlcoholTestDocument();
            case "policeReport":
                return quarantineLegalDoc.getPoliceReportDocument();
            case "emailsToCourtToRequestHearingsAdjourned":
                return quarantineLegalDoc.getEmailsToCourtToRequestHearingsAdjournedDocument();
            case "publicFundingCertificates":
                return quarantineLegalDoc.getPublicFundingCertificatesDocument();
            case "noticesOfActingDischarge":
                return quarantineLegalDoc.getNoticeOfHearingDocument();
            case "requestForFASFormsToBeChanged":
                return quarantineLegalDoc.getRequestForFasFormsToBeChangedDocument();
            case "witnessAvailability":
                return quarantineLegalDoc.getWitnessAvailabilityDocument();
            case "lettersOfComplaint":
                return quarantineLegalDoc.getLettersOfComplaintDocument();
            case "SPIPReferralRequests":
                return quarantineLegalDoc.getSpipReferralRequestsDocument();
            case "homeOfficeDWPResponses":
                return quarantineLegalDoc.getHomeOfficeDwpResponsesDocument();
            case "internalCorrespondence":
                return quarantineLegalDoc.getInternalCorrespondenceDocument();
            case "importantInfoAboutAddressAndContact":
                return quarantineLegalDoc.getImportantInfoAboutAddressAndContactDocument();
            case "privacyNotice":
                return quarantineLegalDoc.getPrivacyNoticeDocument();
            case "specialMeasures":
                return quarantineLegalDoc.getSpecialMeasuresDocument();
            case ANY_OTHER_DOC:
                return quarantineLegalDoc.getAnyOtherDocDocument();
            case "noticeOfHearing":
                return quarantineLegalDoc.getNoticeOfHearingDocument();
            case "courtBundle":
                return quarantineLegalDoc.getCourtBundleDocument();
            case "caseSummary":
                return quarantineLegalDoc.getCaseSummaryDocument();
            default:
                log.info("Unable to fetch document for category {}" + quarantineLegalDoc.getCategoryId());
                return null;
        }
    }

    private void parseCategoryAndCreateList(String category,
                                            uk.gov.hmcts.reform.prl.models.documents.Document caseDocument,
                                            List<Element<uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments>> otherDocsList) {
        log.info("parseCategoryAndCreateList {}", category);
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
                log.info("caseDocument is not null for category {}", category);
                otherDocsList.add(Element.<OtherDocuments>builder().id(
                    UUID.randomUUID()).value(OtherDocuments.builder().documentOther(
                    buildFromCaseDocument(caseDocument)).documentName(caseDocument.getDocumentFileName()).documentTypeOther(
                    DocTypeOtherDocumentsEnum.getValue(category)).build()).build());
            } else {
                log.info("caseDocument is null for category {}", category);
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
            .documentUrl(CafCassCaseData.getDocumentId(url))
            .documentFileName(caseDocument.getDocumentFileName())
            .build();
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
        Bool bool = Bool.builder().filter(filter).should(applicationTypes).minimumShouldMatch(2).must(must).build();
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
                hearings.getCaseHearings().stream().forEach(caseHearing -> {
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
                    filteredHearing.getCaseHearings().stream().forEach(
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
