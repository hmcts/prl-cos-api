package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DisabilityRequirementEnum {

    documentsHelp("I need documents in an alternative format"),
    communicationHelp("I need help communicating and understanding"),
    extraSupport("I need to bring support with me to a hearing"),
    feelComfortableSupport("I need something to feel comfortable during a hearing"),
    helpTravellingMovingBuildingSupport("I need help travelling to, or moving around court buildings"),

    specifiedColorDocuments("Documents in a specified colour"),
    easyReadFormatDocuments("Documents in Easy Read format"),
    brailleDocuments("Braille documents"),
    largePrintDocuments("Documents in large print"),
    audioTranslationDocuments("Audio translation of documents"),
    readOutDocuments("Documents read out to me"),
    emailInformation("Information emailed to me"),
    documentHelpOther("Other"),
    noSupportRequired("No, I do not need any support at this time"),

    hearingLoop("Hearing loop (hearing enhancement system)"),
    infraredReceiver("Infrared receiver (hearing enhancement system)"),
    needToBeClosedWithSpeaker("Need to be close to who is speaking"),
    lipSpeaker("Lip speaker"),
    signLanguageInterpreter("Sign Language interpreter"),
    speechToTextReporter("Speech to text reporter (palantypist)"),
    needExtraTime("Extra time to think and explain myself"),
    visitCourtBeforeHearing("Visit to court before the hearing"),
    explanationOfCourt("Explanation of the court and who's in the room at the hearing"),
    intermediary("Intermediary"),
    communicationHelpOther("Other"),

    supportWorkerCarer("A support worker or carer"),
    friendFamilyMember("A friend or family member"),
    assistanceGuideDog("Assistance / guide dog"),
    therapyAnimal("Therapy animal"),
    supportCourtOther("Other"),
    supportCourtNoOption("No, I do not need any support at this time"),

    appropriateLighting("Appropriate lighting"),
    regularBreaks("Regular breaks"),
    spaceUpAndMoveAround("Space to be able to get up and move around"),
    feelComportableOther("Other"),
    feelComportableNoOption("No, I do not need any support at this time"),

    parkingSpace("Parking space close to the venue"),
    wheelchairAccess("Step free / wheelchair access"),
    venueWheelchair("Use of venue wheelchair"),
    accessToilet("Accessible toilet"),
    helpUsingLift("Help using a lift"),
    differentTypeChair("A different type of chair"),
    guideBuilding("Guiding in the building"),
    travellingCourtOther("Other"),
    travellingCourtNoOption("No, I do not need any support at this time");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DisabilityRequirementEnum getValue(String key) {
        return DisabilityRequirementEnum.valueOf(key);
    }
}
