package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "organisationIdentifier",
    "users"
})
@Generated("jsonschema2pojo")
public class OrgSolicitors {

    @JsonProperty("organisationIdentifier")
    private String organisationIdentifier;
    @JsonProperty("users")
    private List<User> users;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("organisationIdentifier")
    public String getOrganisationIdentifier() {
        return organisationIdentifier;
    }

    @JsonProperty("organisationIdentifier")
    public void setOrganisationIdentifier(String organisationIdentifier) {
        this.organisationIdentifier = organisationIdentifier;
    }

    @JsonProperty("users")
    public List<User> getUsers() {
        return users;
    }

    @JsonProperty("users")
    public void setUsers(List<User> users) {
        this.users = users;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
