package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.citizen.UploadedDocumentRequest;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseDocumentControllerFunctionalTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String GENERATE_UPLOAD_DOCUMENT_REQUEST = "requests/generate-document-request.json";
    public static final String DUMMY_UPLOAD_FILE = "classpath:requests/Dummy_pdf_file.pdf";


    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenGenerateDocumentForCitizen_return200() throws Exception {
        String requestBody = ResourceLoader.loadJson(GENERATE_UPLOAD_DOCUMENT_REQUEST);

        request
            .header("Authorization", "auth")
            .header(
                "serviceAuthorization",
                "test s2sToken"
            )
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/generate-citizen-statement-document")
            .then().assertThat().statusCode(200);

    }

    @Test
    public void givenGenerateDocumentForCitizen_return400() throws Exception {
        String requestBody = ResourceLoader.loadJson(GENERATE_UPLOAD_DOCUMENT_REQUEST);

        request
            .header("Authorization", "auth")
            .header(
                "serviceAuthorization", "test s2sToken")
            .when()
            .contentType("application/json")
            .post("/generate-citizen-statement-document")
            .then()
            .assertThat().statusCode(400);

    }

    @Test
    public void givenGenerateDocumentForCitizen_return404() throws Exception {
        String requestBody = ResourceLoader.loadJson(GENERATE_UPLOAD_DOCUMENT_REQUEST);

        request
            .header("Authorization", "auth")
            .header(
                "serviceAuthorization", "test s2sToken")
            .when()
            .contentType("application/json")
            .post("/generate-citizen-document")
            .then()
            .assertThat().statusCode(405);

    }

    @Test
    public void givenUploadStatementDocumentForCitizen_return200() throws Exception {

        final File fileToUpload = ResourceLoader.readFile(DUMMY_UPLOAD_FILE);

        List<MultipartFile> fileList = new ArrayList<>();
        fileList.add((MultipartFile) fileToUpload);

        UploadedDocumentRequest uploadedDocumentRequest = UploadedDocumentRequest.builder()
            .caseId("1667826894103746")
            .documentType("Your position statements")
            .documentRequestedByCourt(YesOrNo.Yes)
            .parentDocumentType("Witness statements and evidence")
            .files(fileList)
            .isApplicant("Yes")
            .partyName("")
            .partyId("65d93485-7605-438a-8cc3-fc701e80f5b3")
            .build();

        String requestBody = ResourceLoader.loadJson(String.valueOf(uploadedDocumentRequest));
        request
            .header("Authorization", "auth")
            .header(
                "serviceAuthorization",
                "test s2sToken"
            )
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/upload-citizen-statement-document")
            .then().assertThat().statusCode(200);

    }

    @Test
    public void givenDeleteDocumentForCitizen_return200() throws Exception {
        String requestBody = ResourceLoader.loadJson(GENERATE_UPLOAD_DOCUMENT_REQUEST);

        request
            .header("Authorization", "auth")
            .header(
                "serviceAuthorization",
                "test s2sToken"
            )
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/generate-citizen-statement-document")
            .then().assertThat().statusCode(200);

    }

    @Test
    public void givenUploadDocumentForCitizen_return200() throws Exception {
        final File fileToUpload = ResourceLoader.readFile(DUMMY_UPLOAD_FILE);

        request
            .header("Authorization", "auth")
            .header(
                "serviceAuthorization",
                "test s2sToken"
            )
            .multiPart("file",fileToUpload)
            .param("typeOfDocument", "C8")
            .pathParam("caseId","1667826894103746")
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .post("/upload-citizen-document")
            .then().assertThat().statusCode(200);

    }

    @Test
    public void givenDeleteDocumentForCitizenById_return200() throws Exception {
        final File fileToUpload = ResourceLoader.readFile(DUMMY_UPLOAD_FILE);

        request
            .header("Authorization", "auth")
            .header(
                "serviceAuthorization",
                "test s2sToken"
            )
            .pathParam("caseId","1667826894103746")
            .contentType("application/json")
            .post("/{caseId}/document")
            .then().assertThat().statusCode(200);

    }

}
