package org.jembi.bsis.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jembi.bsis.helpers.builders.ComponentTypeBuilder.aComponentType;
import static org.jembi.bsis.helpers.builders.ComponentTypeCombinationBuilder.aComponentTypeCombination;
import static org.jembi.bsis.helpers.matchers.ComponentTypeCombinationMatcher.hasSameStateAsComponentTypeCombination;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.jembi.bsis.model.componenttype.ComponentType;
import org.jembi.bsis.model.componenttype.ComponentTypeCombination;
import org.jembi.bsis.model.componenttype.ComponentTypeTimeUnits;
import org.jembi.bsis.repository.ComponentTypeCombinationRepository;
import org.jembi.bsis.suites.UnitTestSuite;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ComponentTypeCombinationCRUDServiceTests extends UnitTestSuite {
  
  @InjectMocks
  private ComponentTypeCombinationCRUDService componentTypeCombinationCRUDService;
  
  @Mock
  private ComponentTypeCombinationRepository componentTypeCombinationRepository; 
  
  @Test
  public void testCreateComponentTypeCombination_shouldSaveCombinationCorrectly() {
    ComponentType sourceComponent = aComponentType()
        .withId(1L)
        .build();
        
    List<ComponentType> producedComponentTypes = Arrays.asList(
        aComponentType()
            .withId(2L)
            .build(),
        aComponentType()
            .withId(3L)
            .build()
    );
    
    ComponentTypeCombination expectedCombination = aComponentTypeCombination() 
        .withId(1L)
        .withCombinationName("CombinationName")
        .withSourceComponentTypes(new HashSet<>(Arrays.asList(sourceComponent)))
        .withComponentTypes(producedComponentTypes)
        .build();
  
    ComponentTypeCombination createComponentTypeCombination = componentTypeCombinationCRUDService
          .createComponentTypeCombination(expectedCombination);
    
    verify(componentTypeCombinationRepository).save(
        argThat(hasSameStateAsComponentTypeCombination(expectedCombination)));
    
    assertThat(createComponentTypeCombination, is(expectedCombination));
  }
 
  @Test
  public void testUpdateComponentTypeCombinations_shouldSetCorrectFieldsAndUpdate() {
    long componentCombinationId = 789L;
    ComponentType sourceComponent = aComponentType()
        .withId(1L)
        .build();
    List<ComponentType> producedComponentTypes = Arrays.asList(
        aComponentType()
            .withId(2L)
            .build(),
        aComponentType()
            .withId(3L)
            .build()
    );
    
    ComponentTypeCombination existingCombination = aComponentTypeCombination()
        .withId(componentCombinationId)
        .withCombinationName("combinationName")
        .withComponentTypes(producedComponentTypes)
        .withSourceComponentTypes(new HashSet<>(Arrays.asList(sourceComponent)))
        .build();
    
    ComponentTypeCombination updatedCombination = aComponentTypeCombination()
        .withId(componentCombinationId)
        .withCombinationName("UpdatedCombinationName")
        .withComponentTypes(producedComponentTypes)
        .withSourceComponentTypes(new HashSet<>(Arrays.asList(sourceComponent)))
        .build();
    
    when(componentTypeCombinationRepository.findComponentTypeCombinationById(componentCombinationId))
        .thenReturn(existingCombination);
        
    when(componentTypeCombinationRepository.update(existingCombination)).thenReturn(existingCombination);
        
    ComponentTypeCombination returnedCombination = componentTypeCombinationCRUDService
       .updateComponentTypeCombinations(updatedCombination);
             
    verify(componentTypeCombinationRepository).update(
        argThat(hasSameStateAsComponentTypeCombination(updatedCombination)));
        
    assertThat(returnedCombination, hasSameStateAsComponentTypeCombination(updatedCombination));
  }
}