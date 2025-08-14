package uk.gov.hmcts.reform.prl.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class CourtSealFinderService {

    public String getCourtSeal(String regionId) {
        return "7".equals(regionId) ? "[userImage:familycourtseal-bilingual.png]" : "[userImage:familycourtseal.png]";
    }

}