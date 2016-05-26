package helpers.builders;

import backingform.ComponentBackingForm;

public class ComponentBackingFormBuilder {

  private Long id;
  
  public ComponentBackingFormBuilder withId(Long id) {
    this.id = id;
    return this;
  }

  public ComponentBackingForm build() {
    ComponentBackingForm backingForm = new ComponentBackingForm();
    backingForm.setId(id);
    return backingForm;
  }

  public static ComponentBackingFormBuilder aComponentBackingForm() {
    return new ComponentBackingFormBuilder();
  }

}