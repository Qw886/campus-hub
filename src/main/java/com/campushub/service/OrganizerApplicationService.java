package com.campushub.service;

import com.campushub.dto.OrganizerApplicationRequest;
import com.campushub.dto.OrganizerApplicationReviewRequest;
import com.campushub.vo.OrganizerApplicationPageResponse;
import com.campushub.vo.OrganizerApplicationResponse;

public interface OrganizerApplicationService {
    OrganizerApplicationResponse getMine(Long userId);
    void apply(Long userId, OrganizerApplicationRequest request);
    OrganizerApplicationPageResponse listPending(int page, int size);
    void review(Long adminId, Long applicationId, OrganizerApplicationReviewRequest request);
}
