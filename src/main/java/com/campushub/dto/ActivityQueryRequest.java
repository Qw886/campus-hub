package com.campushub.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityQueryRequest {

    @NotNull(message = "页码不能为空")
    @Min(value = 1 , message = "页码不能小于1")
    private Integer page = 1;

    @NotNull(message = "每页数量不能为空")
    @Min(value = 1,  message = "每页数量不能小于1")
    @Max(value = 50 , message = "每页数量不能大于50")
    private Integer size = 10;

    @Min(value = 1, message = "分类编号必须大于0")
    private Long categoryId;
    @Size(max = 100, message = "关键词最大长度为100")
    private String keyword;
}
