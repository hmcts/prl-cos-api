package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class C100JsonMapperTest {

    @InjectMocks
    C100JsonMapper c100JsonMapper;
    @Mock
    CombinedMapper combinedMapper;
    @Mock
    ChildrenMapper childrenMapper;
    @Mock
    MiamMapper miamMapper;
    @Mock
    TypeOfApplicationMapper typeOfApplicationMapper;
    @Mock
    HearingUrgencyMapper hearingUrgencyMapper;
    @Mock
    AllegationsOfHarmMapper allegationOfHarmMapper;
    @Mock
    OtherPeopleInTheCaseMapper otherPeopleInTheCaseMapper;
    @Mock
    OtherProceedingsMapper otherproceedingsMapper;
    @Mock
    AttendingTheHearingMapper attendingTheHearingMapper;
    @Mock
    InternationalElementMapper internationalElementMapper;
    @Mock
    LitigationCapacityMapper litigationCapacityMapper;


    @Test
    public void testC100JsonMapperWithSomeFields() {
        CaseData caseData = CaseData.builder().courtId("CourtId").id(213123).feeAmount("312312")
            .familymanCaseNumber("123123").dateSubmitted("2019/1/2").build();
        assertNotNull(c100JsonMapper.map(caseData));
    }

    @Test
    public void testC100JsonMapperWithNullValuesInAllFields() {
        CaseData caseData = CaseData.builder().build();

        //when();
        //when(objectMapper.convertValue(partyDetails, Applicant.class)).thenReturn(applicant);
        assertNotNull(c100JsonMapper.map(caseData));
    }
}
