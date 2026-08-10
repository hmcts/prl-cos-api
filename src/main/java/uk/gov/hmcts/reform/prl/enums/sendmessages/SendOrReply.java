package uk.gov.hmcts.reform.prl.enums.sendmessages;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "sendOrReply", generate = true)
public enum SendOrReply {
    @CCD(label = "Send a message")
    SEND,
    @CCD(label = "Reply to a message")
    REPLY
}
