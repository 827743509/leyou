package com.leyou.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
@Controller
@RequestMapping("brand")
public class BrandController {
    @Autowired
    private BrandService brandService;
    //查询品牌信息的方法
    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrand(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy",defaultValue ="id") String sortBy,
            @RequestParam(value = "desc",defaultValue = "false") Boolean desc
    ){
        PageResult<Brand> result=brandService.queryBrand(key,page,rows,sortBy,desc);
        if(CollectionUtils.isEmpty(result.getItems())){return  ResponseEntity.notFound().build();}
        return ResponseEntity.ok(result);
    }
}
