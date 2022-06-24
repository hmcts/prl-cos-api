package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.InterpreterNeed;
import uk.gov.hmcts.reform.prl.models.complextypes.WelshNeed;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class AttendingTheHearingMapperTest {

    @InjectMocks
    AttendingTheHearingMapper attendingTheHearingMapper;

    InterpreterNeed interpreterNeed;
    WelshNeed welshNeed;
    List<Element<InterpreterNeed>> interpreterNeeds;
    List<Element<WelshNeed>> welshNeeds;


    @Test
    public void testApplicantsMapperWithAllFields() {
        List<PartyEnum> partyType = new ArrayList<>();
        partyType.add(PartyEnum.applicant);
        partyType.add(PartyEnum.respondent);
        interpreterNeed = InterpreterNeed.builder().party(partyType)
            .name("Name").language("English").otherAssistance("yes").build();
        Element<InterpreterNeed> interpreterNeedElement = Element.<InterpreterNeed>builder().value(interpreterNeed).build();
        interpreterNeeds = Collections.singletonList(interpreterNeedElement);

        List<SpokenOrWrittenWelshEnum> welshEnum = new ArrayList<>();
        welshEnum.add(SpokenOrWrittenWelshEnum.spoken);
        welshEnum.add(SpokenOrWrittenWelshEnum.written);

        welshNeed = WelshNeed.builder().whoNeedsWelsh("Names of Welsh People")
            .spokenOrWritten(welshEnum).build();
        Element<WelshNeed> welshNeedElement = Element.<WelshNeed>builder().value(welshNeed).build();
        welshNeeds = Collections.singletonList(welshNeedElement);

        CaseData caseData = CaseData.builder().isWelshNeeded(YesOrNo.Yes)
            .welshNeeds(welshNeeds).isIntermediaryNeeded(YesOrNo.Yes).interpreterNeeds(interpreterNeeds)
            .isDisabilityPresent(YesOrNo.No).adjustmentsRequired("Adjustments Required details")
            .isSpecialArrangementsRequired(YesOrNo.No).specialArrangementsRequired("Special Arrangements Required")
            .isIntermediaryNeeded(YesOrNo.Yes).build();
        assertNotNull(attendingTheHearingMapper.map(caseData));

    }

    @Test
    public void testApplicantsMapperWithNullValuesInWelsh() {
        List<PartyEnum> partyType = new ArrayList<>();
        partyType.add(PartyEnum.applicant);
        partyType.add(PartyEnum.respondent);
        interpreterNeeds = null;
        CaseData caseData = CaseData.builder().isWelshNeeded(YesOrNo.Yes)
            .welshNeeds(null).isIntermediaryNeeded(YesOrNo.Yes).interpreterNeeds(interpreterNeeds)
            .isDisabilityPresent(YesOrNo.No).adjustmentsRequired("Adjustments Required details")
            .isSpecialArrangementsRequired(YesOrNo.No).specialArrangementsRequired("Special Arrangements Required")
            .build();
        assertNotNull(attendingTheHearingMapper.map(caseData));

    }


    @Test
    public void testApplicantsMapperWithNullValuesInterpreter() {
        List<SpokenOrWrittenWelshEnum> welshEnum = new ArrayList<>();
        welshEnum.add(SpokenOrWrittenWelshEnum.spoken);
        welshEnum.add(SpokenOrWrittenWelshEnum.written);

        welshNeed = WelshNeed.builder().whoNeedsWelsh("Names of Welsh People")
            .spokenOrWritten(welshEnum).build();
        Element<WelshNeed> welshNeedElement = Element.<WelshNeed>builder().value(welshNeed).build();
        welshNeeds = Collections.singletonList(welshNeedElement);

        CaseData caseData = CaseData.builder().isWelshNeeded(YesOrNo.Yes)
            .welshNeeds(welshNeeds).isIntermediaryNeeded(YesOrNo.Yes)
            .isDisabilityPresent(YesOrNo.No).adjustmentsRequired("Adjustments Required details")
            .isSpecialArrangementsRequired(YesOrNo.No).specialArrangementsRequired("Special Arrangements Required")
            .build();
        assertNotNull(attendingTheHearingMapper.map(caseData));

    }

    @Test
    public void testApplicantsMapperWithSomeFields() {
        List<PartyEnum> partyType = new ArrayList<>();
        partyType.add(PartyEnum.applicant);
        partyType.add(PartyEnum.respondent);
        interpreterNeed = InterpreterNeed.builder().party(partyType)
            .name("Name").language("English").otherAssistance("yes").build();
        Element<InterpreterNeed> interpreterNeedElement = Element.<InterpreterNeed>builder().value(interpreterNeed).build();
        interpreterNeeds = Collections.singletonList(interpreterNeedElement);

        List<SpokenOrWrittenWelshEnum> welshEnum = new ArrayList<>();
        welshEnum.add(SpokenOrWrittenWelshEnum.spoken);
        welshEnum.add(SpokenOrWrittenWelshEnum.written);

        welshNeed = WelshNeed.builder().whoNeedsWelsh("Names of Welsh People")
            .spokenOrWritten(welshEnum).build();
        Element<WelshNeed> welshNeedElement = Element.<WelshNeed>builder().value(welshNeed).build();
        welshNeeds = Collections.singletonList(welshNeedElement);

        CaseData caseData = CaseData.builder().isWelshNeeded(YesOrNo.Yes)
            .welshNeeds(welshNeeds).isIntermediaryNeeded(YesOrNo.Yes)
            .isDisabilityPresent(YesOrNo.No).adjustmentsRequired("Adjustments Required details")
            .isSpecialArrangementsRequired(YesOrNo.No).specialArrangementsRequired("Special Arrangements Required")
            .build();
        assertNotNull(attendingTheHearingMapper.map(caseData));

    }


}


