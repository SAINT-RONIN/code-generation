package com.banking.util;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class IbanGenerator {

    private static final String COUNTRY_CODE = "NL";
    private static final String BANK_CODE = "INHO0";

    public static String generate() {
        String accountNumber = generateRandomNineDigits();
        int checkDigits = calculateIbanCheckDigits(BANK_CODE + accountNumber);
        return String.format("%s%02d%s%s", COUNTRY_CODE, checkDigits, BANK_CODE, accountNumber);
    }

    private static String generateRandomNineDigits() {
        return String.format("%09d", ThreadLocalRandom.current().nextInt(1, 1_000_000_000));
    }

    private static int calculateIbanCheckDigits(String bban) {
        String rearranged = bban + COUNTRY_CODE + "00";
        String numeric = rearranged.chars()
                .mapToObj(c -> Character.isLetter(c)
                        ? String.valueOf(c - 'A' + 10)
                        : String.valueOf((char) c))
                .collect(Collectors.joining());
        int remainder = new BigInteger(numeric).mod(BigInteger.valueOf(97)).intValue();
        return 98 - remainder;
    }
}
