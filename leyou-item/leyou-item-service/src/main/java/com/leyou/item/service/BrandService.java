package com.leyou.item.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;

import java.util.List;

public interface BrandService {

    PageResult<Brand> queryBrand(String key, Integer page, Integer rows, String sortBy, Boolean desc);

    void addBrand(Brand brand, List<Long> cids);
}