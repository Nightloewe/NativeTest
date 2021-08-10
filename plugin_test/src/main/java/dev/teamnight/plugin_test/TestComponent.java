package dev.teamnight.plugin_test;

import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestComponent {

    public TestComponent() {
        LogManager.getLogger().info("Started TestComponent successfully");
    }

    @GetMapping("/")
    public String test() {
        return "test";
    }

}
