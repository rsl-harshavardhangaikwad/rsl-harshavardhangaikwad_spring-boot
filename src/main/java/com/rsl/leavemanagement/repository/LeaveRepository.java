package com.rsl.leavemanagement.repository;

import com.rsl.leavemanagement.model.LeaveRequest;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for leave request persistence operations.
 * Follows the Repository pattern so the service layer is decoupled
 * from the underlying storage mechanism.
 */
public interface LeaveRepository {

    /**
     * Persist a new leave request and return the saved entity (with generated id).
     */
    LeaveRequest save(LeaveRequest leaveRequest);

    /**
     * Return all stored leave requests.
     */
    List<LeaveRequest> findAll();

    /**
     * Find a leave request by its unique identifier.
     *
     * @param id the leave request id
     * @return an {@link Optional} containing the entity, or empty if not found
     */
    Optional<LeaveRequest> findById(Long id);

    /**
     * Delete the leave request with the given id.
     *
     * @param id the leave request id
     * @return {@code true} if an entity was removed, {@code false} otherwise
     */
    boolean deleteById(Long id);

    /**
     * Check whether a leave request with the given id exists.
     */
    boolean existsById(Long id);
}
