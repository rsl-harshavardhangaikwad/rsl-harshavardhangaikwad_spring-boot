package com.rsl.leavemanagement.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain model representing a leave request entity.
 * Stored in the in-memory repository.
 *
 * <p>Explicit getters, setters, and a Builder are used instead of Lombok
 * to ensure full compatibility across all Java / compiler toolchain versions.</p>
 */
public class LeaveRequest {

    private Long id;
    private String employeeId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;

    /** No-args constructor required for certain frameworks and test utilities. */
    public LeaveRequest() {
    }

    /** All-args constructor used internally and by tests. */
    public LeaveRequest(Long id, String employeeId, LeaveType leaveType,
                        LocalDate startDate, LocalDate endDate,
                        String reason, LeaveStatus status) {
        this.id = id;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public Long getId() { return id; }

    public String getEmployeeId() { return employeeId; }

    public LeaveType getLeaveType() { return leaveType; }

    public LocalDate getStartDate() { return startDate; }

    public LocalDate getEndDate() { return endDate; }

    public String getReason() { return reason; }

    public LeaveStatus getStatus() { return status; }

    // -----------------------------------------------------------------------
    // Setters
    // -----------------------------------------------------------------------

    public void setId(Long id) { this.id = id; }

    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }

    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public void setReason(String reason) { this.reason = reason; }

    public void setStatus(LeaveStatus status) { this.status = status; }

    // -----------------------------------------------------------------------
    // Builder
    // -----------------------------------------------------------------------

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private String employeeId;
        private LeaveType leaveType;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;
        private LeaveStatus status;

        private Builder() {}

        public Builder id(Long id) { this.id = id; return this; }
        public Builder employeeId(String employeeId) { this.employeeId = employeeId; return this; }
        public Builder leaveType(LeaveType leaveType) { this.leaveType = leaveType; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder status(LeaveStatus status) { this.status = status; return this; }

        public LeaveRequest build() {
            return new LeaveRequest(id, employeeId, leaveType, startDate, endDate, reason, status);
        }
    }

    // -----------------------------------------------------------------------
    // Object overrides
    // -----------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeaveRequest that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LeaveRequest{id=" + id + ", employeeId='" + employeeId + "', leaveType=" + leaveType +
               ", startDate=" + startDate + ", endDate=" + endDate + ", status=" + status + '}';
    }
}
