package uk.gov.hmcts.reform.prl.mapper.welshlang;

import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingUrgentCheckListEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoTransferApplicationReasonEnum;
import uk.gov.hmcts.reform.prl.mapper.AppObjectMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WelshNeed;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WelshLangMapperTest {

    @Test
    public void shouldGetWelshValueForCA() throws Exception {
        WelshNeed welshNeed = WelshNeed.builder().whoNeedsWelsh("test").spokenOrWritten(Arrays.asList(
            SpokenOrWrittenWelshEnum.spoken,
            SpokenOrWrittenWelshEnum.both, SpokenOrWrittenWelshEnum.written)).build();
        Element<WelshNeed> wrappedChildren = Element.<WelshNeed>builder().value(welshNeed).build();
        List<Element<WelshNeed>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData data = CaseData.builder().ordersApplyingFor(Arrays.asList(OrderTypeEnum.childArrangementsOrder))
            .attendHearing(AttendHearing.builder()
                               .welshNeeds(listOfChildren)
                               .build())
            .standardDirectionOrder(StandardDirectionOrder.builder()
                                        .sdoHearingUrgentCheckList(List.of(SdoHearingUrgentCheckListEnum.immediateRisk))
                                        .sdoTransferApplicationReason(List.of(SdoTransferApplicationReasonEnum
                                                                                  .ongoingProceedings))
                                        .build())
            .build();

        // Get the Welsh Value of each object using Welsh Mapper
        Map<String, Object> caseDataMap  = AppObjectMapper.getObjectMapper()
            .convertValue(uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(data).build(),
                          Map.class);
        Map<String, Object> caseDataValues = (Map<String, Object>) caseDataMap.get("case_data");
        caseDataValues.forEach((k,v) -> {
            if (v != null) {
                Object updatedWelshObj = WelshLangMapper.applyWelshTranslation(k, v, true);
                caseDataValues.put(k, updatedWelshObj);
            }
        });

        Assert.assertEquals(caseDataValues.get("ordersApplyingFor"),
                            Arrays.asList(WelshLangMapper.CA_WELSH_MAP.get(OrderTypeEnum
                                                                               .childArrangementsOrder
                                                                               .getDisplayedValue())));
        Assert.assertEquals(caseDataValues.get("sdoHearingUrgentCheckList"),
                            Arrays.asList(WelshLangMapper.CA_WELSH_MAP.get(SdoHearingUrgentCheckListEnum
                                                                               .immediateRisk
                                                                               .getDisplayedValue())));
        Assert.assertEquals(caseDataValues.get("sdoTransferApplicationReason"),
                            Arrays.asList(WelshLangMapper.CA_WELSH_MAP.get(SdoTransferApplicationReasonEnum
                                                                               .ongoingProceedings
                                                                               .getDisplayedValue())));
    }


    @Test
    public void shouldGetWelshValueForDA() throws Exception {
        WelshNeed welshNeed = WelshNeed.builder().whoNeedsWelsh("test").spokenOrWritten(Arrays.asList(
            SpokenOrWrittenWelshEnum.spoken,
            SpokenOrWrittenWelshEnum.both, SpokenOrWrittenWelshEnum.written)).build();
        Element<WelshNeed> wrappedChildren = Element.<WelshNeed>builder().value(welshNeed).build();
        List<Element<WelshNeed>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData data = CaseData.builder().typeOfApplicationOrders(TypeOfApplicationOrders.builder()
                                                                       .orderType(Arrays.asList(
                                                                           FL401OrderTypeEnum.nonMolestationOrder))
                                                                       .build())
            .attendHearing(AttendHearing.builder()
                               .welshNeeds(listOfChildren)
                               .build()).build();

        // Get the Welsh Value of each object using Welsh Mapper
        Map<String, Object> caseDataMap  = AppObjectMapper.getObjectMapper()
            .convertValue(uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(data).build(),
                          Map.class);
        Map<String, Object> caseDataValues = (Map<String, Object>) caseDataMap.get("case_data");
        caseDataValues.forEach((k,v) -> {
            if (v != null) {
                Object updatedWelshObj = WelshLangMapper.applyWelshTranslation(k, v, false);
                caseDataValues.put(k, updatedWelshObj);
            }
        });

        Map<String, Object> map = new HashMap<>();
        map.put("orderType", Arrays.asList(WelshLangMapper.DA_WELSH_MAP.get(FL401OrderTypeEnum
                                                                                .nonMolestationOrder
                                                                                .getDisplayedValue())));
        Assert.assertEquals(caseDataValues.get("typeOfApplicationOrders"), map);
    }
}
