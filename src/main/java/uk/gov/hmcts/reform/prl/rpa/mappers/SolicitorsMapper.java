package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

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
        JsonArray solicitorJsonArray = null;
        if (applicantElementsCheck.isEmpty() && respondentElementsCheck.isEmpty()) {
            return null;
        }
        if (!applicantElementsCheck.isEmpty()) {
            List<PartyDetails> applicantList = caseData.getApplicants().stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            solicitorJsonArray = getSolicitorArray(applicantList);
        }

        if (!respondentElementsCheck.isEmpty()) {
            List<PartyDetails> respondentList = caseData.getRespondents().stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            solicitorJsonArray = getSolicitorArray(respondentList);
        }

        return solicitorJsonArray;
    }

    private JsonArray getSolicitorArray(List<PartyDetails> partyList) {
        return partyList.stream()
            .map(party -> {
                Organisations org = organisationService1.fetchOrgs(
                    "","",party.getSolicitorOrg() != null ? party.getSolicitorOrg().getOrganisationID() : null
                );
                return new NullAwareJsonObjectBuilder()
                    .add("name", party.getRepresentativeFirstName() + party.getRepresentativeLastName())
                    .add("address", getSolicitorAddress(party, org))
                    .add("contactDX", getDxNumber(party, org))
                    .add("contactEmailAddress", party.getSolicitorEmail())
                    .add("reference", party.getSolicitorReference())
                    .add("ID", CommonUtils.getSolicitorId(party))
                    .add("organisationID", org.getOrganisationIdentifier())
                    .add("organisationName", org.getName())
                    .build();
            }).collect(JsonCollectors.toJsonArray());
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
