package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class GoodsController {
    @Autowired
   private GoodsService goodsService;
    //商品新增
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){
        try {
            goodsService.saveGoods(spuBo);
            return  ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


    }
    //根据SpuId查询商品详情
    @GetMapping("spu/detail/{id}")
   public  ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("id")Long id){
     SpuDetail spuDetail=goodsService.querySpuDetailBySpuId(id);
     if (spuDetail==null){return  ResponseEntity.notFound().build();}
     return  ResponseEntity.ok(spuDetail);
   }
   //根据SpuId查询sku
    @GetMapping("sku/list")
   public  ResponseEntity<List<Sku>>  querySkuBySpuId(@RequestParam("id") Long id){
        List<Sku> skus= goodsService.querySkusBySpuId(id);
        if(CollectionUtils.isEmpty(skus)){return  ResponseEntity.notFound().build();}
        return  ResponseEntity.ok(skus);
    }
    //商品修改
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        try {
            goodsService.updateGoods(spuBo);
            return  ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            e.printStackTrace();
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpu(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows
    ){
        PageResult<SpuBo> result=goodsService.querySpuByPage(key,saleable,page,rows);
        if(result==null|| CollectionUtils.isEmpty(result.getItems())){return  ResponseEntity.notFound().build();}
        return  ResponseEntity.ok(result);
    }
    @GetMapping("{id}")
    public  ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){
       Spu spu= this.goodsService.querySpuById(id);
        if(spu==null){return  ResponseEntity.notFound().build();}
        return  ResponseEntity.ok(spu);
    }
}
