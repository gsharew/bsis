package org.jembi.bsis.helpers.builders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jembi.bsis.model.componenttype.ComponentType;
import org.jembi.bsis.model.componenttype.ComponentTypeCombination;

public class ComponentTypeCombinationBuilder  extends AbstractEntityBuilder<ComponentTypeCombination> {
  
  private Long id;
  private boolean isDeleted = false;
  private String combinationName;
  private List<ComponentType> componentTypes;
  private Set<ComponentType> sourceComponentTypes;

  public ComponentTypeCombinationBuilder withId(Long id) {
    this.id = id;
    return this;
  }

  public ComponentTypeCombinationBuilder withCombinationName(String combinationName) {
    this.combinationName = combinationName;
    return this;
  }

  public ComponentTypeCombinationBuilder withComponentTypes(List<ComponentType> componentTypes) {
    this.componentTypes = componentTypes;
    return this;
  }

  public ComponentTypeCombinationBuilder withComponentType(ComponentType componentType) {
    if (this.componentTypes == null) {
      this.componentTypes = new ArrayList<>();
    }
    this.componentTypes.add(componentType);
    return this;
  }

  public ComponentTypeCombinationBuilder withSourceComponentTypes(Set<ComponentType> sourceComponentTypes) {
    this.sourceComponentTypes = sourceComponentTypes;
    return this;
  }

  public ComponentTypeCombinationBuilder withSourceComponentType(ComponentType sourceComponentType) {
    if (this.sourceComponentTypes == null) {
      this.sourceComponentTypes = new HashSet<>();
    }
    this.sourceComponentTypes.add(sourceComponentType);
    return this;
  }
  
  public ComponentTypeCombinationBuilder withSourceComponents(Set<ComponentType> sourceComponentTypes) {
    this.sourceComponentTypes = sourceComponentTypes;
    return this;
  }
  
  public ComponentTypeCombinationBuilder thatIsDeleted() {
    this.isDeleted = true;
    return this;
  }
  
  @Override
  public ComponentTypeCombination build() {
    ComponentTypeCombination componentTypeCombination = new ComponentTypeCombination();
    componentTypeCombination.setId(id);
    componentTypeCombination.setCombinationName(combinationName);
    componentTypeCombination.setComponentTypes(componentTypes);
    componentTypeCombination.setSourceComponentTypes(sourceComponentTypes);
    componentTypeCombination.setIsDeleted(isDeleted);
    componentTypeCombination.setComponentTypes(componentTypes);
    componentTypeCombination.setSourceComponentTypes(sourceComponentTypes);
    return componentTypeCombination;
  }
  
  public static ComponentTypeCombinationBuilder aComponentTypeCombination() {
    return new ComponentTypeCombinationBuilder();
  }
}
