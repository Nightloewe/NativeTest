package dev.teamnight.native_test;

import dev.teamnight.plugin.api.Plugin;
import org.graalvm.polyglot.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationListener;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;

@SpringBootApplication
public class NativeTestApplication implements ApplicationListener<ApplicationContextInitializedEvent> {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(NativeTestApplication.class);
        app.addListeners(new NativeTestApplication());
        app.run(args);
    }

    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent e) {
        var context = (AnnotationConfigServletWebServerApplicationContext) e.getApplicationContext();
        var pluginsDirectory = Paths.get(System.getProperty("user.dir")).resolve("plugins");
        var javaHome = System.getProperty("java.home");

        Context polyglot = null;
        try {
            var builder = Context.newBuilder()
                    .allowAllAccess(true)
                    .option("java.Properties.java.class.path", pluginsDirectory.toAbsolutePath().toString());

            if(javaHome != null) {
                System.out.println("Setting java home");
                builder = builder.option("java.JavaHome", javaHome);
            }

            polyglot = builder.build();
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        var plugin = polyglot.getBindings("java").getMember("dev.teamnight.plugin_test.TestPlugin");
        Plugin plugin1 = null;

        if(plugin.isMetaObject()) {
            Class<?> clazz = plugin.as(Class.class);

            try {
                plugin1 = (Plugin) clazz.getConstructor().newInstance();
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        } else {
            plugin1 = plugin.as(Plugin.class);
        }

        if(plugin1 != null) {
            for(Class<?> clazz : plugin1.getBeans()) {
                context.register(clazz);
            }
        }
    }
}
