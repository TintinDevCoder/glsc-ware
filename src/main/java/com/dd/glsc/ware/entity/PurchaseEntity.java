package com.dd.glsc.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dd.common.constant.WareConstant;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 采购信息
 *
 *
 * @author dd
 * @email 18211882344@163.com
 * @date 2025-12-05 15:50:11
 */
@Data
@TableName("wms_purchase")
public class PurchaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 采购单id
     */
    @TableId
    private Long id;
    /**
     * 采购人id
     */
    private Long assigneeId;
    /**
     * 采购人名
     */
    private String assigneeName;
    /**
     * 联系方式
     */
    private String phone;
    /**
     * 优先级
     */
    private Integer priority;
    /**
     * 状态（使用枚举的 code 存储到数据库）
     */
    private Integer status;
    /**
     * 仓库id
     */
    private Long wareId;
    /**
     * 总金额
     */
    private BigDecimal amount;
    /**
     * 创建日期
     */
    private Date createTime;
    /**
     * 更新日期
     */
    private Date updateTime;

    /**
     * 获取状态枚举
     */
    public WareConstant.PurchaseStatusEnum getStatusEnum() {
        return WareConstant.PurchaseStatusEnum.fromCode(this.status);
    }

    /**
     * 设置状态（通过枚举）
     */
    public void setStatusEnum(WareConstant.PurchaseStatusEnum statusEnum) {
        this.status = (statusEnum == null ? null : statusEnum.getCode());
    }
}
