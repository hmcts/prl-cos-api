package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseEvidenceDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.domesticAbuse;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.previousMiamAttendance;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_1;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_2;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum.miamCertificate;
import static uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService.CONFIDENTIAL;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MiamPolicyUpgradeFileUploadService {


    private final ManageDocumentsService manageDocumentsService;

    public CaseData renameConfidentialDocumentForMiamPolicyUpgrade(CaseData caseData, String systemAuthorisation) {
        log.info("Inside renameConfidentialDocumentForMiamPolicyUpgrade");
        if ((caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(domesticAbuse))
            && Yes.equals(caseData.getMiamPolicyUpgradeDetails().getMpuIsDomesticAbuseEvidenceProvided())
            && isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuDomesticAbuseEvidenceDocument())) {
            List<Element<DomesticAbuseEvidenceDocument>> mpuConfidentialDomesticAbuseEvidenceDocument = new ArrayList<>();
            caseData.getMiamPolicyUpgradeDetails().getMpuDomesticAbuseEvidenceDocument()
                .stream().forEach(domesticAbuseEvidenceDocument -> {
                    log.info("Going to append Confidential prefix for Domestic Abuse Evidence Document");
                    Document domesticAbuseDocument = domesticAbuseEvidenceDocument.getValue().getDomesticAbuseDocument();
                    if (!domesticAbuseDocument.getDocumentFileName().startsWith(CONFIDENTIAL)) {
                        domesticAbuseDocument = manageDocumentsService.downloadAndDeleteDocument(
                            domesticAbuseDocument, systemAuthorisation);
                        mpuConfidentialDomesticAbuseEvidenceDocument.add(element(DomesticAbuseEvidenceDocument.builder().domesticAbuseDocument(
                            domesticAbuseDocument).build()));
                    }
                });
            caseData = caseData.toBuilder()
                .miamPolicyUpgradeDetails(caseData.getMiamPolicyUpgradeDetails()
                                              .toBuilder()
                                              .mpuDomesticAbuseEvidenceDocument(isNotEmpty(
                                                  mpuConfidentialDomesticAbuseEvidenceDocument)
                                                                                    ? mpuConfidentialDomesticAbuseEvidenceDocument
                                                                                    : caseData.getMiamPolicyUpgradeDetails()
                                                  .getMpuDomesticAbuseEvidenceDocument())
                                              .build())
                .build();
        }
        if (caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(previousMiamAttendance)
            && ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())) {
            if (miamPolicyUpgradePreviousAttendance_Value_1.equals(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
                && ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuDocFromDisputeResolutionProvider())
                && !caseData.getMiamPolicyUpgradeDetails().getMpuDocFromDisputeResolutionProvider().getDocumentFileName().startsWith(
                CONFIDENTIAL)) {
                log.info("Going to append Confidential prefix previous maim attendance document for option 1");
                Document mpuDocFromDisputeResolutionProvider = manageDocumentsService.downloadAndDeleteDocument(
                    caseData.getMiamPolicyUpgradeDetails().getMpuDocFromDisputeResolutionProvider(),
                    systemAuthorisation
                );
                caseData = caseData.toBuilder()
                    .miamPolicyUpgradeDetails(caseData.getMiamPolicyUpgradeDetails()
                                                  .toBuilder()
                                                  .mpuDocFromDisputeResolutionProvider(
                                                      mpuDocFromDisputeResolutionProvider)
                                                  .build())
                    .build();
            } else if (miamPolicyUpgradePreviousAttendance_Value_2.equals(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
                && miamCertificate.equals(caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence())
                && ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuCertificateByMediator())
                && !caseData.getMiamPolicyUpgradeDetails().getMpuCertificateByMediator().getDocumentFileName().startsWith(
                CONFIDENTIAL)) {
                log.info("Going to append Confidential prefix previous maim attendance document for option 2");
                Document mpuCertificateByMediator = manageDocumentsService.downloadAndDeleteDocument(
                    caseData.getMiamPolicyUpgradeDetails().getMpuCertificateByMediator(),
                    systemAuthorisation
                );
                caseData = caseData.toBuilder()
                    .miamPolicyUpgradeDetails(caseData.getMiamPolicyUpgradeDetails()
                                                  .toBuilder()
                                                  .mpuCertificateByMediator(mpuCertificateByMediator)
                                                  .build())
                    .build();
            }
        }
        return caseData;
    }
}
