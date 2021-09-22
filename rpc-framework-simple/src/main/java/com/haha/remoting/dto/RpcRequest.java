package com.haha.remoting.dto;

import lombok.*;
import java.io.Serializable;

/**
 * rpc请求实现类
 * rpc请求实体类。当你要调用远程方法的时候，你需要先传输一个RpcRequest给对方，
 * RpcRequest里面包含了要调用的目标方法和类的名称、参数等数据。
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest  implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;

    public String getRpcServiceName(){
        return this.getInterfaceName()+this.getGroup()+this.getVersion();
    }

}
