package uk.gov.hmcts.reform.prl.mapper.welshLang;

import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum;
import uk.gov.hmcts.reform.prl.mapper.AppObjectMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.WelshNeed;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WelshLangMapperTest {

    @Test
    public void shouldGetWelshValue() throws Exception {
        WelshNeed welshNeed = WelshNeed.builder().whoNeedsWelsh("test").spokenOrWritten(Arrays.asList(
            SpokenOrWrittenWelshEnum.spoken,
            SpokenOrWrittenWelshEnum.both, SpokenOrWrittenWelshEnum.written)).build();
        Element<WelshNeed> wrappedChildren = Element.<WelshNeed>builder().value(welshNeed).build();
        List<Element<WelshNeed>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData data = CaseData.builder().ordersApplyingFor(Arrays.asList(OrderTypeEnum.childArrangementsOrder))
            .welshNeeds(listOfChildren).build();

        // Get the Welsh Value of each object using Welsh Mapper
        Map<String, Object> caseDataMap  = AppObjectMapper.getObjectMapper()
            .convertValue(uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(data).build(),
                          Map.class);
        Map<String, Object> caseDataValues = (Map<String, Object>) caseDataMap.get("case_data");
        caseDataValues.forEach((k,v) -> {
            if(v != null) {
                Object updatedWelshObj = WelshLangMapper.applyWelshTranslation(k, v);
                caseDataValues.put(k, updatedWelshObj);
            }
        });

        Assert.assertEquals(caseDataValues.get("ordersApplyingFor"),
                            Arrays.asList(WelshLangMapper.CA_WELSH_MAP.get(OrderTypeEnum
                                                                               .childArrangementsOrder
                                                                               .getDisplayedValue())));
    }
}
