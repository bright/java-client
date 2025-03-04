package io.split.telemetry.domain;

import com.google.gson.annotations.SerializedName;

public class LastSynchronization {
    /* package private */ static final String FIELD_SPLIT = "sp";
    /* package private */ static final String FIELD_SEGMENTS = "se";
    /* package private */ static final String FIELD_IMPRESSIONS = "im";
    /* package private */ static final String FIELD_IMPRESSIONS_COUNT = "ic";
    /* package private */ static final String FIELD_EVENTS = "ev";
    /* package private */ static final String FIELD_TOKEN = "to";
    /* package private */ static final String FIELD_TELEMETRY = "te";

    @SerializedName(FIELD_SPLIT)
    private long _splits;
    @SerializedName(FIELD_SEGMENTS)
    private long _segments;
    @SerializedName(FIELD_IMPRESSIONS)
    private long _impressions;
    @SerializedName(FIELD_IMPRESSIONS_COUNT)
    private long _impressionsCount;
    @SerializedName(FIELD_EVENTS)
    private long _events;
    @SerializedName(FIELD_TOKEN)
    private long _token;
    @SerializedName(FIELD_TELEMETRY)
    private long _telemetry;

    public long get_splits() {
        return _splits;
    }

    public void set_splits(long _splits) {
        this._splits = _splits;
    }

    public long get_segments() {
        return _segments;
    }

    public void set_segments(long _segments) {
        this._segments = _segments;
    }

    public long get_impressions() {
        return _impressions;
    }

    public void set_impressions(long _impressions) {
        this._impressions = _impressions;
    }

    public long get_events() {
        return _events;
    }

    public void set_events(long _events) {
        this._events = _events;
    }

    public long get_token() {
        return _token;
    }

    public void set_token(long _token) {
        this._token = _token;
    }

    public long get_telemetry() {
        return _telemetry;
    }

    public void set_telemetry(long _telemetry) {
        this._telemetry = _telemetry;
    }

    public long get_impressionsCount() {
        return _impressionsCount;
    }

    public void set_impressionsCount(long _impressionsCount) {
        this._impressionsCount = _impressionsCount;
    }
}
