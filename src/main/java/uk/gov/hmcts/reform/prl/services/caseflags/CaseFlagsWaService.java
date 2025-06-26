package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.events.CaseFlagsEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CaseFlagsWaService {
    private static final String REQUESTED = "Requested";
    private final EventService eventPublisher;
    private final ObjectMapper objectMapper;
    private final AllTabServiceImpl allTabService;

    public void setUpWaTaskForCaseFlagsEventHandler(String authorisation, CallbackRequest callbackRequest) {
        CaseFlagsEvent caseFlagsEvent = CaseFlagsEvent.builder()
            .authorisation(authorisation)
            .callbackRequest(callbackRequest)
            .build();
        eventPublisher.publishEvent(caseFlagsEvent);
    }

    public void checkAllRequestedFlagsAndCloseTask(CaseData caseData) {
        List<Element<FlagDetail>> allFlagsDetails = new ArrayList<>();
        addCaseFlags(caseData, allFlagsDetails);
        allFlagsDetails.addAll(extractAllPartyFlagDetails(caseData.getAllPartyFlags()));

        boolean allFlagsAreActioned = allFlagsDetails.stream()
            .filter(Objects::nonNull)
            .noneMatch(detail -> REQUESTED.equals(detail.getValue().getStatus()));

        if (allFlagsAreActioned && YesOrNo.Yes.equals(caseData.getReviewRaRequestWrapper().getIsCaseFlagsTaskCreated())) {
            String caseId = String.valueOf(caseData.getId());
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificEvent(
                caseId,
                CaseEvent.CLOSE_REVIEW_RA_REQUEST_TASK.getValue()
            );
            startAllTabsUpdateDataContent.caseDataMap().put(PrlAppsConstants.WA_IS_CASE_FLAG_TASK_CREATED, YesOrNo.No);
            allTabService.submitAllTabsUpdate(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                startAllTabsUpdateDataContent.caseDataMap()
            );
        }
    }

    private void addCaseFlags(CaseData caseData, List<Element<FlagDetail>> allFlagsDetails) {
        if (caseData.getCaseFlags() != null && caseData.getCaseFlags().getDetails() != null) {
            allFlagsDetails.addAll(caseData.getCaseFlags().getDetails());
        }
    }


    public void checkCaseFlagsToCreateTask(CaseData caseData, CaseData caseDataBefore) {

        boolean anyExistingCaseFlags = isCaseHasNoRequestedFlags(caseDataBefore);

        boolean anyNewCaseFlags = isCaseHasNoRequestedFlags(caseData);

        if (anyExistingCaseFlags && !anyNewCaseFlags) {
            caseData.getReviewRaRequestWrapper().setIsCaseFlagsTaskCreated(YesOrNo.No);
        }
    }

    public boolean isCaseHasNoRequestedFlags(CaseData caseData) {
        List<Element<FlagDetail>> allFlagsDetails = new ArrayList<>();
        addCaseFlags(caseData, allFlagsDetails);
        allFlagsDetails.addAll(extractAllPartyFlagDetails(caseData.getAllPartyFlags()));

        return allFlagsDetails.stream()
            .filter(Objects::nonNull)
            .noneMatch(detail -> REQUESTED.equals(detail.getValue().getStatus()));
    }

    public void setSelectedFlags(CaseData caseData) {
        AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();
        List<Element<Flags>> selectedFlagsList = new ArrayList<>();

        Flags caseLevelFlag = deepCopy(caseData.getCaseFlags(), Flags.class);
        if (caseLevelFlag != null) {
            caseLevelFlag.setPartyName("Case Level");
            selectedFlagsList.add(ElementUtils.element(caseLevelFlag));
        }

        Arrays.stream(allPartyFlags.getClass().getDeclaredFields())
            .filter(field -> field.getType().equals(Flags.class))
            .forEach(field -> addFlagsToList(field, allPartyFlags, selectedFlagsList));

        if (caseData.getCaseFlags() != null && caseData.getCaseFlags().getDetails() != null) {
            caseData.getCaseFlags().getDetails().forEach(flagDetail -> {
                if (!REQUESTED.equals(flagDetail.getValue().getStatus())) {
                    selectedFlagsList.forEach(selectedFlag -> selectedFlag.getValue().getDetails().remove(flagDetail));
                }
            });
        }
        List<Element<FlagDetail>> allFlagsDetails = extractAllPartyFlagDetails(allPartyFlags);

        allFlagsDetails.forEach(flagDetail -> {
            if (!REQUESTED.equals(flagDetail.getValue().getStatus())) {
                selectedFlagsList.forEach(selectedFlag -> selectedFlag.getValue().getDetails().remove(flagDetail));
            }
        });

        List<Element<Flags>> finalList = selectedFlagsList.stream()
            .filter(f -> f.getValue() != null && CollectionUtils.isNotEmpty(f.getValue().getDetails()))
            .toList();


        caseData.getReviewRaRequestWrapper().setSelectedFlags(finalList);
    }

    private void addFlagsToList(Field field, AllPartyFlags allPartyFlags, List<Element<Flags>> selectedFlagsList) {
        field.setAccessible(true);
        try {
            Flags selectedFlag = deepCopy((Flags) field.get(allPartyFlags), Flags.class);
            if (selectedFlag != null) {
                selectedFlagsList.add(ElementUtils.element(selectedFlag));
            }
        } catch (IllegalAccessException e) {
            // ignore
        }
    }

    public Element<FlagDetail> validateAllFlags(CaseData caseData) {
        List<Element<FlagDetail>> allFlagsDetails = caseData.getReviewRaRequestWrapper().getSelectedFlags().stream()
            .filter(e -> e != null && e.getValue() != null)
            .map(e -> e.getValue().getDetails())
            .flatMap(Collection::stream)
            .toList();

        List<Element<FlagDetail>> sortedAllFlagsDetails = allFlagsDetails.stream()
            .filter(detail -> detail.getValue() != null && detail.getValue().getDateTimeModified() != null)
            .sorted(Comparator.comparing((Element<FlagDetail> detail) ->
                                             detail.getValue().getDateTimeModified()).reversed())
            .toList();

        return sortedAllFlagsDetails.getFirst();
    }

    public void searchAndUpdateCaseFlags(CaseData caseData, Element<FlagDetail> mostRecentlyModified) {
        if (caseData.getCaseFlags() != null && CollectionUtils.isNotEmpty(caseData.getCaseFlags().getDetails())) {
            List<Element<FlagDetail>> allCaseLevelFlagsDetails = new ArrayList<>(caseData.getCaseFlags().getDetails());
            allCaseLevelFlagsDetails.forEach(flagDetail -> {
                if (mostRecentlyModified.getId().equals(flagDetail.getId())) {
                    final int index =  caseData.getCaseFlags().getDetails().indexOf(flagDetail);
                    caseData.getCaseFlags().getDetails().set(index, mostRecentlyModified);
                }
            });
        }

        AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();
        List<Element<FlagDetail>> allPartyLevelFlagsDetails = new ArrayList<>(extractAllPartyFlagDetails(allPartyFlags));
        allPartyLevelFlagsDetails.forEach(flagDetail -> {
            if (mostRecentlyModified.getId().equals(flagDetail.getId())) {
                Arrays.stream(caseData.getAllPartyFlags().getClass().getDeclaredFields())
                    .filter(field -> field.getType().equals(Flags.class))
                    .forEach(field -> mapModifiedFieldToPartyFlags(mostRecentlyModified, flagDetail, field, allPartyFlags));
            }
        });

    }

    private void mapModifiedFieldToPartyFlags(Element<FlagDetail> mostRecentlyModified,
                                              Element<FlagDetail> flagDetail,
                                              Field field,
                                              AllPartyFlags allPartyFlags) {
        field.setAccessible(true);
        try {
            Flags flags = (Flags) field.get(allPartyFlags);
            if (flags != null && flags.getDetails() != null && flags.getDetails().contains(flagDetail)) {
                final int index = flags.getDetails().indexOf(flagDetail);
                flags.getDetails().set(index, mostRecentlyModified);
                field.set(allPartyFlags, flags);
            }
        } catch (IllegalAccessException e) {
            //ignore
        }
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
            .toList();
    }

    private  <T> T deepCopy(T object, Class<T> objectClass) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(object), objectClass);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
}
