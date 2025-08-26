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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.applicant;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.respondent;

@ExtendWith(SpringExtension.class)
class BarristerRemoveServiceTest extends BarristerTestAbstract {
    @InjectMocks
    BarristerRemoveService barristerRemoveService;
    @Mock
    protected UserService userService;
    @Mock
    protected OrganisationService organisationService;
    @Mock
    private UserDetails userDetails;

    @BeforeEach
    public void setup() {
        barristerRemoveService = new BarristerRemoveService(userService, organisationService);
        UserDetails userDetails = UserDetails.builder()
            .id("1")
            .roles(List.of(COURT_ADMIN))
            .build();
        Optional<Organisations> org = Optional.of(Organisations.builder().build());
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(org);
    }

    @Test
    void shouldGetCaseworkerRemovalBarristersC100() {
        setupApplicantsC100();
        setupRespondentsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(allApplicants)
            .respondents(allRespondents)
            .build();

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(caseData, AUTHORISATION, false);
        DynamicList listOfBarristersToRemove = allocatedBarrister.getPartyList();

        assertEquals(listOfBarristersToRemove.getValue(), null);
        assertEquals(2, listOfBarristersToRemove.getListItems().size());

        assertPartyToRemove(listOfBarristersToRemove, applicant, PARTY_ID_PREFIX, 0, 3);
        assertPartyToRemove(listOfBarristersToRemove, respondent, PARTY_ID_PREFIX, 1, 7);
    }

    @Test
    void shouldGetSolicitorRemovalBarristerForApplicantC100() {
        setupApplicantsC100();
        CaseData caseData = CaseData.builder().caseTypeOfApplication("C100").applicants(allApplicants).build();

        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org3").build());
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(caseData, AUTHORISATION, false);

        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        assertPartyToRemove(partiesDynamicList, applicant, PARTY_ID_PREFIX, 0, 3);
    }

    @Test
    void shouldGetSolicitorRemovalBarristerForApplicantFL401() {
        setupApplicantFL401();
        applicantFL401 = applicantFL401.toBuilder()
            .barrister(Barrister.builder()
                           .barristerId("id")
                           .barristerFirstName("BarFN1")
                           .barristerLastName("BarLN1")
                           .build())
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(applicantFL401)
            .build();


        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org1").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(caseData, AUTHORISATION, false);

        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        assertPartyToRemove(partiesDynamicList, applicant, PARTY_ID_PREFIX, 0, 1);
    }

    @Test
    void shouldGetSolicitorRemovalBarristerForApplicantSameSolicitorC100() {
        setupApplicantsC100();
        allApplicants.get(2).getValue().getSolicitorOrg().setOrganisationID("Org1");
        allApplicants.get(2).getValue().getSolicitorOrg().setOrganisationName("Org1");

        CaseData caseData = CaseData.builder().caseTypeOfApplication("C100").applicants(allApplicants).build();
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org1").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(
            caseData,
            AUTHORISATION,
            false
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        assertPartyToRemove(partiesDynamicList, applicant, PARTY_ID_PREFIX, 0, 3);
    }

