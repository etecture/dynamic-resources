package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.Version;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author rhk
 */
public class MediaTypeExpression implements MediaType {

    private static final Pattern MIME_TYPE_EXPRESSION = Pattern.compile(
            "^(?<category>\\*|text|application|image|message|audio|model|multipart|video)/(?<subtype>(?:(?<space>vnd|prs|x)(?:-|\\.))?.+?)(?:\\.v(?<version>[0-9.]+))?(?:\\+(?<alttype>.+?))?(?:;\\s*charset\\s*=\\s*(?<encoding>.+))?$");
    private final String category, subtype, space, alttype;
    private final Charset encoding;
    private final VersionExpression version;

    public MediaTypeExpression(String mediaType) {
        Matcher matcher = MIME_TYPE_EXPRESSION.matcher(mediaType);
        if (matcher.matches()) {
            category = matcher.group("category");
            if (StringUtils.isBlank(category)) {
                throw new IllegalArgumentException(
                        "The Mime-Type must specify a category.");
            }
            subtype = matcher.group("subtype");
            if (StringUtils.isBlank("subtype")) {
                throw new IllegalArgumentException(
                        "The Mime-Type must specify a subtype.");
            }
            alttype = matcher.group("alttype");
            space = matcher.group("space");
            encoding = Charset.forName(getGroupOrDefaultValue(matcher,
                    "encoding",
                    "UTF-8"));
            String versionString = matcher.group("version");
            if (StringUtils.isBlank(versionString)) {
                version = null;
            } else {
                version = new VersionExpression(versionString);
            }
        } else {
            throw new IllegalArgumentException(String.format(
                    "Mime-Type %s must match the Mime-Type-Expression!",
                    mediaType));
        }
    }

    private static String getGroupOrDefaultValue(Matcher matcher,
            String groupName, String defaultValue) {
        String groupValue = matcher.group(groupName);
        if (StringUtils.isBlank(groupValue)) {
            return defaultValue;
        } else {
            return groupValue;
        }
    }

    @Override
    public String category() {
        return category;
    }

    @Override
    public String subType() {
        return subtype;
    }

    @Override
    public String space() {
        return space;
    }

    @Override
    public String alternative() {
        return alttype;
    }

    @Override
    public Charset encoding() {
        return encoding;
    }

    @Override
    public Version version() {
        return version;
    }

    @Override
    public String toString() {
        return String.format("%s/%s%s%s; charset=%s",
                category,
                subtype,
                (version == null ? "" : ".v" + version),
                (StringUtils.isBlank(alttype) ? "" : "+" + alttype),
                encoding
                .toString());
    }

    @Override
    public boolean isCompatibleTo(String... mediaTypeStrings) {
        MediaType[] mediaTypes = new MediaType[mediaTypeStrings.length];
        for (int i = 0; i < mediaTypes.length; i++) {
            mediaTypes[i] = new MediaTypeExpression(mediaTypeStrings[i]);
        }
        return isCompatibleTo(mediaTypes);
    }

    @Override
    public boolean isCompatibleTo(MediaType... mediaTypes) {
        for (MediaType mediaType : mediaTypes) {
            if (isCompatible(mediaType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCompatible(MediaType mediaType) {
        if ("*".equals(this.subtype) || this.subtype.equalsIgnoreCase(mediaType
                .subType())) {
            return true;
        } else if (StringUtils.isNotBlank(this.alttype) && this.alttype
                .equalsIgnoreCase(mediaType.subType())) {
            return true;
        } else if (StringUtils.isNotBlank(mediaType.alternative()) && mediaType
                .alternative().equalsIgnoreCase(this.subtype)) {
            return true;
        }
        return false;
    }
}
