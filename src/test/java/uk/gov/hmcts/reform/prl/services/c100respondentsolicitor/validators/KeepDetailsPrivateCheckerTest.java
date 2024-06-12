package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class KeepDetailsPrivateCheckerTest {

    @InjectMocks
    KeepDetailsPrivateChecker keepDetailsPrivateChecker;

    @Mock
    RespondentTaskErrorService respondentTaskErrorService;

    CaseData caseData;

    PartyDetails respondent;

    @Before
    public void setUp() {

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
    public void isStartedTest() {
        boolean anyNonEmpty = keepDetailsPrivateChecker.isStarted(respondent, true);

        assertTrue(anyNonEmpty);
    }

    @Test
    public void isStartedWithoutRespKeepDetailsPrivateTest() {
        respondent = PartyDetails.builder()
            .response(Response
                          .builder()
                          .keepDetailsPrivate(KeepDetailsPrivate
                                                  .builder()
                                                  .build())
                          .build())
            .build();

        boolean anyNonEmpty = keepDetailsPrivateChecker.isStarted(respondent, true);
        Assert.assertFalse(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedTest() {
        boolean anyNonEmpty = keepDetailsPrivateChecker.isFinished(respondent, true);
        Assert.assertTrue(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedWithoutRespondentTest() {
        respondent = null;
        boolean anyNonEmpty = keepDetailsPrivateChecker.isFinished(respondent, true);
        Assert.assertFalse(anyNonEmpty);
    }


}
