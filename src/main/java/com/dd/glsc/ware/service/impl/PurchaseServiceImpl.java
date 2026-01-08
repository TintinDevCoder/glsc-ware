package com.dd.glsc.ware.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dd.common.common.BusinessException;
import com.dd.common.common.ErrorCode;
import com.dd.common.constant.WareConstant;
import com.dd.glsc.ware.entity.PurchaseDetailEntity;
import com.dd.glsc.ware.entity.PurchaseEntity;
import com.dd.glsc.ware.entity.dto.PurchaseMergeDTO;
import com.dd.glsc.ware.service.PurchaseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dd.common.utils.PageUtils;
import com.dd.common.utils.Query;
import com.dd.glsc.ware.dao.PurchaseDao;
import com.dd.glsc.ware.service.PurchaseService;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    private PurchaseDetailService purchaseDetailService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> purchaseEntityQueryWrapper = new QueryWrapper<>();
        String statusStr = (String) params.get("status");
        if (StrUtil.isNotEmpty(statusStr)) {
            Integer statusInt = Integer.valueOf(statusStr);
            WareConstant.PurchaseStatusEnum statusEnum = WareConstant.PurchaseStatusEnum.fromCode(statusInt);
            if (statusEnum != null) {
                // 仍然按整数存库，但通过枚举校验
                purchaseEntityQueryWrapper.lambda().eq(PurchaseEntity::getStatus, statusEnum.getCode());
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

        if (purchaseId == null) {
            PurchaseEntity purchase = new PurchaseEntity();
            // 使用枚举设置新建状态
            purchase.setStatusEnum(WareConstant.PurchaseStatusEnum.NEW);
            purchase.setCreateTime(new Date());
            purchase.setUpdateTime(new Date());
            this.save(purchase);
            purchaseId = purchase.getId();
        }
        // 检查采购单状态，必须是新建或已分配状态
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(PurchaseEntity::getStatus).eq(PurchaseEntity::getId, purchaseId);
        PurchaseEntity purchaseStatusEntity = this.getOne(queryWrapper);
        WareConstant.PurchaseStatusEnum statusEnum = purchaseStatusEntity == null ? null : purchaseStatusEntity.getStatusEnum();
        // 只允许 新建 或 已分配 状态进行合并
        if (statusEnum != WareConstant.PurchaseStatusEnum.NEW && statusEnum != WareConstant.PurchaseStatusEnum.ASSIGNED) {
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
                    .set(PurchaseDetailEntity::getStatus, WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode()); // 使用枚举设置已分配状态
            purchaseDetailService.update(updateWrapper);
        }
    }

    @Override
    public PageUtils listByUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> purchaseEntityQueryWrapper = new QueryWrapper<>();
        // 只查 新建 和 已分配 状态
        purchaseEntityQueryWrapper.lambda()
                .in(PurchaseEntity::getStatus,
                        WareConstant.PurchaseStatusEnum.NEW.getCode(),
                        WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                purchaseEntityQueryWrapper
        );

        return new PageUtils(page);
    }

}