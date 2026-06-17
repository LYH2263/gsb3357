package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.entity.User;
import com.school.mapper.TeacherMapper;
import com.school.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
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

    private User approvedUser;
    private User pendingUser;

    @BeforeEach
    void setUp() {
        approvedUser = new User();
        approvedUser.setUid(1);
        approvedUser.setUsername("zhangsan");
        approvedUser.setUserpassword("123456");
        approvedUser.setCheckedok("已通过");

        pendingUser = new User();
        pendingUser.setUid(2);
        pendingUser.setUsername("lisi");
        pendingUser.setUserpassword("123456");
        pendingUser.setCheckedok("待审核");
    }

    @Test
    void should_fail_when_student_password_wrong() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        Map<String, Object> result = authService.login("zhangsan", "wrongpwd", "student");

        assertFalse((boolean) result.get("success"));
        assertEquals("用户名或密码错误", result.get("message"));
        verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void should_fail_when_student_pending() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(pendingUser);

        Map<String, Object> result = authService.login("lisi", "123456", "student");

        assertFalse((boolean) result.get("success"));
        assertEquals("账号审核中，请联系老师批准", result.get("message"));
        verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void should_succeed_when_student_approved() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(approvedUser);

        Map<String, Object> result = authService.login("zhangsan", "123456", "student");

        assertTrue((boolean) result.get("success"));
        assertEquals(approvedUser, result.get("user"));
        assertEquals("student", result.get("role"));
        verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void should_fail_when_username_or_userno_exists() {
        User existing = new User();
        existing.setUid(10);
        existing.setUsername("zhangsan");
        existing.setUserno("2024001");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        User newUser = new User();
        newUser.setUsername("zhangsan");
        newUser.setUserno("2024001");
        newUser.setUserpassword("abc");

        Map<String, Object> result = authService.registerStudent(newUser);

        assertFalse((boolean) result.get("success"));
        assertEquals("用户名或学号已存在", result.get("message"));
        verify(userMapper, never()).insert(any(User.class));
    }
}
