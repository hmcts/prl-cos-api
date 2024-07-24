package uk.gov.hmcts.reform.prl.filter.cafcaas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Element;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.InterpreterNeed;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CafCassFilterTest {

    @InjectMocks
    private CafCassFilter cafCassFilter;
    private static final String jsonInString =
        "classpath:response/CafCaasResponse.json";

    @Test
    public void filterTest() throws IOException {
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
    public void filterNonValueListTest() {
        List<Element<InterpreterNeed>> elementList = new ArrayList<>();
        assertNotNull(cafCassFilter.filterNonValueList(elementList));
    }

    @Test
    public void filterNonValueListTest1() {
        List<Element<InterpreterNeed>> elementList = new ArrayList<>();
        Element element = Element.builder().build();
        elementList.add(element);
        assertNotNull(cafCassFilter.filterNonValueList(elementList));
    }

    @Test
    public void filterNonValueListTest2() {
        List<Element<InterpreterNeed>> elementList = new ArrayList<>();
        Element element = Element.builder().value(InterpreterNeed.builder().build()).build();
        elementList.add(element);
        assertNotNull(cafCassFilter.filterNonValueList(elementList));
    }
}
