package uk.gov.hmcts.reform.prl.utils.noticeofchange;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
public class RespondentPolicyConverter {
    public OrganisationPolicy generate(SolicitorRole solicitorRole,
                                       Optional<Element<PartyDetails>> optionalRespondentElement) {
        return OrganisationPolicy.builder()
            .organisation(getOrganisation(optionalRespondentElement))
            .orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel())
            .build();
    }

    private Organisation getOrganisation(Optional<Element<PartyDetails>> optionalRespondentElement) {
        return optionalRespondentElement.map(Element::getValue)
            .filter(element ->
                        isNotEmpty(element.getRepresentativeFirstName())
                            && isNotEmpty(element.getRepresentativeLastName())
                            && isNotEmpty(element.getSolicitorOrg()))
            .map(PartyDetails::getSolicitorOrg)
            .orElse(Organisation.builder().build());
    }
}
