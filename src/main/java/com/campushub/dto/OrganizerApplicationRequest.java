package com.campushub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizerApplicationRequest {
    @NotBlank(message = "请填写申请理由")
    @Size(min = 10, max = 500, message = "申请理由需要10到500个字符")
    private String reason;
}
