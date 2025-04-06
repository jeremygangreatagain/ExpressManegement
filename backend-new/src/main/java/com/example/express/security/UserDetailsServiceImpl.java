package com.example.express.security;

import com.example.express.entity.Staff;
import com.example.express.entity.Store; // Import Store
import com.example.express.entity.User;
import com.example.express.service.StaffService;
import com.example.express.service.StoreService; // Import StoreService
import com.example.express.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 自定义UserDetailsService实现，用于从数据库中加载用户信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private UserService userService;

  @Autowired
  private StaffService staffService;

  @Autowired
  private StoreService storeService; // Inject StoreService

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // 先从用户表中查找
    User user = userService.getByUsername(username);
    if (user != null) {
      // 用户存在，创建UserDetails对象
      Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

      // 根据角色设置权限
      if (user.getRole() == 1) {
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
      } else if (user.getRole() == 2) { // Correctly handle STAFF role from User table
        authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
      } else { // Default to USER for role 0 or any other value
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
      }

      // Return CustomUserDetails for regular users
      return new CustomUserDetails(
          user.getId(),
          user.getUsername(),
          user.getPassword(),
          user.getName(), // Assuming User entity has a name field, adjust if needed
          authorities);
    }

    // 从员工表中查找
    Staff staff = staffService.getByUsername(username);
    if (staff != null) {
      // 员工存在，创建UserDetails对象
      Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));

      // Fetch store details
      String storeName = null;
      if (staff.getStoreId() != null) {
        Store store = storeService.getById(staff.getStoreId());
        if (store != null) {
          storeName = store.getName();
        }
      }

      // Return CustomUserDetails for staff users
      return new CustomUserDetails(
          staff.getId(),
          staff.getUsername(),
          staff.getPassword(),
          staff.getName(),
          staff.getStoreId(),
          storeName,
          authorities);
    }

    // 用户不存在，抛出异常
    throw new UsernameNotFoundException("用户名不存在: " + username);
  }
}
