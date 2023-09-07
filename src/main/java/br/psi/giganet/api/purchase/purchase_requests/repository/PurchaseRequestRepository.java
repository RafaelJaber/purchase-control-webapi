package br.psi.giganet.api.purchase.purchase_requests.repository;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

    List<PurchaseRequest> findByRequester(Employee requester);

    List<PurchaseRequest> findByStatus(ProcessStatus status);

}
