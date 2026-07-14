package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.CommonDataRefApi;
import uk.gov.hmcts.reform.prl.clients.JudicialUserDetailsApi;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.exception.NoStaffResponseException;
import uk.gov.hmcts.reform.prl.mapper.staffresponse.StaffResponseToDynamicListElementFilter;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.CaseFlag;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategorySubValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.concat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGALOFFICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_ID;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RefDataUserService {

    public static final String JUDICIAL_USER_CACHE = "judicialUserCache";
    public static final String STAFF_REF_DATA_CACHE = "staffRefDataCache";

    private final AuthTokenGenerator authTokenGenerator;
    private final StaffRefDataService staffRefDataService;
    private final JudicialUserDetailsApi judicialUserDetailsApi;
    private final IdamClient idamClient;
    private final CommonDataRefApi commonDataRefApi;
    private final LaunchDarklyClient launchDarklyClient;

    @Value("${prl.refdata.username}")
    private String refDataIdamUsername;
    @Value("${prl.refdata.password}")
    private String refDataIdamPassword;

    /**
     * Gets all staff filtered by the provided filter and returns them as a dynamic list.
     *
     * @param filter the filter to apply to the staff responses
     * @return a dynamic list of staff
     */
    public DynamicList getStaffDynamicList(StaffResponseToDynamicListElementFilter filter) {
        List<DynamicListElement> listItems = new ArrayList<>();
        try {
            List<StaffResponse> allStaff = staffRefDataService.getAllStaffDetails();
            listItems.addAll(applyStaffResponseFilter(allStaff, filter));
        } catch (Exception e) {
            log.error("Error retrieving staff list - {}", e.getMessage());
        }
        return DynamicList.builder()
            .listItems(listItems)
            .build();
    }

    private List<DynamicListElement> applyStaffResponseFilter(List<StaffResponse> listOfStaffResponse,
                                                              StaffResponseToDynamicListElementFilter filter) {
        return listOfStaffResponse.stream()
            .map(filter::filter)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }

    public List<DynamicListElement> getLegalAdvisorList() {
        try {
            List<StaffResponse> allStaff = staffRefDataService.getAllStaffDetails();
            return onlyLegalAdvisor(allStaff);
        } catch (NoStaffResponseException e) {
            log.error("Staff details Lookup Failed - {}", e.getMessage());
        }
        return List.of(DynamicListElement.builder().build());
    }

    public List<JudicialUsersApiResponse> getAllJudicialUserDetails(JudicialUsersApiRequest judicialUsersApiRequest) {
        if (launchDarklyClient.isFeatureEnabled("judicial-v2-change")) {
            log.info("Refdata Judicial API V2 called and LD flag is ON");
            return judicialUserDetailsApi.getAllJudicialUserDetailsV2(
                idamClient.getAccessToken(refDataIdamUsername, refDataIdamPassword),
                authTokenGenerator.generate(),
                judicialUsersApiRequest
            );
        }
        log.info("Refdata Judicial API V1 called and LD flag is OFF");
        return judicialUserDetailsApi.getAllJudicialUserDetails(
            idamClient.getAccessToken(refDataIdamUsername, refDataIdamPassword),
            authTokenGenerator.generate(),
            judicialUsersApiRequest
        );
    }

    /**
     * Gets judicial user details by IDAM ID (sidamId) with caching.
     * Cache is evicted every 30 minutes.
     *
     * @param sidamId The IDAM user ID
     * @return List of judicial user details, or empty list if not found
     */
    @Cacheable(cacheNames = JUDICIAL_USER_CACHE, key = "#sidamId")
    public List<JudicialUsersApiResponse> getJudicialUserBySidamId(String sidamId) {
        log.info("Fetching judicial user details for sidamId: {} (not cached)", sidamId);
        Map<String, Object> requestBody = Map.of("sidam_ids", new String[]{sidamId});
        List<JudicialUsersApiResponse> response = judicialUserDetailsApi.getJudicialUsersByRequestMap(
            idamClient.getAccessToken(refDataIdamUsername, refDataIdamPassword),
            authTokenGenerator.generate(),
            requestBody
        );
        if (response != null && !response.isEmpty()) {
            JudicialUsersApiResponse judge = response.getFirst();
            log.info("Judicial API response for sidamId {}: postNominals={}, appointments={}",
                sidamId, judge.getPostNominals(), judge.getAppointments());
        }
        return response;
    }

    @CacheEvict(allEntries = true, cacheNames = JUDICIAL_USER_CACHE)
    @Scheduled(fixedDelay = 1800000) // 30 minutes
    public void evictJudicialUserCache() {
        log.info("Evicting judicial user cache");
    }

    @CacheEvict(allEntries = true, cacheNames = STAFF_REF_DATA_CACHE)
    @Scheduled(fixedDelay = 1800000) // 30 minutes
    public void evictStaffRefDataCache() {
        log.info("Evicting staff ref data cache");
    }

    private List<DynamicListElement> onlyLegalAdvisor(List<StaffResponse> listOfStaffResponse) {
        if (null != listOfStaffResponse && !listOfStaffResponse.isEmpty()) {
            return listOfStaffResponse.stream()
                .filter(response -> response.getStaffProfile().getUserType().equalsIgnoreCase(LEGALOFFICE))
                .map(this::getDisplayEntry).collect(Collectors.toList());
        }
        return List.of(DynamicListElement.builder().build());
    }

    private DynamicListElement getDisplayEntry(StaffResponse staffResponse) {
        String value = concat(staffResponse.getStaffProfile().getLastName(),"(").concat(staffResponse.getStaffProfile().getEmailId()).concat(")");
        return DynamicListElement.builder().code(value).label(value).build();
    }

    public CommonDataResponse retrieveCategoryValues(String authorization, String categoryId,String isHearingChildRequired) {
        log.info("retrieveCategoryValues {}", categoryId);
        CommonDataResponse commonDataResponse = null;
        try {
            commonDataResponse = commonDataRefApi.getAllCategoryValuesByCategoryId(
                authorization,
                authTokenGenerator.generate(),
                categoryId,
                SERVICE_ID,
                isHearingChildRequired
            );

        } catch (FeignException e) {
            log.error("Category Values look up failed", e);
        }
        return commonDataResponse;
    }


    public CaseFlag retrieveCaseFlags(String authorization, String flagType) {
        log.info("retrieve case flags for flag type{} ", flagType);
        CaseFlag caseFlag = null;
        try {
            caseFlag = commonDataRefApi.retrieveCaseFlagsByServiceId(
                authorization,
                authTokenGenerator.generate(),
                SERVICE_ID,
                flagType
            );
        } catch (Exception e) {
            log.error("Case flags Values look up failed", e);
        }
        return caseFlag;
    }

    public List<DynamicListElement> filterCategoryValuesByCategoryId(CommonDataResponse commonDataResponse,String categoryId) {
        if (null != commonDataResponse) {
            return commonDataResponse.getCategoryValues().stream()
                .filter(response -> response.getCategoryKey().equalsIgnoreCase(categoryId))
                .map(this::getDisplayCategoryEntry)
                .sorted(Comparator.comparing(DynamicListElement::getCode, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        }

        return List.of(DynamicListElement.builder().build());
    }


    private DynamicListElement getDisplayCategoryEntry(CategoryValues categoryValues) {
        String value = categoryValues.getValueEn();
        String key = categoryValues.getKey();
        return DynamicListElement.builder().code(key).label(value).build();
    }

    public List<DynamicListElement> filterCategorySubValuesByCategoryId(CommonDataResponse commonDataResponse,String hearingPlatform) {
        log.info("categoryValuesByCategoryId {}", hearingPlatform);
        List<DynamicListElement> listOfSubCategoryValues;
        if (null != commonDataResponse && null != commonDataResponse.getCategoryValues()) {
            listOfSubCategoryValues = commonDataResponse.getCategoryValues().stream()
                .filter(categoryValues -> categoryValues.getChildNodes() != null && categoryValues.getValueEn().equalsIgnoreCase(hearingPlatform))
                .flatMap(categoryValues -> categoryValues.getChildNodes().stream())
                .map(this::displaySubChannelEntry)
                .sorted(Comparator.comparing(DynamicListElement::getLabel, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
            return listOfSubCategoryValues;
        }

        return List.of(DynamicListElement.builder().build());

    }

    private DynamicListElement displaySubChannelEntry(CategorySubValues categorySubValues) {
        String value = categorySubValues.getValueEn();
        String key = categorySubValues.getKey();
        return DynamicListElement.builder().code(key).label(value).build();
    }

}
