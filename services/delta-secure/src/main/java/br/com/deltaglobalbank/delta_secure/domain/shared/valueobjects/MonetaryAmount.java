package br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class MonetaryAmount {

    private MonetaryAmount() {
    }

    public static Double parseBrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().replace("R$", "").trim();
        boolean hasComma = normalized.contains(",");
        boolean hasDot = normalized.contains(".");
        String numeric;
        if (hasComma && hasDot) {
            numeric = normalized.replace(".", "").replace(",", ".");
        } else if (hasComma) {
            numeric = normalized.replace(",", ".");
        } else {
            numeric = normalized;
        }
        try {
            return Double.parseDouble(numeric);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String toDisplayString(double value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.of("pt", "BR"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(value);
    }

    public static String toSWorksString(double value) {
        BigDecimal decimal = BigDecimal.valueOf(value);
        if (decimal.stripTrailingZeros().scale() <= 0) {
            return decimal.toBigInteger().toString();
        }
        return decimal.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
