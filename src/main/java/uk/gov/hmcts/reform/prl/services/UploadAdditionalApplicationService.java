package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.ApplicationStatus;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2Consent;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.OtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.PaymentStatus;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.UploadAdditionalApplicationsFieldsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.caseaccess.CaseUser;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2ApplicationDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Payment;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Supplement;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.UploadApplicationDraftOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Urgency;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.services.caseaccess.CcdDataStoreService;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_REPRESENTING;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadAdditionalApplicationService {

    public static final String SOLICITOR_REPRESENTING_PARTY_TYPE = "solicitorRepresentingPartyType";
    public static final String LEGAL_REPRESENATIVE_OF_APPLICANT = "Legal Represenative of Applicant ";
    public static final String LEGAL_REPRESENATIVE_OF_RESPONDENT = "Legal Represenative of Respondent ";
    public static final String SOLICITOR_REPRESENTING_CAAPPLICANT = "SOLICITOR_REPRESENTING_CAAPPLICANT";
    public static final String SOLICITOR_REPRESENTING_DAAPPLICANT = "SOLICITOR_REPRESENTING_DAAPPLICANT";
    public static final String SOLICITOR_REPRESENTING_CARESPONDENT = "SOLICITOR_REPRESENTING_CARESPONDENT";
    public static final String SOLICITOR_REPRESENTING_DARESPONDENT = "SOLICITOR_REPRESENTING_DARESPONDENT";
    private final IdamClient idamClient;
    private final ObjectMapper objectMapper;
    private final ApplicationsFeeCalculator applicationsFeeCalculator;
    private final PaymentRequestService paymentRequestService;
    private final FeeService feeService;
    public static final String ADDITIONAL_APPLICANTS_LIST = "additionalApplicantsList";
    private final DynamicMultiSelectListService dynamicMultiSelectListService;
    private final CcdDataStoreService userDataStoreService;

    public void getAdditionalApplicationElements(String authorisation, CaseData caseData,
                                                        List<Element<AdditionalApplicationsBundle>> additionalApplicationElements) {
        String author = null;
        UserDetails userDetails = idamClient.getUserDetails(authorisation);
        if (caseData.getUploadAdditionalApplicationData() != null) {
            String applicantName = getSelectedApplicantName(caseData.getUploadAdditionalApplicationData().getAdditionalApplicantsList());
            author = getAuthor(caseData.getUploadAdditionalApplicationData(), userDetails, applicantName);
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

    private String getAuthor(UploadAdditionalApplicationData uploadAdditionalApplicationData, UserDetails userDetails, String applicantName) {
        String author = null;
        if (StringUtils.isNotEmpty(uploadAdditionalApplicationData.getSolicitorRepresentingPartyType())) {
            switch (uploadAdditionalApplicationData.getSolicitorRepresentingPartyType()) {
                case SOLICITOR_REPRESENTING_CAAPPLICANT:
                case SOLICITOR_REPRESENTING_DAAPPLICANT:
                    author = LEGAL_REPRESENATIVE_OF_APPLICANT + applicantName;
                    break;
                case SOLICITOR_REPRESENTING_CARESPONDENT:
                case SOLICITOR_REPRESENTING_DARESPONDENT:
                    author = LEGAL_REPRESENATIVE_OF_RESPONDENT + applicantName;
                    break;
                default:
                    break;
            }
        } else {
            author = userDetails.getFullName();
        }
        log.info("author " + author);
        return author;
    }

    private AdditionalApplicationsBundle getAdditionalApplicationsBundle(String authorisation, CaseData caseData, String author,
                                                                         String currentDateTime, C2DocumentBundle c2DocumentBundle,
                                                                         OtherApplicationsBundle otherApplicationsBundle) {
        FeeResponse feeResponse = null;
        PaymentServiceResponse paymentServiceResponse = null;
        Payment payment = null;
        String hwfReferenceNumber = null;
        boolean fl403ApplicationAlreadyPresent = applicationsFeeCalculator.isFl403ApplicationAlreadyPresent(caseData);
        List<FeeType> feeTypes = applicationsFeeCalculator.getFeeTypes(caseData.getUploadAdditionalApplicationData(), fl403ApplicationAlreadyPresent);
        if (CollectionUtils.isNotEmpty(feeTypes)) {
            feeResponse = feeService.getFeesDataForAdditionalApplications(feeTypes);
            if (null != feeResponse && feeResponse.getAmount().compareTo(BigDecimal.ZERO) != 0) {
                paymentServiceResponse = paymentRequestService.createServiceRequestForAdditionalApplications(
                    caseData,
                    authorisation,
                    feeResponse
                );
            }
            hwfReferenceNumber = YesOrNo.Yes.equals(caseData.getUploadAdditionalApplicationData().getAdditionalApplicationsHelpWithFees())
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
        setFlagsForHwfRequested(caseData, payment, hwfReferenceNumber);
        return AdditionalApplicationsBundle.builder().author(
                author)
            .uploadedDateTime(currentDateTime)
            .c2DocumentBundle(c2DocumentBundle)
            .otherApplicationsBundle(
                otherApplicationsBundle)
            .payment(payment)
            .applicationStatus(null != feeResponse && feeResponse.getAmount().compareTo(BigDecimal.ZERO) != 0
                                   ? ApplicationStatus.PENDING_ON_PAYMENT.getDisplayedValue()
                                   : ApplicationStatus.SUBMITTED.getDisplayedValue())
            .build();
    }

    private static void setFlagsForHwfRequested(CaseData caseData, Payment payment, String hwfReferenceNumber) {
        if (isNotEmpty(payment)) {
            if (StringUtils.isNotEmpty(hwfReferenceNumber)) {
                caseData.setHwfRequestedForAdditionalApplications(Yes);
            } else {
                caseData.setHwfRequestedForAdditionalApplications(No);
            }
        } else {
            caseData.setHwfRequestedForAdditionalApplications(null);
        }
        log.info("HwfRequestedForAdditionalApplications " + caseData.getHwfRequestedForAdditionalApplications());
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
        OtherApplicationType applicationType = null;

        if (caseData.getUploadAdditionalApplicationData().getTemporaryOtherApplicationsBundle() != null) {
            OtherApplicationsBundle temporaryOtherApplicationsBundle = caseData.getUploadAdditionalApplicationData()
                .getTemporaryOtherApplicationsBundle();
            applicationType = getOtherApplicationType(temporaryOtherApplicationsBundle);
            otherApplicationsBundle = OtherApplicationsBundle.builder()
                .author(author)
                .uploadedDateTime(currentDateTime)
                .applicantName(applicantName)
                .document(temporaryOtherApplicationsBundle.getDocument())
                .documentRelatedToCase(CollectionUtils.isNotEmpty(temporaryOtherApplicationsBundle.getDocumentAcknowledge())
                                           ? Yes : No)
                .urgency(null != temporaryOtherApplicationsBundle.getUrgencyTimeFrameType()
                             ? Urgency.builder().urgencyType(temporaryOtherApplicationsBundle.getUrgencyTimeFrameType()).build() : null)
                .supplementsBundle(createSupplementsBundle(
                    temporaryOtherApplicationsBundle.getSupplementsBundle(),
                    author
                ))
                .supportingEvidenceBundle(createSupportingEvidenceBundle(
                    temporaryOtherApplicationsBundle.getSupportingEvidenceBundle(),
                    author
                ))
                .applicationType(applicationType)
                .build();
        }
        return otherApplicationsBundle;
    }

    private static OtherApplicationType getOtherApplicationType(OtherApplicationsBundle temporaryOtherApplicationsBundle) {
        OtherApplicationType applicationType = null;
        if (null != temporaryOtherApplicationsBundle.getCaApplicantApplicationType()) {
            applicationType = OtherApplicationType.valueOf(temporaryOtherApplicationsBundle.getCaApplicantApplicationType().name());
        } else if (null != temporaryOtherApplicationsBundle.getCaRespondentApplicationType()) {
            applicationType = OtherApplicationType.valueOf(temporaryOtherApplicationsBundle.getCaRespondentApplicationType().name());
        } else if (null != temporaryOtherApplicationsBundle.getDaApplicantApplicationType()) {
            applicationType = OtherApplicationType.valueOf(temporaryOtherApplicationsBundle.getDaApplicantApplicationType().name());
        } else if (null != temporaryOtherApplicationsBundle.getDaRespondentApplicationType()) {
            applicationType = OtherApplicationType.valueOf(temporaryOtherApplicationsBundle.getDaRespondentApplicationType().name());
        }
        return applicationType;
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
                .documentRelatedToCase(CollectionUtils.isNotEmpty(temporaryC2Document.getDocumentAcknowledge())
                                           ? Yes : No)
                .reasonsForC2Application(
                    temporaryC2Document.getReasonsForC2Application())
                .parentalResponsibilityType(
                    temporaryC2Document.getParentalResponsibilityType())
                .hearingList(temporaryC2Document.getHearingList())
                .additionalDraftOrdersBundle(createAdditionalDraftOrdersBundle(temporaryC2Document.getAdditionalDraftOrdersBundle()))
                .supplementsBundle(createSupplementsBundle(temporaryC2Document.getSupplementsBundle(), author))
                .supportingEvidenceBundle(createSupportingEvidenceBundle(
                    temporaryC2Document.getSupportingEvidenceBundle(),
                    author
                ))
                .c2ApplicationDetails(C2ApplicationDetails.builder()
                                          .consent(C2ApplicationTypeEnum.applicationWithNotice.equals(
                                              caseData.getUploadAdditionalApplicationData().getTypeOfC2Application())
                                                       ? C2Consent.withoutConsent : C2Consent.withConsent)
                                          .build())
                .urgency(null != temporaryC2Document.getUrgencyTimeFrameType()
                         ? Urgency.builder().urgencyType(temporaryC2Document.getUrgencyTimeFrameType()).build() : null)
                .build();
        }
        return c2DocumentBundle;
    }

    private List<Element<Supplement>> createSupplementsBundle(List<Element<Supplement>> supplementsBundle, String author) {
        List<Element<Supplement>> supplementElementList = new ArrayList<>();
        if (supplementsBundle != null && !supplementsBundle.isEmpty()) {
            for (Element<Supplement> supplementElement : supplementsBundle) {
                Supplement supplement = Supplement.builder()
                    .dateTimeUploaded(LocalDateTime.now())
                    .document(supplementElement.getValue().getDocument())
                    .notes(supplementElement.getValue().getNotes())
                    .name(supplementElement.getValue().getName())
                    .secureAccommodationType(supplementElement.getValue().getSecureAccommodationType())
                    .documentRelatedToCase(CollectionUtils.isNotEmpty(supplementElement.getValue().getDocumentAcknowledge())
                                               ? Yes : No)
                    .uploadedBy(author)
                    .build();
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
                SupportingEvidenceBundle supportingEvidence = SupportingEvidenceBundle.builder()
                    .dateTimeUploaded(LocalDateTime.now())
                    .document(supportingEvidenceBundleElement.getValue().getDocument())
                    .notes(supportingEvidenceBundleElement.getValue().getNotes())
                    .name(supportingEvidenceBundleElement.getValue().getName())
                    .documentRelatedToCase(CollectionUtils.isNotEmpty(supportingEvidenceBundleElement.getValue().getDocumentAcknowledge())
                                               ? Yes : No)
                    .uploadedBy(author).build();
                supportingElementList.add(element(supportingEvidence));
            }
        }
        return supportingElementList;
    }

    private List<Element<UploadApplicationDraftOrder>> createAdditionalDraftOrdersBundle(
        List<Element<UploadApplicationDraftOrder>> additionalDraftOrdersBundle) {
        List<Element<UploadApplicationDraftOrder>> elementList = new ArrayList<>();
        if (additionalDraftOrdersBundle != null && !additionalDraftOrdersBundle.isEmpty()) {
            for (Element<UploadApplicationDraftOrder> draftOrderElement : additionalDraftOrdersBundle) {
                UploadApplicationDraftOrder uploadApplicationDraftOrder = UploadApplicationDraftOrder.builder()
                    .document(draftOrderElement.getValue().getDocument())
                    .title(draftOrderElement.getValue().getTitle())
                    .documentRelatedToCase(CollectionUtils.isNotEmpty(draftOrderElement.getValue().getDocumentAcknowledge())
                                               ? Yes : No)
                    .build();
                elementList.add(element(uploadApplicationDraftOrder));
            }
        }
        return elementList;
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
        log.info("createUploadAdditionalApplicationBundle HwfRequestedForAdditionalApplications "
                     + caseData.getHwfRequestedForAdditionalApplications());
        caseDataUpdated.put("hwfRequestedForAdditionalApplications", caseData.getHwfRequestedForAdditionalApplications());
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

    public Map<String, Object> prePopulateApplicants(CallbackRequest callbackRequest, String authorisation) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        listItems.addAll(dynamicMultiSelectListService.getApplicantsMultiSelectList(caseData).get("applicants"));
        listItems.addAll(dynamicMultiSelectListService.getRespondentsMultiSelectList(caseData).get("respondents"));
        listItems.addAll(dynamicMultiSelectListService.getOtherPeopleMultiSelectList(caseData));
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("prePopulateApplicants before caseDataUpdated " + caseDataUpdated);
        caseDataUpdated.put(ADDITIONAL_APPLICANTS_LIST, DynamicMultiSelectList.builder().listItems(listItems).build());
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        caseDataUpdated.put(SOLICITOR_REPRESENTING_PARTY_TYPE, populateSolicitorRepresentingPartyType(authorisation, caseData));
        log.info("prePopulateApplicants after caseDataUpdated " + caseDataUpdated);
        return caseDataUpdated;
    }

    private String populateSolicitorRepresentingPartyType(String authorisation, CaseData caseData) {
        UserDetails userDetails = idamClient.getUserDetails(authorisation);
        String representingPartyType = "";
        if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
            FindUserCaseRolesResponse findUserCaseRolesResponse
                = userDataStoreService.findUserCaseRoles(
                String.valueOf(caseData.getId()),
                authorisation
            );
            for (CaseUser caseUser : findUserCaseRolesResponse.getCaseUsers()) {
                Optional<SolicitorRole> solicitorRole = SolicitorRole.fromCaseRoleLabel(caseUser.getCaseRole());
                if (solicitorRole.isPresent()) {
                    switch (solicitorRole.get().getRepresenting()) {
                        case CAAPPLICANT:
                            representingPartyType = SOLICITOR_REPRESENTING + CAAPPLICANT.name();
                            break;
                        case CARESPONDENT:
                            representingPartyType = SOLICITOR_REPRESENTING + CARESPONDENT.name();
                            break;
                        case DAAPPLICANT:
                            representingPartyType = SOLICITOR_REPRESENTING + DAAPPLICANT.name();
                            break;
                        case DARESPONDENT:
                            representingPartyType = SOLICITOR_REPRESENTING + DARESPONDENT.name();
                            break;
                        default:
                            break;
                    }
                } else if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                    representingPartyType = SOLICITOR_REPRESENTING + CAAPPLICANT.name();
                } else {
                    representingPartyType = SOLICITOR_REPRESENTING + DAAPPLICANT.name();
                }
            }
        }
        log.info("representingPartyType " + representingPartyType);
        return representingPartyType;
    }

    public SubmittedCallbackResponse uploadAdditionalApplicationSubmitted(CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        log.info("inside uploadAdditionalApplicationSubmitted caseData " + caseData.getHwfRequestedForAdditionalApplications());
        String confirmationHeader;
        String confirmationBody;
        String serviceRequestLink = "/cases/case-details/" + caseData.getId() + "/#Service%20Request";
        if (isNotEmpty(caseData.getHwfRequestedForAdditionalApplications())) {
            if (Yes.equals(caseData.getHwfRequestedForAdditionalApplications())) {
                confirmationHeader = "# Help with fees requested";
                confirmationBody = "### What happens next \n\nThe court will review the document and will be in touch with you to let you "
                    + "know what happens next.";
            } else {
                confirmationHeader = "# Continue to payment";
                confirmationBody = "### What happens next \n\nThis application has been submitted, you will need to pay the application fee."
                    + " \n\nGo to the <a href='" + serviceRequestLink + "'>service request</a> sections to make a payment. "
                    + "Once the fee has been paid the court will process the application";
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
