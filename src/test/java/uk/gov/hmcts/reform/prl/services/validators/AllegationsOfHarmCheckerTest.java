package uk.gov.hmcts.reform.prl.services.validators;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;

import static uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren.APPLICANTS;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;

@RunWith(MockitoJUnitRunner.class)
public class AllegationsOfHarmCheckerTest {

    @Mock
    TaskErrorService taskErrorService;
    @InjectMocks
    AllegationsOfHarmChecker allegationsOfHarmChecker;


    @Test
    public void whenNoCaseDataThenIsStartedIsFalse() {

        CaseData casedata = CaseData.builder().build();

        assert !allegationsOfHarmChecker.isStarted(casedata);

    }

    @Test
    public void whenPartialCaseDataThenIsStartedTrue() {
        CaseData casedata = CaseData.builder()
            .allegationsOfHarmYesNo(YES)
            .build();

        assert  allegationsOfHarmChecker.isStarted(casedata);
    }



    @Test
    public void whenNoCaseDataThenNotFinished(){

        CaseData casedata = CaseData.builder().build();

        boolean isFinished = allegationsOfHarmChecker.isFinished(casedata);

        assert(!isFinished);
    }

    @Test
    public void FinishedFieldsValidatedToTrue(){
        CaseData casedata = CaseData.builder()
            .allegationsOfHarmYesNo(NO)
            .build();

        boolean isFinished = allegationsOfHarmChecker.isFinished(casedata);

        assert(isFinished);
    }

    @Test
    public void ValidateAbusePresentFalse(){
        CaseData casedata = CaseData.builder().build();

        boolean isAbusePresent = allegationsOfHarmChecker.isStarted(casedata);

        assert(!isAbusePresent);
    }

    @Test
    public void whenNoCaseDataThenHasMandatoryFalse() {

        CaseData casedata = CaseData.builder().build();

        assert !allegationsOfHarmChecker.hasMandatoryCompleted(casedata);

    }

    @Test
    public void whenFinishedCaseDataThenHasMandatoryFalse() {

        CaseData casedata = CaseData.builder()
            .allegationsOfHarmYesNo(NO)
        .build();

        assert !allegationsOfHarmChecker.hasMandatoryCompleted(casedata);

    }

    @Test
    public void whenNoCaseDataValidateFieldsReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assert !allegationsOfHarmChecker.validateFields(caseData);
    }

    @Test
    public void whenAbuseDataPresentThenAbusePresentReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .allegationsOfHarmDomesticAbuseYesNo(YES)
            .physicalAbuseVictim(Collections.singletonList(APPLICANTS))
            .build();

    }





}
