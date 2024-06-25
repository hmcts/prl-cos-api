package uk.gov.hmcts.reform.prl.mapper.citizen.awp;

import lombok.extern.slf4j.Slf4j;
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
        additionalApplicationsBundle = mapPaymentDetailsAndRemove(caseData,
                                                                  citizenAwpRequest,
                                                                  additionalApplicationsBundle);
        log.info("Mapped data with payment details {}", additionalApplicationsBundle);

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundles =
            isNotEmpty(caseData.getAdditionalApplicationsBundle())
                ? caseData.getAdditionalApplicationsBundle() : new ArrayList<>();
        additionalApplicationsBundles.add(element(additionalApplicationsBundle));

        return caseData.toBuilder()
            .additionalApplicationsBundle(additionalApplicationsBundles)
            .build();
    }

    private AdditionalApplicationsBundle mapPaymentDetailsAndRemove(CaseData caseData,
                                                                    CitizenAwpRequest citizenAwpRequest,
                                                                    AdditionalApplicationsBundle additionalApplicationsBundle) {
        Optional<Element<CitizenAwpPayment>> optionalCitizenAwpPaymentElement =
            getCitizenAwpPaymentIfPresent(caseData.getCitizenAwpPayments(), getPaymentRequestToCompare(citizenAwpRequest));
        //update payment details
        if (optionalCitizenAwpPaymentElement.isPresent()) {
            additionalApplicationsBundle = additionalApplicationsBundle.toBuilder()
                .payment(getPaymentDetails(citizenAwpRequest,
                                           optionalCitizenAwpPaymentElement.get().getValue()))
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
                .build();
        }
        return null;
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
                .applicationStatus(ApplicationStatus.SUBMITTED.getDisplayedValue())
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
        return Payment.builder()
            .hwfReferenceNumber(YesOrNo.Yes.equals(citizenAwpRequest.getHaveHwfReference())
                                    ? citizenAwpRequest.getHwfReferenceNumber() : null)
            .status(PaymentStatus.PAID.getDisplayedValue())
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
        return concat(concat(citizenAwpRequest.getAwpType(), "_"),
                      citizenAwpRequest.getAwpReason().replace("-", "_"))
            .toUpperCase();
    }
}
