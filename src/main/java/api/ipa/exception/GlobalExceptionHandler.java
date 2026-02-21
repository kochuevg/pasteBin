package api.ipa.exception;

import api.ipa.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponse> pasteExpiredException(final ForbiddenOperationException ex) {
        final ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AlreadyTakenException.class)
    public ResponseEntity<ErrorResponse> pasteExpiredException(final AlreadyTakenException ex) {
        final ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PasteExpiredException.class)
    public ResponseEntity<ErrorResponse> pasteExpiredException(final PasteExpiredException ex) {
        final ErrorResponse error = new ErrorResponse(
                "Paste with key: " + ex.getMessage() + " is already expired",
                HttpStatus.GONE,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.GONE);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> userNotFoundException(final UserNotFoundException ex) {
        final ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PasteNotFoundException.class)
    public ResponseEntity<ErrorResponse> pasteNotFoundException(final PasteNotFoundException ex) {
        final ErrorResponse error = new ErrorResponse(
                "Paste with key " + ex.getMessage() + " was not found",
                HttpStatus.NOT_FOUND,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exception(Exception e) {
        ErrorResponse error = new ErrorResponse(
                "An unexpected error occurred: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
