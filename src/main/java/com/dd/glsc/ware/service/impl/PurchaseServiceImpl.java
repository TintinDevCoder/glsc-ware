package com.dd.glsc.ware.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dd.common.common.BusinessException;
import com.dd.common.common.ErrorCode;
import com.dd.glsc.ware.entity.PurchaseDetailEntity;
import com.dd.glsc.ware.entity.dto.PurchaseMergeDTO;
import com.dd.glsc.ware.service.PurchaseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dd.common.utils.PageUtils;
import com.dd.common.utils.Query;

import com.dd.glsc.ware.dao.PurchaseDao;
import com.dd.glsc.ware.entity.PurchaseEntity;
import com.dd.glsc.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    private PurchaseDetailService purchaseDetailService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> purchaseEntityQueryWrapper = new QueryWrapper<>();
        String status = (String) params.get("status");
        if (StrUtil.isNotEmpty(status)) {
            Integer statusInt = Integer.valueOf(status);
            if (statusInt >= 0 && statusInt <= 4) {
                purchaseEntityQueryWrapper.lambda().eq(PurchaseEntity::getStatus, statusInt);
            }
        }
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                purchaseEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 合并采购需求到采购单
     * @param purchaseMergeDTO
     */
    @Override
    @Transactional
    public void mergePurchase(PurchaseMergeDTO purchaseMergeDTO) {
        Long purchaseId = purchaseMergeDTO.getPurchaseId();
        // 检查采购单状态，必须是新建或已分配状态
        if (purchaseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "采购单不能为空");
        }
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(PurchaseEntity::getStatus).eq(PurchaseEntity::getId, purchaseId);
        PurchaseEntity status = this.getOne(queryWrapper);
        if (status.getStatus() != 0 && status.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "采购单状态不允许合并采购需求");
        }
        Long[] items = purchaseMergeDTO.getItems();
        if (items != null && items.length > 0) {
            // 检查采购需求是否已经被分配
            QueryWrapper<PurchaseDetailEntity> purchaseDetailEntityQueryWrapper = new QueryWrapper<>();
            purchaseDetailEntityQueryWrapper.lambda()
                    .in(PurchaseDetailEntity::getId, items)
                    .isNull(PurchaseDetailEntity::getPurchaseId);
            long count = purchaseDetailService.count(purchaseDetailEntityQueryWrapper);
            if (count != items.length) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "部分采购需求已被分配不能重复分配，或是采购需求不存在");
            }
            // 批量更新采购需求
            UpdateWrapper<PurchaseDetailEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda()
                    .in(PurchaseDetailEntity::getId, items)
                    .set(PurchaseDetailEntity::getPurchaseId, purchaseId)
                    .set(PurchaseDetailEntity::getStatus, 1); // 采购需求状态
            purchaseDetailService.update(updateWrapper);
        }
    }

    @Override
    public PageUtils listByUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> purchaseEntityQueryWrapper = new QueryWrapper<>();
        purchaseEntityQueryWrapper.lambda().in(PurchaseEntity::getStatus, 0, 1);

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                purchaseEntityQueryWrapper
        );

        return new PageUtils(page);
    }

}