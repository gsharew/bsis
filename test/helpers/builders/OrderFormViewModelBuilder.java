package helpers.builders;

import java.util.Date;

import model.location.Location;
import viewmodel.OrderFormViewModel;

public class OrderFormViewModelBuilder extends AbstractBuilder<OrderFormViewModel> {

  private Long id;
  private Date orderDate;
  private Location dispatchedFrom;
  private Location dispatchedTo;

  public OrderFormViewModelBuilder withId(Long id) {
    this.id = id;
    return this;
  }

  public OrderFormViewModelBuilder withOrderDate(Date orderDate) {
    this.orderDate = orderDate;
    return this;
  }

  public OrderFormViewModelBuilder withDispatchedFrom(Location dispatchedFrom) {
    this.dispatchedFrom = dispatchedFrom;
    return this;
  }

  public OrderFormViewModelBuilder withDispatchedTo(Location dispatchedTo) {
    this.dispatchedTo = dispatchedTo;
    return this;
  }

  public OrderFormViewModel build() {
    OrderFormViewModel backingForm = new OrderFormViewModel();
    backingForm.setId(id);
    backingForm.setDispatchedFrom(dispatchedFrom);
    backingForm.setDispatchedTo(dispatchedTo);
    backingForm.setOrderDate(orderDate);
    return backingForm;
  }

  public static OrderFormViewModelBuilder anOrderFormViewModel() {
    return new OrderFormViewModelBuilder();
  }

}
