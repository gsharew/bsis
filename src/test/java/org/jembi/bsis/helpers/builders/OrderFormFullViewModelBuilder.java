package org.jembi.bsis.helpers.builders;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jembi.bsis.model.order.OrderStatus;
import org.jembi.bsis.model.order.OrderType;
import org.jembi.bsis.viewmodel.ComponentViewModel;
import org.jembi.bsis.viewmodel.LocationViewModel;
import org.jembi.bsis.viewmodel.OrderFormFullViewModel;
import org.jembi.bsis.viewmodel.OrderFormItemViewModel;

public class OrderFormFullViewModelBuilder extends AbstractBuilder<OrderFormFullViewModel> {

  private Long id;
  private Date orderDate;
  private LocationViewModel dispatchedFrom;
  private LocationViewModel dispatchedTo;
  private OrderStatus status = OrderStatus.CREATED;
  private OrderType type = OrderType.ISSUE;
  private List<OrderFormItemViewModel> items = new ArrayList<>();
  private List<ComponentViewModel> components = new ArrayList<>();
  private Map<String, Boolean> permissions = new HashMap<>();

  public OrderFormFullViewModelBuilder withId(Long id) {
    this.id = id;
    return this;
  }

  public OrderFormFullViewModelBuilder withOrderDate(Date orderDate) {
    this.orderDate = orderDate;
    return this;
  }

  public OrderFormFullViewModelBuilder withDispatchedFrom(LocationViewModel dispatchedFrom) {
    this.dispatchedFrom = dispatchedFrom;
    return this;
  }

  public OrderFormFullViewModelBuilder withDispatchedTo(LocationViewModel dispatchedTo) {
    this.dispatchedTo = dispatchedTo;
    return this;
  }

  public OrderFormFullViewModelBuilder withOrderStatus(OrderStatus status) {
    this.status = status;
    return this;
  }

  public OrderFormFullViewModelBuilder withOrderType(OrderType type) {
    this.type = type;
    return this;
  }
  
  public OrderFormFullViewModelBuilder withItem(OrderFormItemViewModel item) {
    this.items.add(item);
    return this;
  }

  public OrderFormFullViewModelBuilder withComponent(ComponentViewModel component) {
    this.components.add(component);
    return this;
  }

  public OrderFormFullViewModelBuilder withPermission(String key, boolean value) {
    this.permissions.put(key, value);
    return this;
  }

  public OrderFormFullViewModel build() {
    OrderFormFullViewModel viewModel = new OrderFormFullViewModel();
    viewModel.setId(id);
    viewModel.setDispatchedFrom(dispatchedFrom);
    viewModel.setDispatchedTo(dispatchedTo);
    viewModel.setOrderDate(orderDate);
    viewModel.setStatus(status);
    viewModel.setType(type);
    viewModel.setItems(items);
    viewModel.setComponents(components);
    viewModel.setPermissions(permissions);
    return viewModel;
  }

  public static OrderFormFullViewModelBuilder anOrderFormFullViewModel() {
    return new OrderFormFullViewModelBuilder();
  }

}