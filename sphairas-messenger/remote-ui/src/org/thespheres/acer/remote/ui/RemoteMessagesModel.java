/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui;

import com.google.common.eventbus.EventBus;
import java.beans.PropertyChangeEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Collator;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.TreePath;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jdesktop.swingx.JXTreeTable;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;
import org.thespheres.acer.MessageId;
import org.thespheres.acer.beans.ChannelObject;
import org.thespheres.acer.beans.MessageContent;
import org.thespheres.acer.beans.MessageEvent;
import org.thespheres.acer.beans.MessageObject;
import org.thespheres.acer.remote.ui.util.JaxRSUtil;
import org.thespheres.acer.remote.ui.util.MessageComparator;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.client.jms.JMSListener;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.services.jms.JMSTopic;
import org.thespheres.betula.services.util.Signees;

/**
 *
 * @author boris.heithecker
 */
@Messages({"RemoteMessagesModel.displayName=Mitteilungen ({0})"})
public class RemoteMessagesModel implements JMSListener<MessageEvent> {

    public static final String PROP_INIT = "init";
    public static final String PROP_MESSAGE = "message";
    private final static Map<String, Object> INSTANCES = new HashMap<>();
    private final EventBus events = new EventBus();
    private final Map<String, Set<MessageId>> messageIds = new HashMap<>();
    private final Map<MessageId, RemoteMessage> messages = new HashMap<>();
    private final ChannelComp channelComparator = new ChannelComp();
    private final TreeMap<String, RemoteChannel> channels = new TreeMap<>(channelComparator);
    private final Map<String, Set<DraftMessage>> drafts = new HashMap<>();
    private JXTreeTable treeTable;
    private final ProviderInfo providerInfo;
    private AbstractNode node;
    private JMSTopicListenerService jms;

    private RemoteMessagesModel(ProviderInfo provider) {
        providerInfo = provider;
        Util.RP(providerInfo.getURL()).post(this::initialize);
    }

    public static RemoteMessagesModel find(ProviderInfo provider) throws IllegalStateException {
        synchronized (INSTANCES) {
            Object o;
            String url = provider.getURL();
            if ((o = INSTANCES.get(url)) != null) {
                if (o instanceof IllegalStateException) {
                    throw (IllegalStateException) o;
                } else {
                    return (RemoteMessagesModel) o;
                }
            } else {
                try {
                    RemoteMessagesModel ret = new RemoteMessagesModel(provider);
                    INSTANCES.put(url, ret);
                    return ret;
                } catch (IllegalStateException ioex) {
                    INSTANCES.put(url, ioex);
                    throw ioex;
                }
            }
        }
    }

    public ProviderInfo getProviderInfo() {
        return providerInfo;
    }

    public WebProvider getWebProvider() {
        return WebProvider.find(getProviderInfo().getURL(), WebProvider.class);
    }

    public synchronized Node getNodeDelegate() {
        if (node == null) {
            node = new AbstractNode(Children.LEAF, Lookups.singleton(this));
            node.setName(RemoteMessagesModel.class.getName() + "#" + providerInfo.getURL());
            String dn = NbBundle.getMessage(RemoteMessagesModel.class, "RemoteMessagesModel.displayName", providerInfo.getDisplayName());
            node.setDisplayName(dn);
        }
        return node;
    }

    private void initialize() {

        final WebTarget target = JaxRSUtil.create(providerInfo.getURL());
        final Response resp = target.path("channel")
                .request(MediaType.APPLICATION_JSON)
                .get();
        JaxRSUtil.checkResponse(providerInfo, resp);
//        String readEntity = resp.readEntity(String.class);
        final GenericType<List<ChannelObject>> gt = new GenericType<List<ChannelObject>>() {
        };
        final List<ChannelObject> allChannels = resp.readEntity(gt);
        final Response resp2 = target.path("message")
                .request()
                .get();
        JaxRSUtil.checkResponse(providerInfo, resp2);
        final GenericType<List<MessageObject>> gt2 = new GenericType<List<MessageObject>>() {
        };
//        String readEntity2 = resp2.readEntity(String.class);
        final List<MessageObject> all = resp2.readEntity(gt2);

        final Map<String, Set<MessageId>> ids = all.stream().collect(Collectors.groupingBy(MessageObject::getChannelName, Collectors.mapping(MessageObject::getMessageId, Collectors.toSet())));
        final Map<MessageId, RemoteMessage> rms = all.stream().collect(Collectors.toMap(MessageObject::getMessageId, this::createRemoteMessage));
        final Map<String, RemoteChannel> rcs = allChannels.stream().collect(Collectors.toMap(ChannelObject::getName, this::createRemoteChannel, (erc, nrc) -> erc));
        synchronized (this) {
            messageIds.clear();
            messageIds.putAll(ids);
            messages.clear();
            messages.putAll(rms);
            channelComparator.init(rcs);
            channels.clear();
            channels.putAll(rcs);
        }
        jms = JMSTopicListenerService.find(providerInfo.getURL(), JMSTopic.MESSAGES_TOPIC.getJmsResource());
        jms.registerListener(MessageEvent.class, this);
        Signees.get(providerInfo.getURL()); //initialize
        events.post(new ChangeEvent(this));
    }

