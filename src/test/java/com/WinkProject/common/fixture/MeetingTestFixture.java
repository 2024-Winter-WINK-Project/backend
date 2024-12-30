package com.winkproject.common.fixture;

import com.winkproject.meeting.domain.Meeting;
import com.winkproject.meeting.domain.Place;
import com.winkproject.meeting.dto.request.MeetingCreateRequest;
import com.winkproject.meeting.dto.request.MeetingUpdateRequest;

import java.time.LocalDateTime;

public class MeetingTestFixture {
    
    public static Meeting createMeeting(String name) {
        Meeting meeting = new Meeting();
        meeting.setName(name);
        meeting.setDescription("테스트 모임입니다.");
        meeting.setStartTime(LocalDateTime.now().plusDays(1));
        meeting.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        meeting.setPlace(createPlace());
        return meeting;
    }
    
    public static Place createPlace() {
        Place place = new Place();
        place.setName("테스트 장소");
        place.setAddress("서울시 강남구 테헤란로");
        place.setLatitude(37.4989);
        place.setLongitude(127.0282);
        return place;
    }
    
    public static MeetingCreateRequest createMeetingRequest(String name) {
        MeetingCreateRequest request = new MeetingCreateRequest();
        request.setName(name);
        request.setDescription("테스트 모임입니다.");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        
        MeetingCreateRequest.PlaceRequest placeRequest = new MeetingCreateRequest.PlaceRequest();
        placeRequest.setName("테스트 장소");
        placeRequest.setAddress("서울시 강남구 테헤란로");
        placeRequest.setLatitude(37.4989);
        placeRequest.setLongitude(127.0282);
        
        request.setPlace(placeRequest);
        return request;
    }
    
    public static MeetingUpdateRequest createMeetingUpdateRequest(String name) {
        MeetingUpdateRequest request = new MeetingUpdateRequest();
        request.setName(name);
        request.setDescription("수정된 모임입니다.");
        request.setStartTime(LocalDateTime.now().plusDays(2));
        request.setEndTime(LocalDateTime.now().plusDays(2).plusHours(3));
        
        MeetingUpdateRequest.PlaceRequest placeRequest = new MeetingUpdateRequest.PlaceRequest();
        placeRequest.setName("수정된 장소");
        placeRequest.setAddress("서울시 서초구 반포대로");
        placeRequest.setLatitude(37.5038);
        placeRequest.setLongitude(127.0244);
        
        request.setPlace(placeRequest);
        return request;
    }
} 