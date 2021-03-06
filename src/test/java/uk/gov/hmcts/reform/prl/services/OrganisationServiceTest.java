package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import feign.Request;
import feign.Response;
import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.OrganisationApi;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;
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

    private final String authToken = "Bearer testAuthtoken";
    private final String serviceAuthToken = "serviceTestAuthtoken";

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
    }

    @Test
    public void testApplicantOrganisationDetails() throws NotFoundException {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .build();

        String applicantNames = "TestFirst TestLast";

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

        when(organisationService.getOrganisationDetaiils(authToken, organisationId)).thenReturn(organisations);
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
    public void testRespondentOrganisationDetails() throws NotFoundException {

        PartyDetails respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .build();

        String applicantNames = "TestFirst TestLast";

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

        CaseData caseData1 = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(elementList)
            .build();

        when(organisationApi.findOrganisation(authToken,
                                              serviceAuthToken,
                                              respondent.getSolicitorOrg().getOrganisationID()))
            .thenReturn(organisations);
        String organisationId = respondent.getSolicitorOrg().getOrganisationID();
        organisationService.getOrganisationDetaiils(authToken, organisationId);

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
    public void testApplicantOrganisationDetailsForFl401() throws NotFoundException {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .build();

        String applicantNames = "TestFirst TestLast";

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

        when(organisationService.getOrganisationDetaiils(authToken, organisationId)).thenReturn(organisations);
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

    public static FeignException feignException(int status, String message) {
        return FeignException.errorStatus(message, Response.builder()
            .status(status)
            .request(Request.create(GET, EMPTY, Map.of(), new byte[]{}, UTF_8, null))
            .build());
    }
}
