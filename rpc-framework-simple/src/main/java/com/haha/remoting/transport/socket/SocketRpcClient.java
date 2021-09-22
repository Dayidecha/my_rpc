package com.haha.remoting.transport.socket;

import com.haha.exception.RpcException;
import com.haha.extension.ExtensionLoader;
import com.haha.registry.ServiceDiscovery;
import com.haha.remoting.dto.RpcRequest;
import com.haha.remoting.transport.RpcRequestTransport;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j

public class SocketRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient(){
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
    }
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {

        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        try(Socket socket = new Socket()){
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            return inputStream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RpcException("调用服务失败:", e);
        }

    }

}
