package com.dd.glsc.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dd.common.utils.PageUtils;
import com.dd.glsc.ware.entity.PurchaseEntity;
import com.dd.glsc.ware.entity.dto.PurchaseMergeDTO;

import java.util.Map;

/**
 * 采购信息
 *
 * @author dd
 * @email 18211882344@163.com
 * @date 2025-12-05 15:50:11
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 合并采购需求到采购单
     * @param purchaseMergeDTO
     */
    void mergePurchase(PurchaseMergeDTO purchaseMergeDTO);

    /**
     * 获取所有新建或已分配的采购单列表
     * @param params
     * @return
     */
    PageUtils listByUnreceive(Map<String, Object> params);
}

