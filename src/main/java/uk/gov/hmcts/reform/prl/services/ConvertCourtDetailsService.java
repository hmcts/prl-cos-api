package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.CourtDetailsPilotEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.logging.log4j.util.Strings.concat;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConvertCourtDetailsService {

    public Map<String, Object> verifyIfDynamicList(Map<String, Object> caseDataMap, String key) {
        Object submitCountyCourtSelection = caseDataMap.get(key);
        caseDataMap = EnumUtils.isValidEnum(CourtDetailsPilotEnum.class, submitCountyCourtSelection.toString())
            ? convertToDynamicList(caseDataMap, key) : caseDataMap;
        log.info("Court List ===> " + caseDataMap.get(key));
        return caseDataMap;
    }


    public Map<String, Object> convertToDynamicList(Map<String, Object> caseDataMap, String key) {

        log.info("Inside convertToDynamicList");
        List<DynamicListElement> courtDynamicListElements = new ArrayList<>();

        String courtNameKey = caseDataMap.get(key).toString();
        CourtDetailsPilotEnum courtDetailsPilotEnum = CourtDetailsPilotEnum.valueOf(courtNameKey);
        CourtVenue selectedCourtVenue = getCourtVenue(courtDetailsPilotEnum);
        DynamicListElement selectedDynamicListElement = getModifiedDisplayEntry(selectedCourtVenue);
        for (CourtDetailsPilotEnum courtDetailsPilot : CourtDetailsPilotEnum.values()) {
            CourtVenue courtVenue = getCourtVenue(courtDetailsPilot);
            DynamicListElement dynamicListElement = getModifiedDisplayEntry(courtVenue);
            courtDynamicListElements.add(dynamicListElement);
        }

        caseDataMap.put(key, DynamicList.builder()
            .value(selectedDynamicListElement)
            .listItems(courtDynamicListElements)
            .build());

        return caseDataMap;
    }

    private static CourtVenue getCourtVenue(CourtDetailsPilotEnum courtDetailsPilotEnum) {
        CourtVenue courtVenue = CourtVenue.builder().courtName(courtDetailsPilotEnum.getCourtName())
            .siteName(courtDetailsPilotEnum.getSiteName())
            .courtAddress(courtDetailsPilotEnum.getCourtAddress())
            .postcode(courtDetailsPilotEnum.getPostcode())
            .courtEpimmsId(courtDetailsPilotEnum.getCourtEpimmsId())
            .build();
        return courtVenue;
    }

    private DynamicListElement getModifiedDisplayEntry(CourtVenue location) {
        String value = concat(concat(concat(location.getSiteName(), " - "), concat(location.getCourtAddress(), " - ")),
                              location.getPostcode());
        String key = location.getCourtEpimmsId();
        return DynamicListElement.builder().code(key).label(value).build();
    }
}
