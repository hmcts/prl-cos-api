package uk.gov.hmcts.reform.prl.utils.noticeofchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.util.Optional;

public class RespondentPolicyConvertorTest {

    @Mock
    RespondentPolicyConverter respondentPolicyConverter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void generatePolicyTest(){

        SolicitorRole solicitorRole = SolicitorRole.SOLICITORA;

        Optional<Element<PartyDetails>> optionalRespondentElement = Optional.empty();

        OrganisationPolicy organisationPolicy = respondentPolicyConverter
            .generate(solicitorRole, optionalRespondentElement);

        Assert.assertNull(organisationPolicy);
    }
}
