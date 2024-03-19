package com.bigwillc.cfrpcdemoapi;

import java.util.List;
import java.util.Map;

public interface UserService {
    User findById(int id);

    User findById(int id, String name);

//    User findById(long id);

    long getId(long id);

    long getId(User user);

    long getId(Float id);

    String getName();

    int[] getIds();

    long[] getLongIds();

    int[] getIds(int[] ids);

    List<User> getUserList(List<User> list);

    Map<String, User> getUserMap(Map<String, User> map);

}
