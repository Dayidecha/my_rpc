package com.haha.serviceImpl;

import com.haha.HelloService;
import com.haha.config.RpcServiceConfig;
import com.haha.remoting.transport.socket.SocketRpcServer;

public class SocketService {
    public static void main(String[] args) {
        HelloService helloService = new Hello2Impl();
        SocketRpcServer socketRpcServer = new SocketRpcServer();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setService(helloService);
        socketRpcServer.registerService(rpcServiceConfig);
        socketRpcServer.start();
    }
}
