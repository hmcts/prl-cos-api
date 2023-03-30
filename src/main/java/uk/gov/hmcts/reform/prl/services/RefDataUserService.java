package uk.gov.hmcts.reform.prl.services;



import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.CommonDataRefApi;
import uk.gov.hmcts.reform.prl.clients.JudicialUserDetailsApi;
import uk.gov.hmcts.reform.prl.clients.StaffResponseDetailsApi;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategorySubValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.concat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGALOFFICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFORDERASC;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFSORTCOLUMN;

@Slf4j
@Service
public class RefDataUserService {

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    StaffResponseDetailsApi staffResponseDetailsApi;

    @Autowired
    JudicialUserDetailsApi judicialUserDetailsApi;

    @Autowired
    IdamClient idamClient;

    @Autowired
    CommonDataRefApi commonDataRefApi;

    @Value("${prl.refdata.username}")
    private String refDataIdamUsername;

    @Value("${prl.refdata.password}")
    private String refDataIdamPassword;

    private  List<DynamicListElement> listOfCategoryValues;
    private CommonDataResponse commonDataResponse;

    public List<DynamicListElement> getLegalAdvisorList() {
        try {
            List<StaffResponse> listOfStaffResponse = getStaffResponse();
            if (listOfStaffResponse != null) {
                log.info(" size of staff details {}", listOfStaffResponse.size());
            }
            return onlyLegalAdvisor(listOfStaffResponse);
        } catch (Exception e) {
            log.error("Staff details Lookup Failed - {}", e.getMessage());
        }
        return List.of(DynamicListElement.builder().build());
    }

    public List<StaffResponse> getStaffResponse() {
        return staffResponseDetailsApi.getAllStaffResponseDetails(
            idamClient.getAccessToken(refDataIdamUsername, refDataIdamPassword),
            authTokenGenerator.generate(),
            SERVICENAME,
            STAFFSORTCOLUMN,
            STAFFORDERASC
        );
    }

    public List<JudicialUsersApiResponse> getAllJudicialUserDetails(JudicialUsersApiRequest judicialUsersApiRequest) {
        return judicialUserDetailsApi.getAllJudicialUserDetails(
            idamClient.getAccessToken(refDataIdamUsername, refDataIdamPassword),
            authTokenGenerator.generate(),
            judicialUsersApiRequest);
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
            log.error("Category Values look up failed {} ", e.getMessage());
        }
        return commonDataResponse;
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
                .map(CategoryValues::getChildNodes).collect(Collectors.toList()).stream()
                .flatMap(Collection::stream)
                .map(this::displaySubChannelEntry)
                .collect(Collectors.toList());
            Collections.sort(listOfSubCategoryValues, (a, b) -> a.getCode().compareToIgnoreCase(b.getCode()));
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



