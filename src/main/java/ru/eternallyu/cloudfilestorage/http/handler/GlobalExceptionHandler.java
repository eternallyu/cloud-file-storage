package ru.eternallyu.cloudfilestorage.http.handler;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.eternallyu.cloudfilestorage.dto.response.ErrorResponseDto;
import ru.eternallyu.cloudfilestorage.error.UserAlreadyExistsException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    ErrorResponseDto
    handleUserAlreadyExistsException(UserAlreadyExistsException exception) {
        return new ErrorResponseDto(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    ErrorResponseDto
    handleAllOtherExceptions(Exception exception) {
        return new ErrorResponseDto(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ErrorResponseDto
    handleMethodArgumentNotValidException() {
        return new ErrorResponseDto("Username and password must be between 5 and 20 characters");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    ErrorResponseDto
    handleAccessDeniedException() {
        return new ErrorResponseDto("Access denied");
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    ErrorResponseDto
    handleBadCredentialsException(BadCredentialsException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof UsernameNotFoundException) {
            return new ErrorResponseDto(exception.getMessage());
        }
        return new ErrorResponseDto("Invalid password");

    }
}
