package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Supplement;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadAdditionalApplicationService {

    private final IdamClient idamClient;

    public List<Element<AdditionalApplicationsBundle>> getAdditionalApplicationElements(String authorisation, CaseData caseData) {
        UserDetails userDetails = idamClient.getUserDetails(authorisation);
        List<Element<AdditionalApplicationsBundle>> additionalApplicationElements = new ArrayList<>();
        if (caseData.getUploadAdditionalApplicationData() != null) {
            String author = userDetails.getEmail();
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss aa",
                                                                                            Locale.UK
            ));
            C2DocumentBundle c2DocumentBundle = null;
            OtherApplicationsBundle otherApplicationsBundle = null;
            c2DocumentBundle = getC2DocumentBundle(caseData, author, currentDateTime, c2DocumentBundle);
            otherApplicationsBundle = getOtherApplicationsBundle(caseData,
                                                                 author,
                                                                 currentDateTime,
                                                                 otherApplicationsBundle
            );
            AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder().author(
                author).uploadedDateTime(currentDateTime).c2DocumentBundle(c2DocumentBundle).otherApplicationsBundle(
                otherApplicationsBundle).build();

            if (caseData.getAdditionalApplicationList() != null && !caseData.getAdditionalApplicationList().isEmpty()) {
                additionalApplicationElements = caseData.getAdditionalApplicationList();
            }
            additionalApplicationElements.add(element(additionalApplicationsBundle));
        }
        return additionalApplicationElements;
    }

    private OtherApplicationsBundle getOtherApplicationsBundle(CaseData caseData, String author,
                                                               String currentDateTime, OtherApplicationsBundle otherApplicationsBundle) {
        if (caseData.getUploadAdditionalApplicationData().getTemporaryOtherApplicationsBundle() != null) {
            OtherApplicationsBundle temporaryOtherApplicationsBundle = caseData.getUploadAdditionalApplicationData()
                .getTemporaryOtherApplicationsBundle();
            otherApplicationsBundle = OtherApplicationsBundle.builder().author(author).uploadedDateTime(currentDateTime).document(
                temporaryOtherApplicationsBundle.getDocument()).documentAcknowledge(
                    temporaryOtherApplicationsBundle.getDocumentAcknowledge()).parentalResponsibilityType(
                temporaryOtherApplicationsBundle.getParentalResponsibilityType()).urgencyTimeFrameType(
                temporaryOtherApplicationsBundle.getUrgencyTimeFrameType()).supplementsBundle(
                    createSupplementsBundle(temporaryOtherApplicationsBundle.getSupplementsBundle(),
                                                                                                                      author
            )).supportingEvidenceBundle(createSupportingEvidenceBundle(temporaryOtherApplicationsBundle.getSupportingEvidenceBundle(),
                                                                       author
            )).build();
        }
        return otherApplicationsBundle;
    }

    private C2DocumentBundle getC2DocumentBundle(CaseData caseData, String author, String currentDateTime, C2DocumentBundle c2DocumentBundle) {
        if (caseData.getUploadAdditionalApplicationData().getTemporaryC2Document() != null) {
            C2DocumentBundle temporaryC2Document = caseData.getUploadAdditionalApplicationData().getTemporaryC2Document();
            c2DocumentBundle = C2DocumentBundle.builder().author(author).uploadedDateTime(currentDateTime).document(
                temporaryC2Document.getDocument()).documentAcknowledge(temporaryC2Document.getDocumentAcknowledge()).c2AdditionalOrdersRequested(
                temporaryC2Document.getC2AdditionalOrdersRequested()).parentalResponsibilityType(
                    temporaryC2Document.getParentalResponsibilityType()).hearingList(
                temporaryC2Document.getHearingList()).urgencyTimeFrameType(temporaryC2Document.getUrgencyTimeFrameType()).additionalDraftOrdersBundle(
                temporaryC2Document.getAdditionalDraftOrdersBundle()).supplementsBundle(createSupplementsBundle(
                temporaryC2Document.getSupplementsBundle(),
                author
            )).supportingEvidenceBundle(createSupportingEvidenceBundle(temporaryC2Document.getSupportingEvidenceBundle(),
                                                                       author
            )).build();
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
}
