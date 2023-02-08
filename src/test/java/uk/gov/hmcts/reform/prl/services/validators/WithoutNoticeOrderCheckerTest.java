package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.ReasonForOrderWithoutGivingNoticeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDetailsOfWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.ReasonForWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBailConditionDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class WithoutNoticeOrderCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    WithoutNoticeOrderChecker withoutNoticeOrderChecker;

    private CaseData caseData;
    private WithoutNoticeOrderDetails withoutNoticeOrderDetails;
    private ReasonForWithoutNoticeOrder reasonForWithoutNoticeOrder;
    private RespondentBailConditionDetails respondentBailConditionDetails;
    private OtherDetailsOfWithoutNoticeOrder otherDetailsOfWithoutNoticeOrder;

    @Before
    public void setUp() {
        caseData = CaseData.builder().build();
        withoutNoticeOrderDetails = WithoutNoticeOrderDetails.builder().build();
        reasonForWithoutNoticeOrder = ReasonForWithoutNoticeOrder.builder().build();
        respondentBailConditionDetails = RespondentBailConditionDetails.builder().build();
        otherDetailsOfWithoutNoticeOrder = OtherDetailsOfWithoutNoticeOrder.builder().build();
    }

    @Test
    public void whenNoWithoutOrderDetailsProvidedShouldReturnsFalse() {
        caseData = caseData.toBuilder().orderWithoutGivingNoticeToRespondent(withoutNoticeOrderDetails).build();
        assertFalse(withoutNoticeOrderChecker
                        .isStarted(caseData));

    }

    @Test
    public void whenWithoutOrderDetailsProvidedShouldReturnsTrue() {
        withoutNoticeOrderDetails = withoutNoticeOrderDetails.toBuilder().orderWithoutGivingNotice(YesOrNo.Yes).build();
        caseData = caseData.toBuilder().orderWithoutGivingNoticeToRespondent(
            withoutNoticeOrderDetails).build();
        assertTrue(withoutNoticeOrderChecker
                        .isStarted(caseData));
    }

    @Test
    public void whenNoDataHasMandatoryCompletedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();
        assertTrue(!withoutNoticeOrderChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenWithoutOrderDetailsProvidedShouldReturnsAsFinished() {
        withoutNoticeOrderDetails = withoutNoticeOrderDetails.toBuilder().orderWithoutGivingNotice(YesOrNo.Yes).build();
        reasonForWithoutNoticeOrder = reasonForWithoutNoticeOrder.toBuilder().reasonForOrderWithoutGivingNotice(
            Arrays.asList(ReasonForOrderWithoutGivingNoticeEnum.harmToApplicantOrChild)).build();
        respondentBailConditionDetails = respondentBailConditionDetails.toBuilder().isRespondentAlreadyInBailCondition(
            YesNoDontKnow.dontKnow).build();
        otherDetailsOfWithoutNoticeOrder = otherDetailsOfWithoutNoticeOrder.toBuilder().otherDetails("test").build();
        caseData = caseData.toBuilder().orderWithoutGivingNoticeToRespondent(
            withoutNoticeOrderDetails).reasonForOrderWithoutGivingNotice(reasonForWithoutNoticeOrder)
            .bailDetails(respondentBailConditionDetails).anyOtherDtailsForWithoutNoticeOrder(otherDetailsOfWithoutNoticeOrder).build();
        assertTrue(withoutNoticeOrderChecker
                       .isFinished(caseData));
    }

    @Test
    public void whenWithoutNoticeOrderReasonNotProvidedThenShouldNotReturnsAsFinished() {
        withoutNoticeOrderDetails = withoutNoticeOrderDetails.toBuilder().orderWithoutGivingNotice(YesOrNo.Yes).build();
        respondentBailConditionDetails = respondentBailConditionDetails.toBuilder().isRespondentAlreadyInBailCondition(
            YesNoDontKnow.dontKnow).build();
        caseData = caseData.toBuilder().orderWithoutGivingNoticeToRespondent(
                withoutNoticeOrderDetails).reasonForOrderWithoutGivingNotice(reasonForWithoutNoticeOrder)
            .bailDetails(respondentBailConditionDetails).build();
        assertFalse(withoutNoticeOrderChecker
                       .isFinished(caseData));
    }

    @Test
    public void whenBailConditionNotProvidedShouldReturnsAsFinished() {
        withoutNoticeOrderDetails = withoutNoticeOrderDetails.toBuilder().orderWithoutGivingNotice(YesOrNo.Yes).build();
        reasonForWithoutNoticeOrder = reasonForWithoutNoticeOrder.toBuilder().reasonForOrderWithoutGivingNotice(
            Arrays.asList(ReasonForOrderWithoutGivingNoticeEnum.harmToApplicantOrChild)).build();
        caseData = caseData.toBuilder().orderWithoutGivingNoticeToRespondent(
                withoutNoticeOrderDetails).reasonForOrderWithoutGivingNotice(reasonForWithoutNoticeOrder)
            .bailDetails(respondentBailConditionDetails).build();
        assertFalse(withoutNoticeOrderChecker
                       .isFinished(caseData));
    }

    @Test
    public void whenWithoutOrderDetailsProvidedAsNoShouldReturnsAsFinished() {
        withoutNoticeOrderDetails = withoutNoticeOrderDetails.toBuilder().orderWithoutGivingNotice(YesOrNo.No).build();
        caseData = caseData.toBuilder().orderWithoutGivingNoticeToRespondent(
                withoutNoticeOrderDetails).reasonForOrderWithoutGivingNotice(reasonForWithoutNoticeOrder)
            .bailDetails(respondentBailConditionDetails).anyOtherDtailsForWithoutNoticeOrder(otherDetailsOfWithoutNoticeOrder).build();
        assertTrue(withoutNoticeOrderChecker
                       .isFinished(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(withoutNoticeOrderChecker.getDefaultTaskState(CaseData.builder().build()));
    }


    @Test
    public void whenWithoutOrderDetailsProvidedShouldReturnsFalse() {
        withoutNoticeOrderDetails = withoutNoticeOrderDetails.toBuilder().orderWithoutGivingNotice(null).build();
        caseData = caseData.toBuilder().orderWithoutGivingNoticeToRespondent(
            withoutNoticeOrderDetails).build();
        assertTrue(!withoutNoticeOrderChecker
                       .isStarted(caseData));
    }

}
