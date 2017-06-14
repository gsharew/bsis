package org.jembi.bsis.service;

import static org.jembi.bsis.helpers.builders.GeneralConfigBuilder.aGeneralConfig;
import static org.jembi.bsis.helpers.builders.MimeMailMessageBuilder.aMimeMailMessage;
import static org.jembi.bsis.helpers.builders.UserBuilder.aUser;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.persistence.NoResultException;

import org.jembi.bsis.constant.GeneralConfigConstants;
import org.jembi.bsis.model.admin.GeneralConfig;
import org.jembi.bsis.model.user.User;
import org.jembi.bsis.repository.GeneralConfigRepository;
import org.jembi.bsis.repository.UserRepository;
import org.jembi.bsis.suites.UnitTestSuite;
import org.jembi.bsis.template.TemplateEngine;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.mail.javamail.MimeMailMessage;

public class PasswordResetServiceTests extends UnitTestSuite {

  private static final String TEST_EMAIL = "default.user@jembi.org";
  private static final String USERNAME = "Суперпользователь";
  private static final String EXPECTED_PASSWORD = "$2a$10$JP08kh2Cz2FF/1pMdkiUN.4r6CdOKFymCLeFGFU3P0ygqFZtkjteq";
  private static final String TEXT = "Ваш пароль был сброшен до  \"" + EXPECTED_PASSWORD 
      + "\". Вам потребуется изменить его при следующем входе в систему";
  private static final String BSIS_PASSWORD_RESET_MAIL_SUBJECT = "BSIS Password reset";

  @Spy
  @InjectMocks
  private PasswordResetService passwordResetService;
  @Mock
  private UserRepository userRepository;
  @Mock
  private BsisEmailSender bsisEmailSender;
  @Mock
  private GeneralConfigRepository generalConfigRepository;
  @Mock
  private TemplateEngine templateEngine;

  @Test
  public void tesResetUserPasswordWithLatinCharacters_shouldUpdateUserPassword() throws MessagingException, IOException {
    // Data
    String passwordResetName = "email.resetPassword.message";
    String passwordResetDescription = "Password reset email";
    
    Map<String, String> map = new HashMap<>();
    map.put("password", EXPECTED_PASSWORD);
    
    MimeMailMessage expectedMessage = aMimeMailMessage()
        .withTo(TEST_EMAIL)
        .withSubject(BSIS_PASSWORD_RESET_MAIL_SUBJECT)
        .withText(TEXT)
        .build();
    
    GeneralConfig passwordResetMessage = aGeneralConfig()
        .withName(passwordResetName)
        .withValue(TEXT)
        .withDescription(passwordResetDescription)
        .build();
    GeneralConfig passwordResetSubject= aGeneralConfig()
        .withName(passwordResetName)
        .withValue(BSIS_PASSWORD_RESET_MAIL_SUBJECT)
        .withDescription(passwordResetDescription)
        .build();
    
    // set up expectations
    User user = aUser().withUsername(USERNAME).withEmailId(TEST_EMAIL).withPasswordReset().build();
    when(userRepository.findUser(user.getUsername())).thenReturn(user);
    when(generalConfigRepository.getGeneralConfigByName(GeneralConfigConstants.PASSWORD_RESET_MESSAGE)).thenReturn(passwordResetMessage);
    when(generalConfigRepository.getGeneralConfigByName(GeneralConfigConstants.PASSWORD_RESET_SUBJECT)).thenReturn(passwordResetSubject);
    when(userRepository.updateUser(user, true)).thenAnswer(AdditionalAnswers.returnsFirstArg());
    when(bsisEmailSender.createMailMessage(TEST_EMAIL, BSIS_PASSWORD_RESET_MAIL_SUBJECT, TEXT)).thenReturn(expectedMessage);
    when(templateEngine.execute(passwordResetMessage.getName(), passwordResetMessage.getValue(), map)).thenReturn(TEXT);
    doReturn(EXPECTED_PASSWORD).when(passwordResetService).generateRandomPassword();
    doReturn(map).when(passwordResetService).getMapWithPassword(EXPECTED_PASSWORD); 
    doNothing().when(bsisEmailSender).sendEmail(any(MimeMailMessage.class));
    
    // Test
    passwordResetService.resetUserPassword(USERNAME);
    // verify
    verify(bsisEmailSender).sendEmail(expectedMessage);
    verify(userRepository).updateUser(user, true);
  }
  
  @Test(expected = NoResultException.class)
  public void testPasswordResetWithNoExistingUser_shouldThrow() throws IOException {
    String username = "anyUser";

    // mocks
    when(userRepository.findUser(anyString())).thenReturn(null);

    // Test
    passwordResetService.resetUserPassword(username);
  }
}
