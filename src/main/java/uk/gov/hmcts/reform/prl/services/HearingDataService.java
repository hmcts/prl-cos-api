package uk.gov.hmcts.reform.prl.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGCHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_N;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_Y;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;

@Slf4j
@Service
public class HearingDataService {

    @Autowired
    RefDataUserService refDataUserService;

    @Autowired
    HearingService hearingService;

    public List<DynamicListElement> prePopulateHearingType(String authorisation) {
        try {
            log.info("Prepopulate HearingType call in HearingDataService");
            CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
                authorisation,
                HEARINGTYPE,
                IS_HEARINGCHILDREQUIRED_N
            );
            return refDataUserService.categoryValuesByCategoryId(commonDataResponse, HEARINGTYPE);
        } catch (Exception e) {
            log.error("Category Values look up failed - " + e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());
    }

    public List<DynamicListElement> prePopulateHearingChannel(String authorisation) {
        try {
            log.info("Prepopulate HearingChannel call in HearingDataService");
            CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
                authorisation,
                HEARINGCHANNEL,
                IS_HEARINGCHILDREQUIRED_N
            );
            return refDataUserService.categoryValuesByCategoryId(commonDataResponse, HEARINGCHANNEL);
        } catch (Exception e) {
            log.error("Category Values look up failed - " + e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());

    }

    public List<Element<HearingData>> mapHearingData(List<Element<HearingData>> hearingDatas, DynamicList hearingTypesDynamicList,
                                                     DynamicList hearingDatesDynamicList) {
        hearingDatas.stream().parallel().forEach(hearingDataElement -> {
            HearingData hearingData = hearingDataElement.getValue();
            hearingData.getHearingTypes().setListItems(null != hearingTypesDynamicList
                                                                             ? hearingTypesDynamicList.getListItems() : null);
            hearingData.getConfirmedHearingDates().setListItems(null != hearingDatesDynamicList
                                                                    ? hearingDatesDynamicList.getListItems() : null);
        
        });
        return hearingDatas;
    }

    public   List<DynamicListElement> getHearingStartDate(String authorization,CaseData caseData) {
        try {
            String caseReferenceNumber =  String.valueOf(caseData.getId());
            Hearings hearingDetails = hearingService.getHearings(authorization, caseReferenceNumber);
            log.info("Hearing Details from hmc for the case id:{}",caseReferenceNumber);
            if (null != hearingDetails && null != hearingDetails.getCaseHearings()) {
                return hearingDetails.getCaseHearings().stream()
                    .filter(caseHearing -> LISTED.equalsIgnoreCase(caseHearing.getHmcStatus()))
                    .map(CaseHearing::getHearingDaySchedule).collect(Collectors.toList()).stream()
                    .flatMap(Collection::stream)
                    .map(this::displayEntry)
                    .collect(Collectors.toList());

            }
        } catch (Exception e) {
            log.error("List of Hearing Start Date Values look up failed - " + e.getMessage(), e);
        }
        //TODO: need to ensure this hardcoded values has to be removed while merging into release branch. Its added to test in preview/aat environment
        return List.of(DynamicListElement.builder().code(String.valueOf(LocalDateTime.now())).label(String.valueOf(LocalDateTime.now())).build());
    }

    private DynamicListElement displayEntry(HearingDaySchedule hearingDaySchedule) {
        LocalDateTime hearingStartDateTime = hearingDaySchedule.getHearingStartDateTime();
        return DynamicListElement.builder().code(String.valueOf(hearingStartDateTime)).label(String.valueOf(hearingStartDateTime)).build();
    }

    public  List<DynamicListElement> getHearingSubChannels(String authorization) {
        try {
            log.info("Prepopulate HearingSubChannel in HearingDataService");
            CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
                authorization,
                HEARINGCHANNEL,
                IS_HEARINGCHILDREQUIRED_Y
            );
            refDataUserService.categorySubValuesByCategoryId(
                commonDataResponse,
                HEARINGCHANNEL
            );
        } catch (Exception e) {
            log.error("Category Values look up failed - " + e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());

    }
}
