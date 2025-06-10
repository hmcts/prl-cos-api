package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

    PartyDetails refugePartyDetails1NotConfidential;

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

        refugePartyDetails1NotConfidential = PartyDetails.builder()
            .firstName("ABC 1")
            .lastName("XYZ 2")
            .dateOfBirth(LocalDate.of(2000, 01, 01))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("abc1@xyz.com")
            .phoneNumber("09876543211")
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
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
        RefugeConfidentialDocuments refugeConfidentialDocuments = RefugeConfidentialDocuments.builder().partyType(
            "Applicant").build();
        Element<RefugeConfidentialDocuments> element = Element.<RefugeConfidentialDocuments>builder().value(
            refugeConfidentialDocuments).build();
        List<Element<RefugeConfidentialDocuments>> list = new ArrayList<>();
        list.add(element);
        CaseData caseData = CaseData
            .builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .refugeDocuments(list)
            .applicants(applicantList)
            .build();
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters = RefugeDocumentHandlerParameters
            .builder().forAllParties(true)
            .removeDocument(true)
            .build();
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord = new RefugeConfidentialDocumentsRecord(
            list,
            list
        );
        RefugeConfidentialDocumentsRecord returnedRefuge = confidentialityC8RefugeService
            .listRefugeDocumentsForConfidentialTab(
                caseData,
                applicant,
                0,
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord
            );
        assertNotNull(returnedRefuge);
    }

    @Test
    public void testListRefugeDocumentsForConfidentialTabFL401() {
        CaseData caseData = CaseData
            .builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(PartyDetails.builder()
                                 .liveInRefuge(YesOrNo.Yes)
                                 .refugeConfidentialityC8Form(Document
                                                                  .builder()
                                                                  .build())
                                 .build())
            .build();
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters = RefugeDocumentHandlerParameters.builder().forAllParties(
                true)
            .listDocument(true).build();
        RefugeConfidentialDocuments refugeConfidentialDocuments = RefugeConfidentialDocuments.builder().build();
        Element<RefugeConfidentialDocuments> element = Element.<RefugeConfidentialDocuments>builder().value(
            refugeConfidentialDocuments).build();
        List<Element<RefugeConfidentialDocuments>> list = new ArrayList<>();
        list.add(element);
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord
            = new RefugeConfidentialDocumentsRecord(list, list);
        RefugeConfidentialDocumentsRecord returnedRefuge = confidentialityC8RefugeService
            .listRefugeDocumentsForConfidentialTab(caseData, caseData.getApplicantsFL401(), 0,
                                                   refugeDocumentHandlerParameters, refugeConfidentialDocumentsRecord
            );
        assertNotNull(returnedRefuge);
    }

    @Test
    public void testListRefugeDocumentsForConfidentialTabNoCaseType() {
        CaseData caseData = CaseData
            .builder()
            .build();

        PartyDetails partyDetails = PartyDetails
            .builder()
            .build();
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters = new RefugeDocumentHandlerParameters();
        RefugeConfidentialDocuments refugeConfidentialDocuments = RefugeConfidentialDocuments.builder().build();
        Element<RefugeConfidentialDocuments> element = Element.<RefugeConfidentialDocuments>builder().value(
            refugeConfidentialDocuments).build();
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord
            = new RefugeConfidentialDocumentsRecord(List.of(element), List.of(element));
        RefugeConfidentialDocumentsRecord returnedRefuge = confidentialityC8RefugeService
            .listRefugeDocumentsForConfidentialTab(
                caseData,
                partyDetails,
                0,
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord
            );
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

    @Test
    public void processC8RefugeDocumentsOnAmendForC100WithEmptyData() {
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        CaseData caseDataBefore = CaseData.builder().build();
        CaseData caseData = CaseData
            .builder()
            .applicants(applicantList)
            .build();
        assertEquals(
            new RefugeConfidentialDocumentsRecord(List.of(), List.of()),
            confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(caseDataBefore, caseData, "")
        );
    }

    @Test
    public void processC8RefugeDocumentsOnAmendForC100OnSubmit() {
        CaseData caseDataBefore = CaseData.builder().build();
        CaseData caseData = CaseData.builder().build();
        assertEquals(
            new RefugeConfidentialDocumentsRecord(List.of(), List.of()),
            confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(caseDataBefore, caseData, "")
        );
    }

    @Test
    public void processC8RefugeDocumentsOnAmendForC100WithForApplicant() {
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        CaseData caseDataBefore = CaseData
            .builder()
            .applicants(applicantList)
            .build();
        CaseData caseData = CaseData
            .builder()
            .applicants(applicantList)
            .build();
        assertNull(confidentialityC8RefugeService
                       .processC8RefugeDocumentsOnAmendForC100(
                           caseDataBefore,
                           caseData,
                           CaseEvent.AMEND_APPLICANTS_DETAILS.getValue()
                       ));
    }

    @Test
    public void processC8RefugeDocumentsOnAmendForFL401ForApplicant() {
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        CaseData caseDataBefore = CaseData
            .builder()
            .applicantsFL401(applicant)
            .build();
        CaseData caseData = CaseData
            .builder()
            .applicantsFL401(applicant)
            .build();
        assertNull(confidentialityC8RefugeService
                       .processC8RefugeDocumentsOnAmendForFL401(
                           caseDataBefore,
                           caseData,
                           CaseEvent.AMEND_APPLICANTS_DETAILS.getValue()
                       ));
    }

    @Test
    public void processC8RefugeDocumentsOnAmendForFL401ForRespondent() {
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        CaseData caseDataBefore = CaseData
            .builder()
            .respondentsFL401(applicant)
            .build();
        CaseData caseData = CaseData
            .builder()
            .respondentsFL401(applicant)
            .build();
        assertNull(confidentialityC8RefugeService
                       .processC8RefugeDocumentsOnAmendForFL401(
                           caseDataBefore,
                           caseData,
                           CaseEvent.AMEND_RESPONDENTS_DETAILS.getValue()
                       ));
    }

    @Test
    public void processC8RefugeDocumentsOnAmendForC100WithForRespondent() {
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        CaseData caseDataBefore = CaseData
            .builder()
            .applicants(applicantList)
            .build();
        CaseData caseData = CaseData
            .builder()
            .applicants(applicantList)
            .build();
        assertNull(confidentialityC8RefugeService
                       .processC8RefugeDocumentsOnAmendForC100(
                           caseDataBefore,
                           caseData,
                           CaseEvent.AMEND_RESPONDENTS_DETAILS.getValue()
                       ));
    }

    @Test
    public void processC8RefugeDocumentsOnAmendForC100WithForOtherPeople() {
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        CaseData caseDataBefore = CaseData
            .builder()
            .applicants(applicantList)
            .build();
        CaseData caseData = CaseData
            .builder()
            .applicants(applicantList)
            .build();
        assertNull(confidentialityC8RefugeService
                       .processC8RefugeDocumentsOnAmendForC100(
                           caseDataBefore,
                           caseData,
                           CaseEvent.AMEND_OTHER_PEOPLE_IN_THE_CASE_REVISED.getValue()
                       ));
    }

    @Test
    public void processC8RefugeDocumentsOnAmendForC100WithForApplicantAddressIsKnown() {
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        CaseData caseDataBefore = CaseData
            .builder()
            .applicants(applicantList)
            .build();
        CaseData caseData = CaseData
            .builder()
            .applicants(applicantList)
            .build();
        assertNull(confidentialityC8RefugeService
                       .processC8RefugeDocumentsOnAmendForC100(
                           caseDataBefore,
                           caseData,
                           CaseEvent.AMEND_APPLICANTS_DETAILS.getValue()
                       ));
    }

    @Test
    public void processC8RefugeDocumentsOnAmendForC100WithForApplicantAddressIsKnownBefore() {
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.No)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        PartyDetails applicantBefore = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Element<PartyDetails> wrappedApplicantBefore = Element.<PartyDetails>builder().value(applicantBefore).build();
        List<Element<PartyDetails>> applicantListBefore = new ArrayList<>();
        applicantListBefore.add(wrappedApplicantBefore);
        CaseData caseDataBefore = CaseData
            .builder()
            .applicants(applicantListBefore)
            .build();
        CaseData caseData = CaseData
            .builder()
            .applicants(applicantList)
            .build();
        assertNull(confidentialityC8RefugeService
                       .processC8RefugeDocumentsOnAmendForC100(
                           caseDataBefore,
                           caseData,
                           CaseEvent.AMEND_APPLICANTS_DETAILS.getValue()
                       ));
    }

    @Test
    public void processC8RefugeDocumentsOnAmendForC100WithForApplicantRefugeHasChangedFromNoToYes() {
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        PartyDetails applicantBefore = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.No)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Element<PartyDetails> wrappedApplicantBefore = Element.<PartyDetails>builder().value(applicantBefore).build();
        List<Element<PartyDetails>> applicantListBefore = new ArrayList<>();
        applicantListBefore.add(wrappedApplicantBefore);
        CaseData caseDataBefore = CaseData
            .builder()
            .applicants(applicantListBefore)
            .build();
        CaseData caseData = CaseData
            .builder()
            .applicants(applicantList)
            .build();
        assertNull(confidentialityC8RefugeService
                       .processC8RefugeDocumentsOnAmendForC100(
                           caseDataBefore,
                           caseData,
                           CaseEvent.AMEND_APPLICANTS_DETAILS.getValue()
                       ));
    }

    @Test
    public void processC8RefugeDocumentsOnAmendForC100WithForApplicantRefugeHasChangedFromYesToNo() {
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.No)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        PartyDetails applicantBefore = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Element<PartyDetails> wrappedApplicantBefore = Element.<PartyDetails>builder().value(applicantBefore).build();
        List<Element<PartyDetails>> applicantListBefore = new ArrayList<>();
        applicantListBefore.add(wrappedApplicantBefore);
        CaseData caseDataBefore = CaseData
            .builder()
            .applicants(applicantListBefore)
            .build();
        CaseData caseData = CaseData
            .builder()
            .applicants(applicantList)
            .build();
        assertNull(confidentialityC8RefugeService
                       .processC8RefugeDocumentsOnAmendForC100(
                           caseDataBefore,
                           caseData,
                           CaseEvent.AMEND_APPLICANTS_DETAILS.getValue()
                       ));
    }

    @Test
    public void processC8RefugeDocumentsOnAmendForC100WithForApplicantRefugeDocumentIsSame() {
        Document document = Document.builder().documentFileName("test").build();
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(document)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        PartyDetails applicantBefore = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(document)
            .build();
        Element<PartyDetails> wrappedApplicantBefore = Element.<PartyDetails>builder().value(applicantBefore).build();
        List<Element<PartyDetails>> applicantListBefore = new ArrayList<>();
        applicantListBefore.add(wrappedApplicantBefore);
        CaseData caseDataBefore = CaseData
            .builder()
            .applicants(applicantListBefore)
            .build();
        CaseData caseData = CaseData
            .builder()
            .applicants(applicantList)
            .build();
        assertNull(confidentialityC8RefugeService
                       .processC8RefugeDocumentsOnAmendForC100(
                           caseDataBefore,
                           caseData,
                           CaseEvent.AMEND_APPLICANTS_DETAILS.getValue()
                       ));
    }

    @Test
    public void processRefugeDocumentsOnSubmitC100() {
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        Map<String, Object> map = new HashMap<>();
        CaseData caseData = CaseData
            .builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicantList)
            .otherPartyInTheCaseRevised(applicantList)
            .build();
        confidentialityC8RefugeService.processRefugeDocumentsOnSubmit(map, caseData);
        assertTrue(true);
    }

    @Test
    public void processRefugeDocumentsOnSubmitFL401() {
        // Todo
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Map<String, Object> map = new HashMap<>();
        CaseData caseData = CaseData
            .builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(applicant)
            .build();
        confidentialityC8RefugeService.processRefugeDocumentsOnSubmit(map, caseData);
        assertTrue(true);
    }

    @Test
    public void processRefugeDocumentsOnSubmitEmpty() {
        Map<String, Object> map = new HashMap<>();
        CaseData caseData = CaseData
            .builder()
            .build();
        confidentialityC8RefugeService.processRefugeDocumentsOnSubmit(map, caseData);
        assertTrue(true);
    }

    @Test
    public void processRefugeDocumentsOnReSubmitC100() {
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        Map<String, Object> map = new HashMap<>();
        CaseData caseData = CaseData
            .builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicantList)
            .otherPartyInTheCaseRevised(applicantList)
            .build();
        confidentialityC8RefugeService.processRefugeDocumentsOnReSubmit(map, caseData);
        assertTrue(true);
    }

    @Test
    public void processRefugeDocumentsOnReSubmitFL401() {
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        Map<String, Object> map = new HashMap<>();
        CaseData caseData = CaseData
            .builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(applicant)
            .build();
        confidentialityC8RefugeService.processRefugeDocumentsOnReSubmit(map, caseData);
        assertTrue(true);
    }

    @Test
    public void processRefugeDocumentsOnReSubmitEmpty() {
        Map<String, Object> map = new HashMap<>();
        CaseData caseData = CaseData
            .builder()
            .build();
        confidentialityC8RefugeService.processRefugeDocumentsOnReSubmit(map, caseData);
        assertTrue(true);
    }

    @Test
    public void processRefugeDocumentsC7ResponseSubmission() {
        Map<String, Object> map = new HashMap<>();
        PartyDetails applicant = PartyDetails
            .builder()
            .liveInRefuge(YesOrNo.Yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .refugeConfidentialityC8Form(Document.builder().build())
            .build();
        RefugeConfidentialDocuments refugeConfidentialDocuments = RefugeConfidentialDocuments.builder().partyType(
            "Applicant").build();
        Element<RefugeConfidentialDocuments> element = Element.<RefugeConfidentialDocuments>builder().value(
            refugeConfidentialDocuments).build();
        List<Element<RefugeConfidentialDocuments>> list = new ArrayList<>();
        list.add(element);
        confidentialityC8RefugeService.processRefugeDocumentsC7ResponseSubmission(map, applicant, list, list, 0);
        assertTrue(true);
    }

    @Test
    public void testprocessC8RefugeDocumentsOnAmendForC100WithForApplicant() {
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        Element<PartyDetails> wrappedApplicant2 = Element.<PartyDetails>builder().value(
            refugePartyDetails1NotConfidential).build();


        RefugeConfidentialDocuments refugeConfidentialDocuments = RefugeConfidentialDocuments.builder().partyType(
            "Applicant").build();
        Element<RefugeConfidentialDocuments> element = Element.<RefugeConfidentialDocuments>builder().value(
            refugeConfidentialDocuments).build();
        List<Element<RefugeConfidentialDocuments>> list = new ArrayList<>();
        list.add(element);

        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);

        List<Element<PartyDetails>> applicantList2 = new ArrayList<>();
        applicantList2.add(wrappedApplicant2);


        CaseData caseDataBefore = CaseData
            .builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .refugeDocuments(list)
            .applicants(applicantList)
            .build();
        CaseData caseData = CaseData
            .builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .refugeDocuments(list)
            .applicants(applicantList2)
            .build();


        RefugeConfidentialDocumentsRecord value = confidentialityC8RefugeService.processC8RefugeDocumentsOnAmendForC100(
            caseData,
            caseDataBefore,
            "amendApplicantsDetails"
        );
        System.out.print("value: " + value);

        assertNotNull(value);
    }

    @Test
    public void testProcessC8RefugeDocumentsChangesForC100SameApplicant() {
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        Element<PartyDetails> wrappedApplicant2 = Element.<PartyDetails>builder().value(refugePartyDetails1).build();

        List<Element<PartyDetails>> partyDetailsWrappedList = Collections.singletonList(wrappedApplicant);
        List<Element<PartyDetails>> partyDetailsWrappedList2 = Collections.singletonList(wrappedApplicant2);

        RefugeConfidentialDocuments refugeConfidentialDocuments = RefugeConfidentialDocuments.builder().partyType(
            "Applicant").build();
        Element<RefugeConfidentialDocuments> element = Element.<RefugeConfidentialDocuments>builder().value(
            refugeConfidentialDocuments).build();
        List<Element<RefugeConfidentialDocuments>> list = new ArrayList<>();
        list.add(element);

        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);

        CaseData caseData = CaseData
            .builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .refugeDocuments(list)
            .applicants(applicantList)
            .build();

        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters = new RefugeDocumentHandlerParameters();

        RefugeConfidentialDocumentsRecord result = confidentialityC8RefugeService.processC8RefugeDocumentsChangesForC100(
            caseData,
            Optional.ofNullable(partyDetailsWrappedList),
            Optional.ofNullable(partyDetailsWrappedList2),
            refugeDocumentHandlerParameters,
            null
        );
        System.out.print("result: " + result);
        assertNull(result);
    }

    @Test
    public void testProcessC8RefugeDocumentsChangesForC100UpdatedApplicant() {
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(refugePartyDetails1).build();
        Element<PartyDetails> wrappedApplicant2 = Element.<PartyDetails>builder().value(
            refugePartyDetails1NotConfidential).build();

        List<Element<PartyDetails>> partyDetailsWrappedList = Collections.singletonList(wrappedApplicant);
        List<Element<PartyDetails>> partyDetailsWrappedList2 = Collections.singletonList(wrappedApplicant2);

        RefugeConfidentialDocuments refugeConfidentialDocuments = RefugeConfidentialDocuments.builder().partyType(
            "Applicant").build();
        Element<RefugeConfidentialDocuments> element = Element.<RefugeConfidentialDocuments>builder().value(
            refugeConfidentialDocuments).build();
        List<Element<RefugeConfidentialDocuments>> list = new ArrayList<>();
        list.add(element);

        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant2);

        CaseData caseData = CaseData
            .builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .refugeDocuments(list)
            .applicants(applicantList)
            .build();

        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters = new RefugeDocumentHandlerParameters();

        RefugeConfidentialDocumentsRecord result = confidentialityC8RefugeService.processC8RefugeDocumentsChangesForC100(
            caseData,
            Optional.ofNullable(partyDetailsWrappedList),
            Optional.ofNullable(partyDetailsWrappedList2),
            refugeDocumentHandlerParameters,
            null
        );
        System.out.print("result: " + result);
        assertNull(result);
    }
}
