package uk.gov.hmcts.reform.prl.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;

@Slf4j
@Service
public class HearingPrePopulateService {

    @Autowired
    RefDataUserService refDataUserService;

    public List<DynamicListElement> prePopulateHearingType(String authorisation) {
        log.info("Prepopulate HearingType call in HearingPrePopulateService");
        return refDataUserService.retrieveCategoryValues(authorisation, HEARINGTYPE);
    }

    public List<Element<HearingData>> mapHearingData(List<Element<HearingData>> hearingDatas, DynamicList hearingTypesDynamicList) {
        hearingDatas.stream().parallel().forEach(hearingDataElement -> {
            hearingDataElement.getValue().getHearingTypes().setListItems(null != hearingTypesDynamicList
                                                                             ? hearingTypesDynamicList.getListItems() : null);
        });
        return hearingDatas;
    }


}
