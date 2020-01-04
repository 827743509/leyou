package com.leyou.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {



   public List<Brand> selectBrand( @Param("key") String key,@Param("sortBy") String sortBy,@Param("sort") String sort);
}
