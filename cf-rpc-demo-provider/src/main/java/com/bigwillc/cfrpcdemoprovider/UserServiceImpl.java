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

    @Override
    public User findById(int id, String name) {
        return new User(id, id +  name + System.currentTimeMillis());
    }

    @Override
    public long getId(long id) {
        return id;
    }

    @Override
    public long getId(User user) {
        return 0;
    }

    @Override
    public long getId(Float id) {
        return 1L;
    }

    @Override
    public String getName() {
        return "bigwillc";
    }

    @Override
    public int[] getIds() {
        return new int[]{1, 2, 3};
    }

    @Override
    public long[] getLongIds() {
        return new long[]{200, 300, 400};
    }

    @Override
    public int[] getIds(int[] ids) {
        return new int[]{3, 2, 1};
    }


}
