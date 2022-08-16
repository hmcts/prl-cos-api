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


    @Value("${welshCourts}")
    protected String welshCourt;

    private List<String> welshCourts;


    @PostConstruct
    public void init() {
        welshCourts = Arrays.stream(welshCourt.split(",")).collect(Collectors.toList());

    }


    public boolean isWelshSeal(String courtName) {
        return welshCourts.contains(courtName);
    }

}
