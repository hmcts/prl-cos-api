package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.prl.clients.os.OsCourtFinderApi;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.OsPlacesResponse;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OsCourtFinderService {
    private final OsCourtFinderApi osCourtFinderApi;

    public String getLocalCustodianCodeByPostCode(String postCode) throws NotFoundException {
        OsPlacesResponse osPlacesResponse = null;
        try {
            osPlacesResponse = osCourtFinderApi.findCouncilByPostcode(postCode);
        } catch (Exception e) {
            log.info("OsCourtFinderService.getLocalCustodianCodeByPostCode() method is throwing exception : {}",e);
        }
        if (osPlacesResponse != null
            && !CollectionUtils.isEmpty(osPlacesResponse.getResults())) {
            return osPlacesResponse.getResults().getFirst().getDpa().getLocalCustodianCode();
        }
        return null;
    }
}
