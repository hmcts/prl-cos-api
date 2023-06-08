package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.PaymentStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Supplement;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadAdditionalApplicationService {

    private final IdamClient idamClient;
    private final ObjectMapper objectMapper;

    private final ApplicationsFeeCalculator applicationsFeeCalculator;
    private final PaymentRequestService paymentRequestService;

    private final FeeService feeService;

    public FeeResponse getAdditionalApplicationElements(String authorisation, CaseData caseData,
                                                        List<Element<AdditionalApplicationsBundle>> additionalApplicationElements) {
        UserDetails userDetails = idamClient.getUserDetails(authorisation);
        FeeResponse feeResponse = null;
        if (caseData.getUploadAdditionalApplicationData() != null) {
            String applicantName = getSelectedApplicantName(caseData.getUploadAdditionalApplicationData().getAdditionalApplicantsList());
            String author = userDetails.getEmail();
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(
                "dd-MMM-yyyy HH:mm:ss a",
                Locale.UK
            ));
            C2DocumentBundle c2DocumentBundle = null;
            OtherApplicationsBundle otherApplicationsBundle = null;
            c2DocumentBundle = getC2DocumentBundle(caseData, author, currentDateTime, applicantName, c2DocumentBundle);
            otherApplicationsBundle = getOtherApplicationsBundle(caseData,
                                                                 author,
                                                                 currentDateTime, applicantName,
                                                                 otherApplicationsBundle
            );

            List<FeeType> feeTypes = applicationsFeeCalculator.getFeeTypes(caseData.getUploadAdditionalApplicationData());
            if (CollectionUtils.isNotEmpty(feeTypes)) {
                feeResponse = feeService.getFeesDataForAdditionalApplications(feeTypes);
            }

            AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder().author(
                    author).uploadedDateTime(currentDateTime).c2DocumentBundle(c2DocumentBundle).otherApplicationsBundle(
                    otherApplicationsBundle)
                .applicationsFeesToPay(null != feeResponse ? PrlAppsConstants.CURRENCY_SIGN_POUND + feeResponse.getAmount() : null)
                .paymentStatus(null != feeResponse ? PaymentStatus.pending_payment.getDisplayedValue()
                                   : PaymentStatus.not_applicable.getDisplayedValue())
                .build();

            additionalApplicationElements.add(element(additionalApplicationsBundle));
        }
        return feeResponse;
    }

    private String getSelectedApplicantName(DynamicMultiSelectList applicantsList) {
        String applicantName = "";
        if (Objects.nonNull(applicantsList)) {
            List<DynamicMultiselectListElement> selectedElement = applicantsList.getValue();
            if (isNotEmpty(selectedElement)) {
                List<String> appList = selectedElement.stream().map(DynamicMultiselectListElement::getLabel)
                    .collect(Collectors.toList());
                applicantName = String.join(",",appList);
            }
        }
        return applicantName;
    }

    private OtherApplicationsBundle getOtherApplicationsBundle(CaseData caseData, String author,
                                                               String currentDateTime, String applicantName,
                                                               OtherApplicationsBundle otherApplicationsBundle) {
        if (caseData.getUploadAdditionalApplicationData().getTemporaryOtherApplicationsBundle() != null) {
            OtherApplicationsBundle temporaryOtherApplicationsBundle = caseData.getUploadAdditionalApplicationData()
                .getTemporaryOtherApplicationsBundle();
            otherApplicationsBundle = OtherApplicationsBundle.builder()
                .author(author)
                .uploadedDateTime(currentDateTime)
                .applicantName(applicantName)
                .document(temporaryOtherApplicationsBundle.getDocument())
                .documentAcknowledge(temporaryOtherApplicationsBundle.getDocumentAcknowledge())
                .parentalResponsibilityType(temporaryOtherApplicationsBundle.getParentalResponsibilityType())
                .urgencyTimeFrameType(temporaryOtherApplicationsBundle.getUrgencyTimeFrameType())
                .supplementsBundle(createSupplementsBundle(temporaryOtherApplicationsBundle.getSupplementsBundle(),
                                                                                                                      author))
                .supportingEvidenceBundle(createSupportingEvidenceBundle(temporaryOtherApplicationsBundle.getSupportingEvidenceBundle(),
                                                                       author))
                .build();
        }
        return otherApplicationsBundle;
    }

    private C2DocumentBundle getC2DocumentBundle(CaseData caseData, String author, String currentDateTime, String applicantName,
                                                 C2DocumentBundle c2DocumentBundle) {
        if (caseData.getUploadAdditionalApplicationData().getTemporaryC2Document() != null) {
            C2DocumentBundle temporaryC2Document = caseData.getUploadAdditionalApplicationData().getTemporaryC2Document();
            c2DocumentBundle = C2DocumentBundle.builder()
                .author(author)
                .uploadedDateTime(currentDateTime)
                .applicantName(applicantName)
                .document(temporaryC2Document.getDocument())
                .documentAcknowledge(temporaryC2Document.getDocumentAcknowledge())
                .c2AdditionalOrdersRequested(
                temporaryC2Document.getC2AdditionalOrdersRequested()).parentalResponsibilityType(
                    temporaryC2Document.getParentalResponsibilityType())
                .hearingList(temporaryC2Document.getHearingList())
                .urgencyTimeFrameType(temporaryC2Document.getUrgencyTimeFrameType())
                .additionalDraftOrdersBundle(temporaryC2Document.getAdditionalDraftOrdersBundle())
                .supplementsBundle(createSupplementsBundle(temporaryC2Document.getSupplementsBundle(), author))
                .supportingEvidenceBundle(createSupportingEvidenceBundle(temporaryC2Document.getSupportingEvidenceBundle(), author))
                .build();
        }
        return c2DocumentBundle;
    }

    private List<Element<Supplement>> createSupplementsBundle(List<Element<Supplement>> supplementsBundle, String author) {
        List<Element<Supplement>> supplementElementList = new ArrayList<>();
        if (supplementsBundle != null && !supplementsBundle.isEmpty()) {
            for (Element<Supplement> supplementElement : supplementsBundle) {
                Supplement supplement = Supplement.builder().dateTimeUploaded(LocalDateTime.now()).document(
                    supplementElement.getValue().getDocument()).notes(supplementElement.getValue().getNotes()).name(
                    supplementElement.getValue().getName()).secureAccommodationType(
                        supplementElement.getValue().getSecureAccommodationType()).documentAcknowledge(
                    supplementElement.getValue().getDocumentAcknowledge()).uploadedBy(author).build();
                supplementElementList.add(element(supplement));
            }
        }
        return supplementElementList;
    }

    private List<Element<SupportingEvidenceBundle>> createSupportingEvidenceBundle(List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle,
                                                                                   String author) {
        List<Element<SupportingEvidenceBundle>> supportingElementList = new ArrayList<>();
        if (supportingEvidenceBundle != null && !supportingEvidenceBundle.isEmpty()) {
            for (Element<SupportingEvidenceBundle> supportingEvidenceBundleElement : supportingEvidenceBundle) {
                SupportingEvidenceBundle supportingEvidence = SupportingEvidenceBundle.builder().dateTimeUploaded(
                    LocalDateTime.now()).document(supportingEvidenceBundleElement.getValue().getDocument()).notes(
                    supportingEvidenceBundleElement.getValue().getNotes()).name(
                        supportingEvidenceBundleElement.getValue().getName()).documentAcknowledge(
                    supportingEvidenceBundleElement.getValue().getDocumentAcknowledge()).uploadedBy(author).build();
                supportingElementList.add(element(supportingEvidence));
            }
        }
        return supportingElementList;
    }


    public Map<String, Object> calculateAdditionalApplicationsFee(CallbackRequest callbackRequest, String authorisation) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        return applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);
    }

    public Map<String, Object> createUploadAdditionalApplicationBundle(String authorisation, CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<Element<AdditionalApplicationsBundle>> additionalApplicationElements = new ArrayList<>();
        if (caseData.getAdditionalApplicationsBundle() != null && !caseData.getAdditionalApplicationsBundle().isEmpty()) {
            additionalApplicationElements = caseData.getAdditionalApplicationsBundle();
        }
        FeeResponse feeResponse =
            getAdditionalApplicationElements(
                authorisation,
                caseData,
                additionalApplicationElements
            );
        additionalApplicationElements.sort(Comparator.comparing(
            m -> m.getValue().getUploadedDateTime(),
            Comparator.reverseOrder()
        ));
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("additionalApplicationsBundle", additionalApplicationElements);

        if (null != feeResponse) {
            PaymentServiceResponse paymentServiceResponse = paymentRequestService.createServiceRequestForAdditionalApplications(
                callbackRequest,
                authorisation,
                feeResponse
            );
            log.info("PaymentServiceResponse ===> " + paymentServiceResponse);
        }
        return caseDataUpdated;
    }
}
