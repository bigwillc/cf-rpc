package com.bigwillc.cfrpcdemoprovider;

import com.bigwillc.cfrpccore.annotation.CFProvider;
import com.bigwillc.cfrpcdemoapi.User;
import com.bigwillc.cfrpcdemoapi.UserService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@CFProvider
public class UserServiceImpl implements UserService {

    @Value("#{${app.metas}}")
    Map<String, String> metas;

    @Autowired
    Environment environment;


    @Override
    public User findById(int id) {
        return new User(id, "bigwillc-" + environment.getProperty("server.port") + "_" +System.currentTimeMillis());
    }

    @Override
    public User findById(long id) {
        return new User((int) id, "bigwillc-" + environment.getProperty("server.port") + "_" +System.currentTimeMillis());
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
    public String getName(int id) {
        return "bigwillc-" + id + "-" + System.currentTimeMillis();
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

    @Override
    public List<User> getList(List<User> list) {
        // 遍历list，对每个name 拼接“—”加时间戳
        list.forEach(user -> {
            user.setName(user.getName() + "-" + System.currentTimeMillis());
        });
        return list;
    }

    @Override
    public Map<String, User> getMap(Map<String, User> map) {
        // 遍历map，对每个value 值里面的User拼接“—”加时间戳
        map.forEach((k, v) -> {
            v.setName(v.getName() + "-" + System.currentTimeMillis());
        });
        return map;
    }

    @Override
    public boolean getFlag(boolean flag) {
        return !flag;
    }

    @Override
    public User[] findUsers(User[] users) {
        // 对传入的user[]的name拼接时间戳
        for (User user : users) {
            user.setName(user.getName() + "-" + System.currentTimeMillis());
        }
        return users;
    }

    @Override
    public User ex(boolean flag) {
        if(flag) throw new RuntimeException("just throw an exception");
        return new User(1002, "CF1002");
    }

    String timeoutPorts = "8081,8085";

    @Override
    public User find(int timeout) {
        String port = environment.getProperty("server.port");
        if (Arrays.stream(timeoutPorts.split(",")).anyMatch(port::equals)) {
            try {
                Thread.sleep(timeout);
            } catch (Exception e) {
                throw new RuntimeException();
            }

        }
        return new User(1001, "CF1001" + "_" + port);
    }

    @Override
    public String setTimeoutPort(String timeoutPort) {
        setTimeoutPorts(timeoutPort);
        return timeoutPort + "modify success!";
    }

    @Override
    public String getVersion() {
        return metas.get("version") + "-" + System.currentTimeMillis();
    }

    public void setTimeoutPorts(String timeoutPorts) {
        this.timeoutPorts = timeoutPorts;
    }


}
