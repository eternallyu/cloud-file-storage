package ru.eternallyu.cloudfilestorage.error;

public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }
}
