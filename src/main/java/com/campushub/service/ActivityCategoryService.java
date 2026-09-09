package com.campushub.service;

import com.campushub.vo.ActivityCategoryResponse;

import java.util.List;

public interface ActivityCategoryService {
    List<ActivityCategoryResponse> listEnabledCategories();
}
