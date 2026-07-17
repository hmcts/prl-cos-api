package uk.gov.hmcts.reform.prl.models.sendandreply;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCruAccess;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class SendOrReplyMessage {

    //PRL-3454 - send & reply messages enhancements
    @CCD(
            label = "Messages",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class, CaseworkerWaTaskConfigurationCuAccess.class}
    )
    @JsonProperty("messages")
    private final List<Element<Message>> messages;

    @CCD(
            label = "Select a message to reply to",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class}
    )
    private DynamicList messageReplyDynamicList;
    @CCD(
            label = "Do you need to respond to this message?",
            hint = "If no response is required, the message will be marked as closed.",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class}
    )
    private YesOrNo respondToMessage;
    @CCD(
            label = "Message Table",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class}
    )
    private String messageReplyTable;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private Message sendMessageObject;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private Message replyMessageObject;
    @CCD(
            label = "Add document",
            hint = "Attach a document to the letter",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
    )
    @JsonProperty("externalMessageAttachDocsList")
    private List<Element<SendAndReplyDynamicDoc>> externalMessageAttachDocsList;

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    @JsonProperty("internalMessageAttachDocsList")
    private final List<Element<SendReplyTempDoc>> internalMessageAttachDocsList;

    public static String[] temporaryFieldsAboutToStart() {
        return new String[]{
            "messageContent", "respondToMessage",
            "messageMetaData", "messageReplyDynamicList", "sendMessageObject",
            "replyMessageObject", "messageReplyTable", "chooseSendOrReply",
            "internalMessageAttachDocsList"
        };
    }

    public static String[] temporaryFieldsAboutToSubmit() {
        return new String[]{
            "messageContent",
            "messageMetaData", "messageReplyDynamicList", "messageReplyTable","externalMessageAttachDocsList",
            "internalMessageAttachDocsList"
        };
    }
}

