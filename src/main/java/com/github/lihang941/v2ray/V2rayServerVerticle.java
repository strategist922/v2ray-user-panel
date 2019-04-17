package com.github.lihang941.v2ray;

import com.github.lihang941.v2ray.event.AddUserEvent;
import com.github.lihang941.v2ray.tool.Logger;
import com.google.protobuf.Message;
import com.v2ray.core.app.proxyman.command.AddUserOperation;
import com.v2ray.core.app.proxyman.command.AlterInboundRequest;
import com.v2ray.core.app.proxyman.command.AlterInboundResponse;
import com.v2ray.core.common.protocol.SecurityConfig;
import com.v2ray.core.common.protocol.SecurityType;
import com.v2ray.core.common.protocol.User;
import com.v2ray.core.common.serial.TypedMessage;
import com.v2ray.core.proxy.vmess.Account;
import in.zhaoj.v2ray.V2RayApiClient;
import io.grpc.StatusRuntimeException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * @author : lihang941
 * @since : 2019/4/17
 */
public class V2rayServerVerticle extends AbstractVerticle {

    private Logger logger = new Logger(V2rayServerVerticle.class.getName());
    private String host;
    private int port;
    private int level;
    private String tag;

    private V2RayApiClient v2RayApiClient;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        JsonObject jsonObject = config().getJsonObject("v2ray");
        this.host = Objects.requireNonNull(jsonObject.getString("host"), "check config v2ray.host is null");
        this.port = Objects.requireNonNull(jsonObject.getInteger("port"), "check config v2ray.port is null");
        this.level = Objects.requireNonNull(jsonObject.getInteger("level"), "check config v2ray.level is null");
        this.tag = Objects.requireNonNull(jsonObject.getString("tag"), "check config v2ray.tag is null");
        vertx.eventBus().<AddUserEvent>consumer(AddUserEvent.class.getName(), message -> {
            AddUserEvent addUserEvent = message.body();
            Account account = Account.newBuilder()
                    .setAlterId(addUserEvent.getAlterId())
                    .setId(addUserEvent.getId())
                    .setSecuritySettings(SecurityConfig.newBuilder().setType(SecurityType.AUTO).build())
                    .build();
            User user = User.newBuilder()
                    .setAccount(toTypedMessage(account))
                    .setLevel(level)
                    .setEmail(addUserEvent.getEmail())
                    .build();

            AddUserOperation userOperation = AddUserOperation.newBuilder()
                    .setUser(user)
                    .build();

            try {
                AlterInboundResponse alterInboundResponse = v2RayApiClient.getHandlerServiceBlockingStub().alterInbound(AlterInboundRequest.newBuilder()
                        .setTag(tag)
                        .setOperation(toTypedMessage(userOperation))
                        .build());
            } catch (StatusRuntimeException e) {

            }
        });

    }

    @Override
    public void start() throws Exception {
        super.start();
        v2RayApiClient = new V2RayApiClient(host, port);
        logger.info("v2ray rpc api start success");
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        v2RayApiClient.shutdown();
    }

    private static TypedMessage toTypedMessage(Message message) {
        return TypedMessage.newBuilder()
                .setType(message.getDescriptorForType().getFullName())
                .setValue(message.toByteString())
                .build();
    }
}
