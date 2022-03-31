package uk.gov.hmcts.reform.prl.rpa.mappers.json;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class NullAwareJsonObjectBuilderTest {

    @InjectMocks
    NullAwareJsonObjectBuilder nullAwareJsonObjectBuilder;

    @Test
    public void testNullAwareJsonBuilderWithStringParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        delegate.add("keyString","value");
        delegate.add("keyJsonValue",JsonValue.FALSE);
        assertEquals(nullAwareJsonObjectBuilder.add("keyString","value").build().get("keyString"),delegate.build().get("keyString"));
        assertEquals(JsonValue.FALSE,nullAwareJsonObjectBuilder.add("keyJsonValue",JsonValue.FALSE).build().get("keyJsonValue"));
    }

    @Test
    public void testNullAwareJsonBuilderWithStringAndBigIntegerParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        delegate.add("keyBigInt",BigInteger.ONE);
        assertEquals(nullAwareJsonObjectBuilder.add("keyBigInt",BigInteger.ONE).build().get("keyBigInt"), delegate.build().get("keyBigInt"));
    }

    @Test
    public void testNullAwareJsonBuilderWithStringAndBigDecimalParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();

        delegate.add("key", BigDecimal.ONE);
        assertEquals(nullAwareJsonObjectBuilder.add("key",BigDecimal.ONE).build().get("key"), delegate.build().get("key"));
    }

    @Test
    public void testNullAwareJsonBuilderWithStringAndIntParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        delegate.add("key",Integer.valueOf(1));
        assertEquals(nullAwareJsonObjectBuilder.add("key",Integer.valueOf(1)).build().get("key"),delegate.build().get("key"));
    }

    @Test
    public void testNullAwareJsonBuilderWithStringAndLongParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        delegate.add("key",Long.valueOf(1));
        assertEquals(nullAwareJsonObjectBuilder.add("key",Long.valueOf(1)).build().get("key"),delegate.build().get("key"));
    }

    @Test
    public void testNullAwareJsonBuilderWithStringAndBooleanParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        delegate.add("key",Boolean.TRUE);
        assertEquals(nullAwareJsonObjectBuilder.add("key",Boolean.TRUE).build().get("key"),delegate.build().get("key"));
    }

    @Test
    public void testNullAwareJsonBuilderWithStringAndDoubleParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        delegate.add("key",Double.valueOf(1));
        assertEquals(nullAwareJsonObjectBuilder.add("key",Double.valueOf(1)).build().get("key"),delegate.build().get("key"));
    }

    @Test
    public void testNullAwareJsonBuilderWithStringAndJsonObjectParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        JsonObjectBuilder delegate1 = JsonProvider.provider().createObjectBuilder();
        delegate.add("key",delegate1);
        assertEquals(nullAwareJsonObjectBuilder.add("key",delegate1).build().get("key"),delegate.build().get("key"));
    }

    @Test
    public void testNullAwareJsonBuilderWithStringAndJsonArrayParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        JsonArrayBuilder delegate1 = JsonProvider.provider().createArrayBuilder();
        delegate.add("key",delegate1);
        assertEquals(nullAwareJsonObjectBuilder.add("key",delegate1).build().get("key"),delegate.build().get("key"));
    }

    @Test
    public void testNullAwareJsonBuilderAddNullParams() {
        JsonObjectBuilder delegate = JsonProvider.provider().createObjectBuilder();
        delegate.addNull("key");
        assertEquals(nullAwareJsonObjectBuilder.addNull("key").build().get("key"),delegate.build().get("key"));
    }
}
