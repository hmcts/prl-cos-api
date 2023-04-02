package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorKeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class KeepDetailsPrivateCheckerTest {

    @InjectMocks
    KeepDetailsPrivateChecker keepDetailsPrivateChecker;

    CaseData caseData;

    @Before
    public void setUp() {

        List<ConfidentialityListEnum> confidentialityListEnums = new ArrayList<>();

        confidentialityListEnums.add(ConfidentialityListEnum.email);
        confidentialityListEnums.add(ConfidentialityListEnum.phoneNumber);

        PartyDetails respondent = PartyDetails.builder()
            .response(Response
                          .builder()
                          .activeRespondent(Yes)
                          .keepDetailsPrivate(KeepDetailsPrivate
                                                  .builder()
                                                  .build())
                          .solicitorKeepDetailsPriate(SolicitorKeepDetailsPrivate
                                   .builder()
                                   .respKeepDetailsPrivateConfidentiality(KeepDetailsPrivate
                                            .builder()
                                            .confidentiality(Yes)
                                            .confidentialityList(confidentialityListEnums)
                                            .build())
                                   .respKeepDetailsPrivate(KeepDetailsPrivate
                                                               .builder()
                                                               .otherPeopleKnowYourContactDetails(YesNoDontKnow.yes)
                                                               .confidentiality(Yes)
                                                               .confidentialityList(confidentialityListEnums)
                                                               .build())
                                                          .build())

                          .build())
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder().respondents(respondentList).build();
    }

    @Test
    public void isStartedTest() {
        boolean anyNonEmpty = keepDetailsPrivateChecker.isStarted(caseData, "A");

        assertTrue(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedTest() {
        boolean anyNonEmpty = keepDetailsPrivateChecker.isFinished(caseData, "A");

        Assert.assertTrue(anyNonEmpty);
    }
}
