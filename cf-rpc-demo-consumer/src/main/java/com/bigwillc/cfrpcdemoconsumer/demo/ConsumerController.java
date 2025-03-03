package com.bigwillc.cfrpcdemoconsumer.demo;

import com.bigwillc.cfrpccore.annotation.CFConsumer;
import com.bigwillc.cfrpcdemoapi.User;
import com.bigwillc.cfrpcdemoapi.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bigwillc on 2025/2/27
 */
@RestController
@RequestMapping("/api")
public class ConsumerController {

    @CFConsumer
    UserService userService;

    // Case 1: 常规int类型，返回User对象
    @GetMapping("/users/{id}")
    public User findById(@PathVariable("id") int id) {
        return userService.findById(id);
    }

    // Case 2: 测试方法重载，同名方法，参数不同
    @GetMapping("/users/{id}/name/{name}")
    public User findByIdAndName(@PathVariable("id") int id, @PathVariable("name") String name) {
        return userService.findById(id, name);
    }

    // Case 3: 测试返回字符串
    @GetMapping("/users/name")
    public String getName() {
        return userService.getName();
    }

    // Case 4: 测试重载方法返回字符串
    @GetMapping("/users/name/{value}")
    public String getNameById(@PathVariable("value") int value) {
        return userService.getName(value);
    }

    // Case 5: 测试local toString方法
    @GetMapping("/users/toString")
    public String getUserServiceToString() {
        return userService.toString();
    }

    // Case 6: 测试long类型
    @GetMapping("/users/id/{value}")
    public long getId(@PathVariable("value") String value) {
        long id = Long.valueOf(value);
        return userService.getId(id);
    }

    // Case 7: 测试long+float类型
    @GetMapping("/users/id/float/{value}")
    public long getIdFloat(@PathVariable("value") String value) {
        float id = Float.valueOf(value);
        return userService.getId(id);
    }

    // Case 8: 测试参数是User类型
    @PostMapping("/users/id/user")
    public long getIdFromUser(@RequestBody User user) {
        return userService.getId(user);
    }

    // Case 9: 测试返回long[]
    @GetMapping("/users/longIds")
    public long[] getLongIds() {
        return userService.getLongIds();
    }

    // Case 10: 测试参数和返回值都是long[]
    @PostMapping("/users/ids")
    public int[] getIds(@RequestBody int[] ids) {
        return userService.getIds(ids);
    }

    // Case 11: 测试参数和返回值都是List类型
    @PostMapping("/users/list")
    public List<User> getList(@RequestBody List<User> users) {
        return userService.getList(users);
    }

    // Case 12: 测试参数和返回值都是Map类型
    @PostMapping("/users/map")
    public Map<String, User> getMap(@RequestBody Map<String, User> userMap) {
        return userService.getMap(userMap);
    }

    // Case 13: 测试参数和返回值都是Boolean/boolean类型
    @GetMapping("/users/flag/{value}")
    public boolean getFlag(@PathVariable("value") String value) {
        Boolean flag = Boolean.valueOf(value);
        return userService.getFlag(flag);
    }

    // Case 14: 测试参数和返回值都是User[]类型
    @PostMapping("/users/array")
    public User[] findUsers(@RequestBody User[] users) {
        return userService.findUsers(users);
    }

    // Case 15: 测试参数为boolean，返回值都是User类型
    @GetMapping("/users/ex/{throwException}")
    public User ex(@PathVariable("throwException") boolean throwException) {
        return userService.ex(throwException);
    }

    // Case 16: 测试服务端抛出一个RuntimeException异常 (same endpoint as case 16 but with exception handling)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
