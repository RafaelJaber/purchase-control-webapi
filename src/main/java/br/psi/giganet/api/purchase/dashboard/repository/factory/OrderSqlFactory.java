package br.psi.giganet.api.purchase.dashboard.repository.factory;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

public class OrderSqlFactory {

    public static String countPurchaseOrders(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "COALESCE(COUNT(o.id), 0) " +
                "FROM purchase_orders o " +
                "WHERE  (DATE(o.createddate) >= ':initialDate' AND DATE(o.createddate) <= ':finalDate')  AND  " +
                "status NOT IN (':statuses')";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String countPurchaseOrdersByCompetence(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "COALESCE(COUNT(o.id), 0) " +
                "FROM purchase_orders o " +
                "   INNER JOIN purchase_order_competences comp ON o.id = comp.purchase_order " +
                "WHERE (comp.date >= ':initialDate' AND comp.date <= ':finalDate')  AND  " +
                "status NOT IN (':statuses') " +
                "GROUP BY o.id ";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String findAllPurchaseOrders(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "o.id, " +
                "o.createddate, " +
                "q.description, " +
                "o.total " +
                "FROM purchase_orders o " +
                "INNER JOIN quotation_approvals ap ON ap.id = o.approval " +
                "INNER JOIN quotations q ON q.id = ap.quotation " +
                "WHERE (DATE(o.createddate) >= ':initialDate' AND DATE(o.createddate) <= ':finalDate')  AND  " +
                "o.status IN (':statuses')";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String findAllPurchaseOrdersByCompetence(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "o.id, " +
                "o.createddate, " +
                "q.description, " +
                "SUM(comp.total) AS \"total\" " +
                "FROM purchase_orders o " +
                "INNER JOIN quotation_approvals ap ON ap.id = o.approval " +
                "INNER JOIN quotations q ON q.id = ap.quotation " +
                "INNER JOIN purchase_order_competences comp ON o.id = comp.purchase_order " +
                "WHERE  (comp.date >= ':initialDate' AND comp.date <= ':finalDate')  AND  " +
                "o.status IN (':statuses') "+
                "GROUP BY o.id, o.createddate, q.description ";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String findAllDetailedPurchaseOrders(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "o.createddate, " +
                "o.id, " +
                "sup.name AS supplier, " +
                "o.status, " +
                "q.description, " +
                "cc.name AS costCenter, " +
                "o.total, " +
                "pmc.name AS paymentCondition " +
                "FROM purchase_orders o " +
                "INNER JOIN quotation_approvals ap ON ap.id = o.approval " +
                "INNER JOIN quotations q ON q.id = ap.quotation " +
                "INNER JOIN suppliers sup ON sup.id = o.supplier " +
                "INNER JOIN cost_centers cc ON cc.id = o.costcenter " +
                "INNER JOIN purchase_orders_payment_conditions opmc ON opmc.purchaseorder = o.id " +
                "INNER JOIN payment_conditions pmc ON pmc.id = opmc.condition " +
                "WHERE (DATE(o.createddate) >= ':initialDate' AND DATE(o.createddate) <= ':finalDate')  AND  " +
                "o.status IN (':statuses')";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String findAllDetailedPurchaseOrdersByCompetences(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "o.createddate, " +
                "o.id, " +
                "sup.name AS supplier, " +
                "o.status, " +
                "q.description, " +
                "cc.name AS costCenter, " +
                "SUM(comp.total) AS \"total\", " +
                "pmc.name AS paymentCondition " +
                "FROM purchase_orders o " +
                "INNER JOIN quotation_approvals ap ON ap.id = o.approval " +
                "INNER JOIN quotations q ON q.id = ap.quotation " +
                "INNER JOIN suppliers sup ON sup.id = o.supplier " +
                "INNER JOIN cost_centers cc ON cc.id = o.costcenter " +
                "INNER JOIN purchase_orders_payment_conditions opmc ON opmc.purchaseorder = o.id " +
                "INNER JOIN payment_conditions pmc ON pmc.id = opmc.condition " +
                "INNER JOIN purchase_order_competences comp ON o.id = comp.purchase_order " +
                "WHERE  (comp.date >= ':initialDate' AND comp.date <= ':finalDate')  AND  " +
                "o.status IN (':statuses') " +
                "GROUP BY o.createddate, o.id, sup.name, o.status, q.description, cc.name, pmc.name  ";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String totalsPurchaseOrders(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "COALESCE(SUM(o.total), 0) " +
                "FROM purchase_orders o " +
                "WHERE  (DATE(o.createddate) >= ':initialDate' AND DATE(o.createddate) <= ':finalDate')  AND  " +
                "status IN (':statuses')";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String totalsPurchaseOrdersByCompetence(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "COALESCE(SUM(comp.total), 0) " +
                "FROM purchase_orders o " +
                "   INNER JOIN purchase_order_competences comp ON o.id = comp.purchase_order " +
                "WHERE  (comp.date >= ':initialDate' AND comp.date <= ':finalDate')  AND  " +
                "status IN (':statuses') ";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String totalsPurchaseOrdersGroupByDays(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "DATE(days.day) as \"date\", " +
                "COALESCE(t.total, 0) " +
                "FROM generate_series(':initialDate'\\:\\:timestamp,':finalDate', '1 day') AS days(\"day\") " +
                "LEFT JOIN (" +
                "   SELECT DATE(o.createddate) AS \"day\", SUM(o.total) AS \"total\" " +
                "   FROM purchase_orders o " +
                "   WHERE ( DATE(o.createddate) >= ':initialDate' AND DATE(o.createddate) <= ':finalDate') AND " +
                "   status IN (':statuses') " +
                "   GROUP BY DATE(o.createddate)) " +
                "AS t ON DATE(days.day) = (t.day)";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String totalsPurchaseOrdersByCostCenter(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "c.name, " +
                "COALESCE(t.total, 0) " +
                "FROM cost_centers c " +
                "LEFT JOIN " +
                "   ( SELECT cc.id AS \"id\", " +
                "     COALESCE(SUM(o.total), 0) AS \"total\" " +
                "     FROM  cost_centers cc " +
                "     INNER JOIN  purchase_orders o " +
                "     ON cc.id = o.costcenter " +
                "     WHERE (DATE(o.createddate) >= ':initialDate' AND DATE(o.createddate) <= ':finalDate')  AND " +
                "     status IN (':statuses') " +
                "     GROUP BY  cc.id) " +
                "AS t ON t.id = c.id";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String totalsPurchaseOrdersByCostCenterAndCompetence(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        String template = "SELECT " +
                "c.name, " +
                "COALESCE(t.total, 0) " +
                "FROM cost_centers c " +
                "LEFT JOIN " +
                "   ( SELECT cc.id AS \"id\", " +
                "     COALESCE(SUM(comp.total), 0) AS \"total\" " +
                "     FROM  cost_centers cc " +
                "     INNER JOIN  purchase_orders o ON cc.id = o.costcenter " +
                "     INNER JOIN purchase_order_competences comp ON o.id = comp.purchase_order " +
                "       WHERE  (comp.date >= ':initialDate' AND comp.date <= ':finalDate')  AND  " +
                "     status IN (':statuses') " +
                "     GROUP BY  cc.id ) " +
                "AS t ON t.id = c.id";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString())
                .replaceAll(":statuses", Arrays.stream(statuses).map(ProcessStatus::name)
                        .collect(Collectors.joining("','")));
    }

    public static String findMostPurchasedItems(LocalDate initialDate, LocalDate finalDate) {
        String template = "SELECT " +
                "p.name AS \"product\", " +
                "COUNT(i.id) AS \"countOrders\", " +
                "SUM(i.total) AS \"total\" " +
                "FROM purchase_order_items i " +
                "INNER JOIN products p ON p.id = i.product " +
                "INNER JOIN purchase_orders o ON o.id = i.purchase_order " +
                "WHERE (DATE(o.createddate) >= ':initialDate' AND DATE(o.createddate) <= ':finalDate')  AND " +
                "o.status != 'CANCELED' " +
                "GROUP BY p.name " +
                "ORDER BY COUNT(i.id) DESC " +
                "LIMIT 5";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString());
    }

    public static String findMostPurchasedItemsByCompetence(LocalDate initialDate, LocalDate finalDate) {
        String template = "SELECT " +
                "p.name AS \"product\", " +
                "COUNT(i.id) AS \"countOrders\", " +
                "SUM(i.total) AS \"total\" " +
                "FROM purchase_order_items i " +
                "INNER JOIN products p ON p.id = i.product " +
                "INNER JOIN purchase_orders o ON o.id = i.purchase_order " +
                "INNER JOIN purchase_order_competences comp ON o.id = comp.purchase_order " +
                "WHERE  (comp.date >= ':initialDate' AND comp.date <= ':finalDate')  AND  " +
                "o.status != 'CANCELED' " +
                "GROUP BY p.name " +
                "ORDER BY COUNT(i.id) DESC " +
                "LIMIT 5";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString());
    }

    public static String findMostPurchasedSuppliers(LocalDate initialDate, LocalDate finalDate) {
        String template = "SELECT " +
                "s.name AS \"supplier\", " +
                "COUNT(o.id) AS \"countOrders\", " +
                "SUM(o.total) AS \"total\" FROM purchase_orders o " +
                "INNER JOIN suppliers s ON s.id = o.supplier " +
                "WHERE (DATE(o.createddate) >= ':initialDate' AND DATE(o.createddate) <= ':finalDate')  AND " +
                "o.status != 'CANCELED' " +
                "GROUP BY s.name " +
                "ORDER BY COUNT(o.id) DESC, " +
                "SUM(o.total) DESC " +
                "LIMIT 5";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString());
    }

    public static String findMostPurchasedSuppliersByCompetence(LocalDate initialDate, LocalDate finalDate) {
        String template = "SELECT " +
                "s.name AS \"supplier\", " +
                "COUNT(o.id) AS \"countOrders\", " +
                "SUM(o.total) AS \"total\" FROM purchase_orders o " +
                "INNER JOIN suppliers s ON s.id = o.supplier " +
                "INNER JOIN purchase_order_competences comp ON o.id = comp.purchase_order " +
                "WHERE  (comp.date >= ':initialDate' AND comp.date <= ':finalDate')  AND  " +
                "o.status != 'CANCELED' " +
                "GROUP BY s.name " +
                "ORDER BY COUNT(o.id) DESC, " +
                "SUM(o.total) DESC " +
                "LIMIT 5";

        return template
                .replaceAll(":initialDate", initialDate.toString())
                .replaceAll(":finalDate", finalDate.toString());
    }
}
