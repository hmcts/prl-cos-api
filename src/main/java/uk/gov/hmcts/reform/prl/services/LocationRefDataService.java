package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    @Value("${courts.daFilter}")
    protected String daCourtsToFilter;

    public List<DynamicListElement> getCourtLocations(String authToken) {
        try {
            CourtDetails courtDetails = locationRefDataApi.getCourtDetailsByService(
                authToken,
                authTokenGenerator.generate(),
                SERVICE_ID
            );
            return onlyEnglandAndWalesLocations(courtDetails);
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
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
            return daOnlyEnglandAndWalesLocations(courtDetails, "FL401");
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
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
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
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
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());
    }

    private List<DynamicListElement> daOnlyEnglandAndWalesLocations(CourtDetails locationRefData, String caseType) {
        String[] courtList = daCourtsToFilter.split(",");

        return (locationRefData == null
            ? new ArrayList<>()
            : locationRefData.getCourtVenues().stream().filter(location -> !"Scotland".equals(location.getRegion()))
            .filter(location -> FAMILY_COURT_TYPE_ID.equalsIgnoreCase(location.getCourtTypeId()))
            .filter(location -> {
                if (courtList.length == 1) {
                    return true;
                }
                List<String> ids = Arrays.stream(courtList).map(ele -> Arrays.stream(ele.split(":")).toArray()[0]
                    .toString())
                    .collect(Collectors.toList());
                return ids.contains(location.getCourtEpimmsId());
            })
            .map(this::getDaDisplayEntry).collect(Collectors.toList()));
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
            : locationRefData.getCourtVenues().stream().filter(location -> !"Scotland".equals(location.getRegion()))
            .filter(location -> FAMILY_COURT_TYPE_ID.equalsIgnoreCase(location.getCourtTypeId()))
            .filter(location -> {
                if (courtList.length == 1) {
                    return true;
                }
                List<String> ids = Arrays.stream(courtList).map(ele -> Arrays.stream(ele.split(":")).toArray()[0]
                    .toString())
                    .collect(Collectors.toList());
                return ids.contains(location.getCourtEpimmsId());
            })
            .map(this::getDisplayEntry).collect(Collectors.toList()));
    }

    private List<DynamicListElement> filterOnboardedCourtList(String courtList, CourtDetails locationRefData) {
        List<String> filteredCourtList = Arrays.stream(courtList.split(",")).filter(
            element -> StringUtils.isEmpty(Arrays.stream(element.split(":")).toArray().length > 1
                                               ? element.split(":")[1] : "")
        ).collect(Collectors.toList());

        String[] filteredCourtArray = filteredCourtList.toArray(new String[filteredCourtList.size()]);
        return (locationRefData == null
            ? new ArrayList<>()
            : locationRefData.getCourtVenues().stream().filter(location -> !"Scotland".equals(location.getRegion()))
            .filter(location -> FAMILY_COURT_TYPE_ID.equalsIgnoreCase(location.getCourtTypeId()))
            .filter(location -> {
                if (filteredCourtArray.length == 1) {
                    return true;
                }
                List<String> ids = Arrays.stream(filteredCourtArray).map(ele -> Arrays.stream(ele.split(":")).toArray()[0]
                        .toString())
                    .collect(Collectors.toList());
                return ids.contains(location.getCourtEpimmsId());
            })
            .map(this::getDisplayEntry).collect(Collectors.toList()));
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

    public Optional<CourtVenue> getCourtDetailsFromEpimmsId(String baseLocationId, String authToken) {
        CourtDetails courtDetails = locationRefDataApi.getCourtDetailsByService(
            authToken,
            authTokenGenerator.generate(),
            SERVICE_ID
        );
        return  (null == courtDetails || null == courtDetails.getCourtVenues())
            ? Optional.empty()
            : courtDetails.getCourtVenues().stream().filter(location -> !"Scotland".equals(
                location.getRegion()))
            .filter(location -> FAMILY_COURT_TYPE_ID.equalsIgnoreCase(location.getCourtTypeId()))
            .filter(location -> baseLocationId.equalsIgnoreCase(location.getCourtEpimmsId()))
            .findFirst();
    }
}
