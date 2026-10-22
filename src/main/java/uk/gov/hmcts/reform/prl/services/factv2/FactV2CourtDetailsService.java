package uk.gov.hmcts.reform.prl.services.factv2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.clients.factv2.FactV2Api;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtAddress;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.court.ServiceArea;
import uk.gov.hmcts.reform.prl.models.court.factv2.FactV2CourtDetails;
import uk.gov.hmcts.reform.prl.models.court.factv2.FactV2CourtSearchResult;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class FactV2CourtDetailsService {
    private static final String NEAREST_ACTION = "NEAREST";
    private static final int DEFAULT_SEARCH_LIMIT = 10;

    private final FactV2Api factV2Api;
    private final FactV2TokenProvider tokenProvider;
    private final SystemUserService systemUserService;
    private final CourtFinderApi currentCourtFinderApi;

    @Value("${fact.v2.enabled:false}")
    private boolean enabled;

    @Value("${fact.v2.service-area.fl401:Domestic abuse}")
    private String fl401ServiceArea = "Domestic abuse";

    public Court getCourtDetails(String courtSlug) {
        if (!enabled) {
            return currentCourtFinderApi.getCourtDetails(courtSlug);
        }

        String token = tokenProvider.getAccessToken();
        String userId = systemUserService.getUserId(systemUserService.getSysUserToken());
        FactV2CourtDetails details = factV2Api.getCourtDetailsBySlug(courtSlug, "Bearer " + token, userId);
        Court court = toCurrentCourt(details);
        log.info("Retrieved FaCT V2 court details for slug {}: courtName={}, countyLocationCode={}, emailAddresses={}",
            courtSlug,
            court == null ? null : court.getCourtName(),
            court == null ? null : court.getCountyLocationCode(),
            court == null ? List.of() : court.getCourtEmailAddresses().stream()
                .map(CourtEmailAddress::getAddress)
                .filter(Objects::nonNull)
                .toList());
        return court;
    }

    public Court getNearestDomesticAbuseCourt(String postcode) {
        log.info("Starting FaCT {} FL401 postcode lookup for postcode {}",
            enabled ? "V2" : "V1", postcode);
        if (!enabled) {
            ServiceArea serviceArea = currentCourtFinderApi.findClosestDomesticAbuseCourtByPostCode(postcode);
            if (serviceArea == null || serviceArea.getCourts() == null || serviceArea.getCourts().isEmpty()) {
                log.warn("FaCT V1 returned no FL401 courts for postcode {}", postcode);
                return null;
            }
            String slug = serviceArea.getCourts().getFirst().getCourtSlug();
            log.info("FaCT V1 selected FL401 court slug {} for postcode {}", slug, postcode);
            return getCourtDetails(slug);
        }

        String token = tokenProvider.getAccessToken();
        String userId = systemUserService.getUserId(systemUserService.getSysUserToken());
        log.info("Calling FaCT V2 postcode search for FL401: postcode={}, serviceArea={}, action={}, limit={}",
            postcode, fl401ServiceArea, NEAREST_ACTION, DEFAULT_SEARCH_LIMIT);
        List<FactV2CourtSearchResult> results = factV2Api.getCourtsByPostcode(
            postcode,
            fl401ServiceArea,
            NEAREST_ACTION,
            DEFAULT_SEARCH_LIMIT,
            "Bearer " + token,
            userId
        );
        if (results == null || results.isEmpty() || results.getFirst().getCourtSlug() == null) {
            log.warn("FaCT V2 returned no usable FL401 court results for postcode {}", postcode);
            return null;
        }

        FactV2CourtSearchResult selected = results.getFirst();
        log.info("FaCT V2 returned {} FL401 courts for postcode {}; selected courtName={}, courtId={}, slug={}, distance={}",
            results.size(), postcode, selected.getCourtName(), selected.getCourtId(), selected.getCourtSlug(),
            selected.getDistance());
        return getCourtDetails(selected.getCourtSlug());
    }

    public Court getCourtDetailsFromFactUrl(String factUrl) {
        if (factUrl == null || factUrl.isBlank()) {
            return null;
        }
        String path = URI.create(factUrl).getPath();
        if (path == null || path.isBlank()) {
            return null;
        }
        String normalizedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int lastSlash = normalizedPath.lastIndexOf('/');
        String slug = normalizedPath.substring(lastSlash + 1);
        return slug.isBlank() ? null : getCourtDetails(slug);
    }

    Court toCurrentCourt(FactV2CourtDetails details) {
        if (details == null) {
            return null;
        }
        FactV2CourtDetails.FactV2CourtCode codes = first(details.getCourtCodes());
        FactV2CourtDetails.FactV2ProfessionalInformation professional = first(details.getCourtProfessionalInformation());

        return Court.builder()
            .courtName(details.getName())
            .courtSlug(details.getSlug())
            .open(details.isOpen())
            .countyLocationCode(codes == null || codes.getCountyCourtCode() == null ? 0 : codes.getCountyCourtCode())
            .courtTypes(details.getCourtAddresses() == null ? List.of() : details.getCourtAddresses().stream()
                .filter(Objects::nonNull)
                .flatMap(address -> Optional.ofNullable(address.getCourtTypes()).orElse(List.of()).stream())
                .map(FactV2CourtDetails.FactV2CourtType::getName)
                .filter(Objects::nonNull)
                .distinct()
                .toList())
            .address(mapAddresses(details.getCourtAddresses()))
            .gbs(codes == null ? null : codes.getGbs())
            .dxNumber(details.getCourtDxCodes() == null ? List.of() : details.getCourtDxCodes().stream()
                .filter(Objects::nonNull)
                .map(FactV2CourtDetails.FactV2DxCode::getDxCode)
                .filter(Objects::nonNull)
                .toList())
            .inPerson(details.getCourtAddresses() != null && details.getCourtAddresses().stream()
                .filter(Objects::nonNull)
                .anyMatch(address -> "VISIT_US".equals(address.getAddressType())
                    || "VISIT_OR_CONTACT_US".equals(address.getAddressType())))
            .accessScheme(professional != null && Boolean.TRUE.equals(professional.getAccessScheme()))
            .courtEmailAddresses(mapContacts(details.getCourtContactDetails()))
            .build();
    }

    private List<CourtAddress> mapAddresses(List<FactV2CourtDetails.FactV2Address> addresses) {
        if (addresses == null) {
            return List.of();
        }
        return addresses.stream()
            .filter(Objects::nonNull)
            .map(address -> CourtAddress.builder()
                .addressLines(Stream.of(address.getAddressLine1(), address.getAddressLine2())
                    .filter(Objects::nonNull)
                    .filter(line -> !line.isBlank())
                    .toList())
                .postcode(address.getPostcode())
                .town(address.getTownCity())
                .type(address.getAddressType())
                .build())
            .toList();
    }

    private List<CourtEmailAddress> mapContacts(List<FactV2CourtDetails.FactV2Contact> contacts) {
        if (contacts == null) {
            return List.of();
        }
        return contacts.stream()
            .filter(Objects::nonNull)
            .map(contact -> CourtEmailAddress.builder()
                .address(contact.getEmail())
                .description(contact.getCourtContactDescription() == null
                    ? null : contact.getCourtContactDescription().getName())
                .explanation(contact.getExplanation())
                .build())
            .toList();
    }

    private <T> T first(List<T> values) {
        return values == null || values.isEmpty() ? null : values.getFirst();
    }
}
