package uk.gov.hmcts.reform.prl.services.barrister;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.OrganisationService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BarristerAddServiceTest extends BarristerTestAbstract {
    @InjectMocks
    BarristerAddService barristerAddService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private OrganisationService organisationService;

    private static final String AUTH_TOKEN = "auth-token";

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
            userDetails,
            AUTH_TOKEN
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(4, partiesDynamicList.getListItems().size());

        assertPartyToAdd(partiesDynamicList, true, PARTY_ID_PREFIX, 0, 1);
        assertPartyToAdd(partiesDynamicList, true, PARTY_ID_PREFIX, 1, 2);
        assertPartyToAdd(partiesDynamicList, false, PARTY_ID_PREFIX, 2, 5);
        assertPartyToAdd(partiesDynamicList, false, PARTY_ID_PREFIX, 3, 6);
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
            userDetails,
            AUTH_TOKEN
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());
        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(2, partiesDynamicList.getListItems().size());

        assertPartyToAdd(partiesDynamicList, true, PARTY_ID_PREFIX, 0, 1);
        assertPartyToAdd(partiesDynamicList, false, PARTY_ID_PREFIX, 1, 1);
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
            userDetails,
            AUTH_TOKEN
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(1, partiesDynamicList.getListItems().size());

        assertPartyToAdd(partiesDynamicList, false, PARTY_ID_PREFIX, 0, 1);
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
            userDetails,
            AUTH_TOKEN
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(1, partiesDynamicList.getListItems().size());
        DynamicListElement appParty1 = partiesDynamicList.getListItems().get(0);

        assertPartyToAdd(partiesDynamicList, true, PARTY_ID_PREFIX, 0, 1);
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
            userDetails,
            AUTH_TOKEN
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(2, partiesDynamicList.getListItems().size());

        assertPartyToAdd(partiesDynamicList, false, PARTY_ID_PREFIX, 0, 5);
        assertPartyToAdd(partiesDynamicList, false, PARTY_ID_PREFIX, 1, 6);
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
            userDetails,
            AUTH_TOKEN
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(2, partiesDynamicList.getListItems().size());
        assertPartyToAdd(partiesDynamicList, true, PARTY_ID_PREFIX, 0, 1);
        assertPartyToAdd(partiesDynamicList, true, PARTY_ID_PREFIX, 1, 2);
    }

    @Test
    void shouldThrowExceptionForUndefinedCaseType() {
        setupApplicantsC100();

        CaseData caseData = CaseData.builder()
            .build();

        assertThrows(
            RuntimeException.class,
            () -> barristerAddService.getAllocatedBarrister(caseData, userDetails, AUTH_TOKEN)
        );
    }

    @Test
    void shouldGetApplicantForSolicitorC100() {
        setupApplicantsC100();
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        CaseData caseData = CaseData.builder().caseTypeOfApplication("C100").applicants(allApplicants).build();

        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org1").build());
        when(organisationService.findUserOrganisation(AUTH_TOKEN)).thenReturn(mockOrg);
        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            userDetails,
            AUTH_TOKEN
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        System.out.println(partiesDynamicList.getListItems());
        assertEquals(1, partiesDynamicList.getListItems().size());
        DynamicListElement appParty1 = partiesDynamicList.getListItems().getFirst();
        assertEquals(
            "AppFN1 AppLN1 (Applicant), AppFN1 AppLN1, Org1",
            appParty1.getLabel()
        );
        assertEquals(PARTY_ID_PREFIX + "1", appParty1.getCode());
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
        when(organisationService.findUserOrganisation(AUTH_TOKEN)).thenReturn(mockOrg);
        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            userDetails,
            AUTH_TOKEN
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        DynamicListElement appParty1 = partiesDynamicList.getListItems().getFirst();
        assertEquals(
            "AppFN1 AppLN1 (Applicant), AppFN1 AppLN1, Org1",
            appParty1.getLabel()
        );
        assertEquals(PARTY_ID_PREFIX + "1", appParty1.getCode());
    }

    @Test
    void shouldGetApplicantsWithSameSolicitorC100() {
        setupApplicantsC100();
        allApplicants.get(1).getValue().getSolicitorOrg().setOrganisationID("Org1");
        allApplicants.get(1).getValue().getSolicitorOrg().setOrganisationName("Org1");
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));

        CaseData caseData = CaseData.builder().caseTypeOfApplication("C100").applicants(allApplicants).build();
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org1").build());
        when(organisationService.findUserOrganisation(AUTH_TOKEN)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            userDetails,
            AUTH_TOKEN
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(2, partiesDynamicList.getListItems().size());
        DynamicListElement appParty1 = partiesDynamicList.getListItems().getFirst();
        assertEquals(
            "AppFN1 AppLN1 (Applicant), AppFN1 AppLN1, Org1",
            appParty1.getLabel()
        );
        assertEquals(PARTY_ID_PREFIX + "1", appParty1.getCode());

        DynamicListElement appParty2 = partiesDynamicList.getListItems().get(1);
        assertEquals(
            "AppFN2 AppLN2 (Applicant), AppFN2 AppLN2, Org1",
            appParty2.getLabel()
        );
        assertEquals(PARTY_ID_PREFIX + "2", appParty2.getCode());
    }

    @Test
    void shouldGetRespondentForSolicitorC100() {
        setupRespondentsC100();
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        CaseData caseData = CaseData.builder().caseTypeOfApplication("C100").respondents(allRespondents).build();
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org5").build());
        when(organisationService.findUserOrganisation(AUTH_TOKEN)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            userDetails,
            AUTH_TOKEN
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        DynamicListElement resParty1 = partiesDynamicList.getListItems().getFirst();
        assertEquals(
            "RespFN5 RespLN5 (Respondent), RespFN5 RespLN5, Org5",
            resParty1.getLabel()
        );
        assertEquals(PARTY_ID_PREFIX + "5", resParty1.getCode());
    }

    @Test
    void shouldGetRespondentForSolicitorFL401() {
        setupRespondentFl401();
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        CaseData caseData = CaseData.builder().caseTypeOfApplication("FL401").applicantsFL401(applicantFL401)
            .respondentsFL401(respondentFL401).build();
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org1").build());
        when(organisationService.findUserOrganisation(AUTH_TOKEN)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            userDetails,
            AUTH_TOKEN
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(1, partiesDynamicList.getListItems().size());
        DynamicListElement resParty1 = partiesDynamicList.getListItems().getFirst();
        assertEquals(
            "RespFN1 RespLN1 (Respondent), RespFN1 RespLN1, Org1",
            resParty1.getLabel()
        );
        assertEquals(PARTY_ID_PREFIX + "1", resParty1.getCode());
    }

    @Test
    void shouldGetRespondentsWithSameSolicitorC100() {
        setupRespondentsC100();
        when(userDetails.getRoles()).thenReturn(List.of(Roles.SOLICITOR.getValue()));
        allRespondents.get(1).getValue().getSolicitorOrg().setOrganisationID("Org5");
        allRespondents.get(1).getValue().getSolicitorOrg().setOrganisationName("Org5");
        CaseData caseData = CaseData.builder().caseTypeOfApplication("C100").respondents(allRespondents).build();
        Optional<Organisations> mockOrg = Optional.of(Organisations.builder().organisationIdentifier("Org5").build());
        when(organisationService.findUserOrganisation(AUTH_TOKEN)).thenReturn(mockOrg);

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(
            caseData,
            userDetails,
            AUTH_TOKEN
        );
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertNull(partiesDynamicList.getValue());
        assertEquals(2, partiesDynamicList.getListItems().size());
        DynamicListElement resParty1 = partiesDynamicList.getListItems().getFirst();
        assertEquals(
            "RespFN5 RespLN5 (Respondent), RespFN5 RespLN5, Org5",
            resParty1.getLabel()
        );
        assertEquals(PARTY_ID_PREFIX + "5", resParty1.getCode());
        DynamicListElement resParty2 = partiesDynamicList.getListItems().get(1);
        assertEquals(
            "RespFN6 RespLN6 (Respondent), RespFN6 RespLN6, Org5",
            resParty2.getLabel()
        );
        assertEquals(PARTY_ID_PREFIX + "6", resParty2.getCode());
    }

    private Element<PartyDetails> buildPartyDetails(String id, String appFirstName, String appLastName, boolean hasRep,
                                                    String repappFirstName, String repappLastName, String orgName) {
        return Element.<PartyDetails>builder().id(UUID.fromString(PARTY_ID_PREFIX + id))
            .value(getPartyDetails(id, appFirstName, appLastName, hasRep, repappFirstName, repappLastName, orgName))
            .build();
    }

    private PartyDetails getPartyDetails(String id, String appFirstName, String appLastName, boolean hasRep,
                                         String repappFirstName, String repappLastName, String orgName) {
        return PartyDetails.builder()
            .partyId(UUID.fromString(PARTY_ID_PREFIX + id))
            .firstName(appFirstName)
            .lastName(appLastName)
            .doTheyHaveLegalRepresentation(hasRep ? YesNoDontKnow.yes : null)
            .solicitorPartyId(hasRep ? UUID.fromString(SOL_PARTY_ID_PREFIX + id) : null)
            .representativeFirstName(hasRep ? repappFirstName : null)
            .representativeLastName(hasRep ? repappLastName : null)
            .solicitorOrg(Organisation.builder().organisationName(orgName).build())
            .build();
    }

    protected void assertPartyToAdd(DynamicList listOfBarristers, boolean appOrResp, String prefix, int itemIndex, int partyIndex) {
        String appRepPrefix = appOrResp ? "App" : "Resp";
        DynamicListElement appParty = listOfBarristers.getListItems().get(itemIndex);
        String label = appRepPrefix + "FN" + partyIndex + " " + appRepPrefix + "LN" + partyIndex + " "
            + (appOrResp ? "(Applicant)" : "(Respondent)") + ", "
            + appRepPrefix + "FN" + partyIndex + " " + appRepPrefix + "LN" + partyIndex + ", "
            + "Org" + partyIndex;
        assertEquals(label, appParty.getLabel());
        assertEquals(prefix + partyIndex, appParty.getCode());
    }

}
