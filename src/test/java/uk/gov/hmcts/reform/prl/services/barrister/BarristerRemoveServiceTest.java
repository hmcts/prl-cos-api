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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN;

@ExtendWith(SpringExtension.class)
class BarristerRemoveServiceTest extends BarristerTestAbstract {
    @InjectMocks
    BarristerRemoveService barristerRemoveService;
    @Mock
    protected UserService userService;
    @Mock
    protected OrganisationService organisationService;
    @Mock
    protected EventService eventPublisher;

    @BeforeEach
    public void setup() {
        barristerRemoveService = new BarristerRemoveService(userService, organisationService, eventPublisher);
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

        AllocatedBarrister allocatedBarrister = barristerRemoveService.getBarristerListToRemove(caseData, AUTHORISATION);
        DynamicList listOfBarristersToRemove = allocatedBarrister.getPartyList();

        assertEquals(listOfBarristersToRemove.getValue(), null);
        assertEquals(2, listOfBarristersToRemove.getListItems().size());

        assertPartyToRemove(listOfBarristersToRemove, true, PARTY_ID_PREFIX, 0, 3);
        assertPartyToRemove(listOfBarristersToRemove, false, PARTY_ID_PREFIX, 1, 7);
    }

    protected void assertPartyToRemove(DynamicList listOfBarristersToRemove, boolean appResp, String prefix, int itemIndex, int partyIndex) {
        String appRepPrefix = appResp ? "App" : "Resp";
        DynamicListElement appParty = listOfBarristersToRemove.getListItems().get(itemIndex);
        String label = appRepPrefix + "FN" + partyIndex + " " + appRepPrefix + "LN" + partyIndex + " "
            + (appResp ? "(Applicant)" : "(Respondent)") + ", "
            + appRepPrefix + "FN" + partyIndex + " " + appRepPrefix + "LN" + partyIndex + ", "
            + "BarFN" + partyIndex + " " + "BarLN" + partyIndex;
        assertEquals(label, appParty.getLabel());
        assertEquals(prefix + partyIndex, appParty.getCode());
    }

}
