package com.rsl.leavemanagement.dto;

import com.rsl.leavemanagement.model.LeaveStatus;
import com.rsl.leavemanagement.model.LeaveType;

import java.time.LocalDate;

/**
 * DTO for outgoing leave request responses.
 * Exposes all fields including system-generated id and status.
 */
public record LeaveResponseDto(
        Long id,
        String employeeId,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        LeaveStatus status
) {
}
