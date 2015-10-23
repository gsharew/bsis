package service;

import static helpers.builders.BloodTestBuilder.aBloodTest;
import static helpers.builders.BloodTestResultBuilder.aBloodTestResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import model.bloodtesting.BloodTestResult;
import model.bloodtesting.BloodTestType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import constant.GeneralConfigConstants;

@RunWith(MockitoJUnitRunner.class)
public class DonorDeferralStatusCalculatorTests {

    @InjectMocks
    private DonorDeferralStatusCalculator donorDeferralStatusCalculator;
    @Mock
    private GeneralConfigAccessorService generalConfigAccessorService;

    @Test
    public void testShouldDonorBeDeferredWithNonConfirmatoryResult_shouldReturnFalse() {
        List<BloodTestResult> bloodTestResults = Arrays.asList(
                aBloodTestResult()
                        .withResult("POS")
                        .withBloodTest(aBloodTest()
                                .withBloodTestType(BloodTestType.BASIC_TTI)
                                .withPositiveResults("POS,+")
                                .build())
                        .build()
        );
        
        when(generalConfigAccessorService.getBooleanValue(GeneralConfigConstants.DEFER_DONORS_WITH_NEG_CONFIRMATORY_OUTCOMES))
                .thenReturn(false);

        boolean returnedValue = donorDeferralStatusCalculator.shouldDonorBeDeferred(bloodTestResults);

        assertThat(returnedValue, is(false));
    }

    @Test
    public void testShouldDonorBeDeferredWithNegativeConfirmatoryResult_shouldReturnFalse() {
        List<BloodTestResult> bloodTestResults = Arrays.asList(
                aBloodTestResult()
                        .withResult("NEG")
                        .withBloodTest(aBloodTest()
                                .withBloodTestType(BloodTestType.CONFIRMATORY_TTI)
                                .withPositiveResults("POS,+")
                                .build())
                        .build()
        );
        
        when(generalConfigAccessorService.getBooleanValue(GeneralConfigConstants.DEFER_DONORS_WITH_NEG_CONFIRMATORY_OUTCOMES))
                .thenReturn(false);

        boolean returnedValue = donorDeferralStatusCalculator.shouldDonorBeDeferred(bloodTestResults);

        assertThat(returnedValue, is(false));
    }

    @Test
    public void testShouldDonorBeDeferredWithPositiveConfirmatoryResult_shouldReturnTrue() {
        List<BloodTestResult> bloodTestResults = Arrays.asList(
                aBloodTestResult()
                        .withResult("POS")
                        .withBloodTest(aBloodTest()
                                .withBloodTestType(BloodTestType.CONFIRMATORY_TTI)
                                .withPositiveResults("POS,+")
                                .build())
                        .build()
        );

        boolean returnedValue = donorDeferralStatusCalculator.shouldDonorBeDeferred(bloodTestResults);

        assertThat(returnedValue, is(true));
    }

}