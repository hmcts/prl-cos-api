package uk.gov.hmcts.reform.prl.utils.noticeofchange;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RespondentPolicyConvertorTest {

    @InjectMocks
    RespondentPolicyConverter respondentPolicyConverter;

    @Test
    void generatePolicyTest() {

        SolicitorRole solicitorRole = SolicitorRole.C100RESPONDENTSOLICITOR1;

        Organisation organisation = Organisation.builder().build();

        PartyDetails respondent = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .solicitorOrg(organisation)
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        Optional<Element<PartyDetails>> optionalRespondentElement = Optional.of(wrappedRespondents);

        OrganisationPolicy organisationPolicy = respondentPolicyConverter
            .caGenerate(solicitorRole, optionalRespondentElement);

        assertEquals("[C100RESPONDENTSOLICITOR1]", organisationPolicy.getOrgPolicyCaseAssignedRole());
    }

    @Test
    void generatePolicyForDaTest() {
        SolicitorRole solicitorRole = SolicitorRole.FL401RESPONDENTSOLICITOR;

        Organisation organisation = Organisation.builder().build();

        PartyDetails respondent = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .solicitorOrg(organisation)
            .build();

        OrganisationPolicy organisationPolicy = respondentPolicyConverter
            .daGenerate(solicitorRole, respondent);

        assertEquals("[FL401RESPONDENTSOLICITOR]", organisationPolicy.getOrgPolicyCaseAssignedRole());
    }
}
