package com.rsl.leavemanagement.service;

import com.rsl.leavemanagement.dto.LeaveRequestDto;
import com.rsl.leavemanagement.dto.LeaveResponseDto;
import com.rsl.leavemanagement.exception.InvalidLeaveOperationException;
import com.rsl.leavemanagement.exception.LeaveNotFoundException;
import com.rsl.leavemanagement.model.LeaveRequest;
import com.rsl.leavemanagement.model.LeaveStatus;
import com.rsl.leavemanagement.model.LeaveType;
import com.rsl.leavemanagement.repository.LeaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Unit tests for {@link LeaveServiceImpl}.
 *
 * <p>Uses Mockito to stub the {@link LeaveRepository} so that all tests
 * are pure unit tests with no Spring context involved.</p>
 */
@ExtendWith(MockitoExtension.class)
class LeaveServiceImplTest {

    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private LeaveServiceImpl leaveService;

    // -----------------------------------------------------------------------
    // Shared fixtures
    // -----------------------------------------------------------------------

    private LeaveRequest pendingLeave;
    private LeaveRequest approvedLeave;
    private LeaveRequestDto validDto;

    @BeforeEach
    void setUp() {
        pendingLeave = LeaveRequest.builder()
                .id(1L)
                .employeeId("EMP001")
                .leaveType(LeaveType.ANNUAL)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(5))
                .reason("Family vacation planned well in advance")
                .status(LeaveStatus.PENDING)
                .build();

        approvedLeave = LeaveRequest.builder()
                .id(2L)
                .employeeId("EMP002")
                .leaveType(LeaveType.SICK)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .reason("Recovering from surgery, doctor advised rest")
                .status(LeaveStatus.APPROVED)
                .build();

        validDto = new LeaveRequestDto(
                "EMP001",
                LeaveType.CASUAL,
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(4),
                "Personal work that requires my presence"
        );
    }

    // -----------------------------------------------------------------------
    // getAllLeaves
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("getAllLeaves()")
    class GetAllLeaves {

        @Test
        @DisplayName("returns empty list when repository has no entries")
        void returnsEmptyListWhenNoLeaves() {
            given(leaveRepository.findAll()).willReturn(List.of());

            List<LeaveResponseDto> result = leaveService.getAllLeaves();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns all leave response DTOs from repository")
        void returnsAllLeaves() {
            given(leaveRepository.findAll()).willReturn(List.of(pendingLeave, approvedLeave));

            List<LeaveResponseDto> result = leaveService.getAllLeaves();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(1).id()).isEqualTo(2L);
        }
    }

    // -----------------------------------------------------------------------
    // getLeaveById
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("getLeaveById()")
    class GetLeaveById {

        @Test
        @DisplayName("returns response DTO when leave exists")
        void returnsLeaveWhenFound() {
            given(leaveRepository.findById(1L)).willReturn(Optional.of(pendingLeave));

            LeaveResponseDto result = leaveService.getLeaveById(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.employeeId()).isEqualTo("EMP001");
            assertThat(result.status()).isEqualTo(LeaveStatus.PENDING);
        }

        @Test
        @DisplayName("throws LeaveNotFoundException when leave is missing")
        void throwsWhenNotFound() {
            given(leaveRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> leaveService.getLeaveById(99L))
                    .isInstanceOf(LeaveNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // -----------------------------------------------------------------------
    // createLeave
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("createLeave()")
    class CreateLeave {

        @Test
        @DisplayName("saves leave with PENDING status and returns DTO with generated id")
        void createsLeaveWithPendingStatus() {
            LeaveRequest saved = LeaveRequest.builder()
                    .id(10L)
                    .employeeId(validDto.employeeId())
                    .leaveType(validDto.leaveType())
                    .startDate(validDto.startDate())
                    .endDate(validDto.endDate())
                    .reason(validDto.reason())
                    .status(LeaveStatus.PENDING)
                    .build();

            given(leaveRepository.save(any(LeaveRequest.class))).willReturn(saved);

            LeaveResponseDto result = leaveService.createLeave(validDto);

            assertThat(result.id()).isEqualTo(10L);
            assertThat(result.status()).isEqualTo(LeaveStatus.PENDING);
            assertThat(result.employeeId()).isEqualTo("EMP001");
        }

        @Test
        @DisplayName("throws InvalidLeaveOperationException when endDate is before startDate")
        void throwsWhenEndDateBeforeStartDate() {
            LeaveRequestDto badDto = new LeaveRequestDto(
                    "EMP001",
                    LeaveType.ANNUAL,
                    LocalDate.now().plusDays(5),  // startDate
                    LocalDate.now().plusDays(2),  // endDate — before start
                    "Some valid reason to explain the leave request"
            );

            assertThatThrownBy(() -> leaveService.createLeave(badDto))
                    .isInstanceOf(InvalidLeaveOperationException.class)
                    .hasMessageContaining("End date must not be before start date");

            then(leaveRepository).should(never()).save(any());
        }
    }

    // -----------------------------------------------------------------------
    // updateLeave
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("updateLeave()")
    class UpdateLeave {

        @Test
        @DisplayName("updates PENDING leave and returns updated DTO")
        void updatesPendingLeave() {
            given(leaveRepository.findById(1L)).willReturn(Optional.of(pendingLeave));
            given(leaveRepository.save(any(LeaveRequest.class))).willAnswer(inv -> inv.getArgument(0));

            LeaveResponseDto result = leaveService.updateLeave(1L, validDto);

            assertThat(result.leaveType()).isEqualTo(LeaveType.CASUAL);
            assertThat(result.reason()).isEqualTo(validDto.reason());
        }

        @Test
        @DisplayName("throws InvalidLeaveOperationException when updating APPROVED leave")
        void throwsWhenUpdatingApprovedLeave() {
            given(leaveRepository.findById(2L)).willReturn(Optional.of(approvedLeave));

            assertThatThrownBy(() -> leaveService.updateLeave(2L, validDto))
                    .isInstanceOf(InvalidLeaveOperationException.class)
                    .hasMessageContaining("APPROVED");

            then(leaveRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("throws LeaveNotFoundException when leave id does not exist")
        void throwsWhenLeaveNotFound() {
            given(leaveRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> leaveService.updateLeave(99L, validDto))
                    .isInstanceOf(LeaveNotFoundException.class);
        }
    }

    // -----------------------------------------------------------------------
    // deleteLeave
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("deleteLeave()")
    class DeleteLeave {

        @Test
        @DisplayName("deletes PENDING leave successfully")
        void deletesPendingLeave() {
            given(leaveRepository.findById(1L)).willReturn(Optional.of(pendingLeave));

            leaveService.deleteLeave(1L);

            then(leaveRepository).should().deleteById(1L);
        }

        @Test
        @DisplayName("throws InvalidLeaveOperationException when deleting APPROVED leave")
        void throwsWhenDeletingApprovedLeave() {
            given(leaveRepository.findById(2L)).willReturn(Optional.of(approvedLeave));

            assertThatThrownBy(() -> leaveService.deleteLeave(2L))
                    .isInstanceOf(InvalidLeaveOperationException.class)
                    .hasMessageContaining("APPROVED");

            then(leaveRepository).should(never()).deleteById(any());
        }

        @Test
        @DisplayName("throws LeaveNotFoundException when leave id does not exist")
        void throwsWhenLeaveNotFound() {
            given(leaveRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> leaveService.deleteLeave(99L))
                    .isInstanceOf(LeaveNotFoundException.class);

            then(leaveRepository).should(never()).deleteById(any());
        }
    }
}
