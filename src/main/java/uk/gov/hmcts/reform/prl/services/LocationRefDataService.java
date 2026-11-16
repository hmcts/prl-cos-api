package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.LocationRefDataApi;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.court.CourtDetails;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.apache.logging.log4j.util.Strings.concat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_BASE_LOCATION_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_BASE_LOCATION_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_REGION_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_REGION_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FAMILY_COURT_TYPE_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationRefDataService {
    public static final String LOCATION_REFERENCE_DATA_LOOKUP_FAILED = "Location Reference Data Lookup Failed - ";
    private final AuthTokenGenerator authTokenGenerator;
    private final LocationRefDataApi locationRefDataApi;

    public static final String SCOTLAND = "Scotland";
    public static final String MIDLANDS = "Midlands";

    @Value("${courts.filter}")
    protected String courtsToFilter;

    @Value("${courts.daFilter}")
    protected String daCourtsToFilter;

    @Value("${courts.caDefaultCourtEpimmsID}")
    protected String caDefaultCourtEpimmsID;

    public boolean isCourtOnboarded(String epimmsId) {
        if (StringUtils.isBlank(epimmsId) || StringUtils.isBlank(courtsToFilter)) {
            return false;
        }

        boolean configured = false;
        for (String court : courtsToFilter.split(",")) {
            String[] entry = court.split(":", 2);
            if (epimmsId.trim().equals(entry[0].trim())) {
                if (entry.length > 1 && StringUtils.isNotBlank(entry[1])) {
                    return false;
                }
                configured = true;
            }
        }
        return configured;
    }

    public List<DynamicListElement> getCourtLocations(String authToken) {
        try {
            CourtDetails courtDetails = locationRefDataApi.getCourtDetailsByService(
                authToken,
                authTokenGenerator.generate(),
                SERVICE_ID
            );
            return onlyEnglandAndWalesLocations(courtDetails).stream()
                .sorted(Comparator.comparing(m -> m.getLabel(), Comparator.naturalOrder()))
                .toList();
        } catch (FeignException e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());
    }

    public List<DynamicListElement> getDaCourtLocations(String authToken) {
        try {
            CourtDetails courtDetails = locationRefDataApi.getCourtDetailsByService(
                authToken,
                authTokenGenerator.generate(),
                SERVICE_ID
            );
            return daOnlyEnglandAndWalesLocations(courtDetails);
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());
    }

    public List<DynamicListElement> getFilteredCourtLocations(String authToken) {
        try {
            CourtDetails courtDetails = locationRefDataApi.getCourtDetailsByService(
                authToken,
                authTokenGenerator.generate(),
                SERVICE_ID
            );
            return filterOnboardedCourtList(this.courtsToFilter,courtDetails);
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());
    }

    public List<DynamicListElement> getDaFilteredCourtLocations(String authToken) {
        try {
            CourtDetails courtDetails = locationRefDataApi.getCourtDetailsByService(
                authToken,
                authTokenGenerator.generate(),
                SERVICE_ID
            );
            return filterOnboardedCourtList(this.daCourtsToFilter, courtDetails);
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());
    }

    private List<DynamicListElement> daOnlyEnglandAndWalesLocations(CourtDetails locationRefData) {
        String[] courtList = daCourtsToFilter.split(",");

        return (locationRefData == null
            ? new ArrayList<>()
            : locationRefData.getCourtVenues().stream().filter(location -> !SCOTLAND.equals(location.getRegion()))
            .filter(location -> FAMILY_COURT_TYPE_ID.equalsIgnoreCase(location.getCourtTypeId()))
            .filter(location -> {
                if (courtList.length == 1) {
                    return true;
                }
                List<String> ids = Arrays.stream(courtList).map(ele -> Arrays.stream(ele.split(":")).toArray()[0]
                    .toString())
                    .toList();
                return ids.contains(location.getCourtEpimmsId());
            })
            .map(this::getDaDisplayEntry).toList());
    }

    private DynamicListElement getDaDisplayEntry(CourtVenue location) {
        String value = concat(
            concat(concat(location.getSiteName(), " - "), concat(location.getCourtAddress(), " - ")),
            location.getPostcode()
        );
        String key = location.getCourtEpimmsId() + ":";
        if (daCourtsToFilter.length() > 1) {
            Optional<String> code = Arrays.stream(daCourtsToFilter.split(",")).filter(ele -> Arrays.stream(ele.split(":")).toArray()[0]
                .toString().equalsIgnoreCase(location.getCourtEpimmsId())).findFirst();
            if (code.isPresent()) {
                key += Arrays.stream(code.get().split(":")).toArray().length > 1
                    ? Arrays.stream(code.get().split(":")).toArray()[1] : "";
            }
        }
        return DynamicListElement.builder().code(key).label(value).build();
    }

    private List<DynamicListElement> onlyEnglandAndWalesLocations(CourtDetails locationRefData) {
        String[] courtList = courtsToFilter.split(",");

        return (locationRefData == null
            ? new ArrayList<>()
            : locationRefData.getCourtVenues().stream().filter(location -> !SCOTLAND.equals(location.getRegion()))
            .filter(location -> FAMILY_COURT_TYPE_ID.equalsIgnoreCase(location.getCourtTypeId()))
            .filter(location -> {
                if (courtList.length == 1) {
                    return true;
                }
                List<String> ids = Arrays.stream(courtList).map(ele -> Arrays.stream(ele.split(":")).toArray()[0]
                    .toString())
                    .toList();
                return ids.contains(location.getCourtEpimmsId());
            })
            .map(this::getDisplayEntry).toList());
    }

    private List<DynamicListElement> filterOnboardedCourtList(String courtList, CourtDetails locationRefData) {
        String[] filteredCourtArray = Arrays.stream(courtList.split(",")).filter(
            element -> StringUtils.isEmpty(Arrays.stream(element.split(":")).toArray().length > 1
                                               ? element.split(":")[1] : "")
        ).toArray(size -> new String[size]);
        return (locationRefData == null
            ? new ArrayList<>()
            : locationRefData.getCourtVenues().stream()
            .filter(location -> {
                List<String> ids = Arrays.stream(filteredCourtArray).map(ele -> Arrays.stream(ele.split(":")).toArray()[0]
                        .toString()).toList();
                return !SCOTLAND.equals(location.getRegion()) && ids.contains(location.getCourtEpimmsId())
                    && FAMILY_COURT_TYPE_ID.equalsIgnoreCase(location.getCourtTypeId());
            })
            .map(this::getDisplayEntry).toList());
    }


    private DynamicListElement getDisplayEntry(CourtVenue location) {
        String value = concat(
            concat(concat(location.getSiteName(), " - "), concat(location.getCourtAddress(), " - ")),
            location.getPostcode()
        );
        String key = location.getCourtEpimmsId() + ":";
        if (courtsToFilter.length() > 1) {
            Optional<String> code = Arrays.stream(courtsToFilter.split(",")).filter(ele -> Arrays.stream(ele.split(":")).toArray()[0]
                .toString().equalsIgnoreCase(location.getCourtEpimmsId())).findFirst();
            if (code.isPresent()) {
                key += Arrays.stream(code.get().split(":")).toArray().length > 1
                    ? Arrays.stream(code.get().split(":")).toArray()[1] : "";
            }
        }
        return DynamicListElement.builder().code(key).label(value).build();
    }

    public DynamicListElement getDisplayEntryFromEpimmsId(String baseLocationId, String authToken) {
        return getCourtDetailsFromEpimmsId(baseLocationId, authToken)
            .map(this::getDisplayEntry)
            .orElse(DynamicListElement.EMPTY);
    }

    public Optional<CourtVenue> getCourtDetailsFromEpimmsId(String baseLocationId, String authToken) {
        log.info("RefData court venue lookup started: epimmsId={}, serviceCode={}, courtTypeId={}",
                 baseLocationId, SERVICE_ID, FAMILY_COURT_TYPE_ID);
        CourtDetails courtDetails = locationRefDataApi.getCourtDetailsByService(
            authToken,
            authTokenGenerator.generate(),
            SERVICE_ID
        );
        if (courtDetails == null || courtDetails.getCourtVenues() == null) {
            log.warn("RefData court venue response contains no venues: epimmsId={}", baseLocationId);
            return Optional.empty();
        }
        List<CourtVenue> matchingVenues = courtDetails.getCourtVenues().stream()
            .filter(location -> StringUtils.equalsIgnoreCase(baseLocationId, location.getCourtEpimmsId()))
            .toList();
        log.info("RefData court venue response: requestedEpimmsId={}, totalVenueCount={}, matchingVenues={}",
                 baseLocationId, courtDetails.getCourtVenues().size(), matchingVenues);
        Optional<CourtVenue> selectedVenue = courtDetails.getCourtVenues().stream().filter(location -> !SCOTLAND.equals(
                location.getRegion()))
            .filter(location -> FAMILY_COURT_TYPE_ID.equalsIgnoreCase(location.getCourtTypeId()))
            .filter(location -> baseLocationId.equalsIgnoreCase(location.getCourtEpimmsId()))
            .findFirst();
        if (selectedVenue.isPresent()) {
            CourtVenue venue = selectedVenue.get();
            log.info("RefData court venue selected: requestedEpimmsId={}, courtVenueId={}, epimmsId={}, "
                         + "siteName={}, courtName={}, venueName={}, postcode={}",
                     baseLocationId, venue.getCourtVenueId(), venue.getCourtEpimmsId(), venue.getSiteName(),
                     venue.getCourtName(), venue.getVenueName(), venue.getPostcode());
        } else {
            log.warn("RefData court venue response contains no matching England/Wales family court: epimmsId={}, courtTypeId={}",
                     baseLocationId, FAMILY_COURT_TYPE_ID);
        }
        return selectedVenue;
    }


    public CaseManagementLocation getDefaultCourtForCA(String authorisation) {
        CourtDetails courtDetails = locationRefDataApi.getCourtDetailsByService(
            authorisation,
            authTokenGenerator.generate(),
            SERVICE_ID
        );
        CaseManagementLocation defaultCaseManagementLocation = CaseManagementLocation.builder()
            .region(C100_DEFAULT_REGION_ID)
            .baseLocation(C100_DEFAULT_BASE_LOCATION_ID).regionName(C100_DEFAULT_REGION_NAME)
            .baseLocationName(C100_DEFAULT_BASE_LOCATION_NAME).build();

        if (null == courtDetails || null == courtDetails.getCourtVenues()) {
            log.error("******Default court Id is failing, as fallback defaulted to Ctsc stoke****");
            return defaultCaseManagementLocation;
        }

        Optional<CourtVenue> courtVenue = courtDetails.getCourtVenues().stream()
            .filter(location -> caDefaultCourtEpimmsID.equalsIgnoreCase(location.getCourtEpimmsId()))
            .findFirst();
        if (courtVenue.isPresent()) {
            return CaseManagementLocation.builder()
                .baseLocation(courtVenue.get().getCourtEpimmsId())
                .baseLocationName(courtVenue.get().getVenueName())
                .region(courtVenue.get().getRegionId())
                .regionName(courtVenue.get().getRegion()).build();
        }
        log.error("******Default court Id is failing, as fallback defaulted to Ctsc stoke****");
        return defaultCaseManagementLocation;
    }
}
