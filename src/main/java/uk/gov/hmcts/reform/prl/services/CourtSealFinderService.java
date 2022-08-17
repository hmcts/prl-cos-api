package uk.gov.hmcts.reform.prl.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;




@Slf4j
@Service
public class CourtSealFinderService {


    @Value("${welshCourtIds}")
    protected String welshCourtIds;

    private List<String> welshCourtCodes;


    @PostConstruct
    public void init() {
        welshCourtCodes = Arrays.stream(welshCourtIds.split(",")).collect(Collectors.toList());

    }


    public boolean isWelshSeal(String courtName) {
        return welshCourtCodes.contains(courtName);
    }

}
