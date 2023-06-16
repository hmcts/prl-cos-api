package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.ApplicationStatus;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.PaymentStatus;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.UploadAdditionalApplicationsFieldsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Payment;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Supplement;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.math.BigDecimal;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ADDITIONAL_APPLICATIONS_HELP_WITH_FEES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ADDITIONAL_APPLICATION_FEES_TO_PAY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
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
    public static final String ADDITIONAL_APPLICANTS_LIST = "additionalApplicantsList";
    private final DynamicMultiSelectListService dynamicMultiSelectListService;

    public void getAdditionalApplicationElements(String authorisation, CaseData caseData,
                                                        List<Element<AdditionalApplicationsBundle>> additionalApplicationElements) {
        UserDetails userDetails = idamClient.getUserDetails(authorisation);

        if (caseData.getUploadAdditionalApplicationData() != null) {
            String applicantName = getSelectedApplicantName(caseData.getUploadAdditionalApplicationData().getAdditionalApplicantsList());
            String author = userDetails.getEmail();
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(
                "dd-MMM-yyyy HH:mm:ss a",
                Locale.UK
            ));
            C2DocumentBundle c2DocumentBundle = getC2DocumentBundle(caseData, author, currentDateTime, applicantName);
            OtherApplicationsBundle otherApplicationsBundle = getOtherApplicationsBundle(caseData,
                                                                                         author,
                                                                                         currentDateTime, applicantName
            );

            AdditionalApplicationsBundle additionalApplicationsBundle = getAdditionalApplicationsBundle(
                authorisation,
                caseData,
                author,
                currentDateTime,
                c2DocumentBundle,
                otherApplicationsBundle
            );

            additionalApplicationElements.add(element(additionalApplicationsBundle));
        }
    }

    private AdditionalApplicationsBundle getAdditionalApplicationsBundle(String authorisation, CaseData caseData, String author,
                                                                         String currentDateTime, C2DocumentBundle c2DocumentBundle,
                                                                         OtherApplicationsBundle otherApplicationsBundle) {
        FeeResponse feeResponse = null;
        PaymentServiceResponse paymentServiceResponse = null;
        Payment payment = null;
        List<FeeType> feeTypes = applicationsFeeCalculator.getFeeTypes(caseData.getUploadAdditionalApplicationData());
        if (CollectionUtils.isNotEmpty(feeTypes)) {
            feeResponse = feeService.getFeesDataForAdditionalApplications(feeTypes);
            if (null != feeResponse && feeResponse.getAmount().compareTo(BigDecimal.ZERO) != 0) {
                paymentServiceResponse = paymentRequestService.createServiceRequestForAdditionalApplications(
                    caseData,
                    authorisation,
                    feeResponse
                );
            }
            String hwfReferenceNumber = YesOrNo.Yes.equals(caseData.getUploadAdditionalApplicationData().getAdditionalApplicationsHelpWithFees())
                ? caseData.getUploadAdditionalApplicationData().getAdditionalApplicationsHelpWithFeesNumber() : null;

            payment = Payment.builder()
                .fee(null != feeResponse ? PrlAppsConstants.CURRENCY_SIGN_POUND + feeResponse.getAmount() : null)
                .paymentServiceRequestReferenceNumber(null != paymentServiceResponse
                                                          ? paymentServiceResponse.getServiceRequestReference() : null)
                .hwfReferenceNumber(hwfReferenceNumber)
                .status(null != feeResponse ? PaymentStatus.PENDING.getDisplayedValue()
                            : PaymentStatus.NOT_APPLICABLE.getDisplayedValue())
                .build();
        }
        return AdditionalApplicationsBundle.builder().author(
                author).uploadedDateTime(currentDateTime).c2DocumentBundle(c2DocumentBundle).otherApplicationsBundle(
                otherApplicationsBundle)
            .payment(payment)
            .applicationStatus(null != feeResponse && feeResponse.getAmount().compareTo(BigDecimal.ZERO) != 0
                                   ? ApplicationStatus.PENDING_ON_PAYMENT.getDisplayedValue()
                                   : ApplicationStatus.SUBMITTED.getDisplayedValue())
            .build();
    }

    private String getSelectedApplicantName(DynamicMultiSelectList applicantsList) {
        String applicantName = "";
        if (Objects.nonNull(applicantsList)) {
            List<DynamicMultiselectListElement> selectedElement = applicantsList.getValue();
            if (isNotEmpty(selectedElement)) {
                List<String> appList = selectedElement.stream().map(DynamicMultiselectListElement::getLabel)
                    .collect(Collectors.toList());
                applicantName = String.join(",", appList);
            }
        }
        return applicantName;
    }

    private OtherApplicationsBundle getOtherApplicationsBundle(CaseData caseData, String author,
                                                               String currentDateTime, String applicantName) {
        OtherApplicationsBundle otherApplicationsBundle = null;
        if (caseData.getUploadAdditionalApplicationData().getTemporaryOtherApplicationsBundle() != null) {
            OtherApplicationsBundle temporaryOtherApplicationsBundle = caseData.getUploadAdditionalApplicationData()
                .getTemporaryOtherApplicationsBundle();
            otherApplicationsBundle = OtherApplicationsBundle.builder()
                .author(author)
                .uploadedDateTime(currentDateTime)
                .applicantName(applicantName)
                .document(temporaryOtherApplicationsBundle.getDocument())
                .documentAcknowledge(temporaryOtherApplicationsBundle.getDocumentAcknowledge())
                .urgencyTimeFrameType(temporaryOtherApplicationsBundle.getUrgencyTimeFrameType())
                .supplementsBundle(createSupplementsBundle(
                    temporaryOtherApplicationsBundle.getSupplementsBundle(),
                    author
                ))
                .supportingEvidenceBundle(createSupportingEvidenceBundle(
                    temporaryOtherApplicationsBundle.getSupportingEvidenceBundle(),
                    author
                ))
                .caApplicationType(temporaryOtherApplicationsBundle.getCaApplicationType())
                .daApplicationType(temporaryOtherApplicationsBundle.getDaApplicationType())
                .build();
        }
        return otherApplicationsBundle;
    }

    private C2DocumentBundle getC2DocumentBundle(CaseData caseData, String author, String currentDateTime, String applicantName) {
        C2DocumentBundle c2DocumentBundle = null;
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
                .supportingEvidenceBundle(createSupportingEvidenceBundle(
                    temporaryC2Document.getSupportingEvidenceBundle(),
                    author
                ))
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


    public Map<String, Object> calculateAdditionalApplicationsFee(CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        return applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData);
    }

    public Map<String, Object> createUploadAdditionalApplicationBundle(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<Element<AdditionalApplicationsBundle>> additionalApplicationElements = new ArrayList<>();
        if (caseData.getAdditionalApplicationsBundle() != null && !caseData.getAdditionalApplicationsBundle().isEmpty()) {
            additionalApplicationElements = caseData.getAdditionalApplicationsBundle();
        }
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
        cleanOldUpUploadAdditionalApplicationData(caseDataUpdated);
        return caseDataUpdated;
    }

    private void cleanOldUpUploadAdditionalApplicationData(Map<String, Object> caseDataUpdated) {
        log.info("before cleanUpUploadAdditionalApplicationData caseDataUpdated " + caseDataUpdated);
        for (UploadAdditionalApplicationsFieldsEnum field : UploadAdditionalApplicationsFieldsEnum.values()) {
            if (caseDataUpdated.containsKey(field.getValue())) {
                log.info("removing " + field.getValue());
                caseDataUpdated.remove(field.getValue());
            }
        }
        log.info("after cleanUpUploadAdditionalApplicationData caseDataUpdated " + caseDataUpdated);
    }

    public Map<String, Object> prePopulateApplicants(CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        listItems.addAll(dynamicMultiSelectListService.getApplicantsMultiSelectList(caseData).get("applicants"));
        listItems.addAll(dynamicMultiSelectListService.getRespondentsMultiSelectList(caseData).get("respondents"));
        listItems.addAll(dynamicMultiSelectListService.getOtherPeopleMultiSelectList(caseData));
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put(ADDITIONAL_APPLICANTS_LIST, DynamicMultiSelectList.builder().listItems(listItems).build());
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        caseDataUpdated.put(ADDITIONAL_APPLICATION_FEES_TO_PAY, null);
        caseDataUpdated.put(ADDITIONAL_APPLICATIONS_HELP_WITH_FEES, null);
        return caseDataUpdated;
    }

    public SubmittedCallbackResponse uploadAdditionalApplicationSubmitted(CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        log.info("inside uploadAdditionalApplicationSubmitted caseData " + caseData);
        String confirmationHeader;
        String confirmationBody;
        if (isNotEmpty(caseData.getUploadAdditionalApplicationData())
            && caseData.getUploadAdditionalApplicationData().getAdditionalApplicationFeesToPay() != null) {
            if (Yes.equals(caseData.getUploadAdditionalApplicationData().getAdditionalApplicationsHelpWithFees())) {
                confirmationHeader = "# Help with fees requested";
                confirmationBody = "### What happens next \n\nThe court will review the document and will be in touch with you to let you"
                    + "know what happens next.";
            } else {
                confirmationHeader = "# Continue to payment";
                confirmationBody = "### What happens next \n\nThis application has been submitted, you will need to pay the application fee."
                    + " \n\nGo to the <a href=''>Service request</a> sections to make a payment. Once the fee has been paid the court will "
                    + "process the application";
            }
        } else {
            confirmationHeader = "# Application submitted";
            confirmationBody = "### What happens next \n\nThis application has been submitted, The court will process the application";
        }

        return SubmittedCallbackResponse.builder().confirmationHeader(
            confirmationHeader).confirmationBody(
            confirmationBody
        ).build();
    }
}
