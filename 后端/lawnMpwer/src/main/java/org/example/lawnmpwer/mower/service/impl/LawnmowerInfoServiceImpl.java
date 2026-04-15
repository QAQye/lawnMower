package org.example.lawnmpwer.mower.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.lawnmpwer.mower.entity.LawnmowerInfo;
import org.example.lawnmpwer.mower.mapper.LawnmowerInfoMapper;
import org.example.lawnmpwer.mower.service.LawnmowerInfoService;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LawnmowerInfoServiceImpl
        extends ServiceImpl<LawnmowerInfoMapper, LawnmowerInfo>
        implements LawnmowerInfoService {

    @Override
    public List<LawnmowerInfo> getMowerListWithStatusCheck() {
        List<LawnmowerInfo> mowerList = this.list();

        for (LawnmowerInfo mower : mowerList) {
            try {
                InetAddress address = InetAddress.getByName(mower.getIpAddress());
                boolean reachable = address.isReachable(1000);

                if (reachable) {
                    mower.setStatus(1); // 在线
                } else {
                    mower.setStatus(0); // 离线
                }
            } catch (Exception e) {
                mower.setStatus(3); // 异常
            }

            this.updateById(mower);
        }

        return mowerList;
    }

    @Override
    public void saveOrUpdateFromMqtt(String robotId, String mowerName, String ipAddress, Integer status) {
        String finalRobotId = safeTrim(robotId);
        String finalName = safeTrim(mowerName);
        String finalIp = safeTrim(ipAddress);
        Integer finalStatus = (status == null) ? 1 : status;

        // 名称兜底：没有 mowerName 就用 robotId
        if ((finalName == null || finalName.isEmpty()) && finalRobotId != null && !finalRobotId.isEmpty()) {
            finalName = finalRobotId;
        }

        // robotId、name、ip 全都没有，就没必要入库
        if ((finalRobotId == null || finalRobotId.isEmpty())
                && (finalName == null || finalName.isEmpty())
                && (finalIp == null || finalIp.isEmpty())) {
            return;
        }

        LawnmowerInfo existing = null;

        // 先按 IP 查
        if (finalIp != null && !finalIp.isEmpty()) {
            existing = this.getOne(
                    Wrappers.<LawnmowerInfo>lambdaQuery()
                            .eq(LawnmowerInfo::getIpAddress, finalIp),
                    false
            );
        }

        // IP 没查到，再按 name 查
        if (existing == null && finalName != null && !finalName.isEmpty()) {
            existing = this.getOne(
                    Wrappers.<LawnmowerInfo>lambdaQuery()
                            .eq(LawnmowerInfo::getName, finalName),
                    false
            );
        }

        LocalDateTime now = LocalDateTime.now();

        if (existing == null) {
            LawnmowerInfo mower = new LawnmowerInfo();
            mower.setName(finalName);
            mower.setIpAddress(finalIp);
            mower.setStatus(finalStatus);
            mower.setTasksTime(0.0);
            mower.setCreateTime(now);
            mower.setUpdateTime(now);
            this.save(mower);
            return;
        }

        // 已存在就更新
        if (finalName != null && !finalName.isEmpty()) {
            existing.setName(finalName);
        }
        if (finalIp != null && !finalIp.isEmpty()) {
            existing.setIpAddress(finalIp);
        }
        existing.setStatus(finalStatus);
        existing.setUpdateTime(now);

        this.updateById(existing);
    }

    private String safeTrim(String text) {
        if (text == null) {
            return null;
        }
        text = text.trim();
        return text.isEmpty() ? null : text;
    }
}