package br.psi.giganet.api.purchase.purchase_order.repository;

import br.psi.giganet.api.purchase.branch_offices.model.BranchOffice;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.cost_center.model.CostCenter;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.locations.model.Location;
import br.psi.giganet.api.purchase.projects.model.Project;
import br.psi.giganet.api.purchase.purchase_order.model.PurchaseOrder;
import br.psi.giganet.api.purchase.purchase_order.model.PurchaseOrderCompetence;
import br.psi.giganet.api.purchase.purchase_order.model.PurchaseOrderFreight;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.AdvancedPurchaseOrderDTO;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.OrderWithQuotationAndCompetenciesDTO;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.PurchaseOrderCompetenceDTO;
import br.psi.giganet.api.purchase.purchase_order.repository.dto.PurchaseOrderWithQuotationDTO;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import br.psi.giganet.api.purchase.quotations.model.Quotation;
import br.psi.giganet.api.purchase.suppliers.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
public class AdvancedPurchaseOrderRepositoryImpl implements AdvancedPurchaseOrderRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<AdvancedPurchaseOrderDTO> findAllByAdvancedSearch(List<String> queries, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AdvancedPurchaseOrderDTO> criteria = builder.createQuery(AdvancedPurchaseOrderDTO.class);

        Root<PurchaseOrder> root = criteria.from(PurchaseOrder.class);
        Join<QuotationApproval, PurchaseOrder> approval = root.join("approval", JoinType.INNER);
        Join<Quotation, QuotationApproval> quotation = approval.join("quotation", JoinType.INNER);
        Join<Supplier, PurchaseOrder> supplier = root.join("supplier", JoinType.INNER);
        Join<BranchOffice, PurchaseOrder> office = root.join("branchOffice", JoinType.INNER);
        Join<Employee, PurchaseOrder> responsible = root.join("responsible", JoinType.INNER);
        Join<Project, PurchaseOrder> project = root.join("project", JoinType.LEFT);
        Join<Location, PurchaseOrder> location = root.join("location", JoinType.LEFT);

        criteria.multiselect(
                root.get("id").alias("id"),
                approval.get("id").alias("approval"),
                quotation.get("id").alias("quotation"),
                quotation.get("description").alias("description"),
                root.get("status").alias("status"),
                root.get("lastModifiedDate").alias("lastModifiedDate"),
                root.get("total").alias("total"),
                supplier.get("id").alias("supplierId"),
                supplier.get("name").alias("supplierName"),
                office.get("shortName").alias("branchOffice"),
                responsible.get("id").alias("responsibleId"),
                responsible.get("name").alias("responsibleName"));

        if (queries != null && !queries.isEmpty()) {
            List<Predicate> predicateList = queries.stream()
                    .map(query -> {
                        Predicate predicate = getAdvancedPredicate(query, root, approval, quotation, supplier, office,
                                responsible, project, location, builder, criteria);
                        if (predicate == null) {
                            throw new IllegalArgumentException("Consulta inválida. '" + query + "'");
                        }
                        return predicate;
                    })
                    .collect(Collectors.toList());
            Predicate[] queryPredicates = new Predicate[predicateList.size()];
            Predicate queryAndPredicates = builder.and(predicateList.toArray(queryPredicates));

            criteria.where(queryAndPredicates);

        }

        criteria.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

        TypedQuery<AdvancedPurchaseOrderDTO> query = entityManager.createQuery(criteria);
        query.setMaxResults(pageable.getPageSize());
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        return new PageImpl<>(
                query.getResultList(),
                pageable,
                countFindAllByAdvancedSearch(queries));
    }

    private Long countFindAllByAdvancedSearch(List<String> queries) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);

        Root<PurchaseOrder> root = criteria.from(PurchaseOrder.class);
        Join<QuotationApproval, PurchaseOrder> approval = root.join("approval", JoinType.INNER);
        Join<Quotation, QuotationApproval> quotation = approval.join("quotation", JoinType.INNER);
        Join<Supplier, PurchaseOrder> supplier = root.join("supplier", JoinType.INNER);
        Join<BranchOffice, PurchaseOrder> office = root.join("branchOffice", JoinType.INNER);
        Join<Employee, PurchaseOrder> responsible = root.join("responsible", JoinType.INNER);
        Join<Project, PurchaseOrder> project = root.join("project", JoinType.LEFT);
        Join<Location, PurchaseOrder> location = root.join("location", JoinType.LEFT);

        if (queries != null && !queries.isEmpty()) {
            List<Predicate> predicateList = queries.stream()
                    .map(query -> {
                        Predicate predicate = getAdvancedPredicate(query, root, approval, quotation, supplier, office, responsible, project, location, builder, criteria);
                        if (predicate == null) {
                            throw new IllegalArgumentException("Consulta inválida. '" + query + "'");
                        }
                        return predicate;
                    })
                    .collect(Collectors.toList());
            Predicate[] queryPredicates = new Predicate[predicateList.size()];
            Predicate queryAndPredicates = builder.and(predicateList.toArray(queryPredicates));

            criteria.where(queryAndPredicates);

        }

        criteria.select(builder.count(root));
        return entityManager.createQuery(criteria).getSingleResult();
    }

    private Predicate getAdvancedPredicate(
            String query, Root<PurchaseOrder> root,
            Join<QuotationApproval, PurchaseOrder> approval,
            Join<Quotation, QuotationApproval> quotation,
            Join<Supplier, PurchaseOrder> supplier,
            Join<BranchOffice, PurchaseOrder> office,
            Join<Employee, PurchaseOrder> responsible,
            Join<Project, PurchaseOrder> project,
            Join<Location, PurchaseOrder> location,
            CriteriaBuilder builder,
            CriteriaQuery<?> criteria) {


        final String operators = "[:<>~]";
        Pattern pattern = Pattern.compile("[A-Za-z]+" + operators + "\\X*");

        Subquery<PurchaseOrder> competenceSubquery = criteria.subquery(PurchaseOrder.class);
        Root<PurchaseOrderCompetence> competence = competenceSubquery.from(PurchaseOrderCompetence.class);
        Join<PurchaseOrder, PurchaseOrderCompetence> orderJoin = competence.join("order", JoinType.INNER);
        Join<CostCenter, PurchaseOrderCompetence> competenceCostCenter = competence.join("costCenter", JoinType.LEFT);
        competenceSubquery.select(orderJoin.get("id"));

        if (pattern.matcher(query).matches()) {
            String[] fields = query.split(operators);
            final String key = fields[0];
            final String value = fields.length > 1 ? fields[1] : "";
            final String operator = query.replaceAll("(" + key + "|" + value + ")", "");

            switch (key) {
                case "id":
                    return builder.equal(root.get("id"), value);

                case "quotationApproval":
                    return builder.equal(approval.get("id"), value);

                case "quotation":
                    return builder.equal(quotation.get("id"), value);

                case "description":
                    return builder.like(builder.upper(quotation.get("description")), "%" + value.toUpperCase() + "%");

                case "responsible":
                    return builder.like(builder.upper(responsible.get("name")), "%" + value.toUpperCase() + "%");

                case "supplier":
                    return builder.like(builder.upper(supplier.get("name")), "%" + value.toUpperCase() + "%");

                case "branchOffice":
                    return builder.like(builder.upper(office.get("name")), "%" + value.toUpperCase() + "%");

                case "project":
                    return builder.like(builder.upper(project.get("name")), "%" + value.toUpperCase() + "%");

                case "location":
                    return builder.like(builder.upper(location.get("name")), "%" + value.toUpperCase() + "%");

                case "createdDate":
                    return operator.equals(">") ? builder.greaterThanOrEqualTo(
                            root.get("createdDate").as(LocalDate.class), LocalDate.parse(value)) :
                            operator.equals("<") ? builder.lessThanOrEqualTo(
                                    root.get("createdDate").as(LocalDate.class), LocalDate.parse(value)) :
                                    null;

                case "lastModifiedDate":
                    return operator.equals(">") ? builder.greaterThanOrEqualTo(
                            root.get("lastModifiedDate").as(LocalDate.class), LocalDate.parse(value)) :
                            operator.equals("<") ? builder.lessThanOrEqualTo(
                                    root.get("lastModifiedDate").as(LocalDate.class), LocalDate.parse(value)) :
                                    null;

                case "status":
                    return builder.equal(root.get("status"), ProcessStatus.valueOf(value));

                case "competence":
                    competenceSubquery.where(builder.equal(competence.get("date"), LocalDate.parse(value)));
                    return builder.in(root.get("id")).value(competenceSubquery);

                case "costCenter":
                    competenceSubquery.where(builder.like(builder.upper(competenceCostCenter.get("name")), "%" + value.toUpperCase() + "%"));
                    return builder.in(root.get("id")).value(competenceSubquery);

                default:
                    return null;

            }

        }

        return null;
    }

    @Override
    public List<OrderWithQuotationAndCompetenciesDTO> findAllWithQuotationAndCompetencies() {
        List<PurchaseOrderWithQuotationDTO> orders = findAllWithQuotation(Sort.by(Sort.Direction.DESC, "createdDate"));
        List<PurchaseOrderCompetenceDTO> competencies = findAllCompetenciesByPurchaseOrders(
                orders.stream()
                        .map(PurchaseOrderWithQuotationDTO::getId)
                        .collect(Collectors.toList()));

        return orders.stream()
                .map(order -> {
                    final var dto = new OrderWithQuotationAndCompetenciesDTO();
                    dto.setApproval(order.getApproval());
                    dto.setCompetencies(competencies.stream()
                            .filter(c -> order.getId().equals(c.getOrderId()))
                            .map(PurchaseOrderCompetenceDTO::getDate)
                            .collect(Collectors.toList()));
                    dto.setDeliveryDate(order.getDeliveryDate());
                    dto.setDescription(order.getDescription());
                    dto.setId(order.getId());
                    dto.setQuotation(order.getQuotation());
                    dto.setResponsibleId(order.getResponsibleId());
                    dto.setResponsibleName(order.getResponsibleName());
                    dto.setStatus(order.getStatus());
                    dto.setSupplierId(order.getSupplierId());
                    dto.setSupplierName(order.getSupplierName());
                    dto.setBranchOfficeId(order.getBranchOfficeId());
                    dto.setBranchOfficeName(order.getBranchOfficeName());
                    dto.setBranchOfficeShortName(order.getBranchOfficeShortName());
                    dto.setTotal(order.getTotal());

                    return dto;
                })
                .collect(Collectors.toList());

    }

    @Override
    public List<PurchaseOrderWithQuotationDTO> findAllWithQuotation(Sort sort) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PurchaseOrderWithQuotationDTO> criteria = builder.createQuery(PurchaseOrderWithQuotationDTO.class);

        Root<PurchaseOrder> root = criteria.from(PurchaseOrder.class);
        Join<QuotationApproval, PurchaseOrder> approval = root.join("approval", JoinType.INNER);
        Join<Quotation, QuotationApproval> quotation = approval.join("quotation", JoinType.INNER);
        Join<PurchaseOrderFreight, PurchaseOrder> freight = root.join("freight", JoinType.INNER);
        Join<Supplier, PurchaseOrder> supplier = root.join("supplier", JoinType.INNER);
        Join<BranchOffice, PurchaseOrder> office = root.join("branchOffice", JoinType.INNER);
        Join<Employee, PurchaseOrder> responsible = root.join("responsible", JoinType.INNER);

        criteria.multiselect(
                root.get("id").alias("id"),
                approval.get("id").alias("approval"),
                quotation.get("id").alias("quotation"),
                quotation.get("description").alias("description"),
                root.get("status").alias("status"),
                freight.get("deliveryDate").alias("deliveryDate"),
                root.get("createdDate").alias("createdDate"),
                root.get("lastModifiedDate").alias("lastModifiedDate"),
                root.get("total").alias("total"),
                supplier.get("id").alias("supplierId"),
                supplier.get("name").alias("supplierName"),
                office.get("id").alias("branchOfficeId"),
                office.get("name").alias("branchOfficeName"),
                office.get("shortName").alias("branchOfficeShortName"),
                responsible.get("id").alias("responsibleId"),
                responsible.get("name").alias("responsibleName"));

        criteria.orderBy(QueryUtils.toOrders(sort, root, builder));

        return entityManager.createQuery(criteria).getResultList();
    }

    private List<PurchaseOrderCompetenceDTO> findAllCompetenciesByPurchaseOrders(List<Long> orders) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PurchaseOrderCompetenceDTO> criteria = builder.createQuery(PurchaseOrderCompetenceDTO.class);

        Root<PurchaseOrderCompetence> root = criteria.from(PurchaseOrderCompetence.class);
        Join<PurchaseOrder, PurchaseOrderCompetence> order = root.join("order", JoinType.INNER);

        criteria.where(order.get("id").in(orders));

        criteria.multiselect(
                root.get("id").alias("id"),
                root.get("date").alias("date"),
                order.get("id").alias("orderId"));

        return entityManager.createQuery(criteria).getResultList();
    }

}
