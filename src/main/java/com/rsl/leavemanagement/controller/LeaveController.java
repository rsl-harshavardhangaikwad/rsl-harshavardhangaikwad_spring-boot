package com.rsl.leavemanagement.controller;

import com.rsl.leavemanagement.dto.LeaveRequestDto;
import com.rsl.leavemanagement.dto.LeaveResponseDto;
import com.rsl.leavemanagement.service.LeaveService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller exposing the Leave Management API.
 *
 * <p>All endpoints are prefixed with {@code /leaves} as defined in the assignment
 * specification. Constructor-based DI is used so the controller is easily testable
 * with {@code MockMvc} and a mocked service.</p>
 */
@RestController
@RequestMapping("/leaves")
public class LeaveController {

    private static final Logger log = LoggerFactory.getLogger(LeaveController.class);

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    /**
     * GET /leaves — retrieve all leave requests.
     */
    @GetMapping
    public ResponseEntity<List<LeaveResponseDto>> getAllLeaves() {
        log.debug("GET /leaves");
        List<LeaveResponseDto> leaves = leaveService.getAllLeaves();
        return ResponseEntity.ok(leaves);
    }

    /**
     * GET /leaves/{id} — retrieve a leave request by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeaveResponseDto> getLeaveById(@PathVariable Long id) {
        log.debug("GET /leaves/{}", id);
        return ResponseEntity.ok(leaveService.getLeaveById(id));
    }

    /**
     * POST /leaves — create a new leave request.
     * Returns 201 Created with the saved entity.
     */
    @PostMapping
    public ResponseEntity<LeaveResponseDto> createLeave(
            @Valid @RequestBody LeaveRequestDto dto) {
        log.debug("POST /leaves for employeeId={}", dto.employeeId());
        LeaveResponseDto created = leaveService.createLeave(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /leaves/{id} — update an existing leave request.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LeaveResponseDto> updateLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequestDto dto) {
        log.debug("PUT /leaves/{}", id);
        return ResponseEntity.ok(leaveService.updateLeave(id, dto));
    }

    /**
     * DELETE /leaves/{id} — delete a leave request.
     * Returns 204 No Content on success.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeave(@PathVariable Long id) {
        log.debug("DELETE /leaves/{}", id);
        leaveService.deleteLeave(id);
        return ResponseEntity.noContent().build();
    }
}
