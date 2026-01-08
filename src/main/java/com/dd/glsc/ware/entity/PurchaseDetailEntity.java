package com.dd.glsc.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;

import com.dd.common.valid.group.AddGroup;
import com.dd.common.valid.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * 
 * 
 * @author dd
 * @email 18211882344@163.com
 * @date 2025-12-05 15:50:11
 */
@Data
@TableName("wms_purchase_detail")
public class PurchaseDetailEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
    @Null(message = "新增不能指定id", groups = {AddGroup.class})
    @NotNull(message = "修改id必须有值", groups = {UpdateGroup.class})
	private Long id;
	/**
	 * 采购单id
	 */
	private Long purchaseId;
	/**
	 * 采购商品id
	 */
	private Long skuId;
	/**
	 * 采购数量
	 */
    @NotNull(message = "商品数量必须有值", groups = {AddGroup.class})
	private Integer skuNum;
	/**
	 * 采购金额
	 */
	private BigDecimal skuPrice;
	/**
	 * 仓库id
	 */
    @NotNull(message = "必须指定仓库", groups = {AddGroup.class})
	private Long wareId;
	/**
	 * 状态[0新建，1已分配，2正在采购，3已完成，4采购失败]
	 */
	private Integer status;

}
