package com.dd.glsc.ware.entity.dto;

import lombok.Data;

@Data
public class PurchaseMergeDTO {
    private Long purchaseId;
    private Long[] items;
}
