package org.jembi.bsis.factory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jembi.bsis.helpers.matchers.BloodTestFullViewModelMatcher.hasSameStateAsBloodTestFullViewModel;
import static org.jembi.bsis.helpers.matchers.BloodTestViewModelMatcher.hasSameStateAsBloodTestViewModel;

import java.util.Arrays;
import java.util.List;

import org.jembi.bsis.helpers.builders.BloodTestBuilder;
import org.jembi.bsis.helpers.builders.BloodTestFullViewModelBuilder;
import org.jembi.bsis.helpers.builders.BloodTestViewModelBuilder;
import org.jembi.bsis.model.bloodtesting.BloodTest;
import org.jembi.bsis.suites.UnitTestSuite;
import org.jembi.bsis.viewmodel.BloodTestFullViewModel;
import org.jembi.bsis.viewmodel.BloodTestViewModel;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

public class BloodTestFactoryTests extends UnitTestSuite {
  
  @Spy
  @InjectMocks
  private BloodTestFactory bloodTestFactory;
  
  @Test
  public void testCreateFullViewModel_shouldReturnViewModelWithTheCorrectState() {
    // Set up fixture
    BloodTest bloodTest = BloodTestBuilder.aBasicBloodTypingBloodTest().withId(1L).withTestName("ABC test")
        .withTestNameShort("ABC").withValidResults("A,B,C,D").withPositiveResults("A,B,C").withNegativeResults("D")
        .withRankInCategory(1).build();

    // Set up expectations
    BloodTestFullViewModel expectedViewModel = BloodTestFullViewModelBuilder.aBasicBloodTypingBloodTestFullViewModel()
        .withId(1L).withTestName("ABC test").withTestNameShort("ABC").withValidResult("A").withValidResult("B")
        .withValidResult("C").withValidResult("D").withPositiveResult("A").withPositiveResult("B")
        .withPositiveResult("C").withNegativeResult("D").withRankInCategory(1).build();

    // Exercise SUT
    BloodTestFullViewModel returnedViewModel = bloodTestFactory.createFullViewModel(bloodTest);

    // Verify
    assertThat(returnedViewModel, hasSameStateAsBloodTestFullViewModel(expectedViewModel));
  }

  @Test
  public void testCreateFullViewModels_shouldReturnViewModelsWithTheCorrectState() {
    // Set up fixture
    List<BloodTest> bloodTests = Arrays.asList(BloodTestBuilder.aBasicBloodTypingBloodTest().withId(1L).build(),
        BloodTestBuilder.aBasicBloodTypingBloodTest().withId(2L).build());

    // Set up expectations
    List<BloodTestFullViewModel> expectedViewModels =
        Arrays.asList(BloodTestFullViewModelBuilder.aBasicBloodTypingBloodTestFullViewModel().withId(1L).build(),
            BloodTestFullViewModelBuilder.aBasicBloodTypingBloodTestFullViewModel().withId(2L).build());


    // Exercise SUT
    List<BloodTestFullViewModel> returnedViewModels = bloodTestFactory.createFullViewModels(bloodTests);

    // Verify
    assertThat("Correct number of view models returned", returnedViewModels.size(), is(2));
    assertThat(returnedViewModels.get(0), hasSameStateAsBloodTestFullViewModel(expectedViewModels.get(0)));
    assertThat(returnedViewModels.get(1), hasSameStateAsBloodTestFullViewModel(expectedViewModels.get(1)));
  }

  @Test
  public void testCreateViewModel_shouldReturnFullViewModelWithTheCorrectState() {
    // Set up fixture
    BloodTest bloodTest = BloodTestBuilder.aBasicBloodTypingBloodTest()
        .withId(1L)
        .withTestName("ABC test")
        .withTestNameShort("ABC")
        .withValidResults("A,B,C,D")
        .withPositiveResults("A,B,C")
        .withNegativeResults("D")
        .withRankInCategory(1)
        .build();
    
    // Set up expectations
    BloodTestViewModel expectedViewModel = BloodTestViewModelBuilder.aBasicBloodTypingBloodTestViewModel()
        .withId(1L)
        .withTestNameShort("ABC")
        .build();
    
    // Exercise SUT
    BloodTestViewModel returnedViewModel = bloodTestFactory.createViewModel(bloodTest);
    
    // Verify
    assertThat(returnedViewModel, hasSameStateAsBloodTestViewModel(expectedViewModel));
  }

  @Test
  public void testCreateViewModels_shouldReturnFullViewModelsWithTheCorrectState() {
    // Set up fixture
    List<BloodTest> bloodTests = Arrays.asList(
        BloodTestBuilder.aBasicBloodTypingBloodTest().withId(1L).build(), 
        BloodTestBuilder.aBasicBloodTypingBloodTest().withId(2L).build());

    // Set up expectations
    List<BloodTestViewModel> expectedViewModels = Arrays.asList(
        BloodTestViewModelBuilder.aBasicBloodTypingBloodTestViewModel().withId(1L).build(),
        BloodTestViewModelBuilder.aBasicBloodTypingBloodTestViewModel().withId(2L).build());

    // Exercise SUT
    List<BloodTestViewModel> returnedViewModels = bloodTestFactory.createViewModels(bloodTests);
    
    // Verify
    assertThat("Correct number of view models returned", returnedViewModels.size(), is(2));
    assertThat(returnedViewModels.get(0), hasSameStateAsBloodTestViewModel(expectedViewModels.get(0)));
    assertThat(returnedViewModels.get(1), hasSameStateAsBloodTestViewModel(expectedViewModels.get(1)));
  }
}

