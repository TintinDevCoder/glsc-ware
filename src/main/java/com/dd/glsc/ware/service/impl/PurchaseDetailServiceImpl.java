package com.dd.glsc.ware.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dd.common.common.BaseResponse;
import com.dd.common.common.BusinessException;
import com.dd.common.common.ErrorCode;
import com.dd.common.to.SkuInfoTO;
import com.dd.common.to.SkuTotalPriceTO;
import com.dd.common.utils.PageUtils;
import com.dd.common.utils.Query;
import com.dd.glsc.ware.dao.PurchaseDetailDao;
import com.dd.glsc.ware.entity.PurchaseDetailEntity;
import com.dd.glsc.ware.entity.vo.PurchaseDetailEntityVO;
import com.dd.glsc.ware.feign.ProductFeignService;
import com.dd.glsc.ware.service.PurchaseDetailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {
    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                new QueryWrapper<PurchaseDetailEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageOnCondition(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> purchaseDetailEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StrUtil.isNotEmpty(key)) {
            purchaseDetailEntityQueryWrapper.and(wrapper ->
                    wrapper.lambda().eq(PurchaseDetailEntity::getPurchaseId, key).or().eq(PurchaseDetailEntity::getSkuId, key)
                            .or().eq(PurchaseDetailEntity::getWareId, key)
                            .or().eq(PurchaseDetailEntity::getId, key));
        }
        String status = (String) params.get("status");
        if (StrUtil.isNotEmpty(status)) {
            Integer s = Integer.parseInt(status);
            if (s >= 0 && s <= 4) {
                purchaseDetailEntityQueryWrapper.lambda().eq(PurchaseDetailEntity::getStatus, s);
            }
        }
        String wareId = (String) params.get("wareId");
        if (StrUtil.isNotEmpty(wareId)) {
            purchaseDetailEntityQueryWrapper.lambda().eq(PurchaseDetailEntity::getWareId, Integer.parseInt(wareId));
        }
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                purchaseDetailEntityQueryWrapper
        );
        // 封装返回
        // 查询商品名
        List<PurchaseDetailEntity> records = page.getRecords();
        // 获取sku的id
        List<Long> skuIdList = records.stream().map(record -> record.getSkuId()).collect(Collectors.toList());
        Map<String, Object> param = new HashMap<>();
        // 设定参数
        param.put("ids", skuIdList);
        // 获取 SKU 信息
        BaseResponse skuOnConditon = productFeignService.getSkuOnConditon(param);
        Object data = skuOnConditon.getData();
        // 安全转换远程返回的列表，避免 LinkedHashMap 强转异常
        List<SkuInfoTO> skuInfoList = new ArrayList<>();
        if (data instanceof List<?>) {
            ObjectMapper mapper = new ObjectMapper();
            for (Object item : (List<?>) data) {
                // 远程返回可能是 Map，需要逐个转换为 SkuInfoTO
                SkuInfoTO skuInfo = mapper.convertValue(item, SkuInfoTO.class);
                skuInfoList.add(skuInfo);
            }
        } else {
            throw new IllegalArgumentException("Invalid data returned from SKU service");
        }
        // 组装 SKU ID 和 SKU 名称的映射关系
        Map<Long, String> skuIdNameMap = new HashMap<>();
        for (SkuInfoTO skuInfoTO : skuInfoList) {
            skuIdNameMap.put(skuInfoTO.getSkuId(), skuInfoTO.getSkuName());
        }
        // 封装返回结果
        List<PurchaseDetailEntityVO> purchaseDetailEntityVOS = records.stream().map(record -> {
            PurchaseDetailEntityVO recordVO = new PurchaseDetailEntityVO();
            BeanUtils.copyProperties(record, recordVO);

            // 获取 SKU 名称并赋值
            String skuName = skuIdNameMap.get(record.getSkuId());
            recordVO.setSkuName(skuName != null ? skuName : "未知"); // 对 null 值提供默认
            return recordVO;
        }).collect(Collectors.toList());

        // 创建分页结果
        IPage<PurchaseDetailEntityVO> pager = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        pager.setRecords(purchaseDetailEntityVOS);

        return new PageUtils(pager);
    }

    @Override
    public void savePurchaseDetail(PurchaseDetailEntity purchaseDetail) {
        // 计算采购金额
        // 校验参数
        Long skuId = purchaseDetail.getSkuId();
        Integer skuNum = purchaseDetail.getSkuNum();
        if (skuId == null || skuNum == null || skuNum <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "SKU ID 和数量不能为空且数量必须大于0");
        }
        BigDecimal skuPrice = this.getTotalPriceByPurchaseId(skuId, skuNum);
        purchaseDetail.setSkuPrice(skuPrice);
        this.save(purchaseDetail);
    }

    @Override
    public void updatePurchaseDetail(PurchaseDetailEntity purchaseDetail) {
        // 校验是否可修改
        PurchaseDetailEntity purchase = this.getById(purchaseDetail.getId());
        if (purchase == null || purchase.getStatus() != 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "采购单不存在或已分配");
        }
        // 修改采购金额
        Long skuId = purchaseDetail.getSkuId();
        Integer skuNum = purchaseDetail.getSkuNum();
        if (skuId != null && (skuNum != null && skuNum > 0)) {
            BigDecimal skuPrice = this.getTotalPriceByPurchaseId(skuId, skuNum);
            purchaseDetail.setSkuPrice(skuPrice);
        }
        this.updateById(purchaseDetail);
    }

    /**
     * 根据采购需求计算总价
     * @param skuId,
     * @param skuNum
     * @return
     */
    @Override
    public BigDecimal getTotalPriceByPurchaseId(Long skuId, Integer skuNum) {
        List<SkuTotalPriceTO> skuTotalPriceTOList= new LinkedList<>();
        SkuTotalPriceTO skuTotalPriceTO = new SkuTotalPriceTO();
        skuTotalPriceTO.setSkuId(skuId);
        skuTotalPriceTO.setNum(skuNum);
        skuTotalPriceTOList.add(skuTotalPriceTO);
        BigDecimal totcalPrice = productFeignService.getTotcalPrice(skuTotalPriceTOList);
        return totcalPrice;
    }

    @Override
    public void removePurchaseDetail(List<Long> list) {
        // 校验是否可删除
        List<PurchaseDetailEntity> purchaseDetails = this.listByIds(list);
        for (PurchaseDetailEntity purchaseDetail : purchaseDetails) {
            if (purchaseDetail.getStatus() != 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "采购单已分配，无法删除");
            }
        }
        this.removeByIds(list);
    }


}