package org.example.lawnmpwer.mower.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.lawnmpwer.mower.entity.LawnmowerInfo;

import java.util.List;

public interface LawnmowerInfoService extends IService<LawnmowerInfo> {
    List<LawnmowerInfo> getMowerListWithStatusCheck();

    // 新增：根据 MQTT 消息自动新增/更新除草机
    void saveOrUpdateFromMqtt(String robotId, String mowerName, String ipAddress, Integer status);
}