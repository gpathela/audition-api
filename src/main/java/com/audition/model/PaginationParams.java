package com.audition.model;

import com.audition.common.constants.ErrorMessages;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationParams {

    @Min(value = 0, message = ErrorMessages.PAGE_NON_NEGATIVE)
    private int page;

    @Min(value = 1, message = ErrorMessages.SIZE_MIN_ONE)
    @Max(value = 100, message = ErrorMessages.SIZE_MAX_HUNDRED)
    private int size = 10;
}
