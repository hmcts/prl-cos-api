package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.events.CaseFlagsEvent;
import uk.gov.hmcts.reform.prl.events.WorkAllocationTaskStatusEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CaseFlagsWaService {
    private static final String REQUESTED = "Requested";
    private final EventService eventPublisher;
    private final ObjectMapper objectMapper;

    public void setUpWaTaskForCaseFlagsEventHandler(String authorisation, CallbackRequest callbackRequest) {
        CaseFlagsEvent caseFlagsEvent = CaseFlagsEvent.builder()
            .authorisation(authorisation)
            .callbackRequest(callbackRequest)
            .build();
        eventPublisher.publishEvent(caseFlagsEvent);
    }

    public void checkWorkAllocationTaskStatus(String authorisation, CallbackRequest callbackRequest) {
        WorkAllocationTaskStatusEvent checkWaTaskStatusEvent = WorkAllocationTaskStatusEvent.builder()
            .authorisation(authorisation)
            .callbackRequest(callbackRequest)
            .build();
        eventPublisher.publishEvent(checkWaTaskStatusEvent);
    }

    public void filterRequestedPartyFlags(AllPartyFlags allPartyFlags) {
        if (allPartyFlags != null) {
            Arrays.stream(allPartyFlags.getClass().getDeclaredFields())
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        Flags flagsValue = (Flags) field.get(allPartyFlags);
                        if (CollectionUtils.isNotEmpty(flagsValue.getDetails())) {
                            if (!REQUESTED.equals(flagsValue.getDetails().getFirst().getValue().getStatus())) {
                                field.set(allPartyFlags, Flags.builder().build());
                            }
                        }
                    } catch (IllegalAccessException e) {
                        // ignore it
                    }
                });
        }
    }

    public void filterRequestedCaseLevelFlags(Flags caseLevelFlags) {
        if (caseLevelFlags != null && CollectionUtils.isNotEmpty(caseLevelFlags.getDetails())) {
            List<Element<FlagDetail>> flagDetails = deepCopyArray(caseLevelFlags.getDetails(),
                                                                  new TypeReference<List<Element<FlagDetail>>>() {});
            if (CollectionUtils.isNotEmpty(flagDetails)) {
                for (Element<FlagDetail> flagDetail : flagDetails) {
                    if (!REQUESTED.equals(flagDetail.getValue().getStatus())) {
                        caseLevelFlags.getDetails().remove(flagDetail);
                    }
                }
            }
        }
    }

    public List<String> validateAllFlags(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        List<Element<FlagDetail>> allFlagsDetails = new ArrayList<>();
        allFlagsDetails.addAll(caseData.getCaseFlags().getDetails());
        allFlagsDetails.addAll(extractAllPartyFlagDetails(caseData.getAllPartyFlags()));

        List<Element<FlagDetail>> sortedAllFlagsDetails = allFlagsDetails.stream()
            .filter(detail -> detail.getValue() != null && detail.getValue().getDateTimeModified() != null)
            .sorted(Comparator.comparing((Element<FlagDetail> detail) ->
                                             detail.getValue().getDateTimeModified()).reversed())
            .toList();

        FlagDetail mostRecentlyModified = sortedAllFlagsDetails.get(0).getValue();
        if (REQUESTED.equals(mostRecentlyModified.getStatus())) {
            errors.add("Please select the status of flag other than Requested");
        }
        return errors;
    }

    private List<Element<FlagDetail>> extractAllPartyFlagDetails(AllPartyFlags allPartyFlags) {
        if (allPartyFlags == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(allPartyFlags.getClass().getDeclaredFields())
            .filter(field -> field.getType().equals(Flags.class))
            .map(field -> {
                field.setAccessible(true);
                try {
                    Flags flags = (Flags) field.get(allPartyFlags);
                    return flags != null ? flags.getDetails() : null;
                } catch (IllegalAccessException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    public <T> T deepCopyArray(T object, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(object), typeReference);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
}
