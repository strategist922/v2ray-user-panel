package com.github.lihang941.v2ray;

import com.github.lihang941.v2ray.event.AddUserEvent;
import com.github.lihang941.v2ray.event.DeleteUserEvent;
import com.github.lihang941.v2ray.event.EventMessageCodec;
import com.github.lihang941.v2ray.service.UserService;
import com.github.lihang941.v2ray.tool.Logger;
import com.google.protobuf.Message;
import com.v2ray.core.app.proxyman.command.AddUserOperation;
import com.v2ray.core.app.proxyman.command.AlterInboundRequest;
import com.v2ray.core.app.proxyman.command.RemoveUserOperation;
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

import java.util.Map;
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
    private UserService userService = UserService.userService;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        JsonObject jsonObject = config().getJsonObject("v2ray");
        this.host = Objects.requireNonNull(jsonObject.getString("host"), "check config v2ray.host is null");
        this.port = Objects.requireNonNull(jsonObject.getInteger("port"), "check config v2ray.port is null");
        this.level = Objects.requireNonNull(jsonObject.getInteger("level"), "check config v2ray.level is null");
        this.tag = Objects.requireNonNull(jsonObject.getString("tag"), "check config v2ray.tag is null");


        // 添加用户
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
                v2RayApiClient.getHandlerServiceBlockingStub().alterInbound(AlterInboundRequest.newBuilder()
                        .setTag(tag)
                        .setOperation(toTypedMessage(userOperation))
                        .build());
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getDescription().endsWith("User " + addUserEvent.getEmail() + " already exists.")) {
                    throw new ErrorMessageException(addUserEvent.getEmail() + " 不存在,删除用户失败");
                } else if (e.getStatus().getDescription().endsWith("handler not found: " + tag)) {
                    throw new ErrorMessageException("v2ray.tag 不存在,请检查配置项");
                } else {
                    throw e;
                }
            }
        });


        // 删除用户
        vertx.eventBus().<DeleteUserEvent>consumer(DeleteUserEvent.class.getName(), message -> {
            DeleteUserEvent userEvent = message.body();
            try {
                v2RayApiClient.getHandlerServiceBlockingStub().alterInbound(AlterInboundRequest.newBuilder()
                        .setTag(tag)
                        .setOperation(toTypedMessage(RemoveUserOperation.newBuilder()
                                .setEmail(userEvent.getEmail())
                                .build()))
                        .build());
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getDescription().endsWith("User " + userEvent.getEmail() + " not found.")) {
                    throw new ErrorMessageException(userEvent.getEmail() + " 不存在,删除用户失败");
                } else if (e.getStatus().getDescription().endsWith("handler not found: " + tag)) {
                    throw new ErrorMessageException("v2ray.tag 不存在,请检查配置项");
                } else {
                    throw e;
                }
            }
        });
    }

    @Override
    public void start() throws Exception {
        super.start();
        v2RayApiClient = new V2RayApiClient(host, port);
        logger.info("v2ray rpc api start success");
        registerEventCodec();
        initUsers();
    }

    @Override
    public void stop() throws Exception {
        unregisterEventCodec();
        super.stop();
        removeUsers();
        v2RayApiClient.shutdown();
    }


    private void initUsers() {

        for (com.github.lihang941.v2ray.bean.User user : userService.getUsers().values()) {
            vertx.eventBus().send(AddUserEvent.class.getName(), new AddUserEvent()
                    .setAlterId(user.getAlterId())
                    .setEmail(user.getEmail())
                    .setId(user.getId()), res -> {
                if (res.succeeded()) {
                    logger.info("add user = " + user.getEmail() + " success");
                } else {
                    logger.info("add user = " + user.getEmail() + " failure");
                }
            });
        }
    }


    private void removeUsers() {
        for (com.github.lihang941.v2ray.bean.User user : userService.getUsers().values()) {
            vertx.eventBus().send(DeleteUserEvent.class.getName(), new DeleteUserEvent()
                    .setEmail(user.getEmail()), res -> {
                if (res.succeeded()) {
                    logger.info("remove user = " + user.getEmail() + " success");
                } else {
                    logger.info("remove user = " + user.getEmail() + " failure");
                }
            });
        }
    }


    private void registerEventCodec() {
        vertx.eventBus().registerCodec(new EventMessageCodec<>(AddUserEvent.class));
        vertx.eventBus().registerCodec(new EventMessageCodec<>(DeleteUserEvent.class));
    }

    private void unregisterEventCodec() {
        vertx.eventBus().unregisterCodec(AddUserEvent.class.getName());
        vertx.eventBus().unregisterCodec(DeleteUserEvent.class.getName());
    }

    private static TypedMessage toTypedMessage(Message message) {
        return TypedMessage.newBuilder()
                .setType(message.getDescriptorForType().getFullName())
                .setValue(message.toByteString())
                .build();
    }
}
