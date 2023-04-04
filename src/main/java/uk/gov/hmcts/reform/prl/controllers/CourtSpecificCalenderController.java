package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.models.holidaydates.UkHolidayDates;

import java.io.IOException;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CourtSpecificCalenderController {

    @GetMapping(value = "courtSpecificCalender/bank-holidays.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UkHolidayDates> bankHolidays() throws IOException {
        Resource resource = new ClassPathResource("/bank-holidays.json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        UkHolidayDates ukHolidayDates = mapper.readValue(resource.getInputStream(), UkHolidayDates.class);
        return ok(ukHolidayDates);
    }

}
