package uk.gov.hmcts.reform.prl.services;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseEvidenceDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_1;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_2;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class MiamPolicyUpgradeFileUploadServiceTest {

    @InjectMocks
    private MiamPolicyUpgradeFileUploadService miamPolicyUpgradeFileUploadService;

    @Mock
    ManageDocumentsService manageDocumentsService;

    @Mock
    private  SystemUserService systemUserService;

    @Mock
    CaseDocumentClient caseDocumentClient;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Test
    public void testRenameMiamPolicyUpgradeDocumentEmptyMpuReasons() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(CaseData
                .builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                    .builder()
                    .build())
                .build(),
            "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentPreviousAttendanceMpuReason() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(CaseData
                .builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                    .builder()
                    .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                    .build())
                .build(),
            "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentPreviousAttendanceMpuReasonWithPreviousMiamAttendanceReason() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(CaseData
                .builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                    .builder()
                    .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                    .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_2)
                    .build())
                .build(),
            "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentPreviousAttendanceMpuReasonWithPreviousMiamAttendanceReason1() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(CaseData
                .builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                    .builder()
                    .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                    .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_1)
                    .build())
                .build(),
            "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentPreviousAttendanceMpuReasonWithPreviousMiamAttendanceReason1WithDoc() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(CaseData
                .builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                    .builder()
                    .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                    .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_1)
                    .mpuDocFromDisputeResolutionProvider(Document.builder().documentFileName("test").build())
                    .build())
                .build(),
            "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentPreviousAttendanceMpuReasonWithPreviousMiamAttendanceReason1WithConfidentialDoc() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(CaseData
                .builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                    .builder()
                    .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                    .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_1)
                    .mpuDocFromDisputeResolutionProvider(Document.builder().documentFileName("Confidential_").build())
                    .build())
                .build(),
            "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentMpuPreviousMiamAttendanceReason() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(CaseData
                .builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                    .builder()
                    .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                    .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_2)
                    .build())
                .build(),
            "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentMpuPreviousMiamAttendanceReasonAndCertificateChosenAsEvidence() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(CaseData
                .builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                    .builder()
                    .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                    .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_2)
                    .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamCertificate)
                    .build())
                .build(),
            "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentMpuPreviousMiamAttendanceReasonAndCertificateUploaded() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(CaseData
                .builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                    .builder()
                    .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                    .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_2)
                    .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamCertificate)
                    .mpuCertificateByMediator(Document.builder().documentFileName("test").build())
                    .build())
                .build(),
            "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentMpuPreviousMiamAttendanceReasonAndConfidentialCertificateUploaded() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(CaseData
                .builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                    .builder()
                    .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                    .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_2)
                    .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamCertificate)
                    .mpuCertificateByMediator(Document.builder().documentFileName("Confidential_").build())
                    .build())
                .build(),
            "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentDomesticMpuReason() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(CaseData
                .builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                    .builder()
                    .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
                    .build())
                .build(),
            "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentDomesticMpuReasonEvidenceProvided() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(CaseData
                .builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                    .builder()
                    .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
                    .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
                    .build())
                .build(),
            "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentDomesticMpuReasonEvidenceProvidedWithDoc() {
        when(manageDocumentsService.downloadAndDeleteDocument(any(), any())).thenReturn(Document.builder().build());
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(CaseData
                .builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                    .builder()
                    .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
                    .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
                    .mpuDomesticAbuseEvidenceDocument(List.of(Element
                        .<DomesticAbuseEvidenceDocument>builder().value(DomesticAbuseEvidenceDocument
                            .builder()
                            .domesticAbuseDocument(Document.builder().documentFileName("test").build())
                            .build())
                        .build()))
                    .build())
                .build(),
            "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentDomesticMpuReasonEvidenceProvidedWithConfidentialDoc() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(CaseData
                .builder()
                .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                    .builder()
                    .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
                    .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
                    .mpuDomesticAbuseEvidenceDocument(List.of(Element
                        .<DomesticAbuseEvidenceDocument>builder().value(DomesticAbuseEvidenceDocument
                            .builder()
                            .domesticAbuseDocument(Document.builder().documentFileName("Confidential_").build())
                            .build())
                        .build()))
                    .build())
                .build(),
            "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyWithoutConfidentialEmptyMpuReasons() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .build())
            .build()));
    }

    @Test
    public void testRenameMiamPolicyWithoutConfidentialPreviousMpuReasons() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                .build())
            .build()));
    }

    @Test
    public void testRenameMiamPolicyWithoutConfidentialPreviousMpuReason1() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_1)
                .build())
            .build()));
    }

    @Test
    public void testRenameMiamPolicyWithoutConfidentialPreviousMpuReason1WithDoc() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_1)
                .mpuDocFromDisputeResolutionProvider(Document.builder().documentFileName("test").build())
                .build())
            .build()));
    }

    @Test
    public void testRenameMiamPolicyWithoutConfidentialPreviousMpuReason1WithConfidentialDoc() {
        uk.gov.hmcts.reform.prl.models.documents.Document doc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("Confidential_test.pdf")
            .documentBinaryUrl("http://test.link")
            .documentUrl("1accfb1e-2574-4084-b97e-1cd53fd14815").build();

        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_1)
                .mpuDocFromDisputeResolutionProvider(doc)
                .build())
            .build()));
    }

    @Test
    public void testRenameMiamPolicyWithoutConfidentialPreviousMpuReason2() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_2)
                .build())
            .build()));
    }

    @Test
    public void testRenameMiamPolicyWithoutConfidentialPreviousMpuReason2WithCertificate() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_2)
                .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamCertificate)
                .build())
            .build()));
    }

    @Test
    public void testRenameMiamPolicyWithoutConfidentialPreviousMpuReason2WithCertificateByMediator() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_2)
                .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamCertificate)
                .mpuCertificateByMediator(Document.builder().documentFileName("test").build())
                .build())
            .build()));
    }

    @Test
    public void testRenameMiamPolicyWithoutConfidentialPreviousMpuReason2WithCertificateByMediatorConfidential() {
        uk.gov.hmcts.reform.prl.models.documents.Document doc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("Confidential_test.pdf")
            .documentBinaryUrl("http://test.link")
            .documentUrl("1accfb1e-2574-4084-b97e-1cd53fd14815").build();

        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
                .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_2)
                .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamCertificate)
                .mpuCertificateByMediator(doc)
                .build())
            .build()));
    }

    @Test
    public void testRenameMiamPolicyWithoutConfidentialDomesticMpuReasons() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
                .build())
            .build()));
    }

    @Test
    public void testRenameMiamPolicyWithoutConfidentialDomesticMpuReasonsWithEvidence() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
                .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
                .build())
            .build()));
    }

    @Test
    public void testRenameMiamPolicyWithoutConfidentialDomesticMpuReasonsWithEvidenceAndDocument() {
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
                .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
                .mpuDomesticAbuseEvidenceDocument(List.of(Element
                    .<DomesticAbuseEvidenceDocument>builder().value(DomesticAbuseEvidenceDocument
                        .builder()
                        .domesticAbuseDocument(Document.builder().documentFileName("test").build())
                        .build())
                    .build()))
                .build())
            .build()));
    }

    @Test
    public void testRenameMiamPolicyWithoutConfidentialDomesticMpuReasonsWithEvidenceAndConfidentialDocument() {
        uk.gov.hmcts.reform.prl.models.documents.Document doc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("Confidential_test.pdf")
            .documentBinaryUrl("http://test.link")
            .documentUrl("1accfb1e-2574-4084-b97e-1cd53fd14815").build();

        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
                .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
                .mpuDomesticAbuseEvidenceDocument(List.of(Element
                    .<DomesticAbuseEvidenceDocument>builder().value(DomesticAbuseEvidenceDocument
                        .builder()
                        .domesticAbuseDocument(doc)
                        .build())
                    .build()))
                .build())
            .build()));
    }

    @Test(expected = IllegalStateException.class)
    public void testRenameMiamPolicyThrowsIllegalStateException() {
        miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(CaseData
            .builder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
                .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
                .mpuDomesticAbuseEvidenceDocument(List.of(Element
                    .<DomesticAbuseEvidenceDocument>builder().value(DomesticAbuseEvidenceDocument
                        .builder()
                        .domesticAbuseDocument(Document.builder().documentFileName("Confidential_").build())
                        .build())
                    .build()))
                .build())
            .build());
    }

    @Test
    public void testDownloadAndUploadDocumentWithoutConfidential2() {

        uk.gov.hmcts.reform.prl.models.documents.Document doc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("Confidential_test.pdf")
            .documentBinaryUrl("http://test.link")
            .documentUrl("1accfb1e-2574-4084-b97e-1cd53fd14815").build();

        when(authTokenGenerator.generate()).thenReturn("test");
        assertNotNull(miamPolicyUpgradeFileUploadService.downloadAndUploadDocumentWithoutConfidential(doc, "test"));

    }

    @Test(expected = IllegalStateException.class)
    public void testDownloadAndUploadDocumentWithoutConfidential3() {

        uk.gov.hmcts.reform.prl.models.documents.Document doc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("Confidential_test.pdf")
            .documentBinaryUrl("http://test.link")
            .documentUrl("http://test.link").build();

        when(authTokenGenerator.generate()).thenReturn("test");
        miamPolicyUpgradeFileUploadService.downloadAndUploadDocumentWithoutConfidential(doc, "test");

    }

}
