package uk.gov.hmcts.reform.prl.models.ordnancesurvey;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dpa {
    @JsonProperty("UPRN")
    private String uprn;

    @JsonProperty("UDPRN")
    private String udprn;

    @JsonProperty("ADDRESS")
    private String address;

    @JsonProperty("ORGANISATION_NAME")
    private String organisationName;
    @JsonProperty("DEPARTMENT_NAME")
    private String departmentName;
    @JsonProperty("SUB_BUILDING_NAME")
    private String subBuildingName;
    @JsonProperty("BUILDING_NAME")
    private String buildingName;
    @JsonProperty("BUILDING_NUMBER")
    private String buildingNumber; // sometimes number as string

    @JsonProperty("DEPENDENT_THOROUGHFARE_NAME")
    private String dependentThoroughfareName;
    @JsonProperty("THOROUGHFARE_NAME")
    private String thoroughfareName;
    @JsonProperty("DOUBLE_DEPENDENT_LOCALITY")
    private String doubleDependentLocality;
    @JsonProperty("DEPENDENT_LOCALITY")
    private String dependentLocality;

    @JsonProperty("POST_TOWN")
    private String postTown;
    @JsonProperty("POSTCODE")
    private String postcode;

    @JsonProperty("RPC")
    private String rpc;

    @JsonProperty("X_COORDINATE")
    private Double xcoordinate;
    @JsonProperty("Y_COORDINATE")
    private Double ycoordinate;
    @JsonProperty("LNG")
    private Double longitude;
    @JsonProperty("LAT")
    private Double latitude;

    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("LOGICAL_STATUS_CODE")
    private Integer logicalStatusCode;
    @JsonProperty("CLASSIFICATION_CODE")
    private String classificationCode;
    @JsonProperty("CLASSIFICATION_CODE_DESCRIPTION")
    private String classificationCodeDescription;

    @JsonProperty("LOCAL_CUSTODIAN_CODE")
    private String localCustodianCode;
    @JsonProperty("LOCAL_CUSTODIAN_CODE_DESCRIPTION")
    private String localCustodianCodeDescription;

    @JsonProperty("COUNTRY_CODE")
    private String countryCode;
    @JsonProperty("COUNTRY_CODE_DESCRIPTION")
    private String countryCodeDescription;

    @JsonProperty("POSTAL_ADDRESS_CODE")
    private String postalAddressCode;
    @JsonProperty("POSTAL_ADDRESS_CODE_DESCRIPTION")
    private String postalAddressCodeDescription;

    @JsonProperty("BLPU_STATE_CODE")
    private Integer blpuStateCode;
    @JsonProperty("BLPU_STATE_CODE_DESCRIPTION")
    private String blpuStateCodeDescription;

    @JsonProperty("TOPOGRAPHY_LAYER_TOID")
    private String toponymyLayerToid;

    @JsonProperty("WARD_CODE")
    private String wardCode;
    @JsonProperty("PARISH_CODE")
    private String parishCode;

    @JsonProperty("PARENT_UPRN")
    private String parentUprn;

    @JsonProperty("LAST_UPDATE_DATE")
    private String lastUpdateDate;
    @JsonProperty("ENTRY_DATE")
    private String entryDate;
    @JsonProperty("LEGAL_NAME")
    private String legalName;
    @JsonProperty("BLPU_STATE_DATE")
    private String blpuStateDate;

    @JsonProperty("LANGUAGE")
    private String language;

    @JsonProperty("MATCH")
    private Double match;
    @JsonProperty("MATCH_DESCRIPTION")
    private String matchDescription;

    @JsonProperty("DELIVERY_POINT_SUFFIX")
    private String deliveryPointSuffix;
}
