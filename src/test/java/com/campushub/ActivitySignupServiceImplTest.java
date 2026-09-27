package com.campushub;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campushub.common.exception.BusinessException;
import com.campushub.entity.Activity;
import com.campushub.entity.ActivitySignup;
import com.campushub.mapper.ActivityCheckinMapper;
import com.campushub.mapper.ActivityMapper;
import com.campushub.mapper.ActivitySignupMapper;
import com.campushub.service.impl.ActivitySignupServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class ActivitySignupServiceImplTest {

    @Test
    void signupInWindowIncrementsParticipants() {
        ActivityMapper activityMapper = mock(ActivityMapper.class);
        ActivitySignupMapper signupMapper = mock(ActivitySignupMapper.class);
        ActivityCheckinMapper checkinMapper = mock(ActivityCheckinMapper.class);
        Activity activity = openActivity();
        when(activityMapper.selectByIdForUpdate(1L)).thenReturn(activity);
        when(signupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(signupMapper.insert(any(ActivitySignup.class))).thenReturn(1);
        when(activityMapper.incrementParticipants(1L)).thenReturn(1);

        new ActivitySignupServiceImpl(activityMapper, signupMapper, checkinMapper).signup(2L, 1L);

        verify(signupMapper).insert(any(ActivitySignup.class));
        verify(activityMapper).incrementParticipants(1L);
    }

    @Test
    void duplicateSignupIsRejectedWithoutChangingCount() {
        ActivityMapper activityMapper = mock(ActivityMapper.class);
        ActivitySignupMapper signupMapper = mock(ActivitySignupMapper.class);
        ActivityCheckinMapper checkinMapper = mock(ActivityCheckinMapper.class);
        ActivitySignup existing = new ActivitySignup();
        existing.setStatus(1);
        when(activityMapper.selectByIdForUpdate(1L)).thenReturn(openActivity());
        when(signupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> new ActivitySignupServiceImpl(activityMapper, signupMapper, checkinMapper).signup(2L, 1L));

        assertEquals(409, exception.getCode());
        verify(signupMapper, never()).insert(any(ActivitySignup.class));
        verify(activityMapper, never()).incrementParticipants(1L);
    }

    @Test
    void signupStateReadsTheRequestedActivityInsteadOfTheCurrentPage() {
        ActivityMapper activityMapper = mock(ActivityMapper.class);
        ActivitySignupMapper signupMapper = mock(ActivitySignupMapper.class);
        ActivityCheckinMapper checkinMapper = mock(ActivityCheckinMapper.class);
        ActivitySignup signup = new ActivitySignup();
        signup.setStatus(1);
        when(signupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(signup);
        when(checkinMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        var state = new ActivitySignupServiceImpl(activityMapper, signupMapper, checkinMapper).getState(2L, 99L);

        assertEquals(true, state.active());
        assertEquals(true, state.checkedIn());
    }

    @Test
    void cancelledSignupIsNotShownAsActive() {
        ActivityMapper activityMapper = mock(ActivityMapper.class);
        ActivitySignupMapper signupMapper = mock(ActivitySignupMapper.class);
        ActivityCheckinMapper checkinMapper = mock(ActivityCheckinMapper.class);
        ActivitySignup signup = new ActivitySignup();
        signup.setStatus(2);
        when(signupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(signup);

        var state = new ActivitySignupServiceImpl(activityMapper, signupMapper, checkinMapper).getState(2L, 99L);

        assertEquals(false, state.active());
        assertEquals(false, state.checkedIn());
        verify(checkinMapper, never()).selectCount(any(LambdaQueryWrapper.class));
    }

    private Activity openActivity() {
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setStatus(2);
        activity.setCurrentParticipants(0);
        activity.setMaxParticipants(10);
        activity.setSignupStartTime(LocalDateTime.now().minusMinutes(10));
        activity.setSignupEndTime(LocalDateTime.now().plusMinutes(10));
        return activity;
    }
}
