package uk.gov.hmcts.reform.prl.utils;



import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;

@Slf4j
@Component
public class HearingUtils {

    @Autowired
    HearingService hearingService;

    public   List<DynamicListElement> getHearingStartDate(String authorization,CaseData caseData) {
        try {
            String caseReferenceNumber =  String.valueOf(caseData.getId());
            Hearings hearingDetails = hearingService.getHearings(authorization, caseReferenceNumber);
            log.info("Hearing Details from hmc for the case id:{}",caseReferenceNumber);
            if (null != hearingDetails && null != hearingDetails.getCaseHearings()) {
                return hearingDetails.getCaseHearings().stream()
                    .filter(caseHearing -> LISTED.equalsIgnoreCase(caseHearing.getHmcStatus()))
                        .map(CaseHearing::getHearingDaySchedule).collect(Collectors.toList()).stream()
                    .flatMap(Collection::stream)
                    .map(this::displayEntry)
                    .collect(Collectors.toList());


            }
        } catch (Exception e) {
            log.error("List of Hearing Start Date Values look up failed - " + e.getMessage(), e);
        }

        return List.of(DynamicListElement.builder().build());
    }

    private DynamicListElement displayEntry(HearingDaySchedule hearingDaySchedule) {
        LocalDateTime hearingStartDateTime = hearingDaySchedule.getHearingStartDateTime();
        return DynamicListElement.builder().code(String.valueOf(hearingStartDateTime)).label(String.valueOf(hearingStartDateTime)).build();
    }


}
