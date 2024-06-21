package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.ApplicationStatus;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2Consent;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.CombinedC2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.OtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.PaymentStatus;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.UploadAdditionalApplicationsFieldsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.caseaccess.CaseUser;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
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
import uk.gov.hmcts.reform.prl.utils.UploadAdditionalApplicationUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_C2_APPLICATION_SNR_CODE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_OTHER_APPLICATION_SNR_CODE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_WA_TASK_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_WA_TASK_TO_BE_CREATED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CA_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CA_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMMA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.UNDERSCORE;
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

    public static final String REPRESENTED_PARTY_TYPE = "representedPartyType";
    public static final String LEGAL_REPRESENTATIVE_OF_APPLICANT = "Legal Representative of Applicant ";
    public static final String LEGAL_REPRESENTATIVE_OF_RESPONDENT = "Legal Representative of Respondent ";
    public static final String TEMPORARY_C_2_DOCUMENT = "temporaryC2Document";
    public static final String APPLICANT_CASE_NAME = "applicantCaseName";
    public static final String ADDITIONAL_APPLICANTS_LIST = "additionalApplicantsList";
    public static final String APPLICANTSOLICITOR = "[APPLICANTSOLICITOR]";

    private final IdamClient idamClient;
    private final ObjectMapper objectMapper;
    private final ApplicationsFeeCalculator applicationsFeeCalculator;
    private final PaymentRequestService paymentRequestService;
    private final FeeService feeService;
    private final DynamicMultiSelectListService dynamicMultiSelectListService;
    private final CcdDataStoreService userDataStoreService;
    private final SendAndReplyService sendAndReplyService;
    private final AuthTokenGenerator authTokenGenerator;
    private final UploadAdditionalApplicationUtils uploadAdditionalApplicationUtils;

    public void getAdditionalApplicationElements(String authorisation, String userAuthorisation, CaseData caseData,
                                                 List<Element<AdditionalApplicationsBundle>> additionalApplicationElements) {
        String author;
        UserDetails userDetails = idamClient.getUserDetails(userAuthorisation);
        if (caseData.getUploadAdditionalApplicationData() != null) {
            List<Element<ServedParties>> selectedParties = getSelectedParties(caseData);
            String partyName = getSelectedPartyName(selectedParties);
            author = getAuthor(caseData.getUploadAdditionalApplicationData(), userDetails, partyName);
            String currentDateTime = LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)).format(DateTimeFormatter.ofPattern(
                "dd-MMM-yyyy HH:mm:ss a",
                Locale.UK
            ));

            C2DocumentBundle c2DocumentBundle = getC2DocumentBundle(caseData, author, currentDateTime, partyName);
            OtherApplicationsBundle otherApplicationsBundle = getOtherApplicationsBundle(caseData,
                                                                                         author,
                                                                                         currentDateTime, partyName
            );

            AdditionalApplicationsBundle additionalApplicationsBundle = getAdditionalApplicationsBundle(
                authorisation,
                caseData,
                author,
                currentDateTime,
                c2DocumentBundle,
                otherApplicationsBundle,
                selectedParties
            );

            additionalApplicationElements.add(element(additionalApplicationsBundle));
        }
    }

    private String getAuthor(UploadAdditionalApplicationData uploadAdditionalApplicationData, UserDetails userDetails, String partyName) {
        String author;
        if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue()) && StringUtils.isNotEmpty(
            uploadAdditionalApplicationData.getRepresentedPartyType())) {
            switch (uploadAdditionalApplicationData.getRepresentedPartyType()) {
                case CA_APPLICANT, DA_APPLICANT -> author = LEGAL_REPRESENTATIVE_OF_APPLICANT + partyName;
                case CA_RESPONDENT, DA_RESPONDENT -> author = LEGAL_REPRESENTATIVE_OF_RESPONDENT + partyName;
                default -> author = userDetails.getFullName();
            }
        } else {
            author = userDetails.getFullName();
        }
        return author;
    }

    private AdditionalApplicationsBundle getAdditionalApplicationsBundle(String authorisation, CaseData caseData, String author,
                                                                         String currentDateTime, C2DocumentBundle c2DocumentBundle,
                                                                         OtherApplicationsBundle otherApplicationsBundle,
                                                                         List<Element<ServedParties>> selectedParties) {
        FeeResponse feeResponse = null;
        Optional<PaymentServiceResponse> paymentServiceResponse;
        Payment payment = null;
        String hwfReferenceNumber = null;
        List<FeeType> feeTypes = applicationsFeeCalculator.getFeeTypes(caseData);
        if (CollectionUtils.isNotEmpty(feeTypes)) {
            feeResponse = feeService.getFeesDataForAdditionalApplications(feeTypes);
            paymentServiceResponse = getPaymentServiceResponse(
                authorisation,
                caseData,
                c2DocumentBundle,
                otherApplicationsBundle,
                feeResponse
            );
            hwfReferenceNumber = YesOrNo.Yes.equals(caseData.getUploadAdditionalApplicationData().getAdditionalApplicationsHelpWithFees())
                ? caseData.getUploadAdditionalApplicationData().getAdditionalApplicationsHelpWithFeesNumber() : null;
            String checkHwfStatus = StringUtils.isNotEmpty(hwfReferenceNumber)
                ? PaymentStatus.HWF.getDisplayedValue() : PaymentStatus.PENDING.getDisplayedValue();
            String serviceRequestReference = paymentServiceResponse.map(PaymentServiceResponse::getServiceRequestReference).orElse(null);
            payment = Payment.builder()
                .fee(null != feeResponse ? PrlAppsConstants.CURRENCY_SIGN_POUND + feeResponse.getAmount() : null)
                .paymentServiceRequestReferenceNumber(serviceRequestReference)
                .hwfReferenceNumber(hwfReferenceNumber)
                .status(null != feeResponse ? checkHwfStatus
                            : PaymentStatus.NOT_APPLICABLE.getDisplayedValue())
                .build();
        }
        setFlagsForHwfRequested(caseData, payment, hwfReferenceNumber);
        String applicationStatus = null != feeResponse && feeResponse.getAmount().compareTo(BigDecimal.ZERO) != 0
            ? ApplicationStatus.PENDING_ON_PAYMENT.getDisplayedValue()
            : ApplicationStatus.SUBMITTED.getDisplayedValue();
        return AdditionalApplicationsBundle.builder().author(
                author)
            .selectedParties(selectedParties)
            .partyType(getPartyType(selectedParties, caseData))
            .uploadedDateTime(currentDateTime)
            .c2DocumentBundle(null != c2DocumentBundle
                                  ? c2DocumentBundle.toBuilder().applicationStatus(applicationStatus).build() : null)
            .otherApplicationsBundle(null != otherApplicationsBundle
                                         ? otherApplicationsBundle.toBuilder().applicationStatus(applicationStatus).build()
                                         : null)
            .payment(payment)
            .build();
    }

    private Optional<PaymentServiceResponse> getPaymentServiceResponse(String authorisation, CaseData caseData, C2DocumentBundle c2DocumentBundle,
                                                                       OtherApplicationsBundle otherApplicationsBundle,
                                                                       FeeResponse feeResponse) {
        Optional<PaymentServiceResponse> paymentServiceResponse = Optional.empty();
        if (null != feeResponse && feeResponse.getAmount().compareTo(BigDecimal.ZERO) != 0) {
            String serviceReferenceResponsibleParty = getServiceReferenceResponsibleParty(
                c2DocumentBundle,
                otherApplicationsBundle
            );
            paymentServiceResponse = Optional.of(paymentRequestService.createServiceRequestForAdditionalApplications(
                caseData,
                authorisation,
                feeResponse,
                serviceReferenceResponsibleParty
            ));
        }
        return paymentServiceResponse;
    }

    private PartyEnum getPartyType(List<Element<ServedParties>> selectedParties, CaseData caseData
    ) {
        for (Element<ServedParties> partyElement : selectedParties) {
            if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                return getPartyTypeForC100(caseData, partyElement);
            } else {
                return getPartyTypeForFl401(caseData, partyElement);
            }
        }
        return null;
    }

    private static PartyEnum getPartyTypeForC100(CaseData caseData, Element<ServedParties> partyElement) {
        if (CollectionUtils.isNotEmpty(caseData.getApplicants())) {
            for (Element<PartyDetails> applicant : caseData.getApplicants()) {
                if (applicant.getId().toString().equals(partyElement.getValue().getPartyId())) {
                    return PartyEnum.applicant;
                }
            }
        }
        if (CollectionUtils.isNotEmpty(caseData.getRespondents())) {
            for (Element<PartyDetails> respondent : caseData.getRespondents()) {
                if (respondent.getId().toString().equals(partyElement.getValue().getPartyId())) {
                    return PartyEnum.respondent;
                }
            }
        }
        return isOtherParty(caseData, partyElement);
    }

    private static PartyEnum isOtherParty(CaseData caseData, Element<ServedParties> partyElement) {
        if (CollectionUtils.isNotEmpty(caseData.getOthersToNotify())) {
            for (Element<PartyDetails> otherParties : caseData.getOthersToNotify()) {
                if (otherParties.getId().toString().equals(partyElement.getValue().getPartyId())) {
                    return PartyEnum.other;
                }
            }
        }
        return null;
    }

    private static PartyEnum getPartyTypeForFl401(CaseData caseData, Element<ServedParties> partyElement) {
        if (isNotEmpty(caseData.getApplicantsFL401())
            && isNotEmpty(caseData.getRespondentsFL401())
            && isNotEmpty(partyElement.getValue())) {
            if (isNotEmpty(caseData.getApplicantsFL401().getPartyId())
                && partyElement.getValue().getPartyId().equals(caseData.getApplicantsFL401().getPartyId().toString())) {
                return PartyEnum.applicant;
            } else {
                return PartyEnum.respondent;
            }
        }
        return null;
    }

    private String getServiceReferenceResponsibleParty(C2DocumentBundle c2DocumentBundle, OtherApplicationsBundle otherApplicationsBundle) {
        StringBuilder serviceReferenceResponsibleParty = new StringBuilder();
        String applicantName = null;
        List<String> reasonForApplications = new ArrayList<>();
        if (isNotEmpty(c2DocumentBundle)) {
            applicantName = c2DocumentBundle.getApplicantName();
            reasonForApplications.add("C2 Application");
        }
        if (isNotEmpty(otherApplicationsBundle) && isNotEmpty(otherApplicationsBundle.getApplicationType())) {
            applicantName = otherApplicationsBundle.getApplicantName();
            reasonForApplications.add(otherApplicationsBundle.getApplicationType().getDisplayedValue());
        }
        serviceReferenceResponsibleParty = serviceReferenceResponsibleParty
            .append(applicantName)
            .append(HYPHEN_SEPARATOR)
            .append(String.join(COMMA,reasonForApplications));

        return serviceReferenceResponsibleParty.toString();
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
    }

    private List<Element<ServedParties>> getSelectedParties(CaseData caseData) {
        DynamicMultiSelectList applicantsList = caseData.getUploadAdditionalApplicationData()
            .getAdditionalApplicantsList();
        List<Element<ServedParties>> selectedParties = new ArrayList<>();
        if (Objects.nonNull(applicantsList)) {
            List<DynamicMultiselectListElement> selectedElements = applicantsList.getValue();
            if (isNotEmpty(selectedElements)) {
                boolean isDaCase = PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(
                    caseData));
                selectedElements.forEach(party ->
                                                      selectedParties.add(element(ServedParties.builder().partyId(
                                                              isDaCase ? getPartyIdForDaCase(
                                                                  party.getCode(),
                                                                  caseData
                                                              ) : party.getCode())
                                                                                      .partyName(party.getLabel()).build()))
                );
            }
        }
        return selectedParties;
    }

    private String getPartyIdForDaCase(String code, CaseData caseData) {
        String applicantName = caseData.getApplicantsFL401().getFirstName() + " "
            + caseData.getApplicantsFL401().getLastName();
        if (code.contains(applicantName)) {
            return caseData.getApplicantsFL401().getPartyId().toString();
        } else {
            return caseData.getRespondentsFL401().getPartyId().toString();
        }
    }

    private String getSelectedPartyName(List<Element<ServedParties>> selectedParties) {
        String partyName = "";
        List<String> partyNames = new ArrayList<>();
        if (Objects.nonNull(selectedParties)) {
            selectedParties.forEach(party -> partyNames.add(party.getValue().getPartyName()));
            partyName = String.join(",", partyNames);

        }
        return partyName;
    }

    private OtherApplicationsBundle getOtherApplicationsBundle(CaseData caseData, String author,
                                                               String currentDateTime, String partyName) {
        OtherApplicationsBundle otherApplicationsBundle = null;
        OtherApplicationType applicationType;

        if (caseData.getUploadAdditionalApplicationData().getTemporaryOtherApplicationsBundle() != null) {
            OtherApplicationsBundle temporaryOtherApplicationsBundle = caseData.getUploadAdditionalApplicationData()
                .getTemporaryOtherApplicationsBundle();
            applicationType = uploadAdditionalApplicationUtils.getOtherApplicationType(temporaryOtherApplicationsBundle);
            otherApplicationsBundle = OtherApplicationsBundle.builder()
                .author(author)
                .uploadedDateTime(currentDateTime)
                .applicantName(partyName)
                .finalDocument(List.of(element(temporaryOtherApplicationsBundle.getDocument())))
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

    private C2DocumentBundle getC2DocumentBundle(CaseData caseData, String author, String currentDateTime, String partyName) {
        C2DocumentBundle c2DocumentBundle = null;
        if (caseData.getUploadAdditionalApplicationData().getTemporaryC2Document() != null) {
            C2DocumentBundle temporaryC2Document = caseData.getUploadAdditionalApplicationData().getTemporaryC2Document();
            c2DocumentBundle = C2DocumentBundle.builder()
                .author(author)
                .uploadedDateTime(currentDateTime)
                .applicantName(partyName)
                .finalDocument(List.of(element(temporaryC2Document.getDocument())))
                .documentRelatedToCase(CollectionUtils.isNotEmpty(temporaryC2Document.getDocumentAcknowledge())
                                           ? Yes : No)
                .combinedReasonsForC2Application(getReasonsForApplication(temporaryC2Document))
                .otherReasonsFoC2Application(StringUtils.isNotEmpty(temporaryC2Document.getOtherReasonsFoC2Application())
                                                 ? temporaryC2Document.getOtherReasonsFoC2Application() : null)
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
                .requestedHearingToAdjourn(null != temporaryC2Document.getHearingList() && null != temporaryC2Document.getHearingList().getValue()
                                               ? temporaryC2Document.getHearingList().getValue().getLabel() : null)
                .build();
        }
        return c2DocumentBundle;
    }

    private List<CombinedC2AdditionalOrdersRequested> getReasonsForApplication(C2DocumentBundle temporaryC2Document) {
        List<CombinedC2AdditionalOrdersRequested> combinedReasonsForC2Applications = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(temporaryC2Document.getCaReasonsForC2Application())) {
            temporaryC2Document.getCaReasonsForC2Application().forEach(reasonsForC2Application -> combinedReasonsForC2Applications
                .add(CombinedC2AdditionalOrdersRequested.getValue(reasonsForC2Application.name())));
        } else if (CollectionUtils.isNotEmpty(temporaryC2Document.getDaReasonsForC2Application())) {
            temporaryC2Document.getDaReasonsForC2Application().forEach(reasonsForC2Application -> combinedReasonsForC2Applications
                .add(CombinedC2AdditionalOrdersRequested.getValue(reasonsForC2Application.name())));
        } else if (CollectionUtils.isNotEmpty(temporaryC2Document.getCombinedReasonsForC2Application())) {
            combinedReasonsForC2Applications.addAll(temporaryC2Document.getCombinedReasonsForC2Application());
        }
        return combinedReasonsForC2Applications;
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

    public Map<String, Object> calculateAdditionalApplicationsFee(String authorisation, CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        UploadAdditionalApplicationData uploadAdditionalApplicationData = caseData.getUploadAdditionalApplicationData();
        if (isNotEmpty(uploadAdditionalApplicationData)
                && StringUtils.isEmpty(uploadAdditionalApplicationData.getRepresentedPartyType())) {
            caseData.setUploadAdditionalApplicationData(uploadAdditionalApplicationData.toBuilder().representedPartyType(
                populateSolicitorRepresentingPartyType(authorisation, caseData)).build());
        }
        caseDataUpdated.putAll(applicationsFeeCalculator.calculateAdditionalApplicationsFee(caseData));
        return caseDataUpdated;
    }



    public Map<String, Object> createUploadAdditionalApplicationBundle(String systemAuthorisation,
                                                                       String userAuthorisation,
                                                                       CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        List<Element<AdditionalApplicationsBundle>> additionalApplicationElements = new ArrayList<>();
        if (caseData.getAdditionalApplicationsBundle() != null && !caseData.getAdditionalApplicationsBundle().isEmpty()) {
            additionalApplicationElements = caseData.getAdditionalApplicationsBundle();
        }
        getAdditionalApplicationElements(
            systemAuthorisation,
            userAuthorisation,
            caseData,
            additionalApplicationElements
        );
        additionalApplicationElements.sort(Comparator.comparing(
            m -> m.getValue().getUploadedDateTime(),
            Comparator.reverseOrder()
        ));
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("additionalApplicationsBundle", additionalApplicationElements);
        caseDataUpdated.put(
            "hwfRequestedForAdditionalApplications",
            caseData.getHwfRequestedForAdditionalApplications()
        );

        caseDataUpdated.put(AWP_WA_TASK_NAME, uploadAdditionalApplicationUtils.getAwPTaskName(caseData));
        caseDataUpdated.put(AWP_WA_TASK_TO_BE_CREATED, uploadAdditionalApplicationUtils.getValueOfAwpTaskToBeCreated(caseData));

        cleanOldUpUploadAdditionalApplicationData(caseDataUpdated);
        return caseDataUpdated;
    }

    private void cleanOldUpUploadAdditionalApplicationData(Map<String, Object> caseDataUpdated) {
        for (UploadAdditionalApplicationsFieldsEnum field : UploadAdditionalApplicationsFieldsEnum.values()) {
            caseDataUpdated.remove(field.getValue());
        }
    }

    public Map<String, Object> prePopulateApplicants(CallbackRequest callbackRequest, String authorisation) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        listItems.addAll(dynamicMultiSelectListService.getApplicantsMultiSelectList(caseData).get("applicants"));
        listItems.addAll(dynamicMultiSelectListService.getRespondentsMultiSelectList(caseData).get("respondents"));
        listItems.addAll(dynamicMultiSelectListService.getOtherPeopleMultiSelectList(caseData));
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put(ADDITIONAL_APPLICANTS_LIST, DynamicMultiSelectList.builder().listItems(listItems).build());
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        caseDataUpdated.put(
            REPRESENTED_PARTY_TYPE,
            populateSolicitorRepresentingPartyType(authorisation, caseData)
        );
        caseDataUpdated.put(APPLICANT_CASE_NAME, caseData.getApplicantCaseName());
        return caseDataUpdated;
    }

    private String populateSolicitorRepresentingPartyType(String authorisation, CaseData caseData) {
        UserDetails userDetails = idamClient.getUserDetails(authorisation);
        String representedPartyType = "";
        if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
            FindUserCaseRolesResponse findUserCaseRolesResponse
                = userDataStoreService.findUserCaseRoles(
                String.valueOf(caseData.getId()),
                authorisation
            );
            for (CaseUser caseUser : findUserCaseRolesResponse.getCaseUsers()) {
                if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                    && APPLICANTSOLICITOR.equalsIgnoreCase(caseUser.getCaseRole())) {
                    representedPartyType = CAAPPLICANT.name();
                } else {
                    representedPartyType = findSolicitorRepresentedPartyType(caseUser, representedPartyType);
                }
            }
        }
        return representedPartyType;
    }

    private static String findSolicitorRepresentedPartyType(CaseUser caseUser, String representedPartyType) {
        Optional<SolicitorRole> solicitorRole = SolicitorRole.fromCaseRoleLabel(caseUser.getCaseRole());
        if (solicitorRole.isPresent()) {
            switch (solicitorRole.get().getRepresenting()) {
                case CAAPPLICANT -> representedPartyType = CAAPPLICANT.name();
                case CARESPONDENT -> representedPartyType = CARESPONDENT.name();
                case DAAPPLICANT -> representedPartyType = DAAPPLICANT.name();
                case DARESPONDENT -> representedPartyType = DARESPONDENT.name();
                default -> log.info("No matching solicitor role found");
            }
        }
        return representedPartyType;
    }

    public SubmittedCallbackResponse uploadAdditionalApplicationSubmitted(CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        String confirmationHeader;
        String confirmationBody;
        String serviceRequestLink = "/cases/case-details/" + caseData.getId() + "/#Service%20Request";
        if (isNotEmpty(caseData.getHwfRequestedForAdditionalApplications())) {
            if (Yes.equals(caseData.getHwfRequestedForAdditionalApplications())) {
                confirmationHeader = "# Help with fees requested";
                confirmationBody = """
                    ### What happens next

                    The court will review the document and will be in touch to let you
                    know what happens next.
                    """;
            } else {
                confirmationHeader = "# Continue to payment";
                confirmationBody = "### What happens next \n\nThis application has been submitted and you will now need to pay the application fee."
                    + " \n\nGo to the <a href='" + serviceRequestLink + "'>Service request</a> section to make a payment. "
                    + "Once the fee has been paid, the court will process the application.";
            }
        } else {
            confirmationHeader = "# Application submitted";
            confirmationBody = """
                You will get updates from the court about the progress of your application.


                ### What happens next

                The court will review your documents and will be in touch to let you know what happens next.
                """;
        }

        return SubmittedCallbackResponse.builder().confirmationHeader(
            confirmationHeader).confirmationBody(
            confirmationBody
        ).build();
    }

    public Map<String, Object> populateHearingList(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("applying for {}",caseData.getUploadAdditionalApplicationData().getAdditionalApplicationsApplyingFor());
        if (AdditionalApplicationTypeEnum.c2Order.equals(
            caseData.getUploadAdditionalApplicationData().getAdditionalApplicationsApplyingFor())) {
            String s2sToken = authTokenGenerator.generate();
            DynamicList futureHearingList = sendAndReplyService.getFutureHearingDynamicList(
                authorisation,
                s2sToken,
                String.valueOf(caseData.getId())
            );
            C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder().hearingList(futureHearingList).build();
            caseDataUpdated.put(TEMPORARY_C_2_DOCUMENT, c2DocumentBundle);
        }
        return caseDataUpdated;
    }

    public List<Element<AdditionalApplicationsBundle>> updateAwpApplicationStatus(
        String awpApplicationSelectedFromSnR,
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle,
        String applicationStatus) {
        String[] awpSelectedApplicationDetails = awpApplicationSelectedFromSnR.split(UNDERSCORE);

        if (awpSelectedApplicationDetails[0].equals(AWP_OTHER_APPLICATION_SNR_CODE)) {
            additionalApplicationsBundle.stream()
                .filter(
                    t -> t.getValue().getOtherApplicationsBundle() != null
                        && t.getValue().getOtherApplicationsBundle().getUploadedDateTime().equals(
                        awpSelectedApplicationDetails[1])
                )
                .map(Element::getValue)
                .forEach(additionalApplicationsBundle1 ->
                    additionalApplicationsBundle1.setOtherApplicationsBundle(additionalApplicationsBundle1.getOtherApplicationsBundle()
                                                                          .toBuilder()
                                                                          .applicationStatus(applicationStatus)
                                                                          .build())
                );
        } else if (awpSelectedApplicationDetails[0].equals(AWP_C2_APPLICATION_SNR_CODE)) {
            additionalApplicationsBundle.stream()
                .filter(
                    t -> t.getValue().getC2DocumentBundle() != null
                        && t.getValue().getC2DocumentBundle().getUploadedDateTime().equals(awpSelectedApplicationDetails[1])
                )
                .map(Element::getValue)
                .forEach(additionalApplicationsBundle1 ->
                    additionalApplicationsBundle1.setC2DocumentBundle(additionalApplicationsBundle1.getC2DocumentBundle()
                                                                          .toBuilder()
                                                                          .applicationStatus(applicationStatus)
                                                                          .build())
                );
        }
        return additionalApplicationsBundle;
    }
}
