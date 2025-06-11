package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.internationalelements.CitizenInternationalElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class InternationalElementsCheckerTest {

    @InjectMocks
    InternationalElementsChecker internationalElementsChecker;

    @Mock
    RespondentTaskErrorService respondentTaskErrorService;

    CaseData caseData;

    PartyDetails respondent;

    @BeforeEach
    public void setUp() {

        respondent = PartyDetails
            .builder()
            .response(Response
                          .builder()
                          .citizenInternationalElements(CitizenInternationalElements
                                                            .builder()
                                                            .childrenLiveOutsideOfEnWl(Yes)
                                                            .childrenLiveOutsideOfEnWlDetails("Test")
                                                            .parentsAnyOneLiveOutsideEnWl(Yes)
                                                            .parentsAnyOneLiveOutsideEnWlDetails("Test")
                                                            .anotherPersonOrderOutsideEnWl(Yes)
                                                            .anotherPersonOrderOutsideEnWlDetails("Test")
                                                            .anotherCountryAskedInformation(Yes)
                                                            .anotherCountryAskedInformationDetaails("Test")
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
        Boolean bool = internationalElementsChecker.isStarted(respondent, true);
        assertTrue(bool);
    }

    @Test
    public void isNotStartedTest() {
        PartyDetails blankRespondent = PartyDetails.builder().build();
        Boolean bool = internationalElementsChecker.isStarted(blankRespondent, true);
        assertFalse(bool);
    }

    @Test
    public void mandatoryCompletedTest() {
        Boolean bool = internationalElementsChecker.isFinished(respondent, true);
        assertTrue(bool);
    }

    @Test
    public void isNotFinishedEmptyResponseTest() {
        PartyDetails blankRespondent = PartyDetails.builder().response(Response
                                                                           .builder().build()).build();
        Boolean bool = internationalElementsChecker.isFinished(blankRespondent, true);
        assertFalse(bool);
    }

    @Test
    public void isNotFinishedEmptyStringChildrenLiveOutsideOfEnWlDetails() {
        PartyDetails blankRespondent = PartyDetails
            .builder()
            .response(Response
                          .builder()
                          .citizenInternationalElements(CitizenInternationalElements
                                                            .builder()
                                                            .build())
                          .build())
            .build();
        Boolean bool = internationalElementsChecker.isFinished(blankRespondent, true);
        assertFalse(bool);
    }

    @Test
    public void isNotFinishedEmptyCitizenInternationalElementsTest() {
        PartyDetails blankRespondent = PartyDetails
            .builder()
            .response(Response
                          .builder()
                          .citizenInternationalElements(CitizenInternationalElements
                                                            .builder()
                                                            .childrenLiveOutsideOfEnWl(Yes)
                                                            .childrenLiveOutsideOfEnWlDetails("")
                                                            .build())
                          .build())
            .build();
        Boolean bool = internationalElementsChecker.isFinished(blankRespondent, true);
        assertFalse(bool);
    }

    @Test
    public void mandatoryCompletedWithoutRespdntTest() {
        respondent = null;
        Boolean bool = internationalElementsChecker.isFinished(respondent, true);
        assertFalse(bool);
    }

    @Test
    public void mandatoryNotCompletedWithoutDetailsTest() {
        respondent = PartyDetails
            .builder()
            .response(Response
                          .builder()
                          .citizenInternationalElements(CitizenInternationalElements
                                                            .builder()
                                                            .childrenLiveOutsideOfEnWl(Yes)
                                                            .parentsAnyOneLiveOutsideEnWl(Yes)
                                                            .anotherPersonOrderOutsideEnWl(Yes)
                                                            .anotherCountryAskedInformation(Yes)
                                                            .build())
                          .build())
            .build();
        Boolean bool = internationalElementsChecker.isFinished(respondent, true);
        assertFalse(bool);
    }

    @Test
    public void mandatoryCompletedWithoutDetailsTest() {
        respondent = PartyDetails
            .builder()
            .response(Response
                          .builder()
                          .citizenInternationalElements(CitizenInternationalElements
                                                            .builder()
                                                            .childrenLiveOutsideOfEnWl(No)
                                                            .parentsAnyOneLiveOutsideEnWl(No)
                                                            .anotherPersonOrderOutsideEnWl(No)
                                                            .anotherCountryAskedInformation(No)
                                                            .build())
                          .build())
            .build();
        Boolean bool = internationalElementsChecker.isFinished(respondent, true);
        assertTrue(bool);
    }

}
