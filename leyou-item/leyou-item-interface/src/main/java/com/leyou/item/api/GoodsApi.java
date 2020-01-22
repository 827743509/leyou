package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping
public interface GoodsApi {


    //根据SpuId查询商品详情
    @GetMapping("spu/detail/{id}")
   public  SpuDetail querySpuDetailBySpuId(@PathVariable("id")Long id);
    //根据SpuId查询skus
    @GetMapping("sku/list")
    public List<Sku> querySkuBySpuId(@RequestParam("id") Long id);
    @GetMapping("spu/page")
    public  PageResult<SpuBo> querySpu(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows
    );
    //根据spuId查询spu
    @GetMapping("{id}")
    public Spu querySpuById(@PathVariable("id") Long id);
}