    @Test
    void shouldGetSolicitorRemovalBarristerForRespondentC100() {
        setupRespondentsC100();
        CaseData caseData = CaseData.builder().caseTypeOfApplication("C100").respondents(allRespondents).build();
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org7").build());
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(
            caseData,
            AUTHORISATION,
            false
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        assertPartyToRemove(partiesDynamicList, respondent, PARTY_ID_PREFIX, 0, 7);
    }

    @Test
    void shouldGetRespondentForSolicitorFL401() {
        setupRespondentFl401();
        respondentFL401 = respondentFL401.toBuilder()
            .barrister(Barrister.builder()
                           .barristerId("id")
                           .barristerFirstName("BarFN1")
                           .barristerLastName("BarLN1")
                           .build())
            .build();
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        CaseData caseData = CaseData.builder().caseTypeOfApplication("FL401").applicantsFL401(applicantFL401)
            .respondentsFL401(respondentFL401).build();
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org1").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(
            caseData,
            AUTHORISATION,
            false
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        assertPartyToRemove(partiesDynamicList, respondent, PARTY_ID_PREFIX, 0, 1);
    }

    @Test
    void shouldGetRespondentsWithSameSolicitorC100() {
        setupRespondentsC100();
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        allRespondents.get(1).getValue().getSolicitorOrg().setOrganisationID("Org7");
        allRespondents.get(1).getValue().getSolicitorOrg().setOrganisationName("Org7");
        allRespondents.add(buildPartyDetailsElement(6, false, true, true));

        CaseData caseData = CaseData.builder().caseTypeOfApplication("C100").respondents(allRespondents).build();
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org7").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(
            caseData,
            AUTHORISATION,
            false
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(2, partiesDynamicList.getListItems().size());
        assertPartyToRemove(partiesDynamicList, respondent, PARTY_ID_PREFIX, 0, 7);
        assertPartyToRemove(partiesDynamicList, respondent, PARTY_ID_PREFIX, 1, 6);
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

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(
            caseData, AUTHORISATION, false
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

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(
            caseData, AUTHORISATION, false
        );

        assertEquals(0, allocatedBarrister.getPartyList().getListItems().size());
    }

    @Test
    void shouldGetRemovalBarristerPartyForApplicantC100() {
        setupApplicantsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(allApplicants)
            .build();

        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier(BARRISTER_ORG_ID_PREFIX + "3").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(caseData, AUTHORISATION, true);

        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        assertPartyToRemove(partiesDynamicList, applicant, PARTY_ID_PREFIX, 0, 3);
    }

    @Test
    void shouldGetRemovalBarristerPartyForRespondentC100() {
        setupRespondentsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .respondents(allRespondents)
            .build();

        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier(BARRISTER_ORG_ID_PREFIX + "7").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(caseData, AUTHORISATION, true);

        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        assertPartyToRemove(partiesDynamicList, respondent, PARTY_ID_PREFIX, 0, 7);
    }

    @Test
    void shouldGetRemovalBarristerPartyForApplicantFL401() {
        applicantFL401 = buildPartyDetails(0, "App", "App", "Bar", "Org", true, true);
        CaseData caseData = CaseData.builder().caseTypeOfApplication("FL401").applicantsFL401(applicantFL401)
            .respondentsFL401(respondentFL401).build();
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier(BARRISTER_ORG_ID_PREFIX + "0").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(
            caseData,
            AUTHORISATION,
            true
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        assertPartyToRemove(partiesDynamicList, applicant, PARTY_ID_PREFIX, 0, 0);
    }

    @Test
    void shouldGetRemovalBarristerPartyForRespondentFL401() {
        respondentFL401 = buildPartyDetails(1, "Resp", "Resp", "Bar", "Org", true, true);
        CaseData caseData = CaseData.builder().caseTypeOfApplication("FL401").applicantsFL401(applicantFL401)
            .respondentsFL401(respondentFL401).build();
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier(BARRISTER_ORG_ID_PREFIX + "1").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(
            caseData,
            AUTHORISATION,
            true
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        assertPartyToRemove(partiesDynamicList, respondent, PARTY_ID_PREFIX, 0, 1);
    }

    @Test
    void shouldGetEmptyRemovalBarristerPartyWhenNoAssociatedCase() {
        setupRespondentsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .respondents(allRespondents)
            .build();

        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("some org").build());
        when(organisationService.findUserOrganisation(AUTHORISATION)).thenReturn(mockOrg);
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(caseData, AUTHORISATION, true);

        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(0, partiesDynamicList.getListItems().size());
    }

    protected void assertPartyToRemove(DynamicList listOfBarristersToRemove, PartyEnum partyEnum, String prefix, int itemIndex, int partyIndex) {
        String appRepPrefix = partyEnum == applicant ? "App" : "Resp";
        DynamicListElement appParty = listOfBarristersToRemove.getListItems().get(itemIndex);
        String label = appRepPrefix + "FN" + partyIndex + " " + appRepPrefix + "LN" + partyIndex + " "
            + (partyEnum == applicant ? "(Applicant)" : "(Respondent)") + ", "
            + appRepPrefix + "FN" + partyIndex + " " + appRepPrefix + "LN" + partyIndex + ", "
            + "BarFN" + partyIndex + " " + "BarLN" + partyIndex;
        assertEquals(label, appParty.getLabel());
        assertEquals((prefix + partyIndex).length(), appParty.getCode().length());
    }
}
