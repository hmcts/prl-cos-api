package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.clients.os.OsCourtFinderApi;
import uk.gov.hmcts.reform.prl.models.LocalAuthorityCourt;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.OsPlacesResponse;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OsCourtFinderService {

    private final SystemUserService systemUserService;
    private final OsCourtFinderApi osCourtFinderApi;
    private final LocalAuthorityCourtDataLoader localAuthorityCourtDataLoader;
    private final LocationRefDataService locationRefDataService;
    private final CourtFinderApi courtFinderApi;

    public Court getC100NearestFamilyCourt(String postcode) throws NotFoundException {
        if (postcode == null || postcode.isEmpty()) {
            throw new IllegalArgumentException("postcode is null or empty");
        }
        List<LocalAuthorityCourt> localAuthorityCourtList = localAuthorityCourtDataLoader.getLocalAuthorityCourtList();
        // first check if the postcode is from specific post codes otherwise use os api to get local custodian code
        Optional<LocalAuthorityCourt> selectedLocalAuthorityCourt = localAuthorityCourtList.stream()
            .filter(court -> court.getSpecificPostCodes().stream().anyMatch(postcode::startsWith))
            .findFirst();

        if (selectedLocalAuthorityCourt.isEmpty()) {
            String localCustodianCode = getLocalCustodianCodeByPostCode(postcode);
            if (localCustodianCode != null) {
                selectedLocalAuthorityCourt = localAuthorityCourtList.stream()
                    .filter(court -> court.getLocalCustodianCode().equals(localCustodianCode))
                    .findFirst();
            }
        }
        if (selectedLocalAuthorityCourt.isPresent()) {
            Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId(
                selectedLocalAuthorityCourt.get().getEpimmsId(),
                systemUserService.getSysUserToken()
            );
            if (courtVenue.isPresent()) {
                return getFactCourtDetails(courtVenue.get());
            }
        }

        return null;
    }

    public String getLocalCustodianCodeByPostCode(String postCode) throws NotFoundException {
        OsPlacesResponse osPlacesResponse = null;
        try {
            osPlacesResponse = osCourtFinderApi.findCouncilByPostcode(postCode);
        } catch (Exception e) {
            log.info("OsCourtFinderService.getLocalCustodianCodeByPostCode() method is throwing exception : {}",e);
        }
        if (osPlacesResponse != null
            && !isEmpty(osPlacesResponse.getResults())) {
            return osPlacesResponse.getResults().getFirst().getDpa().getLocalCustodianCode();
        }
        return null;
    }

    private Court getFactCourtDetails(CourtVenue courtVenue) {
        Court court = null;
        String factUrl = courtVenue.getFactUrl();
        if (factUrl != null && factUrl.split("/").length > 4) {
            try {
                court = courtFinderApi.getCourtDetails(factUrl.split("/")[4]);
            } catch (Exception ex) {
                log.error("Error fetching court details from Fact ", ex);
            }
        }
        return court;
    }

}
