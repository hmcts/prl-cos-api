package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResSolInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorInternationalElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class InternationalElementsCheckerTest {

    @InjectMocks
    InternationalElementsChecker internationalElementsChecker;

    @Mock
    RespondentTaskErrorService respondentTaskErrorService;

    CaseData caseData;

    PartyDetails respondent;

    @Before
    public void setUp() {

        respondent = PartyDetails
            .builder()
            .response(Response
                          .builder()
                          .resSolInternationalElements(ResSolInternationalElements
                                                           .builder()
                                                           .internationalElementParentInfo(SolicitorInternationalElement
                                                                                               .builder()
                                                                                               .reasonForParent(Yes)
                                                                                               .reasonForParentDetails("Test")
                                                                                               .reasonForJurisdictionDetails("Test")
                                                                                               .requestToAuthorityDetails("Test")
                                                                                               .build())
                                                           .internationalElementChildInfo(SolicitorInternationalElement
                                                                                              .builder()
                                                                                              .reasonForChild(Yes)
                                                                                              .reasonForChildDetails("Test")
                                                                                              .reasonForParent(Yes)
                                                                                              .reasonForParentDetails("Test")
                                                                                              .reasonForJurisdiction(Yes)
                                                                                              .reasonForJurisdictionDetails("Test")
                                                                                              .requestToAuthority(Yes)
                                                                                              .requestToAuthorityDetails("Test")
                                                                                              .build())
                                                           .internationalElementRequestInfo(SolicitorInternationalElement
                                                                                                .builder()
                                                                                                .requestToAuthority(No)
                                                                                                .build())
                                                           .internationalElementJurisdictionInfo(SolicitorInternationalElement
                                                                                                .builder()
                                                                                                     .reasonForJurisdiction(No)
                                                                                                     .build())
                                                           .build())
                          .build())
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);
        doNothing().when(respondentTaskErrorService).addEventError(Mockito.any(), Mockito.any(), Mockito.any());

        caseData = CaseData.builder().respondents(respondentList).build();

    }

    @Test
    public void isStarted() {
        Boolean bool = internationalElementsChecker.isStarted(respondent);
        assertTrue(bool);
    }

    @Test
    public void mandatoryCompleted() {
        Boolean bool = internationalElementsChecker.isFinished(respondent);
        assertTrue(bool);
    }

}
