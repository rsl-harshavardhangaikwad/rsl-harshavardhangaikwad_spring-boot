package com.rsl.leavemanagement.exception;

/**
 * Thrown when a leave request update or delete is attempted
 * on a request whose current status does not allow it.
 */
public class InvalidLeaveOperationException extends RuntimeException {

    public InvalidLeaveOperationException(String message) {
        super(message);
    }
}
