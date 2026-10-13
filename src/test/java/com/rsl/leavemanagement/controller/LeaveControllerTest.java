package com.rsl.leavemanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsl.leavemanagement.dto.LeaveRequestDto;
import com.rsl.leavemanagement.dto.LeaveResponseDto;
import com.rsl.leavemanagement.exception.GlobalExceptionHandler;
import com.rsl.leavemanagement.exception.LeaveNotFoundException;
import com.rsl.leavemanagement.model.LeaveStatus;
import com.rsl.leavemanagement.model.LeaveType;
import com.rsl.leavemanagement.service.LeaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc tests for {@link LeaveController}.
 *
 * <p>{@code @WebMvcTest} loads only the web layer (controller + exception handlers)
 * without the full application context. The service is mocked with {@code @MockBean}.</p>
 */
@WebMvcTest(controllers = {LeaveController.class, GlobalExceptionHandler.class})
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LeaveService leaveService;

    // -----------------------------------------------------------------------
    // Shared fixtures
    // -----------------------------------------------------------------------

    private LeaveResponseDto leaveResponse1;
    private LeaveResponseDto leaveResponse2;
    private LeaveRequestDto validRequest;

    @BeforeEach
    void setUp() {
        leaveResponse1 = new LeaveResponseDto(
                1L, "EMP001", LeaveType.ANNUAL,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5),
                "Family vacation planned for the long weekend", LeaveStatus.PENDING
        );

        leaveResponse2 = new LeaveResponseDto(
                2L, "EMP002", LeaveType.SICK,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                "Medical procedure requires post-op recovery rest", LeaveStatus.APPROVED
        );

        validRequest = new LeaveRequestDto(
                "EMP001",
                LeaveType.ANNUAL,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5),
                "Family vacation planned for the long weekend"
        );
    }

    // -----------------------------------------------------------------------
    // GET /leaves
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("GET /leaves")
    class GetAllLeaves {

        @Test
        @DisplayName("returns 200 with list of leaves")
        void returns200WithAllLeaves() throws Exception {
            given(leaveService.getAllLeaves()).willReturn(List.of(leaveResponse1, leaveResponse2));

            mockMvc.perform(get("/leaves").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].employeeId", is("EMP001")))
                    .andExpect(jsonPath("$[1].id", is(2)));
        }

        @Test
        @DisplayName("returns 200 with empty list when no leaves exist")
        void returns200WithEmptyList() throws Exception {
            given(leaveService.getAllLeaves()).willReturn(List.of());

            mockMvc.perform(get("/leaves").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // -----------------------------------------------------------------------
    // GET /leaves/{id}
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("GET /leaves/{id}")
    class GetLeaveById {

        @Test
        @DisplayName("returns 200 with leave when id exists")
        void returns200WhenFound() throws Exception {
            given(leaveService.getLeaveById(1L)).willReturn(leaveResponse1);

            mockMvc.perform(get("/leaves/1").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.status", is("PENDING")))
                    .andExpect(jsonPath("$.leaveType", is("ANNUAL")));
        }

        @Test
        @DisplayName("returns 404 when leave id does not exist")
        void returns404WhenNotFound() throws Exception {
            given(leaveService.getLeaveById(99L)).willThrow(new LeaveNotFoundException(99L));

            mockMvc.perform(get("/leaves/99").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    // -----------------------------------------------------------------------
    // POST /leaves
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("POST /leaves")
    class CreateLeave {

        @Test
        @DisplayName("returns 201 Created with saved leave on valid request")
        void returns201OnValidRequest() throws Exception {
            given(leaveService.createLeave(any(LeaveRequestDto.class))).willReturn(leaveResponse1);

            mockMvc.perform(post("/leaves")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.status", is("PENDING")));
        }

        @Test
        @DisplayName("returns 400 Bad Request when employeeId is blank")
        void returns400WhenEmployeeIdBlank() throws Exception {
            LeaveRequestDto badRequest = new LeaveRequestDto(
                    "",    // blank employeeId — violates @NotBlank
                    LeaveType.ANNUAL,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5),
                    "Family vacation planned for the long weekend"
            );

            mockMvc.perform(post("/leaves")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.employeeId").exists());
        }

        @Test
        @DisplayName("returns 400 Bad Request when reason is too short")
        void returns400WhenReasonTooShort() throws Exception {
            LeaveRequestDto badRequest = new LeaveRequestDto(
                    "EMP001",
                    LeaveType.ANNUAL,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5),
                    "Short"    // < 10 chars — violates @Size(min=10)
            );

            mockMvc.perform(post("/leaves")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.reason").exists());
        }

        @Test
        @DisplayName("returns 400 Bad Request when leaveType is null")
        void returns400WhenLeaveTypeNull() throws Exception {
            String json = """
                    {
                        "employeeId": "EMP001",
                        "leaveType": null,
                        "startDate": "%s",
                        "endDate": "%s",
                        "reason": "Family vacation planned for the long weekend"
                    }
                    """.formatted(
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5)
            );

            mockMvc.perform(post("/leaves")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }
    }

    // -----------------------------------------------------------------------
    // PUT /leaves/{id}
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /leaves/{id}")
    class UpdateLeave {

        @Test
        @DisplayName("returns 200 with updated leave on valid request")
        void returns200OnValidUpdate() throws Exception {
            given(leaveService.updateLeave(eq(1L), any(LeaveRequestDto.class)))
                    .willReturn(leaveResponse1);

            mockMvc.perform(put("/leaves/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)));
        }

        @Test
        @DisplayName("returns 404 when updating non-existent leave")
        void returns404WhenNotFound() throws Exception {
            given(leaveService.updateLeave(eq(99L), any(LeaveRequestDto.class)))
                    .willThrow(new LeaveNotFoundException(99L));

            mockMvc.perform(put("/leaves/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    // -----------------------------------------------------------------------
    // DELETE /leaves/{id}
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /leaves/{id}")
    class DeleteLeave {

        @Test
        @DisplayName("returns 204 No Content on successful deletion")
        void returns204OnSuccess() throws Exception {
            willDoNothing().given(leaveService).deleteLeave(1L);

            mockMvc.perform(delete("/leaves/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 when deleting non-existent leave")
        void returns404WhenNotFound() throws Exception {
            willThrow(new LeaveNotFoundException(99L)).given(leaveService).deleteLeave(99L);

            mockMvc.perform(delete("/leaves/99"))
                    .andExpect(status().isNotFound());
        }
    }
}
