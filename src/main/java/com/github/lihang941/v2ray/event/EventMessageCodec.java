package com.github.lihang941.v2ray.event;

import io.netty.util.CharsetUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

/**
 * @author : lihang941
 * @since : 2019/4/19
 */
public class EventMessageCodec<Event> implements MessageCodec<Event, Event> {

    private Class<Event> clazz;

    public EventMessageCodec(Class<Event> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void encodeToWire(Buffer buffer, Event event) {
        byte[] strBytes = Json.encode(event).getBytes(CharsetUtil.UTF_8);
        buffer.appendInt(strBytes.length);
        buffer.appendBytes(strBytes);
    }

    @Override
    public Event decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        int readOffset = pos + 4;
        byte[] bytes = buffer.getBytes(readOffset, readOffset + length);
        return Json.decodeValue(new String(bytes, CharsetUtil.UTF_8), clazz);
    }

    @Override
    public Event transform(Event event) {
        return event;
    }

    @Override
    public String name() {
        return clazz.getName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
