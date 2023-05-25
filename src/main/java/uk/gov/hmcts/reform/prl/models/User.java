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
    "userIdentifier",
    "firstName",
    "lastName",
    "email",
    "idamStatus",
    "roles",
    "idamStatusCode",
    "idamMessage"
})
@Generated("jsonschema2pojo")
public class User {

    @JsonProperty("userIdentifier")
    private String userIdentifier;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("email")
    private String email;
    @JsonProperty("idamStatus")
    private String idamStatus;
    @JsonProperty("roles")
    private List<String> roles;
    @JsonProperty("idamStatusCode")
    private String idamStatusCode;
    @JsonProperty("idamMessage")
    private String idamMessage;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("userIdentifier")
    public String getUserIdentifier() {
        return userIdentifier;
    }

    @JsonProperty("userIdentifier")
    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    @JsonProperty("firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    @JsonProperty("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("idamStatus")
    public String getIdamStatus() {
        return idamStatus;
    }

    @JsonProperty("idamStatus")
    public void setIdamStatus(String idamStatus) {
        this.idamStatus = idamStatus;
    }

    @JsonProperty("roles")
    public List<String> getRoles() {
        return roles;
    }

    @JsonProperty("roles")
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @JsonProperty("idamStatusCode")
    public String getIdamStatusCode() {
        return idamStatusCode;
    }

    @JsonProperty("idamStatusCode")
    public void setIdamStatusCode(String idamStatusCode) {
        this.idamStatusCode = idamStatusCode;
    }

    @JsonProperty("idamMessage")
    public String getIdamMessage() {
        return idamMessage;
    }

    @JsonProperty("idamMessage")
    public void setIdamMessage(String idamMessage) {
        this.idamMessage = idamMessage;
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
