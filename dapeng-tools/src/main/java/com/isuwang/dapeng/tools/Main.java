package com.isuwang.dapeng.tools;

import com.isuwang.dapeng.tools.helpers.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 命令行工具入口
 */
public class Main {
    private static final String RUNNING_INFO = "runningInfo";
    private static final String META_DATA = "metadata";
    private static final String XML_REQUEST = "request:xml";
    private static final String JSON_REQUEST = "request:json";
    private static final String XML_EXAMPLE = "example:xml";
    private static final String JSON_EXAMPLE = "example:json";
    private static final String ROUT_INFO = "routeInfo";

    private static String help = "-----------------------------------------------------------------------\n" +
            " |commands: runningInfo | routInfo | metadata | request | json | xml  \n" +
            " | 1 .通过指定服务名，或服务名+版本号，获取对应的服务的容器ip和端口: \n" +
            " |    java -jar dapeng.jar runningInfo com.isuwang.soa.hello.service.HelloService\n" +
            " |    java -jar dapeng.jar runningInfo com.isuwang.soa.hello.service.HelloService 1.0.1\n" +
            " | 2. 通过服务名和版本号，获取元信息: \n" +
            " |    java -jar dapeng.jar metadata com.isuwang.soa.hello.service.HelloService 1.0.1\n" +
            " | 3. 通过json文件，请求对应服务，并打印结果: \n" +
            " |    java -jar dapeng.jar request:json request.json\n" +
            " | 4. 通过xml文件，请求对应服务，并打印结果: \n" +
            " |    java -jar dapeng.jar request:xml request.xml\n" +
            " | 5. 通过系统参数，json文件，调用指定服务器的服务并打印结果: \n" +
            " |    java -Dsoa.service.ip=192.168.0.1 -Dsoa.service.port=9091 -jar dapeng.jar request request.json\n" +
            " | 6. 通过系统参数，xml文件，调用指定服务器的服务并打印结果: \n" +
            " |    java -Dsoa.service.ip=192.168.0.1 -Dsoa.service.port=9091 -jar dapeng.jar request request.xml\n" +
            " | 7. 通过服务名/版本号/方法名，获取请求json的示例: \n" +
            " |    java -jar dapeng.jar example:json com.isuwang.soa.hello.service.HelloService 1.0.0 sayHello\n" +
            " | 8. 通过服务名/版本号/方法名，获取请求xml的示例: \n" +
            " |    java -jar dapeng.jar example:xml com.isuwang.soa.hello.service.HelloService 1.0.0 sayHello\n" +
            " | 9. 获取当前zookeeper中的服务路由信息: \n" +
            " |    java -jar dapeng.jar routInfo\n" +
            " | 10.指定配置文件，设置路由信息: \n" +
            " |    java -jar dapeng.jar routInfo route.cfg \n" +
            "-----------------------------------------------------------------------";

    public static void main(String[] args) {

        if (args == null || args.length == 0) {
            System.out.println(help);
            System.exit(0);
        }
        switch (args[0]) {
            case RUNNING_INFO:
                ZookeeperSerachHelper.getInfos(args);
                break;
            case META_DATA:
                MetaInfoHelper.getService(args);
                break;
            case XML_REQUEST:
                RequestHelper.post(args);
                break;
            case JSON_REQUEST:
                RequestHelper.post(args);
                break;
            case JSON_EXAMPLE:
                RequestExampleHelper.getRequestJson(args);
                break;
            case XML_EXAMPLE:
                RequestExampleHelper.getRequestXml(args);
                break;
            case ROUT_INFO:
                RouteInfoHelper.routeInfo(args);
                break;
            default:
                System.out.println(help);
        }
        System.exit(0);
    }
}
