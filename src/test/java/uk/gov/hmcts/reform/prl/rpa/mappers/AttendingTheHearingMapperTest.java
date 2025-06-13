package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.InterpreterNeed;
import uk.gov.hmcts.reform.prl.models.complextypes.WelshNeed;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.applicant;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.respondent;
import static uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum.spoken;
import static uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum.written;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class AttendingTheHearingMapperTest {

    @InjectMocks
    AttendingTheHearingMapper attendingTheHearingMapper;

    @Test
    void testApplicantsMapperWithAllFields() {
        InterpreterNeed interpreterNeed = InterpreterNeed.builder().party(List.of(applicant, respondent))
            .name("Name").language("English").otherAssistance("yes").build();
        Element<InterpreterNeed> interpreterNeedElement = Element.<InterpreterNeed>builder().value(interpreterNeed).build();
        List<Element<InterpreterNeed>> interpreterNeeds = List.of(interpreterNeedElement);

        WelshNeed welshNeed = WelshNeed.builder().whoNeedsWelsh("Names of Welsh People")
            .spokenOrWritten(List.of(spoken, written)).build();
        Element<WelshNeed> welshNeedElement = Element.<WelshNeed>builder().value(welshNeed).build();
        List<Element<WelshNeed>> welshNeeds = List.of(welshNeedElement);

        CaseData caseData = CaseData.builder().attendHearing(
            AttendHearing.builder()
                .isWelshNeeded(Yes)
                .welshNeeds(welshNeeds).isIntermediaryNeeded(Yes).interpreterNeeds(interpreterNeeds)
                .isDisabilityPresent(No).adjustmentsRequired("Adjustments Required details")
                .isSpecialArrangementsRequired(No).specialArrangementsRequired("Special Arrangements Required")
                .isIntermediaryNeeded(Yes)
                .build()
            )
            .build();
        assertNotNull(attendingTheHearingMapper.map(caseData));
    }

    @Test
    void testApplicantsMapperWithNullValuesInWelsh() {
        CaseData caseData = CaseData.builder().attendHearing(
            AttendHearing.builder()
                .isWelshNeeded(Yes)
                .welshNeeds(null).isIntermediaryNeeded(Yes).interpreterNeeds(null)
                .isDisabilityPresent(No).adjustmentsRequired("Adjustments Required details")
                .isSpecialArrangementsRequired(No).specialArrangementsRequired("Special Arrangements Required")
                .build()
        ).build();
        assertNotNull(attendingTheHearingMapper.map(caseData));
    }

    @Test
    void testApplicantsMapperWithNullValuesInterpreter() {
        List<SpokenOrWrittenWelshEnum> welshEnum = List.of(spoken, written);

        WelshNeed welshNeed = WelshNeed.builder().whoNeedsWelsh("Names of Welsh People")
            .spokenOrWritten(welshEnum).build();
        Element<WelshNeed> welshNeedElement = Element.<WelshNeed>builder().value(welshNeed).build();
        List<Element<WelshNeed>> welshNeeds = List.of(welshNeedElement);

        CaseData caseData = CaseData.builder().attendHearing(AttendHearing.builder()
                                                                 .isWelshNeeded(Yes)
                                                                 .welshNeeds(welshNeeds).isIntermediaryNeeded(Yes)
                                                                 .isDisabilityPresent(No).adjustmentsRequired("Adjustments Required details")
                                                                 .isSpecialArrangementsRequired(No)
                                                                 .specialArrangementsRequired("Special Arrangements Required")
                                                                 .build()
            )
            .build();

        assertNotNull(attendingTheHearingMapper.map(caseData));
    }

    @Test
    void testApplicantsMapperWithSomeFields() {

        WelshNeed welshNeed = WelshNeed.builder().whoNeedsWelsh("Names of Welsh People")
            .spokenOrWritten(List.of(spoken, written)).build();
        Element<WelshNeed> welshNeedElement = Element.<WelshNeed>builder().value(welshNeed).build();
        List<Element<WelshNeed>> welshNeeds = List.of(welshNeedElement);

        CaseData caseData = CaseData.builder().attendHearing(AttendHearing.builder()
                                                                 .welshNeeds(welshNeeds).isIntermediaryNeeded(Yes)
                                                                 .isWelshNeeded(Yes)
                                                                 .isDisabilityPresent(No).adjustmentsRequired("Adjustments Required details")
                                                                 .isSpecialArrangementsRequired(No)
                                                                 .specialArrangementsRequired("Special Arrangements Required")
                                                                 .build())
            .build();

        assertNotNull(attendingTheHearingMapper.map(caseData));
    }
}
