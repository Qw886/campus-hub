package com.campushub.service;

import com.campushub.dto.ActivityCreateRequest;
import com.campushub.dto.OrganizerActivityQueryRequest;
import com.campushub.dto.SignupQueryRequest;
import com.campushub.vo.ActivityDetailResponse;
import com.campushub.vo.OrganizerActivityPageResponse;
import com.campushub.vo.ParticipantPageResponse;

public interface OrganizerActivityService {

    Long create(Long organizerId , ActivityCreateRequest request);

    void update(Long organizerId, Long activityId, ActivityCreateRequest request);

    OrganizerActivityPageResponse listMine(Long organizerId, OrganizerActivityQueryRequest request);

    ActivityDetailResponse getMine(Long organizerId, Long activityId);

    ParticipantPageResponse listParticipants(Long organizerId, Long activityId, SignupQueryRequest request);

    void cancel(Long organizerId, Long activityId);
}
