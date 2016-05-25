package helpers.builders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.order.OrderStatus;
import model.order.OrderType;
import viewmodel.ComponentViewModel;
import viewmodel.LocationViewModel;
import viewmodel.OrderFormItemViewModel;
import viewmodel.OrderFormViewModel;

public class OrderFormViewModelBuilder extends AbstractBuilder<OrderFormViewModel> {

  private Long id;
  private Date orderDate;
  private LocationViewModel dispatchedFrom;
  private LocationViewModel dispatchedTo;
  private OrderStatus status = OrderStatus.CREATED;
  private OrderType type = OrderType.ISSUE;
  private List<OrderFormItemViewModel> items = new ArrayList<>();
  private List<ComponentViewModel> components = new ArrayList<>();

  public OrderFormViewModelBuilder withId(Long id) {
    this.id = id;
    return this;
  }

  public OrderFormViewModelBuilder withOrderDate(Date orderDate) {
    this.orderDate = orderDate;
    return this;
  }

  public OrderFormViewModelBuilder withDispatchedFrom(LocationViewModel dispatchedFrom) {
    this.dispatchedFrom = dispatchedFrom;
    return this;
  }

  public OrderFormViewModelBuilder withDispatchedTo(LocationViewModel dispatchedTo) {
    this.dispatchedTo = dispatchedTo;
    return this;
  }

  public OrderFormViewModelBuilder withOrderStatus(OrderStatus status) {
    this.status = status;
    return this;
  }

  public OrderFormViewModelBuilder withOrderType(OrderType type) {
    this.type = type;
    return this;
  }
  
  public OrderFormViewModelBuilder withItem(OrderFormItemViewModel item) {
    this.items.add(item);
    return this;
  }

  public OrderFormViewModelBuilder withComponent(ComponentViewModel component) {
    this.components.add(component);
    return this;
  }

  public OrderFormViewModel build() {
    OrderFormViewModel viewModel = new OrderFormViewModel();
    viewModel.setId(id);
    viewModel.setDispatchedFrom(dispatchedFrom);
    viewModel.setDispatchedTo(dispatchedTo);
    viewModel.setOrderDate(orderDate);
    viewModel.setStatus(status);
    viewModel.setType(type);
    viewModel.setItems(items);
    viewModel.setComponents(components);
    return viewModel;
  }

  public static OrderFormViewModelBuilder anOrderFormViewModel() {
    return new OrderFormViewModelBuilder();
  }

}
