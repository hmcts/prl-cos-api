package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

public enum UploadAdditionalApplicationsFieldsEnum {

    temporaryOtherApplicationsBundle("temporaryOtherApplicationsBundle"),
    temporaryC2Document("temporaryC2Document"),
    additionalApplicantsList("additionalApplicantsList"),
    typeOfC2Application("typeOfC2Application"),
    additionalApplicationsHelpWithFeesNumber("additionalApplicationsHelpWithFeesNumber");

    private final String value;

    UploadAdditionalApplicationsFieldsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
