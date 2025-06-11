package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.prl.Application;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RootController.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {Application.class})
@PropertySource(value = "classpath:application.yaml")
@Ignore
public class RootControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldResponseSuccess() throws Exception {

        // given
        MockHttpServletRequestBuilder getRequest = MockMvcRequestBuilders.get("/");

        // when
        ResultActions performedGet = mvc.perform(getRequest);

        // then
        performedGet.andExpect(status().isOk()).andReturn();
    }
}
