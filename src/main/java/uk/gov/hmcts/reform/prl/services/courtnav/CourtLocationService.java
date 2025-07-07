package uk.gov.hmcts.reform.prl.services.courtnav;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.exception.CourtLocationUnprocessableException;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CourtSealFinderService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtLocationService {

    private final CourtSealFinderService courtSealFinderService;
    private final LocationRefDataService locationRefDataService;

    public CaseData populateCourtLocation(String auth, CaseData caseData) {
        String epimsId = caseData.getSpecialCourtName();
        CaseData populatedCaseData = caseData;

        if (StringUtils.isNotBlank(epimsId)) {
            Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId(epimsId, auth);

            if (courtVenue.isPresent()) {
                populatedCaseData = populateFromEpimsId(caseData, courtVenue.get(), epimsId);
            } else {
                log.warn("Could not find court venue for EPIMS ID: {}", epimsId);
            }
        } else {
            log.debug("No EPIMS ID provided on case");
        }

        validateCaseManagementLocation(populatedCaseData);
        return populatedCaseData;
    }

    private CaseData populateFromEpimsId(CaseData caseData, CourtVenue venue, String epimsId) {
        return caseData.toBuilder()
            .courtName(venue.getCourtName())
            .courtId(epimsId)
            .caseManagementLocation(CaseManagementLocation.builder()
                                        .region(venue.getRegionId())
                                        .regionName(venue.getRegion())
                                        .baseLocation(epimsId)
                                        .baseLocationId(epimsId)
                                        .baseLocationName(venue.getCourtName())
                                        .build())
            .isCafcass(CaseUtils.cafcassFlag(venue.getRegionId()))
            .courtSeal(courtSealFinderService.getCourtSeal(venue.getRegionId()))
            .build();
    }

    private void validateCaseManagementLocation(CaseData caseData) {
        CaseManagementLocation location = caseData.getCaseManagementLocation();

        if (location == null
            || isBlank(location.getRegion())
            || isBlank(location.getBaseLocation())
            || isBlank(location.getRegionName())
            || isBlank(location.getBaseLocationName())) {

            log.warn("Case management location is invalid: one or more required fields are missing or blank.");

            throw new CourtLocationUnprocessableException("Case management location is invalid.");
        }
    }
}
