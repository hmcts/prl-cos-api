package uk.gov.hmcts.reform.prl.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@Service
public class RespondentOrgPolicyService {

    private static final int MAX_RESPONDENTS = 5;

    /**
     * Updates {ca|da}Respondent{n}Policy.Organisation based on respondent solicitor org.
     * Preserves existing OrgPolicyCaseAssignedRole and OrgPolicyReference.
     *
     * @param caseDataMap full CCD case data map (existing values included)
     * @param respondents respondents list from your typed CaseData (or map-parsed equivalent)
     */
    public Map<String, Object> populateRespondentOrganisations(Map<String, Object> caseDataMap,
                                                               List<Element<PartyDetails>> respondents,
                                                               String caseType) {
        Map<String, Object> updates = new HashMap<>();

        if (respondents == null || caseDataMap == null) {
            return updates;
        }

        for (int i = 0; i < respondents.size() && i < MAX_RESPONDENTS; i++) {
            PartyDetails respondent = respondents.get(i).getValue();

            boolean hasRep = YesNoDontKnow.yes.equals(respondent.getDoTheyHaveLegalRepresentation());

            var solicitorOrg = respondent.getSolicitorOrg();
            boolean hasOrgId = solicitorOrg != null && StringUtils.isNotBlank(solicitorOrg.getOrganisationID());

            if (hasRep && hasOrgId) {
                String policyKey = policyKey(caseType, i + 1);

                Object existingObj = caseDataMap.get(policyKey);
                if (!(existingObj instanceof Map<?, ?> m)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> existingPolicy = (Map<String, Object>) m;


                // Build Organisation map (CCD-style)
                Map<String, Object> organisation = new HashMap<>();
                organisation.put("OrganisationID", solicitorOrg.getOrganisationID());
                if (StringUtils.isNotBlank(solicitorOrg.getOrganisationName())) {
                    organisation.put("OrganisationName", solicitorOrg.getOrganisationName());
                }

                Map<String, Object> updatedPolicy = new HashMap<>(existingPolicy);
                updatedPolicy.put("Organisation", organisation);

                updates.put(policyKey, updatedPolicy);
            }
        }

        return updates;
    }

    private String policyKey(String caseType, int index1Based) {
        String prefix = C100_CASE_TYPE.equalsIgnoreCase(caseType) ? "caRespondent" : "daRespondent";
        return prefix + index1Based + "Policy";
    }
}
