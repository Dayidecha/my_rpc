package com.haha.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RpcConfigEnum {
    RPC_CONFIG_ENUM("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String value;

}
