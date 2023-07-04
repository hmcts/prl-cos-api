package uk.gov.hmcts.reform.prl.services.hearings;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingService {

    private List<CaseLinkedData> caseLinkedData;

    private final AuthTokenGenerator authTokenGenerator;

    private final HearingApiClient hearingApiClient;

    @Value("#{'${hearing_component.futureHearingStatus}'.split(',')}")
    private List<String> futureHearingStatusList;

    public Hearings getHearings(String userToken, String caseReferenceNumber) {
        Hearings hearings = null;
        try {
            hearings = hearingApiClient.getHearingDetails(userToken, authTokenGenerator.generate(), caseReferenceNumber);

            if (hearings != null) {
                for (CaseHearing eachHearing : hearings.getCaseHearings()) {
                    eachHearing.setNextHearingDate(getNextHearingDateWithInHearing(eachHearing));
                    eachHearing.setUrgentFlag(getUrgentFlagWithInHearing(eachHearing));
                }
            }
            return hearings;

        } catch (Exception e) {
            log.error("Error in getting hearings ", e);
        }
        return null;
    }


    public List<CaseLinkedData> getCaseLinkedData(String userToken, CaseLinkedRequest caseLinkedRequest) {

        try {
            caseLinkedData = hearingApiClient.getCaseLinkedData(userToken, authTokenGenerator.generate(), caseLinkedRequest);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return caseLinkedData;
    }


    public NextHearingDetails getNextHearingDate(String userToken, String caseReferenceNumber) {

        try {
            return hearingApiClient.getNextHearingDate(userToken, authTokenGenerator.generate(), caseReferenceNumber);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public Hearings getFutureHearings(String userToken, String caseReferenceNumber) {

        try {
            return hearingApiClient.getFutureHearings(userToken, authTokenGenerator.generate(), caseReferenceNumber);
        } catch (Exception e) {
            log.error("Error in getFutureHearings ----> {}", e);
        }
        return null;
    }

    private LocalDateTime getNextHearingDateWithInHearing(CaseHearing hearing) {

        LocalDateTime nextHearingDate = null;
        LocalDateTime tempNextDateListed = null;
        if (hearing.getHmcStatus().equals(LISTED)) {
            Optional<LocalDateTime> minDateOfHearingDaySche = hearing.getHearingDaySchedule().stream()
                   .filter(u -> u.getHearingStartDateTime().isAfter(LocalDateTime.now()))
                   .map(u -> u.getHearingStartDateTime())
                   .min(LocalDateTime::compareTo);
            if (minDateOfHearingDaySche.isPresent() && (tempNextDateListed == null || tempNextDateListed.isAfter(minDateOfHearingDaySche.get()))) {
                tempNextDateListed = minDateOfHearingDaySche.get();
                nextHearingDate = tempNextDateListed;
            }
        }
        return nextHearingDate;
    }

    private boolean getUrgentFlagWithInHearing(CaseHearing hearing) {

        LocalDateTime urgencyLimitDate = LocalDateTime.now().plusDays(15).withNano(1);
        final List<String> hearingStatuses =
            futureHearingStatusList.stream().map(String::trim).collect(Collectors.toList());

        boolean isInFutureHearingStatusList = hearingStatuses.stream()
            .anyMatch(
                hearingStatus -> hearingStatus.equals(hearing.getHmcStatus())
            );

        return isInFutureHearingStatusList
            && hearing.getHearingDaySchedule() != null
            && hearing.getHearingDaySchedule().stream()
                .filter(
                    hearDaySche ->
                        hearDaySche
                            .getHearingStartDateTime()
                            .isAfter(
                                LocalDateTime
                                    .now())
                            &&
                            hearDaySche
                                .getHearingStartDateTime()
                                .isBefore(
                                    urgencyLimitDate)
                )
                .collect(Collectors.toList())
                .size() > 0;

    }
}
