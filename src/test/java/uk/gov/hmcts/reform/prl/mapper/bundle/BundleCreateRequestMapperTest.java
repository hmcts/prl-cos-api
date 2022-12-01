package uk.gov.hmcts.reform.prl.mapper.bundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.OtherPersonConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingInformation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRUG_AND_ALCOHOL_TESTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LETTERS_FROM_SCHOOL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MAIL_SCREENSHOTS_MEDIA_FILES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_RECORDS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PATERNITY_TEST_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POLICE_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class BundleCreateRequestMapperTest {
    @InjectMocks
    private BundleCreateRequestMapper bundleCreateRequestMapper;

    @Test
    public void testBundleCreateRequestMapper() {
        List<ContactInformation> contactInformationList = Collections.singletonList(ContactInformation.builder()
            .addressLine1("29, SEATON DRIVE")
            .addressLine2("test line")
            .townCity("NORTHAMPTON")
            .postCode("NN3 9SS")
            .build());

        Organisations organisations = Organisations.builder()
            .organisationIdentifier("79ZRSOU")
            .name("Civil - Organisation 2")
            .contactInformation(contactInformationList)
            .build();


        PartyDetails partyDetailsWithOrganisations = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .isAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .isEmailAddressConfidential(Yes)
            .solicitorOrg(Organisation.builder()
                .organisationID("79ZRSOU")
                .organisationName("Civil - Organisation 2")
                .build())
            .organisations(organisations)
            .build();

        Element<PartyDetails> applicants = Element.<PartyDetails>builder().value(partyDetailsWithOrganisations).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(applicants);

        ApplicantConfidentialityDetails applicantConfidentialityDetails = ApplicantConfidentialityDetails.builder()
            .phoneNumber("1234567890")
            .firstName("UserFirst")
            .lastName("UserLast")
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .email("test@confidential.com")
            .build();

        Element<ApplicantConfidentialityDetails> applicantConfidential = Element.<ApplicantConfidentialityDetails>builder()
            .value(applicantConfidentialityDetails).build();
        List<Element<ApplicantConfidentialityDetails>> applicantConfidentialList = Collections.singletonList(
            applicantConfidential);

        OtherPersonConfidentialityDetails otherPersonConfidentialityDetails = uk.gov.hmcts.reform.prl.models.complextypes.confidentiality
            .OtherPersonConfidentialityDetails.builder()
            .isPersonIdentityConfidential(Yes)
            .firstName("test1")
            .lastName("last1")
            .relationshipToChildDetails("uncle")
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .build();

        Element<OtherPersonConfidentialityDetails> otherPerson = Element.<OtherPersonConfidentialityDetails>builder()
            .value(otherPersonConfidentialityDetails).build();
        List<Element<OtherPersonConfidentialityDetails>> otherPersonList = Collections.singletonList(otherPerson);


        ChildConfidentialityDetails childConfidentialityDetails = ChildConfidentialityDetails.builder()
            .firstName("ChildFirst")
            .lastName("ChildLast")
            .otherPerson(otherPersonList)
            .build();

        Element<ChildConfidentialityDetails> childConfidential = Element.<ChildConfidentialityDetails>builder()
            .value(childConfidentialityDetails).build();
        List<Element<ChildConfidentialityDetails>> childConfidentialList = Collections.singletonList(childConfidential);
        List<FurtherEvidence> furtherEvidences = new ArrayList<>();
        furtherEvidences.add(FurtherEvidence.builder().typeOfDocumentFurtherEvidence(FurtherEvidenceDocumentType.consentOrder)
        List<FurtherEvidence> furtherEvidences = new ArrayList<>();
        furtherEvidences.add(FurtherEvidence.builder().typeOfDocumentFurtherEvidence(FurtherEvidenceDocumentType.miamCertificate)
            .documentFurtherEvidence(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Sample1.pdf").build())
            .restrictCheckboxFurtherEvidence(new ArrayList<>()).build());

        List<OtherDocuments> otherDocuments = new ArrayList<>();
        otherDocuments.add(OtherDocuments.builder().documentName("Application docu")
            .documentOther(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Sample2.pdf").build()).documentTypeOther(
                DocTypeOtherDocumentsEnum.applicantStatement).restrictCheckboxOtherDocuments(new ArrayList<>()).build());

        List<OrderDetails> orders = new ArrayList<>();
        orders.add(OrderDetails.builder().orderType("orders")
            .orderDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Order.pdf").build()).build());

        List<ResponseDocuments> citizenC7uploadedDocs = new ArrayList<>();
        citizenC7uploadedDocs.add(ResponseDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("C7Document.pdf").build()).build());

        List<UploadedDocuments> uploadedDocuments = new ArrayList<>();
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("PositionStatement.pdf").build())
            .documentType(YOUR_POSITION_STATEMENTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("PositionStatement.pdf").build())
            .documentType(YOUR_POSITION_STATEMENTS).isApplicant("Yes").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("WitnessStatement.pdf").build())
            .documentType(YOUR_WITNESS_STATEMENTS).isApplicant("Yes").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("WitnessStatement.pdf").build())
            .documentType(YOUR_WITNESS_STATEMENTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("LettersFromSchool.pdf").build())
            .documentType(LETTERS_FROM_SCHOOL).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("LettersFromSchool.pdf").build())
            .documentType(EXPERT_REPORTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("CAFCASSReports.pdf").build())
            .documentType(CAFCASS_REPORTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("OtherWitnessDocuments.pdf").build())
            .documentType(OTHER_WITNESS_STATEMENTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("MedicalRecords.pdf").build())
            .documentType(MEDICAL_RECORDS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("MedicalReports.pdf").build())
            .documentType(MEDICAL_REPORTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("PaternityReports.pdf").build())
            .documentType(PATERNITY_TEST_REPORTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("DrugAndAlchol.pdf").build())
            .documentType(DRUG_AND_ALCOHOL_TESTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("PoliceReports.pdf").build())
            .documentType(POLICE_REPORTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("MediaScreenshots.pdf").build())
            .documentType(MAIL_SCREENSHOTS_MEDIA_FILES).isApplicant("No").build());
        //uploadedDocuments.add(uploadedDocuments);
        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .languagePreferenceWelsh(No)
        CaseData caseData = CaseData.builder()
            .id(123456789123L)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(No).build())
            .applicants(listOfApplicants)
            .state(State.GATEKEEPING)
            //.allegationsOfHarmYesNo(No)
            .applicantsConfidentialDetails(applicantConfidentialList)
            .childrenConfidentialDetails(childConfidentialList)
            .otherDocuments(ElementUtils.wrapElements(otherDocuments))
            .furtherEvidences(ElementUtils.wrapElements(furtherEvidences))
            .build();

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(caseData,"sample.yaml");
            .state(State.CASE_HEARING)
            .finalDocument(Document.builder().documentFileName("C100AppDoc").documentUrl("Url").build())
            .c1ADocument(Document.builder().documentFileName("c1ADocument").documentUrl("Url").build())
            .otherDocuments(ElementUtils.wrapElements(otherDocuments))
            .furtherEvidences(ElementUtils.wrapElements(furtherEvidences))
            .orderCollection(ElementUtils.wrapElements(orders))
            .bundleInformation(BundlingInformation.builder().build())
            .citizenResponseC7DocumentList(ElementUtils.wrapElements(citizenC7uploadedDocs))
            .citizenUploadedDocumentList(ElementUtils.wrapElements(uploadedDocuments))
            .build();

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(c100CaseData,"eventI","sample.yaml");
        assertNotNull(bundleCreateRequest);
    }
}
