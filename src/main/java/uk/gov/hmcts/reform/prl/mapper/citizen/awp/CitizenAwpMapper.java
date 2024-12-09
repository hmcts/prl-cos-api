package uk.gov.hmcts.reform.prl.mapper.citizen.awp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.ApplicationStatus;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2Consent;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.CombinedC2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.OtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.PaymentStatus;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.citizen.awp.CitizenAwpRequest;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2ApplicationDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Payment;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Urgency;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.CitizenAwpPayment;
import uk.gov.hmcts.reform.prl.models.dto.payment.CreatePaymentRequest;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.logging.log4j.util.Strings.concat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getCitizenAwpPaymentIfPresent;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@Slf4j
@Component
public class CitizenAwpMapper {
    private static final String DATE_FORMAT = "dd-MMM-yyyy hh:mm:ss a";

    public CaseData map(CaseData caseData,
                        CitizenAwpRequest citizenAwpRequest) {
        log.info("Mapping AWP citizen to solicitor");


        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .author(citizenAwpRequest.getPartyName())
            .uploadedDateTime(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE))
                                  .format(DateTimeFormatter.ofPattern(DATE_FORMAT)))
            .partyType(PartyEnum.valueOf(citizenAwpRequest.getPartyType()))
            .selectedParties(getSelectedParties(citizenAwpRequest))
            .c2DocumentBundle(getC2ApplicationBundle(citizenAwpRequest))
            .otherApplicationsBundle(getOtherApplicationBundle(citizenAwpRequest))
            .build();

        log.info("Mapped data before adding payment details {}", additionalApplicationsBundle);
        //Map citizen awp payment details & then remove from in progress
        additionalApplicationsBundle = mapPaymentDetailsAndRemove(
            caseData,
            citizenAwpRequest,
            additionalApplicationsBundle
        );
        log.info("Mapped data with payment details {}", additionalApplicationsBundle);

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundles =
            isNotEmpty(caseData.getAdditionalApplicationsBundle())
                ? caseData.getAdditionalApplicationsBundle() : new ArrayList<>();
        additionalApplicationsBundles.add(element(additionalApplicationsBundle));

        return caseData.toBuilder()
            .hwfRequestedForAdditionalApplicationsFlag(setC100HwfRequestedForAdditionalApplicationsFlag(
                caseData,
                citizenAwpRequest
            ))
            .additionalApplicationsBundle(additionalApplicationsBundles)
            .awpWaTaskName(getAwpTaskName(additionalApplicationsBundle, false))
            //PRL-4024 - awp hwf ref no.
            .awpHwfRefNo(citizenAwpRequest.getHwfReferenceNumber())
            .build();
    }

    public static String getAwpTaskName(AdditionalApplicationsBundle additionalApplicationsBundle, boolean isHwfPayment) {
        //PRL-4023 - postfix to awp task name
        if (null != additionalApplicationsBundle.getC2DocumentBundle()) {
            return "C2";
        } else if (!isHwfPayment && null != additionalApplicationsBundle.getOtherApplicationsBundle()) {
            switch (additionalApplicationsBundle.getOtherApplicationsBundle().getApplicationType()) {
                case C1_REQUEST_GRANT_FOR_PARENTAL_RESPONSIBILITY, C1_REQUEST_APPOINT_A_GUARDIAN_FOR_CHILD:
                    return "C1-Apply for certain orders under the Children Act";
                case C3_ORDER_AUTHORISING_SEARCH_FOR_TAKING_CHARGE_OF_AND_DELIVERY_OF_A_CHILD:
                    return "C3 - Application for an order authorizing search and taking charge of a child";
                case C4_ASK_COURT_TO_ORDER_SOMEONE_TO_PROVIDE_CHILD_INFORMATION:
                    return "C4 - Application for an order for disclosure of a child’s whereabouts";
                case C79_ENFORCE_A_CHILD_ARRANGEMENTS_ORDER:
                    return "C79 - Application to enforce a child arrangements order";
                case D89_ASK_TO_DELIVER_PAPER_TO_OTHER_PARTY:
                    return "D89 - Request for personal service by a court bailiff";
                case EX740_PREVENT_QUESTIONING_IN_PERSON_ACCUSING_SOMEONE:
                    return "EX740 - Application to prohibit cross examination (victim)";
                case EX741_PREVENT_QUESTIONING_IN_PERSON_SOMEONE_ACCUSING_YOU:
                    return "EX741 - Application to prohibit cross examination (perpetrator)";
                case FP25_REQUEST_TO_ORDER_A_WITNESS_TO_ATTEND_COURT:
                    return "FP25 - Witness summons";
                case FC600_REQUEST_COURT_TO_ACT_WHEN_SOMEONE_IN_THE_CASE_IS_DISOBEYING_COURT_ORDER:
                    return "FC600 - Committal application";
                case N161_APPEAL_A_ORDER_OR_ASK_PERMISSION_TO_APPEAL:
                    return "N161 - Appellant’s notice";
                case FL403_CHANGE_EXTEND_OR_CANCEL_NON_MOLESTATION_ORDER_OR_OCCUPATION_ORDER:
                    return "FL403 - Application to vary, discharge or extend an order";
                case FL407_REQUEST_THE_COURT_ISSUES_AN_ARREST_WARRANT:
                    return "FL407 - Application for a warrant of arrest";
                default:
                    return additionalApplicationsBundle.getOtherApplicationsBundle().getApplicationType().getDisplayedValue();
            }
        }
        return null;
    }

    private YesOrNo setC100HwfRequestedForAdditionalApplicationsFlag(CaseData caseData, CitizenAwpRequest citizenAwpRequest) {
        if (YesOrNo.Yes.equals(caseData.getHwfRequestedForAdditionalApplicationsFlag())) {
            return caseData.getHwfRequestedForAdditionalApplicationsFlag();
        } else if (YesOrNo.Yes.equals(citizenAwpRequest.getHaveHwfReference())
            && StringUtils.isNotEmpty(citizenAwpRequest.getHwfReferenceNumber())) {
            return YesOrNo.Yes;
        } else {
            return YesOrNo.No;
        }
    }

    private AdditionalApplicationsBundle mapPaymentDetailsAndRemove(CaseData caseData,
                                                                    CitizenAwpRequest citizenAwpRequest,
                                                                    AdditionalApplicationsBundle additionalApplicationsBundle) {
        Optional<Element<CitizenAwpPayment>> optionalCitizenAwpPaymentElement =
            getCitizenAwpPaymentIfPresent(
                caseData.getCitizenAwpPayments(),
                getPaymentRequestToCompare(citizenAwpRequest)
            );
        //update payment details
        if (optionalCitizenAwpPaymentElement.isPresent()) {
            additionalApplicationsBundle = additionalApplicationsBundle.toBuilder()
                .payment(getPaymentDetails(
                    citizenAwpRequest,
                    optionalCitizenAwpPaymentElement.get().getValue()
                ))
                .build();
            //Remove in progress citizen awp payment details
            caseData.getCitizenAwpPayments().remove(optionalCitizenAwpPaymentElement.get());
        }

        return additionalApplicationsBundle;
    }

    private C2DocumentBundle getC2ApplicationBundle(CitizenAwpRequest citizenAwpRequest) {
        if ("C2".equals(citizenAwpRequest.getAwpType())) {

            log.info("Inside mapping citizen awp C2");
            return C2DocumentBundle.builder()
                .applicantName(citizenAwpRequest.getPartyName())
                .author(citizenAwpRequest.getPartyName())
                .uploadedDateTime(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE))
                                      .format(DateTimeFormatter.ofPattern(DATE_FORMAT)))
                .documentRelatedToCase(YesOrNo.Yes)
                .finalDocument(getDocuments(citizenAwpRequest.getUploadedApplicationForms()))
                .supportingEvidenceBundle(YesOrNo.Yes.equals(citizenAwpRequest.getHasSupportingDocuments())
                                              ? getSupportingBundles(citizenAwpRequest) : null)
                .combinedReasonsForC2Application(Arrays.asList(CombinedC2AdditionalOrdersRequested
                                                                   .getValue(getApplicationKey(citizenAwpRequest)))) //REVISIT
                //.otherReasonsFoC2Application(null) //REVISIT - NOT NEEDED FOR CITIZEN AS THERE IS OTHER OPTION
                .urgency(YesOrNo.Yes.equals(citizenAwpRequest.getUrgencyInFiveDays())
                             ? getUrgency(citizenAwpRequest) : null)
                .c2ApplicationDetails(getC2ApplicationDetails(citizenAwpRequest))
                .applicationStatus(ApplicationStatus.SUBMITTED.getDisplayedValue())
                .requestedHearingToAdjourn(citizenAwpRequest.getHearingToDelayCancel())
                .applicationStatus(getApplicationStatus(citizenAwpRequest))
                .build();
        }
        return null;
    }

    private String getApplicationStatus(CitizenAwpRequest citizenAwpRequest) {
        return YesOrNo.Yes.equals(citizenAwpRequest.getHaveHwfReference())
            && StringUtils.isNotEmpty(citizenAwpRequest.getHwfReferenceNumber())
            ? ApplicationStatus.PENDING_ON_PAYMENT.getDisplayedValue()
            : ApplicationStatus.SUBMITTED.getDisplayedValue();
    }

    private OtherApplicationsBundle getOtherApplicationBundle(CitizenAwpRequest citizenAwpRequest) {
        if (!"C2".equals(citizenAwpRequest.getAwpType())) {
            log.info("Inside mapping citizen awp other applications");
            return OtherApplicationsBundle.builder()
                .applicantName(citizenAwpRequest.getPartyName())
                .author(citizenAwpRequest.getPartyName())
                .uploadedDateTime(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE))
                                      .format(DateTimeFormatter.ofPattern(DATE_FORMAT)))
                .documentRelatedToCase(YesOrNo.Yes)
                .finalDocument(getDocuments(citizenAwpRequest.getUploadedApplicationForms()))
                .supportingEvidenceBundle(YesOrNo.Yes.equals(citizenAwpRequest.getHasSupportingDocuments())
                                              ? getSupportingBundles(citizenAwpRequest) : null)
                .urgency(YesOrNo.Yes.equals(citizenAwpRequest.getUrgencyInFiveDays())
                             ? getUrgency(citizenAwpRequest) : null)
                .applicationType(OtherApplicationType.getValue(getApplicationKey(citizenAwpRequest))) //REVISIT
                .applicationStatus(getApplicationStatus(citizenAwpRequest))
                .build();
        }
        return null;
    }

    private List<Element<ServedParties>> getSelectedParties(CitizenAwpRequest citizenAwpRequest) {
        return Collections.singletonList(
            element(ServedParties.builder()
                        .partyId(citizenAwpRequest.getPartyId())
                        .partyName(citizenAwpRequest.getPartyName())
                        .build()
            ));
    }

    private List<Element<Document>> getDocuments(List<Document> uploadedApplicationForms) {
        return nullSafeCollection(uploadedApplicationForms).stream()
            .map(ElementUtils::element)
            .toList();
    }

    private List<Element<SupportingEvidenceBundle>> getSupportingBundles(CitizenAwpRequest citizenAwpRequest) {
        return nullSafeCollection(citizenAwpRequest.getSupportingDocuments()).stream()
            .map(document -> element(
                SupportingEvidenceBundle.builder()
                    .uploadedBy(citizenAwpRequest.getPartyName())
                    .dateTimeUploaded(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
                    .documentRelatedToCase(YesOrNo.Yes)
                    .document(document)
                    .build()
            )).toList();
    }

    private Urgency getUrgency(CitizenAwpRequest citizenAwpRequest) {
        return Urgency.builder()
            .urgencyType(UrgencyTimeFrameType.WITHIN_5_DAYS)
            .urgencyReason(citizenAwpRequest.getUrgencyInFiveDaysReason())
            .build();
    }

    private C2ApplicationDetails getC2ApplicationDetails(CitizenAwpRequest citizenAwpRequest) {

        return C2ApplicationDetails.builder()
            .consent(YesOrNo.Yes.equals(citizenAwpRequest.getAgreementForRequest())
                         ? C2Consent.withConsent : C2Consent.withoutConsent)
            .reasonForNotInformingRespondent(YesOrNo.No.equals(citizenAwpRequest.getInformOtherParties())
                                                 ? citizenAwpRequest.getReasonCantBeInformed() : null)
            .build();
    }

    private Payment getPaymentDetails(CitizenAwpRequest citizenAwpRequest,
                                      CitizenAwpPayment citizenAwpPayment) {

        String additionalApplicationPaymentStatus;

        if (StringUtils.isEmpty(citizenAwpPayment.getFee())) {
            additionalApplicationPaymentStatus = PaymentStatus.NOT_APPLICABLE.getDisplayedValue();
        } else if (YesOrNo.Yes.equals(citizenAwpRequest.getHaveHwfReference())
            && StringUtils.isNotEmpty(citizenAwpRequest.getHwfReferenceNumber())) {
            additionalApplicationPaymentStatus = PaymentStatus.HWF.getDisplayedValue();
        } else {
            additionalApplicationPaymentStatus = PaymentStatus.PAID.getDisplayedValue();
        }

        return Payment.builder()
            .hwfReferenceNumber(YesOrNo.Yes.equals(citizenAwpRequest.getHaveHwfReference())
                                    ? citizenAwpRequest.getHwfReferenceNumber() : null)
            .status(additionalApplicationPaymentStatus)
            .fee(citizenAwpPayment.getFee())
            .paymentServiceRequestReferenceNumber(citizenAwpPayment.getServiceReqRef())
            .paymentReferenceNumber(citizenAwpPayment.getPaymentReqRef())
            .build();
    }

    private CreatePaymentRequest getPaymentRequestToCompare(CitizenAwpRequest citizenAwpRequest) {
        return CreatePaymentRequest.builder()
            .awpType(citizenAwpRequest.getAwpType())
            .partyType(citizenAwpRequest.getPartyType())
            .feeType(citizenAwpRequest.getFeeType())
            .build();
    }

    private String getApplicationKey(CitizenAwpRequest citizenAwpRequest) {
        return concat(
            concat(citizenAwpRequest.getAwpType(), "_"),
            citizenAwpRequest.getAwpReason().replace("-", "_")
        )
            .toUpperCase();
    }
}
