package com.kylin.electricassistsys.controller.tjisuan;

import com.baomidou.mybatisplus.plugins.Page;
import com.kylin.electricassistsys.data.api.tjisuan.TJisuanByqnDataApi;
import com.kylin.electricassistsys.dto.tjisuan.TJisuanByqnDto;
import com.kylin.electricassistsys.dto.tjisuan.TJisuanByqnSelDto;
import com.kylin.electricassistsys.mybeanutils.JSONResult;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;

/**
 * @Auther: cwx
 * @Date: 2018/5/21 16:18
 * @Description: 變壓器N-1信息接口請求類
 */
@RestController
@RequestMapping("/byqn")
@Api(value = "byqn", description = "變壓器N-1信息接口業務請求類", produces = MediaType.APPLICATION_JSON)
public class TJisuanByqnDataController {
    @Resource
    private TJisuanByqnDataApi tJisuanByqnDataApi;
    /**
     * 动态查询變壓器N-1信息进行分页
     * @param dto
     * @return
     */
    @RequestMapping("page")
    public JSONResult getPages(@RequestBody TJisuanByqnSelDto dto){
        JSONResult result =null;
        try {
            Page page =   tJisuanByqnDataApi.selectPage(new Page(dto.getPage(), dto.getLimit()),dto);
            result=JSONResult.success(page);
        }catch (Throwable  e){
            result=JSONResult.failure("服务器错误请联系管理员");
        }
        return result ;
    }

    /**
     *
     * 添加變壓器N-1信息指标数据
     * @return
     */
    @RequestMapping("insert")
    public JSONResult saveData(@RequestBody TJisuanByqnDto dto){
        JSONResult result =null;
        try {
            tJisuanByqnDataApi.insert(dto);
            result=JSONResult.success();
        }catch (Throwable e){
            result=JSONResult.failure("服务器错误请联系管理员");
        }
        return result;

    }
    /**
     *
     * 删除根据id變壓器N-1信息数据
     * @return
     */
    @RequestMapping("del")
    public JSONResult delData(@RequestBody String id){
        JSONResult result =null;
        try {
            tJisuanByqnDataApi.delete(id);
            result=JSONResult.success();
        }catch (Throwable e){
            e.printStackTrace();
            result=JSONResult.failure("服务器错误请联系管理员");
        }
        return result;
    }
    /**
     *
     * 根据id进行批量删除 變壓器N-1信息数据
     * @return
     */
    @RequestMapping("batchDel")
    public JSONResult batchDelData(@RequestBody String ids){
        JSONResult result=null;
        try {
            tJisuanByqnDataApi.deleteBatchIds(ids);
            result=JSONResult.success();
        }catch (Throwable e){
            result=JSONResult.failure("服务器错误请联系管理员");
        }

        return result;
    }
    /**
     *
     * 更新用中压线路N-1校验数据
     * @return
     */
    @RequestMapping("update")
    public JSONResult updateData(@RequestBody TJisuanByqnDto dto){
        JSONResult result=null;
        try {
            tJisuanByqnDataApi.update(dto);
            result=JSONResult.success();
        }catch (Throwable e){
            result=JSONResult.failure("服务器错误请联系管理员");
        }
        return result;
    }

    /**
     *
     * 更新用中压线路N-1校验数据
     * @return
     */
    @RequestMapping("list")
    public JSONResult listData(){
        JSONResult result=null;
        try {
            result=JSONResult.success(tJisuanByqnDataApi.getList());
        }catch (Throwable e){
            result=JSONResult.failure("服务器错误请联系管理员");
        }
        return result;
    }




}
