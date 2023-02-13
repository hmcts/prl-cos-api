package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.LocationRefDataApi;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.court.CourtDetails;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.concat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FAMILY_COURT_TYPE_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationRefDataService {
    private final AuthTokenGenerator authTokenGenerator;
    private final LocationRefDataApi locationRefDataApi;

    @Value("${courts.filter}")
    protected String courtsToFilter;

    @Value("${locationfinder.api.url}")
    protected String locationfinderUrl;

    public List<DynamicListElement> getCourtLocations(String authToken) {
        try {
            CourtDetails courtDetails = locationRefDataApi.getCourtDetailsByService(authToken,
                                                                                    authTokenGenerator.generate(),
                                                                                    SERVICE_ID);
            log.info("courtDetails in location Ref data service {}", courtDetails);
            if (null != courtDetails) {
                log.info("court venues in location Ref data service {}", null != courtDetails.getCourtVenues()
                    ? courtDetails.getCourtVenues().size() : "court venues is empty");
            }
            log.info("location ref url in location Ref data service {}", locationfinderUrl);
            log.info("courtsTofilter in location Ref data service {}", courtsToFilter);
            return onlyEnglandAndWalesLocations(courtDetails);
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());
    }

    private List<DynamicListElement> onlyEnglandAndWalesLocations(CourtDetails locationRefData) {
        String[] courtList = courtsToFilter.split(",");

        return (locationRefData == null
            ? new ArrayList<>()
            : locationRefData.getCourtVenues().stream().filter(location -> !"Scotland".equals(location.getRegion()))
            .filter(location -> FAMILY_COURT_TYPE_ID.equalsIgnoreCase(location.getCourtTypeId()))
            .filter(location -> {
                log.info("locations inside filter locations {}",location.getCourtEpimmsId());
                if (courtList.length == 1) {
                    log.info("courtList lenght is 1 inside onlyEnglandAndWalesLocations");
                    return true;
                }
                return Arrays.asList(courtList).contains(location.getCourtEpimmsId());
            })
            .map(this::getDisplayEntry).collect(Collectors.toList()));
    }

    private DynamicListElement getDisplayEntry(CourtVenue location) {
        String value = concat(concat(concat(location.getSiteName(), " - "), concat(location.getCourtAddress(), " - ")),
                              location.getPostcode());
        String key = location.getCourtEpimmsId();
        log.info("key in display entry method() {} ",key);
        log.info("value in display entry method() {} ",value);
        return DynamicListElement.builder().code(key).label(value).build();
    }

    public String getCourtDetailsFromEpimmsId(String baseLocationId, String authToken) {
        CourtDetails courtDetails = locationRefDataApi.getCourtDetailsByService(authToken,
                                                                                authTokenGenerator.generate(),
                                                                                SERVICE_ID);
        Optional<CourtVenue> courtVenue = courtDetails.getCourtVenues().stream().filter(location -> !"Scotland".equals(location.getRegion()))
            .filter(location -> FAMILY_COURT_TYPE_ID.equalsIgnoreCase(location.getCourtTypeId()))
            .filter(location -> baseLocationId.equalsIgnoreCase(location.getCourtEpimmsId()))
            .findFirst();
        return courtVenue.map(venue -> venue.getCourtEpimmsId() + "-" + venue.getRegionId()
            + "-" + venue.getCourtName() + "-" + venue.getPostcode() + "-" + venue.getRegion()
            + "-" + venue.getSiteName()).orElse("");
    }
}
