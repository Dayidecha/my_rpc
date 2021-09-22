package com.haha.provider.impl;

import com.haha.enums.RpcErrorMessageEnum;
import com.haha.exception.RpcException;
import com.haha.extension.ExtensionLoader;
import com.haha.config.RpcServiceConfig;
import lombok.extern.slf4j.Slf4j;
import com.haha.provider.ServiceProvider;
import com.haha.registry.ServiceRegistry;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    /**
     * key: rpc service name(interface name + version + group)
     * value: service object
     */
    private final Map<String,Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl(){
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }


    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        //获取要注册的服务名称 包括类名+版本+组
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if(registeredService.contains(rpcServiceName)){
            System.out.println(rpcServiceConfig.getService());
            return;
        }
        //没注册过，先存到缓存中
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName,rpcServiceConfig.getService());
        System.out.println(rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
                String host = InetAddress.getLocalHost().getHostAddress();
                this.addService(rpcServiceConfig);
                //**********
                serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(),new InetSocketAddress(host,9998));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        System.out.println(InetAddress.getLocalHost().getHostAddress());
        System.out.println(InetAddress.getLocalHost().getHostName());
        System.out.println(InetAddress.getLocalHost().toString());
        ZkServiceProviderImpl zkServiceProvider = new ZkServiceProviderImpl();

        System.out.println(zkServiceProvider.serviceRegistry.toString());


    }


}
