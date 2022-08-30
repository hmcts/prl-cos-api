package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CafCassControllerIntegrationTest {
    @Autowired
    private transient MockMvc mockMvc;

    @Test
    public void givenValidDatetimeRangeSearchCasesByCafCassControllerReturnOkStatus() throws Exception {
        mockMvc.perform(
                        get("/searchCases")
                                .contentType(APPLICATION_JSON)
                                .header("authorisation", "authorisationKey")
                                .header("serviceauthorisation", "serviceauthorisationKey")
                                .queryParam("start_date", "2022-08-22T10:39:43.49")
                                .queryParam("end_date", "2022-08-26T10:44:54.055"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void givenNullStartDateTimeWithValidEndDateSearchCasesByCafCassControllerReturnsErrorStatus() throws Exception {
        mockMvc.perform(
                        get("/searchCases")
                                .contentType(APPLICATION_JSON)
                                .header("authorisation", "authorisationKey")
                                .header("serviceauthorisation", "serviceauthorisationKey")
                                .queryParam("start_date", null)
                                .queryParam("end_date", "2022-08-26T10:44:54.055"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}