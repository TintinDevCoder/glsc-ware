package com.dd.glsc.ware.dao;

import com.dd.common.to.WareSkuTO;
import com.dd.glsc.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author dd
 * @email 18211882344@163.com
 * @date 2025-12-05 15:50:11
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    /**
     * 根据sku_id集合查询每个sku的总可用库存
     */
    List<WareSkuTO> getSkuStockByIds(@Param("skuIds") List<Long> skuIds);
}
