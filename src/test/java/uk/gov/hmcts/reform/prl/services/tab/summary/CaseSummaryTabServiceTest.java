package uk.gov.hmcts.reform.prl.services.tab.summary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OtherProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.AllegationOfHarmGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.AllocatedJudgeDetailsGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.CaseClosedDateGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.CaseStatusGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.ConfidentialDetailsGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.DateOfSubmissionGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.OrderAppliedForGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.OtherProceedingsGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.SpecialArrangementsGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.UrgencyGenerator;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.refuge.RefugeCaseGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CaseSummaryTabServiceTest {

    @InjectMocks
    CaseSummaryTabService caseSummaryTabService;

    @Mock
    AllocatedJudgeDetailsGenerator allocatedJudgeDetailsGenerator;

    @Mock
    RefugeCaseGenerator refugeCaseGenerator;

    @Mock
    CaseStatusGenerator caseStatusGenerator;

    @Mock
    ConfidentialDetailsGenerator confidentialDetailsGenerator;

    @Mock
    OrderAppliedForGenerator orderAppliedForGenerator;

    @Mock
    OtherProceedingsGenerator otherProceedingsGenerator;

    @Mock
    SpecialArrangementsGenerator specialArrangementsGenerator;

    @Mock
    UrgencyGenerator urgencyGenerator;

    @Mock
    AllegationOfHarmGenerator allegationOfHarmGenerator;

    @Mock
    DateOfSubmissionGenerator dateOfSubmissionGenerator;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    CaseClosedDateGenerator caseClosedDateGenerator;

    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final CaseSummary CASE_SUMMARY0 = mock(CaseSummary.class);
    private static final CaseSummary CASE_SUMMARY1 = mock(CaseSummary.class);
    private static final CaseSummary CASE_SUMMARY2 = mock(CaseSummary.class);
    private static final CaseSummary CASE_SUMMARY3 = mock(CaseSummary.class);
    private static final CaseSummary CASE_SUMMARY4 = mock(CaseSummary.class);
    private static final CaseSummary CASE_SUMMARY5 = mock(CaseSummary.class);
    private static final CaseSummary CASE_SUMMARY6 = mock(CaseSummary.class);
    private static final CaseSummary CASE_SUMMARY7 = mock(CaseSummary.class);
    private static final CaseSummary CASE_SUMMARY8 = mock(CaseSummary.class);

    private static final CaseSummary CASE_SUMMARY9 = mock(CaseSummary.class);

    private static final String[] EMPTY_ARRAY = {};

    @BeforeEach
    public void setUp() {
        when(allocatedJudgeDetailsGenerator.generate(CASE_DATA)).thenReturn(CASE_SUMMARY0);
        when(refugeCaseGenerator.generate(CASE_DATA)).thenReturn(CASE_SUMMARY1);
        when(caseStatusGenerator.generate(CASE_DATA)).thenReturn(CASE_SUMMARY2);
        when(confidentialDetailsGenerator.generate(CASE_DATA)).thenReturn(CASE_SUMMARY3);
        when(orderAppliedForGenerator.generate(CASE_DATA)).thenReturn(CASE_SUMMARY4);
        when(specialArrangementsGenerator.generate(CASE_DATA)).thenReturn(CASE_SUMMARY5);
        when(urgencyGenerator.generate(CASE_DATA)).thenReturn(CASE_SUMMARY6);
        when(allegationOfHarmGenerator.generate(CASE_DATA)).thenReturn(CASE_SUMMARY7);
        when(dateOfSubmissionGenerator.generate(CASE_DATA)).thenReturn(CASE_SUMMARY8);
        when(otherProceedingsGenerator.generate(CASE_DATA)).thenReturn(CASE_SUMMARY9);
        when(caseClosedDateGenerator.generate(CASE_DATA)).thenReturn(CASE_SUMMARY9);
        when(otherProceedingsGenerator.getOtherProceedingsDetails(CASE_DATA)).thenReturn(new ArrayList<>());

        when(objectMapper.convertValue(eq(CASE_SUMMARY0),
                                       Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field0", "value0"));
        when(objectMapper.convertValue(eq(CASE_SUMMARY1),
                                       Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("fieldR", "valueR"));
        when(objectMapper.convertValue(eq(CASE_SUMMARY2),
                                       Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field1", "value1"));
        when(objectMapper.convertValue(eq(CASE_SUMMARY3),
                                       Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field2", "value2"));
        when(objectMapper.convertValue(eq(CASE_SUMMARY4),
                                       Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field3", "value3"));
        when(objectMapper.convertValue(eq(CASE_SUMMARY5),
                                       Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field4", "value4"));
        when(objectMapper.convertValue(eq(CASE_SUMMARY6),
                                       Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field5", "value5"));
        when(objectMapper.convertValue(eq(CASE_SUMMARY7),
                                       Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field6", "value6"));
        when(objectMapper.convertValue(eq(CASE_SUMMARY8),
                                       Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field7", "value7"));
        when(objectMapper.convertValue(eq(CASE_SUMMARY9),
                                      Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field9", "value9"));

    }

    @Test
    public void testWhenAllGeneratedFieldsAreDisjointed() {

        final Map<String, Object> actual = caseSummaryTabService.updateTab(CASE_DATA);

        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("field0", "value0");
        fields.put("fieldR", "valueR");
        fields.put("field1", "value1");
        fields.put("field2", "value2");
        fields.put("field3", "value3");
        fields.put("field4", "value4");
        fields.put("field5", "value5");
        fields.put("field6", "value6");
        fields.put("field7", "value7");
        fields.put("field9", "value9");
        fields.put("otherProceedingEmptyTable", null);

        assertEquals(fields, actual);
    }

    @Test
    public void testWhenAllGeneratedFieldsWithNullValuesWillKeep() {

        Map<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("field0", "value0");
        objectObjectHashMap.put("fieldNull", null);

        when(objectMapper.convertValue(eq(CASE_SUMMARY0),
                                       Mockito.<TypeReference<Map<String, Object>>>any()))
            .thenReturn(objectObjectHashMap);


        Map<String, Object> expected = new HashMap<>();
        expected.put("field0", "value0");
        expected.put("fieldNull", null);
        expected.put("fieldR", "valueR");
        expected.put("field1", "value1");
        expected.put("field2", "value2");
        expected.put("field3", "value3");
        expected.put("field4", "value4");
        expected.put("field5", "value5");
        expected.put("field6", "value6");
        expected.put("field7", "value7");
        expected.put("field9", "value9");
        expected.put("otherProceedingEmptyTable", null);

        Map<String, Object> actual = caseSummaryTabService.updateTab(CASE_DATA);

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void testWhenAllGeneratedFieldsWithNullValuesWillOverrideWhenOtherNonNull() {

        Map<String, Object> map = new HashMap<>();
        map.put("field0", "value0");
        map.put("field1", null);
        when(otherProceedingsGenerator.getOtherProceedingsDetails(Mockito.any()))
            .thenReturn(List.of(Element.<OtherProceedings>builder().value(OtherProceedings.builder().build()).build()));
        when(objectMapper.convertValue(eq(CASE_SUMMARY0),
                                       Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(map);

        final Map<String, Object> actual = caseSummaryTabService.updateTab(CASE_DATA);

        Map<String, Object> expected = new HashMap<>();
        expected.put("field0", "value0");
        expected.put("fieldR", "valueR");
        expected.put("field1", "value1");
        expected.put("field2", "value2");
        expected.put("field3", "value3");
        expected.put("field4", "value4");
        expected.put("field5", "value5");
        expected.put("field6", "value6");
        expected.put("field7", "value7");
        expected.put("field9", "value9");
        expected.put("otherProceedingEmptyTable", null);
        expected.put("otherProceedingsForSummaryTab", List.of(Element.<OtherProceedings>builder()
                                                                  .value(OtherProceedings.builder().build()).build()));

        assertThat(actual).isEqualTo(expected);

    }
}
