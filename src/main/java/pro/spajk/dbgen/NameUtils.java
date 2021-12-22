package pro.spajk.dbgen;

import org.apache.commons.lang3.text.WordUtils;

import static java.lang.Character.isDigit;
import static java.lang.Character.toLowerCase;
import static javax.lang.model.SourceVersion.isKeyword;
import static org.apache.commons.lang3.StringUtils.*;

public class NameUtils {

    public static final String ILLEGAL_CHARACTER_REGEX = "[^0-9a-zA-Z_$]";

    public static String getBuilderTypeParameterName() {
        return "T";
    }

    public static String replaceIllegalCharacters(String name) {
        return name.replaceAll(ILLEGAL_CHARACTER_REGEX, "_");
    }

    public static String normalizeName(String name) {
        name = capitalizeTrailingWords(name);

        if (isDigit(name.charAt(0))) {
            name = "_" + name;
        }

        return name;
    }

    public static String capitalizeTrailingWords(String name) {
        char[] wordDelimiters = {'-', ' ', '_'};

        if (containsAny(name, wordDelimiters)) {
            String capitalizedNodeName;
            if (areAllWordsUpperCaseBesideDelimiters(name, wordDelimiters)) {
                capitalizedNodeName = WordUtils.capitalizeFully(name, wordDelimiters);
            } else {
                capitalizedNodeName = WordUtils.capitalize(name, wordDelimiters);
            }
            name = name.charAt(0) + capitalizedNodeName.substring(1);

            for (char c : wordDelimiters) {
                name = remove(name, c);
            }
        } else if (areAllWordsUpperCaseBesideDelimiters(name, wordDelimiters)) {
            name = WordUtils.capitalizeFully(name, wordDelimiters);
        }

        return name;
    }

    private static boolean areAllWordsUpperCaseBesideDelimiters(String words, char... delimiters) {
        char[] wordChars = words.toCharArray();
        for (char c : wordChars) {
            if (!containsAny("" + c, delimiters) && Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
    }

    private static String makeLowerCamelCase(String name) {
        return toLowerCase(name.charAt(0)) + name.substring(1);
    }

    public static String getPropertyName(String fieldName) {
        fieldName = replaceIllegalCharacters(fieldName);
        fieldName = normalizeName(fieldName);
        fieldName = makeLowerCamelCase(fieldName);

        if (isKeyword(fieldName)) {
            fieldName = "_" + fieldName;
        }

        if (isKeyword(fieldName)) {
            fieldName += "_";
        }

        return fieldName;
    }


    public static String getSetterName(String propertyName) {
        propertyName = getPropertyNameForAccessor(propertyName);

        String prefix = "set";

        String setterName;
        if (propertyName.length() > 1 && Character.isUpperCase(propertyName.charAt(1))) {
            setterName = prefix + propertyName;
        } else {
            setterName = prefix + capitalize(propertyName);
        }

        if (setterName.equals("setClass")) {
            setterName = "setClass_";
        }

        return setterName;
    }


    public static String getGetterName(String propertyName, boolean isBool) {
        propertyName = getPropertyNameForAccessor(propertyName);

        String prefix = isBool ? "is" : "get";

        String getterName;
        if (propertyName.length() > 1 && Character.isUpperCase(propertyName.charAt(1))) {
            getterName = prefix + propertyName;
        } else {
            getterName = prefix + capitalize(propertyName);
        }

        if (getterName.equals("getClass")) {
            getterName = "getClass_";
        }

        return getterName;
    }

    private static String getPropertyNameForAccessor(String propertyName) {
        propertyName = replaceIllegalCharacters(propertyName);
        propertyName = capitalizeTrailingWords(propertyName);
        return propertyName;
    }
}
