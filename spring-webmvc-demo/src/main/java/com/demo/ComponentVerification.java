package com.demo;

import com.demo.service.ZhouyuService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 验证@Component注解是否正常工作的简单程序
 */
public class ComponentVerification {

    @Configuration
    @ComponentScan("com.demo.service")
    static class TestConfig {
        // 简单的配置类，只扫描service包
    }

    public static void main(String[] args) {
        System.out.println("🔍 开始验证@Component注解是否正常工作...");
        
        try {
            // 创建Spring应用上下文
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class);
            
            // 获取ZhouyuService Bean
            ZhouyuService service = context.getBean(ZhouyuService.class);
            
            // 调用服务方法
            String result = service.test();
            
            // 输出结果
            System.out.println("✅ @Component注解工作正常！");
            System.out.println("📦 成功获取到ZhouyuService Bean");
            System.out.println("🎯 服务方法调用结果: " + result);
            System.out.println("🏷️  Bean名称: " + context.getBeanNamesForType(ZhouyuService.class)[0]);
            
            context.close();
            
        } catch (Exception e) {
            System.err.println("❌ @Component注解验证失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
