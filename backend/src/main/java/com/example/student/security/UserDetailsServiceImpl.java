package com.example.student.security;

import com.example.student.entity.SysUser;
import com.example.student.mapper.SysUserMapper;
import com.example.student.mapper.StudentMapper;
import com.example.student.mapper.TeacherMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户详情服务实现
 *
 * @author example
 */
@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("加载用户信息: {}", username);
        
        // 1. 首先尝试通过用户名查询用户
        SysUser user = sysUserMapper.selectUserByUsername(username);
        
        // 2. 如果找不到用户，尝试通过学号查询学生
        if (user == null) {
            log.info("尝试通过学号查询学生: {}", username);
            Long userId = studentMapper.selectUserIdByStudentNo(username);
            if (userId != null) {
                user = sysUserMapper.selectById(userId);
                log.info("通过学号 {} 找到用户: {}", username, user);
            }
        }
        
        // 3. 如果还是找不到，尝试通过教师编号查询教师
        if (user == null) {
            log.info("尝试通过教师编号查询教师: {}", username);
            Long userId = teacherMapper.selectUserIdByTeacherNo(username);
            if (userId != null) {
                user = sysUserMapper.selectById(userId);
                log.info("通过教师编号 {} 找到用户: {}", username, user);
            }
        }
        
        // 4. 如果都找不到，抛出异常
        if (user == null) {
            log.warn("用户 {} 不存在", username);
            throw new UsernameNotFoundException("用户不存在");
        }

        // 查询用户权限信息
        List<String> permissions = sysUserMapper.selectPermsByUserId(user.getId());
        log.info("用户 {} 的权限列表: {}", username, permissions);

        // 查询用户角色
        List<String> roles = sysUserMapper.selectRoleKeysByUserId(user.getId());
        log.info("用户 {} 的角色列表: {}", username, roles);

        // 构建LoginUser对象
        LoginUser loginUser = new LoginUser(user, permissions);
        loginUser.setRoles(roles);

        return loginUser;
    }
} 