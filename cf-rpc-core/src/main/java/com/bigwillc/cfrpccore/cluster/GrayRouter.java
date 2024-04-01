package com.bigwillc.cfrpccore.cluster;

import com.bigwillc.cfrpccore.api.Router;
import com.bigwillc.cfrpccore.meta.InstanceMeta;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 灰度路由
 *
 * 可以做一些灰度用户，某次请求上加灰度标记
 * 结合蓝绿
 * 100 都是normal
 * 100 都是灰度
 *
 * @author bigwillc on 2024/3/31
 */
@Slf4j
@Data
public class GrayRouter implements Router<InstanceMeta> {

    private int grayRatio = 0;

    private Random random = new Random();

    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }

    @Override
    public List<InstanceMeta> route(List<InstanceMeta> providers) {

        if(providers == null || providers.size() <= 1) {
            return providers;
        }

        List<InstanceMeta> normalNodes = new ArrayList<>();
        List<InstanceMeta> grayNodes = new ArrayList<>();

        // 分类
        providers.stream().forEach(p -> {
            if ("true".equals(p.getParameters().get("gray"))) {
                grayNodes.add(p);
            } else {
                normalNodes.add(p);
            }
        });

        log.debug(" ===> grayRouter grayNode/normalNode: {}/{}, grayRatio: {}", grayNodes.size(), normalNodes.size(), grayRatio);

        if(normalNodes.isEmpty() || grayNodes.isEmpty()) {
            return providers;
        }

        if (grayRatio <= 0) {
            return normalNodes;
        } else if (grayRatio >= 100) {
            return grayNodes;
        }

        // 虚拟节点算法，这个要求loadbalance 必须是均匀的，线性分布
        // grayRatio = 10
        // graynodes 1 03
        // normalnodes 2 01, 02
        // all = 1*2, 2*9
        // 03,03, 01,02,01,02,01,02,01,02,01,02,01,02,01,02,01,02

        // 在A情况下，返回normal nodes,
        // B 的情况下返回gray nodes

        if(random.nextInt(100) < grayRatio) {
            return grayNodes;
        } else {
            return normalNodes;
        }
    }


}
