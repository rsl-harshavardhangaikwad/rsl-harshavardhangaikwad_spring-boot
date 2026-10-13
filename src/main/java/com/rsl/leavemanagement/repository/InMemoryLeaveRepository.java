package com.rsl.leavemanagement.repository;

import com.rsl.leavemanagement.model.LeaveRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of {@link LeaveRepository} backed by a
 * {@link ConcurrentHashMap} for thread-safe, lock-free read operations.
 *
 * <p>An {@link AtomicLong} counter is used to generate monotonically
 * increasing, unique IDs without synchronization overhead.</p>
 */
@Repository
public class InMemoryLeaveRepository implements LeaveRepository {

    private final Map<Long, LeaveRequest> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public LeaveRequest save(LeaveRequest leaveRequest) {
        if (leaveRequest.getId() == null) {
            leaveRequest.setId(idCounter.getAndIncrement());
        }
        store.put(leaveRequest.getId(), leaveRequest);
        return leaveRequest;
    }

    @Override
    public List<LeaveRequest> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<LeaveRequest> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean deleteById(Long id) {
        return store.remove(id) != null;
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }
}
