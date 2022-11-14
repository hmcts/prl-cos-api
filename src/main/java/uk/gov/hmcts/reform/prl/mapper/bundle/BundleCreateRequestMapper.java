package uk.gov.hmcts.reform.prl.mapper.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.Applications;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.Data;
import uk.gov.hmcts.reform.prl.models.dto.bundle.DocumentLink;
import uk.gov.hmcts.reform.prl.models.dto.bundle.Order;
import uk.gov.hmcts.reform.prl.models.dto.bundle.Value;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts.restrictToGroup;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BundleCreateRequestMapper {
    public BundleCreateRequest mapCaseDataToBundleCreateRequest(CaseData caseData, String eventId, String bundleConfigFileName) {
        BundleCreateRequest bundleCreateRequest = BundleCreateRequest.builder().caseDetails(CaseDetails.builder().id(String.valueOf(caseData.getId()))
                .caseData(mapCaseData(caseData, bundleConfigFileName)).build())
            .caseTypeId(CASE_TYPE).jurisdictionId(JURISDICTION).eventId(eventId).build();
        //log.info("*** createbundle request payload  : {}", bundleCreateRequest.toString());
        return bundleCreateRequest;
    }

    private uk.gov.hmcts.reform.prl.models.dto.bundle.CaseData mapCaseData(CaseData caseData, String bundleConfigFileName) {
        return uk.gov.hmcts.reform.prl.models.dto.bundle.CaseData.builder().id(String.valueOf(caseData.getId())).bundleConfiguration(
                bundleConfigFileName)
            .data(Data.builder().caseNumber(String.valueOf(caseData.getId())).applicantCaseName(caseData.getApplicantCaseName())
                .otherDocuments(mapOtherDocumentsFromCaseData(caseData.getOtherDocuments())).applications(mapApplicationsFromCaseData(caseData))
                .orders(mapOrdersFromCaseData(caseData.getOrderCollection())).build()).build();

    }

    private List<Applications> mapApplicationsFromCaseData(CaseData caseData) {
        List<Applications> applications = new ArrayList<>();
        if (YesOrNo.Yes.equals(caseData.getLanguagePreferenceWelsh())) {
            applications.add(mapApplicationsFromDocument(caseData.getFinalWelshDocument()));
            applications.add(mapApplicationsFromDocument(caseData.getC1AWelshDocument()));
        } else {
            applications.add(mapApplicationsFromDocument(caseData.getFinalDocument()));
            applications.add(mapApplicationsFromDocument(caseData.getC1ADocument()));
        }
        applications.addAll(mapApplicationsFromFurtherEvidences(caseData.getFurtherEvidences()));
        return applications;
    }

    private Applications mapApplicationsFromDocument(Document document) {
        return (null != document) ? Applications.builder().appDocument(document).documentFileName(document.getDocumentFileName()).build()
            : Applications.builder().build();
    }

    private List<Applications> mapApplicationsFromFurtherEvidences(List<Element<FurtherEvidence>> furtherEvidencesFromCaseData) {
        List<Applications> applications = new ArrayList<>();
        Optional<List<Element<FurtherEvidence>>> existingFurtherEvidences = ofNullable(furtherEvidencesFromCaseData);
        if (existingFurtherEvidences.isEmpty()) {
            return applications;
        }
        ElementUtils.unwrapElements(furtherEvidencesFromCaseData).forEach(furtherEvidence -> {
            if (!furtherEvidence.getRestrictCheckboxFurtherEvidence().contains(restrictToGroup)) {
                if (!FurtherEvidenceDocumentType.consentOrder.equals(furtherEvidence.getTypeOfDocumentFurtherEvidence())) {
                    applications.add(mapApplicationsFromDocument(furtherEvidence.getDocumentFurtherEvidence()));
                }
            }
        });
        return applications;
    }

    private List<Element<Order>> mapOrdersFromCaseData(List<Element<OrderDetails>> ordersFromCaseData) {
        List<Element<Order>> orders = new ArrayList<>();
        Optional<List<Element<OrderDetails>>> existingOrders = ofNullable(ordersFromCaseData);
        if (existingOrders.isEmpty()) {
            return orders;
        }
        ordersFromCaseData.forEach(orderDetailsElement -> {
            OrderDetails orderDetails = orderDetailsElement.getValue();
            Document document = orderDetails.getOrderDocument();
            orders.add(ElementUtils.element(orderDetailsElement.getId(),
                Order.builder().orderType(orderDetails.getOrderType()).documentFileName(document.getDocumentFileName())
                    .orderDocument(document).build()));
        });
        return orders;
    }

    private List<uk.gov.hmcts.reform.prl.models.dto.bundle.OtherDocument> mapOtherDocumentsFromCaseData(
        List<Element<OtherDocuments>> otherDocumentsFromCaseData) {
        List<uk.gov.hmcts.reform.prl.models.dto.bundle.OtherDocument> otherDocuments = new ArrayList<>();
        Optional<List<Element<OtherDocuments>>> existingOtherDocuments = ofNullable(otherDocumentsFromCaseData);
        if (existingOtherDocuments.isEmpty()) {
            return otherDocuments;
        }
        List<Element<OtherDocuments>> otherDocumentsNotConfidential = otherDocumentsFromCaseData.stream()
            .filter(element -> !element.getValue().getRestrictCheckboxOtherDocuments().contains(restrictToGroup))
            .collect(Collectors.toList());

        otherDocumentsNotConfidential
            .forEach(otherDocumentsElement -> {
                Document otherDocument = otherDocumentsElement.getValue().getDocumentOther();
                otherDocuments.add(uk.gov.hmcts.reform.prl.models.dto.bundle.OtherDocument.builder().id(
                    (null != otherDocumentsElement.getId() ? otherDocumentsElement.getId().toString() : null))
                    .value(Value.builder().documentFileName(otherDocument.getDocumentFileName()).documentLink(
                            DocumentLink.builder().documentFilename(otherDocument.getDocumentFileName())
                                .documentUrl(otherDocument.getDocumentUrl()).documentBinaryUrl(
                                    otherDocument.getDocumentBinaryUrl()).build())
                        .typeOfDocumentFurtherEvidence(otherDocumentsElement.getValue().getDocumentTypeOther().getId()).build()).build());

            });
        return otherDocuments;
    }
}
