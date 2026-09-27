package com.campushub;

import com.campushub.mapper.ActivityMapper;
import com.campushub.service.impl.ActivityLifecycleServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActivityLifecycleServiceImplTest {
    @Test
    void returnsNumberOfFinishedActivities() {
        ActivityMapper mapper = mock(ActivityMapper.class);
        when(mapper.finishExpired(any())).thenReturn(3);
        assertEquals(3, new ActivityLifecycleServiceImpl(mapper).finishExpiredActivities());
    }
}
