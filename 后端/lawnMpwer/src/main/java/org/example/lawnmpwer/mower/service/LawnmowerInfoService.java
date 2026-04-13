package org.example.lawnmpwer.mower.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.lawnmpwer.mower.entity.LawnmowerInfo;
import java.util.List;

public interface LawnmowerInfoService extends IService<LawnmowerInfo> {
    List<LawnmowerInfo> getMowerListWithStatusCheck();
}