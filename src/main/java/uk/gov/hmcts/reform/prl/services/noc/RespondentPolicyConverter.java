package uk.gov.hmcts.reform.prl.services.noc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.SolicitorRole;
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
                isNotEmpty(element.getSolicitorOrg()) && isNotEmpty(element.getSolicitorOrg().getOrganisationName()))
            .map(child -> child.getSolicitorOrg())
            .orElse(Organisation.builder().build());
    }
}
