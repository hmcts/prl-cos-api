package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.YesNoIDontKnow;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class KeepDetailsPrivateCheckerTest {

    @InjectMocks
    KeepDetailsPrivateChecker keepDetailsPrivateChecker;

    @Mock
    RespondentTaskErrorService respondentTaskErrorService;

    CaseData caseData;

    PartyDetails respondent;

    @BeforeEach
    void setUp() {

        List<ConfidentialityListEnum> confidentialityListEnums = new ArrayList<>();

        confidentialityListEnums.add(ConfidentialityListEnum.email);
        confidentialityListEnums.add(ConfidentialityListEnum.phoneNumber);

        respondent = PartyDetails.builder()
            .response(Response
                          .builder()
                          .keepDetailsPrivate(KeepDetailsPrivate
                                                  .builder()
                                                  .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                                  .confidentiality(Yes)
                                                  .confidentialityList(confidentialityListEnums)
                                                  .build())
                          .build())
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);
        doNothing().when(respondentTaskErrorService).addEventError(Mockito.any(), Mockito.any(), Mockito.any());
        caseData = CaseData.builder().respondents(respondentList).build();
    }

    @Test
    void isStartedTest() {
        boolean anyNonEmpty = keepDetailsPrivateChecker.isStarted(respondent, true);

        assertTrue(anyNonEmpty);
    }

    @Test
    void isStartedWithoutRespKeepDetailsPrivateTest() {
        respondent = PartyDetails.builder()
            .response(Response
                          .builder()
                          .keepDetailsPrivate(KeepDetailsPrivate
                                                  .builder()
                                                  .build())
                          .build())
            .build();

        boolean anyNonEmpty = keepDetailsPrivateChecker.isStarted(respondent, true);
        assertFalse(anyNonEmpty);
    }

    @Test
    void hasMandatoryCompletedTest() {
        boolean anyNonEmpty = keepDetailsPrivateChecker.isFinished(respondent, true);
        assertTrue(anyNonEmpty);
    }

    @Test
    void hasMandatoryCompletedWithoutRespondentTest() {
        respondent = null;
        boolean anyNonEmpty = keepDetailsPrivateChecker.isFinished(respondent, true);
        assertFalse(anyNonEmpty);
    }


}
