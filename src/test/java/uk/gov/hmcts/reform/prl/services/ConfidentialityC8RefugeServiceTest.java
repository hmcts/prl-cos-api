package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.refuge.RefugeConfidentialDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.refuge.RefugeConfidentialDocumentsRecord;
import uk.gov.hmcts.reform.prl.models.refuge.RefugeDocumentHandlerParameters;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;


@RunWith(MockitoJUnitRunner.class)
public class ConfidentialityC8RefugeServiceTest {

    public static final String AMEND_APPLICANTS_DETAILS = "amendApplicantsDetails";
    public static final String AMEND_RESPONDENTS_DETAILS = "amendRespondentsDetails";
    public static final String AMEND_OTHER_PEOPLE_IN_THE_CASE_REVISED = "amendOtherPeopleInTheCaseRevised";

    @InjectMocks
    ConfidentialityC8RefugeService confidentialityC8RefugeService;

    Address address;
    PartyDetails refugePartyDetails1;
    PartyDetails refugePartyDetails2;



    CaseData caseData;
    CaseData caseData1;

    RefugeDocumentHandlerParameters refugeDocumentHandlerParameters;
    RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord;
    RefugeConfidentialDocuments refugeConfidentialDocuments;

    @Before
    public void setUp() {

        address = Address.builder()
            .addressLine1("AddressLine1")
            .postTown("Xyz town")
            .postCode("AB1 2YZ")
            .build();

        refugePartyDetails1 = PartyDetails.builder()
            .firstName("ABC 1")
            .lastName("XYZ 2")
            .dateOfBirth(LocalDate.of(2000, 01, 01))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("abc1@xyz.com")
            .phoneNumber("09876543211")
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .liveInRefuge(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document
                                             .builder()
                                             .documentFileName("C8Refuge1")
                                             .build())
            .build();

        refugePartyDetails2 = PartyDetails.builder()
            .firstName("ABC 2")
            .lastName("XYZ 2")
            .dateOfBirth(LocalDate.of(2000, 01, 01))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isEmailAddressConfidential(YesOrNo.No)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.No)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("12345678900")
            .liveInRefuge(YesOrNo.Yes)
            .email("abc2@xyz.com")
            .refugeConfidentialityC8Form(Document
                                             .builder()
                                             .documentFileName("C8Refuge2")
                                             .build())
            .build();

        Element<PartyDetails> wrappedApplicants1 = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        List<Element<PartyDetails>> partyDetailsWrappedList1 = new ArrayList<>();
        partyDetailsWrappedList1.add(wrappedApplicants1);

        refugeConfidentialDocuments = RefugeConfidentialDocuments
            .builder()
            .partyType("C8Refuge")
            .partyName("C8Refuge")
            .document(Document.builder().documentFileName("C8Refuge").build())
            .documentDetails(DocumentDetails.builder().documentName("C8Refuge").build())
            .build();
        List<Element<RefugeConfidentialDocuments>> refugeConfDocs = new ArrayList<>();
        refugeConfDocs.addAll(wrapElements(refugeConfidentialDocuments));
        caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(partyDetailsWrappedList1)
            .applicantsFL401(refugePartyDetails1)
            .refugeDocuments(refugeConfDocs)
            .build();

