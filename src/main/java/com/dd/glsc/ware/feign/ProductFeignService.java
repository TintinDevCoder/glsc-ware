package com.dd.glsc.ware.feign;

import com.dd.common.common.BaseResponse;
import com.dd.common.to.SkuInfoTO;
import com.dd.common.to.SkuTotalPriceTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 远程调用优惠券服务
 */
@FeignClient("glsc-product")
public interface ProductFeignService {
    /**
     * 条件查询sku
     * @param params
     * @return
     */
    @RequestMapping("/product/skuinfo/get/sku")
    public BaseResponse<List<SkuInfoTO>> getSkuOnConditon(@RequestParam Map<String, Object> params);

    /**
     * 计算sku总价
     * @param skus
     * @return
     */
    @RequestMapping("/product/skuinfo/getTotcalPrice")
    public BigDecimal getTotcalPrice(@RequestBody List<SkuTotalPriceTO> skus);
}
