package com.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campushub.entity.ActivityCategory;
import com.campushub.mapper.ActivityCategoryMapper;
import com.campushub.service.ActivityCategoryService;
import com.campushub.vo.ActivityCategoryResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ActivityCategoryServiceImpl implements ActivityCategoryService {


    private final ActivityCategoryMapper activityCategoryMapper;

    // ② 新增：构造方法，接收并保存 Mapper
    public ActivityCategoryServiceImpl(
            ActivityCategoryMapper activityCategoryMapper
    ) {
        this.activityCategoryMapper = activityCategoryMapper;
    }

    @Override
    public List<ActivityCategoryResponse> listEnabledCategories() {

        LambdaQueryWrapper<ActivityCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityCategory::getStatus , 1);
        queryWrapper.orderByAsc(ActivityCategory::getSort);
        queryWrapper.orderByAsc(ActivityCategory::getId);

        List<ActivityCategory> categories =
                activityCategoryMapper.selectList(queryWrapper);

        List<ActivityCategoryResponse> responses = new ArrayList<>();

        for (ActivityCategory category : categories) {
            ActivityCategoryResponse response =
                    new ActivityCategoryResponse(
                            category.getId(),
                            category.getName()
                    );

            responses.add(response);
        }

        return responses;
    }

}
