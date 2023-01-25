package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherChildrenNotInTheCase;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

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
    ChildDetailsRevisedMapper childDetailsRevisedMapper;
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
    OtherChildrenNotInTheCase otherChildrenNotInTheCase;
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
            .familymanCaseNumber("123123")
            .dateSubmitted("2019/1/2").build();
        assertNotNull(c100JsonMapper.map(caseData));
    }

    @Test
    public void testC100JsonMapperWithNullValuesInAllFields() {
        CaseData caseData = CaseData.builder().build();

        //when();
        //when(objectMapper.convertValue(partyDetails, Applicant.class)).thenReturn(applicant);
        assertNotNull(c100JsonMapper.map(caseData));
    }

    @Test
    public void testC100JsonMapperWithChildDetailsRevised() {
        ChildDetailsRevised child = ChildDetailsRevised.builder().build();
        Element<ChildDetailsRevised> wrappedChildren = Element.<ChildDetailsRevised>builder().value(child).build();
        List<Element<ChildDetailsRevised>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().courtId("CourtId").id(213123).feeAmount("312312")
            .isNewCaseCreatedFlagForChildDetails(YesOrNo.Yes)
            .newChildDetails(listOfChildren)
            .familymanCaseNumber("123123").dateSubmitted("2019/1/2").build();
        assertNotNull(c100JsonMapper.map(caseData));
    }

    @Ignore
    @Test
    public void testC100JsonMapperWithOtherChildrenNotInTheCase() {

        OtherChildrenNotInTheCase childrenNotInTheCase = OtherChildrenNotInTheCase.builder()
                         .firstName("Test")
                         .lastName("Last")
                         .dateOfBirth(LocalDate.of(1990, 8, 1))
                         .gender(Gender.male)
                         .build();
        Element<OtherChildrenNotInTheCase> wrappedOtherChildrenNotInTheCase =
            Element.<OtherChildrenNotInTheCase>builder().value(childrenNotInTheCase).build();
        List<Element<OtherChildrenNotInTheCase>> listOfOtherChildrenNotInTheCase = Collections.singletonList(wrappedOtherChildrenNotInTheCase);

        CaseData caseData = CaseData.builder().courtId("CourtId").id(213123).feeAmount("312312")
            .childrenNotInTheCase(listOfOtherChildrenNotInTheCase)
            .familymanCaseNumber("123123").dateSubmitted("2019/1/2").build();
        assertNotNull(c100JsonMapper.map(caseData));
    }

}
