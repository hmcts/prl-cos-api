package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherChildrenNotInTheCase;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Relations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import javax.json.JsonValue;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;

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
    OtherProceedingsMapper otherproceedingsMapper;
    @Mock
    OtherChildrenNotInTheCaseMapper otherChildrenNotInTheCaseMapper;
    @Mock
    OtherPeopleInTheCaseRevisedMapper otherPeopleInTheCaseRevisedMapper;
    @Mock
    AttendingTheHearingMapper attendingTheHearingMapper;
    @Mock
    InternationalElementMapper internationalElementMapper;
    @Mock
    LitigationCapacityMapper litigationCapacityMapper;
    @Mock
    ChildrenAndApplicantsMapper childrenAndApplicantsMapper;
    @Mock
    ChildrenAndRespondentsMapper childrenAndRespondentsMapper;
    @Mock
    ChildrenAndOtherPeopleMapper childrenAndOtherPeopleMapper;

    @Mock
    AllegationsOfHarmRevisedMapper allegationsOfHarmRevisedMapper;

    @Test
    public void testC100JsonMapperWithSomeFields() {
        OtherChildrenNotInTheCase child = OtherChildrenNotInTheCase.builder().firstName("Lewis").lastName("Christine")
            .dateOfBirth(LocalDate.of(1990, 8, 1))
            .gender(Gender.male).otherGender("").build();
        Element<OtherChildrenNotInTheCase> childElement = Element.<OtherChildrenNotInTheCase>builder().value(child).build();
        List<Element<OtherChildrenNotInTheCase>> children = Collections.singletonList(childElement);
        CaseData caseData = CaseData.builder().courtId("CourtId").id(213123).feeAmount("312312")
            .familymanCaseNumber("123123")
            .childrenNotInTheCase(children)
            .dateSubmitted("2019/1/2").build();
        when(otherChildrenNotInTheCaseMapper.map(children)).thenReturn(JsonValue.EMPTY_JSON_ARRAY);

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
        when(otherChildrenNotInTheCaseMapper.map(listOfOtherChildrenNotInTheCase)).thenReturn(JsonValue.EMPTY_JSON_ARRAY);
        assertNotNull(c100JsonMapper.map(caseData));
    }


    @Test
    public void testC100JsonMapperForV2() {

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
                .relations(Relations.builder().build())
            .taskListVersion(TASK_LIST_VERSION_V2)
            .childrenNotInTheCase(listOfOtherChildrenNotInTheCase)
            .familymanCaseNumber("123123").dateSubmitted("2019/1/2").build();
        when(otherChildrenNotInTheCaseMapper.map(listOfOtherChildrenNotInTheCase)).thenReturn(JsonValue.EMPTY_JSON_ARRAY);
        assertNotNull(c100JsonMapper.map(caseData));
    }
}
