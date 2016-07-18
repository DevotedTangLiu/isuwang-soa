package com.isuwang.dapeng.container.registry;

import com.isuwang.dapeng.container.Container;
import com.isuwang.dapeng.container.spring.SpringContainer;
import com.isuwang.dapeng.core.ProcessorKey;
import com.isuwang.dapeng.core.Service;
import com.isuwang.dapeng.core.SoaBaseProcessor;
import com.isuwang.dapeng.registry.RegistryAgent;
import com.isuwang.dapeng.registry.RegistryAgentProxy;
import com.isuwang.dapeng.registry.zookeeper.RegistryAgentImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry Container
 *
 * @author craneding
 * @date 16/1/19
 */
public class ZookeeperRegistryContainer implements Container {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegistryContainer.class);

    private final static RegistryAgent registryAgent = new RegistryAgentImpl(false);

    private static ConcurrentHashMap<Integer,List<String>> tmpServices = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void start() {
        RegistryAgentProxy.setCurrentInstance(RegistryAgentProxy.Type.Server, registryAgent);

        registryAgent.setProcessorMap(ProcessorCache.getProcessorMap());
        registryAgent.start();

        Map<Object, Class<?>> contexts = SpringContainer.getContexts();
        Set<Object> ctxs = contexts.keySet();

        ctxs.forEach(ZookeeperRegistryContainer::registryService);
    }

    @Override
    public void stop() {
        ProcessorCache.getProcessorMap().clear();

        registryAgent.stop();
    }

    public static Map<ProcessorKey, SoaBaseProcessor<?>> getProcessorMap() {
        return ProcessorCache.getProcessorMap();
    }


    public static void registryService(Object context) {
        registryService(context, false);
    }

    public static void registryService(Object context, Boolean isTmp) {
        Map<Object, Class<?>> contexts = SpringContainer.getContexts();
        Class<?> contextClass = contexts.get(context);

        try {
            Method method = contextClass.getMethod("getBeansOfType", Class.class);
            Map<String, SoaBaseProcessor<?>> processorMap = (Map<String, SoaBaseProcessor<?>>) method.invoke(context, contextClass.getClassLoader().loadClass(SoaBaseProcessor.class.getName()));

            Set<String> keys = processorMap.keySet();
            for (String key : keys) {
                SoaBaseProcessor<?> processor = processorMap.get(key);

                if (processor.getInterfaceClass().getClass() != null) {
                    Service service = processor.getInterfaceClass().getAnnotation(Service.class);

                    ProcessorKey processorKey = new ProcessorKey(processor.getInterfaceClass().getName(), service.version());
                    ProcessorCache.getProcessorMap().put(processorKey, processor);
                    registryAgent.registerService(processor.getInterfaceClass().getName(), service.version());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    // TODO 添加一个方法获得临时的service
    public Map<Integer,List<String>> getTmpService() {
        return tmpServices;
    }


}
