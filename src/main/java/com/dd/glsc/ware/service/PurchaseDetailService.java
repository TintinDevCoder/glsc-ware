package com.dd.glsc.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dd.common.utils.PageUtils;
import com.dd.glsc.ware.entity.PurchaseDetailEntity;

import java.util.Map;

/**
 * 
 *
 * @author dd
 * @email 18211882344@163.com
 * @date 2025-12-05 15:50:11
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据条件分页查询采购需求
     * @param params
     * @return
     */
    PageUtils queryPageOnCondition(Map<String, Object> params);
}

