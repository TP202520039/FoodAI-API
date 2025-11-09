package  com.tp.foodai.shared.domain.model.valueobjects;

import java.util.regex.Pattern;

public class GuidValidator {
    private static final Pattern GUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    public static boolean isValidGuid(String guid) {
        return guid != null && GUID_PATTERN.matcher(guid).matches();
    }
}
