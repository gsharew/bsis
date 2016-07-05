package org.jembi.bsis.controllerservice;

import static org.jembi.bsis.helpers.builders.LocationBuilder.aDistributionSite;
import static org.jembi.bsis.helpers.builders.OrderFormBackingFormBuilder.anOrderFormBackingForm;
import static org.jembi.bsis.helpers.builders.OrderFormBuilder.anOrderForm;
import static org.jembi.bsis.helpers.builders.OrderFormFullViewModelBuilder.anOrderFormFullViewModel;
import static org.jembi.bsis.helpers.matchers.OrderFormMatcher.hasSameStateAsOrderForm;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.jembi.bsis.backingform.OrderFormBackingForm;
import org.jembi.bsis.factory.OrderFormFactory;
import org.jembi.bsis.model.location.Location;
import org.jembi.bsis.model.order.OrderForm;
import org.jembi.bsis.service.OrderFormCRUDService;
import org.jembi.bsis.suites.UnitTestSuite;
import org.jembi.bsis.viewmodel.OrderFormFullViewModel;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

public class OrderFormControllerServiceTests extends UnitTestSuite {
  
  @InjectMocks
  private OrderFormControllerService orderFormControllerService;
  
  @Mock
  private OrderFormFactory orderFormFactory;
  
  @Mock
  private OrderFormCRUDService orderFormCRUDService;

  @Test
  public void testUpdateOrderFormWithoutStatusChange_shouldUpdateFieldsCorrectly() {
    // Fixture
    Long orderFormId = Long.valueOf(1);
    Date createdDate = new Date();
    Date orderDate = new Date();
    Location dispatchedFrom = aDistributionSite().build();
    Location dispatchedTo = aDistributionSite().build();

    OrderFormBackingForm backingForm = anOrderFormBackingForm().withId(orderFormId).build();
    OrderForm orderFormCreatedFromBackingForm = anOrderForm()
        .withId(orderFormId)
        .withOrderDate(orderDate)
        .withDispatchedFrom(dispatchedFrom)
        .withDispatchedTo(dispatchedTo)
        .build();
    OrderForm expectedOrderForm = anOrderForm()
        .withId(orderFormId)
        .withCreatedDate(createdDate)
        .withOrderDate(orderDate)
        .withDispatchedFrom(dispatchedFrom)
        .withDispatchedTo(dispatchedTo)
        .build();
    OrderFormFullViewModel expectedViewModel = anOrderFormFullViewModel().withId(orderFormId).build();
    
    // Expectations
    when(orderFormFactory.createEntity(backingForm)).thenReturn(orderFormCreatedFromBackingForm);
    when(orderFormCRUDService.updateOrderForm(orderFormCreatedFromBackingForm)).thenReturn(expectedOrderForm);
    when(orderFormFactory.createFullViewModel(argThat(hasSameStateAsOrderForm(expectedOrderForm)))).thenReturn(expectedViewModel);
    
    // Test
    orderFormControllerService.updateOrderForm(backingForm);
    
    // Assertions
    Mockito.verify(orderFormFactory).createEntity(backingForm);
    Mockito.verify(orderFormCRUDService).updateOrderForm(orderFormCreatedFromBackingForm);
    Mockito.verify(orderFormFactory).createFullViewModel(argThat(hasSameStateAsOrderForm(expectedOrderForm)));
  }
}
