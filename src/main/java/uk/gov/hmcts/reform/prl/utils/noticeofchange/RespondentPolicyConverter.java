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
    public OrganisationPolicy caGenerate(SolicitorRole solicitorRole,
                                         Optional<Element<PartyDetails>> optionalPartyElement) {
        return OrganisationPolicy.builder()
            .organisation(getCaOrganisation(optionalPartyElement))
            .orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel())
            .build();
    }

    private Organisation getCaOrganisation(Optional<Element<PartyDetails>> optionalPartyElement) {
        return optionalPartyElement.map(Element::getValue)
            .filter(element ->
                        isNotEmpty(element.getRepresentativeFirstName())
                            && isNotEmpty(element.getRepresentativeLastName())
                            && isNotEmpty(element.getSolicitorOrg()))
            .map(PartyDetails::getSolicitorOrg)
            .orElse(Organisation.builder().build());
    }

    public OrganisationPolicy daGenerate(SolicitorRole solicitorRole,
                                         PartyDetails partyDetails) {
        return OrganisationPolicy.builder()
            .organisation(getDaOrganisation(partyDetails))
            .orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel())
            .build();
    }

    private Organisation getDaOrganisation(PartyDetails partyDetails) {
        if (isNotEmpty(partyDetails.getRepresentativeFirstName())
            && isNotEmpty(partyDetails.getRepresentativeLastName())
            && isNotEmpty(partyDetails.getSolicitorOrg())) {
            return partyDetails.getSolicitorOrg();
        }

        return Organisation.builder().build();
    }
}
