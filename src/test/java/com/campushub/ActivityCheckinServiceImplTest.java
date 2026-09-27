package com.campushub;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campushub.common.exception.BusinessException;
import com.campushub.entity.Activity;
import com.campushub.entity.ActivitySignup;
import com.campushub.mapper.ActivityCheckinMapper;
import com.campushub.mapper.ActivityMapper;
import com.campushub.mapper.ActivitySignupMapper;
import com.campushub.service.impl.ActivityCheckinServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActivityCheckinServiceImplTest {
    @Test
    void userWithoutActiveSignupCannotCheckin() {
        ActivityMapper activityMapper = mock(ActivityMapper.class);
        ActivitySignupMapper signupMapper = mock(ActivitySignupMapper.class);
        ActivityCheckinMapper checkinMapper = mock(ActivityCheckinMapper.class);
        Activity activity = new Activity();
        activity.setStatus(2);
        activity.setActivityStartTime(LocalDateTime.now().plusMinutes(5));
        activity.setActivityEndTime(LocalDateTime.now().plusHours(1));
        when(activityMapper.selectByIdForUpdate(1L)).thenReturn(activity);
        when(signupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> new ActivityCheckinServiceImpl(activityMapper, signupMapper, checkinMapper).checkin(2L, 1L));

        assertEquals(409, exception.getCode());
    }
}
