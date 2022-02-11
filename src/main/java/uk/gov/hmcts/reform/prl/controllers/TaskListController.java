package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.utils.DgsSerializer;

import java.util.Map;

@Api
@RestController
@RequestMapping("/update-task-list")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListController extends AbstractCallbackController {

    @Autowired
    ApplicationsTabService applicationsTabService;

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest,
                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) throws JsonProcessingException {

        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());

        String m = callbackRequest.getCaseDetails().getData().toString();

        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());

        SimpleModule module = new SimpleModule();
        module.addSerializer(CaseData.class, new DgsSerializer());
        objectMapper.registerModule(module);


        String serliazed = objectMapper.writeValueAsString(caseData);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(serliazed, Map.class);
        String new1 = map.toString();



        applicationsTabService.updateApplicationTabData(getCaseData(callbackRequest.getCaseDetails()));

        publishEvent(new CaseDataChanged(getCaseData(callbackRequest.getCaseDetails())));
    }
}
