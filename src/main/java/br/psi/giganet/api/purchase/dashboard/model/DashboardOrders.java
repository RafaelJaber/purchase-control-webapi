package br.psi.giganet.api.purchase.dashboard.model;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Data
public class DashboardOrders {

    private BigInteger count;
    private BigDecimal totalsRealized;
    private BigDecimal totalsPending;
    private BigDecimal totalsInTransit;
    private List<Map<String, Object>> orders;
    private List<Map<String, Object>> realizedOrders;
    private List<Map<String, Object>> pendingOrders;
    private List<Map<String, Object>> inTransitOrders;
    private List<Map<String, Object>> totalsByCostCenter;
    private List<Map<String, Object>> totalsGroupByDay;
    private List<Map<String, Object>> mostPurchasedItems;
    private List<Map<String, Object>> mostPurchasedSuppliers;

}
