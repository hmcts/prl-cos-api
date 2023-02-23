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
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.concat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGCHILDREQUIRED;
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

    public List<DynamicListElement> getLegalAdvisorList() {
        try {
            List<StaffResponse> listOfStaffResponse = getStaffResponse();
            if (listOfStaffResponse != null) {
                log.info(" size of staff details {}", listOfStaffResponse.size());
            }
            return onlyLegalAdvisor(listOfStaffResponse);
        } catch (Exception e) {
            log.error("Staff details Lookup Failed - " + e.getMessage(), e);
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

    public List<DynamicListElement> retrieveCategoryValues(String authorization, String categoryId) {
        log.info("retrieveCategoryValues {}", categoryId);
        try {
            CommonDataResponse commonDataResponse = commonDataRefApi.getAllCategoryValuesByCategoryId(
                authorization,
                authTokenGenerator.generate(),
                categoryId,
                SERVICE_ID,
                HEARINGCHILDREQUIRED
            );

            categoryValuesByCategoryId(commonDataResponse,categoryId);
            return listOfCategoryValues;
        } catch (Exception e) {
            log.error("Category Values look up failed - " + e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());
    }

    private List<DynamicListElement> categoryValuesByCategoryId(CommonDataResponse commonDataResponse,String categoryId) {
        log.info("categoryValuesByCategoryId {},{}", commonDataResponse,categoryId);
        if (null != commonDataResponse) {
            listOfCategoryValues = commonDataResponse.getListOfValues().stream()
                .filter(response -> response.getCategoryKey().equalsIgnoreCase(categoryId))
                .map(this::getDisplayCategoryEntry).collect(Collectors.toList());
            return listOfCategoryValues;
        }

        return List.of(DynamicListElement.builder().build());
    }


    private DynamicListElement getDisplayCategoryEntry(CategoryValues categoryValues) {
        String value = categoryValues.getValueEn();
        String key = categoryValues.getKey();
        return DynamicListElement.builder().code(key).label(value).build();
    }
}
