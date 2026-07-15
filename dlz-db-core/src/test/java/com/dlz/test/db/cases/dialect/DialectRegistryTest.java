package com.dlz.test.db.cases.dialect;

import com.dlz.db.dialect.DbDialect;
import com.dlz.db.dialect.DialectRegistry;
import com.dlz.db.exception.DbParameterException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DialectRegistryTest {
    @Test
    void registerAndResolveDialectByNormalizedId() {
        DbDialect custom = () -> "  test-dialect ";
        DialectRegistry.register(custom);
        assertSame(custom, DialectRegistry.get("TEST-DIALECT"));
        assertTrue(DialectRegistry.contains("test-dialect"));
        assertTrue(DialectRegistry.all().contains(custom));
        DbDialect urlDialect = () -> "url-dialect";
        DbDialect matchingDialect = new DbDialect() {
            @Override
            public String id() {
                return urlDialect.id();
            }

            @Override
            public boolean matchesUrl(String url) {
                return url != null && url.contains(":url-dialect:");
            }
        };
        DialectRegistry.register(matchingDialect);
        assertSame(matchingDialect, DialectRegistry.resolve(null, "jdbc:url-dialect:test"));
        assertThrows(DbParameterException.class, () -> DialectRegistry.register(null));
        assertThrows(DbParameterException.class, () -> DialectRegistry.register(() -> " "));
        assertNull(DialectRegistry.get(null));
    }

    @Test
    void builtInEnumRemainsCompatibleDialect() {
        assertEquals("sqlite", DialectRegistry.get("sqlite").id());
    }
}
