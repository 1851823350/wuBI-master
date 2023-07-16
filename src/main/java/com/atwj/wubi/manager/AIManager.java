package com.atwj.wubi.manager;

import com.atwj.wubi.common.ErrorCode;
import com.atwj.wubi.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 调用鱼聪明AI接口
 *
 * @author blablalala
 * 
 */
@Service
public class AIManager {

    @Resource
    private YuCongMingClient yuCongMingClient;

    public String doChart(Long modelId, String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        if(response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "返回数据为空");
        }
        return response.getData().getContent();
    }
}
