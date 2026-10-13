package com.rsl.leavemanagement.dto;

import com.rsl.leavemanagement.model.LeaveType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO for incoming leave request creation/update payloads.
 * Contains Bean Validation constraints for input sanitization.
 */
public record LeaveRequestDto(

        @NotBlank(message = "Employee ID must not be blank")
        @Size(max = 20, message = "Employee ID must be at most 20 characters")
        String employeeId,

        @NotNull(message = "Leave type must not be null")
        LeaveType leaveType,

        @NotNull(message = "Start date must not be null")
        @FutureOrPresent(message = "Start date must be today or in the future")
        LocalDate startDate,

        @NotNull(message = "End date must not be null")
        @Future(message = "End date must be in the future")
        LocalDate endDate,

        @NotBlank(message = "Reason must not be blank")
        @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
        String reason
) {
}
