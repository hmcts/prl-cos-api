package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PartyRepresentationUtils {

    public static boolean hasLegalRepresentation(PartyDetails partyDetails) {
        if (isNotEmpty(partyDetails)) {
            return (isNotEmpty(partyDetails.getSolicitorOrg())
                && isNotEmpty(partyDetails.getSolicitorOrg().getOrganisationID()))
                || isNotEmpty(partyDetails.getRepresentativeFirstName())
                || isNotEmpty(partyDetails.getRepresentativeLastName())
                || isNotEmpty(partyDetails.getSolicitorEmail())
                || isNotEmpty(partyDetails.getSolicitorTelephone());
        }
        return false;
    }

    public static boolean areNoPartiesRepresented(CaseDetails caseDetail, ObjectMapper objectMapper) {
        CaseData caseData = CaseUtils.getCaseData(caseDetail, objectMapper);
        return areNoPartiesRepresented(caseData);
    }

    public static boolean areNoPartiesRepresented(CaseData caseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return nullSafeList(caseData.getApplicants()).stream().noneMatch(
                    el -> hasLegalRepresentation(el.getValue()))
                && nullSafeList(caseData.getRespondents()).stream().noneMatch(
                    el -> hasLegalRepresentation(el.getValue()));
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return !hasLegalRepresentation(caseData.getApplicantsFL401())
                && !hasLegalRepresentation(caseData.getRespondentsFL401());
        } else {
            throw new IllegalArgumentException("Case has no case type");
        }
    }

    public static boolean areAnyPartiesRepresented(CaseData caseData) {
        return !areNoPartiesRepresented(caseData);
    }
}