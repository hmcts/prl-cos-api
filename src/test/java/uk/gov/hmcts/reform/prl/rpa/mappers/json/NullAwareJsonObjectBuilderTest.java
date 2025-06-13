package uk.gov.hmcts.reform.prl.rpa.mappers.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class NullAwareJsonObjectBuilderTest {

    @InjectMocks
    NullAwareJsonObjectBuilder nullAwareJsonObjectBuilder;

    @Test
    void testNullAwareJsonBuilderWithStringParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        delegate.add("keyString","value");
        delegate.add("keyJsonValue",JsonValue.FALSE);
        assertEquals(nullAwareJsonObjectBuilder.add("keyString","value").build().get("keyString"),delegate.build().get("keyString"));
        assertEquals(JsonValue.FALSE,nullAwareJsonObjectBuilder.add("keyJsonValue",JsonValue.FALSE).build().get("keyJsonValue"));
    }

    @Test
    void testNullAwareJsonBuilderWithStringAndBigIntegerParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        delegate.add("keyBigInt",BigInteger.ONE);
        assertEquals(nullAwareJsonObjectBuilder.add("keyBigInt",BigInteger.ONE).build().get("keyBigInt"), delegate.build().get("keyBigInt"));
    }

    @Test
    void testNullAwareJsonBuilderWithStringAndBigDecimalParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();

        delegate.add("key", BigDecimal.ONE);
        assertEquals(nullAwareJsonObjectBuilder.add("key",BigDecimal.ONE).build().get("key"), delegate.build().get("key"));
    }

    @Test
    void testNullAwareJsonBuilderWithStringAndIntParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        delegate.add("key",1);
        assertEquals(nullAwareJsonObjectBuilder.add("key",1).build().get("key"),delegate.build().get("key"));
    }

    @Test
    void testNullAwareJsonBuilderWithStringAndLongParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        delegate.add("key",1L);
        assertEquals(nullAwareJsonObjectBuilder.add("key",1L).build().get("key"),delegate.build().get("key"));
    }

    @Test
    void testNullAwareJsonBuilderWithStringAndBooleanParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        delegate.add("key",Boolean.TRUE);
        assertEquals(nullAwareJsonObjectBuilder.add("key",Boolean.TRUE).build().get("key"),delegate.build().get("key"));
    }

    @Test
    void testNullAwareJsonBuilderWithStringAndDoubleParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        delegate.add("key",Double.valueOf("1"));
        assertEquals(nullAwareJsonObjectBuilder.add("key",Double.valueOf("1")).build().get("key"),delegate.build().get("key"));
    }

    @Test
    void testNullAwareJsonBuilderWithStringAndJsonObjectParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        JsonObjectBuilder delegate1 = JsonProvider.provider().createObjectBuilder();
        delegate.add("key",delegate1);
        assertEquals(nullAwareJsonObjectBuilder.add("key",delegate1).build().get("key"),delegate.build().get("key"));
    }

    @Test
    void testNullAwareJsonBuilderWithStringAndJsonArrayParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        JsonArrayBuilder delegate1 = JsonProvider.provider().createArrayBuilder();
        delegate.add("key",delegate1);
        assertEquals(nullAwareJsonObjectBuilder.add("key",delegate1).build().get("key"),delegate.build().get("key"));
    }

    @Test
    void testNullAwareJsonBuilderAddNullParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        delegate.addNull("key");
        assertEquals(nullAwareJsonObjectBuilder.addNull("key").build().get("key"),delegate.build().get("key"));
    }
}
