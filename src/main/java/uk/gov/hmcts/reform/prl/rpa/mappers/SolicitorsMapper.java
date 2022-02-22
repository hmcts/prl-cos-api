package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.services.OrganisationService1;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.util.Map;
import java.util.Optional;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonCollectors;

import static java.util.Optional.ofNullable;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SolicitorsMapper {

    private final AddressMapper addressMapper;

    private final OrganisationService1 organisationService1;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUserService systemUserService;

    public JsonObject mapSolicitorAddress(ContactInformation contactInformation) {
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

    public JsonArray mapSolicitorList(Map<String, PartyDetails> solicitorMap) {
        return solicitorMap.entrySet().stream()
            .map(party -> callOrgSearchFormSolicitorMap(party.getKey(), party.getValue()))
            .collect(JsonCollectors.toJsonArray());
    }

    public JsonObject callOrgSearchFormSolicitorMap(String id, PartyDetails party) {
        log.info("Fetching the organisation details for org {}", party.getSolicitorOrg());
        if (party.getSolicitorOrg() != null && party.getSolicitorOrg().getOrganisationID() != null) {
            Organisations org = organisationService1.fetchOrgs(
                systemUserService.getSysUserToken(),
                authTokenGenerator.generate(),
                party.getSolicitorOrg().getOrganisationID()
            );

            return new NullAwareJsonObjectBuilder()
                .add(
                    "name",
                    getSolicitorFullName(party.getRepresentativeFirstName(), party.getRepresentativeLastName())
                )
                .add("address", getSolicitorAddress(party, org))
                .add("contactDX", getDxNumber(party, org))
                .add("contactEmailAddress", party.getSolicitorEmail())
                .add("reference", party.getSolicitorReference())
                .add("ID", id)
                .add("organisationID", org.getOrganisationIdentifier())
                .add("organisationName", org.getName())
                .build();
        }
        return new NullAwareJsonObjectBuilder()
            .add("name", getSolicitorFullName(
                party.getRepresentativeFirstName(),
                party.getRepresentativeLastName()
            ))
            .add("address", getSolicitorAddress(party, null))
            .add("contactDX", getDxNumber(party, null))
            .add("contactEmailAddress", party.getSolicitorEmail())
            .add("reference", party.getSolicitorReference())
            .add("ID", "null")
            .add("organisationID", "null")
            .add("organisationName", "null")
            .build();
    }

    public String getSolicitorFullName(String representativeFirstName, String representativeLastName) {
        if (representativeFirstName != null && representativeLastName != null) {
            return representativeFirstName + representativeLastName;
        } else if (representativeFirstName == null) {
            return representativeLastName != null ? representativeLastName : null;
        } else if (representativeLastName == null) {
            return representativeFirstName != null ? representativeFirstName : null;
        }
        return null;
    }

    public JsonObject getSolicitorAddress(PartyDetails party, Organisations org) {
        Optional<Address> solicitorAddress = ofNullable(party.getSolicitorAddress());
        if (solicitorAddress.isPresent() && !ofNullable(solicitorAddress.get().getAddressLine1()).isEmpty()) {
            return addressMapper.mapAddress(party.getSolicitorAddress());
        } else if (!ObjectUtils.isEmpty(org) && org.getContactInformation() != null
            && !org.getContactInformation().isEmpty()) {
            return mapSolicitorAddress(org.getContactInformation().get(0));
        } else {
            return null;
        }
    }

    public String getDxNumber(PartyDetails applicant, Organisations org) {
        if (applicant.getDxNumber() != null) {
            return applicant.getDxNumber();
        } else if (ObjectUtils.isEmpty(org) || ObjectUtils.isEmpty(org.getContactInformation())
            || ObjectUtils.isEmpty(org.getContactInformation().get(0))) {
            return null;
        } else if (!org.getContactInformation().get(0).getDxAddress().isEmpty()) {
            return org.getContactInformation().get(0).getDxAddress()
                .get(0).getDxNumber() != null ? org.getContactInformation().get(0).getDxAddress()
                .get(0).getDxNumber() : null;
        } else {
            return null;
        }
    }

}
