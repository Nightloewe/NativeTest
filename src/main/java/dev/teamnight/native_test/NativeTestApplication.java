package dev.teamnight.native_test;

import dev.teamnight.plugin.api.Plugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.graalvm.polyglot.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.nativex.hint.TypeHint;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@TypeHint(types = ParameterizedMessageFactory.class)
public class NativeTestApplication implements ApplicationListener<ApplicationContextInitializedEvent> {

    private static final Logger LOGGER = LogManager.getLogger(NativeTestApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(NativeTestApplication.class);
        app.addListeners(new NativeTestApplication());
        app.run(args);
    }

    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent e) {
        var context = (AnnotationConfigServletWebServerApplicationContext) e.getApplicationContext();
        var pluginsDirectory = Paths.get(System.getProperty("user.dir")).resolve("plugins").toAbsolutePath();
        var javaHome = System.getProperty("java.home");

        String classpath = "";
        try {
            classpath = Files.list(pluginsDirectory)
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .collect(Collectors.joining(":"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        LOGGER.info("Initializing Plugin Engine using system properties: \n" +
                "Working Directory: {}\n" +
                "Plugin Directory: {}\n" +
                "Java Home Directory: {}\n" +
                "Classpath: {}", System.getProperty("user.dir"), pluginsDirectory, javaHome, classpath);

        Context polyglot = null;
        var builder = Context.newBuilder("java")
                .allowAllAccess(true)
                .option("java.Properties.java.class.path", classpath)
                .option("java.Classpath", classpath);

        if(javaHome != null) {
            System.setProperty("org.graalvm.home", javaHome);
            builder.option("java.JavaHome", javaHome);
        }
        polyglot = builder.build();

        var java = polyglot.getBindings("java");

        LOGGER.info("Polyglot Binding name is {}", java.getClass().getCanonicalName());
        LOGGER.info("Polyglot member keys type is {}", java.getMemberKeys().getClass().getCanonicalName());

        for(String name : java.getMemberKeys()) {
            LOGGER.info("Polyglot Binding found: {}", name);
        }

        var plugin = java.getMember("dev.teamnight.plugin_test.TestPlugin");

        if(plugin == null) {
            LOGGER.error("plugin is null");
            System.exit(1);
            return;
        }

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
