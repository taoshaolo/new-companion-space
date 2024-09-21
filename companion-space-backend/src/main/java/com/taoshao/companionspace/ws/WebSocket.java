package com.taoshao.companionspace.ws;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.taoshao.companionspace.config.HttpSessionConfigurator;
import com.taoshao.companionspace.model.entity.Chat;
import com.taoshao.companionspace.model.entity.Team;
import com.taoshao.companionspace.model.entity.User;
import com.taoshao.companionspace.model.request.MessageRequest;
import com.taoshao.companionspace.model.vo.ChatMessageVo;
import com.taoshao.companionspace.model.vo.WebSocketVo;
import com.taoshao.companionspace.service.ChatService;
import com.taoshao.companionspace.service.TeamService;
import com.taoshao.companionspace.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.taoshao.companionspace.constant.ChatConstant.*;
import static com.taoshao.companionspace.constant.UserConstant.ADMIN_ROLE;
import static com.taoshao.companionspace.constant.UserConstant.LOGIN_USER_STATUS;
import static com.taoshao.companionspace.utils.StringUtils.stringJsonListToLongSet;

/**
 * @author taoshao
 */
@Component
@Slf4j
@ServerEndpoint(value = "/websocket/{userId}/{teamId}", configurator = HttpSessionConfigurator.class)
public class WebSocket {
    /**
     * 保存队伍的连接信息
     */
    private static final Map<String, ConcurrentHashMap<String, WebSocket>> ROOMS = new HashMap<>();
    /**
     * 线程安全的无序的集合
     */
    private static final CopyOnWriteArraySet<Session> SESSIONS = new CopyOnWriteArraySet<>();
    /**
     * 存储在线连接数
     */
    private static final Map<String, Session> SESSION_POOL = new HashMap<>(0);
    private static UserService userService;
    private static ChatService chatService;
    private static TeamService teamService;
    /**
     * 房间在线人数
     */
    private static int onlineCount = 0;
    /**
     * 当前信息
     */
    private Session session;
    private HttpSession httpSession;

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocket.onlineCount--;
    }

    @Resource
    public void setHeatMapService(UserService userService) {
        WebSocket.userService = userService;
    }

    @Resource
    public void setHeatMapService(ChatService chatService) {
        WebSocket.chatService = chatService;
    }

    @Resource
    public void setHeatMapService(TeamService teamService) {
        WebSocket.teamService = teamService;
    }


    /**
     * 队伍内群发消息
     *
     * @param teamId
     * @param msg
     * @throws Exception
     */
    public static void broadcast(String teamId, String msg) {
        ConcurrentHashMap<String, WebSocket> map = ROOMS.get(teamId);
        // keySet获取map集合key的集合  然后在遍历key即可
        for (String key : map.keySet()) {
            try {
                WebSocket webSocket = map.get(key);
                webSocket.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送消息
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 当新的 WebSocket 连接打开时，会调用这个方法。
     * @param session
     * @param userId
     * @param teamId
     * @param config
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") String userId, @PathParam(value = "teamId") String teamId, EndpointConfig config) {
        try {
            if (StringUtils.isBlank(userId) || "undefined".equals(userId)) {
                sendError(userId, "参数有误");
                return;
            }

            //config.getUserProperties().get(HttpSession.class.getName()): 从 WebSocket 端点配置的用户属性中获取 HTTP 会话对象。
            //httpSession.getAttribute(LOGIN_USER_STATUS): 从 HTTP 会话中获取用户状态属性，这通常是一个用户对象。
            //如果用户存在，将当前的 WebSocket 会话和 HTTP 会话保存到类成员变量中。
            HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
            User user = (User) httpSession.getAttribute(LOGIN_USER_STATUS);
            if (user != null) {
                this.session = session;
                this.httpSession = httpSession;
            }
            //房间和在线用户管理
            if (!"NaN".equals(teamId)) {
                if (!ROOMS.containsKey(teamId)) {
                    ConcurrentHashMap<String, WebSocket> room = new ConcurrentHashMap<>(0);
                    // 将当前用户的 ID 和 WebSocket 连接对象放入映射中。
                    room.put(userId, this);
                    //以团队 ID 作为键，将新创建的映射放入 ROOMS 映射中。
                    ROOMS.put(String.valueOf(teamId), room);
                    // 在线数加1
                    addOnlineCount();
                } else {
                    if (!ROOMS.get(teamId).containsKey(userId)) {
                        ROOMS.get(teamId).put(userId, this);
                        // 在线数加1
                        addOnlineCount();
                    }
                }
                log.info("有新连接加入！当前在线人数为" + getOnlineCount());
            } else {
                SESSIONS.add(session);
                SESSION_POOL.put(userId, session);
                log.info("有新用户加入，userId={}, 当前在线人数为：{}", userId, SESSION_POOL.size());
                sendAllUsers();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接关闭调用的方法
     * @param userId
     * @param teamId
     * @param session
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId, @PathParam(value = "teamId") String teamId, Session session) {
        try {
            if (!"NaN".equals(teamId)) {
                ROOMS.get(teamId).remove(userId);
                if (getOnlineCount() > 0) {
                    subOnlineCount();
                }
                log.info("用户退出:当前在线人数为:" + getOnlineCount());
            } else {
                if (!SESSION_POOL.isEmpty()) {
                    SESSION_POOL.remove(userId);
                    SESSIONS.remove(session);
                }
                log.info("【WebSocket消息】连接断开，总数为：" + SESSION_POOL.size());
                sendAllUsers();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理客户端收到的消息
     * @param message
     * @param userId
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        if ("PING".equals(message)) {
            sendOneMessage(userId, "pong");
            log.error("心跳包，发送给={},在线:{}人", userId, getOnlineCount());
            return;
        }
        log.info("服务端收到用户username={}的消息:{}", userId, message);
        MessageRequest messageRequest = new Gson().fromJson(message, MessageRequest.class);
        Long toId = messageRequest.getToId();
        Long teamId = messageRequest.getTeamId();
        String text = messageRequest.getText();
        Integer chatType = messageRequest.getChatType();
        User fromUser = userService.getById(userId);
        Team team = teamService.getById(teamId);
        if (chatType == PRIVATE_CHAT) {
            // 私聊
            privateChat(fromUser, toId, text, chatType);
        } else if (chatType == TEAM_CHAT) {
            // 队伍聊天
            teamChat(fromUser, text, team, chatType);
        } else {
            // 大厅聊天
            hallChat(fromUser, text, chatType);
        }
    }

    /**
     * 队伍聊天
     *
     * @param user
     * @param text
     * @param team
     * @param chatType
     */
    private void teamChat(User user, String text, Team team, Integer chatType) {
        ChatMessageVo chatMessageVo = new ChatMessageVo();
        WebSocketVo fromWebSocketVo = new WebSocketVo();
        BeanUtils.copyProperties(user, fromWebSocketVo);
        chatMessageVo.setFormUser(fromWebSocketVo);
        chatMessageVo.setText(text);
        chatMessageVo.setTeamId(team.getId());
        chatMessageVo.setChatType(chatType);
        chatMessageVo.setCreateTime(DateUtil.format(new Date(), "yyyy年MM月dd日 HH:mm:ss"));
        if (user.getId().equals(team.getUserId()) || user.getUserRole() == ADMIN_ROLE) {
            chatMessageVo.setIsAdmin(true);
        }
        User loginUser = (User) this.httpSession.getAttribute(LOGIN_USER_STATUS);
        if (loginUser.getId().equals(user.getId())) {
            chatMessageVo.setIsMy(true);
        }
        String toJson = new Gson().toJson(chatMessageVo);
        try {
            //将消息广播给团队中的所有成员。
            broadcast(String.valueOf(team.getId()), toJson);
            //保存聊天记录到数据库或持久化存储。
            savaChat(user.getId(), null, text, team.getId(), chatType);
            //删除与团队聊天相关的缓存。
            chatService.deleteKey(CACHE_CHAT_TEAM, String.valueOf(team.getId()));
            log.error("队伍聊天，发送给={},队伍={},在线:{}人", user.getId(), team.getId(), getOnlineCount());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 大厅聊天
     *
     * @param user
     * @param text
     */
    private void hallChat(User user, String text, Integer chatType) {
        ChatMessageVo chatMessageVo = new ChatMessageVo();
        WebSocketVo fromWebSocketVo = new WebSocketVo();
        BeanUtils.copyProperties(user, fromWebSocketVo);
        chatMessageVo.setFormUser(fromWebSocketVo);
        chatMessageVo.setText(text);
        chatMessageVo.setChatType(chatType);
        chatMessageVo.setCreateTime(DateUtil.format(new Date(), "yyyy年MM月dd日 HH:mm:ss"));
        if (user.getUserRole() == ADMIN_ROLE) {
            chatMessageVo.setIsAdmin(true);
        }
        User loginUser = (User) this.httpSession.getAttribute(LOGIN_USER_STATUS);
        if (loginUser.getId().equals(user.getId())) {
            chatMessageVo.setIsMy(true);
        }
        String toJson = new Gson().toJson(chatMessageVo);
        //将消息发送给所有在线的用户
        sendAllMessage(toJson);
        savaChat(user.getId(), null, text, null, chatType);
        chatService.deleteKey(CACHE_CHAT_HALL, String.valueOf(user.getId()));
    }

    /**
     * 私人聊天
     *
     * @param user     使用者
     * @param toId     至id
     * @param text     文本
     * @param chatType 聊天类型
     */
    private void privateChat(User user, Long toId, String text, Integer chatType) {
        Session toSession = SESSION_POOL.get(toId.toString());
        if (toSession != null) {
            ChatMessageVo chatMessageVo = chatService.chatResult(user.getId(), toId, text, chatType, DateUtil.date(System.currentTimeMillis()));
            User loginUser = (User) this.httpSession.getAttribute(LOGIN_USER_STATUS);
            if (loginUser.getId().equals(user.getId())) {
                chatMessageVo.setIsMy(true);
            }
            String toJson = new Gson().toJson(chatMessageVo);
            sendOneMessage(toId.toString(), toJson);
            log.info("发送给用户username={}，消息：{}", chatMessageVo.getToUser(), toJson);
        } else {
            log.info("用户不在线username={}的session", toId);
        }
        savaChat(user.getId(), toId, text, null, chatType);
        chatService.deleteKey(CACHE_CHAT_PRIVATE, user.getId() + "" + toId);
        chatService.deleteKey(CACHE_CHAT_PRIVATE, toId + "" + user.getId());
    }

    /**
     * 保存聊天
     *
     * @param userId   用户id
     * @param toId     至id
     * @param text     文本
     * @param teamId   团队id
     * @param chatType 聊天类型
     */
    private void savaChat(Long userId, Long toId, String text, Long teamId, Integer chatType) {
        if (chatType == PRIVATE_CHAT) {
            User user = userService.getById(userId);
            Set<Long> userIds = stringJsonListToLongSet(user.getUserIds());
            if (!userIds.contains(toId)) {
                sendError(String.valueOf(userId), "该用户不是你的好友");
                return;
            }
        }
        Chat chat = new Chat();
        chat.setFromId(userId);
        chat.setText(String.valueOf(text));
        chat.setChatType(chatType);
        chat.setCreateTime(new Date());
        if (toId != null && toId > 0) {
            chat.setToId(toId);
        }
        if (teamId != null && teamId > 0) {
            chat.setTeamId(teamId);
        }
        chatService.save(chat);
    }

    /**
     * 发送失败
     *
     * @param userId       用户id
     * @param errorMessage 错误消息
     */
    private void sendError(String userId, String errorMessage) {
        JSONObject obj = new JSONObject();
        obj.set("error", errorMessage);
        sendOneMessage(userId, obj.toString());
    }

    /**
     * 此为广播消息
     *
     * @param message 消息
     */
    public void sendAllMessage(String message) {
        log.info("【WebSocket消息】广播消息：" + message);
        for (Session session : SESSIONS) {
            try {
                if (session.isOpen()) {
                    synchronized (session) {
                        session.getBasicRemote().sendText(message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 此为单点消息
     *
     * @param userId  用户编号
     * @param message 消息
     */
    public void sendOneMessage(String userId, String message) {
        Session session = SESSION_POOL.get(userId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    log.info("【WebSocket消息】单点消息：" + message);
                    session.getAsyncRemote().sendText(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送所有在线用户信息
     */
    public void sendAllUsers() {
        log.info("【WebSocket消息】发送所有在线用户信息");
        HashMap<String, List<WebSocketVo>> stringListHashMap = new HashMap<>(0);
        List<WebSocketVo> webSocketVos = new ArrayList<>();
        stringListHashMap.put("users", webSocketVos);
        for (Serializable key : SESSION_POOL.keySet()) {
            User user = userService.getById(key);
            WebSocketVo webSocketVo = new WebSocketVo();
            BeanUtils.copyProperties(user, webSocketVo);
            webSocketVos.add(webSocketVo);
        }
        sendAllMessage(JSONUtil.toJsonStr(stringListHashMap));
    }
}
