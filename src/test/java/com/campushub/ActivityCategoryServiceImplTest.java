package com.campushub;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campushub.entity.ActivityCategory;
import com.campushub.mapper.ActivityCategoryMapper;
import com.campushub.service.impl.ActivityCategoryServiceImpl;
import com.campushub.vo.ActivityCategoryResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.campushub.entity.ActivityCategory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.verify;


class ActivityCategoryServiceImplTest {

    @Test
    void shouldReturnEmptyListWhenNoCategoriesFound() {
        ActivityCategoryMapper mapper =
                mock(ActivityCategoryMapper.class);

        when(mapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        ActivityCategoryServiceImpl service =
                new ActivityCategoryServiceImpl(mapper);

        List<ActivityCategoryResponse> result =
                service.listEnabledCategories();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldConvertCategoryToResponse() {
        ActivityCategory category = new ActivityCategory();
        category.setId(10L);
        category.setName("测试分类");

        ActivityCategoryMapper mapper =
                mock(ActivityCategoryMapper.class);

        when(mapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(category));

        ActivityCategoryServiceImpl service =
                new ActivityCategoryServiceImpl(mapper);

        List<ActivityCategoryResponse> result =
                service.listEnabledCategories();

        assertEquals(1, result.size());
        assertEquals(Long.valueOf(10), result.get(0).getId());
        assertEquals("测试分类", result.get(0).getName());
    }

    @Test
    void shouldFilterEnabledCategoriesAndOrderBySortThenId() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new Configuration(), ""),
                ActivityCategory.class
        );

        ActivityCategoryMapper mapper =
                mock(ActivityCategoryMapper.class);

        when(mapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        ActivityCategoryServiceImpl service =
                new ActivityCategoryServiceImpl(mapper);

        service.listEnabledCategories();

        ArgumentCaptor<LambdaQueryWrapper> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);

        verify(mapper).selectList(captor.capture());

        LambdaQueryWrapper<?> queryWrapper = captor.getValue();

        String sql = queryWrapper.getSqlSegment()
                .replaceAll("\\s+", "");

        assertEquals(
                "(status=#{ew.paramNameValuePairs.MPGENVAL1})"
                        + "ORDERBYsortASC,idASC",
                sql
        );

        assertEquals(
                1,
                queryWrapper.getParamNameValuePairs().get("MPGENVAL1")
        );
    }

}