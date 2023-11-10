package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class OtherProceedingsCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    OtherProceedingsChecker otherProceedingsChecker;

    @Test
    public void startedWithPreviousOrOngoingProceedings() {
        CaseData caseData = CaseData.builder()
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.yes)
            .build();
        boolean isStarted = otherProceedingsChecker.isStarted(caseData);
        assertTrue(isStarted);
    }

    @Test
    public void notStartedWithoutPreviousOrOngoingProceedings() {
        CaseData caseData = CaseData.builder()
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.no)
            .build();
        boolean isStarted = otherProceedingsChecker.isStarted(caseData);
        assertFalse(isStarted);
    }

    @Test
    public void finishedIfNoPreviousOrOngoingProceedings() {
        CaseData caseData = CaseData.builder()
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.no)
            .build();
        boolean isFinished = otherProceedingsChecker.isFinished(caseData);
        assertTrue(isFinished);
    }

    @Test
    public void notFinishedWithPreviousOrOngoingProceedings() {
        CaseData caseData = CaseData.builder()
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.yes)
            .build();
        boolean isFinished = otherProceedingsChecker.isFinished(caseData);
        assertFalse(isFinished);
    }

    @Test
    public void finishedWithPreviousOrOngoingProceedingList() {

        ProceedingDetails proceedingDetails = ProceedingDetails.builder().build();
        Element<ProceedingDetails> wrappedProceedings = Element.<ProceedingDetails>builder().value(proceedingDetails).build();
        List<Element<ProceedingDetails>> listOfProceedings = Collections.singletonList(wrappedProceedings);


        CaseData caseData = CaseData.builder()
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.yes)
            .existingProceedings(listOfProceedings)
            .build();
        boolean isFinished = otherProceedingsChecker.isFinished(caseData);
        assertFalse(isFinished);
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(otherProceedingsChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
