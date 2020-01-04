package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.mapper.BrandMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void addBrand(Brand brand, List<Long> cids) {
        //保存品牌信息
        brandMapper.insertSelective(brand);
        cids.forEach(cid->brandMapper.insertBrandAndCategory(brand.getId(),cid));
    }
}
