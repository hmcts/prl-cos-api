package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfidentialDetailsChangeHelper {

    public boolean haveConfidentialDetailsChanged(CaseData current, CaseData previous) {
        List<Element<PartyDetails>> currentApplicants = getApplicantsByCaseType(current);
        List<Element<PartyDetails>> previousApplicants = getApplicantsByCaseType(previous);

        if (currentApplicants.size() != previousApplicants.size()) {
            return true;
        }

        for (int i = 0; i < currentApplicants.size(); i++) {
            PartyDetails curr = currentApplicants.get(i).getValue();
            PartyDetails prev = previousApplicants.get(i).getValue();

            if (curr == null || prev == null) {
                return true;
            }

            if (haveContactDetailsChanged(curr, prev)) {
                return true;
            }
        }
        return false;
    }

    public boolean haveContactDetailsChanged(PartyDetails curr, PartyDetails prev) {
        if (checkIfAddressConfidentialityHasChanged(curr, prev)
            || checkIfEmailConfidentialityHasChanged(curr, prev)
            || checkIfPhoneConfidentialityHasChanged(curr, prev)) {
            return true;
        }

        if (hasDetailChanged(curr.getAddress(), prev.getAddress())
            || hasDetailChanged(curr.getEmail(), prev.getEmail())
            || hasDetailChanged(curr.getPhoneNumber(), prev.getPhoneNumber())) {
            return true;
        }
        return false;
    }

    public static boolean checkIfAddressConfidentialityHasChanged(PartyDetails current, PartyDetails previous) {
        return isNotEmpty(current.getIsAddressConfidential())
            && isNotEmpty(previous.getIsAddressConfidential())
            && !previous.getIsAddressConfidential().equals(current.getIsAddressConfidential());
    }

    public static boolean checkIfEmailConfidentialityHasChanged(PartyDetails current, PartyDetails previous) {
        return isNotEmpty(current.getIsEmailAddressConfidential())
            && isNotEmpty(previous.getIsEmailAddressConfidential())
            && !previous.getIsEmailAddressConfidential().equals(current.getIsEmailAddressConfidential());
    }

    public static boolean checkIfPhoneConfidentialityHasChanged(PartyDetails current, PartyDetails previous) {
        return isNotEmpty(current.getIsPhoneNumberConfidential())
            && isNotEmpty(previous.getIsPhoneNumberConfidential())
            && !previous.getIsPhoneNumberConfidential().equals(current.getIsPhoneNumberConfidential());
    }

    private List<Element<PartyDetails>> getApplicantsByCaseType(CaseData caseData) {
        String applicationType = caseData.getCaseTypeOfApplication();

        if (C100_CASE_TYPE.equalsIgnoreCase(applicationType)) {
            return Optional.ofNullable(caseData.getApplicants()).orElse(List.of());
        }

        if (FL401_CASE_TYPE.equalsIgnoreCase(applicationType)) {
            PartyDetails applicant = caseData.getApplicantsFL401();
            if (applicant == null) {
                return List.of();
            }

            UUID id = applicant.getPartyId() != null ? applicant.getPartyId() : UUID.randomUUID();
            Element<PartyDetails> applicantsFL401 = Element.<PartyDetails>builder()
                .id(id)
                .value(applicant)
                .build();

            return List.of(applicantsFL401);
        }
        return List.of();
    }

    private boolean hasDetailChanged(Object currentDetail, Object previousDetail) {
        return !Objects.equals(
            String.valueOf(currentDetail).trim().toLowerCase(),
            String.valueOf(previousDetail).trim().toLowerCase()
        );
    }


}


