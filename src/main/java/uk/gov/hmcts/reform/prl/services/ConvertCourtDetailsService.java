package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.CourtDetailsPilotEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.logging.log4j.util.Strings.concat;

@Component
@RequiredArgsConstructor
public class ConvertCourtDetailsService {

    public Map<String, Object> verifyIfDynamicList(Map<String, Object> caseDataMap, String key) {
        Object submitCountyCourtSelection = caseDataMap.get(key);
        caseDataMap = submitCountyCourtSelection.toString().contains("list_items")
            ? caseDataMap : convertToDynamicList(
            caseDataMap,
            key
        );
        return caseDataMap;
    }


    private Map<String, Object> convertToDynamicList(Map<String, Object> caseDataMap, String key) {

        String courtNameKey = caseDataMap.get(key).toString();
        CourtDetailsPilotEnum courtDetailsPilotEnum = CourtDetailsPilotEnum.valueOf(courtNameKey);
        CourtVenue courtVenue = CourtVenue.builder().courtName(courtDetailsPilotEnum.getCourtName())
            .courtEpimmsId(courtDetailsPilotEnum.getCourtCode())
            .build();
        List<DynamicListElement> courtDynamicListElements = new ArrayList<>();
        DynamicListElement dynamicListElement = getModifiedDisplayEntry(courtVenue);
        courtDynamicListElements.add(dynamicListElement);

        caseDataMap.put(key, DynamicList.builder()
            .value(dynamicListElement)
            .listItems(courtDynamicListElements)
            .build());

        return caseDataMap;
    }

    private DynamicListElement getModifiedDisplayEntry(CourtVenue location) {
        String value = concat(concat(location.getCourtName(), " - "), location.getCourtEpimmsId());
        String key = location.getCourtEpimmsId();
        return DynamicListElement.builder().code(key).label(value).build();
    }
}
