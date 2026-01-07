package com.college.club.common.vo;

import lombok.Data;
import java.util.List;

/**
 * 通用分页返回VO
 * @param <T> 数据类型
 */
@Data
public class PageVO<T> {
    private List<T> records; // 当前页数据列表
    private long total; // 总记录数
    private long pages; // 总页数
    private Integer current; // 当前页码
    private Integer size; // 每页条数
}