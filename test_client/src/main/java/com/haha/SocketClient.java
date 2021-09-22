package com.haha;

import com.haha.config.RpcServiceConfig;
import com.haha.proxy.RpcClientProxy;
import com.haha.remoting.transport.RpcRequestTransport;
import com.haha.remoting.transport.socket.SocketRpcClient;

public class SocketClient {
    public static void main(String[] args) {
        RpcRequestTransport rpcRequestTransport = new SocketRpcClient();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcServiceConfig,rpcRequestTransport);
        HelloService proxy = rpcClientProxy.getProxy(HelloService.class);
        String hello = proxy.hello(new Hello("hello", "wo shi haha"));
        System.out.println(hello);

    }
}
