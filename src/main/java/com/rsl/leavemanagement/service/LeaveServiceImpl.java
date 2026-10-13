package com.rsl.leavemanagement.service;

import com.rsl.leavemanagement.dto.LeaveRequestDto;
import com.rsl.leavemanagement.dto.LeaveResponseDto;
import com.rsl.leavemanagement.exception.InvalidLeaveOperationException;
import com.rsl.leavemanagement.exception.LeaveNotFoundException;
import com.rsl.leavemanagement.model.LeaveRequest;
import com.rsl.leavemanagement.model.LeaveStatus;
import com.rsl.leavemanagement.repository.LeaveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of {@link LeaveService}.
 *
 * <p>Business rules enforced here:</p>
 * <ul>
 *   <li>New leave requests always start with {@link LeaveStatus#PENDING}.</li>
 *   <li>Only {@code PENDING} leaves can be updated.</li>
 *   <li>Only {@code PENDING} leaves can be deleted (approved/rejected ones must be cancelled first).</li>
 *   <li>Start date must not be after end date.</li>
 * </ul>
 *
 * <p>Constructor-based dependency injection is used so that the repository
 * can easily be replaced in unit tests.</p>
 */
@Service
public class LeaveServiceImpl implements LeaveService {

    private static final Logger log = LoggerFactory.getLogger(LeaveServiceImpl.class);

    private final LeaveRepository leaveRepository;

    public LeaveServiceImpl(LeaveRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }

    // -------------------------------------------------------------------------
    // Query operations
    // -------------------------------------------------------------------------

    @Override
    public List<LeaveResponseDto> getAllLeaves() {
        log.debug("Fetching all leave requests");
        return leaveRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public LeaveResponseDto getLeaveById(Long id) {
        log.debug("Fetching leave request with id={}", id);
        LeaveRequest leave = findOrThrow(id);
        return toResponseDto(leave);
    }

    // -------------------------------------------------------------------------
    // Command operations
    // -------------------------------------------------------------------------

    @Override
    public LeaveResponseDto createLeave(LeaveRequestDto dto) {
        validateDateRange(dto);
        log.info("Creating leave request for employeeId={}", dto.employeeId());

        LeaveRequest leave = LeaveRequest.builder()
                .employeeId(dto.employeeId())
                .leaveType(dto.leaveType())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .reason(dto.reason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRepository.save(leave);
        log.info("Created leave request id={}", saved.getId());
        return toResponseDto(saved);
    }

    @Override
    public LeaveResponseDto updateLeave(Long id, LeaveRequestDto dto) {
        LeaveRequest existing = findOrThrow(id);

        if (existing.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveOperationException(
                    String.format("Leave request id=%d cannot be updated because its status is '%s'. " +
                            "Only PENDING requests can be modified.", id, existing.getStatus()));
        }

        validateDateRange(dto);
        log.info("Updating leave request id={}", id);

        existing.setEmployeeId(dto.employeeId());
        existing.setLeaveType(dto.leaveType());
        existing.setStartDate(dto.startDate());
        existing.setEndDate(dto.endDate());
        existing.setReason(dto.reason());

        LeaveRequest updated = leaveRepository.save(existing);
        return toResponseDto(updated);
    }

    @Override
    public void deleteLeave(Long id) {
        LeaveRequest existing = findOrThrow(id);

        if (existing.getStatus() == LeaveStatus.APPROVED) {
            throw new InvalidLeaveOperationException(
                    String.format("Leave request id=%d is APPROVED and cannot be deleted. " +
                            "Please contact HR to cancel it.", id));
        }

        log.info("Deleting leave request id={}", id);
        leaveRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private LeaveRequest findOrThrow(Long id) {
        return leaveRepository.findById(id)
                .orElseThrow(() -> new LeaveNotFoundException(id));
    }

    private void validateDateRange(LeaveRequestDto dto) {
        if (dto.startDate() != null && dto.endDate() != null
                && dto.endDate().isBefore(dto.startDate())) {
            throw new InvalidLeaveOperationException(
                    "End date must not be before start date.");
        }
    }

    private LeaveResponseDto toResponseDto(LeaveRequest leave) {
        return new LeaveResponseDto(
                leave.getId(),
                leave.getEmployeeId(),
                leave.getLeaveType(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                leave.getStatus()
        );
    }
}
