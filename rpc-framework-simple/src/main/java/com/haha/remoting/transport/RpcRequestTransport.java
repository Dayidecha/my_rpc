package com.haha.remoting.transport;

import com.haha.extension.SPI;
import com.haha.remoting.dto.RpcRequest;

@SPI
public interface RpcRequestTransport {
    /**
     * 发送RpcRequest请求到服务器并返回结果
     * @param rpcRequest
     * @return
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
