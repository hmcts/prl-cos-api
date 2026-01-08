package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.MaskEmail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentOrgPolicyService {

    private static final int MAX_RESPONDENTS = 5;

    private final RoleAssignmentService roleAssignmentService;

    private final OrganisationService organisationService;

    private final MaskEmail maskEmail;

    /**
     * Updates {ca|da}Respondent{n}Policy.Organisation based on respondent solicitor org.
     * Preserves existing OrgPolicyCaseAssignedRole and OrgPolicyReference.
     *
     * @param caseDataMap full CCD case data map (existing values included)
     * @param caseData model
     */
    public Map<String, Object> populateRespondentOrganisations(Map<String, Object> caseDataMap, CaseData caseData) {
        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        if (respondents == null || respondents.isEmpty()) {
            return caseDataMap;
        }

        String caseId = String.valueOf(caseData.getId());

        for (int i = 0; i < respondents.size() && i < MAX_RESPONDENTS; i++) {
            PartyDetails respondent = respondents.get(i).getValue();
            if (isValidRespondent(respondent)) {
                String policyKey = policyKey(CaseUtils.getCaseTypeOfApplication(caseData), i + 1);
                Map<String, Object> updatedPolicy = updatePolicy(caseDataMap, policyKey, respondent, caseId);
                if (updatedPolicy != null) {
                    caseDataMap.put(policyKey, updatedPolicy);
                    assignRoleIfPossible(updatedPolicy, respondent, caseId, policyKey);
                }
            }
        }
        return caseDataMap;
    }

    private boolean isValidRespondent(PartyDetails respondent) {
        if (respondent == null) {
            return false;
        }
        boolean hasRep = YesNoDontKnow.yes.equals(respondent.getDoTheyHaveLegalRepresentation());
        var solicitorOrg = respondent.getSolicitorOrg();
        boolean hasOrgId = solicitorOrg != null && StringUtils.isNotBlank(solicitorOrg.getOrganisationID());
        String solicitorEmail = respondent.getSolicitorEmail();
        return hasRep && hasOrgId && StringUtils.isNotBlank(solicitorEmail);
    }

    private Map<String, Object> updatePolicy(Map<String, Object> caseDataMap, String policyKey, PartyDetails respondent, String caseId) {
        Object existingObj = caseDataMap.get(policyKey);
        if (!(existingObj instanceof Map<?, ?> existingPolicy)) {
            log.warn("Policy {} missing or not a map on case {}; skipping", policyKey, caseId);
            return null;
        }
        Map<String, Object> organisation = new HashMap<>();
        var solicitorOrg = respondent.getSolicitorOrg();
        organisation.put("OrganisationID", solicitorOrg.getOrganisationID());
        if (StringUtils.isNotBlank(solicitorOrg.getOrganisationName())) {
            organisation.put("OrganisationName", solicitorOrg.getOrganisationName());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> existingPolicyMap = (Map<String, Object>) existingPolicy;
        Map<String, Object> updatedPolicy = new HashMap<>(existingPolicyMap);
        updatedPolicy.put("Organisation", organisation);
        return updatedPolicy;
    }

    private void assignRoleIfPossible(Map<String, Object> updatedPolicy, PartyDetails respondent, String caseId, String policyKey) {
        Object roleObj = updatedPolicy.get("OrgPolicyCaseAssignedRole");
        String assignedRole = roleObj != null ? roleObj.toString() : null;
        if (StringUtils.isBlank(assignedRole)) {
            log.warn("OrgPolicyCaseAssignedRole missing for {} on case {}; skipping role assignment", policyKey, caseId);
            return;
        }
        String solicitorEmail = respondent.getSolicitorEmail();
        Optional<String> userIdOpt = organisationService.findUserByEmail(solicitorEmail);
        if (userIdOpt.isEmpty()) {
            log.warn("Unable to resolve IDAM user for respondent solicitor {} on case {}", maskEmail.mask(solicitorEmail), caseId);
            return;
        }
        String assigneeUserId = userIdOpt.get();
        roleAssignmentService.createRoleAssignment(
            caseId,
            assigneeUserId,
            uk.gov.hmcts.reform.prl.enums.RoleCategory.PROFESSIONAL,
            assignedRole,
            false
        );
    }

    private String policyKey(String caseType, int index1Based) {
        String prefix = C100_CASE_TYPE.equalsIgnoreCase(caseType) ? "caRespondent" : "daRespondent";
        return prefix + index1Based + "Policy";
    }

}
