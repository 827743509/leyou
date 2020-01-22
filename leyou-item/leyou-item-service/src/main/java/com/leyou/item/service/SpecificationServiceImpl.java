package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationServiceImpl implements  SpecificationService {
    @Autowired
    private SpecGroupMapper groupMapper;
    @Autowired
    private SpecParamMapper paramMapper;
    public List<SpecGroup> querySpecGroup(Long cid) {
        SpecGroup record=new SpecGroup();
        record.setCid(cid);
        return groupMapper.select(record);

    }

    @Override
    public List<SpecParam> querySpecParam(Long gid,Long cid,Boolean generic,Boolean searching) {
        SpecParam record=new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setGeneric(generic);
        record.setSearching(searching);
        return  paramMapper.select(record);
    }

    @Override
    public List<SpecGroup> queryGroupsWithParam(Long cid) {
        List<SpecGroup> groups = querySpecGroup(cid);
        groups.forEach(group->{
            List<SpecParam> params = querySpecParam(group.getId(), cid, null, null);
            group.setParams(params);
        });
        return groups;
    }

}
