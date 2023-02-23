package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CourtSpecificCalenderController {

    @GetMapping(value = "courtSpecificCalender/bank-holidays.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> bankHolidays() throws IOException {
        log.info("bankHolidays()---> Start");
        Resource resource = new ClassPathResource("resources/bank-holidays.json");
        ObjectMapper mapper = new ObjectMapper();
        String readValue = mapper.readValue(resource.getInputStream(), String.class);
        log.info("bankHolidays()---> End");
        return ok(readValue);
    }

}
