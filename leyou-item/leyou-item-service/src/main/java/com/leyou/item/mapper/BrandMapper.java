package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {



   public List<Brand> selectBrand( @Param("key") String key,@Param("sortBy") String sortBy,@Param("sort") String sort);
    @Insert("insert into tb_category_brand (category_id,brand_id) values (#{cid} ,#{id} )")
    void insertBrandAndCategory(@Param("id") Long id, @Param("cid")Long cid);
}
