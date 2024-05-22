package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.CommonDataRefApi;
import uk.gov.hmcts.reform.prl.clients.JudicialUserDetailsApi;
import uk.gov.hmcts.reform.prl.clients.StaffResponseDetailsApi;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.CaseFlag;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategorySubValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.concat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGALOFFICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RD_STAFF_FIRST_PAGE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RD_STAFF_PAGE_SIZE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RD_STAFF_SECOND_PAGE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RD_STAFF_TOTAL_RECORDS_HEADER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFORDERASC;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFSORTCOLUMN;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RefDataUserService {
    private final AuthTokenGenerator authTokenGenerator;
    private final StaffResponseDetailsApi staffResponseDetailsApi;
    private final JudicialUserDetailsApi judicialUserDetailsApi;
    private final IdamClient idamClient;
    private final CommonDataRefApi commonDataRefApi;
    private final LaunchDarklyClient launchDarklyClient;

    @Value("${prl.refdata.username}")
    private String refDataIdamUsername;
    @Value("${prl.refdata.password}")
    private String refDataIdamPassword;

    private List<DynamicListElement> listOfCategoryValues;
    private CommonDataResponse commonDataResponse;

    private CaseFlag caseFlag;

    public List<DynamicListElement> getLegalAdvisorList() {
        try {
            ResponseEntity<List<StaffResponse>> response = getStaffResponse(RD_STAFF_FIRST_PAGE);
            if (null != response) {
                Optional<String> totalRecordsStr = Optional.ofNullable(response.getHeaders().getFirst(
                    RD_STAFF_TOTAL_RECORDS_HEADER));
                int totalRecords = totalRecordsStr.map(Integer::parseInt).orElse(0);
                if (totalRecords > 0 && totalRecords < RD_STAFF_PAGE_SIZE) {
                    return onlyLegalAdvisor(response.getBody());
                } else {
                    List<DynamicListElement> listOfLegalAdvisors = onlyLegalAdvisor(response.getBody());
                    int noOfPages = (int) Math.ceil(totalRecords / (double) RD_STAFF_PAGE_SIZE);
                    for (int pageNumber = RD_STAFF_SECOND_PAGE; pageNumber < noOfPages; pageNumber++) {
                        listOfLegalAdvisors.addAll(onlyLegalAdvisor(getStaffResponse(pageNumber).getBody()));
                    }
                    return listOfLegalAdvisors;
                }

            }
        } catch (Exception e) {
            log.error("Staff details Lookup Failed - {}", e);
        }
        return List.of(DynamicListElement.builder().build());
    }

    public ResponseEntity<List<StaffResponse>> getStaffResponse(int pageNumber) {
        return staffResponseDetailsApi.getAllStaffResponseDetails(
            idamClient.getAccessToken(refDataIdamUsername, refDataIdamPassword),
            authTokenGenerator.generate(),
            SERVICENAME,
            STAFFSORTCOLUMN,
            STAFFORDERASC,
            RD_STAFF_PAGE_SIZE,
            pageNumber
        );
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

    private List<DynamicListElement> onlyLegalAdvisor(List<StaffResponse> listOfStaffResponse) {
        if (null != listOfStaffResponse) {
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
        try {
            commonDataResponse = commonDataRefApi.getAllCategoryValuesByCategoryId(
                authorization,
                authTokenGenerator.generate(),
                categoryId,
                SERVICE_ID,
                isHearingChildRequired
            );

        } catch (Exception e) {
            log.error("Category Values look up failed {} ", e);
        }
        return commonDataResponse;
    }


    public CaseFlag retrieveCaseFlags(String authorization, String flagType) {
        log.info("retrieve case flags for flag type{} ", flagType);
        try {
            caseFlag = commonDataRefApi.retrieveCaseFlagsByServiceId(
                authorization,
                authTokenGenerator.generate(),
                SERVICE_ID,
                flagType
            );
        } catch (Exception e) {
            log.error("Case flags Values look up failed {} ", e);
        }
        return caseFlag;
    }

    public List<DynamicListElement> filterCategoryValuesByCategoryId(CommonDataResponse commonDataResponse,String categoryId) {
        if (null != commonDataResponse) {
            listOfCategoryValues = commonDataResponse.getCategoryValues().stream()
                .filter(response -> response.getCategoryKey().equalsIgnoreCase(categoryId))
                .map(this::getDisplayCategoryEntry).collect(Collectors.toList());
            Collections.sort(listOfCategoryValues, (a, b) -> a.getCode().compareToIgnoreCase(b.getCode()));
            return listOfCategoryValues;
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
                .map(CategoryValues::getChildNodes).toList().stream()
                .flatMap(Collection::stream)
                .map(this::displaySubChannelEntry)
                .collect(Collectors.toList());
            Collections.sort(listOfSubCategoryValues, (a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));
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



