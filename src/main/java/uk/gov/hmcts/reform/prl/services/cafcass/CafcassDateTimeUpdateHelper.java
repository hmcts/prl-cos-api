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
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResponseToAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.DocumentLink;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.ApplicantDetails;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseData;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Element;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.manageorder.CaseOrder;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum.applicantApplication;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassUpdateHelperUtils.addInOtherDocuments;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassUpdateHelperUtils.buildCaseDataWithProcessedDocumentsCleared;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassUpdateHelperUtils.filterCancelledHearingsBeforeListing;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassUpdateHelperUtils.isCafcassCymruRegion;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassUpdateHelperUtils.isDocumentPresent;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassUpdateHelperUtils.parseQuarantineLegalDocs;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassUpdateHelperUtils.removeRedactedDocuments;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassUpdateHelperUtils.updateCaseWithHearingData;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeList;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassDateTimeUpdateHelper {

    public static final String CONFIDENTIAL = "confidential";
    public static final String ANY_OTHER_DOC = "anyOtherDoc";

    private final CafCassFilter cafCassFilter;
    private final HearingService hearingService;
    private final SystemUserService systemUserService;
    private final ObjectMapper objMapper;

    @Value("#{'${cafcaas.excludedDocumentCategories}'.split(',')}")
    private List<String> excludedDocumentCategoryList;

    @Value("#{'${cafcaas.excludedDocuments}'.split(',')}")
    private List<String> excludedDocumentList;

    public boolean hasCafcassCaseDataChanged(CaseDetails caseDetails, CaseDetails caseDetailsBefore) {
        String authorisation = systemUserService.getSysUserToken();

        return !Objects.equals(
            prepareForComparison(caseDetails, authorisation),
            prepareForComparison(caseDetailsBefore, authorisation)
        );
    }

    private CafCassCaseDetail prepareForComparison(CaseDetails caseDetails, String authorisation) {
        CafCassCaseDetail cafCassCaseDetail = convertToCafcassCaseDetail(caseDetails);
        if (cafCassCaseDetail == null) {
            return null;
        }

        cafCassCaseDetail = applyCafcassFilter(cafCassCaseDetail);
        cafCassCaseDetail = getHearingDetailsForCase(authorisation, cafCassCaseDetail);
        if (cafCassCaseDetail == null) {
            return null;
        }
        addSpecificDocumentsFromCaseFileViewBasedOnCategories(cafCassCaseDetail);
        cafCassCaseDetail = removeUnnecessaryFieldsFromResponse(cafCassCaseDetail);
        removeRedactedDocumentsFromResponse(cafCassCaseDetail);
        return cafCassCaseDetail;
    }

    private CafCassCaseDetail convertToCafcassCaseDetail(CaseDetails caseData) {
        if (caseData == null) {
            return null;
        }

        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        CafCassCaseDetail cafCassCaseDetail = objectMapper.convertValue(caseData, CafCassCaseDetail.class);
        CafCassCaseData cafCassCaseData = caseData.getData() == null
            ? CafCassCaseData.builder().build()
            : objectMapper.convertValue(caseData.getData(), CafCassCaseData.class);
        cafCassCaseDetail.setCaseData(cafCassCaseData);
        return cafCassCaseDetail;
    }

    private CafCassCaseDetail applyCafcassFilter(CafCassCaseDetail cafCassCaseDetail) {
        CafCassCaseData caseData = cafCassCaseDetail.getCaseData();
        final CafCassCaseData cafCassCaseData = caseData.toBuilder()
            .applicants(cafCassFilter.filterNonValueList(caseData.getApplicants()))
            .otherPeopleInTheCaseTable(cafCassFilter.filterNonValueList(caseData.getOtherPeopleInTheCaseTable()))
            .respondents(cafCassFilter.filterNonValueList(caseData.getRespondents()))
            .children(cafCassFilter.filterNonValueList(caseData.getChildren()))
            .interpreterNeeds(cafCassFilter.filterNonValueList(caseData.getInterpreterNeeds()))
            .otherDocuments(cafCassFilter.filterNonValueList(caseData.getOtherDocuments()))
            .manageOrderCollection(cafCassFilter.filterNonValueList(caseData.getManageOrderCollection()))
            .orderCollection(cafCassFilter.filterNonValueList(caseData.getOrderCollection()))
            .build();
        cafCassCaseDetail.setCaseData(cafCassCaseData);
        log.info("After applying filter Result Size --> {}", 1);
        return cafCassCaseDetail;
    }

    private CafCassCaseDetail getHearingDetailsForCase(String authorisation, CafCassCaseDetail cafCassCaseDetail) {
        Map<String, String> caseIdWithRegionIdMap = new HashMap<>();
        CaseManagementLocation caseManagementLocation = cafCassCaseDetail.getCaseData().getCaseManagementLocation();
        if (caseManagementLocation == null) {
            return null;
        }
        if (isCafcassCymruRegion(caseManagementLocation.getRegionId())) {
            addCaseRegionMapping(
                caseIdWithRegionIdMap,
                cafCassCaseDetail,
                caseManagementLocation.getRegionId(),
                caseManagementLocation.getBaseLocationId()
            );
        } else if (caseManagementLocation.getRegion() != null && Integer.parseInt(caseManagementLocation.getRegion()) < 7) {
            addCaseRegionMapping(
                caseIdWithRegionIdMap,
                cafCassCaseDetail,
                caseManagementLocation.getRegion(),
                caseManagementLocation.getBaseLocation()
            );
            cafCassCaseDetail.getCaseData().setCafcassUploadedDocs(null);
        } else {
            return null;
        }

        List<Hearings> listOfHearingDetails = hearingService.getHearingsForAllCases(
            authorisation,
            caseIdWithRegionIdMap
        );
        filterCancelledHearingsBeforeListing(listOfHearingDetails);
        updateHearingDataCafcass(cafCassCaseDetail, listOfHearingDetails);
        return cafCassCaseDetail;
    }

    private void addCaseRegionMapping(Map<String, String> caseIdWithRegionIdMap,
                                      CafCassCaseDetail cafCassCaseDetail,
                                      String region,
                                      String baseLocation) {
        caseIdWithRegionIdMap.put(String.valueOf(cafCassCaseDetail.getId()), region + "-" + baseLocation);
        cafCassCaseDetail.getCaseData().setCourtEpimsId(baseLocation);
    }

    private void updateHearingDataCafcass(CafCassCaseDetail cafCassCaseDetail, List<Hearings> listOfHearingDetails) {
        if (CollectionUtils.isNotEmpty(listOfHearingDetails)) {
            Hearings filteredHearing =
                listOfHearingDetails.stream().filter(hearings -> hearings.getCaseRef().equals(String.valueOf(
                    cafCassCaseDetail.getId()))).findFirst().orElse(null);

            if (filteredHearing != null && CollectionUtils.isNotEmpty(filteredHearing.getCaseHearings())) {
                updateCaseWithHearingData(cafCassCaseDetail, filteredHearing);
            }
        }
    }

    private void addSpecificDocumentsFromCaseFileViewBasedOnCategories(CafCassCaseDetail cafCassCaseDetail) {
        log.info("Adding documents for case ID {} ", cafCassCaseDetail.getId());
        List<Element<OtherDocuments>> otherDocsList = new ArrayList<>();
        CafCassCaseData caseData = cafCassCaseDetail.getCaseData();
        populateReviewDocuments(otherDocsList, caseData);
        populateRespondentC1AResponseDoc(caseData.getRespondents(), otherDocsList);
        populateConfidentialDoc(caseData, otherDocsList);
        populateBundleDoc(caseData, otherDocsList);
        populateAnyOtherDoc(caseData, otherDocsList);
        populateAdditionalOrderDocuments(caseData, otherDocsList);

        List<Element<ApplicantDetails>> respondents = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseData.getRespondents())) {
            caseData.getRespondents().forEach(applicantDetailsElement -> {
                ApplicantDetails applicantDetails = applicantDetailsElement.getValue().toBuilder().response(null).build();
                respondents.add(Element.<ApplicantDetails>builder().id(applicantDetailsElement.getId()).value(
                    applicantDetails).build());
            });
        }

        cafCassCaseDetail.setCaseData(buildCaseDataWithProcessedDocumentsCleared(caseData, otherDocsList, respondents));
    }

    private void populateAnyOtherDoc(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        addCaseDocuments(ANY_OTHER_DOC, caseData.getOtherDocumentsUploaded(), otherDocsList);
        addInOtherDocuments(ANY_OTHER_DOC, caseData.getUploadOrderDoc(), otherDocsList);
        populateServiceOfApplicationUploadDocs(caseData, otherDocsList);
        populateStatementOfServiceDocs(caseData, otherDocsList);
    }

    private void addCaseDocuments(String category,
                                  List<uk.gov.hmcts.reform.prl.models.documents.Document> documents,
                                  List<Element<OtherDocuments>> otherDocsList) {
        nullSafeList(documents).forEach(document -> addInOtherDocuments(category, document, otherDocsList));
    }

    private void populateStatementOfServiceDocs(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        addStatementOfServiceDocuments(caseData.getStmtOfServiceForOrder(), otherDocsList);
        addStatementOfServiceDocuments(caseData.getStmtOfServiceForApplication(), otherDocsList);
        addStatementOfServiceDocuments(caseData.getStmtOfServiceAddRecipient(), otherDocsList);
    }

    private void addStatementOfServiceDocuments(List<uk.gov.hmcts.reform.prl.models.Element<StmtOfServiceAddRecipient>> documents,
                                                List<Element<OtherDocuments>> otherDocsList) {
        nullSafeList(documents).forEach(
            documentElement -> addInOtherDocuments(
                ANY_OTHER_DOC,
                documentElement.getValue().getStmtOfServiceDocument(),
                otherDocsList
            ));
    }

    private void populateAdditionalOrderDocuments(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        nullSafeList(caseData.getAdditionalOrderDocuments())
            .stream()
            .flatMap(el -> el.getValue().getAdditionalDocuments().stream())
            .forEach(doc -> addInOtherDocuments(applicantApplication.getId(), doc.getValue(), otherDocsList));
    }

    private void populateServiceOfApplicationUploadDocs(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        addInOtherDocuments(ANY_OTHER_DOC, caseData.getSpecialArrangementsLetter(), otherDocsList);
        addInOtherDocuments(ANY_OTHER_DOC, caseData.getAdditionalDocuments(), otherDocsList);
        addDocumentElements(ANY_OTHER_DOC, caseData.getAdditionalDocumentsList(), otherDocsList);

        if (CollectionUtils.isNotEmpty(caseData.getFinalServedApplicationDetailsList())) {
            caseData.getFinalServedApplicationDetailsList().forEach(
                servedApplicationDetails -> {
                    nullSafeList(servedApplicationDetails.getValue().getBulkPrintDetails()).forEach(
                        bulkPrintDetailsElement ->
                            processServiceOfApplicationBulkPrintDocs(bulkPrintDetailsElement.getValue(), otherDocsList)
                    );
                    nullSafeList(servedApplicationDetails.getValue().getEmailNotificationDetails())
                        .forEach(emailNotificationDetailsElement -> processServiceOfApplicationEmailedDocs(
                            emailNotificationDetailsElement.getValue(),
                            otherDocsList
                        ));
                }
            );
        }
    }

    private void addDocumentElements(
        String category,
        List<uk.gov.hmcts.reform.prl.models.Element<uk.gov.hmcts.reform.prl.models.documents.Document>> documents,
        List<Element<OtherDocuments>> otherDocsList
    ) {
        nullSafeList(documents).forEach(documentElement -> addInOtherDocuments(category, documentElement.getValue(), otherDocsList));
    }

    private void processServiceOfApplicationBulkPrintDocs(BulkPrintDetails bulkPrintDetails,
                                                          List<Element<OtherDocuments>> otherDocsList) {
        addDocumentElementsIfMissing(bulkPrintDetails.getPrintDocs(), otherDocsList);
    }

    private void processServiceOfApplicationEmailedDocs(EmailNotificationDetails emailNotificationDetails,
                                                        List<Element<OtherDocuments>> otherDocsList) {
        addDocumentElementsIfMissing(emailNotificationDetails.getDocs(), otherDocsList);
    }

    private void addDocumentElementsIfMissing(
        List<uk.gov.hmcts.reform.prl.models.Element<uk.gov.hmcts.reform.prl.models.documents.Document>> documents,
        List<Element<OtherDocuments>> otherDocsList
    ) {
        nullSafeList(documents).forEach(
            docElement -> addOtherDocumentIfMissing(docElement.getValue(), otherDocsList)
        );
    }

    private void addOtherDocumentIfMissing(uk.gov.hmcts.reform.prl.models.documents.Document caseDocument,
                                           List<Element<OtherDocuments>> otherDocsList) {
        if (!isDocumentPresent(caseDocument, otherDocsList)) {
            addInOtherDocuments(ANY_OTHER_DOC, caseDocument, otherDocsList);
        }
    }

    private void populateBundleDoc(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        if (null != caseData.getBundleInformation()
            && null != caseData.getBundleInformation().getCaseBundles()
            && CollectionUtils.isNotEmpty(caseData.getBundleInformation().getCaseBundles())) {
            caseData.getBundleInformation().getCaseBundles().forEach(bundle -> {
                DocumentLink stitchedDocument = bundle.getValue().getStitchedDocument();
                addBundleDocument(stitchedDocument, otherDocsList);
            });
        }
    }

    private void populateReviewDocuments(List<Element<OtherDocuments>> otherDocsList, CafCassCaseData caseData) {
        Arrays.asList(
            caseData.getCourtStaffUploadDocListDocTab(),
            caseData.getLegalProfUploadDocListDocTab(),
            caseData.getCafcassUploadDocListDocTab(),
            caseData.getLocalAuthorityUploadDocListDocTab(),
            caseData.getCitizenUploadedDocListDocTab(),
            caseData.getConfidentialDocuments(),
            caseData.getBulkScannedDocListDocTab(),
            caseData.getRestrictedDocuments()
        ).forEach(quarantineLegalDocs -> parseQuarantineLegalDocsIfPresent(otherDocsList, quarantineLegalDocs));
    }

    private void addBundleDocument(DocumentLink stitchedDocument, List<Element<OtherDocuments>> otherDocsList) {
        if (ObjectUtils.isNotEmpty(stitchedDocument)) {
            uk.gov.hmcts.reform.prl.models.documents.Document document =
                uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                    .documentFileName(stitchedDocument.getDocumentFilename())
                    .documentUrl(stitchedDocument.getDocumentUrl())
                    .build();
            addInOtherDocuments("courtBundle", document, otherDocsList);
        }
    }

    private void parseQuarantineLegalDocsIfPresent(
        List<Element<OtherDocuments>> otherDocsList,
        List<uk.gov.hmcts.reform.prl.models.Element<QuarantineLegalDoc>> quarantineLegalDocs
    ) {
        if (CollectionUtils.isNotEmpty(quarantineLegalDocs)) {
            parseQuarantineLegalDocs(
                otherDocsList,
                quarantineLegalDocs,
                objMapper,
                excludedDocumentCategoryList,
                excludedDocumentList
            );
        }
    }

    private void populateConfidentialDoc(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        Arrays.asList(
            caseData.getRespondentAc8Documents(),
            caseData.getRespondentBc8Documents(),
            caseData.getRespondentCc8Documents(),
            caseData.getRespondentDc8Documents(),
            caseData.getRespondentEc8Documents()
        ).forEach(responseDocuments -> populateRespondentC8Documents(responseDocuments, otherDocsList));
        addCaseDocuments(CONFIDENTIAL, caseData.getC8FormDocumentsUploaded(), otherDocsList);
    }

    private void populateRespondentC8Documents(List<uk.gov.hmcts.reform.prl.models.Element<ResponseDocuments>> responseDocuments,
                                               List<Element<OtherDocuments>> otherDocsList) {
        nullSafeList(responseDocuments).forEach(responseDocumentsElement -> populateRespondentDocument(
            responseDocumentsElement.getValue().getRespondentC8Document(),
            responseDocumentsElement.getValue().getRespondentC8DocumentWelsh(),
            CONFIDENTIAL,
            otherDocsList
        ));
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

    private void populateRespondentC1AResponseDoc(List<Element<ApplicantDetails>> respondents,
                                                  List<Element<OtherDocuments>> otherDocsList) {
        if (CollectionUtils.isNotEmpty(respondents)) {
            respondents.forEach(respondent -> {
                if (null != respondent.getValue().getResponse()
                    && null != respondent.getValue().getResponse().getResponseToAllegationsOfHarm()) {
                    ResponseToAllegationsOfHarm responseToAllegationsOfHarm =
                        respondent.getValue().getResponse().getResponseToAllegationsOfHarm();
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

    private CafCassCaseDetail removeUnnecessaryFieldsFromResponse(CafCassCaseDetail cafCassCaseDetail) {
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
        return cafCassCaseDetail;
    }

    private void removeRedactedDocumentsFromResponse(CafCassCaseDetail cafCassCaseDetail) {
        CafCassCaseData caseData = cafCassCaseDetail.getCaseData();
        if (caseData != null) {
            caseData.setOtherDocuments(removeRedactedDocuments(caseData.getOtherDocuments(), this::getOtherDocumentId));
            cafCassCaseDetail.setCaseData(caseData.toBuilder()
                                              .orderCollection(removeRedactedDocuments(
                                                  caseData.getOrderCollection(),
                                                  this::getOrderDocumentId
                                              ))
                                              .build());
        }
    }

    private String getOtherDocumentId(OtherDocuments otherDocument) {
        return otherDocument.getDocumentOther() != null ? otherDocument.getDocumentOther().getDocumentId() : null;
    }

    private String getOrderDocumentId(CaseOrder order) {
        return order.getOrderDocument() != null ? order.getOrderDocument().getDocumentId() : null;
    }

    private List<Element<CaseOrder>> removeServeOrderDetails(List<Element<CaseOrder>> orderCollection) {
        return updateElementValues(orderCollection, order -> order.setServeOrderDetails(null));
    }

    private List<Element<ApplicantDetails>> removeResponse(List<Element<ApplicantDetails>> partyDetails) {
        return updateElementValues(partyDetails, partyDetail -> partyDetail.setResponse(null));
    }

    private <T> List<Element<T>> updateElementValues(List<Element<T>> elements, Consumer<T> valueUpdater) {
        nullSafeList(elements).forEach(element -> {
            if (null != element.getValue()) {
                valueUpdater.accept(element.getValue());
            }
        });
        return elements;
    }
}
