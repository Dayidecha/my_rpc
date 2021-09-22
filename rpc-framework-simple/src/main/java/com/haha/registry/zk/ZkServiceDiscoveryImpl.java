package com.haha.registry.zk;

import com.haha.enums.RpcErrorMessageEnum;
import com.haha.exception.RpcException;
import com.haha.registry.ServiceDiscovery;
import com.haha.registry.zk.util.CuratorUtils;
import com.haha.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    //没搞负载均衡

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> childrenNodes = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if(null==childrenNodes||childrenNodes.size()==0){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        //负载均衡：就取第一个
        String[] split = childrenNodes.get(0).split(":");
        String host = split[0];
        int port = Integer.parseInt(split[1]);
        return new InetSocketAddress(host,port);
    }
}
