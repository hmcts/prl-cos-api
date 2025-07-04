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

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfidentialDetailsChangeHelper {

    public boolean haveConfidentialDetailsChanged(CaseData current, CaseData previous) {
        List<Element<PartyDetails>> currentApplicants = Optional.ofNullable(current.getApplicants()).orElse(List.of());
        List<Element<PartyDetails>> previousApplicants = Optional.ofNullable(previous.getApplicants()).orElse(List.of());

        if (currentApplicants.size() != previousApplicants.size()) {
            return true;
        }

        for (int i = 0; i < currentApplicants.size(); i++) {
            PartyDetails curr = currentApplicants.get(i).getValue();
            PartyDetails prev = previousApplicants.get(i).getValue();

            if (curr == null || prev == null) {
                return true;
            }

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

    private boolean hasDetailChanged(Object currentDetail, Object previousDetail) {
        return !Objects.equals(
            String.valueOf(currentDetail).trim().toLowerCase(),
            String.valueOf(previousDetail).trim().toLowerCase()
        );
    }
}


