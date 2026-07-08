package automation.clustering.geocoding;

import java.util.Set;

public class CleanAddress {

    static Set<String> WORDS = Set.of("лифт", "вблизи", "жд/ст", "станция", "ж");

    public static String cleaningAddress(String address) {
        if (address.isBlank()) return "";
        address = address.toLowerCase();

        address = address.replaceAll("\\s?\\([^)]*\\)", "");
        address = address.replaceAll("(\\d+[a-zа-я]?)(?:с|стр)(\\d+)", "$1 стр. $2");
        address = address.replaceAll("([а-яa-z]+)(\\d+)", "$1 $2");
        address = address.replaceAll("([.,])\\s*", "$1 ");
        address = address.replaceAll("(?:вл\\.?|владение)(\\d+)", "владение $1");

        address = address.replaceAll("https\\.*", "");
        address = address.replaceAll("№", "");
        address = address.replaceAll("в навигаторе.*", "");
        address = address.replaceAll("после поворота.*", "");
        address = address.replaceAll("заезд.*", "");
        address = address.replaceAll("\\bводитель.*", "");
        address = address.replaceAll("\\bза\\b.*", "");
        address = address.replaceAll("зона.*", "");
        address = address.replaceAll("кофейня.*", "");
        address = address.replaceAll("важно .*", "");
        address = address.replaceAll("вход .*", "");
        address = address.replaceAll("внимание.*", "");

        String[] massive = address.split("\\s+");

        StringBuilder sb = new StringBuilder();

        for (String word : massive) {
            word = removeDC(word);

            if (WORDS.contains(word)) continue;
            sb.append(word).append(" ");
        }

        return sb.toString();
    }

    private static String removeDC(String word) {
        return word.replace(".", "").replace(",", "");
    }
}