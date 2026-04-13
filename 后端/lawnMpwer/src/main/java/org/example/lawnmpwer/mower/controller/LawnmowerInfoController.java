package org.example.lawnmpwer.mower.controller;

import org.example.lawnmpwer.mower.entity.LawnmowerInfo;
import org.example.lawnmpwer.mower.service.LawnmowerInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/mower")
public class LawnmowerInfoController {

    @Autowired
    private LawnmowerInfoService mowerService;

    // 获取列表（附带动态 Ping 检测）
    @GetMapping("/list")
    public List<LawnmowerInfo> getList() {
        return mowerService.getMowerListWithStatusCheck();
    }

    // 新增
    @PostMapping("/add")
    public boolean add(@RequestBody LawnmowerInfo mower) {
        mower.setStatus(0); // 默认离线
        mower.setTasksTime(0.0);
        mower.setCreateTime(LocalDateTime.now());
        mower.setUpdateTime(LocalDateTime.now());
        return mowerService.save(mower);
    }

    // 修改
    @PutMapping("/update")
    public boolean update(@RequestBody LawnmowerInfo mower) {
        mower.setUpdateTime(LocalDateTime.now());
        return mowerService.updateById(mower);
    }

    // 删除
    @DeleteMapping("/delete/{id}")
    public boolean delete(@PathVariable Long id) {
        return mowerService.removeById(id);
    }
}