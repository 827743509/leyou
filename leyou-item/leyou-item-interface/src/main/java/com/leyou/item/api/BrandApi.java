package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("brand")
public interface BrandApi {
    //查询品牌信息的方法
    @GetMapping("page")
    public PageResult<Brand> queryBrand(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy",defaultValue ="id") String sortBy,
            @RequestParam(value = "desc",defaultValue = "false") Boolean desc
    );

    //根据品牌id查询品牌
    @GetMapping("{id}")
    public  Brand  queryBrandById(@PathVariable("id")Long id);

}
