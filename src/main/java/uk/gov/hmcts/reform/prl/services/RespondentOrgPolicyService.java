package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessService;
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

    private final AssignCaseAccessService assignCaseAccessService;

    private final OrganisationService organisationService;

    private final MaskEmail maskEmail;

    /**
     * Updates caRespondent{n}Policy.Organisation based on respondent solicitor org.
     * Preserves existing OrgPolicyCaseAssignedRole and OrgPolicyReference.
     *
     * @param caseDataMap full CCD case data map (existing values included)
     * @param caseData model
     */
    public Map<String, Object> populateRespondentOrganisations(Map<String, Object> caseDataMap, CaseData caseData) {
        String caseId = String.valueOf(caseData.getId());
        String caseType = CaseUtils.getCaseTypeOfApplication(caseData);

        List<PartyDetails> respondents = getRespondents(caseData, caseType);
        for (int i = 0; i < respondents.size() && i < MAX_RESPONDENTS; i++) {
            PartyDetails respondent = respondents.get(i);
            if (!isValidRespondent(respondent)) {
                continue;
            }
            int index1Based = i + 1;
            String policyKey = policyKey(caseType, index1Based);
            Map<String, Object> updatedPolicy =
                updatePolicy(caseDataMap, policyKey, respondent, caseId, caseType, index1Based);
            caseDataMap.put(policyKey, updatedPolicy);
            assignRoleIfPossible(updatedPolicy, respondent, caseId, policyKey);
        }
        return caseDataMap;
    }

    private List<PartyDetails> getRespondents(CaseData caseData, String caseType) {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseType)) {
            List<Element<PartyDetails>> respondents = caseData.getRespondents();
            if (respondents == null || respondents.isEmpty()) {
                return List.of();
            }
            return respondents.stream()
                .map(e -> e == null ? null : e.getValue())
                .toList();
        }
        PartyDetails fl401Respondent = caseData.getRespondentsFL401();
        return fl401Respondent == null ? List.of() : List.of(fl401Respondent);
    }

    private String policyKey(String caseType, int index1Based) {
        Representing representing = C100_CASE_TYPE.equalsIgnoreCase(caseType)
            ? Representing.CARESPONDENT
            : Representing.DARESPONDENT;
        String template = representing.getPolicyFieldTemplate();
        // DA template contains no format specifier; String.format tolerates unused args.
        return template.contains("%d") ? String.format(template, index1Based) : template;
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

    private Map<String, Object> updatePolicy(Map<String, Object> caseDataMap, String policyKey, PartyDetails respondent,
                                             String caseId, String caseType, int index1Based) {
        Map<String, Object> policyMap;
        Object existingObj = caseDataMap.get(policyKey);

        if (existingObj instanceof Map<?, ?> existingPolicy) {
            @SuppressWarnings("unchecked")
            Map<String, Object> existing = (Map<String, Object>) existingPolicy;
            policyMap = new HashMap<>(existing);
        } else {
            log.info("Policy {} missing on case {}, creating new policy", policyKey, caseId);
            policyMap = new HashMap<>();
            policyMap.put("OrgPolicyCaseAssignedRole", getRespondentSolicitorRole(caseType, index1Based));
        }

        Map<String, Object> organisation = new HashMap<>();
        var solicitorOrg = respondent.getSolicitorOrg();
        organisation.put("OrganisationID", solicitorOrg.getOrganisationID());
        if (StringUtils.isNotBlank(solicitorOrg.getOrganisationName())) {
            organisation.put("OrganisationName", solicitorOrg.getOrganisationName());
        }
        policyMap.put("Organisation", organisation);
        return policyMap;
    }


    private String getRespondentSolicitorRole(String caseType, int index1Based) {
        Representing representing = C100_CASE_TYPE.equalsIgnoreCase(caseType)
            ? Representing.CARESPONDENT
            : Representing.DARESPONDENT;
        return SolicitorRole.fromRepresentingAndIndex(representing, index1Based)
            .map(SolicitorRole::getCaseRoleLabel)
            .orElseThrow(() -> new IllegalStateException(
                "No SolicitorRole for " + representing + " index " + index1Based));
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
        String solicitorOrgId = respondent.getSolicitorOrg() != null ? respondent.getSolicitorOrg().getOrganisationID() : null;
        // Delegate to CCD case-assignment API rather than calling Role Assignment Service directly.
        // The role reference stored on OrgPolicyCaseAssignedRole (e.g. "[C100RESPONDENTSOLICITOR1]") is a CCD
        // case-role reference and is not a valid AMS role name; CCD's case-assignment endpoint knows how to
        // translate it into the correct AMS role assignment via its own drools rules. Same call is used
        // elsewhere in ServiceOfApplicationService for the equivalent path.
        assignCaseAccessService.assignCaseAccessToUserWithRole(
            caseId,
            assigneeUserId,
            assignedRole,
            solicitorOrgId
        );
    }


}
