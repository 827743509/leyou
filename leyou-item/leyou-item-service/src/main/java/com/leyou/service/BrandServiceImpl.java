package com.leyou.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.mapper.BrandMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandServiceImpl implements  BrandService {
    @Autowired
    private  BrandMapper brandMapper;
    @Override
    public PageResult<Brand> queryBrand(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
         // 开始分页
        PageHelper.startPage(page, rows);
        String sort=desc?"desc":"asc";
        List<Brand> list = brandMapper.selectBrand(key, sortBy, sort);
        PageInfo<Brand> info= new PageInfo<>(list);
        return new PageResult<Brand>(info.getTotal(),info.getList());
    }
}
