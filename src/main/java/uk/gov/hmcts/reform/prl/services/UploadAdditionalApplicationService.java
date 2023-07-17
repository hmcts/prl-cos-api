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
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CA_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CA_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
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
    public static final String LEGAL_REPRESENATIVE_OF_APPLICANT = "Legal Represenative of Applicant ";
    public static final String LEGAL_REPRESENATIVE_OF_RESPONDENT = "Legal Represenative of Respondent ";
    public static final String TEMPORARY_C_2_DOCUMENT = "temporaryC2Document";
    private final IdamClient idamClient;
    private final ObjectMapper objectMapper;
    private final ApplicationsFeeCalculator applicationsFeeCalculator;
    private final PaymentRequestService paymentRequestService;
    private final FeeService feeService;
    public static final String ADDITIONAL_APPLICANTS_LIST = "additionalApplicantsList";
    private final DynamicMultiSelectListService dynamicMultiSelectListService;
    private final CcdDataStoreService userDataStoreService;
    private final SendAndReplyService sendAndReplyService;
    private final AuthTokenGenerator authTokenGenerator;

    public void getAdditionalApplicationElements(String authorisation, CaseData caseData,
                                                 List<Element<AdditionalApplicationsBundle>> additionalApplicationElements) {
        String author;
        UserDetails userDetails = idamClient.getUserDetails(authorisation);
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
        String author = null;
        if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue()) && StringUtils.isNotEmpty(
            uploadAdditionalApplicationData.getRepresentedPartyType())) {
            switch (uploadAdditionalApplicationData.getRepresentedPartyType()) {
                case CA_APPLICANT:
                case DA_APPLICANT:
                    author = LEGAL_REPRESENATIVE_OF_APPLICANT + partyName;
                    break;
                case CA_RESPONDENT:
                case DA_RESPONDENT:
                    author = LEGAL_REPRESENATIVE_OF_RESPONDENT + partyName;
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
                                                                         OtherApplicationsBundle otherApplicationsBundle,
                                                                         List<Element<ServedParties>> selectedParties) {
        FeeResponse feeResponse = null;
        PaymentServiceResponse paymentServiceResponse = null;
        Payment payment = null;
        String hwfReferenceNumber = null;
        List<FeeType> feeTypes = applicationsFeeCalculator.getFeeTypes(caseData);
        if (CollectionUtils.isNotEmpty(feeTypes)) {
            feeResponse = feeService.getFeesDataForAdditionalApplications(feeTypes);
            if (null != feeResponse && feeResponse.getAmount().compareTo(BigDecimal.ZERO) != 0) {
                String serviceReferenceResponsibleParty = getServiceReferenceResponsibleParty(
                    c2DocumentBundle,
                    otherApplicationsBundle
                );
                paymentServiceResponse = paymentRequestService.createServiceRequestForAdditionalApplications(
                    caseData,
                    authorisation,
                    feeResponse,
                    serviceReferenceResponsibleParty
                );
            }
            hwfReferenceNumber = YesOrNo.Yes.equals(caseData.getUploadAdditionalApplicationData().getAdditionalApplicationsHelpWithFees())
                ? caseData.getUploadAdditionalApplicationData().getAdditionalApplicationsHelpWithFeesNumber() : null;

            payment = Payment.builder()
                .fee(null != feeResponse ? PrlAppsConstants.CURRENCY_SIGN_POUND + feeResponse.getAmount() : null)
                .paymentServiceRequestReferenceNumber(null != paymentServiceResponse
                                                          ? paymentServiceResponse.getServiceRequestReference() : null)
                .hwfReferenceNumber(hwfReferenceNumber)
                .status(null != feeResponse ? (StringUtils.isNotEmpty(hwfReferenceNumber)
                    ? PaymentStatus.HWF.getDisplayedValue() : PaymentStatus.PENDING.getDisplayedValue())
                            : PaymentStatus.NOT_APPLICABLE.getDisplayedValue())
                .build();
        }
        setFlagsForHwfRequested(caseData, payment, hwfReferenceNumber);
        return AdditionalApplicationsBundle.builder().author(
                author)
            .selectedParties(selectedParties)
            .partyType(getPartyType(selectedParties, caseData))
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
            if (CollectionUtils.isNotEmpty(c2DocumentBundle.getCombinedReasonsForC2Application())) {
                reasonForApplications = c2DocumentBundle.getCombinedReasonsForC2Application().stream()
                    .map(CombinedC2AdditionalOrdersRequested::getDisplayedValue)
                    .collect(Collectors.toList());
            } else {
                reasonForApplications.add("C2 Application");
            }
        }
        if (isNotEmpty(otherApplicationsBundle) && isNotEmpty(otherApplicationsBundle.getApplicationType())) {
            applicantName = otherApplicationsBundle.getApplicantName();
            reasonForApplications.add(otherApplicationsBundle.getApplicationType().getDisplayedValue());
        }
        serviceReferenceResponsibleParty = serviceReferenceResponsibleParty.append(applicantName).append(
            HYPHEN_SEPARATOR);
        serviceReferenceResponsibleParty = serviceReferenceResponsibleParty.append(String.join(
            ",",
            reasonForApplications
        ));

        log.info("serviceReferenceResponsibleParty " + serviceReferenceResponsibleParty);
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
        log.info("HwfRequestedForAdditionalApplications " + caseData.getHwfRequestedForAdditionalApplications());
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
                selectedElements.stream().forEach(party ->
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
            selectedParties.stream().forEach(party -> partyNames.add(party.getValue().getPartyName()));
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
            applicationType = getOtherApplicationType(temporaryOtherApplicationsBundle);
            otherApplicationsBundle = OtherApplicationsBundle.builder()
                .author(author)
                .uploadedDateTime(currentDateTime)
                .applicantName(partyName)
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

    private C2DocumentBundle getC2DocumentBundle(CaseData caseData, String author, String currentDateTime, String partyName) {
        C2DocumentBundle c2DocumentBundle = null;
        if (caseData.getUploadAdditionalApplicationData().getTemporaryC2Document() != null) {
            C2DocumentBundle temporaryC2Document = caseData.getUploadAdditionalApplicationData().getTemporaryC2Document();
            c2DocumentBundle = C2DocumentBundle.builder()
                .author(author)
                .uploadedDateTime(currentDateTime)
                .applicantName(partyName)
                .document(temporaryC2Document.getDocument())
                .documentRelatedToCase(CollectionUtils.isNotEmpty(temporaryC2Document.getDocumentAcknowledge())
                                           ? Yes : No)
                .combinedReasonsForC2Application(getReasonsForApplication(temporaryC2Document))
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

        if (CollectionUtils.isNotEmpty(temporaryC2Document.getReasonsForC2Application())) {
            temporaryC2Document.getReasonsForC2Application().stream().forEach(reasonsForC2Application -> {
                combinedReasonsForC2Applications.add(CombinedC2AdditionalOrdersRequested.getValue(
                    reasonsForC2Application.name()));
            });
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
        caseDataUpdated.put(
            "hwfRequestedForAdditionalApplications",
            caseData.getHwfRequestedForAdditionalApplications()
        );
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
        caseDataUpdated.put(
            REPRESENTED_PARTY_TYPE,
            populateSolicitorRepresentingPartyType(authorisation, caseData)
        );
        log.info("prePopulateApplicants after caseDataUpdated " + caseDataUpdated);
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
                Optional<SolicitorRole> solicitorRole = SolicitorRole.fromCaseRoleLabel(caseUser.getCaseRole());
                if (solicitorRole.isPresent()) {
                    switch (solicitorRole.get().getRepresenting()) {
                        case CAAPPLICANT:
                            representedPartyType = CAAPPLICANT.name();
                            break;
                        case CARESPONDENT:
                            representedPartyType = CARESPONDENT.name();
                            break;
                        case DAAPPLICANT:
                            representedPartyType = DAAPPLICANT.name();
                            break;
                        case DARESPONDENT:
                            representedPartyType = DARESPONDENT.name();
                            break;
                        default:
                            break;
                    }
                } else if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                    representedPartyType = CAAPPLICANT.name();
                } else {
                    representedPartyType = DAAPPLICANT.name();
                }
            }
        }
        log.info("representedPartyType " + representedPartyType);
        return representedPartyType;
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
                confirmationBody = "### What happens next \n\nThe court will review the document and will be in touch to let you "
                    + "know what happens next.";
            } else {
                confirmationHeader = "# Continue to payment";
                confirmationBody = "### What happens next \n\nThis application has been submitted and you will now need to pay the application fee."
                    + " \n\nGo to the <a href='" + serviceRequestLink + "'>Service request</a> section to make a payment. "
                    + "Once the fee has been paid, the court will process the application.";
            }
        } else {
            confirmationHeader = "# Application submitted";
            confirmationBody = "You will get updates from the court about the progress of your application. "
                + "\n\n### What happens next \n\nThe court will review your documents and will be in touch to let you know what happens next.";
        }

        return SubmittedCallbackResponse.builder().confirmationHeader(
            confirmationHeader).confirmationBody(
            confirmationBody
        ).build();
    }

    public Map<String, Object> populateHearingList(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (caseData.getUploadAdditionalApplicationData().getAdditionalApplicationsApplyingFor().contains(
            AdditionalApplicationTypeEnum.c2Order)) {
            String s2sToken = authTokenGenerator.generate();
            DynamicList futureHearingList = sendAndReplyService.getFutureHearingDynamicList(
                authorisation,
                s2sToken,
                String.valueOf(caseData.getId())
            );
            log.info("hearingList ==> " + futureHearingList);
            C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder().hearingList(futureHearingList).build();
            caseDataUpdated.put(TEMPORARY_C_2_DOCUMENT, c2DocumentBundle);
        }
        return caseDataUpdated;
    }
}
