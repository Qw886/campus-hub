package com.campushub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campushub.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
    @Update("""
            UPDATE activity
            SET status = #{newStatus}
            WHERE id = #{activityId}
              AND status = 1
            """)
    int updateAuditStatus(
            @Param("activityId") Long activityId,
            @Param("newStatus") Integer newStatus
    );

    @Select("SELECT * FROM activity WHERE id = #{id} FOR UPDATE")
    Activity selectByIdForUpdate(@Param("id") Long id);

    @Update("UPDATE activity SET current_participants = current_participants + 1 " +
            "WHERE id = #{id} AND status = 2 AND current_participants < max_participants")
    int incrementParticipants(@Param("id") Long id);

    @Update("UPDATE activity SET current_participants = current_participants - 1 " +
            "WHERE id = #{id} AND current_participants > 0")
    int decrementParticipants(@Param("id") Long id);

    @Update("UPDATE activity SET status = 3 " +
            "WHERE status = 2 AND activity_end_time <= #{now}")
    int finishExpired(@Param("now") java.time.LocalDateTime now);
}
