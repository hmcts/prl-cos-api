package uk.gov.hmcts.reform.prl.mapper.welshMap;

import uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.ProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class WelshLangMapper {

    public static final Map<String,String> CA_WELSH_MAP = getCaWelshLangMap();
    public static final List<String> CA_WELSH_CONDITONAL_MAP = getCaConditionalFieldWelshLangMap();

    public static Object applyWelshTranslation(Object key , Object obj) {
        if(obj instanceof String) {
            if(!CA_WELSH_CONDITONAL_MAP.contains(obj)) {
                if(key != null && CA_WELSH_MAP.containsKey(key + "_" + obj)) {
                    obj = CA_WELSH_MAP.get(key + "_" + obj);
                } else if(CA_WELSH_MAP.containsKey(obj)) {
                    obj = CA_WELSH_MAP.get(obj);
                }
            }
        } else if(obj instanceof List) {
            List<Object> list = (List)obj;
            for(int i=0;i <list.size(); i++) {
                Object eachObj = list.get(i);
                list.set(i, applyWelshTranslation(null, eachObj));
            }
        } else if(obj instanceof Map) {
            Map<String, Object> innerMap = (Map<String, Object>)obj;
            innerMap.forEach((k,v) -> {
                if(v!= null) {
                    innerMap.put(k,applyWelshTranslation(k,v));
                }
            });
        }
        return obj;
    }

    // Getting conditional fields from document...
    private static List<String> getCaConditionalFieldWelshLangMap() {
        return Arrays.asList("isAtAddressLessThan5Years",
                             "typeOfChildArrangementsOrder",
                             "applicationPermissionRequired",
                             "canYouProvideEmailAddress",
                             "childrenKnownToLocalAuthority",
                             "isDateOfBirthKnown",
                             "isPlaceOfBirthKnown",
                             "isCurrentAddressKnown",
                             "isAtAddressLessThan5YearsWithDontKnow",
                             "canYouProvideEmailAddress",
                             "canYouProvidePhoneNumber",
                             "doTheyHaveLegalRepresentation",
                             "ordersNonMolestation",
                             "ordersOccupation",
                             "ordersForcedMarriageProtection",
                             "ordersRestraining",
                             "ordersOtherInjunctive",
                             "ordersUndertakingInPlace",
                             "childrenKnownToLocalAuthority",
                             "childrenSubjectOfChildProtectionPlan",
                             "typeOfChildArrangementsOrder");
    }

    // Getting conditional fields and Rendering from document...
    private static List<String> getCaConditionalAndRenderingFieldWelshLangMap() {
        return Arrays.asList("isAddressConfidential",
                             "isCaseUrgent",
                             "doYouNeedAWithoutNoticeHearing",
                             "doYouRequireAHearingWithReducedNotice",
                             "gender",
                             "isEmailAddressConfidential",
                             "isPhoneNumberConfidential",
                             "isPersonIdentityConfidential",
                             "familyMediatorMiam");
    }

    // Getting CA realted values map..
    private static Map<String,String> getCaWelshLangMap() {

        Map<String, String> welshMap = new WeakHashMap<>();

        /**
         * Common Utils - Yes,No,Information is to be kept confidential,Gender,Don't know.
         */
        welshMap.put(YesOrNo.Yes.toString(), "Ydy");
        welshMap.put(YesOrNo.No.toString(),"Nac ydy");
        welshMap.put("confidential_mask","Rhaid cadw’r wybodaeth hon yn gyfrinachol");
        welshMap.put(DontKnow.dontKnow.getDisplayedValue(), "Ddim yn gwybod");
        welshMap.put(Gender.female.getDisplayedValue(), "Benyw");     //will need as a condition
        welshMap.put(Gender.male.getDisplayedValue(), "Gwryw");
        welshMap.put(Gender.other.getDisplayedValue(), "Maent yn uniaethu mewn rhyw ffordd arall");

        /**
         *  Type of Application - What order(s) are you applying for?.
         */
        welshMap.put(OrderTypeEnum.childArrangementsOrder.getDisplayedValue(), "Gorchymyn Trefniadau Plant");
        welshMap.put(OrderTypeEnum.prohibitedStepsOrder.getDisplayedValue(), "Gorchymyn Camau Gwaharddedig");
        welshMap.put(OrderTypeEnum.specificIssueOrder.getDisplayedValue(), "Gorchymyn Materion Penodol");

        /**
         *  Type of Application - Type of child arrangements order.
         */
        welshMap.put(ChildArrangementOrderTypeEnum.spendTimeWithOrder.getDisplayedValue(), "Gorchymyn Treulio Amser Gyda");
        welshMap.put(ChildArrangementOrderTypeEnum.liveWithOrder.getDisplayedValue(), "Gorchymyn Byw Gyda");
        welshMap.put(ChildArrangementOrderTypeEnum.bothLiveWithAndSpendTimeWithOrder.getDisplayedValue(),
                     "Gorchymyn Byw Gyda a Threulio Amser Gyda");

        /**
         * Type of Application - Have you applied to the court for permission to make this application?.
         */
        welshMap.put("applicationPermissionRequired_Yes","Do");
        welshMap.put("applicationPermissionRequired_No","Naddo, nid oes rhaid cael caniatâd");

        /**
         * Hearing urgency - Do you need a without notice hearing?.
         */
        welshMap.put("doYouNeedAWithoutNoticeHearing_Yes","Ydw");
        welshMap.put("doYouNeedAWithoutNoticeHearing_No","Nac ydw");


        /**
         * Hearing urgency - Are respondents aware of proceedings?.
         */
        welshMap.put("awareOfProceeding_Yes", "Ydyn");
        welshMap.put("awareOfProceeding_No", "Nac ydyn");


        /**
         * Applicant -Details - Do you need to keep the address confidential?.
         */
        welshMap.put("isAddressConfidential_Yes","Ydw");
        welshMap.put("isAddressConfidential_No","Nac ydw");

        /**
         * Applicant -Details - Can you provide email address?.
         */
        welshMap.put("canYouProvideEmailAddress_Yes","Gallaf");
        welshMap.put("canYouProvideEmailAddress_No","Na allaf");

        /**
         * Applicant -Details - Do you need to keep the contact number confidential?.
         */
        welshMap.put("isPhoneNumberConfidential_Yes","Ydw");
        welshMap.put("isPhoneNumberConfidential_No","Nac ydw");

        /**
         * Child Details - What is the applicant's relationship to child? (applicantsRelationshipToChild) & What is the respondent’s relationship to Child 1? (respondentsRelationshipToChild).
         */
        welshMap.put(RelationshipsEnum.father.getDisplayedValue(),"Tad");
        welshMap.put(RelationshipsEnum.mother.getDisplayedValue(),"Mam");
        welshMap.put(RelationshipsEnum.stepFather.getDisplayedValue(),"Llystad");
        welshMap.put(RelationshipsEnum.stepMother.getDisplayedValue(),"Llysfam");
        welshMap.put(RelationshipsEnum.grandParent.getDisplayedValue(),"Nain/Taid");
        welshMap.put(RelationshipsEnum.guardian.getDisplayedValue(),"Gwarcheidwad");
        welshMap.put(RelationshipsEnum.specialGuardian.getDisplayedValue(),"Gwarcheidwad Arbennig");
        welshMap.put(RelationshipsEnum.other.getDisplayedValue(),"Arall");

        /**
         * Child Details - Who does the child live with? (childLiveWith).
         */
        welshMap.put(LiveWithEnum.applicant.getDisplayedValue(), "Ceisydd");
        welshMap.put(LiveWithEnum.respondent.getDisplayedValue(), "Atebydd");
        welshMap.put(LiveWithEnum.anotherPerson.getDisplayedValue(), "Unigolyn arall nad yw wedi’i restru");

        /**
         * Child Details - Do you need to keep the identity of the person that the child lives with confidential? (isPersonIdentityConfidential).
         */
        welshMap.put("isPersonIdentityConfidential_Yes","Ydw");
        welshMap.put("isPersonIdentityConfidential_No","Nac ydw");

        /**
         * Respondent - Can you provide email address?.
         */
        welshMap.put("canYouProvideEmailAddress_Yes","Gallaf");
        welshMap.put("canYouProvideEmailAddress_No","Na allaf");

        /**
         * Respondent - Can you provide a contact number?.
         */
        welshMap.put("canYouProvidePhoneNumber_Yes","Gallaf");
        welshMap.put("canYouProvidePhoneNumber_No","Na allaf");

        /**
         * Respondent - Do they have legal representation?.
         */
        welshMap.put("doTheyHaveLegalRepresentation_Yes","Nac oes");
        welshMap.put("doTheyHaveLegalRepresentation_No","Oes");

        /**
         * MIAM.
         */
        welshMap.put(MiamExemptionsChecklistEnum.domesticViolence.getDisplayedValue(), "Trais domestig");
        welshMap.put(MiamExemptionsChecklistEnum.childProtectionConcern.getDisplayedValue(), "Pryderon amddiffyn plant");
        welshMap.put(MiamExemptionsChecklistEnum.urgency.getDisplayedValue(), "Cais brys");
        welshMap.put(MiamExemptionsChecklistEnum.previousMIAMattendance.getDisplayedValue(), "Eisoes wedi mynychu MIAM neu esemptiad MIAM blaenorol");
        welshMap.put(MiamExemptionsChecklistEnum.other.getDisplayedValue(), "Arall");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_22.getDisplayedValue(),
                     "Tystiolaeth sy’n dangos bod darpar barti wedi bod,"
                         + " neu mewn risg o fod, yn ddioddefwr trais domestig gan ddarpar barti "
                         + "arall ar ffurf camdriniaeth mewn perthynas â materion ariannol.");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_21.getDisplayedValue(),
                     "Llythyr gan Ysgrifennydd Gwladol y Swyddfa Gartref yn cadarnhau "
                         + "bod darpar barti wedi cael caniatâd i aros yn y Deyrnas Unedig"
                         + " o dan baragraff 289B Rheolau’r Ysgrifennydd Gwladol a wnaed dan Adran 3(2) "
                         + "Deddf Mewnfudo 1971, a gellir eu gweld yn "
                         + "https://www. gov.uk/guidance/immigration-rules/immigration-rules-index;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_20.getDisplayedValue(),
                     "Llythyr gan awdurdod cyhoeddus yn cadarnhau bod unigolyn sydd, "
                         + "neu a oedd mewn perthynas teuluol â darpar barti, wedi’i asesu fel ei fod yn dioddef,"
                         + " neu mewn risg o ddioddef trais domestig gan y darpar barti hwnnw (neu gopi o’r asesiad hwnnw);");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_19.getDisplayedValue(),
                     "Llythyr neu adroddiad gan sefydliad sy’n darparu gwasanaethau cefnogi trais domestig yn y Deyrnas"
                         + " Unedig yn cadarnhau- (i) bod cais am loches i unigolyn sydd, neu a oedd mewn perthynas teuluol â darpar barti, "
                         + "wedi cael ei wrthod; (ii) y dyddiad y cafodd y cais am loches ei wrthod; a’u "
                         + "(iii) bod wedi gwneud cais am loches oherwydd honiadau o drais domestig gan y darpar barti a gyfeiriwyd atynt ym mharagraff (i);");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_18.getDisplayedValue(),
                     "Llythyr- (i) gan sefydliad sy’n darparu gwasanaethau cefnogi trais domestig, "
                         + "neu elusen gofrestredig, a bod y llythyr yn cadarnhau bod y gwasanaeth- (a) wedi’i leoli yng Nghymru a Lloegr, "
                         + "(b) wedi bod yn gweithredu am gyfnod di-dor o chwe mis neu hirach; a’i "
                         + "(c) fod wedi darparu cefnogaeth i ddarpar barti mewn perthynas ag anghenion yr unigolyn hwnnw fel dioddefwr,"
                         + " neu unigolyn sydd mewn risg o drais domestig; "
                         + "ac (ii) mae’n cynnwys- (a) datganiad sy’n nodi, ym marn broffesiynol resymol awdur y llythyr,"
                         + " bod y darpar barti yn, neu mewn risg o fod yn ddioddefwr trais domestig; "
                         + "(b) disgrifiad o’r materion penodol y gellir dibynnu arnynt i gefnogi’r farn honno; "
                         + "(c) disgrifiad o’r gefnogaeth a roddwyd i’r darpar barti; "
                         + "a (d) datganiad o’r rhesymau pam bod y darpar barti angen y gefnogaeth honno;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_17.getDisplayedValue(),
                     "Llythyr gan swyddog a gyflogir gan awdurdod lleol neu gymdeithas tai (neu sefydliad cyfwerth yn Yr Alban neu yng Ngogledd Iwerddon) "
                         + "yn cefnogi tenantiaid sy’n cynnwys- "
                         + "(i) datganiad sy’n nodi, yn eu barn broffesiynol resymol nhw, bod unigolyn y mae darpar barti yn,"
                         + " neu wedi bod mewn perthynas deuluol ag o/â hi yn, neu mewn risg o ddioddef trais domestig gan y darpar barti hwnnw; "
                         + "(ii) disgrifiad o’r materion penodol y gellir dibynnu arnynt i gefnogi’r farn honno; a "
                         + "(iii) disgrifiad o’r gefnogaeth a roddwyd ganddynt i’r dioddefwr trais domestig neu’r "
                         + "unigolyn sydd mewn risg o drais domestig gan y darpar barti hwnnw;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_16.getDisplayedValue(),
                     "Llythyr gan ymgynghorydd trais rhywiol annibynnol yn cadarnhau eu bod yn "
                         + "darparu cefnogaeth i ddarpar barti mewn perthynas â thrais rhywiol gan ddarpar barti arall;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_15.getDisplayedValue(),
                     "Llythyr gan ymgynghorydd trais domestig annibynnol yn cadarnhau eu bod yn darparu cefnogaeth i ddarpar barti;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_13.getDisplayedValue(),
                     "Llythyr gan unrhyw unigolyn sy’n aelod o gynhadledd asesu risg amlasiantaeth (neu fforwm diogelu lleol addas arall) "
                         + "yn cadarnhau bod darpar barti, neu unigolyn y mae y darpar barti mewn perthynas deuluol ag o/â hi, "
                         + "yn neu wedi bod mewn risg o ddioddef trais domestig gan ddarpar barti arall;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_12.getDisplayedValue(),
                     "Llythyr neu adroddiad gan- (i) y gweithiwr iechyd proffesiynol priodol a wnaeth yr atgyfeiriad a ddisgrifir isod; "
                         + "(ii) gweithiwr iechyd proffesiynol priodol sydd â mynediad at gofnodion meddygol y darpar barti y cyfeiriwyd ato isod; "
                         + "neu (iii) yr unigolyn y gwnaethpwyd yr atgyfeiriad a ddisgrifir isod iddo; "
                         + "yn cadarnhau bod darpar barti wedi cael ei gyfeirio gan weithiwr iechyd proffesiynol priodol at unigolyn sy’n darparu "
                         + "cefnogaeth neu gymorth arbenigol i ddioddefwyr, neu’r rhai hynny sydd mewn risg o ddioddef trais domestig;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_10.getDisplayedValue(),
                     "Adroddiad arbenigwr a gyflwynwyd fel tystiolaeth mewn achos yn y Deyrnas Unedig er mwyn i lys neu dribiwnlys allu cadarnhau "
                         + "bod unigolyn sydd, neu a oedd mewn perthynas teuluol â darpar barti, wedi’i asesu fel bod, "
                         + "neu mewn risg o fod, yn ddioddefwr trais domestig gan y darpar barti hwnnw;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_9.getDisplayedValue(),
                     "Copi o ganfyddiad ffeithiol, a wnaed mewn achos yn y Deyrnas Unedig, bod trais domestig wedi’i gyflawni gan ddarpar barti;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_8.getDisplayedValue(),
                     "Ymgymeriad a roddwyd yng Nghymru a Lloegr dan adran 46 neu 63E Deddf Cyfraith Teulu 1996 (neu a roddwyd yn Yr Alban neu yng "
                         + "Ngogledd Iwerddon yn lle gwaharddeb gwarchod) gan ddarpar barti, ar yr amod na roddwyd traws-ymgymeriad mewn perthynas "
                         + "â thrais domestig gan ddarpar barti arall;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_7.getDisplayedValue(), "Gwaharddeb gwarchod perthnasol;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_6.getDisplayedValue(),
                     "Hysbysiad diogelu rhag trais domestig a roddwyd dan adran 24 Deddf Troseddau a Diogelwch 2010 yn erbyn darpar barti;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_5.getDisplayedValue(),
                     "Gorchymyn llys yn rhwymo darpar barti mewn perthynas â throsedd trais domestig;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_4.getDisplayedValue(),
                     "Tystiolaeth o euogfarn berthnasol am drosedd trais domestig;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_3.getDisplayedValue(),
                     "Tystiolaeth o achos troseddol perthnasol am drosedd trais domestig sydd heb ddod i ben;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_2.getDisplayedValue(),
                     "Tystiolaeth o rybuddiad heddlu perthnasol am drosedd trais domestig;");
        welshMap.put(MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_1.getDisplayedValue(),
                     "Tystiolaeth bod darpar barti wedi cael ei arestio am drosedd trais domestig perthnasol;");
        welshMap.put(MiamChildProtectionConcernChecklistEnum.MIAMChildProtectionConcernChecklistEnum_value_1.getDisplayedValue(),
                     "Mae awdurdod lleol yn cynnal ymholiadau dan Adran 47 Deddf Plant 1989");
        welshMap.put(MiamChildProtectionConcernChecklistEnum.MIAMChildProtectionConcernChecklistEnum_value_2.getDisplayedValue(),
                     "Mae awdurdod lleol wedi rhoi cynllun amddiffyn plant mewn lle");
        welshMap.put(MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_5.getDisplayedValue(),
                     "Mae yna risg sylweddol yn y cyfnod a gymerir i drefnu a mynychu MIAM, "
                         + "y bydd achos mewn perthynas â’r anghydfod yn cael ei ddwyn gerbron llys mewn gwlad arall, "
                         + "lle y gall hawliad dilys i awdurdodaeth fodoli, megis y gall llys yn y wlad arall honno wrando "
                         + "ar yr anghydfod cyn llys yng Nghymru a Lloegr");
        welshMap.put(MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_4.getDisplayedValue(),
                     "Byddai unrhyw oedi a achosir drwy fynychu MIAM yn peri problemau anadferadwy wrth ddelio â’r anghydfod "
                         + "(gan gynnwys colled anadferadwy o dystiolaeth sylweddol)");
        welshMap.put(MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_3.getDisplayedValue(),
                     "Byddai unrhyw oedi a achosir drwy fynychu MIAM yn peri caledi afresymol i’r ceisydd arfaethedig");
        welshMap.put(MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_2.getDisplayedValue(),
                     "Byddai unrhyw oedi a achosir drwy fynychu MIAM yn peri risg sylweddol o gamwedd cyfiawnder");
        welshMap.put(MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_1.getDisplayedValue(),
                     "Mae yna risg i fywyd, rhyddid neu ddiogelwch corfforol y ceisydd arfaethedig neu ei deulu/theulu neu ei gartref/chartref; neu");
        welshMap.put(MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_6.getDisplayedValue(),
                     "Mae yna risg y byddai plentyn yn cael ei gludo o’r Deyrnas Unedig yn anghyfreithlon, "
                         + "neu risg y byddai plentyn sydd y tu allan i Gymru a Lloegr ar hyn o bryd yn cael ei gadw’n anghyfreithlon");
        welshMap.put(MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_7.getDisplayedValue(), "Mae yna risg o niwed i blentyn");
        welshMap.put(MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_5.getDisplayedValue(),
                     "Byddai’r cais yn cael ei wneud ynghylch achos presennol sy’n parhau ac roedd esemptiad MIAM yn berthnasol pan wnaed y cais ynghylch yr achos hwnnw");
        welshMap.put(MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_4.getDisplayedValue(),
                     "Byddai’r cais yn cael ei wneud ynghylch achos presennol sy’n parhau ac mi wnaeth y darpar geisydd fynychu MIAM cyn cychwyn yr achos hwnnw");
        welshMap.put(MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_3.getDisplayedValue(),
                     "Yn y 4 mis cyn gwneud y cais, bod yr unigolyn wedi ffeilio cais perthnasol i’r llys teulu yn cadarnhau bod esemptiad MIAM yn berthnasol"
                         + " a bod y cais yn ymwneud â’r un anghydfod neu yr un anghydfod i raddau helaeth");
        welshMap.put(MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_2.getDisplayedValue(),
                     "Adeg gwneud y cais, mae’r unigolyn yn cymryd rhan mewn math arall o broses i ddatrys anghydfod y tu allan i’r llys yn ymwneud â’r un "
                         + "anghydfod neu yr un anghydfod i raddau helaeth");
        welshMap.put(MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_1.getDisplayedValue(),
                     "Yn y 4 mis cyn gwneud y cais, bod yr unigolyn wedi mynychu MIAM neu wedi cymryd rhan mewn math arall o broses i ddatrys anghydfod y "
                         + "tu allan i’r llys yn ymwneud â’r un anghydfod neu yr un anghydfod i raddau helaeth");
        welshMap.put(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_11.getDisplayedValue(),
                     "Nid oes gan yr un cyfryngwr teuluol awdurdodedig swyddfa o fewn pymtheg milltir i gartref y darpar geisydd.");
        welshMap.put(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_10.getDisplayedValue(),
                     "(i) mae’r darpar geisydd wedi cysylltu â’r holl gyfryngwyr teuluol awdurdodedig sydd â swyddfa o fewn pymtheg milltir "
                         + "i’w gartref neu ei chartref (neu dri ohonynt os oes tri neu fwy), ac mae pob un ohonynt wedi dweud na allant gynnal MIAM "
                         + "o fewn pymtheg diwrnod busnes; a (ii) gellir darparu enwau, cyfeiriadau a rhifau ffôn neu gyfeiriadau e-bost y cyfryngwyr "
                         + "teuluol awdurdodedig hynny a’r dyddiadau cysylltu i’r llys ar gais.");
        welshMap.put(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_9.getDisplayedValue(),
                     "Mae un o’r darpar bartïon yn blentyn yn rhinwedd Rheol 12.3(1)");
        welshMap.put(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_8.getDisplayedValue(),
                     "Nid yw’r darpar geisydd neu’r holl ddarpar atebwyr yn preswylio’n arferol yng Nghymru neu Lloegr.");
        welshMap.put(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_7.getDisplayedValue(),
                     "ni all y darpar geisydd neu’r holl ddarpar atebwyr fynychu MIAM oherwydd y mae ef neu hi, "
                         + "neu y mae nhw (i) yn y carchar neu mewn unrhyw sefydliad arall ac y mae’n rhaid ei g/chadw neu eu cadw yno; "
                         + "(ii) yn destun amodau mechnïaeth sy’n eu hatal rhag cysylltu â’r unigolyn arall; "
                         + "neu (iii) yn destun trwydded gyda gofyniad i beidio â chysylltu â’r unigolyn arall.");
        welshMap.put(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_6.getDisplayedValue(),
                     "(i) mae gan y darpar geisydd neu’r holl ddarpar atebwyr anabledd neu analluogrwydd a fyddai’n golygu na all neu "
                         + "na allant fynychu MIAM oni bai bod y cyfryngwr awdurdodedig yn gallu cynnig cyfleusterau addas; "
                         + "mae’r darpar geisydd wedi cysylltu â’r holl gyfryngwyr teuluol awdurdodedig sydd â swyddfa o fewn pymtheg "
                         + "milltir i’w gartref neu ei chartref (neu dri ohonynt os oes tri neu fwy), "
                         + "ac mae pob un ohonynt wedi dweud na allant ddarparu cyfleusterau o’r fath; "
                         + "a (iii)gellir darparu enwau, cyfeiriadau a rhifau ffôn neu gyfeiriadau e-bost y cyfryngwyr teuluol "
                         + "awdurdodedig hynny a’r dyddiadau cysylltu i’r llys ar gais.");
        welshMap.put(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_5.getDisplayedValue(),
                     "Byddai’r cais yn cael ei wneud heb hysbysiad (Mae paragraff 5.1 o Gyfarwyddyd Ymarfer 18A yn nodi’r "
                         + "amgylchiadau pan ellir gwneud cais heb hysbysiad.)");
        welshMap.put(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_4.getDisplayedValue(),
                     "Nid oes gan y darpar geisydd fanylion cyswllt digonol ar gyfer unrhyw un o’r darpar atebwyr "
                         + "i alluogi cyfryngwr teuluol gysylltu ag unrhyw un o’r darpar atebwyr i drefnu’r MIAM.");
        welshMap.put(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_3.getDisplayedValue(),
                     "Mae’r ceisydd yn fethdalwr ac mae ganddo/i orchymyn methdalu ar gyfer y darpar geisydd.");
        welshMap.put(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_2.getDisplayedValue(),
                     "Mae’r ceisydd yn fethdalwr ac mae ganddo/i ddeiseb gan gredydwr y darpar geisydd am orchymyn methdalu");
        welshMap.put(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_1.getDisplayedValue(),
                     "Mae’r ceisydd yn fethdalwr ac mae ganddo/i gais gan y darpar geisydd am orchymyn methdalu;");

        /**
         * Allegations of harm.
         */
        welshMap.put("allegationsOfHarmYesNo_Yes","Oes");
        welshMap.put("allegationsOfHarmYesNo_No","Nac oes");
        welshMap.put("allegationsOfHarmDomesticAbuseYesNo_Yes","Oes");
        welshMap.put("allegationsOfHarmDomesticAbuseYesNo_No","Nac oes");
        welshMap.put(ApplicantOrChildren.applicants.getDisplayedValue(), "Ceisydd/Ceiswyr");
        welshMap.put(ApplicantOrChildren.children.getDisplayedValue(), "Plentyn/Plant");
        welshMap.put(AbductionChildPassportPossessionEnum.MOTHER.getDisplayedValue(), "Mam");
        welshMap.put(AbductionChildPassportPossessionEnum.FATHER.getDisplayedValue(), "Tad");
        welshMap.put(AbductionChildPassportPossessionEnum.OTHER.getDisplayedValue(), "Arall");
        welshMap.put("allegationsOfHarmChildAbductionYesNo_Yes","Oes");
        welshMap.put("allegationsOfHarmChildAbductionYesNo_No","Nac oes");
        welshMap.put("allegationsOfHarmChildAbuseYesNo_Yes","Oes");
        welshMap.put("allegationsOfHarmChildAbuseYesNo_No","Nac oes");
        welshMap.put("allegationsOfHarmSubstanceAbuseYesNo_Yes","Oes");
        welshMap.put("allegationsOfHarmSubstanceAbuseYesNo_No","Nac oes");
        welshMap.put("allegationsOfHarmOtherConcernsYesNo_Yes","Oes");
        welshMap.put("allegationsOfHarmOtherConcernsYesNo_No","Nac oes");
        welshMap.put("ordersNonMolestation_Yes","Oes");
        welshMap.put("ordersNonMolestation_No","Nac oes");
        welshMap.put("ordersOccupation_Yes","Oes");
        welshMap.put("ordersOccupation_No","Nac oes");
        welshMap.put("ordersForcedMarriageProtection_Yes","Oes");
        welshMap.put("ordersForcedMarriageProtection_No","Nac oes");
        welshMap.put("ordersRestraining_Yes","Oes");
        welshMap.put("ordersRestraining_No","Nac oes");
        welshMap.put("ordersOtherInjunctive_Yes","Oes");
        welshMap.put("ordersOtherInjunctive_No","Nac oes");
        welshMap.put("ordersUndertakingInPlace_Yes","Oes");
        welshMap.put("ordersUndertakingInPlace_No","Nac oes");
        welshMap.put("abductionChildHasPassport_Yes","Oes");
        welshMap.put("abductionChildHasPassport_No","Nac oes");
        welshMap.put("abductionPreviousPoliceInvolvement_Yes"," "); //missing from excel
        welshMap.put("abductionPreviousPoliceInvolvement_No"," ");

        /**
         * Other Proceedings.
         */
        welshMap.put(ProceedingsEnum.ongoing.getDisplayedValue(), "achosion sy’n mynd ymlaen ar hyn o bryd");
        welshMap.put(ProceedingsEnum.previous.getDisplayedValue(), "achosion blaenorol");
        welshMap.put(TypeOfOrderEnum.emergencyProtectionOrder.getDisplayedValue(), "Gorchymyn Diogelu Brys");
        welshMap.put(TypeOfOrderEnum.superviosionOrder.getDisplayedValue(), "Gorchymyn Goruchwylio");
        welshMap.put(TypeOfOrderEnum.careOrder.getDisplayedValue(), "Gorchymyn Gofal");
        welshMap.put(TypeOfOrderEnum.childAbduction.getDisplayedValue(), "Herwgydio plentyn");
        welshMap.put(TypeOfOrderEnum.familyLaw1996Part4.getDisplayedValue(), "Deddf Cyfraith Teulu 1996 Rhan 4");
        welshMap.put(TypeOfOrderEnum.contactOrResidenceOrder.getDisplayedValue(),
                     "Gorchymyn cyswllt neu orchymyn preswylio a wnaed fel rhan o achos ysgaru neu achos diddymu partneriaeth sifil");
        welshMap.put(TypeOfOrderEnum.contactOrResidenceOrderWithAdoption.getDisplayedValue(),
                     "Gorchymyn cyswllt neu orchymyn preswylio a wnaed mewn perthynas â Gorchymyn Mabwysiadu");
        welshMap.put(TypeOfOrderEnum.orderRelatingToChildMaintainance.getDisplayedValue(), "Gorchymyn yn ymwneud â chynhaliaeth plant");
        welshMap.put(TypeOfOrderEnum.childArrangementsOrder.getDisplayedValue(), "Gorchymyn trefniadau plant");
        welshMap.put(TypeOfOrderEnum.otherOrder.getDisplayedValue(), "Gorchmynion eraill");

        /**
         * Attending the hearing.*/
        welshMap.put("isWelshNeeded_yes","Bydd");
        welshMap.put("isWelshNeeded_no","Na fydd");
        welshMap.put(SpokenOrWrittenWelshEnum.spoken.getDisplayedValue(), "Byddant eisiau siarad Cymraeg");
        welshMap.put(SpokenOrWrittenWelshEnum.written.getDisplayedValue(), "Byddant eisiau darllen ac ysgrifennu yn Gymraeg");
        welshMap.put(SpokenOrWrittenWelshEnum.both.getDisplayedValue(), "Y ddau");
        welshMap.put("isInterpreterNeeded_yes","Bydd");
        welshMap.put("isInterpreterNeeded_no","Na fydd");
        welshMap.put(PartyEnum.applicant.getDisplayedValue(), "Ceisydd");
        welshMap.put(PartyEnum.respondent.getDisplayedValue(), "Atebydd");
        welshMap.put(PartyEnum.other.getDisplayedValue(), "Pobl eraill yn yr achos");
        welshMap.put("isDisabilityPresent_Yes","Oes");
        welshMap.put("isDisabilityPresent_No","Nac oes");
        welshMap.put("isSpecialArrangementsRequired_Yes","Bydd");
        welshMap.put("isSpecialArrangementsRequired_No","Na fydd");

        /**
         * International Element.
         */
        welshMap.put("habitualResidentInOtherState_Yes","Oes");
        welshMap.put("habitualResidentInOtherState_No","Nac oes");
        welshMap.put("jurisdictionIssue_Yes","Oes");
        welshMap.put("jurisdictionIssue_No","Nac oes");
        welshMap.put("requestToForeignAuthority_Yes","Oes");
        welshMap.put("requestToForeignAuthority_No","Nac oes");

        /**
         * Litigation capacity.
         */
        welshMap.put("litigationCapacityOtherFactors_Yes","Ydw");
        welshMap.put("litigationCapacityOtherFactors_No","Nac ydw");

        /**
         * Welsh language requirement.
         */
        welshMap.put(LanguagePreference.english.getDisplayedValue(), "Saesneg");
        welshMap.put(LanguagePreference.welsh.getDisplayedValue(), "Cymraeg");

        return welshMap;
    }
}
