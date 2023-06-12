package uk.gov.hmcts.reform.prl.services.cafcass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.cafcass.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("cafcassHearingService")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingService {

    @Value("#{'${cafcaas.hearingStatus}'.split(',')}")
    private List<String> hearingStatusList;

    private final CaseDataService caseDataService;

    private Hearings hearingDetails;

    private List<Hearings> listOfHearingDetails;

    private final AuthTokenGenerator authTokenGenerator;

    private final HearingApiClient hearingApiClient;

    public Hearings getHearings(String userToken, String caseReferenceNumber) {
        try {
            hearingDetails = hearingApiClient.getHearingDetails(userToken, authTokenGenerator.generate(), caseReferenceNumber);
            filterHearings();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return hearingDetails;
    }

    public List<Hearings> getHearingsForAllCases(String userToken, Map<String,String> caseIdWithRegionIdMap) {
        try {
            listOfHearingDetails = hearingApiClient.getHearingDetailsForAllCaseIds(userToken, authTokenGenerator.generate(), caseIdWithRegionIdMap);
        } catch (Exception e) {
            log.error("Error while getHearingsForAllCases {}",e.getMessage());
        }
        return listOfHearingDetails;
    }

    public Hearings getHearingsForCitizenCase(String authorisation, String startDate, String endDate, String caseId) throws IOException {
        Hearings hearingDetailsForCitizen = Hearings.hearingsWith().build();

        CafCassResponse cafCassResponse = caseDataService.getCaseData(authorisation, startDate, endDate);
        List<CafCassCaseDetail> casesWithHearings = cafCassResponse.getCases();
        for (CafCassCaseDetail caseWithHearings : casesWithHearings) {
            if (caseWithHearings.getCaseData().getHearingData().getCaseRef().equals(caseId)) {
                hearingDetailsForCitizen.setCaseHearings(caseWithHearings.getCaseData().getHearingData().getCaseHearings());
                hearingDetailsForCitizen.setCaseRef(caseWithHearings.getCaseData().getHearingData().getCaseRef());
                hearingDetailsForCitizen.setCourtName(caseWithHearings.getCaseData().getHearingData().getCourtName());
                hearingDetailsForCitizen.setCourtTypeId(caseWithHearings.getCaseData().getHearingData().getCourtTypeId());
                hearingDetailsForCitizen.setHmctsServiceCode(caseWithHearings.getCaseData().getHearingData().getHmctsServiceCode());
                return hearingDetailsForCitizen;
            }
        }

        return hearingDetailsForCitizen;
    }

    private void filterHearingsForListOfCaseIds() {

        for (Hearings hearingDetailsFromList : listOfHearingDetails) {
            if (hearingDetailsFromList != null && hearingDetailsFromList.getCaseHearings() != null) {

                final List<CaseHearing> caseHearings = hearingDetailsFromList.getCaseHearings();

                final List<String> hearingStatuses = hearingStatusList.stream().map(String::trim).collect(Collectors.toList());

                final List<CaseHearing> hearings = caseHearings.stream()
                    .filter(hearing ->
                                hearingStatuses.stream().anyMatch(hearingStatus -> hearingStatus.equals(
                                    hearing.getHmcStatus()))
                    )
                    .collect(
                        Collectors.toList());

                // if we find any hearing after filteration, change hmc status to null as it's not required in response.
                if (hearings != null && !hearings.isEmpty()) {
                    hearingDetailsFromList.setCaseHearings(hearings);
                    log.debug("Hearings filtered based on Listed hearing");
                }
            }
        }
    }

    private void filterHearings() {

        if (hearingDetails != null && hearingDetails.getCaseHearings() != null)  {

            final List<CaseHearing> caseHearings = hearingDetails.getCaseHearings();

            final List<String> hearingStatuses = hearingStatusList.stream().map(String::trim).collect(Collectors.toList());

            final List<CaseHearing> hearings = caseHearings.stream()
                    .filter(hearing ->
                        hearingStatuses.stream().anyMatch(hearingStatus -> hearingStatus.equals(
                            hearing.getHmcStatus()))
                    )
                .collect(
                    Collectors.toList());


            // if we find any hearing after filteration, change hmc status to null as it's not required in response.
            if (hearings != null && !hearings.isEmpty()) {
                hearingDetails.setCaseHearings(hearings);
                log.debug("Hearings filtered based on Listed hearing");
            } else {
                hearingDetails = null;
            }
        }
    }
}
