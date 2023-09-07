package br.psi.giganet.api.purchase.dashboard.repository.factory;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

public class QuotationSqlFactory {

    public static String countQuotations(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "COALESCE(COUNT(q.id), 0) " +
                "FROM quotations q " +
                "WHERE  (DATE(q.createddate) >= ':initialDate' AND DATE(q.createddate) <= ':finalDate')  AND  " +
                "status IN (':statuses')";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String totalsQuotations(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "COALESCE(SUM(q.total), 0) " +
                "FROM quotations q " +
                "WHERE  (DATE(q.createddate) >= ':initialDate' AND DATE(q.createddate) <= ':finalDate')  AND  " +
                "status IN (':statuses')";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String countAndTotalsQuotations(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "COALESCE(COUNT(q.id), 0) AS \"count\", " +
                "COALESCE(SUM(q.total), 0) AS \"total\" " +
                "FROM quotations q " +
                "WHERE  (DATE(q.createddate) >= ':initialDate' AND DATE(q.createddate) <= ':finalDate')  AND  " +
                "status IN (':statuses')";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String totalsQuotationsGroupByDays(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "DATE(days.day) as \"date\", " +
                "COALESCE(t.total, 0) " +
                "FROM generate_series(':initialDate'\\:\\:timestamp,':finalDate', '1 day') AS days(\"day\") " +
                "LEFT JOIN (" +
                "   SELECT DATE(q.createddate) AS \"day\", SUM(q.total) AS \"total\" " +
                "   FROM quotations q " +
                "   WHERE ( DATE(q.createddate) >= ':initialDate' AND DATE(q.createddate) <= ':finalDate') AND " +
                "   status IN (':statuses') " +
                "   GROUP BY DATE(q.createddate)) " +
                "AS t ON DATE(days.day) = (t.day)";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String totalsQuotationsByCostCenter(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "c.name, " +
                "COALESCE(t.total, 0) " +
                "FROM cost_centers c " +
                "LEFT JOIN " +
                "   ( SELECT cc.id AS \"id\", " +
                "     COALESCE(SUM(q.total), 0) AS \"total\" " +
                "     FROM  cost_centers cc " +
                "     INNER JOIN  quotations q " +
                "     ON cc.id = q.costcenter " +
                "     WHERE (DATE(q.createddate) >= ':initialDate' AND DATE(q.createddate) <= ':finalDate')  AND " +
                "     status IN (':statuses') " +
                "     GROUP BY  cc.id) " +
                "AS t ON t.id = c.id";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String findMostQuotedItems(LocalDate initialDate, LocalDate finalDate) {
        return null;
//        String template = "SELECT " +
//                "p.name AS \"product\", " +
//                "COUNT(i.id) AS \"countOrders\", " +
//                "SUM(i.total) AS \"total\" " +
//                "FROM quoted_items i " +
//                "INNER JOIN products p ON p.id = i.product " +
//                "INNER JOIN quotations q ON q.id = i.quotation " +
//                "WHERE (DATE(q.createddate) >= ':initialDate' AND DATE(q.createddate) <= ':finalDate')  AND " +
//                "q.status != 'CANCELED' " +
//                "GROUP BY p.name " +
//                "ORDER BY COUNT(i.id) DESC " +
//                "LIMIT 5";
//
//        return template
//                .replaceAll(":initialDate", initialDate.toString())
//                .replaceAll(":finalDate", finalDate.toString());
    }
}
