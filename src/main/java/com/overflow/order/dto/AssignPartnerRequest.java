package com.overflow.order.dto;

import jakarta.validation.constraints.NotNull;

public record AssignPartnerRequest(@NotNull Long partnerId) { }
