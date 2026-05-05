package com.banking.util;

import org.iban4j.CountryCode;
import org.iban4j.Iban;

/**
 * @deprecated Use {@code Iban.random(CountryCode.NL).toString()} directly.
 * This class is kept for backwards compatibility only.
 */
@Deprecated
public class IbanGenerator {

    private IbanGenerator() {}

    public static String generate() {
        return Iban.random(CountryCode.NL).toString();
    }
}
