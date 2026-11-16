package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.os.OsCourtFinderApi;
import uk.gov.hmcts.reform.prl.exception.OsCourtLookupException;
import uk.gov.hmcts.reform.prl.models.LocalAuthorityCourt;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.Dpa;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.OsPlacesResponse;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.Result;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

    public CourtVenue getC100NearestFamilyCourt(String postcode) throws NotFoundException {
        if (postcode == null || postcode.isEmpty()) {
            throw new IllegalArgumentException("postcode is null or empty");
        }
        log.info("C100 court allocation started: postcode={}", postcode);
        String normalisedPostcode = postcode.strip().toUpperCase(Locale.ROOT);
        List<LocalAuthorityCourt> localAuthorityCourtList = localAuthorityCourtDataLoader.getLocalAuthorityCourtList();
        // first check if the postcode is from specific post codes otherwise use os api to get local custodian code
        Optional<LocalAuthorityCourt> selectedLocalAuthorityCourt = localAuthorityCourtList.stream()
            .filter(court -> CollectionUtils.isNotEmpty(court.getSpecificPostCodes()))
            .filter(court -> court.getSpecificPostCodes().stream().anyMatch(normalisedPostcode::startsWith))
            .findFirst();

        boolean postcodeOverride = selectedLocalAuthorityCourt.isPresent();
        if (postcodeOverride) {
            log.info("C100 court allocation matched postcode override: postcode={}, configuredPostcodePrefixes={}",
                     postcode, selectedLocalAuthorityCourt.get().getSpecificPostCodes());
        }
        if (selectedLocalAuthorityCourt.isEmpty()) {
            String localCustodianCode = getLocalCustodianCodeByPostCode(postcode);
            if (localCustodianCode != null) {
                selectedLocalAuthorityCourt = localAuthorityCourtList.stream()
                    .filter(court -> court.getLocalCustodianCode().equals(localCustodianCode))
                    .findFirst();
            }
            if (selectedLocalAuthorityCourt.isEmpty()) {
                log.warn("C100 court allocation found no local-authority mapping: postcode={}, localCustodianCode={}",
                         postcode, localCustodianCode);
            }
        }
        if (selectedLocalAuthorityCourt.isPresent()) {
            LocalAuthorityCourt mapping = selectedLocalAuthorityCourt.get();
            log.info("C100 court allocation mapping selected: postcode={}, source={}, localCustodianCode={}, "
                         + "localAuthority={}, configuredCourtName={}, epimmsId={}",
                     postcode, postcodeOverride ? "postcode override" : "OS local authority", mapping.getLocalCustodianCode(),
                     mapping.getLocalAuthority(), mapping.getRefDataCourtName(), mapping.getEpimmsId());
            Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId(
                mapping.getEpimmsId(),
                systemUserService.getSysUserToken()
            );
            if (courtVenue.isPresent()) {
                log.info("Resolved court from OS and RefData: postcode={}, epimmsId={}, siteName={}, courtName={}",
                         postcode, courtVenue.get().getCourtEpimmsId(), courtVenue.get().getSiteName(),
                         courtVenue.get().getCourtName());
                return courtVenue.get();
            }
            log.warn("C100 court allocation found no RefData family court venue: postcode={}, epimmsId={}",
                     postcode, mapping.getEpimmsId());
        }

        return null;
    }

    public String getLocalCustodianCodeByPostCode(String postCode) throws NotFoundException {
        OsPlacesResponse osPlacesResponse;
        log.info("OS postcode lookup started: postcode={}", postCode);
        try {
            osPlacesResponse = osCourtFinderApi.findCouncilByPostcode(postCode);
        } catch (Exception e) {
            log.error("OS postcode lookup failed: postcode={}", postCode, e);
            throw new OsCourtLookupException(e);
        }
        if (osPlacesResponse != null && !isEmpty(osPlacesResponse.getResults())) {
            List<String> localAuthorities = osPlacesResponse.getResults().stream()
                .map(Result::getDpa)
                .filter(Objects::nonNull)
                .map(dpa -> String.format("postcode=%s, localCustodianCode=%s, localAuthority=%s",
                                         dpa.getPostcode(), dpa.getLocalCustodianCode(), dpa.getLocalCustodianCodeDescription()))
                .distinct()
                .toList();
            log.info("OS postcode lookup response: postcode={}, resultCount={}, localAuthorities={}",
                     postCode, osPlacesResponse.getResults().size(), localAuthorities);
            List<Result> filteredResultList = osPlacesResponse.getResults().stream()
                .filter(result -> StringUtils.isNotEmpty(result.getDpa().getLocalCustodianCodeDescription()))
                .toList();
            if (CollectionUtils.isNotEmpty(filteredResultList)) {
                Dpa selectedAddress = filteredResultList.getFirst().getDpa();
                log.info("OS local authority selected: postcode={}, localCustodianCode={}, localAuthority={}",
                         postCode, selectedAddress.getLocalCustodianCode(), selectedAddress.getLocalCustodianCodeDescription());
                return selectedAddress.getLocalCustodianCode();
            }
        }
        log.warn("OS postcode lookup found no usable local authority: postcode={}", postCode);
        return null;
    }

}
