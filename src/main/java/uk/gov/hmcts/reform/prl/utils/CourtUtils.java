package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import uk.gov.hmcts.reform.prl.enums.CourtDetailsPilotEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.logging.log4j.util.Strings.concat;

@Slf4j
public class CourtUtils {

    public static final String SUBMIT_COUNTY_COURT_SELECTION = "submitCountyCourtSelection";
    public static final String COURT_LIST = "courtList";

    public static Map<String, Object> checkCourtIsDynamicList(Map<String, Object> caseDataMap) {
        if (caseDataMap.containsKey(SUBMIT_COUNTY_COURT_SELECTION)) {
            caseDataMap = verifyCourtDetails(caseDataMap, SUBMIT_COUNTY_COURT_SELECTION);
        }
        if (caseDataMap.containsKey(COURT_LIST)) {
            caseDataMap = verifyCourtDetails(caseDataMap, COURT_LIST);
        }
        return caseDataMap;
    }

    private static Map<String, Object> verifyCourtDetails(Map<String, Object> caseDataMap, String key) {
        Object submitCountyCourtSelection = caseDataMap.get(key);
        caseDataMap = EnumUtils.isValidEnum(CourtDetailsPilotEnum.class, submitCountyCourtSelection.toString())
            ? convertToDynamicList(caseDataMap, key) : caseDataMap;
        log.info(key + " ===> " + caseDataMap.get(key));
        return caseDataMap;
    }


    private static Map<String, Object> convertToDynamicList(Map<String, Object> caseDataMap, String key) {

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

    private static DynamicListElement getModifiedDisplayEntry(CourtVenue location) {
        String value = concat(
            concat(concat(location.getSiteName(), " - "), concat(location.getCourtAddress(), " - ")),
            location.getPostcode()
        );
        String key = location.getCourtEpimmsId();
        return DynamicListElement.builder().code(key).label(value).build();
    }
}
