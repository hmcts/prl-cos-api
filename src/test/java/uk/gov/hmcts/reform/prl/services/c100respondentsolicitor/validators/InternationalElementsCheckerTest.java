package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResSolInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorInternationalElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class InternationalElementsCheckerTest {

    @InjectMocks
    InternationalElementsChecker internationalElementsChecker;

    CaseData caseData;

    @Before
    public void setUp() {

        PartyDetails respondent = PartyDetails
            .builder()
            .response(Response
                          .builder()
                          .activeRespondent(Yes)
                          .resSolInternationalElements(ResSolInternationalElements
                                                           .builder()
                                                           .internationalElementParentInfo(SolicitorInternationalElement
                                                                                               .builder()
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
                                                           .build())
                          .build())
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder().respondents(respondentList).build();

    }

    @Test
    public void isStarted() {
        Boolean bool = internationalElementsChecker.isStarted(caseData);
        assertTrue(bool);
    }

    @Test
    public void mandatoryCompleted() {
        Boolean bool = internationalElementsChecker.hasMandatoryCompleted(caseData);
        assertTrue(bool);
    }

}
