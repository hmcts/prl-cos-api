package uk.gov.hmcts.reform.prl.filter.cafcaas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Element;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.InterpreterNeed;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CafCassFilterTest {

    @InjectMocks
    private CafCassFilter cafCassFilter;
    private static final String jsonInString =
        "classpath:response/CafCaasResponse.json";

    @Test
    void filterTest() throws IOException {
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
        CafCassResponse cafCassResponse = objectMapper.readValue(
            TestResourceUtil.readFileFrom(jsonInString),
            CafCassResponse.class
        );
        cafCassFilter.filter(cafCassResponse);
        assertEquals(cafCassResponse.getTotal(), cafCassResponse.getCases().size());
    }

    @Test
    void filterNonValueListTest() {
        List<Element<InterpreterNeed>> elementList = new ArrayList<>();
        assertNotNull(cafCassFilter.filterNonValueList(elementList));
    }

    @Test
    void filterNonValueListTest1() {
        List<Element<InterpreterNeed>> elementList = new ArrayList<>();
        Element element = Element.builder().build();
        elementList.add(element);
        assertNotNull(cafCassFilter.filterNonValueList(elementList));
    }

    @Test
    void filterNonValueListTest2() {
        List<Element<InterpreterNeed>> elementList = new ArrayList<>();
        Element element = Element.builder().value(InterpreterNeed.builder().build()).build();
        elementList.add(element);
        assertNotNull(cafCassFilter.filterNonValueList(elementList));
    }
}
