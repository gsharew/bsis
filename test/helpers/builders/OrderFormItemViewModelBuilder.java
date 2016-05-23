package helpers.builders;

import model.componenttype.ComponentType;
import viewmodel.ComponentTypeViewModel;
import viewmodel.OrderFormItemViewModel;

public class OrderFormItemViewModelBuilder extends AbstractBuilder<OrderFormItemViewModel> {

  private Long id;
  private ComponentTypeViewModel componentType;
  private String bloodGroup;
  private int numberOfUnits;

  public OrderFormItemViewModelBuilder withId(Long id) {
    this.id = id;
    return this;
  }

  public OrderFormItemViewModelBuilder withBloodGroup(String bloodGroup) {
    this.bloodGroup = bloodGroup;
    return this;
  }

  public OrderFormItemViewModelBuilder withNumberOfUnits(int numberOfUnits) {
    this.numberOfUnits = numberOfUnits;
    return this;
  }

  public OrderFormItemViewModelBuilder withComponentType(ComponentTypeViewModel componentType) {
    this.componentType = componentType;
    return this;
  }

  public OrderFormItemViewModel build() {
    OrderFormItemViewModel viewModel = new OrderFormItemViewModel();
    viewModel.setId(id);
    viewModel.setComponentType(componentType);
    viewModel.setBloodGroup(bloodGroup);
    viewModel.setNumberOfUnits(numberOfUnits);
    return viewModel;
  }

  public static OrderFormItemViewModelBuilder anOrderFormItemViewModel() {
    return new OrderFormItemViewModelBuilder();
  }

}
