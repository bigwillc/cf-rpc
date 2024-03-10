package com.bigwillc.cfrpcdemoprovider;

import com.bigwillc.cfrpccore.annotation.CFProvider;
import com.bigwillc.cfrpcdemoapi.User;
import com.bigwillc.cfrpcdemoapi.UserService;
import org.springframework.stereotype.Component;

@Component
@CFProvider
public class UserServiceImpl implements UserService {
    @Override
    public User findById(int id) {
        return new User(id, "bigwillc-"+System.currentTimeMillis());
    }
}
