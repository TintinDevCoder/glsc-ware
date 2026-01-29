package com.dd.glsc.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dd.common.to.WareSkuTO;
import com.dd.common.utils.PageUtils;
import com.dd.glsc.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author dd
 * @email 18211882344@163.com
 * @date 2025-12-05 15:50:11
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 条件分页查询商品库存
     * @param params
     * @return
     */
    PageUtils queryPageOnCondition(Map<String, Object> params);

    /**
     * 添加库存
     * @param skuId
     * @param wareId
     * @param skuNum
     */
    void addStock(Long skuId, Long wareId, Integer skuNum);

    /**
     * 根据skuId查询库存数量
     * @param skuIds
     * @return
     */
    List<WareSkuTO> getStackBySkuId(List<Long> skuIds);
}

