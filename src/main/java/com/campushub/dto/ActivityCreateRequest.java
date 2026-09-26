package com.campushub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ActivityCreateRequest {

//    字段：title/description/coverUrl/location(String)、categoryId(Long)、maxParticipants(Integer)、四个时间(LocalDateTime)。

    @NotBlank(message = "活动标题不能为空")
    @Size(max = 100 , message = "活动标题最大程度为100")
    private String title;
    @Size(max = 10000 , message = "活动描述最大长度为10000")
    private String description;
    @Size(max = 500 ,message = "封面地址最大长度为500")
    private String coverUrl;
    @NotBlank(message = "活动地点不能为空")
    @Size(max = 200 , message = "活动地点最大长度为200")
    private String location;
    @NotNull(message = "分类不能为空")
    @Positive(message = "分类id必须为正数")
    private Long categoryId;
    @NotNull(message = "最大参与人数不能为空")
    @Positive(message = "最大参与人数必须为正数")
    private Integer maxParticipants;
    @NotNull(message = "时间不能为空")
    private LocalDateTime registrationStartTime;
    @NotNull(message = "时间不能为空")
    private LocalDateTime registrationEndTime;
    @NotNull(message = "时间不能为空")
    private LocalDateTime startTime;
    @NotNull(message = "时间不能为空")
    private LocalDateTime endTime;

}
