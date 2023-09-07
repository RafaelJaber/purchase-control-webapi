package br.psi.giganet.api.purchase.dashboard.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DashboardData {

    private LocalDate initialDate;
    private LocalDate finalDate;

    private DashboardOrders orders;
    private DashboardQuotations quotations;

}
