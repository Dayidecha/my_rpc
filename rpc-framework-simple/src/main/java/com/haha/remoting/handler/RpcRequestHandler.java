package com.haha.remoting.handler;

import com.haha.exception.RpcException;
import com.haha.factory.SingletonFactory;
import com.haha.provider.impl.ZkServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;
import com.haha.provider.ServiceProvider;
import com.haha.remoting.dto.RpcRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 处理rpc请求
 */
@Slf4j
public class RpcRequestHandler {

    private final ServiceProvider serviceProvider;
    public RpcRequestHandler(){
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }
    public Object handle(RpcRequest rpcRequest) {
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return InvokeTargetMethod(rpcRequest,service);
    }

    /**
     *
     * @param rpcRequest rpc请求
     * @param service 服务实体类
     * @return
     */
    private Object InvokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try{
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParamTypes());
            result = method.invoke(service,rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());

        } catch (NoSuchMethodException|IllegalAccessException |InvocationTargetException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}
