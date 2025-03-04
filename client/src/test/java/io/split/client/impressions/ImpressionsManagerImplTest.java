package io.split.client.impressions;

import io.split.client.SplitClientConfig;
import io.split.client.dtos.KeyImpression;
import io.split.client.dtos.TestImpressions;

import io.split.storages.enums.OperationMode;
import io.split.telemetry.domain.enums.ImpressionsDataTypeEnum;
import io.split.telemetry.storage.InMemoryTelemetryStorage;
import io.split.telemetry.storage.TelemetryStorage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import pluggable.CustomStorageWrapper;

import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by patricioe on 6/20/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ImpressionsManagerImplTest {
    private static TelemetryStorage TELEMETRY_STORAGE = Mockito.mock(InMemoryTelemetryStorage.class);

    @Before
    public void setUp() {
        TELEMETRY_STORAGE = Mockito.mock(InMemoryTelemetryStorage.class);
    }

    @Captor
    private ArgumentCaptor<List<TestImpressions>> impressionsCaptor;

    @Captor
    private ArgumentCaptor<HashMap<ImpressionCounter.Key, Integer>> impressionCountCaptor;

    @Test
    public void works() throws URISyntaxException {
        SplitClientConfig config = SplitClientConfig.builder()
                .impressionsQueueSize(4)
                .endpoint("nowhere.com", "nowhere.com")
                .impressionsMode(ImpressionsManager.Mode.DEBUG)
                .build();
        ImpressionsStorage storage = new InMemoryImpressionsStorage(config.impressionsQueueSize());

        ImpressionsSender senderMock = Mockito.mock(ImpressionsSender.class);

        ImpressionsManagerImpl treatmentLog = ImpressionsManagerImpl.instanceForTest(null, config, senderMock, null, TELEMETRY_STORAGE, storage, storage);

        KeyImpression ki1 = keyImpression("test1", "adil", "on", 1L, null);
        KeyImpression ki2 = keyImpression("test1", "adil", "on", 2L, 1L);
        KeyImpression ki3 = keyImpression("test1", "pato", "on", 3L, 2L);
        KeyImpression ki4 = keyImpression("test2", "pato", "on", 4L, 3L);

        treatmentLog.track(Stream.of(new Impression(ki1.keyName, null, ki1.feature, ki1.treatment, ki1.time, null, ki1.changeNumber, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki2.keyName, null, ki2.feature, ki2.treatment, ki2.time, null, ki2.changeNumber, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki3.keyName, null, ki3.feature, ki3.treatment, ki3.time, null, ki3.changeNumber, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki4.keyName, null, ki4.feature, ki4.treatment, ki4.time, null, ki4.changeNumber, null)).collect(Collectors.toList()));

        // Do what the scheduler would do.
        treatmentLog.sendImpressions();

        verify(senderMock).postImpressionsBulk(impressionsCaptor.capture());

        List<TestImpressions> captured = impressionsCaptor.getValue();

        assertThat(captured.size(), is(equalTo(2)));
    }

    @Test
    public void worksButDropsImpressions() throws URISyntaxException {

        SplitClientConfig config = SplitClientConfig.builder()
                .impressionsQueueSize(3)
                .endpoint("nowhere.com", "nowhere.com")
                .impressionsMode(ImpressionsManager.Mode.DEBUG)
                .build();
        ImpressionsStorage storage = new InMemoryImpressionsStorage(config.impressionsQueueSize());

        ImpressionsSender senderMock = Mockito.mock(ImpressionsSender.class);

        ImpressionsManagerImpl treatmentLog = ImpressionsManagerImpl.instanceForTest(null, config, senderMock, null, TELEMETRY_STORAGE, storage, storage);

        // These 4 unique test name will cause 4 entries but we are caping at the first 3.
        KeyImpression ki1 = keyImpression("test1", "adil", "on", 1L, null);
        KeyImpression ki2 = keyImpression("test2", "adil", "on", 2L, null);
        KeyImpression ki3 = keyImpression("test3", "pato", "on", 3L, null);
        KeyImpression ki4 = keyImpression("test4", "pato", "on", 4L, null);

        treatmentLog.track(Stream.of(new Impression(ki1.keyName, null, ki1.feature, ki1.treatment, ki1.time, null, null, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki2.keyName, null, ki2.feature, ki2.treatment, ki2.time, null, null, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki3.keyName, null, ki3.feature, ki3.treatment, ki3.time, null, null, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki4.keyName, null, ki4.feature, ki4.treatment, ki4.time, null, null, null)).collect(Collectors.toList()));

        // Do what the scheduler would do.
        treatmentLog.sendImpressions();

        verify(senderMock).postImpressionsBulk(impressionsCaptor.capture());

        List<TestImpressions> captured = impressionsCaptor.getValue();

        assertThat(captured.size(), is(equalTo(3)));
        Mockito.verify(TELEMETRY_STORAGE, times(1)).recordImpressionStats(ImpressionsDataTypeEnum.IMPRESSIONS_DROPPED, 1);
    }

    @Test
    public void works4ImpressionsInOneTest() throws URISyntaxException {

        SplitClientConfig config = SplitClientConfig.builder()
                .impressionsQueueSize(10)
                .endpoint("nowhere.com", "nowhere.com")
                .impressionsMode(ImpressionsManager.Mode.DEBUG)
                .build();
        ImpressionsStorage storage = new InMemoryImpressionsStorage(config.impressionsQueueSize());

        ImpressionsSender senderMock = Mockito.mock(ImpressionsSender.class);

        ImpressionsManagerImpl treatmentLog = ImpressionsManagerImpl.instanceForTest(null, config, senderMock, null, TELEMETRY_STORAGE, storage, storage);

        // These 4 unique test name will cause 4 entries but we are caping at the first 3.
        KeyImpression ki1 = keyImpression("test1", "adil", "on", 1L, 1L);
        KeyImpression ki2 = keyImpression("test1", "adil", "on", 2L, 1L);
        KeyImpression ki3 = keyImpression("test1", "pato", "on", 3L, 1L);
        KeyImpression ki4 = keyImpression("test1", "pato", "on", 4L, 1L);

        treatmentLog.track(Stream.of(new Impression(ki1.keyName, null, ki1.feature, ki1.treatment, ki1.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki2.keyName, null, ki2.feature, ki2.treatment, ki2.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki3.keyName, null, ki3.feature, ki3.treatment, ki3.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki4.keyName, null, ki4.feature, ki4.treatment, ki4.time, null, 1L, null)).collect(Collectors.toList()));

        // Do what the scheduler would do.
        treatmentLog.sendImpressions();

        verify(senderMock).postImpressionsBulk(impressionsCaptor.capture());

        List<TestImpressions> captured = impressionsCaptor.getValue();

        assertThat(captured.size(), is(equalTo(1)));
        assertThat(captured.get(0).keyImpressions.size(), is(equalTo(4)));
        assertThat(captured.get(0).keyImpressions.get(0), is(equalTo(ki1)));
        Mockito.verify(TELEMETRY_STORAGE, times(4)).recordImpressionStats(ImpressionsDataTypeEnum.IMPRESSIONS_QUEUED, 1);
    }

    @Test
    public void worksNoImpressions() throws URISyntaxException {

        SplitClientConfig config = SplitClientConfig.builder()
                .impressionsQueueSize(10)
                .endpoint("nowhere.com", "nowhere.com")
                .impressionsMode(ImpressionsManager.Mode.DEBUG)
                .build();
        ImpressionsStorage storage = new InMemoryImpressionsStorage(config.impressionsQueueSize());

        ImpressionsSender senderMock = Mockito.mock(ImpressionsSender.class);
        ImpressionsManagerImpl treatmentLog = ImpressionsManagerImpl.instanceForTest(null, config, senderMock, null, TELEMETRY_STORAGE, storage, storage);

        // There are no impressions to post.

        // Do what the scheduler would do.
        treatmentLog.sendImpressions();

        verify(senderMock, never()).postImpressionsBulk(impressionsCaptor.capture());
    }

    @Test
    @Ignore // TODO: This test needs to be updated
    public void alreadySeenImpressionsAreMarked() throws URISyntaxException {
        SplitClientConfig config = SplitClientConfig.builder()
                .impressionsQueueSize(10)
                .endpoint("nowhere.com", "nowhere.com")
                .impressionsMode(ImpressionsManager.Mode.DEBUG)
                .build();
        ImpressionsStorage storage = new InMemoryImpressionsStorage(config.impressionsQueueSize());

        ImpressionsSender senderMock = Mockito.mock(ImpressionsSender.class);

        ImpressionsManagerImpl treatmentLog = ImpressionsManagerImpl.instanceForTest(null, config, senderMock, null, TELEMETRY_STORAGE, storage, storage);

        // These 4 unique test name will cause 4 entries but we are caping at the first 3.
        KeyImpression ki1 = keyImpression("test1", "adil", "on", 1L, 1L);
        KeyImpression ki2 = keyImpression("test1", "adil2", "on", 2L, 1L);
        KeyImpression ki3 = keyImpression("test1", "pato", "on", 3L, 1L);
        KeyImpression ki4 = keyImpression("test1", "pato2", "on", 4L, 1L);

        treatmentLog.track(Stream.of(new Impression(ki1.keyName, null, ki1.feature, ki1.treatment, ki1.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki2.keyName, null, ki2.feature, ki2.treatment, ki2.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki3.keyName, null, ki3.feature, ki3.treatment, ki3.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki4.keyName, null, ki4.feature, ki4.treatment, ki4.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.sendImpressions();

        verify(senderMock).postImpressionsBulk(impressionsCaptor.capture());

        List<TestImpressions> captured = impressionsCaptor.getValue();
        for (TestImpressions testImpressions : captured) {
            for (KeyImpression keyImpression : testImpressions.keyImpressions) {
                assertThat(keyImpression.previousTime, is(equalTo(null)));
            }
        }

        // Do it again. Now they should all have a `seenAt` value
        Mockito.reset(senderMock);
        treatmentLog.track(Stream.of(new Impression(ki1.keyName, null, ki1.feature, ki1.treatment, ki1.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki2.keyName, null, ki2.feature, ki2.treatment, ki2.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki3.keyName, null, ki3.feature, ki3.treatment, ki3.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki4.keyName, null, ki4.feature, ki4.treatment, ki4.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.sendImpressions();

        verify(senderMock).postImpressionsBulk(impressionsCaptor.capture());

        captured = impressionsCaptor.getValue();
        for (TestImpressions testImpressions : captured) {
            for (KeyImpression keyImpression : testImpressions.keyImpressions) {
                assertThat(keyImpression.previousTime, is(equalTo(keyImpression.time)));
            }
        }
    }

    private KeyImpression keyImpression(String feature, String key, String treatment, long time, Long changeNumber) {
        KeyImpression result = new KeyImpression();
        result.feature = feature;
        result.keyName = key;
        result.treatment = treatment;
        result.time = time;
        result.changeNumber = changeNumber;
        return result;
    }

    @Test
    public void testImpressionsOptimizedMode() throws URISyntaxException {
        SplitClientConfig config = SplitClientConfig.builder()
                .impressionsQueueSize(10)
                .endpoint("nowhere.com", "nowhere.com")
                .impressionsMode(ImpressionsManager.Mode.OPTIMIZED)
                .build();
        ImpressionsStorage storage = new InMemoryImpressionsStorage(config.impressionsQueueSize());

        ImpressionsSender senderMock = Mockito.mock(ImpressionsSender.class);

        ImpressionsManagerImpl treatmentLog = ImpressionsManagerImpl.instanceForTest(null, config, senderMock, null, TELEMETRY_STORAGE, storage, storage);

        // These 4 unique test name will cause 4 entries but we are caping at the first 3.
        KeyImpression ki1 = keyImpression("test1", "adil", "on", 1L, 1L);
        KeyImpression ki2 = keyImpression("test1", "adil", "on", 2L, 1L);
        KeyImpression ki3 = keyImpression("test1", "pato", "on", 3L, 1L);
        KeyImpression ki4 = keyImpression("test1", "pato", "on", 4L, 1L);

        treatmentLog.track(Stream.of(new Impression(ki1.keyName, null, ki1.feature, ki1.treatment, ki1.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki2.keyName, null, ki2.feature, ki2.treatment, ki2.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki3.keyName, null, ki3.feature, ki3.treatment, ki3.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.track(Stream.of(new Impression(ki4.keyName, null, ki4.feature, ki4.treatment, ki4.time, null, 1L, null)).collect(Collectors.toList()));
        treatmentLog.sendImpressions();

        verify(senderMock).postImpressionsBulk(impressionsCaptor.capture());

        List<TestImpressions> captured = impressionsCaptor.getValue();
        assertThat(captured.get(0).keyImpressions.size(), is(equalTo(2)));
        for (TestImpressions testImpressions : captured) {
            for (KeyImpression keyImpression : testImpressions.keyImpressions) {
                assertThat(keyImpression.previousTime, is(equalTo(null)));
            }
        }
        // Only the first 2 impressions make it to the server
        assertThat(captured.get(0).keyImpressions,
                contains(keyImpression("test1", "adil", "on", 1L, 1L),
                keyImpression("test1", "pato", "on", 3L, 1L)));

        treatmentLog.sendImpressionCounters();
        verify(senderMock).postCounters(impressionCountCaptor.capture());
        HashMap<ImpressionCounter.Key, Integer> capturedCounts = impressionCountCaptor.getValue();
        assertThat(capturedCounts.size(), is(equalTo(1)));
        assertThat(capturedCounts.entrySet(),
                contains(new AbstractMap.SimpleEntry<>(new ImpressionCounter.Key("test1", 0), 4)));


        // Assert that the sender is never called if the counters are empty.
        Mockito.reset(senderMock);
        treatmentLog.sendImpressionCounters();
        verify(senderMock, Mockito.times(0)).postCounters(Mockito.any());
    }

    @Test
    public void testCounterStandaloneMode() throws URISyntaxException {
        SplitClientConfig config = SplitClientConfig.builder()
                .impressionsQueueSize(10)
                .endpoint("nowhere.com", "nowhere.com")
                .impressionsMode(ImpressionsManager.Mode.OPTIMIZED)
                .build();
        ImpressionsStorage storage = new InMemoryImpressionsStorage(config.impressionsQueueSize());

        ImpressionsSender senderMock = Mockito.mock(ImpressionsSender.class);

        ImpressionsManagerImpl manager = ImpressionsManagerImpl.instanceForTest(null, config, senderMock, null, TELEMETRY_STORAGE, storage, storage);
        Assert.assertNotNull(manager.getCounter());
    }

    @Test
    public void testCounterConsumerMode() throws URISyntaxException {
        SplitClientConfig config = SplitClientConfig.builder()
                .impressionsQueueSize(10)
                .endpoint("nowhere.com", "nowhere.com")
                .impressionsMode(ImpressionsManager.Mode.OPTIMIZED)
                .operationMode(OperationMode.CONSUMER)
                .customStorageWrapper(Mockito.mock(CustomStorageWrapper.class))
                .build();
        ImpressionsStorage storage = new InMemoryImpressionsStorage(config.impressionsQueueSize());

        ImpressionsSender senderMock = Mockito.mock(ImpressionsSender.class);

        ImpressionsManagerImpl manager = ImpressionsManagerImpl.instanceForTest(null, config, senderMock, null, TELEMETRY_STORAGE, storage, storage);
        Assert.assertNull(manager.getCounter());
    }

}
