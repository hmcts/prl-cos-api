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

@RunWith(MockitoJUnitRunner.class)
public class OtherProceedingsCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    OtherProceedingsChecker otherProceedingsChecker;

    @Test
    public void StartedWithPreviousOrOngoingProceedings(){
        CaseData caseData=CaseData.builder()
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.YES)
            .build();
        boolean isStarted = otherProceedingsChecker.isStarted(caseData);
        assert (isStarted);
    }

    @Test
    public void NotStartedWithoutPreviousOrOngoingProceedings(){
        CaseData caseData=CaseData.builder()
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.NO)
            .build();
        boolean isStarted = otherProceedingsChecker.isStarted(caseData);
        assert (!isStarted);
    }

    @Test
    public void FinishedIfNoPreviousOrOngoingProceedings(){
        CaseData caseData=CaseData.builder()
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.NO)
            .build();
        boolean isFinished = otherProceedingsChecker.isFinished(caseData);
        assert (isFinished);
    }

    @Test
    public void NotFinishedWithPreviousOrOngoingProceedings(){
        CaseData caseData=CaseData.builder()
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.YES)
            .build();
        boolean isFinished = otherProceedingsChecker.isFinished(caseData);
        assert (!isFinished);
    }

    @Test
    public void FinishedWithPreviousOrOngoingProceedingList(){

        ProceedingDetails proceedingDetails = ProceedingDetails.builder().build();
        Element<ProceedingDetails> wrappedProceedings = Element.<ProceedingDetails>builder().value(proceedingDetails).build();
        List<Element<ProceedingDetails>> listOfProceedings = Collections.singletonList(wrappedProceedings);


        CaseData caseData=CaseData.builder()
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.YES)
            .otherProceedings(listOfProceedings)
            .build();
        boolean isFinished = otherProceedingsChecker.isFinished(caseData);
        assert (!isFinished);
    }

}
