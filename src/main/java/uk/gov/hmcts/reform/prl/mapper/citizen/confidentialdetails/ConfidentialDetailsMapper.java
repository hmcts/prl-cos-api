package uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pitest.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfidentialDetailsMapper {

    @Autowired
    private ObjectMapper objectMapper;

    private final AllTabServiceImpl allTabsService;

    public CaseData mapConfidentialData(CaseData caseData, boolean updateTabs) {
        List<Element<ApplicantConfidentialityDetails>> respondentsConfidentialDetails = new ArrayList<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            Optional<List<Element<PartyDetails>>> respondentsList = ofNullable(caseData.getRespondents());
            if (respondentsList.isPresent()) {
                List<PartyDetails> respondents = caseData.getRespondents()
                    .stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                respondentsConfidentialDetails = getRespondentConfidentialDetails(respondents);
            }

            caseData = caseData.toBuilder()
                .respondentConfidentialDetails(respondentsConfidentialDetails)
                .build();

        } else {
            if (null != caseData.getRespondentsFL401()) {
                List<PartyDetails> fl401Respondent = List.of(caseData.getRespondentsFL401());
                respondentsConfidentialDetails = getRespondentConfidentialDetails(fl401Respondent);
            }

            caseData = caseData.toBuilder()
                .respondentConfidentialDetails(respondentsConfidentialDetails)
                .build();
        }
        if (updateTabs) {
            allTabsService.updateAllTabsIncludingConfTab(caseData);
        }
        return caseData;
    }

    private List<Element<ApplicantConfidentialityDetails>> getRespondentConfidentialDetails(List<PartyDetails> currentRespondents) {
        List<Element<ApplicantConfidentialityDetails>> tempConfidentialApplicants = new ArrayList<>();
        for (PartyDetails respondent : currentRespondents) {
            boolean addressSet = false;
            boolean emailSet = false;
            boolean phoneSet = false;
            if ((YesOrNo.Yes).equals(respondent.getIsAddressConfidential())) {
                addressSet = true;
            }
            if ((YesOrNo.Yes).equals(respondent.getIsEmailAddressConfidential())) {
                emailSet = true;
            }
            if ((YesOrNo.Yes).equals(respondent.getIsPhoneNumberConfidential())) {
                phoneSet = true;
            }
            if (addressSet || emailSet || phoneSet) {
                tempConfidentialApplicants
                    .add(getRespondentConfidentialityElement(addressSet, emailSet, phoneSet, respondent));
            }
        }
        return tempConfidentialApplicants;
    }

    private Element<ApplicantConfidentialityDetails> getRespondentConfidentialityElement(boolean addressSet,
                                                                                         boolean emailSet,
                                                                                         boolean phoneSet,
                                                                                         PartyDetails respondent) {

        Address address = addressSet ? respondent.getAddress() : null;
        String phoneNumber = phoneSet ? respondent.getPhoneNumber() : null;
        String email = emailSet ? respondent.getEmail() : null;

        if (null != respondent.getResponse()
            && null != respondent.getResponse().getCitizenDetails()) {
            CitizenDetails citizenDetails = respondent.getResponse().getCitizenDetails();
            if (null != citizenDetails.getAddress()
                && null != citizenDetails.getAddress().getPostCode()
                && addressSet) {
                address = citizenDetails.getAddress();
            }
            if (null != citizenDetails.getContact()
                && null != citizenDetails.getContact().getPhoneNumber()) {
                if (!StringUtil.isNullOrEmpty(citizenDetails.getContact().getPhoneNumber())
                    && phoneSet) {
                    phoneNumber = citizenDetails.getContact().getPhoneNumber();
                }
                if (!StringUtil.isNullOrEmpty(citizenDetails.getContact().getEmail())
                    && emailSet) {
                    email = citizenDetails.getContact().getEmail();
                }
            }
        }

        return Element
            .<ApplicantConfidentialityDetails>builder()
            .value(ApplicantConfidentialityDetails.builder()
                       .firstName(respondent.getFirstName())
                       .lastName(respondent.getLastName())
                       .address(address)
                       .phoneNumber(phoneNumber)
                       .email(email)
                       .build()).build();
    }
}
