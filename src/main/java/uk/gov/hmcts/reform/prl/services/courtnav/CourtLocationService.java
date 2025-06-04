package uk.gov.hmcts.reform.prl.services.courtnav;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.CourtSealFinderService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class CourtLocationService {

    private final CourtFinderService courtFinderService;
    private final CourtSealFinderService courtSealFinderService;
    private final LocationRefDataService locationRefDataService;

    public CaseData populateCourtLocation(String auth, CaseData caseData) throws NotFoundException {
        String epimsId = caseData.getSpecialCourtName();

        if (StringUtils.isNotBlank(epimsId)) {
            Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId(epimsId, auth);
            if (courtVenue.isPresent()) {
                return populateFromEpimsId(caseData, courtVenue.get(), epimsId);
            }
        }

        return populateFromPostcode(caseData);
    }

    private CaseData populateFromEpimsId(CaseData caseData, CourtVenue venue, String epimsId) {
        return caseData.toBuilder()
            .courtName(venue.getCourtName())
            .courtId(epimsId)
            .caseManagementLocation(CaseManagementLocation.builder()
                                        .regionId(venue.getRegionId())
                                        .region(venue.getRegion())
                                        .baseLocation(epimsId)
                                        .baseLocationName(venue.getCourtName())
                                        .build())
            .isCafcass(CaseUtils.cafcassFlag(venue.getRegionId()))
            .courtSeal(courtSealFinderService.getCourtSeal(venue.getRegionId()))
            .build();
    }

    private CaseData populateFromPostcode(CaseData caseData) throws NotFoundException {
        Court court = courtFinderService.getNearestFamilyCourt(caseData);
        String email = courtFinderService.getEmailAddress(court)
            .map(CourtEmailAddress::getAddress)
            .orElse(null);

        return caseData.toBuilder()
            .courtName(court.getCourtName())
            .courtEmailAddress(email)
            .build();
    }
}
