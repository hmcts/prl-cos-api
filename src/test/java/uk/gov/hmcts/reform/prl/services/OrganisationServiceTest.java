package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class OrganisationServiceTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private OrganisationApi organisationApi;

    private final String authToken = "Bearer testAuthtoken";
    private final String serviceAuthToken = "serviceTestAuthtoken";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
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

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

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

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
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
                                              applicant.getSolicitorOrg().getOrganisationID()))
            .thenReturn(organisations);
        String organisationId = applicant.getSolicitorOrg().getOrganisationID();
        organisationService.getOrganisationDetaiils(authToken, organisationId);

        System.out.println("casedata=======: "+caseData);
        assertEquals(organisations.getOrganisationIdentifier(), organisationId);

        organisationService.getApplicantOrganisationDetails(caseData);

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

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

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

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .respondents(listOfRespondents)
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

        System.out.println("casedata=======: "+caseData);
        assertEquals(organisations.getOrganisationIdentifier(), organisationId);

        organisationService.getRespondentOrganisationDetails(caseData);

    }
}
