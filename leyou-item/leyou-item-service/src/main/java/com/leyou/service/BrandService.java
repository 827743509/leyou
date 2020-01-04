package com.leyou.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;

public interface BrandService {

    PageResult<Brand> queryBrand(String key, Integer page, Integer rows, String sortBy, Boolean desc);
}
