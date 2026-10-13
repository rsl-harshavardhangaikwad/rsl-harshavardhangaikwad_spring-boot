package com.rsl.leavemanagement.service;

import com.rsl.leavemanagement.dto.LeaveRequestDto;
import com.rsl.leavemanagement.dto.LeaveResponseDto;

import java.util.List;

/**
 * Service interface defining business operations for leave request management.
 */
public interface LeaveService {

    /**
     * Retrieve all leave requests.
     */
    List<LeaveResponseDto> getAllLeaves();

    /**
     * Retrieve a single leave request by its ID.
     *
     * @param id the leave request identifier
     * @return the matching leave response DTO
     * @throws com.rsl.leavemanagement.exception.LeaveNotFoundException if not found
     */
    LeaveResponseDto getLeaveById(Long id);

    /**
     * Create and persist a new leave request with PENDING status.
     *
     * @param dto the validated request payload
     * @return the created leave response DTO with generated id
     */
    LeaveResponseDto createLeave(LeaveRequestDto dto);

    /**
     * Update an existing leave request.
     *
     * @param id  the leave request identifier
     * @param dto the validated update payload
     * @return the updated leave response DTO
     * @throws com.rsl.leavemanagement.exception.LeaveNotFoundException       if not found
     * @throws com.rsl.leavemanagement.exception.InvalidLeaveOperationException if the current
     *                                                                            status does not allow updates
     */
    LeaveResponseDto updateLeave(Long id, LeaveRequestDto dto);

    /**
     * Delete a leave request by ID.
     *
     * @param id the leave request identifier
     * @throws com.rsl.leavemanagement.exception.LeaveNotFoundException       if not found
     * @throws com.rsl.leavemanagement.exception.InvalidLeaveOperationException if the current
     *                                                                            status does not allow deletion
     */
    void deleteLeave(Long id);
}
