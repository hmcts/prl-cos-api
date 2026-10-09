package uk.gov.hmcts.reform.prl.models.dto.ccd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantRespondentOrgPolicies {

    private OrganisationPolicy caApplicant1Policy;
    private OrganisationPolicy caApplicant2Policy;
    private OrganisationPolicy caApplicant3Policy;
    private OrganisationPolicy caApplicant4Policy;
    private OrganisationPolicy caApplicant5Policy;

    private OrganisationPolicy caRespondent1Policy;
    private OrganisationPolicy caRespondent2Policy;
    private OrganisationPolicy caRespondent3Policy;
    private OrganisationPolicy caRespondent4Policy;
    private OrganisationPolicy caRespondent5Policy;
}
