package controller;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

 /**
  * 
  * Exception to be thrown when validation on an argument annotated with @Valid fails.
  */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException errors) {
    Map<String, String> errorMap = new HashMap<String, String>();
    errorMap.put("hasErrors", "true");
    errorMap.put("errorMessage", errors.getMessage());
    errors.printStackTrace();
    for (FieldError error : errors.getBindingResult().getFieldErrors()) {
        errorMap.put(error.getField(), error.getDefaultMessage());
    }
    return new ResponseEntity<Map<String, String>>(errorMap, HttpStatus.BAD_REQUEST);
  }
  
 /**
  * thrown at flush or commit time for detached entities 
  */
  @ExceptionHandler(PersistenceException.class)
  public ResponseEntity<Map<String, String>> handlePersistenceException(
        PersistenceException errors) {
    Map<String, String> errorMap = new HashMap<String, String>();
    errorMap.put("hasErrors", "true");
    errorMap.put("errorMessage", errors.getMessage());
    errors.printStackTrace();
    return new ResponseEntity<Map<String, String>>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
  }
  
 /**
  * Thrown by the persistence provider when getSingleResult() is executed on a query
    and there is no result to return.
  */
  @ExceptionHandler(NoResultException.class)
  public ResponseEntity<Map<String, String>> handleNoResultException(
        NoResultException errors) {
    Map<String, String> errorMap = new HashMap<String, String>();
    errorMap.put("hasErrors", "true");
    errorMap.put("errorMessage", errors.getMessage());
    errors.printStackTrace();
    return new ResponseEntity<Map<String, String>>(errorMap, HttpStatus.NOT_FOUND);
  }
  
 /**
    * Thrown when the application calls Query.uniqueResult() and the query 
    * returned more than one result. Unlike all other Hibernate exceptions, this one is recoverable!
  */
  @ExceptionHandler(NonUniqueResultException .class)
  public ResponseEntity<Map<String, String>> handleNonUniqueResultException (
        NoResultException errors) {
    Map<String, String> errorMap = new HashMap<String, String>();
    errorMap.put("hasErrors", "true");
    errorMap.put("errorMessage", errors.getMessage());
    errors.printStackTrace();
    return new ResponseEntity<Map<String, String>>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
  }
  
  //Service layer Exceptions
  
  /**
  *  Exception thrown on a type mismatch when trying to set a bean property.
  */
  @ExceptionHandler(NoSuchRequestHandlingMethodException.class)
  public ResponseEntity<Map<String, String>> handleNoSuchRequestHandlingMethodException(
        NoSuchRequestHandlingMethodException error) {
    Map<String, String> errorMap = new HashMap<String, String>();
    errorMap.put("hasErrors", "true");
    errorMap.put("errorMessage", error.getMessage());
    error.printStackTrace();
    return new ResponseEntity<Map<String, String>>(errorMap, HttpStatus.NOT_FOUND);
  }
  
  /**
  *   Exception thrown when a request handler does not support a specific request method.
  */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Map<String, String>> handleHttpRequestMethodNotSupportedException(
        NoSuchRequestHandlingMethodException error) {
    Map<String, String> errorMap = new HashMap<String, String>();
    errorMap.put("hasErrors", "true");
    errorMap.put("errorMessage", error.getMessage());
    error.printStackTrace();
    return new ResponseEntity<Map<String, String>>(errorMap, HttpStatus.METHOD_NOT_ALLOWED);
  }
  
  /**
  *  Exception thrown when a client POSTs, PUTs, or PATCHes content of a type not supported   by request handler.
  */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<Map<String, String>> handleHttpMediaTypeNotSupportedException(
        HttpMediaTypeNotSupportedException error) {
    Map<String, String> errorMap = new HashMap<String, String>();
    errorMap.put("hasErrors", "true");
    errorMap.put("errorMessage", error.getMessage());
    error.printStackTrace();
    return new ResponseEntity<Map<String, String>>(errorMap, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
  }
  
  /**
  *  indicates a missing parameter.
  */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, String>> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException error) {
    Map<String, String> errorMap = new HashMap<String, String>();
    errorMap.put("hasErrors", "true");
    errorMap.put("errorMessage", error.getMessage());
    error.printStackTrace();
    return new ResponseEntity<Map<String, String>>(errorMap, HttpStatus.BAD_REQUEST);
  }
  
  /**
  *   Exception thrown when no suitable editor or converter can be found for a bean property.
  */
  @ExceptionHandler(ConversionNotSupportedException.class)
  public ResponseEntity<Map<String, String>> handleConversionNotSupportedException(
        ConversionNotSupportedException error) {
    Map<String, String> errorMap = new HashMap<String, String>();
    errorMap.put("hasErrors", "true");
    errorMap.put("errorMessage", error.getMessage());
    error.printStackTrace();
    return new ResponseEntity<Map<String, String>>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
  }
  
  /**
  *  Exception thrown on a type mismatch when trying to set a bean property.
  */
  @ExceptionHandler(TypeMismatchException.class)
  public ResponseEntity<Map<String, String>> TypeMismatchException(
        TypeMismatchException error) {
    Map<String, String> errorMap = new HashMap<String, String>();
    errorMap.put("hasErrors", "true");
    errorMap.put("errorMessage", error.getMessage());
    error.printStackTrace();
    return new ResponseEntity<Map<String, String>>(errorMap, HttpStatus.BAD_GATEWAY);
  }
  
  
}
