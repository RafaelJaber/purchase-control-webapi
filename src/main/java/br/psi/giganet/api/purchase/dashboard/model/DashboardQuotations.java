package br.psi.giganet.api.purchase.dashboard.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardQuotations {

    private Map<String, Object> totalsPending;
    private Map<String, Object> totalsRealized;
    private Map<String, Object> totalsApproved;
    private Map<String, Object> totalsRejected;
    private List<Map<String, Object>> totalsByCostCenter;
    private List<Map<String, Object>> totalsGroupByDay;

}
