package com.rsl.leavemanagement.exception;

/**
 * Thrown when a leave request with the given ID cannot be found.
 */
public class LeaveNotFoundException extends RuntimeException {

    private final Long leaveId;

    public LeaveNotFoundException(Long leaveId) {
        super("Leave request not found with id: " + leaveId);
        this.leaveId = leaveId;
    }

    public Long getLeaveId() {
        return leaveId;
    }
}