    void addDraftMessage(final String channel, final DraftMessage draft) {
        synchronized (this) {
            drafts.computeIfAbsent(channel, key -> new HashSet<>()).add(draft);
        }
        class Callback implements DraftMessage.Callback {

            @Override
            public void publish() {
                String messageText = draft.getMessageText();
                if (draft.isConfidential()) {
                    messageText = Base64.getEncoder().encodeToString(messageText.getBytes());
                }
                RemoteMessagesModel.this.publish(channel, messageText, draft.isConfidential());
                removeDraftMessage(channel, draft);
            }

        }
        draft.setCallback(new Callback());
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_MESSAGE, null, draft);
        events.post(evt);
    }

    private void removeDraftMessage(String channel, final DraftMessage draft) {
        boolean ret;
        synchronized (this) {
            ret = drafts.computeIfAbsent(channel, key -> new HashSet<>()).remove(draft);
        }
        if (ret) {
            final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_MESSAGE, draft, null);
            events.post(evt);
        }
    }

    String getSigneeFullname(final Signee creator) {
        if (creator == null) {
            return "";
        }
        return Signees.get(providerInfo.getURL())
                .map(m -> m.getSignee(creator))
                //                .map(m -> m.get(creator))
                .orElse(creator.getPrefix());
    }

    private RemoteMessage createRemoteMessage(MessageObject mo) {
        return new RemoteMessage(mo.getMessageId(), this, mo.getText(), mo.getPriority(), mo.getDatePublished(), mo.getAuthor(), mo.getChannelName());
    }

    private RemoteChannel createRemoteChannel(ChannelObject co) {
        return new RemoteChannel(co.getName(), co.getType(), co.getCurrentDisplayName(), this);
    }

    public RemoteMessage getRemoteMessage(MessageId key) {
        synchronized (this) {
            return messages.get(key);
        }
    }

    public RemoteChannel getRemoteChannel(String key) {
        synchronized (this) {
            return channels.get(key);
        }
    }

    public Set<String> getRemoteChannels() {
        synchronized (this) {
            return channels.keySet();
        }
    }

    public Set<MessageId> getRemoteMessageIds(final String channel) {
        synchronized (this) {
            return messageIds.computeIfAbsent(channel, ch -> new HashSet<>());
        }
    }

    public List<AbstractMessage> getRemoteMessagesSorted(String channel) {
        List<AbstractMessage> ret;
        synchronized (this) {
            ret = getRemoteMessageIds(channel).stream().map(messages::get).collect(Collectors.toList());
            ret.addAll(drafts.computeIfAbsent(channel, key -> new HashSet<>()));
            Collections.sort(ret, new MessageComparator());
        }
        return ret;
    }

    private void publish(String channel, String text, boolean encoded) {
        Util.RP(providerInfo.getURL()).post(() -> {
//            bean.publishMessage(channel, text, 5, encoded);

            final WebTarget target = JaxRSUtil.create(providerInfo.getURL());

            final MessageContent mc = new MessageContent(text, encoded, 5);

            final Response resp = target.path("message")
                    .path(channel)
                    .request()
                    .put(Entity.entity(mc, MediaType.APPLICATION_JSON), Response.class);

            JaxRSUtil.checkResponse(providerInfo, resp);
        });
    }

    void delete(MessageId messageId) {
        Util.RP(providerInfo.getURL()).post(() -> {
//            bean.deleteMessage(messageId);
            final WebTarget target = JaxRSUtil.create(providerInfo.getURL());
            
            final String authority;
            try {
                authority = URLEncoder.encode(messageId.getAuthority(), "utf-8");
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            }
            
            final Response resp = target.path("message")
                    .path(authority)
                    .path(Long.toString(messageId.getId()))
                    .queryParam("version", messageId.getVersion().getVersion())
                    .request()
                    .delete(Response.class);

            JaxRSUtil.checkResponse(providerInfo, resp);
        });
    }

    public void createStaticChannel(String name, String displayName, Signee[] list) {
        Util.RP(providerInfo.getURL()).post(() -> {
//            bean.createStaticChannel(name, displayName);

            final WebTarget target = JaxRSUtil.create(providerInfo.getURL());

            JsonBuilderFactory fac = Json.createBuilderFactory(null);
//            JsonArrayBuilder builder = fac.createArrayBuilder();
            JsonObjectBuilder value = fac.createObjectBuilder();
            if (displayName != null) {
                value.add("display-name", displayName);
            }

            final Response resp = target.path("channel/" + name)
                    .request()
                    .put(Entity.entity(value.build(), MediaType.APPLICATION_JSON), Response.class);

            JaxRSUtil.checkResponse(providerInfo, resp);

            if (list != null && list.length != 0) {
//                bean.setStaticChannelRestrictedSignees(name, list);
                throw new UnsupportedOperationException("Not implemented!");
            }
            initialize();
        });
    }

    public void removeChannel(RemoteChannel channel) {
        Util.RP(providerInfo.getURL()).post(() -> {
            final WebTarget target = JaxRSUtil.create(providerInfo.getURL());

            final Response resp = target.path("channel")
                    .path(channel.getChannelName())
                    .request()
                    .delete(Response.class);

            JaxRSUtil.checkResponse(providerInfo, resp);

            initialize();
        });
    }

    @Override
    public void addNotify() {
    }

    @Override
    public void removeNotify() {
    }

    @Override
    public void onMessage(MessageEvent event) {
        MessageId mid = event.getSource();
        switch (event.getType()) {
            case PUBLISH:
                onMessagePublished(mid);
                break;
            case DELETE:
                onMessageDeleted(mid);
                break;
        }
    }

    private void onMessagePublished(MessageId mid) {
        final WebTarget target = JaxRSUtil.create(providerInfo.getURL());

        final Response resp = target.path("message")
                .path(mid.getAuthority())
                .path(Long.toString(mid.getId()))
                .queryParam("version", mid.getVersion().getVersion())
                .request()
                .get(Response.class);

        JaxRSUtil.checkResponse(providerInfo, resp);

        final MessageObject mo = resp.readEntity(MessageObject.class);

        synchronized (this) {
            messageIds.compute(mo.getChannelName(), (c, s) -> {
                s.add(mo.getMessageId());
                return s;
            });
            messages.compute(mo.getMessageId(), (id, rm) -> {
                if (rm == null) {
                    rm = createRemoteMessage(mo);
                } else {
                    rm.update(mo.getText(), mo.getPriority());
                }
                return rm;
            });
        }
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_MESSAGE, null, messages.get(mo.getMessageId()));
        events.post(evt);
    }

    private void onMessageDeleted(MessageId mid) {
        RemoteMessage rm = messages.get(mid);
        synchronized (this) {
            messageIds.compute(rm.getChannelName(), (c, s) -> {
                s.remove(mid);
                return s;
            });
            messages.remove(mid);
        }
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_MESSAGE, rm, null);
        events.post(evt);
    }

    void expandPath(TreePath tp) {
        if (treeTable != null) {
            treeTable.expandPath(tp);
        }
    }

    void setTreeTable(JXTreeTable table) {
        this.treeTable = table;
    }

    public void registerEventListener(Object object) {
        events.register(object);
    }

    public void unregisterEventListener(Object object) {
        events.unregister(object);
    }

    private class ChannelComp implements Comparator<String> {

        private final Collator collator = Collator.getInstance(Locale.getDefault());
        private final Map<String, String> names = new HashMap<>();

        @Override
        public int compare(String c1, String c2) {
            return collator.compare(names.get(c1), names.get(c2));
        }

        private void init(Map<String, RemoteChannel> rcs) {
            names.clear();
            rcs.forEach((s, rc) -> names.put(s, rc.getDisplayName()));
        }

    }
}
