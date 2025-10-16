package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseNameServiceTest {

    @InjectMocks
    CaseNameService caseNameService;

    @Mock
    CaseData caseData;
    @Mock
    PartyDetails applicant;
    @Mock
    PartyDetails respondent;

    Map<String, Object> updatedCaseData = new HashMap<>();

    @Before
    public void setup() {
    }

    @Test
    public void shouldGetFinalCaseNameC100() {
        assertEquals("app1LN V respLN", caseNameService.getCaseNameForCA("app1LN", "respLN"));
    }

    @Test
    public void shouldGetFinalCaseNameFL01() {
        assertEquals("app1FN app1LN & respFN respLN",
                     caseNameService.getCaseNameForDA("app1FN", "app1LN", "respFN", "respLN"));
    }

    @Test
    public void shouldSetFinalCaseNameC100() {
        when(applicant.getLastName()).thenReturn("AppLN");
        when(respondent.getLastName()).thenReturn("RespLN");

        List<Element<PartyDetails>> applicantsList = List.of(Element.<PartyDetails>builder().value(applicant).build());
        List<Element<PartyDetails>> respondentsList = List.of(Element.<PartyDetails>builder().value(respondent).build());
        when(caseData.getApplicants()).thenReturn(applicantsList);
        when(caseData.getRespondents()).thenReturn(respondentsList);
        when(caseData.getCaseTypeOfApplication()).thenReturn("C100");

        caseNameService.setFinalCaseName(updatedCaseData, caseData);

        assertEquals("AppLN V RespLN", updatedCaseData.get("applicantCaseName"));
    }

    @Test
    public void shouldSetFinalCaseNameFL401() {
        when(applicant.getFirstName()).thenReturn("AppFN");
        when(applicant.getLastName()).thenReturn("AppLN");
        when(respondent.getFirstName()).thenReturn("RespFN");
        when(respondent.getLastName()).thenReturn("RespLN");

        when(caseData.getApplicantsFL401()).thenReturn(applicant);
        when(caseData.getRespondentsFL401()).thenReturn(respondent);
        when(caseData.getCaseTypeOfApplication()).thenReturn("FL401");

        caseNameService.setFinalCaseName(updatedCaseData, caseData);

        assertEquals("AppFN AppLN & RespFN RespLN", updatedCaseData.get("applicantCaseName"));

    }

    @Test
    public void shouldUpdateFinalCaseNameC100() {
        updatedCaseData.put("applicantCaseName", "CaseNameBefore");
        when(applicant.getLastName()).thenReturn("AppLN");
        when(respondent.getLastName()).thenReturn("RespLN");

        List<Element<PartyDetails>> applicantsList = List.of(Element.<PartyDetails>builder().value(applicant).build());
        List<Element<PartyDetails>> respondentsList = List.of(Element.<PartyDetails>builder().value(respondent).build());
        when(caseData.getApplicants()).thenReturn(applicantsList);
        when(caseData.getRespondents()).thenReturn(respondentsList);
        when(caseData.getCaseTypeOfApplication()).thenReturn("C100");

        caseNameService.setFinalCaseName(updatedCaseData, caseData);

        assertEquals("AppLN V RespLN", updatedCaseData.get("applicantCaseName"));
    }

    @Test
    public void shouldUpdateFinalCaseNameFL401() {
        updatedCaseData.put("applicantCaseName", "CaseNameBefore");
        when(applicant.getFirstName()).thenReturn("AppFN");
        when(applicant.getLastName()).thenReturn("AppLN");
        when(respondent.getFirstName()).thenReturn("RespFN");
        when(respondent.getLastName()).thenReturn("RespLN");

        when(caseData.getApplicantsFL401()).thenReturn(applicant);
        when(caseData.getRespondentsFL401()).thenReturn(respondent);
        when(caseData.getCaseTypeOfApplication()).thenReturn("FL401");

        caseNameService.setFinalCaseName(updatedCaseData, caseData);

        assertEquals("AppFN AppLN & RespFN RespLN", updatedCaseData.get("applicantCaseName"));

    }

    @Test
    public void shouldNotSetFinalCaseNameC100WhenApplicantsEmpty() {
        List<Element<PartyDetails>> applicantsList = new ArrayList<>();
        when(caseData.getApplicants()).thenReturn(applicantsList);
        when(caseData.getCaseTypeOfApplication()).thenReturn("C100");

        caseNameService.setFinalCaseName(updatedCaseData, caseData);

        assertNull(updatedCaseData.get("applicantCaseName"));
    }

    @Test
    public void shouldNotSetFinalCaseNameC100WhenRespondentsEmpty() {
        List<Element<PartyDetails>> applicantsList = List.of(Element.<PartyDetails>builder().value(applicant).build());
        List<Element<PartyDetails>> respondentsList = new ArrayList<>();
        when(caseData.getApplicants()).thenReturn(applicantsList);
        when(caseData.getRespondents()).thenReturn(respondentsList);
        when(caseData.getCaseTypeOfApplication()).thenReturn("C100");

        caseNameService.setFinalCaseName(updatedCaseData, caseData);

        assertNull(updatedCaseData.get("applicantCaseName"));
    }

    @Test
    public void shouldNotSetFinalCaseNameFL401WhenApplicantNull() {
        when(caseData.getCaseTypeOfApplication()).thenReturn("FL401");

        caseNameService.setFinalCaseName(updatedCaseData, caseData);

        assertNull(updatedCaseData.get("applicantCaseName"));
    }

    @Test
    public void shouldNotSetFinalCaseNameFL401WhenRespondentNull() {
        when(caseData.getCaseTypeOfApplication()).thenReturn("FL401");
        when(caseData.getApplicantsFL401()).thenReturn(applicant);

        caseNameService.setFinalCaseName(updatedCaseData, caseData);

        assertNull(updatedCaseData.get("applicantCaseName"));
    }

    @Test
    public void shouldNotSetFinalCaseNameForInvalidCaseType() {
        when(caseData.getId()).thenReturn(1234L);
        when(caseData.getCaseTypeOfApplication()).thenReturn("INVALID");

        RuntimeException rte = assertThrows(RuntimeException.class, () ->
            caseNameService.setFinalCaseName(updatedCaseData, caseData)
        );

        assertEquals("Invalid caseTypeOfApplication found for case 1234", rte.getMessage());
    }

}
