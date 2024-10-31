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
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class ConfidentialityC8RefugeServiceTest {

    @InjectMocks
    ConfidentialityC8RefugeService confidentialityC8RefugeService;

    Address address;
    PartyDetails refugePartyDetails1;
    PartyDetails refugePartyDetails2;

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
            .isCurrentAddressKnown(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("12345678900")
            .liveInRefuge(YesOrNo.Yes)
            .email("abc2@xyz.com")
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
    public void testRefugeCleanupWithoutAddress() {

        refugePartyDetails1 = refugePartyDetails1.toBuilder()
            .liveInRefuge(YesOrNo.No)
            .isCurrentAddressKnown(YesOrNo.No)
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
    public void testRefugeCleanupFalseWithoutAddress() {

        refugePartyDetails1 = refugePartyDetails1.toBuilder()
            .liveInRefuge(YesOrNo.No)
            .isCurrentAddressKnown(YesOrNo.No)
            .build();
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
    public void processForcePartiesConfidentialityIfLivesInRefugeForFL401WithKnownAddress() {
        Optional<PartyDetails> partyDetails = Optional.ofNullable(PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build());
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
    public void processForcePartiesConfidentialityIfLivesInRefugeForFL401WithKnownAddressCleanUpFalse() {
        Optional<PartyDetails> partyDetails = Optional.ofNullable(PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.No)
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
    public void testListRefugeDocumentsForConfidentialTabC100() {
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        CaseData caseData = CaseData
            .builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicantList)
            .build();
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters = RefugeDocumentHandlerParameters
            .builder().forAllParties(true)
            .build();
        RefugeConfidentialDocuments refugeConfidentialDocuments = RefugeConfidentialDocuments.builder().build();
        Element<RefugeConfidentialDocuments> element = Element.<RefugeConfidentialDocuments>builder().value(refugeConfidentialDocuments).build();
        List<Element<RefugeConfidentialDocuments>> list = new ArrayList<>();
        list.add(element);
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord = new RefugeConfidentialDocumentsRecord(list, list);
        RefugeConfidentialDocumentsRecord returnedRefuge = confidentialityC8RefugeService
            .listRefugeDocumentsForConfidentialTab(caseData, refugeDocumentHandlerParameters, refugeConfidentialDocumentsRecord);
        assertNotNull(returnedRefuge);
    }

    @Test
    public void testListRefugeDocumentsForConfidentialTabFL401() {
        CaseData caseData = CaseData
            .builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters = new RefugeDocumentHandlerParameters();
        RefugeConfidentialDocuments refugeConfidentialDocuments = RefugeConfidentialDocuments.builder().build();
        Element<RefugeConfidentialDocuments> element = Element.<RefugeConfidentialDocuments>builder().value(refugeConfidentialDocuments).build();
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord
            = new RefugeConfidentialDocumentsRecord(List.of(element), List.of(element));
        RefugeConfidentialDocumentsRecord returnedRefuge = confidentialityC8RefugeService
            .listRefugeDocumentsForConfidentialTab(caseData, refugeDocumentHandlerParameters, refugeConfidentialDocumentsRecord);
        assertNotNull(returnedRefuge);
    }

    @Test
    public void testListRefugeDocumentsForConfidentialTabNoCaseType() {
        CaseData caseData = CaseData
            .builder()
            .build();
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters = new RefugeDocumentHandlerParameters();
        RefugeConfidentialDocuments refugeConfidentialDocuments = RefugeConfidentialDocuments.builder().build();
        Element<RefugeConfidentialDocuments> element = Element.<RefugeConfidentialDocuments>builder().value(refugeConfidentialDocuments).build();
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord
            = new RefugeConfidentialDocumentsRecord(List.of(element), List.of(element));
        RefugeConfidentialDocumentsRecord returnedRefuge = confidentialityC8RefugeService
            .listRefugeDocumentsForConfidentialTab(caseData, refugeDocumentHandlerParameters, refugeConfidentialDocumentsRecord);

        assertNotNull(returnedRefuge);
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
