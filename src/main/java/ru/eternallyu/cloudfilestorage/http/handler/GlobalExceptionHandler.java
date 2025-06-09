package ru.eternallyu.cloudfilestorage.http.handler;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ru.eternallyu.cloudfilestorage.dto.response.ErrorResponseDto;
import ru.eternallyu.cloudfilestorage.error.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponseDto handleStorageException(StorageException exception) {
        return new ErrorResponseDto(exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponseDto resourceNotFound(NotFoundException exception) {
        return new ErrorResponseDto(exception.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponseDto handleUserAlreadyExistsException(UserAlreadyExistsException exception) {
        return new ErrorResponseDto(exception.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponseDto handleResourceAlreadyExistsException(ResourceAlreadyExistsException exception) {
        return new ErrorResponseDto(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponseDto handleAllOtherExceptions(Exception exception) {
        return new ErrorResponseDto(exception.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    ErrorResponseDto handleMaxUploadSizeExceededException() {
        return new ErrorResponseDto("Размер файла слишком большой");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponseDto handleMethodArgumentNotValidException() {
        return new ErrorResponseDto("Имя пользователя и пароль должны содержать от 5 до 20 символов");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorResponseDto handleAccessDeniedException() {
        return new ErrorResponseDto("Доступ запрещён");
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorResponseDto handleBadCredentialsException(BadCredentialsException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof UsernameNotFoundException) {
            return new ErrorResponseDto("Неверные данные");
        }
        return new ErrorResponseDto("Неверный пароль");
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponseDto handleBadRequestException(BadRequestException exception) {
        return new ErrorResponseDto(exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorResponseDto handleAuthenticationException() {
        return new ErrorResponseDto("Неверные данные");
    }
}
