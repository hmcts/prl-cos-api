package uk.gov.hmcts.reform.prl.rpa.mappers;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.OrganisationApi;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.DxAddress;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.json.JsonObject;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorsMapperTest {

    @InjectMocks
    SolicitorsMapper solicitorsMapper;
    @Mock
    AddressMapper addressMapper;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    SystemUserService systemUserService;
    @Mock
    Organisations organisations;
    @Mock
    OrganisationService organisationService1;
    @Mock
    Organisation organisation;
    @Mock
    OrganisationApi organisationApi;


    Map<String, PartyDetails> solicitorMap;
    Address address;

    PartyDetails partyDetails;
    private GeneratedDocumentInfo generatedDocumentInfo;
    private String authToken;
    private String s2sToken;
    private UUID uuid;

    @Before
    public void setUp() {
        uuid = randomUUID();
        authToken = "auth-token";
        s2sToken = "s2sToken";
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .createdOn("somedate")
            .binaryUrl("binaryUrl")
            .mimeType("xyz")
            .hashToken("testHashToken")
            .build();
        address = Address.builder()
            .addressLine1("55 Test Street")
            .postTown("Town")
            .postCode("N12 3BH")
            .build();

        organisations = Organisations.builder().organisationIdentifier("Org Identifier")
            .name("Name").build();

        partyDetails = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .solicitorAddress(address)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        ;
    }

    @Test
    public void testSolicitorAddresswithFields() {

        ContactInformation contactInformation = ContactInformation.builder().county("County")
            .country("UK").postCode("PostCode").townCity("towncity").addressLine1("Addressline1")
            .addressLine2("AddressLine2").addressLine3("AddressLine3").build();
        assertNotNull(solicitorsMapper.mapSolicitorAddress(contactInformation));
    }

    @Test
    public void testCallOrgSearchFormSolicitorMapWithNullParty() {
        assertNotNull(solicitorsMapper.callOrgSearchFormSolicitorMap("123", PartyDetails.builder().build()));
    }

    @Test
    public void testCallOrgSearchFormSolicitorMapWithPartyDetails() {
        PartyDetails partyDetails = PartyDetails
            .builder()
            .solicitorOrg(Organisation.builder().organisationID("1234").build())
            .build();
        when(organisationService1.getOrganisationDetails(Mockito.any(),Mockito.any()))
            .thenReturn(Organisations.builder().build());
        assertNotNull(solicitorsMapper.callOrgSearchFormSolicitorMap("123", partyDetails));
    }

    @Test
    public void testSolicitorAddresswithoutFields() {

        ContactInformation contactInformation = null;
        assertTrue(solicitorsMapper.mapSolicitorAddress(contactInformation).isEmpty());
    }

    @Test
    public void testOrgSearchFormSolicitorMap() {
        partyDetails = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            //.solicitorOrg(organisation)
            .build();
        assertNotNull(solicitorsMapper.callOrgSearchFormSolicitorMap("Id", partyDetails));

    }

    @Test
    public void testOrgSearchFormSolicitorMapWithAuthToken() {

        organisation = Organisation.builder().organisationID("Org ID").build();
        partyDetails = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(null)
            .solicitorAddress(address)
            .build();
        assertNotNull(solicitorsMapper.callOrgSearchFormSolicitorMap("Id", partyDetails));

    }

    @Test
    public void testSolicitorFullName() {
        String fullName = solicitorsMapper.getSolicitorFullName("FirstName",
                                                                "LastName");
        assertEquals("FirstNameLastName", fullName);
    }

    @Test
    public void testSolicitorNullName() {
        String fullName = solicitorsMapper.getSolicitorFullName(null, null);
        assertNull(fullName);
    }

    @Test
    public void testSolicitorAddress() {
        JsonObject solicitorAddress = solicitorsMapper.getSolicitorAddress(partyDetails, null);
        assertNull(solicitorAddress);
    }

    @Test
    public void testDxNumber() {
        partyDetails = PartyDetails.builder().dxNumber("DxNumber").build();
        assertEquals("DxNumber", solicitorsMapper.getDxNumber(partyDetails, null));
    }

    @Test
    public void testWithPartyDetailsAndOrgIsNull() {
        assertNull(solicitorsMapper.getDxNumber(partyDetails, null));
    }

    @Test
    public void testOrgIsNotEmpty() {
        List<ContactInformation> contactInformation = new ArrayList<>();
        List<DxAddress> dxAddress = new ArrayList<>();
        dxAddress.add(DxAddress.builder().dxNumber("dxNumber").build());
        contactInformation.add(ContactInformation.builder()
                                   .addressLine1("AddressLine1").dxAddress(dxAddress).build());
        organisations = Organisations.builder().contactInformation(contactInformation).build();
        assertEquals("dxNumber", solicitorsMapper.getDxNumber(partyDetails, organisations));
    }

    @Test
    public void testMapSolicitorList() {
        solicitorMap = new HashMap<>();
        solicitorMap.put("134", partyDetails);
        assertNotNull(solicitorsMapper.mapSolicitorList(solicitorMap));
    }
}
