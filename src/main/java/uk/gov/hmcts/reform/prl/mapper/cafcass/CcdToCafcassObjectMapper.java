package uk.gov.hmcts.reform.prl.mapper.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CcdToCafcassObjectMapper {

    @Autowired
    private ObjectMapper objectMapper;

}
