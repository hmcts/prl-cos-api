package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class HearingUrgencyMapperTest {

    @InjectMocks
    HearingUrgencyMapper hearingUrgencyMapper;


    @Test
    public void testHearingUrgencyMapperWithAllFields() {
        CaseData caseData = CaseData.builder().isCaseUrgent(YesOrNo.Yes).setOutReasonsBelow(
            "Hearing with Reduced notice required due to some reasons")
            .caseUrgencyTimeAndReason("Please attend it by tomorrow eod").effortsMadeWithRespondents(
                "I have sent out letters to each respondent")
            .doYouNeedAWithoutNoticeHearing(YesOrNo.Yes).areRespondentsAwareOfProceedings(YesOrNo.Yes)
            .reasonsForApplicationWithoutNotice(
                "Yes. I would need without notice hearing as this is quite urgent for me")
            .doYouRequireAHearingWithReducedNotice(YesOrNo.Yes).build();

        assertNotNull(hearingUrgencyMapper.map(caseData));

    }

    @Test
    public void testHearingUrgencyMapperWithSomeFields() {
        CaseData caseData = CaseData.builder().isCaseUrgent(YesOrNo.Yes).setOutReasonsBelow(
            "Hearing with Reduced notice required due to some reasons")
            .caseUrgencyTimeAndReason("Please attend it by tomorrow eod").effortsMadeWithRespondents(
                "I have sent out letters to each respondent")
            .doYouNeedAWithoutNoticeHearing(YesOrNo.Yes)
            .doYouRequireAHearingWithReducedNotice(YesOrNo.Yes).build();

        assertNotNull(hearingUrgencyMapper.map(caseData));

    }
}
