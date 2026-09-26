package com.campushub.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActivityAuditRequest {

    @NotNull(message = "审核结果不能为空")
    private Integer auditStatus;

    @Size(max = 500, message = "审核意见不能超过500字")
    private String reason;


}
