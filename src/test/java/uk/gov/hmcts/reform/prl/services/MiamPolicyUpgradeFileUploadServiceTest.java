package uk.gov.hmcts.reform.prl.services;


import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseEvidenceDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
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
    AuthTokenGenerator authTokenGenerator;


    private static final String USER_TOKEN = "Bearer testToken";

    private CaseData caseDataconf;
    private CaseData casedataPreviousMiam;
    private CaseData caseDataMiamAttendanceReason;
    private CaseData caseData;

    Document document;
    private final String caseId = "1234567891011121";


    @Before
    public void init() {
        document =  Document.builder().documentFileName("test").categoryId("test").documentUrl("url").documentBinaryUrl("url").build();

        List<MiamExemptionsChecklistEnum> listMiamExemptionsChecklistEnum = new ArrayList<>();
        listMiamExemptionsChecklistEnum.add(MiamExemptionsChecklistEnum.domesticAbuse);
        listMiamExemptionsChecklistEnum.add(MiamExemptionsChecklistEnum.previousMiamAttendance);
        DomesticAbuseEvidenceDocument domesticAbuseEvidenceDocumentConf = DomesticAbuseEvidenceDocument.builder()
            .domesticAbuseDocument(Document.builder().documentFileName("test").categoryId("test").build()).build();

        Element<DomesticAbuseEvidenceDocument> domesticAbuseEvidenceDocumentConfVal = Element
            .<DomesticAbuseEvidenceDocument>builder().value(domesticAbuseEvidenceDocumentConf).build();


        Document.builder().documentFileName("test").categoryId("test").build();
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetailsConf = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.Yes)
            .mpuApplicantAttendedMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mediatorRegistrationNumber("123")
            .familyMediatorServiceName("test")
            .soleTraderName("test")
            .miamCertificationDocumentUpload(Document.builder().build())
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(listMiamExemptionsChecklistEnum)
            .mpuDomesticAbuseEvidences(List.of(MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1))
            .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
            .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_2)
            .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamCertificate)
            .mpuCertificateByMediator(document)
            .mpuDomesticAbuseEvidenceDocument(List.of(domesticAbuseEvidenceDocumentConfVal))
            .build();

        MiamPolicyUpgradeDetails miamPolicyUpgradeDetailsConfPrevious = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.Yes)
            .mpuApplicantAttendedMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mediatorRegistrationNumber("123")
            .familyMediatorServiceName("test")
            .soleTraderName("test")
            .miamCertificationDocumentUpload(Document.builder().build())
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(listMiamExemptionsChecklistEnum)
            .mpuDomesticAbuseEvidences(List.of(MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1))
            .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
            .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_1)
            .mpuDocFromDisputeResolutionProvider(document)
            .mpuDomesticAbuseEvidenceDocument(List.of(domesticAbuseEvidenceDocumentConfVal))
            .build();
        caseDataconf = CaseData.builder()
            .courtName("testcourt")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .miamPolicyUpgradeDetails(miamPolicyUpgradeDetailsConf)
            .caseTypeOfApplication("C100")
            .build();

        casedataPreviousMiam = CaseData.builder()
            .courtName("testcourt")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .miamPolicyUpgradeDetails(miamPolicyUpgradeDetailsConfPrevious)
            .caseTypeOfApplication("C100")
            .build();

        DomesticAbuseEvidenceDocument domesticAbuseEvidenceDocument = DomesticAbuseEvidenceDocument.builder()
            .domesticAbuseDocument(Document.builder().documentFileName("test").categoryId("test").build()).build();

        Element<DomesticAbuseEvidenceDocument> domesticAbuseEvidenceDocumentVal = Element
            .<DomesticAbuseEvidenceDocument>builder().value(domesticAbuseEvidenceDocument).build();


        Document.builder().documentFileName("test").categoryId("test").build();
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.Yes)
            .mpuApplicantAttendedMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mediatorRegistrationNumber("123")
            .familyMediatorServiceName("test")
            .soleTraderName("test")
            .miamCertificationDocumentUpload(Document.builder().build())
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(listMiamExemptionsChecklistEnum)
            .mpuDomesticAbuseEvidences(List.of(MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1))
            .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
            .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_1)
            .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamCertificate)
            .mpuDocFromDisputeResolutionProvider(Document.builder().documentFileName("Confidential_test").categoryId("test").documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15").documentBinaryUrl("https:google.com").build())
            .mpuCertificateByMediator(Document.builder().documentFileName("Confidential_test").categoryId("test").documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15").documentBinaryUrl("https:google.com").build())
            .mpuDomesticAbuseEvidenceDocument(List.of(domesticAbuseEvidenceDocumentVal))
            .build();


        caseData = CaseData.builder()
            .courtName("testcourt")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails)
            .caseTypeOfApplication("C100")
            .build();

        MiamPolicyUpgradeDetails miamPolicyUpgradeDetailsMiamAttaendanceReason = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.Yes)
            .mpuApplicantAttendedMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mediatorRegistrationNumber("123")
            .familyMediatorServiceName("test")
            .soleTraderName("test")
            .miamCertificationDocumentUpload(Document.builder().build())
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(listMiamExemptionsChecklistEnum)
            .mpuDomesticAbuseEvidences(List.of(MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1))
            .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
            .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_2)
            .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamCertificate)
            .mpuDocFromDisputeResolutionProvider(Document.builder().documentFileName("Confidential_test").categoryId("test").documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15").documentBinaryUrl("https:google.com").build())
            .mpuCertificateByMediator(Document.builder().documentFileName("Confidential_test").categoryId("test").documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15").documentBinaryUrl("https:google.com").build())
            .mpuDomesticAbuseEvidenceDocument(List.of(domesticAbuseEvidenceDocumentVal))
            .build();


        caseDataMiamAttendanceReason = CaseData.builder()
            .courtName("testcourt")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .miamPolicyUpgradeDetails(miamPolicyUpgradeDetailsMiamAttaendanceReason)
            .caseTypeOfApplication("C100")
            .build();

    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentWithConfidentialDomestic() throws Exception {
        when(manageDocumentsService.downloadAndDeleteDocument(
            any(), any())).thenReturn(document);
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(caseDataconf,
                                                                               "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testRenameMiamPolicyUpgradeDocumentWithConfidentialPreviousMiam() throws Exception {
        when(manageDocumentsService.downloadAndDeleteDocument(
            any(), any())).thenReturn(document);
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(casedataPreviousMiam,
                                                                             "4f854707-91bf-4fa0-98ec-893ae0025cae"));
    }

    @Test
    public void testDownloadAndUploadDocumentWithoutConfidential() throws Exception {
        when(systemUserService.getSysUserToken()).thenReturn(USER_TOKEN);
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(caseData));
    }

    @Test
    public void testDownloadAndUploadDocumentWithoutConfidential1() throws Exception {
        when(systemUserService.getSysUserToken()).thenReturn(USER_TOKEN);
        assertNotNull(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(caseDataMiamAttendanceReason));
    }







}
