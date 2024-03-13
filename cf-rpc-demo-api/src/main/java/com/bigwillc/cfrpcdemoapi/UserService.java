package com.bigwillc.cfrpcdemoapi;

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


}
