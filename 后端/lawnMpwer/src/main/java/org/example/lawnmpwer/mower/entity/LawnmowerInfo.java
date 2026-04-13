package org.example.lawnmpwer.mower.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("lawnmower_info")
public class LawnmowerInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String ipAddress;
    private Integer status; // 0-离线, 1-在线
    private Double tasksTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}