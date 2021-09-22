package com.haha.registry.zk.util;

import com.haha.enums.RpcConfigEnum;
import com.haha.utils.PropertiesFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Curator工具类
 */
@Slf4j
public final class CuratorUtils {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    //个人理解：用来做缓存避免每次都访问zookeeper,List是一个服务可能有多个地址（value)
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    //用来记录所有注册到zookeeper节点的服务名称(key)
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "101.34.9.213:2181";

    private CuratorUtils() {
    }

    /**
     * 创建临时节点
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node already exists. The node is:[{}]", path);
            } else {
                //eg: /my-rpc/github.javaguide.HelloService/127.0.0.1:9999
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create persistent node for path [{}] fail", path);
        }
    }

    /**
     * 返回rpcServiceName路径下的所有子节点
     * @param zkClient
     * @param rpcServiceName rpc service name eg:github.javaguide.HelloServicetest2version1
     * @return
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName){
        //如果本地已经缓存有
        if(SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        //没缓存过
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH+"/"+rpcServiceName;
        try{
            result = zkClient.getChildren().forPath(servicePath);
            registerWatcher(rpcServiceName, zkClient);
        }catch (Exception e){
            log.error("get children nodes for path [{}] fail", servicePath);
        }
        return result;
    }

    /**
     * 清空本地缓存和注册中心关于InetSocketAddress的数据
     * @param zkClient
     * @param inetSocketAddress
     *
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        REGISTERED_PATH_SET.stream().parallel().forEach(p->{
            try{
                //DESKTOP-RKTGNGS/192.168.2.101:80
                if(p.endsWith(inetSocketAddress.toString())){
                    zkClient.delete().forPath(p);
                }
            } catch (Exception e) {
                log.error("clear registry for path [{}] fail", p);
            }
            log.info("All registered services on the server are cleared:[{}]", REGISTERED_PATH_SET.toString());
        });
    }


    /**
     * 返回zkClient连接
     * @return
     *
     */
    public static CuratorFramework getZkClient() {
        //读取配置文件
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_ENUM.getValue());
        //读取配置文件中zookeeper服务端地址，没有则用默认值
        String zookeeperAddress = properties != null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getValue()) != null ? properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getValue()) : DEFAULT_ZOOKEEPER_ADDRESS;
        // 如果zkClient已启动
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        //连接失败，则重新连接
        // Retry strategy. Retry 3 times, and will increase the sleep time between retries.
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        //30秒都没连上
        try {
            if(!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)){
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return zkClient;
    }


    /**
     * 给某个节点注册子节点监听器
     * @param rpcServiceName
     * @param zkClient
     * @throws Exception
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }

    public static void main(String[] args) throws UnknownHostException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(),80);
        System.out.println(inetSocketAddress.toString());

        /*CuratorFramework zkClient = getZkClient();
        System.out.println(zkClient.getState());*/
    }

}

