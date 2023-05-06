package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ConsentToApplicationCheckerTest {

    @InjectMocks
    ConsentToApplicationChecker consentToApplicationChecker;

    CaseData caseData;

    @Before
    public void setUp() {


        PartyDetails respondent = PartyDetails.builder()
            .response(Response
                          .builder()
                          .activeRespondent(Yes)
                          .consent(Consent
                                       .builder()
                                       .noConsentReason("test")
                                       .courtOrderDetails("test")
                                       .consentToTheApplication(No)
                                       .applicationReceivedDate(LocalDate.now())
                                       .permissionFromCourt(Yes)
                                       .build())
                          .build())
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder().respondents(respondentList).build();
    }

    @Test
    public void isStartedTest() {
        boolean anyNonEmpty = consentToApplicationChecker.isStarted(caseData);

        assertTrue(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedTest() {
        boolean anyNonEmpty = consentToApplicationChecker.hasMandatoryCompleted(caseData);

        Assert.assertTrue(anyNonEmpty);
    }
}
