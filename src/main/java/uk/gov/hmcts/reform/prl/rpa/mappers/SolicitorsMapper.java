package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Applicant;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.services.OrganisationService1;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.stream.JsonCollectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SolicitorsMapper {

    private final AddressMapper addressMapper;

    private final OrganisationService1 organisationService1;

    private JsonObject mapSolicitorAddress(ContactInformation contactInformation) {
        if (contactInformation != null) {
            return new NullAwareJsonObjectBuilder()
                .add("County", contactInformation.getCounty())
                .add("Country", contactInformation.getCountry())
                .add("PostCode", contactInformation.getPostCode())
                .add("PostTown", contactInformation.getTownCity())
                .add("AddressLine1", contactInformation.getAddressLine1())
                .add("AddressLine2", contactInformation.getAddressLine2())
                .add("AddressLine3", contactInformation.getAddressLine3())
                .build();
        }
        return null;
    }

    public JsonArray mapSolicitorList(CaseData caseData) {
        Optional<List<Element<PartyDetails>>> applicantElementsCheck = ofNullable(caseData.getApplicants());
        Optional<List<Element<PartyDetails>>> respondentElementsCheck = ofNullable(caseData.getRespondents());
        List<PartyDetails> applicantList = new ArrayList<>();
        List<PartyDetails> respondentList = new ArrayList<>();
        JsonArray solicitorJsonArray = null;
        if (applicantElementsCheck.isEmpty() && respondentElementsCheck.isEmpty()) {
            return null;
        }
        if (!applicantElementsCheck.isEmpty()) {
            applicantList = caseData.getApplicants().stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
        }
        if (!respondentElementsCheck.isEmpty()) {
            respondentList = caseData.getRespondents().stream()
                .map(Element::getValue)
                .filter(respondent->respondent.getDoTheyHaveLegalRepresentation().equals(YesNoDontKnow.yes))
                .collect(Collectors.toList());
        }
        applicantList.addAll(respondentList);
        solicitorJsonArray = getSolicitorArray(applicantList);
        return solicitorJsonArray;
    }

    private JsonArray getSolicitorArray(List<PartyDetails> partyList) {
        AtomicInteger counter = new AtomicInteger(1);
        return partyList.stream()
            .map(party -> callOrgSearchFormSolicitorObject(counter, party)
            ).collect(JsonCollectors.toJsonArray());
    }

    private JsonObject callOrgSearchFormSolicitorObject(AtomicInteger counter, PartyDetails party) {
        try{
            if(ObjectUtils.isEmpty(party.getSolicitorOrg()) &&  ObjectUtils.isEmpty(party.getSolicitorOrg().getOrganisationID())){
                return returnWithoutOrgSearch(party,counter);
            }
            Organisations org = organisationService1.fetchOrgs(
                "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiMWVyMFdSd2dJT1RBRm9qRTRyQy9mYmVLdTNJPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJwcmwtc3lzdGVtLXVwZGF0ZUBtYWlsaW5hdG9yLmNvbSIsImN0cyI6Ik9BVVRIMl9TVEFURUxFU1NfR1JBTlQiLCJhdXRoX2xldmVsIjowLCJhdWRpdFRyYWNraW5nSWQiOiI1NGNiNjA3Ni0wOTgyLTQ0NTUtODMwOC1hNjNlZTVhNzVmZmYtNTE3NzQzNjYzIiwiaXNzIjoiaHR0cHM6Ly9mb3JnZXJvY2stYW0uc2VydmljZS5jb3JlLWNvbXB1dGUtaWRhbS1hYXQyLmludGVybmFsOjg0NDMvb3BlbmFtL29hdXRoMi9yZWFsbXMvcm9vdC9yZWFsbXMvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiJ1WGRkUzZLT2JLUDNfNDRCZUFjOWR3dVQwcFkiLCJub25jZSI6IkdKdUFNVFctSmhta2RWeFVTSEI5T3l5Z1VZakVrSFRockwxQ3hxMjBvV0UiLCJhdWQiOiJ4dWl3ZWJhcHAiLCJuYmYiOjE2NDU1MzY4MTksImdyYW50X3R5cGUiOiJhdXRob3JpemF0aW9uX2NvZGUiLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiLCJjcmVhdGUtdXNlciIsIm1hbmFnZS11c2VyIiwic2VhcmNoLXVzZXIiXSwiYXV0aF90aW1lIjoxNjQ1NTM2ODE5LCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTY0NTU2NTYxOSwiaWF0IjoxNjQ1NTM2ODE5LCJleHBpcmVzX2luIjoyODgwMCwianRpIjoiVGRZbFNzRnFoWkZ5OFZpblZmQUx4UVMyZkVRIn0.ZldXfZ40JYivg_zol6ShOzryedVCI7T0Q5XAEDzajO_i7Xfn2C7scGUyLU_JJYFkUwxiBvXB_xfSGdKBy45SusWXlm7wu16ZrjM7SKRmUOQCL2USPMnz-vPzSgrQoeyXbUZoZ_Y8_gLMm8TrfKO_P9t-gyNzCG2kWJk-0ZjpMUCPUThXCfEeACvw0_4NKvMwcYk4fNIVd4CI4jSlmAaLU73KRP_unSN6PmJBIodLo39DQytDAtWIbs0v4I0Dp_LID87en1SY3vMBXMZE_YPWxIqWgA8q5zccAOZRYVKXyJI_AdvtNMgVswpsnGYpu0JSxNeEP_nAvbTezhU8K6aNrQ","eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJwcmxfY29zX2FwaSIsImV4cCI6MTY0NTU1MTE3Nn0.RJRx2gL2YiSUwLcLCf8FE5V14kZ-mkVKsgWenCK_8PaPX9oF701VG37cm8hUkauRQW9VmMNLQaSF9dcvJ9O8NA"
                , party.getSolicitorOrg().getOrganisationID()
            );
            return new NullAwareJsonObjectBuilder()
                .add("name", party.getRepresentativeFirstName() + party.getRepresentativeLastName())
                .add("address", getSolicitorAddress(party, org))
                .add("contactDX", getDxNumber(party, org))
                .add("contactEmailAddress", party.getSolicitorEmail())
                .add("reference", party.getSolicitorReference())
                .add("ID", "SOL_"+counter.getAndIncrement())
                .add("organisationID", org.getOrganisationIdentifier())
                .add("organisationName", org.getName())
                .build();
        }
        catch(Exception ex){
            log.error("Error searching for the organisation for the org {}", party.getSolicitorOrg());
            return returnWithoutOrgSearch(party,counter);
        }
    }

    private JsonObject returnWithoutOrgSearch(PartyDetails party,AtomicInteger counter) {
        return new NullAwareJsonObjectBuilder()
            .add("name", party.getRepresentativeFirstName() + party.getRepresentativeLastName())
            .add("address", getSolicitorAddress(party, null))
            .add("contactDX", getDxNumber(party, null))
            .add("contactEmailAddress", party.getSolicitorEmail())
            .add("reference", party.getSolicitorReference())
            .add("ID", "SOL_"+counter.getAndIncrement())
            .add("organisationID", "null")
            .add("organisationName", "null")
            .build();
    }

    private JsonObject getSolicitorAddress(PartyDetails party, Organisations org) {
        if (party.getSolicitorAddress() != null) {
            return addressMapper.mapAddress(party.getSolicitorAddress());
        } else if (!ObjectUtils.isEmpty(org) && org.getContactInformation() != null && !org.getContactInformation().isEmpty()) {
            return mapSolicitorAddress(org.getContactInformation().get(0));
        } else {
            return null;
        }
    }

    private String getDxNumber(PartyDetails applicant, Organisations org) {
        if (applicant.getDxNumber() != null) {
            return applicant.getDxNumber();
        } else if (ObjectUtils.isEmpty(org) || ObjectUtils.isEmpty(org.getContactInformation()) || ObjectUtils.isEmpty(
            org.getContactInformation().get(0))) {// todo
            return null;
        } else if (!org.getContactInformation().get(0).getDxAddress().isEmpty()) {
            return org.getContactInformation().get(0).getDxAddress().get(
                0).getDxNumber() != null ? org.getContactInformation().get(0).getDxAddress().get(
                0).getDxNumber() : null;
        } else {
            return null;
        }
    }

}
