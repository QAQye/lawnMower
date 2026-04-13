package org.example.lawnmpwer.mower.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.lawnmpwer.mower.entity.LawnmowerInfo;
import org.example.lawnmpwer.mower.mapper.LawnmowerInfoMapper;
import org.example.lawnmpwer.mower.service.LawnmowerInfoService;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

@Service
public class LawnmowerInfoServiceImpl extends ServiceImpl<LawnmowerInfoMapper, LawnmowerInfo> implements LawnmowerInfoService {

    @Override
    public List<LawnmowerInfo> getMowerListWithStatusCheck() {
        List<LawnmowerInfo> mowers = this.list();

        for (LawnmowerInfo mower : mowers) {
            boolean isPingable = false;
            boolean isPortOpen = false;

            try {
                // 1. 先进行 Ping 测试 (500ms 超时)
                InetAddress address = InetAddress.getByName(mower.getIpAddress());
                isPingable = address.isReachable(500);

//                // 2. 如果 Ping 通了，再去探测小车控制端口 (65432) 是否开启
//                if (isPingable) {
//                    try (Socket socket = new Socket()) {
//                        socket.connect(new InetSocketAddress(mower.getIpAddress(), 65432), 500);
//                        isPortOpen = true;
//                    } catch (Exception e) {
//                        isPortOpen = false; // 端口未开启或拒绝连接
//                    }
//                }
            } catch (Exception e) {
                isPingable = false;
            }

            // 3. 核心逻辑判断
            int currentStatus = 0; // 默认 0-离线
            if (isPingable) {
                currentStatus = 1; // 1-在线 (正常可用)
            }

            // 4. 更新到数据库
            if (mower.getStatus() == null || mower.getStatus() != currentStatus) {
                mower.setStatus(currentStatus);
                this.updateById(mower);
            }
        }
        return mowers;
    }
}