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
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.ApplicantDetails;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseData;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Document;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Element;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.manageorder.CaseOrder;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.manageorder.OrderDocument;
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
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CANCELLED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REDACTED_DOCUMENT_UUID;
import static uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum.applicantApplication;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeList;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassCaseDataService {
    public static final String CONFIDENTIAL = "confidential";
    public static final String ANY_OTHER_DOC = "anyOtherDoc";
    public static final String NOTICE_OF_HEARING = "noticeOfHearing";
    private static final Set<String> NOTICE_OF_HEARING_ORDER_TYPES = Set.of(
        "noticeOfHearing",
        "noticeOfHearingParties"
    );

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
        filteredCafcassData.getCases().forEach(cafCassCaseDetail -> {
            CafCassCaseData caseData = cafCassCaseDetail.getCaseData();
            if (caseData.getOrderCollection() != null) {
                caseData.getOrderCollection().forEach(order -> {
                    CaseOrder value = order.getValue();
                    if (value != null) {
                        log.info(
                            "Case {} has hearingId={} on orderTypeId={}",
                            cafCassCaseDetail.getId(),
                            value.getHearingId(),
                            value.getOrderType()
                        );
                    }
                });
            }

            caseData = caseData.toBuilder()
                .applicants(removeResponse(caseData.getApplicants()))
                .respondents(removeResponse(caseData.getRespondents()))
                .orderCollection(removeServeOrderDetails(caseData.getOrderCollection()))
                .build();
            cafCassCaseDetail.setCaseData(caseData);
        });
        return filteredCafcassData;
    }

    private void removeRedactedDocumentsFromResponse(CafCassResponse filteredCafcassData) {
        filteredCafcassData.getCases().forEach(cafCassCaseDetail -> {
            CafCassCaseData caseData = cafCassCaseDetail.getCaseData();
            if (caseData != null && CollectionUtils.isNotEmpty(caseData.getOtherDocuments())) {
                caseData.setOtherDocuments(caseData.getOtherDocuments().stream()
                                           .filter(element -> isNotRedactedDocument(element, this::getOtherDocumentId))
                                           .toList());
            }
            if (caseData != null && CollectionUtils.isNotEmpty(caseData.getOrderCollection())) {
                cafCassCaseDetail.setCaseData(caseData.toBuilder()
                                                  .orderCollection(caseData.getOrderCollection().stream()
                                                                       .filter(element -> isNotRedactedDocument(
                                                                           element,
                                                                           this::getOrderDocumentId
                                                                       ))
                                                                       .toList())
                                                  .build());
            }
        });
    }

    private <T> boolean isNotRedactedDocument(Element<T> documentElement, Function<T, String> documentIdResolver) {
        if (documentElement == null || documentElement.getValue() == null) {
            return true;
        }
        return !REDACTED_DOCUMENT_UUID.equals(documentIdResolver.apply(documentElement.getValue()));
    }

    private String getOtherDocumentId(OtherDocuments otherDocument) {
        return otherDocument.getDocumentOther() != null ? otherDocument.getDocumentOther().getDocumentId() : null;
    }

    private String getOrderDocumentId(CaseOrder order) {
        return order.getOrderDocument() != null ? order.getOrderDocument().getDocumentId() : null;
    }

    private List<Element<CaseOrder>> removeServeOrderDetails(List<Element<CaseOrder>> orderCollection) {
        if (!CollectionUtils.isEmpty(orderCollection)) {
            orderCollection.forEach(order -> {
                if (null != order.getValue()) {
                    order.getValue().setServeOrderDetails(null);
                }
            });
        }
        return orderCollection;
    }

    private List<Element<ApplicantDetails>> removeResponse(List<Element<ApplicantDetails>> partyDetails) {
        partyDetails.forEach(partyDetail -> {
            if (null != partyDetail.getValue()) {
                partyDetail.getValue().setResponse(null);
            }
        });
        return partyDetails;
    }

    private void addSpecificDocumentsFromCaseFileViewBasedOnCategories(CafCassResponse cafCassResponse) {
        cafCassResponse.getCases().parallelStream().forEach(cafCassCaseDetail -> {
            log.info("Adding documents for case ID {} ", cafCassCaseDetail.getId());
            List<Element<uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments>> otherDocsList = new ArrayList<>();
            CafCassCaseData caseData = cafCassCaseDetail.getCaseData();
            populateReviewDocuments(otherDocsList, caseData);
            populateRespondentC1AResponseDoc(caseData.getRespondents(), otherDocsList);
            populateConfidentialDoc(caseData, otherDocsList);
            populateBundleDoc(caseData, otherDocsList);
            populateAnyOtherDoc(caseData, otherDocsList);
            populateAdditionalOrderDocuments(caseData, otherDocsList);

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
                .localAuthorityUploadDocListDocTab(null)
                .restrictedDocuments(null)
                .confidentialDocuments(null)
                .respondentAc8Documents(null)
                .respondentBc8Documents(null)
                .respondentCc8Documents(null)
                .respondentDc8Documents(null)
                .respondentEc8Documents(null)
                .otherPartyC8Documents(null)
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
                .finalServedApplicationDetailsList(null)
                .additionalOrderDocuments(null)
                .respondents(respondents)
                .build();
            cafCassCaseDetail.setCaseData(cafCassCaseData);
        });


    }

    private void populateAnyOtherDoc(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        if (CollectionUtils.isNotEmpty(caseData.getOtherDocumentsUploaded())) {
            caseData.getOtherDocumentsUploaded().parallelStream().forEach(document -> {
                String category = isNoticeOfHearingOrder(caseData, document) ? NOTICE_OF_HEARING : ANY_OTHER_DOC;
                log.info("[DEBUG] otherDocumentsUploaded -> category={}, filename={}",
                         category, document.getDocumentFileName());
                addInOtherDocuments(category, document, otherDocsList);
            });
        }
        if (null != caseData.getUploadOrderDoc()) {
            String category = isNoticeOfHearingOrder(caseData, caseData.getUploadOrderDoc())
                ? NOTICE_OF_HEARING : ANY_OTHER_DOC;
            log.info("[DEBUG] uploadOrderDoc -> category={}, filename={}",
                     category, caseData.getUploadOrderDoc().getDocumentFileName());
            addInOtherDocuments(category, caseData.getUploadOrderDoc(), otherDocsList);
        }
        populateServiceOfApplicationUploadDocs(caseData, otherDocsList);
        populateStatementOfServiceDocs(caseData, otherDocsList);
    }

    private boolean isNoticeOfHearingOrder(CafCassCaseData caseData,
                                           uk.gov.hmcts.reform.prl.models.documents.Document uploadOrderDoc) {
        if (CollectionUtils.isEmpty(caseData.getOrderCollection()) || null == uploadOrderDoc) {
            log.info("[DEBUG] isNoticeOfHearingOrder: no orderCollection or null document, filename={}",
                     null != uploadOrderDoc ? uploadOrderDoc.getDocumentFileName() : "null");
            return false;
        }
        boolean matched = caseData.getOrderCollection().stream()
            .map(Element::getValue)
            .anyMatch(order ->
                          matchesDocumentId(order.getOrderDocument(), uploadOrderDoc)
                              && NOTICE_OF_HEARING_ORDER_TYPES.contains(order.getOrderType())
            );
        log.info("[DEBUG] isNoticeOfHearingOrder: filename={}, documentId={}, matched={}",
                 uploadOrderDoc.getDocumentFileName(), uploadOrderDoc.getDocumentId(), matched);
        return matched;
    }

    private boolean matchesDocumentId(OrderDocument orderDocument, uk.gov.hmcts.reform.prl.models.documents.Document uploadOrderDoc) {
        boolean matched = null != orderDocument
            && Objects.equals(orderDocument.getDocumentId(), uploadOrderDoc.getDocumentId());
        if (null != orderDocument) {
            log.info("[DEBUG] matchesDocumentId: orderDocId={}, uploadDocId={}, matched={}",
                     orderDocument.getDocumentId(), uploadOrderDoc.getDocumentId(), matched);
        }
        return matched;
    }

    private void populateStatementOfServiceDocs(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        if (CollectionUtils.isNotEmpty(caseData.getStmtOfServiceForOrder())) {
            caseData.getStmtOfServiceForOrder().parallelStream().forEach(
                stmtOfServiceAddRecipientElement -> {
                    uk.gov.hmcts.reform.prl.models.documents.Document doc =
                        stmtOfServiceAddRecipientElement.getValue().getStmtOfServiceDocument();
                    String category = isNoticeOfHearingOrder(caseData, doc) ? NOTICE_OF_HEARING : ANY_OTHER_DOC;
                    log.info("[DEBUG] stmtOfServiceForOrder -> category={}, filename={}",
                             category, null != doc ? doc.getDocumentFileName() : "null");
                    addInOtherDocuments(category, doc, otherDocsList);
                });
        }
        if (CollectionUtils.isNotEmpty(caseData.getStmtOfServiceForApplication())) {
            caseData.getStmtOfServiceForApplication().parallelStream().forEach(
                stmtOfServiceAddRecipientElement -> {
                    uk.gov.hmcts.reform.prl.models.documents.Document doc =
                        stmtOfServiceAddRecipientElement.getValue().getStmtOfServiceDocument();
                    String category = isNoticeOfHearingOrder(caseData, doc) ? NOTICE_OF_HEARING : ANY_OTHER_DOC;
                    log.info("[DEBUG] stmtOfServiceForApplication -> category={}, filename={}",
                             category, null != doc ? doc.getDocumentFileName() : "null");
                    addInOtherDocuments(category, doc, otherDocsList);
                });
        }
        if (CollectionUtils.isNotEmpty(caseData.getStmtOfServiceAddRecipient())) {
            caseData.getStmtOfServiceAddRecipient().parallelStream().forEach(
                stmtOfServiceAddRecipientElement -> {
                    uk.gov.hmcts.reform.prl.models.documents.Document doc =
                        stmtOfServiceAddRecipientElement.getValue().getStmtOfServiceDocument();
                    String category = isNoticeOfHearingOrder(caseData, doc) ? NOTICE_OF_HEARING : ANY_OTHER_DOC;
                    log.info("[DEBUG] stmtOfServiceAddRecipient -> category={}, filename={}",
                             category, null != doc ? doc.getDocumentFileName() : "null");
                    addInOtherDocuments(category, doc, otherDocsList);
                });
        }
    }

    private void populateAdditionalOrderDocuments(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        nullSafeList(caseData.getAdditionalOrderDocuments())
            .stream()
            .flatMap(el -> el.getValue().getAdditionalDocuments().stream())
            .forEach(doc -> {
                log.info("[DEBUG] populateAdditionalOrderDocuments -> category={}, filename={}",
                         applicantApplication.getId(), doc.getValue().getDocumentFileName());
                addInOtherDocuments(applicantApplication.getId(), doc.getValue(), otherDocsList);
            });
    }

    private void populateServiceOfApplicationUploadDocs(CafCassCaseData caseData,
                                                        List<Element<OtherDocuments>> otherDocsList) {
        if (null != caseData.getSpecialArrangementsLetter()) {
            String category = isNoticeOfHearingOrder(caseData, caseData.getSpecialArrangementsLetter())
                ? NOTICE_OF_HEARING : ANY_OTHER_DOC;
            log.info("[DEBUG] specialArrangementsLetter -> category={}, filename={}",
                     category, caseData.getSpecialArrangementsLetter().getDocumentFileName());
            addInOtherDocuments(category, caseData.getSpecialArrangementsLetter(), otherDocsList);
        }
        if (null != caseData.getAdditionalDocuments()) {
            String category = isNoticeOfHearingOrder(caseData, caseData.getAdditionalDocuments())
                ? NOTICE_OF_HEARING : ANY_OTHER_DOC;
            log.info("[DEBUG] additionalDocuments -> category={}, filename={}",
                     category, caseData.getAdditionalDocuments().getDocumentFileName());
            addInOtherDocuments(category, caseData.getAdditionalDocuments(), otherDocsList);
        }
        if (CollectionUtils.isNotEmpty(caseData.getAdditionalDocumentsList())) {
            caseData.getAdditionalDocumentsList().parallelStream().forEach(
                documentElement -> {
                    String category = isNoticeOfHearingOrder(caseData, documentElement.getValue())
                        ? NOTICE_OF_HEARING : ANY_OTHER_DOC;
                    log.info("[DEBUG] additionalDocumentsList -> category={}, filename={}",
                             category, documentElement.getValue().getDocumentFileName());
                    addInOtherDocuments(category, documentElement.getValue(), otherDocsList);
                });
        }

        if (ObjectUtils.isNotEmpty(caseData.getFinalServedApplicationDetailsList())) {
            caseData.getFinalServedApplicationDetailsList().forEach(
                servedApplicationDetails -> {
                    nullSafeList(servedApplicationDetails.getValue().getBulkPrintDetails()).forEach(
                        bulkPrintDetailsElement ->
                            processServiceOfApplicationBulkPrintDocs(bulkPrintDetailsElement.getValue(), otherDocsList, caseData)
                    );
                    nullSafeList(servedApplicationDetails.getValue().getEmailNotificationDetails())
                        .forEach(
                            emailNotificationDetailsElement ->
                                processServiceOfApplicationEmailedDocs(
                                    emailNotificationDetailsElement.getValue(), otherDocsList, caseData)
                        );
                }
            );
        }
    }

    private void processServiceOfApplicationBulkPrintDocs(BulkPrintDetails bulkPrintDetails,
                                                          List<Element<OtherDocuments>> otherDocsList, CafCassCaseData caseData) {
        bulkPrintDetails.getPrintDocs().forEach(
            docElement -> {
                if (!isDocumentPresent(docElement.getValue(), otherDocsList)) {
                    String category = isNoticeOfHearingOrder(caseData, docElement.getValue()) ? NOTICE_OF_HEARING : ANY_OTHER_DOC;
                    log.info("[DEBUG] processServiceOfApplicationBulkPrintDocs -> category={}, filename={}",
                             category, docElement.getValue().getDocumentFileName());
                    addInOtherDocuments(category, docElement.getValue(), otherDocsList);
                }
            }
        );
    }

    private void processServiceOfApplicationEmailedDocs(EmailNotificationDetails emailNotificationDetails,
                                                        List<Element<OtherDocuments>> otherDocsList, CafCassCaseData caseData) {
        nullSafeList(emailNotificationDetails.getDocs()).forEach(
            docElement -> {
                if (!isDocumentPresent(docElement.getValue(), otherDocsList)) {
                    String category = isNoticeOfHearingOrder(caseData, docElement.getValue()) ? NOTICE_OF_HEARING : ANY_OTHER_DOC;
                    log.info("[DEBUG] processServiceOfApplicationEmailedDocs -> category={}, filename={}",
                             category, docElement.getValue().getDocumentFileName());
                    addInOtherDocuments(category, docElement.getValue(), otherDocsList);
                }
            }
        );
    }

    private boolean isDocumentPresent(uk.gov.hmcts.reform.prl.models.documents.Document caseDocument,
                                   List<Element<OtherDocuments>> otherDocsList) {
        if (isNotEmpty(caseDocument)) {
            return otherDocsList.stream().anyMatch(el -> {
                try {
                    return el.getValue().getDocumentOther().equals(buildFromCaseDocument(caseDocument));
                } catch (MalformedURLException e) {
                    log.error("Error in populating otherDocsList for CAFCASS {}", e.getMessage());
                }
                return false;
            });
        }
        return false;
    }

    private void populateBundleDoc(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        if (null != caseData.getBundleInformation()
            && null != caseData.getBundleInformation().getCaseBundles()
            && CollectionUtils.isNotEmpty(caseData.getBundleInformation().getCaseBundles())) {
            caseData.getBundleInformation().getCaseBundles().parallelStream().forEach(bundle -> {
                if (isNotEmpty(bundle.getValue().getStitchedDocument())) {
                    uk.gov.hmcts.reform.prl.models.documents.Document document =
                        uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                            .documentFileName(bundle.getValue().getStitchedDocument().getDocumentFilename())
                            .documentUrl(bundle.getValue().getStitchedDocument().getDocumentUrl())
                            .build();
                    addInOtherDocuments("courtBundle", document, otherDocsList);
                }
            });
        }
    }

    private void populateReviewDocuments(List<Element<OtherDocuments>> otherDocsList, CafCassCaseData caseData) {
        System.out.println("[DEBUG] in populateReviewDocuments");
        if (CollectionUtils.isNotEmpty(caseData.getCourtStaffUploadDocListDocTab())) {
            System.out.println("court staff upload:");
            System.out.println(caseData.getCourtStaffUploadDocListDocTab());
            parseQuarantineLegalDocs(
                otherDocsList,
                caseData.getCourtStaffUploadDocListDocTab(), caseData
            );
        }
        if (CollectionUtils.isNotEmpty(caseData.getLegalProfUploadDocListDocTab())) {
            System.out.println("LegalProfUpload:");
            System.out.println(caseData.getLegalProfUploadDocListDocTab());
            parseQuarantineLegalDocs(
                otherDocsList,
                caseData.getLegalProfUploadDocListDocTab(), caseData
            );
        }
        if (CollectionUtils.isNotEmpty(caseData.getCafcassUploadDocListDocTab())) {
            System.out.println("CafcassUploadDoc:");
            System.out.println(caseData.getCafcassUploadDocListDocTab());
            parseQuarantineLegalDocs(
                otherDocsList,
                caseData.getCafcassUploadDocListDocTab(), caseData
            );
        }
        if (CollectionUtils.isNotEmpty(caseData.getLocalAuthorityUploadDocListDocTab())) {
            System.out.println("LocalAuthorityUpload:");
            System.out.println(caseData.getLocalAuthorityUploadDocListDocTab());
            parseQuarantineLegalDocs(
                otherDocsList,
                caseData.getLocalAuthorityUploadDocListDocTab(), caseData
            );
        }
        if (CollectionUtils.isNotEmpty(caseData.getCitizenUploadedDocListDocTab())) {
            System.out.println("CitizenUploaded:");
            System.out.println(caseData.getCitizenUploadedDocListDocTab());
            parseQuarantineLegalDocs(
                otherDocsList,
                caseData.getCitizenUploadedDocListDocTab(), caseData
            );
        }
        if (CollectionUtils.isNotEmpty(caseData.getConfidentialDocuments())) {
            System.out.println("Confidential:");
            System.out.println(caseData.getConfidentialDocuments());
            parseQuarantineLegalDocs(otherDocsList, caseData.getConfidentialDocuments(), caseData);
        }
        if (CollectionUtils.isNotEmpty(caseData.getBulkScannedDocListDocTab())) {
            System.out.println("BulkScanned:");
            System.out.println(caseData.getBulkScannedDocListDocTab());
            parseQuarantineLegalDocs(otherDocsList, caseData.getBulkScannedDocListDocTab(), caseData);
        }
        if (CollectionUtils.isNotEmpty(caseData.getRestrictedDocuments())) {
            System.out.println("Restricted:");
            System.out.println(caseData.getRestrictedDocuments());
            parseQuarantineLegalDocs(otherDocsList, caseData.getRestrictedDocuments(), caseData);
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
        if (CollectionUtils.isNotEmpty(caseData.getOtherPartyC8Documents())) {
            caseData.getOtherPartyC8Documents().parallelStream().forEach(
                responseDocumentsElement ->
                    populateRespondentDocument(
                        responseDocumentsElement.getValue().getRespondentC8Document(),
                        responseDocumentsElement.getValue().getRespondentC8DocumentWelsh(),
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
                                          List<uk.gov.hmcts.reform.prl.models.Element<QuarantineLegalDoc>> quarantineLegalDocs,
                                          CafCassCaseData caseData) {
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
            if (null != document && document.getDocumentUrl() != null && !document.getDocumentUrl().endsWith(REDACTED_DOCUMENT_UUID)) {
                log.info("Found document for category {}", quarantineLegalDocElement.getValue().getCategoryId());
                parseCategoryAndCreateList(
                    quarantineLegalDocElement.getValue().getCategoryId(),
                    document,
                    otherDocsList,
                    caseData
                );
            }
        });
    }

    private void parseCategoryAndCreateList(String category,
                                            uk.gov.hmcts.reform.prl.models.documents.Document caseDocument,
                                            List<Element<uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments>> otherDocsList,
                                            CafCassCaseData caseData) {
        if ((CollectionUtils.isEmpty(excludedDocumentCategoryList) || !excludedDocumentCategoryList.contains(category))
            && (CollectionUtils.isEmpty(excludedDocumentList) || !checkIfDocumentsNeedToExclude(
            excludedDocumentList,
            caseDocument.getDocumentFileName()
        ))) {
            String resolvedCategory = ANY_OTHER_DOC.equals(category) && isNoticeOfHearingOrder(caseData, caseDocument)
                ? NOTICE_OF_HEARING
                : category;
            addInOtherDocuments(resolvedCategory, caseDocument, otherDocsList);
        }
    }

    private void addInOtherDocuments(String category,
                                     uk.gov.hmcts.reform.prl.models.documents.Document caseDocument,
                                     List<Element<OtherDocuments>> otherDocsList) {
        try {
            if (null != caseDocument && caseDocument.getDocumentUrl() != null
                && !caseDocument.getDocumentUrl().endsWith(REDACTED_DOCUMENT_UUID)) {
                Document documentOther = buildFromCaseDocument(caseDocument);
                System.out.println("[DEBUG] inside addInOtherDocuments, adding: ");
                System.out.println(caseDocument.getDocumentFileName());
                System.out.println(category);
                System.out.println(DocTypeOtherDocumentsEnum.getValue(category));
                otherDocsList.add(Element.<OtherDocuments>builder()
                                      .id(UUID.randomUUID())
                                      .value(OtherDocuments.builder()
                                                 .documentOther(documentOther)
                                                 .documentName(caseDocument.getDocumentFileName())
                                                 .documentTypeOther(DocTypeOtherDocumentsEnum.getValue(category))
                                                 .build())
                                      .build());
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

                if (filteredHearing != null && CollectionUtils.isNotEmpty(filteredHearing.getCaseHearings())) {
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
