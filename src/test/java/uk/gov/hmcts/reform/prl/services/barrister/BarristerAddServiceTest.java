package uk.gov.hmcts.reform.prl.services.barrister;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Barrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.applicant;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.respondent;

@ExtendWith(SpringExtension.class)
class BarristerAddServiceTest extends BarristerTestAbstract {
    @InjectMocks
    BarristerAddService barristerAddService;
    @Mock
    protected UserService userService;
    @Mock
    protected OrganisationService organisationService;
    @Mock
    private UserDetails userDetails;

    @BeforeEach
    public void setup() {
        barristerAddService = new BarristerAddService(userService, organisationService);
        UserDetails userDetails = UserDetails.builder()
            .id("1")
            .roles(List.of(COURT_ADMIN))
            .build();
        Optional<Organisations> org = Optional.of(Organisations.builder().build());
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(org);
    }

    @Test
    void shouldGetAllocatedBarristerC100() {
        setupApplicantsC100();
        setupRespondentsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(allApplicants)
            .respondents(allRespondents)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            AUTHORISATION
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(4, partiesDynamicList.getListItems().size());

        assertPartyToAdd(partiesDynamicList, applicant, PARTY_ID_PREFIX, 0, 1, 1);
        assertPartyToAdd(partiesDynamicList, applicant, PARTY_ID_PREFIX, 1, 2, 2);
        assertPartyToAdd(partiesDynamicList, respondent, PARTY_ID_PREFIX, 2, 5, 5);
        assertPartyToAdd(partiesDynamicList, respondent, PARTY_ID_PREFIX, 3, 6, 6);
    }

    @Test
    void shouldGetAllocatedBarristerFL401() {
        setupApplicantFL401();
        setupRespondentFl401();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(applicantFL401)
            .respondentsFL401(respondentFL401)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            AUTHORISATION
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());
        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(2, partiesDynamicList.getListItems().size());

