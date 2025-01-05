package com.WinkProject.meeting.controller;

import com.WinkProject.common.fixture.MeetingTestFixture;
import com.WinkProject.meeting.controller.MeetingController;
import com.WinkProject.meeting.dto.request.MeetingCreateRequest;
import com.WinkProject.meeting.dto.request.MeetingUpdateRequest;
import com.WinkProject.meeting.dto.response.MeetingResponse;
import com.WinkProject.meeting.service.MeetingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MeetingControllerTest {

    @InjectMocks
    private MeetingController meetingController;

    @Mock
    private MeetingService meetingService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(meetingController)
                .build();
    }

    @Test
    @DisplayName("모임 생성 API - 성공")
    void createMeeting_Success() throws Exception {
        // given
        Long userId = 1L;
        MeetingCreateRequest request = MeetingTestFixture.createMeetingRequest("새로운 모임");
        MeetingResponse response = new MeetingResponse();
        response.setName(request.getName());
        
        given(meetingService.createMeeting(any(MeetingCreateRequest.class), eq(userId)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(request.getName()));
    }

    @Test
    @DisplayName("모임 수정 API - 성공")
    void updateMeeting_Success() throws Exception {
        // given
        Long meetingId = 1L;
        Long userId = 1L;
        MeetingUpdateRequest request = MeetingTestFixture.createMeetingUpdateRequest("수정된 모임");
        MeetingResponse response = new MeetingResponse();
        response.setName(request.getName());
        
        given(meetingService.updateMeeting(eq(meetingId), any(MeetingUpdateRequest.class), eq(userId)))
                .willReturn(response);

        // when & then
        mockMvc.perform(put("/meetings/{meetingId}", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(request.getName()));
    }

    @Test
    @DisplayName("모임 삭제 API - 성공")
    void deleteMeeting_Success() throws Exception {
        // given
        Long meetingId = 1L;
        Long userId = 1L;

        // when & then
        mockMvc.perform(delete("/meetings/{meetingId}", meetingId)
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk());
        
        verify(meetingService).deleteMeeting(meetingId, userId);
    }

    @Test
    @DisplayName("최근 모임 조회 API - 성공")
    void getLatestMeetings_Success() throws Exception {
        // given
        Long userId = 1L;
        int limit = 5;
        MeetingResponse response = new MeetingResponse();
        response.setName("테스트 모임");
        
        given(meetingService.getLatestMeetings(eq(limit), eq(userId)))
                .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/meetings/latest")
                        .param("limit", String.valueOf(limit))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("테스트 모임"));
    }
} 