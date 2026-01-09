package com.dd.glsc.ware.service.impl;

import cn.hutool.core.util.StrUtil;
import com.dd.common.common.BaseResponse;
import com.dd.common.to.SkuInfoTO;
import com.dd.glsc.ware.feign.ProductFeignService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dd.common.utils.PageUtils;
import com.dd.common.utils.Query;

import com.dd.glsc.ware.dao.WareSkuDao;
import com.dd.glsc.ware.entity.WareSkuEntity;
import com.dd.glsc.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    private WareSkuDao wareSkuDao;
    @Autowired
    private ProductFeignService productFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageOnCondition(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wareSkuEntityQueryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if(StrUtil.isNotEmpty(skuId)){
            wareSkuEntityQueryWrapper.lambda().eq(WareSkuEntity::getSkuId,skuId);
        }
        String wareId = (String) params.get("wareId");
        if(StrUtil.isNotEmpty(wareId)){
            wareSkuEntityQueryWrapper.lambda().eq(WareSkuEntity::getWareId,wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wareSkuEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 判断是否存在此库存记录
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WareSkuEntity::getSkuId,skuId)
                        .eq(WareSkuEntity::getWareId, wareId);
        Long count = this.count(queryWrapper);
        if (count > 0) {
            // 存在，更新库存
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }else {{
            // 不存在，新增库存记录
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            // 远程查询sku的名字，如果失败，整个事务无需回滚
            try {
                Map<String, Object> param = new HashMap<>();
                param.put("ids", Arrays.asList(skuId));
                BaseResponse<List<SkuInfoTO>> skuOnConditon = productFeignService.getSkuOnConditon(param);
                List<SkuInfoTO> data = skuOnConditon.getData();
                // 安全转换远程返回的列表，避免 LinkedHashMap 强转异常
                List<SkuInfoTO> skuInfoList = new ArrayList<>();
                for (SkuInfoTO datum : data) {
                    skuInfoList.add(datum);
                }
                if (skuInfoList.size() > 0) {
                    wareSkuEntity.setSkuName(skuInfoList.get(0).getSkuName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.save(wareSkuEntity);
        }}
    }

}