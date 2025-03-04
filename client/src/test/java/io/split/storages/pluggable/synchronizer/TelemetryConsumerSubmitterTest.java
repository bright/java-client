package io.split.storages.pluggable.synchronizer;

import io.split.client.ApiKeyCounter;
import io.split.client.SplitClientConfig;
import io.split.client.utils.SDKMetadata;
import io.split.storages.pluggable.domain.ConfigConsumer;
import io.split.storages.pluggable.domain.SafeUserStorageWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import pluggable.CustomStorageWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TelemetryConsumerSubmitterTest {
    private static final String FIRST_KEY = "KEY_1";
    private static final String SECOND_KEY = "KEY_2";

    @Test
    public void testSynchronizeConfig() {
        ApiKeyCounter.getApiKeyCounterInstance().clearApiKeys();
        ApiKeyCounter.getApiKeyCounterInstance().add(FIRST_KEY);
        ApiKeyCounter.getApiKeyCounterInstance().add(FIRST_KEY);
        ApiKeyCounter.getApiKeyCounterInstance().add(FIRST_KEY);
        ApiKeyCounter.getApiKeyCounterInstance().add(SECOND_KEY);
        ApiKeyCounter.getApiKeyCounterInstance().add(SECOND_KEY);
        TelemetryConsumerSubmitter telemetrySynchronizer = new TelemetryConsumerSubmitter(Mockito.mock(CustomStorageWrapper.class), new SDKMetadata("SDK 4.2.x", "22.215135.1", "testMachine"));
        SplitClientConfig splitClientConfig = SplitClientConfig.builder().build();
        ConfigConsumer config = telemetrySynchronizer.generateConfig(splitClientConfig, ApiKeyCounter.getApiKeyCounterInstance().getFactoryInstances(), Stream.of("tag1", "tag2").collect(Collectors.toList()));
        Assert.assertEquals(3, config.get_redundantFactories());
        Assert.assertEquals(2, config.get_tags().size());
    }

    @Test
    public void testTestSynchronizeConfig() throws NoSuchFieldException, IllegalAccessException {
        SafeUserStorageWrapper safeUserStorageWrapper = Mockito.mock(SafeUserStorageWrapper.class);
        TelemetryConsumerSubmitter telemetrySynchronizer = new TelemetryConsumerSubmitter(Mockito.mock(CustomStorageWrapper.class), new SDKMetadata("SDK 4.2.x", "22.215135.1", "testMachine"));
        SplitClientConfig splitClientConfig = SplitClientConfig.builder().build();
        Field telemetryConsumerSubmitterHolder = TelemetryConsumerSubmitter.class.getDeclaredField("_safeUserStorageWrapper");
        telemetryConsumerSubmitterHolder.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(telemetryConsumerSubmitterHolder, telemetryConsumerSubmitterHolder.getModifiers() & ~Modifier.FINAL);
        telemetryConsumerSubmitterHolder.set(telemetrySynchronizer, safeUserStorageWrapper);
        telemetrySynchronizer.synchronizeConfig(splitClientConfig, 10L, new HashMap<>(), new ArrayList<>());
        Mockito.verify(safeUserStorageWrapper, Mockito.times(1)).set(Mockito.anyString(), Mockito.anyObject());
    }
}