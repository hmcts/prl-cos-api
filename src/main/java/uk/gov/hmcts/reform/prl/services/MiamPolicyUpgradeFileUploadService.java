package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseEvidenceDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MULTIPART_FILE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuDomesticAbuse;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance;
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

    private final AuthTokenGenerator authTokenGenerator;

    private final CaseDocumentClient caseDocumentClient;

    private final SystemUserService systemUserService;

    public CaseData renameMiamPolicyUpgradeDocumentWithConfidential(CaseData caseData, String systemAuthorisation) {
        if (CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())) {
            caseData = renameDomesticAbuseDocumentWithConfidential(caseData, systemAuthorisation);
            caseData = renamePreviousMiamAttendanceDocumentWithConfidential(caseData, systemAuthorisation);
        } else {
            log.info("No file to rename for MIAM Policy Upgrade with Confidential prefix for Exemptions");
        }
        return caseData;
    }

    private CaseData renamePreviousMiamAttendanceDocumentWithConfidential(CaseData caseData, String systemAuthorisation) {
        if (caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(mpuPreviousMiamAttendance)
            && ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())) {
            if (miamPolicyUpgradePreviousAttendance_Value_1.equals(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
                && ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuDocFromDisputeResolutionProvider())
                && !caseData.getMiamPolicyUpgradeDetails().getMpuDocFromDisputeResolutionProvider().getDocumentFileName().startsWith(
                CONFIDENTIAL)) {
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

    private CaseData renameDomesticAbuseDocumentWithConfidential(CaseData caseData, String systemAuthorisation) {
        if ((caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(mpuDomesticAbuse))
            && Yes.equals(caseData.getMiamPolicyUpgradeDetails().getMpuIsDomesticAbuseEvidenceProvided())
            && isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuDomesticAbuseEvidenceDocument())) {
            List<Element<DomesticAbuseEvidenceDocument>> mpuConfidentialDomesticAbuseEvidenceDocument = new ArrayList<>();
            caseData.getMiamPolicyUpgradeDetails().getMpuDomesticAbuseEvidenceDocument()
                .stream().forEach(domesticAbuseEvidenceDocument -> {
                    Document domesticAbuseDocument = domesticAbuseEvidenceDocument.getValue().getDomesticAbuseDocument();
                    if (!domesticAbuseDocument.getDocumentFileName().startsWith(CONFIDENTIAL)) {
                        domesticAbuseDocument = manageDocumentsService.downloadAndDeleteDocument(
                            domesticAbuseDocument, systemAuthorisation);
                    }
                    mpuConfidentialDomesticAbuseEvidenceDocument.add(element(DomesticAbuseEvidenceDocument.builder().domesticAbuseDocument(
                        domesticAbuseDocument).build()));
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
        return caseData;
    }

    public CaseData renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())) {
            String systemAuthorisation = systemUserService.getSysUserToken();
            caseData = renameDomesticAbuseDocumentWithoutConfidential(caseData, systemAuthorisation);
            caseData = renamePreviousMiamAttendanceDocumentWithoutConfidential(caseData, systemAuthorisation);
        } else {
            log.info("No file to rename for MIAM Policy Upgrade with Confidential prefix for Exemptions");
        }
        return caseData;
    }

    private CaseData renamePreviousMiamAttendanceDocumentWithoutConfidential(CaseData caseData, String systemAuthorisation) {
        if (caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(mpuPreviousMiamAttendance)
            && ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())) {
            if (miamPolicyUpgradePreviousAttendance_Value_1.equals(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason())
                && ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuDocFromDisputeResolutionProvider())
                && caseData.getMiamPolicyUpgradeDetails().getMpuDocFromDisputeResolutionProvider().getDocumentFileName().startsWith(
                CONFIDENTIAL)) {
                Document mpuDocFromDisputeResolutionProvider = downloadAndUploadDocumentWithoutConfidential(
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
                && caseData.getMiamPolicyUpgradeDetails().getMpuCertificateByMediator().getDocumentFileName().startsWith(
                CONFIDENTIAL)) {
                Document mpuCertificateByMediator = downloadAndUploadDocumentWithoutConfidential(
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

    private CaseData renameDomesticAbuseDocumentWithoutConfidential(CaseData caseData, String systemAuthorisation) {
        if ((caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(mpuDomesticAbuse))
            && Yes.equals(caseData.getMiamPolicyUpgradeDetails().getMpuIsDomesticAbuseEvidenceProvided())
            && isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuDomesticAbuseEvidenceDocument())) {
            List<Element<DomesticAbuseEvidenceDocument>> mpuConfidentialDomesticAbuseEvidenceDocument = new ArrayList<>();
            caseData.getMiamPolicyUpgradeDetails().getMpuDomesticAbuseEvidenceDocument()
                .stream().forEach(domesticAbuseEvidenceDocument -> {
                    Document domesticAbuseDocument = domesticAbuseEvidenceDocument.getValue().getDomesticAbuseDocument();
                    if (domesticAbuseDocument.getDocumentFileName().startsWith(CONFIDENTIAL)) {
                        domesticAbuseDocument = downloadAndUploadDocumentWithoutConfidential(
                            domesticAbuseDocument, systemAuthorisation);
                    }
                    mpuConfidentialDomesticAbuseEvidenceDocument.add(element(DomesticAbuseEvidenceDocument.builder().domesticAbuseDocument(
                        domesticAbuseDocument).build()));
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
        return caseData;
    }

    public Document downloadAndUploadDocumentWithoutConfidential(Document document, String systemAuthorisation) {
        try {
            String serviceToken = authTokenGenerator.generate();
            UUID documentId = UUID.fromString(DocumentUtils.getDocumentId(document.getDocumentUrl()));
            Document newUploadedDocument = getNewUploadedDocumentWithoutConfidential(
                systemAuthorisation,
                serviceToken,
                document,
                documentId
            );
            if (null != newUploadedDocument) {
                caseDocumentClient.deleteDocument(systemAuthorisation,
                                                  serviceToken,
                                                  documentId, true
                );
                return newUploadedDocument;
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to rename the confidential MIAM documents when application is returned",
                e
            );
        }
        return document;
    }

    private Document getNewUploadedDocumentWithoutConfidential(String systemAuthorisation,
                                            String serviceToken,
                                            Document document,
                                            UUID documentId) {
        byte[] docData;
        Document newUploadedDocument = null;
        try {

            Resource resource = caseDocumentClient.getDocumentBinary(systemAuthorisation, serviceToken,
                                                                     documentId
            ).getBody();
            docData = IOUtils.toByteArray(resource.getInputStream());
            UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
                systemAuthorisation,
                serviceToken,
                CASE_TYPE,
                JURISDICTION,
                List.of(
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        document.getDocumentFileName().replace(CONFIDENTIAL, BLANK_STRING),
                        APPLICATION_PDF_VALUE,
                        docData
                    ))
            );
            newUploadedDocument = Document.buildFromDocument(uploadResponse.getDocuments().get(0));
        } catch (Exception ex) {
            log.error("Failed to upload new document {}", ex.getMessage(), ex);
        }
        return newUploadedDocument;
    }
}
