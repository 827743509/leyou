package com.leyou.item.service;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;

import java.util.List;

public interface SpecificationService {
    List<SpecGroup> querySpecGroup(Long cid);

    List<SpecParam> querySpecParam(Long gid,Long cid,Boolean generic,Boolean searching);

}
