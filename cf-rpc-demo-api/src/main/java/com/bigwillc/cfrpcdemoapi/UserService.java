package com.bigwillc.cfrpcdemoapi;

import java.util.List;
import java.util.Map;

public interface UserService {
    User findById(int id);

    User findById(long id);

    User findById(int id, String name);

//    User findById(long id);

    long getId(long id);

    long getId(User user);

    long getId(Float id);

    String getName();

    String getName(int id);

    int[] getIds();

    long[] getLongIds();

    int[] getIds(int[] ids);

    List<User> getList(List<User> list);

    Map<String, User> getMap(Map<String, User> map);

    boolean getFlag(boolean flag);
    User[] findUsers(User[] users);

    User ex(boolean flag);

    User find(int timeout);

    String setTimeoutPort(String timeoutPort);

    String getVersion();

}
