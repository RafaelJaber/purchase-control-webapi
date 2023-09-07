package br.psi.giganet.api.purchase.dashboard.repository;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.dashboard.repository.factory.OrderSqlFactory;
import br.psi.giganet.api.purchase.dashboard.repository.factory.QuotationSqlFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
@SuppressWarnings("unchecked")
public class DashboardRepositoryImpl {

    @PersistenceContext
    private EntityManager entityManager;

    public BigInteger countOrdersByStatusNotIn(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        return (BigInteger) entityManager.createNativeQuery(
                OrderSqlFactory.countPurchaseOrders(initialDate, finalDate, statuses))
                .getResultStream()
                .findFirst()
                .orElse(BigInteger.ZERO);
    }

    public BigInteger countOrdersByStatusNotInAndCompetence(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        return (BigInteger) entityManager.createNativeQuery(
                OrderSqlFactory.countPurchaseOrdersByCompetence(initialDate, finalDate, statuses))
                .getResultStream()
                .findFirst()
                .orElse(BigInteger.ZERO);
    }

    public BigDecimal totalOrdersByStatus(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        return (BigDecimal) entityManager.createNativeQuery(
                OrderSqlFactory.totalsPurchaseOrders(initialDate, finalDate, statuses))
                .getResultStream()
                .findFirst()
                .orElse(BigInteger.ZERO);
    }

    public BigDecimal totalOrdersByStatusByCompetence(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        return (BigDecimal) entityManager.createNativeQuery(
                OrderSqlFactory.totalsPurchaseOrdersByCompetence(initialDate, finalDate, statuses))
                .getResultStream()
                .findFirst()
                .orElse(BigInteger.ZERO);
    }

    public List<Map<String, Object>> findAllPurchaseOrders(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                OrderSqlFactory.findAllPurchaseOrders(initialDate, finalDate, statuses))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", resp[0]);
                    map.put("date", resp[1]);
                    map.put("description", resp[2]);
                    map.put("total", resp[3]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findAllPurchaseOrdersByCompetence(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                OrderSqlFactory.findAllPurchaseOrdersByCompetence(initialDate, finalDate, statuses))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", resp[0]);
                    map.put("date", resp[1]);
                    map.put("description", resp[2]);
                    map.put("total", resp[3]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findAllDetailedPurchaseOrders(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                OrderSqlFactory.findAllDetailedPurchaseOrders(initialDate, finalDate, statuses))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("date", resp[0]);
                    map.put("id", resp[1]);
                    map.put("supplier", resp[2]);
                    map.put("status", resp[3]);
                    map.put("description", resp[4]);
                    map.put("costCenter", resp[5]);
                    map.put("total", resp[6]);
                    map.put("paymentCondition", resp[7]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findAllDetailedPurchaseOrdersByCompetences(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                OrderSqlFactory.findAllDetailedPurchaseOrdersByCompetences(initialDate, finalDate, statuses))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("date", resp[0]);
                    map.put("id", resp[1]);
                    map.put("supplier", resp[2]);
                    map.put("status", resp[3]);
                    map.put("description", resp[4]);
                    map.put("costCenter", resp[5]);
                    map.put("total", resp[6]);
                    map.put("paymentCondition", resp[7]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> totalOrdersByStatusGroupByDays(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                OrderSqlFactory.totalsPurchaseOrdersGroupByDays(initialDate, finalDate, statuses))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("day", resp[0]);
                    map.put("total", resp[1]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> totalOrdersByStatusGroupByCostCenter(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                OrderSqlFactory.totalsPurchaseOrdersByCostCenter(initialDate, finalDate, statuses))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("costCenter", resp[0]);
                    map.put("total", resp[1]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> totalOrdersByStatusGroupByCostCenterAndCompetence(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                OrderSqlFactory.totalsPurchaseOrdersByCostCenterAndCompetence(initialDate, finalDate, statuses))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("costCenter", resp[0]);
                    map.put("total", resp[1]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findMostPurchasedItems(LocalDate initialDate, LocalDate finalDate) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                OrderSqlFactory.findMostPurchasedItems(initialDate, finalDate))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("item", resp[0]);
                    map.put("countOrders", resp[1]);
                    map.put("total", resp[2]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findMostPurchasedItemsByCompetence(LocalDate initialDate, LocalDate finalDate) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                OrderSqlFactory.findMostPurchasedItemsByCompetence(initialDate, finalDate))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("item", resp[0]);
                    map.put("countOrders", resp[1]);
                    map.put("total", resp[2]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findMostPurchasedSuppliers(LocalDate initialDate, LocalDate finalDate) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                OrderSqlFactory.findMostPurchasedSuppliers(initialDate, finalDate))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("supplier", resp[0]);
                    map.put("countOrders", resp[1]);
                    map.put("total", resp[2]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findMostPurchasedSuppliersByCompetence(LocalDate initialDate, LocalDate finalDate) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                OrderSqlFactory.findMostPurchasedSuppliersByCompetence(initialDate, finalDate))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("supplier", resp[0]);
                    map.put("countOrders", resp[1]);
                    map.put("total", resp[2]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findMostQuotedItems(LocalDate initialDate, LocalDate finalDate) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                QuotationSqlFactory.findMostQuotedItems(initialDate, finalDate))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("item", resp[0]);
                    map.put("countQuotations", resp[1]);
                    map.put("total", resp[2]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> countAndTotalsQuotations(LocalDate initialDate, LocalDate finalDate, ProcessStatus statuses) {
        return (Map<String, Object>) entityManager.createNativeQuery(
                QuotationSqlFactory.countAndTotalsQuotations(initialDate, finalDate, statuses))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("status", statuses);
                    map.put("countQuotations", resp[0]);
                    map.put("total", resp[1]);

                    return map;
                })
                .findFirst()
                .orElse(null);

    }


    public List<Map<String, Object>> totalQuotationsByStatusGroupByDays(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                QuotationSqlFactory.totalsQuotationsGroupByDays(initialDate, finalDate, statuses))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("day", resp[0]);
                    map.put("total", resp[1]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> totalQuotationsByStatusGroupByCostCenter(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                QuotationSqlFactory.totalsQuotationsByCostCenter(initialDate, finalDate, statuses))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("costCenter", resp[0]);
                    map.put("total", resp[1]);

                    return map;
                })
                .collect(Collectors.toList());
    }
}
