package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class PartyRepresentationServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PartyRepresentationService partyRepresentationService;

    @Test
    public void areAnyPartiesRepresentedShouldReturnTrueForC100WhenApplicantHasSolicitorOrg() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(element(PartyDetails.builder()
                .solicitorOrg(Organisation.builder().organisationID("ORG123").build())
                .build())))
            .respondents(List.of(element(PartyDetails.builder().build())))
            .build();

        assertTrue(partyRepresentationService.areAnyPartiesRepresented(caseData));
    }

    @Test
    public void areAnyPartiesRepresentedShouldReturnTrueForC100WhenApplicantHasRepresentativeFirstName() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(element(PartyDetails.builder()
                .representativeFirstName("John")
                .build())))
            .respondents(List.of(element(PartyDetails.builder().build())))
            .build();

        assertTrue(partyRepresentationService.areAnyPartiesRepresented(caseData));
    }

    @Test
    public void areAnyPartiesRepresentedShouldReturnTrueForC100WhenApplicantHasRepresentativeLastName() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(element(PartyDetails.builder()
                .representativeLastName("Smith")
                .build())))
            .respondents(List.of(element(PartyDetails.builder().build())))
            .build();

        assertTrue(partyRepresentationService.areAnyPartiesRepresented(caseData));
    }

    @Test
    public void areAnyPartiesRepresentedShouldReturnTrueForC100WhenApplicantHasSolicitorEmail() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(element(PartyDetails.builder()
                .solicitorEmail("solicitor@example.com")
                .build())))
            .respondents(List.of(element(PartyDetails.builder().build())))
            .build();

        assertTrue(partyRepresentationService.areAnyPartiesRepresented(caseData));
    }

    @Test
    public void areAnyPartiesRepresentedShouldReturnTrueForC100WhenApplicantHasSolicitorTelephone() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(element(PartyDetails.builder()
                .solicitorTelephone("01234567890")
                .build())))
            .respondents(List.of(element(PartyDetails.builder().build())))
            .build();

        assertTrue(partyRepresentationService.areAnyPartiesRepresented(caseData));
    }

    @Test
    public void areAnyPartiesRepresentedShouldReturnFalseForC100WhenNoPartiesRepresented() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(element(PartyDetails.builder().build())))
            .respondents(List.of(element(PartyDetails.builder().build())))
            .build();

        assertFalse(partyRepresentationService.areAnyPartiesRepresented(caseData));
    }

    @Test
    public void areAnyPartiesRepresentedShouldReturnFalseForC100WhenSolicitorOrgHasNoId() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(element(PartyDetails.builder()
                .solicitorOrg(Organisation.builder().build())
                .build())))
            .respondents(List.of(element(PartyDetails.builder().build())))
            .build();

        assertFalse(partyRepresentationService.areAnyPartiesRepresented(caseData));
    }

    @Test
    public void areAnyPartiesRepresentedShouldReturnTrueForC100WhenRespondentRepresented() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(element(PartyDetails.builder().build())))
            .respondents(List.of(element(PartyDetails.builder()
                .representativeFirstName("Jane")
                .build())))
            .build();

        assertTrue(partyRepresentationService.areAnyPartiesRepresented(caseData));
    }

    @Test
    public void areAnyPartiesRepresentedShouldReturnFalseForC100WithNullLists() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();

        assertFalse(partyRepresentationService.areAnyPartiesRepresented(caseData));
    }

    @Test
    public void areAnyPartiesRepresentedShouldReturnTrueForFL401WhenApplicantRepresented() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(PartyDetails.builder()
                .solicitorOrg(Organisation.builder().organisationID("ORG1").build())
                .build())
            .respondentsFL401(PartyDetails.builder().build())
            .build();

        assertTrue(partyRepresentationService.areAnyPartiesRepresented(caseData));
    }

    @Test
    public void areAnyPartiesRepresentedShouldReturnTrueForFL401WhenRespondentRepresented() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(PartyDetails.builder().build())
            .respondentsFL401(PartyDetails.builder()
                .solicitorEmail("resp-solicitor@example.com")
                .build())
            .build();

        assertTrue(partyRepresentationService.areAnyPartiesRepresented(caseData));
    }

    @Test
    public void areAnyPartiesRepresentedShouldReturnFalseForFL401WhenNoPartiesRepresented() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(PartyDetails.builder().build())
            .respondentsFL401(PartyDetails.builder().build())
            .build();

        assertFalse(partyRepresentationService.areAnyPartiesRepresented(caseData));
    }

    @Test(expected = IllegalArgumentException.class)
    public void areAnyPartiesRepresentedShouldThrowWhenNoCaseType() {
        CaseData caseData = CaseData.builder().build();

        partyRepresentationService.areAnyPartiesRepresented(caseData);
    }

    @Test
    public void areNoPartiesRepresentedWithCaseDetailsShouldReturnTrue() {
        CaseDetails caseDetails = CaseDetails.builder().id(1234567890L).build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(element(PartyDetails.builder().build())))
            .respondents(List.of(element(PartyDetails.builder().build())))
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class))
            .thenReturn(caseData);

        assertTrue(partyRepresentationService.areNoPartiesRepresented(caseDetails));
    }

    @Test
    public void areNoPartiesRepresentedWithCaseDetailsShouldReturnFalse() {
        CaseDetails caseDetails = CaseDetails.builder().id(1234567890L).build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(element(PartyDetails.builder()
                .solicitorEmail("solicitor@example.com")
                .build())))
            .respondents(List.of(element(PartyDetails.builder().build())))
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class))
            .thenReturn(caseData);

        assertFalse(partyRepresentationService.areNoPartiesRepresented(caseDetails));
    }

    private static Element<PartyDetails> element(PartyDetails partyDetails) {
        return Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(partyDetails)
            .build();
    }
}