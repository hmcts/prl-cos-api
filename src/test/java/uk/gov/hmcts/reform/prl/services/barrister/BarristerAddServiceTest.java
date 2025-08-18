package uk.gov.hmcts.reform.prl.services.barrister;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN;

@ExtendWith(SpringExtension.class)
class BarristerAddServiceTest extends BarristerTestAbstract {
    @InjectMocks
    BarristerAddService barristerAddService;
    @Mock
    protected UserService userService;
    @Mock
    protected OrganisationService organisationService;
    @Mock
    protected EventService eventPublisher;

    @BeforeEach
    public void setup() {
        barristerAddService = new BarristerAddService(userService, organisationService, eventPublisher);
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

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(caseData, AUTHORISATION);
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

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(caseData, AUTHORISATION);
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

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(caseData, AUTHORISATION);
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

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(caseData, AUTHORISATION);
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

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(caseData, AUTHORISATION);
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

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(caseData, AUTHORISATION);
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
            () -> barristerAddService.getAllocatedBarrister(caseData, AUTHORISATION)
        );
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
