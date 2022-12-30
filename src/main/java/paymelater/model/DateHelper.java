package paymelater.model;

/*
 ** DO NOT CHANGE!!
 */


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateHelper {
    public static final LocalDate TODAY = LocalDate.now();
    public static final LocalDate TOMORROW = TODAY.plusDays(1);
    public static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
}
