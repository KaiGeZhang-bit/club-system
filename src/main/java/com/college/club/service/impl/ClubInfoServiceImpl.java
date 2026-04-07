package com.college.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.college.club.common.exception.BusinessException;
import com.college.club.common.vo.ClubInfoVO;
import com.college.club.common.vo.PageVO;
import com.college.club.common.vo.Result;
import com.college.club.dto.ClubAuditDTO;
import com.college.club.dto.CreateClubDTO;
import com.college.club.entity.ClubInfo;
import com.college.club.entity.SysUser;
import com.college.club.entity.UserClubRelation;
import com.college.club.mapper.ClubInfoMapper;
import com.college.club.mapper.UserClubRelationMapper;
import com.college.club.service.ClubInfoService;
import com.college.club.service.SysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClubInfoServiceImpl extends ServiceImpl<ClubInfoMapper, ClubInfo> implements ClubInfoService {

    @Resource
    private ClubInfoMapper clubInfoMapper;
    @Resource
    private SysUserService sysUserService;
    private static final Integer ROLE_TEACHER = 2;

    @Resource
    private UserClubRelationMapper userClubRelationMapper;

    /**
     * 创建社团（无需审核，创建后状态为正常）
     */
    @Override
    @CacheEvict(value = "clubList", allEntries = true)
    public Result<?> createClub(CreateClubDTO dto, Long userId) {

        //校验该用户是否是社团负责人
        if(userId == 0){
            throw BusinessException.businessError("该用户无权进行该操作！");
        }

        // 1. 校验：社团名称是否重复（唯一约束）
        QueryWrapper<ClubInfo> query = new QueryWrapper<>();
        query.eq("club_name", dto.getClubName());
        if (clubInfoMapper.selectCount(query) > 0) {
            throw BusinessException.businessError("社团名称已存在，无法重复创建");
        }

        // 2. 校验：社团类型是否合法（可选，根据实际类型列表限制）
        // 示例：仅允许3种类型，可根据你的业务扩展
        if (!"学术科技类".equals(dto.getClubType())
                && !"文化体育类".equals(dto.getClubType())
                && !"公益服务类".equals(dto.getClubType())) {
            throw BusinessException.businessError("社团类型只能是：学术科技类、文化体育类、公益服务类");
        }

        // 3. 构建社团实体（复制DTO字段 + 设置默认值）
        ClubInfo club = new ClubInfo();
        BeanUtils.copyProperties(dto, club); // 复制所有字段（名称、类型、负责人、老师等）
        club.setStatus(0); // 无需审核，直接设为“正常（1）”
        club.setCreateTime(LocalDateTime.now());
        club.setUpdateTime(LocalDateTime.now());
        // 🔥🔥🔥 只加这一行！！！（把当前登录用户ID设为负责人ID）
        club.setLeaderId(userId);


        // 4. 保存到数据库
        clubInfoMapper.insert(club);

        // 替换原来的返回行，改为单参数（匹配你之前的Result使用方式）
        return Result.success("社团创建成功，ID：" + club.getId());
    }

    @Override
    @CacheEvict(value = "clubList", allEntries = true)
    public Result<String> auditClub(ClubAuditDTO dto) {
        // 1. 获取当前登录用户
        SysUser currentUser = sysUserService.getCurrentUser();
        Long auditorId = currentUser.getId(); // 审核人ID（老师ID）
        Integer userRole = currentUser.getRole(); // 当前用户角色
        String username = currentUser.getUsername(); // 当前用户名

        // 2. 校验当前用户是否是老师（role=2）
        if (!ROLE_TEACHER.equals(userRole)) {
            String roleDesc = switch (userRole) {
                case 0 -> "普通成员";
                case 1 -> "社团负责人";
                case 2 -> "老师";
                default -> "未知角色";
            };
            throw BusinessException.businessError("用户" + username + "（角色：" + roleDesc + "）无社团审核权限，仅老师可操作");
        }

        // 3. 查询社团信息
        ClubInfo club = clubInfoMapper.selectById(dto.getClubId());
        if (club == null) {
            throw BusinessException.businessError("社团不存在，无法审核");
        }

        // 4. 校验社团状态是否为待审核（status=0）
        if (club.getStatus() != 0) {
            String statusDesc = switch (club.getStatus()) {
                case 1 -> "正常";
                case 2 -> "解散";
                default -> "未知状态";
            };
            throw BusinessException.businessError("该社团当前状态为" + statusDesc + "，无法审核");
        }

        // 5. 处理审核操作（1=通过→status=1，2=驳回→status=2）
        Integer targetStatus;
        String msg;
        if (dto.getAuditAction() == 1) {
            targetStatus = 1;
            msg = "社团审核通过，状态更新为正常";
        } else if (dto.getAuditAction() == 2) {
            if (dto.getAuditRemark() == null || dto.getAuditRemark().trim().isEmpty()) {
                throw BusinessException.businessError("驳回社团时，审核备注不能为空");
            }
            targetStatus = 2;
            msg = "社团审核驳回，状态更新为解散";
        } else {
            throw BusinessException.businessError("审核操作不合法，只能是1（通过）或2（驳回）");
        }

        // 6. 更新社团审核信息
        int affectedRows = clubInfoMapper.updateClubAuditInfo(
                dto.getClubId(),
                targetStatus,
                auditorId,
                dto.getAuditRemark(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // 7. 返回结果
        if (affectedRows > 0) {
            return Result.success(msg);
        } else {
            throw BusinessException.businessError("社团审核失败，请重试");
        }
    }

    /**
     * 修改社团信息
     *
     * @param dto 社团信息DTO
     * @return 操作结果
     */
    @Override
    @CacheEvict(value = "clubList", allEntries = true)
    public Result<?> updateClub(CreateClubDTO dto) {
        // 1. 校验：社团是否存在
       QueryWrapper<ClubInfo> query = new QueryWrapper<>();
       query.eq("club_name", dto.getClubName());
       ClubInfo club = clubInfoMapper.selectOne(query);
       if (clubInfoMapper.selectCount(query) <= 0) {
           throw BusinessException.businessError("社团不存在，无法修改");


       }
        // 2. 校验：社团类型是否合法（可选，根据实际类型列表限制）
        // 示例：仅允许3种类型，可根据你的业务扩展
        if (!"学术科技类".equals(dto.getClubType())
                && !"文化体育类".equals(dto.getClubType())
                && !"公益服务类".equals(dto.getClubType())) {
            throw BusinessException.businessError("社团类型只能是：学术科技类、文化体育类、公益服务类");
        }
        //3.检验登录用户是否是社团负责人
        SysUser currentUser = sysUserService.getCurrentUser();

        Long leaderId = currentUser.getId();
        if (!leaderId.equals(club.getLeaderId())) {
            throw BusinessException.businessError("您不是该社团的负责人，无法修改");
        }
        // 4. 更新社团信息
        BeanUtils.copyProperties(dto, club); // 复制所有字段（名称、类型、负责人、老师等）
        club.setUpdateTime(LocalDateTime.now());
        clubInfoMapper.updateById(club);

        return Result.success("社团信息修改成功");
    }


    @Override
    @Cacheable(value = "clubList", key = "(#status == null ? 'null' : #status.toString()) + '_' + #pageNum + '_' + #pageSize")
    public Result<PageVO<ClubInfoVO>> getClubList(Integer status, Integer pageNum, Integer pageSize) {
        System.out.println("=== getClubList 被执行了 ===");

        String key = (status == null ? "null" : status.toString()) + "_" + pageNum + "_" + pageSize;
        System.out.println("生成的缓存 key: " + key);

        System.out.println("===== 执行数据库查询，参数：status=" + status + ", pageNum=" + pageNum + ", pageSize=" + pageSize + " =====");
        // 1. 设置分页参数
        Page<ClubInfo> page = new Page<>(pageNum, pageSize);

        // 2. 构造查询条件
        LambdaQueryWrapper<ClubInfo> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ClubInfo::getStatus, status);
        }
        wrapper.orderByDesc(ClubInfo::getCreateTime);

        // 3. 执行分页查询
        IPage<ClubInfo> pageResult = this.page(page, wrapper);

        // 4. 转换为 VO 列表
        List<ClubInfoVO> voList = pageResult.getRecords().stream().map(club -> {
            ClubInfoVO vo = new ClubInfoVO();
            BeanUtils.copyProperties(club, vo);
            // 状态描述
            switch (club.getStatus()) {
                case 0: vo.setStatusDesc("待审核"); break;
                case 1: vo.setStatusDesc("正常"); break;
                case 2: vo.setStatusDesc("解散"); break;
                default: vo.setStatusDesc("未知");
            }
            return vo;
        }).collect(Collectors.toList());

        // 5. 封装分页 VO
        PageVO<ClubInfoVO> pageVO = new PageVO<>();
        pageVO.setRecords(voList);
        pageVO.setTotal(pageResult.getTotal());
        pageVO.setPages(pageResult.getPages());
        pageVO.setCurrent(pageNum);
        pageVO.setSize(pageSize);

        return Result.success(pageVO);
    }

    @Override
    public Result<ClubInfoVO> getClubDetail(Long id) {
        ClubInfo club = baseMapper.selectById(id);
        if (club == null) {
            throw BusinessException.businessError("社团不存在");
        }
        ClubInfoVO vo = new ClubInfoVO();
        BeanUtils.copyProperties(club, vo);
        // 状态描述
        switch (club.getStatus()) {
            case 0: vo.setStatusDesc("待审核"); break;
            case 1: vo.setStatusDesc("正常"); break;
            case 2: vo.setStatusDesc("解散"); break;
            default: vo.setStatusDesc("未知");
        }
        return Result.success(vo);
    }

    @Override
    public Result<PageVO<ClubInfoVO>> getAuditList(Integer pageNum, Integer pageSize) {
        // 1. 获取当前登录用户，校验是否为老师
        SysUser currentUser = sysUserService.getCurrentUser();
        if (currentUser.getRole() != 2) {
            throw BusinessException.businessError("仅老师可查看待审核社团列表");
        }

        // 2. 分页查询状态为0（待审核）的社团
        Page<ClubInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ClubInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubInfo::getStatus, 0);
        wrapper.orderByDesc(ClubInfo::getCreateTime);

        IPage<ClubInfo> pageResult = this.page(page, wrapper);

        // 3. 转换为VO
        List<ClubInfoVO> voList = pageResult.getRecords().stream().map(club -> {
            ClubInfoVO vo = new ClubInfoVO();
            BeanUtils.copyProperties(club, vo);
            vo.setStatusDesc("待审核");
            return vo;
        }).collect(Collectors.toList());

        // 4. 封装分页VO
        PageVO<ClubInfoVO> pageVO = new PageVO<>();
        pageVO.setRecords(voList);
        pageVO.setTotal(pageResult.getTotal());
        pageVO.setPages(pageResult.getPages());
        pageVO.setCurrent(pageNum);
        pageVO.setSize(pageSize);

        return Result.success(pageVO);
    }




    @Override
    public Result<PageVO<ClubInfoVO>> getMyClubs(Integer pageNum, Integer pageSize) {
        SysUser currentUser = sysUserService.getCurrentUser();
        Long userId = currentUser.getId();

        // 分页查询用户已加入的社团关系（status=1）
        Page<UserClubRelation> relationPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserClubRelation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(UserClubRelation::getUserId, userId)
                .eq(UserClubRelation::getStatus, 1)
                .orderByDesc(UserClubRelation::getJoinTime);
        IPage<UserClubRelation> relationResult = userClubRelationMapper.selectPage(relationPage, relationWrapper);

        // 提取社团ID列表
        List<Long> clubIds = relationResult.getRecords().stream()
                .map(UserClubRelation::getClubId)
                .collect(Collectors.toList());

        if (clubIds.isEmpty()) {
            PageVO<ClubInfoVO> emptyPage = new PageVO<>();
            emptyPage.setRecords(new ArrayList<>());
            emptyPage.setTotal(0);
            emptyPage.setPages(0);
            emptyPage.setCurrent(pageNum);
            emptyPage.setSize(pageSize);
            return Result.success(emptyPage);
        }

        // 查询社团详情
        LambdaQueryWrapper<ClubInfo> clubWrapper = new LambdaQueryWrapper<>();
        clubWrapper.in(ClubInfo::getId, clubIds);
        List<ClubInfo> clubs = this.list(clubWrapper);

        // 转换为 VO
        List<ClubInfoVO> voList = clubs.stream().map(club -> {
            ClubInfoVO vo = new ClubInfoVO();
            BeanUtils.copyProperties(club, vo);
            switch (club.getStatus()) {
                case 0: vo.setStatusDesc("待审核"); break;
                case 1: vo.setStatusDesc("正常"); break;
                case 2: vo.setStatusDesc("解散"); break;
                default: vo.setStatusDesc("未知");
            }
            return vo;
        }).collect(Collectors.toList());

        // 按加入时间排序（保持关系表中的顺序）
        voList.sort(Comparator.comparingInt(a -> clubIds.indexOf(a.getId())));

        PageVO<ClubInfoVO> pageVO = new PageVO<>();
        pageVO.setRecords(voList);
        pageVO.setTotal(relationResult.getTotal());
        pageVO.setPages(relationResult.getPages());
        pageVO.setCurrent(pageNum);
        pageVO.setSize(pageSize);

        return Result.success(pageVO);
    }

    @Override
    public Result<List<ClubInfo>> getMyManageClubs() {
        SysUser user = sysUserService.getCurrentUser();
        // 查询 我是社长的社团（leaderId = 我）
        LambdaQueryWrapper<ClubInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ClubInfo::getLeaderId, user.getId())
                .eq(ClubInfo::getStatus, 1);
        List<ClubInfo> list = clubInfoMapper.selectList(wrapper);
        return Result.success(list);
    }





}