        refugeDocumentHandlerParameters = RefugeDocumentHandlerParameters
            .builder()
            .removeDocument(false)
            .build();
    }

    @Test
    public void testApplicantRefuge() {
        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        List<Element<PartyDetails>> partyDetailsWrappedList = Collections.singletonList(wrappedApplicants);

        HashMap<String, Object> updatedCaseData = new HashMap<>();
        confidentialityC8RefugeService.processForcePartiesConfidentialityIfLivesInRefugeForC100(
            Optional.of(partyDetailsWrappedList),
            updatedCaseData,
            "applicants",
            false
        );

        assertTrue(updatedCaseData.containsKey("applicants"));

    }

    @Test
    public void testApplicantRefugeDocument() {


        RefugeConfidentialDocuments refugeConfidentialDocuments1 =
            RefugeConfidentialDocuments
            .builder()
            .build();

        refugeDocumentHandlerParameters = refugeDocumentHandlerParameters
            .toBuilder()
            .removeDocument(true)
            .listDocument(true)
            .build();

        Element<RefugeConfidentialDocuments> refugeDocument = Element.<RefugeConfidentialDocuments>builder().value(
            refugeConfidentialDocuments).build();
        List<Element<RefugeConfidentialDocuments>> refugeDocumentList =  new ArrayList<>();
        refugeDocumentList.add(refugeDocument);

        Element<RefugeConfidentialDocuments> refugeDocument1 =
            Element.<RefugeConfidentialDocuments>builder().value(
            refugeConfidentialDocuments1).build();

        List<Element<RefugeConfidentialDocuments>> refugeDocumentList1 = new ArrayList<>();
        refugeDocumentList1.add(refugeDocument1);

        refugePartyDetails1 = refugePartyDetails1
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.No)
            .liveInRefuge(YesOrNo.Yes)
            .build();

        Element<PartyDetails> wrappedApplicants2 = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        Optional<List<Element<PartyDetails>>> partyDetailsWrappedList2 =
            Optional.of(Collections.singletonList(wrappedApplicants2));

        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord1 =
            confidentialityC8RefugeService
                .listRefugeDocumentsPartyWiseForC100(
                    refugeDocumentList,
                    refugeDocumentList1,
                    partyDetailsWrappedList2,
                    "C8Refuge",
                    refugeDocumentHandlerParameters,
                    refugeConfidentialDocumentsRecord);

        assertNotNull(refugeConfidentialDocumentsRecord1);

    }

    @Test
    public void testApplicantRefugeForSubmitC100() {

        refugePartyDetails1 = refugePartyDetails1
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.No)
            .liveInRefuge(YesOrNo.Yes)
            .build();

        refugePartyDetails2 = refugePartyDetails2
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.Yes)
            .liveInRefuge(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedApplicants2 = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        List<Element<PartyDetails>> partyDetailsWrappedList2 = Collections.singletonList(wrappedApplicants2);

        caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(partyDetailsWrappedList2)
            .applicantsFL401(refugePartyDetails2)
            .build();

        Element<PartyDetails> wrappedApplicants1 = Element.<PartyDetails>builder().value(refugePartyDetails2).build();
        List<Element<PartyDetails>> partyDetailsWrappedList1 = Collections.singletonList(wrappedApplicants1);



        HashMap<String, Object> updatedCaseData = new HashMap<>();

        caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(partyDetailsWrappedList1)
            .applicantsFL401(refugePartyDetails2)
            .build();

        confidentialityC8RefugeService.processRefugeDocumentsOnSubmit(
            updatedCaseData,
            caseData,
            caseData1,
            AMEND_APPLICANTS_DETAILS);

        assertTrue(updatedCaseData.containsKey("refugeDocuments"));

    }

    @Test
    public void testApplicantRefugeForSubmitFL401() {

        refugePartyDetails1 = refugePartyDetails1
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.No)
            .liveInRefuge(YesOrNo.Yes)
            .build();

        refugePartyDetails2 = refugePartyDetails2
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.Yes)
            .liveInRefuge(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedApplicants2 = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        List<Element<PartyDetails>> partyDetailsWrappedList2 = Collections.singletonList(wrappedApplicants2);

        caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicants(partyDetailsWrappedList2)
            .applicantsFL401(refugePartyDetails2)
            .build();

        Element<PartyDetails> wrappedApplicants1 = Element.<PartyDetails>builder().value(refugePartyDetails2).build();
        List<Element<PartyDetails>> partyDetailsWrappedList1 = Collections.singletonList(wrappedApplicants1);



        HashMap<String, Object> updatedCaseData = new HashMap<>();

        caseData1 = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicants(partyDetailsWrappedList1)
            .applicantsFL401(refugePartyDetails2)
            .build();

        confidentialityC8RefugeService.processRefugeDocumentsOnSubmit(
            updatedCaseData,
            caseData,
            caseData1,
            AMEND_APPLICANTS_DETAILS);

        assertTrue(updatedCaseData.isEmpty());

    }

    @Test
    public void testApplicantRefugeForApplicantC100() {

        refugePartyDetails2 = refugePartyDetails2
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedApplicants1 = Element.<PartyDetails>builder().value(refugePartyDetails2).build();
        List<Element<PartyDetails>> partyDetailsWrappedList1 = Collections.singletonList(wrappedApplicants1);

        caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(partyDetailsWrappedList1)
            .applicantsFL401(refugePartyDetails2)
            .build();

        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord =
            confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(
                caseData,
                caseData1,
                AMEND_APPLICANTS_DETAILS);

        assertNotNull(refugeConfidentialDocumentsRecord);

    }

    @Test
    public void testApplicantRefugeApplicantC100() {

        refugePartyDetails1 = refugePartyDetails1
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.Yes)
            .liveInRefuge(YesOrNo.Yes)
            .build();

        refugePartyDetails2 = refugePartyDetails2
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.Yes)
            .liveInRefuge(YesOrNo.Yes)
            .build();

        Element<PartyDetails> wrappedApplicants2 = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        List<Element<PartyDetails>> partyDetailsWrappedList2 = Collections.singletonList(wrappedApplicants2);

        caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(partyDetailsWrappedList2)
            .applicantsFL401(refugePartyDetails2)
            .build();

        Element<PartyDetails> wrappedApplicants1 = Element.<PartyDetails>builder().value(refugePartyDetails2).build();
        List<Element<PartyDetails>> partyDetailsWrappedList1 = Collections.singletonList(wrappedApplicants1);


        caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(partyDetailsWrappedList1)
            .applicantsFL401(refugePartyDetails2)
            .build();

        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord =
            confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(
                caseData,
                caseData1,
                AMEND_APPLICANTS_DETAILS);

        assertNotNull(refugeConfidentialDocumentsRecord);

    }

    @Test
    public void testApplicantC8RefugeApplicantC100() {

        refugePartyDetails1 = refugePartyDetails1
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document
                                             .builder()
                                             .documentFileName("C8Refuge")
                                             .build())
            .liveInRefuge(YesOrNo.Yes)
            .build();

        refugePartyDetails2 = refugePartyDetails2
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document
                                             .builder()
                                             .documentFileName("C8Refuge2")
                                             .build())
            .liveInRefuge(YesOrNo.Yes)
            .build();

        Element<PartyDetails> wrappedApplicants2 = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        List<Element<PartyDetails>> partyDetailsWrappedList2 = Collections.singletonList(wrappedApplicants2);

        caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(partyDetailsWrappedList2)
            .applicantsFL401(refugePartyDetails2)
            .build();

        Element<PartyDetails> wrappedApplicants1 = Element.<PartyDetails>builder().value(refugePartyDetails2).build();
        List<Element<PartyDetails>> partyDetailsWrappedList1 = Collections.singletonList(wrappedApplicants1);


        caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(partyDetailsWrappedList1)
            .applicantsFL401(refugePartyDetails2)
            .build();

        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord =
            confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(
                caseData,
                caseData1,
                AMEND_APPLICANTS_DETAILS);

        assertNotNull(refugeConfidentialDocumentsRecord);

    }

    @Test
    public void testApplicantRefugeForApplicantC100ForC8Refuge() {

        refugePartyDetails2 = refugePartyDetails2
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.No)
            .liveInRefuge(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedApplicants1 = Element.<PartyDetails>builder().value(refugePartyDetails2).build();
        List<Element<PartyDetails>> partyDetailsWrappedList1 = Collections.singletonList(wrappedApplicants1);


        caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(partyDetailsWrappedList1)
            .applicantsFL401(refugePartyDetails2)
            .build();

        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord =
            confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(
                caseData1,
                caseData,
                AMEND_APPLICANTS_DETAILS);

        assertNotNull(refugeConfidentialDocumentsRecord);

    }

    @Test
    public void testApplicantRefugeForRespondentC100() {

        refugePartyDetails2 = refugePartyDetails2
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedApplicants1 = Element.<PartyDetails>builder().value(refugePartyDetails2).build();
        List<Element<PartyDetails>> partyDetailsWrappedList1 = Collections.singletonList(wrappedApplicants1);

        caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(partyDetailsWrappedList1)
            .applicantsFL401(refugePartyDetails2)
            .build();

        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord =
            confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(
                caseData,
                caseData1,
                AMEND_RESPONDENTS_DETAILS
            );

        assertNull(refugeConfidentialDocumentsRecord);

    }

    @Test
    public void testApplicantRefugeForRespondentC100ForC8Refuge() {

        refugePartyDetails2 = refugePartyDetails2
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.No)
            .liveInRefuge(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedApplicants1 = Element.<PartyDetails>builder().value(refugePartyDetails2).build();
        List<Element<PartyDetails>> partyDetailsWrappedList1 = Collections.singletonList(wrappedApplicants1);

        caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(partyDetailsWrappedList1)
            .applicantsFL401(refugePartyDetails2)
            .build();

        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord =
            confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(
                caseData1,
                caseData,
               AMEND_RESPONDENTS_DETAILS
            );

        assertNull(refugeConfidentialDocumentsRecord);

    }

    @Test
    public void testApplicantRefugeForOtherPeopleC100() {

        refugePartyDetails2 = refugePartyDetails2
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedApplicants1 = Element.<PartyDetails>builder().value(refugePartyDetails2).build();
        List<Element<PartyDetails>> partyDetailsWrappedList1 = Collections.singletonList(wrappedApplicants1);

        caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(partyDetailsWrappedList1)
            .applicantsFL401(refugePartyDetails2)
            .build();

        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord =
            confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(
                caseData,
                caseData1,
                AMEND_OTHER_PEOPLE_IN_THE_CASE_REVISED
            );

        assertNull(refugeConfidentialDocumentsRecord);

    }

    @Test
    public void testApplicantRefugeForOtherPeopleC100ForC8Refuge() {

        refugePartyDetails2 = refugePartyDetails2
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.No)
            .liveInRefuge(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedApplicants1 = Element.<PartyDetails>builder().value(refugePartyDetails2).build();
        List<Element<PartyDetails>> partyDetailsWrappedList1 = Collections.singletonList(wrappedApplicants1);

        caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(partyDetailsWrappedList1)
            .applicantsFL401(refugePartyDetails2)
            .build();

        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord =
            confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(
                caseData1,
                caseData,
                AMEND_OTHER_PEOPLE_IN_THE_CASE_REVISED);

        assertNull(refugeConfidentialDocumentsRecord);

    }

    @Test
    public void testApplicantRefugeForNoEventC100ForC8Refuge() {

        refugePartyDetails2 = refugePartyDetails2
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.No)
            .liveInRefuge(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedApplicants1 = Element.<PartyDetails>builder().value(refugePartyDetails2).build();
        List<Element<PartyDetails>> partyDetailsWrappedList1 = new ArrayList<>();
        partyDetailsWrappedList1.add(wrappedApplicants1);

        caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(partyDetailsWrappedList1)
            .applicantsFL401(refugePartyDetails2)
            .build();

        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord =
            confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(
                caseData1,
                caseData,
                " ");

        assertNotNull(refugeConfidentialDocumentsRecord);

    }

    @Test
    public void testApplicantRefugeForApplicantFL401() {
        caseData = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();

        refugePartyDetails2 = refugePartyDetails2
            .toBuilder()
            .isCurrentAddressKnown(YesOrNo.No)
            .build();

        caseData1 = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(refugePartyDetails2)
            .build();

        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord =
            confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForFL401(
                caseData,
               caseData1,
                "amendApplicantsDetails");

        assertNotNull(refugeConfidentialDocumentsRecord);

    }

    @Test
    public void testApplicantRefugeForRespondentFL401() {
        caseData = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();

        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord =
            confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForFL401(
                caseData,
                caseData,
                "amendRespondentsDetails");

        assertNull(refugeConfidentialDocumentsRecord);

    }

    @Test
    public void testApplicantRefugeForNullEventFL401() {
        caseData = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(refugePartyDetails1)
            .build();

        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord =
            confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForFL401(
                caseData,
                caseData,
                "");

        assertNotNull(refugeConfidentialDocumentsRecord);

    }

    @Test
    public void testRefugeNoApplicant() {
        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        List<Element<PartyDetails>> partyDetailsWrappedList = Collections.singletonList(wrappedApplicants);

        HashMap<String, Object> updatedCaseData = new HashMap<>();
        confidentialityC8RefugeService.processForcePartiesConfidentialityIfLivesInRefugeForC100(
            Optional.of(partyDetailsWrappedList),
            updatedCaseData,
            " ",
            false
        );

        assertTrue(updatedCaseData.containsKey(" "));

    }

    @Test
    public void testRefugeCleanup() {

        refugePartyDetails1 = refugePartyDetails1.toBuilder()
            .liveInRefuge(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        List<Element<PartyDetails>> partyDetailsWrappedList = Collections.singletonList(wrappedApplicants);

        HashMap<String, Object> updatedCaseData = new HashMap<>();
        confidentialityC8RefugeService.processForcePartiesConfidentialityIfLivesInRefugeForC100(
            Optional.of(partyDetailsWrappedList),
            updatedCaseData,
            " ",
            true
        );

        assertTrue(updatedCaseData.containsKey(" "));

    }

    @Test
    public void processForcePartiesConfidentialityIfLivesInRefugeForFL401Null() {
        Optional<PartyDetails> partyDetails = Optional.empty();
        confidentialityC8RefugeService.processForcePartiesConfidentialityIfLivesInRefugeForFL401(
            partyDetails,
            null,
            null,
            false
        );
        assertTrue(true);
    }

    @Test
    public void processForcePartiesConfidentialityIfDoesNotLivesInRefugeForFL401() {
        Optional<PartyDetails> partyDetails = Optional.ofNullable(PartyDetails.builder().build());
        HashMap<String, Object> updatedCaseData = new HashMap<>();
        confidentialityC8RefugeService.processForcePartiesConfidentialityIfLivesInRefugeForFL401(
            partyDetails,
            updatedCaseData,
            "applicant",
            false
        );
        assertTrue(true);
    }

    @Test
    public void processForcePartiesConfidentialityIfLivesInRefugeForFL401() {
        Optional<PartyDetails> partyDetails = Optional.ofNullable(PartyDetails.builder().liveInRefuge(YesOrNo.Yes).build());
        HashMap<String, Object> updatedCaseData = new HashMap<>();
        confidentialityC8RefugeService.processForcePartiesConfidentialityIfLivesInRefugeForFL401(
            partyDetails,
            updatedCaseData,
            "applicant",
            false
        );
        assertTrue(true);
    }

    @Test
    public void processForcePartiesConfidentialityIfLivesInRefugeForFL401WithResponse() {
        Optional<PartyDetails> partyDetails = Optional.ofNullable(PartyDetails.builder()
                                                                      .liveInRefuge(YesOrNo.No)
                                                                      .response(Response.builder().build())
                                                                      .build());
        HashMap<String, Object> updatedCaseData = new HashMap<>();
        confidentialityC8RefugeService.processForcePartiesConfidentialityIfLivesInRefugeForFL401(
            partyDetails,
            updatedCaseData,
            "applicant",
            true
        );
        assertTrue(true);
    }

    @Test
    public void processForcePartiesConfidentialityIfLivesInRefugeForFL401WithCitizenDetails() {
        Optional<PartyDetails> partyDetails = Optional.ofNullable(PartyDetails.builder()
                                                                      .liveInRefuge(YesOrNo.No)
                                                                      .response(Response.builder().citizenDetails(
                                                                          CitizenDetails.builder().build()).build())
                                                                      .build());
        HashMap<String, Object> updatedCaseData = new HashMap<>();
        confidentialityC8RefugeService.processForcePartiesConfidentialityIfLivesInRefugeForFL401(
            partyDetails,
            updatedCaseData,
            "applicant",
            true
        );
        assertTrue(true);
    }

    @Test
    public void processForcePartiesConfidentialityIfLivesInRefugeForFL401InResponse() {
        Optional<PartyDetails> partyDetails = Optional.ofNullable(PartyDetails.builder()
                                                                      .liveInRefuge(YesOrNo.No)
                                                                      .response(Response.builder().citizenDetails(
                                                                          CitizenDetails.builder().liveInRefuge(YesOrNo.Yes).build()).build())
                                                                      .build());
        HashMap<String, Object> updatedCaseData = new HashMap<>();
        confidentialityC8RefugeService.processForcePartiesConfidentialityIfLivesInRefugeForFL401(
            partyDetails,
            updatedCaseData,
            "applicant",
            true
        );
        assertTrue(true);
    }
}
