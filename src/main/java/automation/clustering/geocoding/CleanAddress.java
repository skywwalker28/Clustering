package automation.clustering.geocoding;

public class CleanAddress {
    public static String cleanAddress(String rawAddress) {
        System.out.println(rawAddress);
        String addr = rawAddress.trim();

        addr = addr.replaceFirst("^[.,]+", "");

        String[] sample = {"", "В ПЕРЕХОДЕ", "жд ", "ст ", "Курский Вокзал", "ещение", "8/П", "МО", "Мкр", "вал ",
                "пол ", "обл ", "м-н"};

        addr = addr.replaceAll("(?i)^ИП\\s+[^,]+,?", "");
        addr = addr.replaceAll("(?i)\\|стр|пом|,\\s*\\(", "");
        addr = addr.replaceAll("[()]", "");

        if (addr.equals(" МО Вокзальная 20/1, жд ст Павшино")) {
            System.out.println("Обработка специального случая");
            addr = "Вокзальная улица, 25Ак1";
            return addr;
        }

        for (String s : sample) {
            if (addr.contains(s)) {
                addr = addr.replace(s, "");
            }
        }

        addr = addr.replaceAll("(?i) стр", " строение ");
        addr = addr.replaceAll("(?i) к ", " корпус ");


        addr = addr.trim();
        addr = addr.replaceFirst("^[.,\\s]+", "");
        return addr;
    }
}
