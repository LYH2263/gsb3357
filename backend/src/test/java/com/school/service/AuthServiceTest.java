package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.entity.User;
import com.school.mapper.TeacherMapper;
import com.school.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private TeacherMapper teacherMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void should_fail_when_student_password_wrong() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        Map<String, Object> result = authService.login("testuser", "wrongpassword", "student");

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户名或密码错误", result.get("message"));
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void should_fail_when_student_pending() {
        User user = new User();
        user.setUsername("testuser");
        user.setUserpassword("password123");
        user.setCheckedok("待审核");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        Map<String, Object> result = authService.login("testuser", "password123", "student");

        assertFalse((Boolean) result.get("success"));
        assertEquals("账号审核中，请联系老师批准", result.get("message"));
        assertNull(result.get("user"));
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void should_success_when_student_approved() {
        User user = new User();
        user.setUsername("testuser");
        user.setUserpassword("password123");
        user.setCheckedok("已通过");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        Map<String, Object> result = authService.login("testuser", "password123", "student");

        assertTrue((Boolean) result.get("success"));
        assertEquals(user, result.get("user"));
        assertEquals("student", result.get("role"));
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void should_fail_when_register_duplicate_username_or_userno() {
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setUserno("2024001");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingUser);

        User newUser = new User();
        newUser.setUsername("existinguser");
        newUser.setUserno("2024001");
        newUser.setUserpassword("password123");

        Map<String, Object> result = authService.registerStudent(newUser);

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户名或学号已存在", result.get("message"));
        verify(userMapper, never()).insert(any(User.class));
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }
}
