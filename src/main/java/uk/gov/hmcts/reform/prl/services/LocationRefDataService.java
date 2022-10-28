package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.LocationRefDataApi;
import uk.gov.hmcts.reform.prl.models.court.CourtDetails;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.concat;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationRefDataService {
    private final AuthTokenGenerator authTokenGenerator;
    private final LocationRefDataApi locationRefDataApi;

    public List<String> getCourtLocations(String authToken) {
        try {
            CourtDetails courtDetails = locationRefDataApi.getCourtDetailsByService(authToken,
                                                                                   authTokenGenerator.generate(),
                                                                       "ABA5");
            return onlyEnglandAndWalesLocations(courtDetails);
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    private List<String> onlyEnglandAndWalesLocations(CourtDetails locationRefData) {
        return locationRefData == null
            ? new ArrayList<>()
            : locationRefData.getCourtVenues().stream().filter(location -> !"Scotland".equals(location.getRegion()))
            .map(this::getDisplayEntry).collect(Collectors.toList());
    }

    private String getDisplayEntry(CourtVenue location) {
        return concat(concat(concat(location.getSiteName(), " - "), concat(location.getCourtAddress(), " - ")),
                      location.getPostcode());
    }
}
