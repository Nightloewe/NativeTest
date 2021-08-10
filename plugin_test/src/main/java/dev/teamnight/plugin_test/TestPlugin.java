package dev.teamnight.plugin_test;

import dev.teamnight.plugin.api.Plugin;

import java.util.List;

public class TestPlugin implements Plugin {
    @Override
    public List<Class<?>> getBeans() {
        return List.of(TestComponent.class);
    }
}
