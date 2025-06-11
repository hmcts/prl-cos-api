package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.prl.models.holidaydates.UkHolidayDates;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
public class CourtSpecificCalenderControllerTest {

    @InjectMocks
    private CourtSpecificCalenderController courtSpecificCalenderController;

    @Test
    public void testGetBankHolidays() throws IOException {
        Resource resource = new ClassPathResource("/bank-holidays.json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        UkHolidayDates ukHolidayDates = mapper.readValue(resource.getInputStream(), UkHolidayDates.class);
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<UkHolidayDates> expectedResponse = new ResponseEntity<>(ukHolidayDates, headers, OK);
        ResponseEntity<?> response = courtSpecificCalenderController.bankHolidays();
        assertEquals(expectedResponse.hasBody(), response.hasBody());
    }
}
