package com.dd.glsc.ware.feign;

import com.dd.common.common.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 远程调用优惠券服务
 */
@FeignClient("glsc-product")
public interface ProductFeignService {
    @RequestMapping("/product/skuinfo/get/sku")
    public BaseResponse getSkuOnConditon(@RequestParam Map<String, Object> params);

}
