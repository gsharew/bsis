package org.jembi.bsis.service;

import javax.transaction.Transactional;

import org.jembi.bsis.model.component.Component;
import org.jembi.bsis.model.order.OrderForm;
import org.jembi.bsis.model.order.OrderStatus;
import org.jembi.bsis.model.order.OrderType;
import org.jembi.bsis.repository.OrderFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class OrderFormCRUDService {

  @Autowired
  private OrderFormRepository orderFormRepository;
  
  @Autowired
  private ComponentDispatchService componentDispatchService;
  
  @Autowired
  private OrderFormConstraintChecker orderFormConstraintChecker;
  
  public OrderForm updateOrderForm(OrderForm updatedOrderForm) {
    OrderForm existingOrderForm = orderFormRepository.findById(updatedOrderForm.getId());
    
    // Check that OrderForm can be edited
    if (!orderFormConstraintChecker.canEdit(existingOrderForm)) {
      throw new IllegalStateException("Cannot edit OrderForm.");
    }

    // If the order is being dispatched then transfer or issue each component
    if (updatedOrderForm.getStatus() == OrderStatus.DISPATCHED) {

      // Check that OrderForm can be dispatched
      if (!orderFormConstraintChecker.canDispatch(existingOrderForm)) {
        throw new IllegalStateException("Cannot dispatch OrderForm");
      }

      for (Component component : updatedOrderForm.getComponents()) {
        if (updatedOrderForm.getType() == OrderType.ISSUE) {
          componentDispatchService.issueComponent(component, updatedOrderForm.getDispatchedTo());
        } else if (updatedOrderForm.getType() == OrderType.TRANSFER) {
          componentDispatchService.transferComponent(component, updatedOrderForm.getDispatchedTo());
        }
      }
    }

    existingOrderForm.setOrderDate(updatedOrderForm.getOrderDate());
    existingOrderForm.setStatus(updatedOrderForm.getStatus());
    existingOrderForm.setType(updatedOrderForm.getType());
    existingOrderForm.setDispatchedFrom(updatedOrderForm.getDispatchedFrom());
    existingOrderForm.setDispatchedTo(updatedOrderForm.getDispatchedTo());
    existingOrderForm.setItems(updatedOrderForm.getItems());
    existingOrderForm.setComponents(updatedOrderForm.getComponents());
    return orderFormRepository.update(existingOrderForm);
  }
}
