package uk.gov.hmcts.reform.prl.services.validators;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeList;

@Component
@RequiredArgsConstructor
public class ChildLivingArrangementsValidator {

    public static final String REMOVAL_ERROR = "You cannot remove a person who a child is selected to live with. "
        + "Update who the child lives with in Child details before removing that person.";
    public static final String SUBMISSION_ERROR = "Select who each child lives with in Child details before submitting the application.";

    private final ObjectMapper objectMapper;

    public List<String> validateRemoval(CallbackRequest callbackRequest) {
        CaseData current = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        if (!C100_CASE_TYPE.equals(current.getCaseTypeOfApplication()) || callbackRequest.getCaseDetailsBefore() == null) {
            return List.of();
        }
        CaseData previous = objectMapper.convertValue(callbackRequest.getCaseDetailsBefore().getData(), CaseData.class);
        if (previous == null) {
            return List.of();
        }
        Set<String> currentPartyIds = partyIds(current);
        Set<String> removedPartyIds = new HashSet<>(partyIds(previous));
        removedPartyIds.removeAll(currentPartyIds);

        boolean referencedPartyRemoved = nullSafeList(current.getNewChildDetails()).stream()
            .anyMatch(child -> referencesRemovedParty(child, previous, currentPartyIds, removedPartyIds));
        return referencedPartyRemoved ? List.of(REMOVAL_ERROR) : List.of();
    }

    public List<String> validateSubmission(CaseData caseData) {
        if (!C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            return List.of();
        }
        Set<String> currentPartyIds = partyIds(caseData);
        boolean invalidSelection = nullSafeList(caseData.getNewChildDetails()).stream()
            .anyMatch(child -> !currentPartyIds.contains(selectedCode(child)));
        return invalidSelection ? List.of(SUBMISSION_ERROR) : List.of();
    }

    private boolean referencesRemovedParty(Element<ChildDetailsRevised> child, CaseData previous,
                                           Set<String> currentPartyIds, Set<String> removedPartyIds) {
        String code = selectedCode(child);
        if (removedPartyIds.contains(code)) {
            return true;
        }
        if (currentPartyIds.contains(code) || child == null || child.getId() == null) {
            return false;
        }
        return nullSafeList(previous.getNewChildDetails()).stream()
            .filter(Objects::nonNull)
            .filter(previousChild -> child.getId().equals(previousChild.getId()))
            .anyMatch(previousChild -> removedPartyIds.contains(selectedCode(previousChild)));
    }

    private String selectedCode(Element<ChildDetailsRevised> child) {
        return ofNullable(child)
            .map(Element::getValue)
            .map(ChildDetailsRevised::getWhoDoesTheChildLiveWith)
            .map(DynamicList::getValueCode)
            .orElse(null);
    }

    private Set<String> partyIds(CaseData caseData) {
        return Stream.of(nullSafeList(caseData.getApplicants()), nullSafeList(caseData.getRespondents()),
                         nullSafeList(caseData.getOtherPartyInTheCaseRevised()))
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .map(Element::getId)
            .filter(Objects::nonNull)
            .map(UUID::toString)
            .collect(Collectors.toSet());
    }
}
