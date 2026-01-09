package com.dd.glsc.ware.entity.dto;

import lombok.Data;

@Data
public class PurchaseDoneItem {
    /**
     * 采购项id
     */
    private Long itemId;
    /**
     * 采购项状态
     */
    private Integer status;
    /**
     * 失败原因
     */
    private String reason;
}
