package automation.clustering.optimization;

public class CleanAddress {
    public static String cleanAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "";
        }

        String cleaned = address;

        if (cleaned.trim().equalsIgnoreCase("Точка на карте") ||
                cleaned.trim().equalsIgnoreCase("точка на карте")) {
            return "Адрес не указан (только координаты)";
        }
        cleaned = cleaned.replaceAll("<[^>]*>", "");
        cleaned = cleaned.replaceAll("https?://\\S+", "");
        cleaned = cleaned.replaceAll("yandex\\.\\S+", "");
        cleaned = cleaned.replaceAll("\\b\\d{1,2}\\.\\d{4,}\\s*,\\s*\\d{1,3}\\.\\d{4,}\\b", "");
        cleaned = cleaned.replaceAll("Данные геолокации\\s+\\d{1,2}\\.\\d{4,}\\s*,\\s*\\d{1,3}\\.\\d{4,}", "");
        cleaned = cleaned.replaceAll("Данные геолокации\\s*", "");
        cleaned = cleaned.replaceAll("\\+?7[-\\s]?\\(?\\d{3}\\)?[-\\s]?\\d{3}[-\\s]?\\d{2}[-\\s]?\\d{2}", "");
        cleaned = cleaned.replaceAll("8\\s?\\(?\\d{3}\\)?\\s?\\d{3}[-\\s]?\\d{2}[-\\s]?\\d{2}", "");
        cleaned = cleaned.replaceAll("(?i)^в навигаторе указать:\\s*", "");
        cleaned = cleaned.replaceAll("(?i)^в навигаторе внести\\s*", "");
        cleaned = cleaned.replaceAll("(?i)(см схему проезда|ссылка для водителя|перед заездом звонить|заранее позвонить|обязательно позвонить|после поворота|въезд на третьем|сказать что на склад|после въезда огибаете|входите на склад|зона разгрузки|есть зона разгрузки|укажет точку доставки|либо)\\s*", "");
        cleaned = cleaned.replaceAll("(?i)навигаторе указать:\\s*", "");
        cleaned = cleaned.replaceAll("(?i)навигаторе внести\\s*", "");
        cleaned = cleaned.replaceAll("(?i)(водителю|водитель|для водителя|звонить по|телефон|конт|кладовщик|приемщик|менеджер|грузчик|КПП|ВОРОТА).*$", "");
        cleaned = cleaned.replaceAll("(?i)(сказать что на склад|профреш|после въезда|огибаете|входите|ищете)\\s*", "");
        cleaned = cleaned.replaceAll("\\s+(КПП|ВОРОТА|БОКС)\\s*", " ");
        cleaned = cleaned.replaceAll("\\([^)]+\\)", "");
        cleaned = cleaned.replaceAll("[«»\"'„“]", "");
        cleaned = cleaned.replaceAll("^\\d+[.\\s]+", "");
        cleaned = cleaned.replaceAll("(?i)^точка на карте\\s*", "");
        cleaned = cleaned.replaceAll("[\\r\\n\\t]+", " ");
        cleaned = cleaned.replaceAll("\\s+", " ");
        cleaned = cleaned.replaceAll("\\s*,\\s*", ", ");
        cleaned = cleaned.replaceAll("\\s+\\.\\s+", ". ");
        cleaned = cleaned.replaceAll("\\s+", " ");
        cleaned = cleaned.replaceAll("\\.{2,}", ".");
        cleaned = cleaned.replaceAll(",{2,}", ",");
        cleaned = cleaned.replaceAll("[*#_~|`]", "");
        cleaned = cleaned.replaceAll("\\b[а-яА-Я]\\b\\s+", "");
        cleaned = cleaned.replaceAll("[\\u202A\\u202B\\u202C\\u202D\\u202E]", "");
        cleaned = cleaned.trim();
        cleaned = cleaned.replaceAll("[,.;:!?\\s]+$", "");
        cleaned = cleaned.replaceAll("^[,.;:!?\\s]+", "");
        if (cleaned.length() < 3 || cleaned.matches("^[\\d\\s,.-]+$")) {
            return "Адрес не указан";
        }
        cleaned = cleaned.replaceAll("\\s+", " ");
        if (!cleaned.isEmpty() && !Character.isDigit(cleaned.charAt(0))) {
            cleaned = Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
        }

        return cleaned;
    }
}