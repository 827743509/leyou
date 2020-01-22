package com.leyou.item.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;

import java.util.List;

public interface GoodsService {
    void saveGoods(SpuBo spuBo);

    SpuDetail querySpuDetailBySpuId(Long id);

    List<Sku> querySkusBySpuId(Long id);

    void updateGoods(SpuBo spuBo);

    PageResult<SpuBo> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows);

    Spu querySpuById(Long id);
}
