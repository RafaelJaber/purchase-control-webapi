package br.psi.giganet.api.purchase.config.exception.controller;

import br.psi.giganet.api.purchase.common.messages.service.LogMessageService;
import br.psi.giganet.api.purchase.common.utils.model.WebRequestProjection;
import br.psi.giganet.api.purchase.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.config.exception.exception.UnauthenticatedException;
import br.psi.giganet.api.purchase.config.exception.response.SimpleErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@ControllerAdvice
public class ErrorHandlingController extends ResponseEntityExceptionHandler {

    @Autowired
    private LogMessageService logService;

    private ObjectMapper mapper = new ObjectMapper();

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) throws IOException {
        SimpleErrorResponse response = new SimpleErrorResponse(ex.getMessage());
        logService.send(mapper.writeValueAsString(response));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler({MailException.class})
    public ResponseEntity<Object> handleMailException(MailException ex) throws IOException {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Um erro interno ocorreu durante o envio de email");
        errors.put("description", ex.getLocalizedMessage());
        ex.printStackTrace();
        logService.send(mapper.writeValueAsString(errors));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleErrorResponse("Um erro ocorreu durante o envio do email"));
    }

    @ExceptionHandler({MessagingException.class})
    public ResponseEntity<Object> handleMessagingException(MessagingException ex) throws IOException {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Um erro interno ocorreu durante o envio de email");
        errors.put("description", ex.getLocalizedMessage());
        ex.printStackTrace();
        logService.send(mapper.writeValueAsString(errors));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleErrorResponse("Um erro ocorreu durante o envio do email"));
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> handleRuntimeException(IllegalArgumentException ex) throws IOException {
        SimpleErrorResponse response = new SimpleErrorResponse(ex.getMessage());
        logService.send(mapper.writeValueAsString(response));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler({DateTimeParseException.class})
    public ResponseEntity<Object> handleDateTimeParseException(DateTimeParseException ex, WebRequest request) throws IOException {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "DateTimeParseException");
        errors.put("description", ex.getLocalizedMessage());
        errors.put("request", new WebRequestProjection(request));
        logService.send(mapper.writeValueAsString(errors));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new SimpleErrorResponse("A data informada não está em um formato válido."));
    }

    @ExceptionHandler({DataAccessException.class})
    public ResponseEntity<Object> handleSqlException(DataAccessException ex, WebRequest request) throws IOException {
        String responseMessage = "Não foi possível salvar o recurso. Erro do banco de dados. ";
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Erro SQL - DataAccessException");
        errors.put("request", new WebRequestProjection(request));
        if (ex.getMostSpecificCause() instanceof SQLException) {
            SQLException sqlException = (SQLException) ex.getMostSpecificCause();
            errors.put("code", sqlException.getSQLState());
            errors.put("description", sqlException.getLocalizedMessage());
            responseMessage += "Codigo " + sqlException.getSQLState();
        } else {
            errors.put("description", ex.getLocalizedMessage());
        }
        SimpleErrorResponse response = new SimpleErrorResponse(responseMessage);
        logService.send(mapper.writeValueAsString(errors));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(
            Exception ex, WebRequest request) throws IOException {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Acesso não autorizado");
        errors.put("description", ex.getLocalizedMessage());
        errors.put("request", new WebRequestProjection(request));
        logService.send(mapper.writeValueAsString(errors));

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new SimpleErrorResponse("Você não tem permissões suficientes para realizar esta operação"));
    }

    @ExceptionHandler({UnauthenticatedException.class})
    public ResponseEntity<Object> handleUnauthenticatedException(
            Exception ex, WebRequest request) throws IOException {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Acesso não autorizado");
        errors.put("description", ex.getLocalizedMessage());
        errors.put("request", new WebRequestProjection(request));
        logService.send(mapper.writeValueAsString(errors));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new SimpleErrorResponse("Acesso não autorizado"));
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) throws IOException {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Um erro interno ocorreu");
        errors.put("description", ex.getLocalizedMessage());
        errors.put("request", new WebRequestProjection(request));
        ex.printStackTrace();
        logService.send(mapper.writeValueAsString(errors));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleErrorResponse("Um erro interno ocorreu"));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", status.value());

        //Get all errors
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        body.put("errors", errors);

        return new ResponseEntity<>(body, headers, status);

    }
}