        assertPartyToAdd(partiesDynamicList, applicant, PARTY_ID_PREFIX, 0, 1, 1);
        assertPartyToAdd(partiesDynamicList, respondent, PARTY_ID_PREFIX, 1, 1, 1);
    }

    @Test
    void shouldGetAllocatedBarristerFL401EmptyApplicant() {
        setupRespondentFl401();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(respondentFL401)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            AUTHORISATION
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(1, partiesDynamicList.getListItems().size());

        assertPartyToAdd(partiesDynamicList, respondent, PARTY_ID_PREFIX, 0, 1, 1);
    }

    @Test
    void shouldGetAllocatedBarristerFL401EmptyRespondent() {
        setupApplicantFL401();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(applicantFL401)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            AUTHORISATION
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(1, partiesDynamicList.getListItems().size());

        assertPartyToAdd(partiesDynamicList, applicant, PARTY_ID_PREFIX, 0, 1, 1);
    }

    @Test
    void shouldGetAllocatedBarristerForNullApplicants() {
        setupRespondentsC100();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .respondents(allRespondents)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            AUTHORISATION
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(2, partiesDynamicList.getListItems().size());

        assertPartyToAdd(partiesDynamicList, respondent, PARTY_ID_PREFIX, 0, 5, 5);
        assertPartyToAdd(partiesDynamicList, respondent, PARTY_ID_PREFIX, 1, 6, 6);
    }

    @Test
    void shouldGetAllocatedBarristerForNullRespondents() {
        setupApplicantsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(allApplicants)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            AUTHORISATION
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(2, partiesDynamicList.getListItems().size());
        assertPartyToAdd(partiesDynamicList, applicant, PARTY_ID_PREFIX, 0, 1, 1);
        assertPartyToAdd(partiesDynamicList, applicant, PARTY_ID_PREFIX, 1, 2, 2);
    }

    @Test
    void shouldThrowExceptionForUndefinedCaseType() {
        setupApplicantsC100();

        CaseData caseData = CaseData.builder()
            .build();

        assertThrows(
            RuntimeException.class,
            () -> barristerAddService.getAllocatedBarrister(caseData, AUTHORISATION)
        );
    }

    @Test
    void shouldGetApplicantForSolicitorC100() {
        setupApplicantsC100();
        CaseData caseData = CaseData.builder().caseTypeOfApplication("C100").applicants(allApplicants).build();

        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org1").build());
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);
        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            AUTHORISATION
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        assertPartyToAdd(partiesDynamicList, applicant, PARTY_ID_PREFIX, 0, 1, 1);
    }

    @Test
    void shouldGetApplicantForSolicitorFL401() {
        setupApplicantFL401();
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(applicantFL401)
            .build();
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org1").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);
        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            AUTHORISATION
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        assertPartyToAdd(partiesDynamicList, applicant, PARTY_ID_PREFIX, 0, 1, 1);
    }

    @Test
    void shouldGetApplicantsWithSameSolicitorC100() {
        setupApplicantsC100();
        allApplicants.get(1).getValue().getSolicitorOrg().setOrganisationID("Org1");
        allApplicants.get(1).getValue().getSolicitorOrg().setOrganisationName("Org1");
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));

        CaseData caseData = CaseData.builder().caseTypeOfApplication("C100").applicants(allApplicants).build();
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org1").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            AUTHORISATION
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(2, partiesDynamicList.getListItems().size());
        assertPartyToAdd(partiesDynamicList, applicant, PARTY_ID_PREFIX, 0, 1, 1);
        assertPartyToAdd(partiesDynamicList, applicant, PARTY_ID_PREFIX, 1, 2, 1);
    }

    @Test
    void shouldGetRespondentForSolicitorC100() {
        setupRespondentsC100();
        CaseData caseData = CaseData.builder().caseTypeOfApplication("C100").respondents(allRespondents).build();
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org5").build());
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            AUTHORISATION
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        assertPartyToAdd(partiesDynamicList, respondent, PARTY_ID_PREFIX, 0, 5, 5);
    }

    @Test
    void shouldGetRespondentForSolicitorFL401() {
        setupRespondentFl401();
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        CaseData caseData = CaseData.builder().caseTypeOfApplication("FL401").applicantsFL401(applicantFL401)
            .respondentsFL401(respondentFL401).build();
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org1").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            AUTHORISATION
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        assertPartyToAdd(partiesDynamicList, respondent, PARTY_ID_PREFIX, 0, 1, 1);
    }

    @Test
    void shouldGetRespondentsWithSameSolicitorC100() {
        setupRespondentsC100();
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        allRespondents.get(1).getValue().getSolicitorOrg().setOrganisationID("Org5");
        allRespondents.get(1).getValue().getSolicitorOrg().setOrganisationName("Org5");
        CaseData caseData = CaseData.builder().caseTypeOfApplication("C100").respondents(allRespondents).build();
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org5").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            AUTHORISATION
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(2, partiesDynamicList.getListItems().size());
        assertPartyToAdd(partiesDynamicList, respondent, PARTY_ID_PREFIX, 0, 5, 5);
        assertPartyToAdd(partiesDynamicList, respondent, PARTY_ID_PREFIX, 1, 6, 5);
    }

    @Test
    void shouldReturnAllPartiesIfUserIsCaseworker() {
        setupApplicantsC100();
        setupRespondentsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(allApplicants)
            .respondents(allRespondents)
            .build();

        when(userDetails.getRoles()).thenReturn(List.of("court-admin"));

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData, AUTHORISATION
        );

        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(4, partiesDynamicList.getListItems().size());
        assertPartyToAdd(partiesDynamicList, applicant, PARTY_ID_PREFIX, 0, 1, 1);
        assertPartyToAdd(partiesDynamicList, applicant, PARTY_ID_PREFIX, 1, 2, 2);
        assertPartyToAdd(partiesDynamicList, respondent, PARTY_ID_PREFIX, 2, 5, 5);
        assertPartyToAdd(partiesDynamicList, respondent, PARTY_ID_PREFIX, 3, 6, 6);
    }

    @Test
    void shouldReturnNoPartiesIfSolicitorHasNoOrganisation() {
        setupApplicantsC100();
        setupRespondentsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(allApplicants)
            .respondents(allRespondents)
            .build();

        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(Optional.empty());

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData, AUTHORISATION
        );

        assertEquals(0, allocatedBarrister.getPartyList().getListItems().size());
    }

    @Test
    void shouldReturnEmptyIfSolicitorHasDifferentOrganisationToParties() {
        setupApplicantsC100();
        setupRespondentsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(allApplicants)
            .respondents(allRespondents)
            .build();

        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("some org").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData, AUTHORISATION
        );

        assertEquals(0, allocatedBarrister.getPartyList().getListItems().size());
    }

    @Test
    void shouldNotIncludePartiesWithBarrister() {
        setupApplicantsC100();
        allApplicants.getFirst().getValue().setBarrister(Barrister.builder().barristerId("barrister-id").build());
        allApplicants.get(1).getValue().getSolicitorOrg().setOrganisationID("Org1");
        allApplicants.get(1).getValue().getSolicitorOrg().setOrganisationName("Org1");

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(allApplicants)
            .build();

        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org1").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData, AUTHORISATION
        );

        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        assertPartyToAdd(partiesDynamicList, applicant, PARTY_ID_PREFIX, 0, 2, 1);
    }

    protected void assertPartyToAdd(DynamicList listOfBarristers, PartyEnum partyEnum, String prefix, int itemIndex, int partyIndex, int orgIndex) {
        String appRepPrefix = partyEnum == applicant ? "App" : "Resp";
        DynamicListElement appParty = listOfBarristers.getListItems().get(itemIndex);
        String label = appRepPrefix + "FN" + partyIndex + " " + appRepPrefix + "LN" + partyIndex + " "
            + (partyEnum == applicant ? "(Applicant)" : "(Respondent)") + ", "
            + appRepPrefix + "FN" + partyIndex + " " + appRepPrefix + "LN" + partyIndex + ", "
            + "Org" + orgIndex;
        assertEquals(label, appParty.getLabel());
        assertEquals(prefix + partyIndex, appParty.getCode());
    }

}
