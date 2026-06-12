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
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResponseToAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.ApplicantDetails;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseData;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Document;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Element;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.manageorder.CaseOrder;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CANCELLED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REDACTED_DOCUMENT_UUID;
import static uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum.applicantApplication;
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
        if (caseManagementLocation.getRegionId() != null
            && Integer.parseInt(caseManagementLocation.getRegionId()) < 7) {
            caseIdWithRegionIdMap.put(cafCassCaseDetail.getId().toString(), caseManagementLocation.getRegionId()
                + "-" + caseManagementLocation.getBaseLocationId());
            cafCassCaseDetail.getCaseData().setCourtEpimsId(caseManagementLocation.getBaseLocationId());
        } else if (caseManagementLocation.getRegion() != null && Integer.parseInt(caseManagementLocation.getRegion()) < 7) {
            caseIdWithRegionIdMap.put(
                String.valueOf(cafCassCaseDetail.getId()),
                caseManagementLocation.getRegion() + "-" + caseManagementLocation.getBaseLocation()
            );
            cafCassCaseDetail.getCaseData().setCourtEpimsId(caseManagementLocation.getBaseLocation());
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

    private void filterCancelledHearingsBeforeListing(List<Hearings> listOfHearingDetails) {
        if (CollectionUtils.isNotEmpty(listOfHearingDetails)) {
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

    private void updateHearingDataCafcass(CafCassCaseDetail cafCassCaseDetail, List<Hearings> listOfHearingDetails) {
        if (CollectionUtils.isNotEmpty(listOfHearingDetails)) {
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
    }

    private void populateAnyOtherDoc(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        if (CollectionUtils.isNotEmpty(caseData.getOtherDocumentsUploaded())) {
            caseData.getOtherDocumentsUploaded().forEach(document -> addInOtherDocuments(
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
        if (null != caseData.getSpecialArrangementsLetter()) {
            addInOtherDocuments(ANY_OTHER_DOC, caseData.getSpecialArrangementsLetter(), otherDocsList);
        }
        if (null != caseData.getAdditionalDocuments()) {
            addInOtherDocuments(ANY_OTHER_DOC, caseData.getAdditionalDocuments(), otherDocsList);
        }
        if (CollectionUtils.isNotEmpty(caseData.getAdditionalDocumentsList())) {
            caseData.getAdditionalDocumentsList().forEach(
                documentElement -> addInOtherDocuments(ANY_OTHER_DOC, documentElement.getValue(), otherDocsList)
            );
        }

        if (ObjectUtils.isNotEmpty(caseData.getFinalServedApplicationDetailsList())) {
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

    private void processServiceOfApplicationBulkPrintDocs(BulkPrintDetails bulkPrintDetails,
                                                          List<Element<OtherDocuments>> otherDocsList) {
        bulkPrintDetails.getPrintDocs().forEach(
            docElement -> addOtherDocumentIfMissing(docElement.getValue(), otherDocsList)
        );
    }

    private void processServiceOfApplicationEmailedDocs(EmailNotificationDetails emailNotificationDetails,
                                                        List<Element<OtherDocuments>> otherDocsList) {
        nullSafeList(emailNotificationDetails.getDocs()).forEach(
            docElement -> addOtherDocumentIfMissing(docElement.getValue(), otherDocsList)
        );
    }

    private void addOtherDocumentIfMissing(uk.gov.hmcts.reform.prl.models.documents.Document caseDocument,
                                           List<Element<OtherDocuments>> otherDocsList) {
        if (!isDocumentPresent(caseDocument, otherDocsList)) {
            addInOtherDocuments(ANY_OTHER_DOC, caseDocument, otherDocsList);
        }
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
            caseData.getBundleInformation().getCaseBundles().forEach(bundle -> {
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
        parseQuarantineLegalDocsIfPresent(otherDocsList, caseData.getCourtStaffUploadDocListDocTab());
        parseQuarantineLegalDocsIfPresent(otherDocsList, caseData.getLegalProfUploadDocListDocTab());
        parseQuarantineLegalDocsIfPresent(otherDocsList, caseData.getCafcassUploadDocListDocTab());
        parseQuarantineLegalDocsIfPresent(otherDocsList, caseData.getLocalAuthorityUploadDocListDocTab());
        parseQuarantineLegalDocsIfPresent(otherDocsList, caseData.getCitizenUploadedDocListDocTab());
        parseQuarantineLegalDocsIfPresent(otherDocsList, caseData.getConfidentialDocuments());
        parseQuarantineLegalDocsIfPresent(otherDocsList, caseData.getBulkScannedDocListDocTab());
        parseQuarantineLegalDocsIfPresent(otherDocsList, caseData.getRestrictedDocuments());
    }

    private void parseQuarantineLegalDocsIfPresent(
        List<Element<OtherDocuments>> otherDocsList,
        List<uk.gov.hmcts.reform.prl.models.Element<QuarantineLegalDoc>> quarantineLegalDocs
    ) {
        if (CollectionUtils.isNotEmpty(quarantineLegalDocs)) {
            parseQuarantineLegalDocs(otherDocsList, quarantineLegalDocs);
        }
    }

    private void populateConfidentialDoc(CafCassCaseData caseData, List<Element<OtherDocuments>> otherDocsList) {
        populateRespondentC8Documents(caseData.getRespondentAc8Documents(), otherDocsList);
        populateRespondentC8Documents(caseData.getRespondentBc8Documents(), otherDocsList);
        populateRespondentC8Documents(caseData.getRespondentCc8Documents(), otherDocsList);
        populateRespondentC8Documents(caseData.getRespondentDc8Documents(), otherDocsList);
        populateRespondentC8Documents(caseData.getRespondentEc8Documents(), otherDocsList);
        if (CollectionUtils.isNotEmpty(caseData.getC8FormDocumentsUploaded())) {
            caseData.getC8FormDocumentsUploaded().forEach(c8FormDocumentsUploaded ->
                populateRespondentDocument(c8FormDocumentsUploaded, null, CONFIDENTIAL, otherDocsList));
        }
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

    private void parseQuarantineLegalDocs(List<Element<OtherDocuments>> otherDocsList,
                                          List<uk.gov.hmcts.reform.prl.models.Element<QuarantineLegalDoc>> quarantineLegalDocs) {
        quarantineLegalDocs.forEach(quarantineLegalDocElement -> {
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
                    otherDocsList
                );
            }
        });
    }

    private void parseCategoryAndCreateList(String category,
                                            uk.gov.hmcts.reform.prl.models.documents.Document caseDocument,
                                            List<Element<OtherDocuments>> otherDocsList) {
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
            if (null != caseDocument && caseDocument.getDocumentUrl() != null
                && !caseDocument.getDocumentUrl().endsWith(REDACTED_DOCUMENT_UUID)) {
                Document documentOther = buildFromCaseDocument(caseDocument);
                otherDocsList.add(Element.<OtherDocuments>builder()
                                      .id(buildDocumentElementId(category, caseDocument))
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

    private UUID buildDocumentElementId(String category, uk.gov.hmcts.reform.prl.models.documents.Document caseDocument) {
        return UUID.nameUUIDFromBytes(
            (category + "|" + caseDocument.getDocumentUrl() + "|" + caseDocument.getDocumentFileName())
                .getBytes(StandardCharsets.UTF_8)
        );
    }

    private boolean checkIfDocumentsNeedToExclude(List<String> excludedDocumentList, String documentFilename) {
        boolean isExcluded = false;
        for (String excludedDocumentName : excludedDocumentList) {
            if (documentFilename.contains(excludedDocumentName)) {
                isExcluded = true;
                break;
            }
        }
        return isExcluded;
    }

    private Document buildFromCaseDocument(uk.gov.hmcts.reform.prl.models.documents.Document caseDocument)
        throws MalformedURLException {
        URI uri = URI.create(caseDocument.getDocumentUrl());
        URL url = uri.toURL();
        return Document.builder()
            .documentId(CafCassCaseData.getDocumentId(url))
            .documentFileName(caseDocument.getDocumentFileName())
            .build();
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
}
