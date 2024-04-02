package uk.gov.hmcts.reform.prl.models.dto.ccd;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder(toBuilder = true)
public class CcdDocument {

    @JsonProperty("document_url")
    private String documentUrl;

    @JsonProperty("document_binary_url")
    private String documentBinaryUrl;

    @JsonProperty("document_filename")
    private String documentFileName;

    @JsonProperty("upload_timestamp")
    private Date uploadTimeStamp;

    @JsonCreator
    public CcdDocument(String documentUrl, String documentBinaryUrl, String documentFileName, Date uploadTimeStamp) {
        this.documentUrl = documentUrl;
        this.documentBinaryUrl = documentBinaryUrl;
        this.documentFileName = documentFileName;
        this.uploadTimeStamp = uploadTimeStamp;
    }
}
