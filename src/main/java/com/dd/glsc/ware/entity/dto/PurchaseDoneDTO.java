package com.dd.glsc.ware.entity.dto;

import lombok.Data;

@Data
public class PurchaseDoneDTO {
    /**
     * 领取人id
     */
    private Long userId;
    /**
     * 采购单id
     */
    private Long id;
    /**
     * 采购项
     */
    private PurchaseDoneItem[] items;
}
