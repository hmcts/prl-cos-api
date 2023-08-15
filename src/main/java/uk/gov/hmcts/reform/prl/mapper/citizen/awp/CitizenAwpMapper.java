package uk.gov.hmcts.reform.prl.mapper.citizen.awp;

import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2Consent;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.models.documents.Document.buildFromCitizenDocument;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

public class CitizenAwpMapper {
    private static final String DATE_FORMAT = "dd-MMM-yyyy hh:mm:ss a";

    public CaseData map(CaseData caseData, CitizenAwpRequest citizenAwpRequest) {

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle =
            isNotEmpty(caseData.getAdditionalApplicationsBundle())
                ? caseData.getAdditionalApplicationsBundle() : new ArrayList<>();

        additionalApplicationsBundle.add(
            element(AdditionalApplicationsBundle.builder()
                        .author(citizenAwpRequest.getPartyName())
                        .uploadedDateTime(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE))
                                              .format(DateTimeFormatter.ofPattern(DATE_FORMAT)))
                        .applicationStatus("Completed") //REVISIT
                        .payment(getPaymentDetails(citizenAwpRequest))
                        .partyType(PartyEnum.valueOf(citizenAwpRequest.getPartyType()))
                        .selectedParties(getSelectedParties(citizenAwpRequest))
                        .c2DocumentBundle(getC2ApplicationBundle(citizenAwpRequest))
                        .otherApplicationsBundle(getOtherApplicationBundle(citizenAwpRequest))
                        .build()
            )
        );

        return caseData.toBuilder().additionalApplicationsBundle(additionalApplicationsBundle).build();
    }

    private C2DocumentBundle getC2ApplicationBundle(CitizenAwpRequest citizenAwpRequest) {
        if ("C2".equals(citizenAwpRequest.getAwpType())) {
            C2DocumentBundle.builder()
                .applicantName(citizenAwpRequest.getPartyName())
                .author(citizenAwpRequest.getPartyName())
                .uploadedDateTime(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE))
                                      .format(DateTimeFormatter.ofPattern(DATE_FORMAT)))
                .documentRelatedToCase(YesOrNo.Yes)
                //.document(getDocuments(citizenAwpRequest.getUploadedApplicationForms())) //REVISIT
                .supportingEvidenceBundle(YesOrNo.Yes.equals(citizenAwpRequest.getHasSupportingDocuments())
                                              ? getSupportingBundles(citizenAwpRequest) : null)
                .combinedReasonsForC2Application(null) //REVISIT
                .otherReasonsFoC2Application(null) //REVISIT
                .urgency(YesOrNo.Yes.equals(citizenAwpRequest.getUrgencyInFiveDays())
                             ? getUrgency(citizenAwpRequest) : null)
                .c2ApplicationDetails(getC2ApplicationDetails(citizenAwpRequest))
                .build();
        }
        return null;
    }

    private OtherApplicationsBundle getOtherApplicationBundle(CitizenAwpRequest citizenAwpRequest) {
        if (!"C2".equals(citizenAwpRequest.getAwpType())) {
            OtherApplicationsBundle.builder()
                .applicantName(citizenAwpRequest.getPartyName())
                .author(citizenAwpRequest.getPartyName())
                .uploadedDateTime(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE))
                                      .format(DateTimeFormatter.ofPattern(DATE_FORMAT)))
                .documentRelatedToCase(YesOrNo.Yes)
                //.document(getDocuments(citizenAwpRequest.getUploadedApplicationForms())) //REVISIT
                .supportingEvidenceBundle(YesOrNo.Yes.equals(citizenAwpRequest.getHasSupportingDocuments())
                                              ? getSupportingBundles(citizenAwpRequest) : null)
                .urgency(YesOrNo.Yes.equals(citizenAwpRequest.getUrgencyInFiveDays())
                             ? getUrgency(citizenAwpRequest) : null)
                .applicationType(null) //REVISIT
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

    private List<Element<Document>> getDocuments(List<uk.gov.hmcts.reform.prl.models.c100rebuild.Document> uploadedApplicationForms) {
        return nullSafeCollection(uploadedApplicationForms).stream()
            .map(document -> element(buildFromCitizenDocument(document)))
            .collect(Collectors.toList());
    }

    private List<Element<SupportingEvidenceBundle>> getSupportingBundles(CitizenAwpRequest citizenAwpRequest) {
        return nullSafeCollection(citizenAwpRequest.getSupportingDocuments()).stream()
            .map(document -> element(
                SupportingEvidenceBundle.builder()
                    .uploadedBy(citizenAwpRequest.getPartyName())
                    .dateTimeUploaded(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
                    .documentRelatedToCase(YesOrNo.Yes)
                    .document(buildFromCitizenDocument(document))
                    .build()
            )).collect(Collectors.toList());
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

    private Payment getPaymentDetails(CitizenAwpRequest citizenAwpRequest) {
        return Payment.builder()
            .hwfReferenceNumber(YesOrNo.Yes.equals(citizenAwpRequest.getHaveHwfReference())
                                    ? citizenAwpRequest.getHwfReferenceNumber() : null)
            .status("Paid") //REVISIT
            //ADD PAYMENT DETAILS
            .build();
    }
}
