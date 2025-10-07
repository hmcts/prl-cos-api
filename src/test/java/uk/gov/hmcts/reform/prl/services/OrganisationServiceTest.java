package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.OrganisationApi;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrgSolicitors;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.OrganisationUser;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.MaskEmail;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static junit.framework.TestCase.assertNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class OrganisationServiceTest {

    @InjectMocks
    private OrganisationService organisationService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private OrganisationApi organisationApi;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private MaskEmail maskEmail;

    private final String authToken = "Bearer testAuthtoken";
    private final String serviceAuthToken = "serviceTestAuthtoken";

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
    }

    @Test
    public void testApplicantOrganisationDetails() {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .build();


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
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .organisations(organisations)
            .build();

        Element<PartyDetails> applicants = Element.<PartyDetails>builder().value(partyDetailsWithOrganisations).build();
        List<Element<PartyDetails>> elementList = Collections.singletonList(applicants);

        when(organisationApi.findOrganisation(authToken,
                                              serviceAuthToken,
                                              applicant.getSolicitorOrg().getOrganisationID()))
            .thenReturn(organisations);
        String organisationId = applicant.getSolicitorOrg().getOrganisationID();

        when(organisationService.getOrganisationDetails(authToken, organisationId)).thenReturn(organisations);
        CaseData caseData1 = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(elementList)
            .build();
        assertEquals(organisations.getOrganisationIdentifier(), organisationId);
        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
            .build();
        CaseData caseData2 = organisationService.getApplicantOrganisationDetails(caseData);
        assertEquals(caseData2,caseData1);
    }

    @Test
    public void testRespondentOrganisationDetails() {
        PartyDetails respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .build();

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

        when(organisationApi.findOrganisation(authToken,
                                              serviceAuthToken,
                                              respondent.getSolicitorOrg().getOrganisationID()))
            .thenReturn(organisations);
        String organisationId = respondent.getSolicitorOrg().getOrganisationID();
        organisationService.getOrganisationDetails(authToken, organisationId);

        assertEquals(organisations.getOrganisationIdentifier(), organisationId);
        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .respondents(listOfRespondents)
            .build();
        organisationService.getRespondentOrganisationDetails(caseData);
    }

    @Test
    public void testRespondentOrganisationDetailsNotFound() {
        PartyDetails respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .build();

        when(organisationApi.findOrganisation(Mockito.anyString(),
                                              Mockito.anyString(),
                                              Mockito.anyString()))
            .thenThrow(feignException(404, "Not found"));

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        CaseData caseData = CaseData.builder().respondents(listOfRespondents).build();

        CaseData orgData =  organisationService.getRespondentOrganisationDetails(caseData);
        assertEquals(orgData, caseData);
    }

    @Test
    public void testApplicantOrganisationDetailsForFl401() {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .build();


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
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .organisations(organisations)
            .build();

        when(organisationApi.findOrganisation(authToken,
                                              serviceAuthToken,
                                              applicant.getSolicitorOrg().getOrganisationID()))
            .thenReturn(organisations);
        String organisationId = applicant.getSolicitorOrg().getOrganisationID();

        when(organisationService.getOrganisationDetails(authToken, organisationId)).thenReturn(organisations);
        CaseData expectedCaseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(partyDetailsWithOrganisations)
            .build();
        assertEquals(organisations.getOrganisationIdentifier(), organisationId);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(applicant)
            .build();
        CaseData actualCaseData = organisationService.getApplicantOrganisationDetailsForFL401(caseData);
        assertEquals(actualCaseData,expectedCaseData);
    }

    @Test
    public void testApplicantOrganisationDetailsForFl401NotFound() {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .build();

        when(organisationApi.findOrganisation(Mockito.anyString(),
                                              Mockito.anyString(),
                                              Mockito.anyString()))
            .thenThrow(feignException(404, "Not found"));

        CaseData caseData = CaseData.builder().applicantsFL401(applicant).build();

        CaseData orgData =  organisationService.getApplicantOrganisationDetailsForFL401(caseData);
        assertEquals(orgData, caseData);
    }

    @Test
    public void testRespondentOrganisationDetailsForFl401() {
        PartyDetails respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .build();

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
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .organisations(organisations)
            .build();

        when(organisationApi.findOrganisation(authToken,
                                              serviceAuthToken,
                                              respondent.getSolicitorOrg().getOrganisationID()))
            .thenReturn(organisations);
        String organisationId = respondent.getSolicitorOrg().getOrganisationID();

        when(organisationService.getOrganisationDetails(authToken, organisationId)).thenReturn(organisations);
        CaseData expectedCaseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .respondentsFL401(partyDetailsWithOrganisations)
            .build();
        assertEquals(organisations.getOrganisationIdentifier(), organisationId);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .respondentsFL401(respondent)
            .build();
        CaseData actualCaseData = organisationService.getRespondentOrganisationDetailsForFL401(caseData);
        assertEquals(actualCaseData,expectedCaseData);
    }

    @Test
    public void testRespondentOrganisationDetailsForFl401NotFound() {
        PartyDetails respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .build();

        when(organisationApi.findOrganisation(Mockito.anyString(),
                                              Mockito.anyString(),
                                              Mockito.anyString()))
            .thenThrow(feignException(404, "Not found"));

        CaseData caseData = CaseData.builder().respondentsFL401(respondent).build();

        CaseData orgData =  organisationService.getRespondentOrganisationDetailsForFL401(caseData);
        assertEquals(orgData, caseData);
    }

    @Test
    public void testRespondentOrganisationDetailsForFl401WhenRespondentNull() {
        CaseData caseData = CaseData.builder().respondentsFL401(null).build();

        CaseData orgData =  organisationService.getRespondentOrganisationDetailsForFL401(caseData);
        assertEquals(orgData, caseData);
    }


    @Test
    public void findUserOrganisationTest() {
        Organisations organisations = Organisations.builder()
            .organisationIdentifier("79ZRSOU")
            .name("Civil - Organisation 2")
            .build();

        when(organisationApi.findUserOrganisation(authToken,
                                              serviceAuthToken))
            .thenReturn(organisations);

        Optional<Organisations> orgData =  organisationService.findUserOrganisation(authToken);
        assertEquals(orgData.get(),organisations);
    }

    @Test
    public void findUserOrganisationNotFoundTest() {
        when(organisationApi.findUserOrganisation(authToken,
                                                  serviceAuthToken))
            .thenThrow(feignException(404, "Not found"));

        Optional<Organisations> orgData =  organisationService.findUserOrganisation(authToken);
        assertEquals(orgData,Optional.empty());
    }

    @Test
    public void getOrganisationSolicitorDetailsTest() {
        when(organisationApi.findOrganisationSolicitors(
            Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(OrgSolicitors.builder().build());
        OrgSolicitors orgData =  organisationService.getOrganisationSolicitorDetails(authToken, serviceAuthToken);
        assertEquals(orgData,OrgSolicitors.builder().build());
    }

    public static FeignException feignException(int status, String message) {
        return FeignException.errorStatus(message, Response.builder()
            .status(status)
            .request(Request.create(GET, EMPTY, Map.of(), new byte[]{}, UTF_8, null))
            .build());
    }

    @Test
    public void testGetAllActiveOrganisations() {

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

        when(organisationApi.findOrganisations(
            authToken,
            serviceAuthToken,
            "Active"
        ))
            .thenReturn(List.of(organisations));
        assertNotNull(organisationService.getAllActiveOrganisations(authToken));
    }

    @Test
    public void testUserFoundReturnedByFindUserByEmail() {
        String email = "user@malinator.com";
        when(maskEmail.mask(email)).thenReturn("u**r@malinator.com");
        OrganisationUser organisationUser = OrganisationUser.builder()
            .userIdentifier(UUID.randomUUID().toString())
            .build();
        when(organisationApi.findUserByEmail(anyString(), anyString(), eq(email)))
            .thenReturn(organisationUser);
        Optional<String> userId = organisationService.findUserByEmail(email);
        assertThat(userId)
            .hasValue(organisationUser.getUserIdentifier());
    }

    @Test
    public void testNotUserNotFoundByFindUserByEmail() {
        String email = "user@malinator.com";
        when(maskEmail.mask(email)).thenReturn("u**r@malinator.com");
        when(organisationApi.findUserByEmail(anyString(), anyString(), eq(email)))
            .thenThrow(feignException(404, "Not found"));
        Optional<String> userId = organisationService.findUserByEmail(email);
        assertThat(userId)
            .isEmpty();
    }

    @Test
    public void testExceptionThrownByFindUserByEmail() {
        String email = "user@malinator.com";
        when(maskEmail.mask(email)).thenReturn("u**r@malinator.com");
        when(organisationApi.findUserByEmail(anyString(), anyString(), eq(email)))
            .thenThrow(feignException(500, "Internal Server Error"));
        assertThatThrownBy(() -> organisationService.findUserByEmail(email))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Error while fetching user id by email");
    }

    @Test
    public void findUserOrganisationForbiddenRethrowsTest() {
        when(organisationApi.findUserOrganisation(authToken, serviceAuthToken))
            .thenThrow(feignException(403, "Forbidden"));

        assertThatThrownBy(() -> organisationService.findUserOrganisation(authToken))
            .isInstanceOf(FeignException.Forbidden.class);
    }

    @Test
    public void getApplicantOrganisationDetails_handlesNotFoundException() {
        when(systemUserService.getSysUserToken()).thenReturn("user-token");
        when(authTokenGenerator.generate()).thenReturn("svc-token");
        when(organisationApi.findOrganisation(anyString(), anyString(), eq("ORG-404")))
            .thenThrow(mock(javax.ws.rs.NotFoundException.class));

        PartyDetails applicant = PartyDetails.builder()
            .firstName("A")
            .lastName("One")
            .solicitorOrg(Organisation.builder().organisationID("ORG-404").build())
            .build();

        List<Element<PartyDetails>> applicants = Collections.singletonList(
            Element.<PartyDetails>builder().value(applicant).build()
        );

        CaseData input = CaseData.builder().applicants(applicants).build();

        CaseData out = organisationService.getApplicantOrganisationDetails(input);

        assertNotNull(out);
        assertNotNull(out.getApplicants());
        PartyDetails enriched = out.getApplicants().get(0).getValue();
        assertNull("organisations should not be set when org lookup 404s", enriched.getOrganisations());
    }

    @Test
    public void getRespondentOrganisationDetails_handlesNotFoundException() {
        when(systemUserService.getSysUserToken()).thenReturn("user-token");
        when(authTokenGenerator.generate()).thenReturn("svc-token");
        when(organisationApi.findOrganisation(anyString(), anyString(), eq("ORG-404")))
            .thenThrow(mock(javax.ws.rs.NotFoundException.class));

        PartyDetails respondent = PartyDetails.builder()
            .firstName("R")
            .lastName("One")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder().organisationID("ORG-404").build())
            .build();

        List<Element<PartyDetails>> respondents = Collections.singletonList(
            Element.<PartyDetails>builder().value(respondent).build()
        );

        CaseData input = CaseData.builder().respondents(respondents).build();

        CaseData out = organisationService.getRespondentOrganisationDetails(input);

        assertNotNull(out);
        assertNotNull(out.getRespondents());
        PartyDetails enriched = out.getRespondents().get(0).getValue();
        assertNull("organisations should not be set when org lookup 404s", enriched.getOrganisations());
    }
}
