/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.configui;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.admin.units.RemoteSignee;
import org.thespheres.betula.admin.units.RemoteSignee.DocumentInfo;
import org.thespheres.betula.admin.units.SigneesTopComponentModel;
import org.thespheres.betula.admin.units.navigatorui.RemoteTargetNode;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.services.jms.AbstractDocumentEvent.DocumentEventType;
import org.thespheres.betula.services.jms.MultiTargetAssessmentEvent;
import org.thespheres.betula.services.client.jms.JMSListener;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.XmlDocumentEntry;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.jms.JMSTopic;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.ui.util.ServiceUIUtilities;
import org.thespheres.betula.services.util.SigneeEntitlement;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.util.ContainerBuilder;

/**
 *
 * @author boris.heithecker
 */
class RemoteSigneeChildren extends ChildFactory.Detachable<DocumentId> {
    
    private final RemoteSignee signee;
    private final Object[] documentsModel = new Object[]{null};
    private final Map<DocumentId, List<DocumentInfo>> map = new HashMap<>();
    static final String MIME = "application/betula-signee-target-document";
    private final SigneesTopComponentModel model;
    private JMSTopicListenerService service;
    private final Listener listener = new Listener();
    private RequestProcessor.Task update;
    static final int WAIT_TIME = 2700;
    
    RemoteSigneeChildren(RemoteSignee signee, final SigneesTopComponentModel m) {
        this.signee = signee;
        this.model = m;
    }
    
    private Optional<NamingResolver> getNamingResolver() {
        return Optional.ofNullable(NamingResolver.find(model.getSigneesProviderUrl()));
    }
    
    private DocumentsModel findDocumentsModel() throws RuntimeException {
        synchronized (documentsModel) {
            if (documentsModel[0] == null) {
                try {
                    final String pUrl = signee.getSignees().getProviderUrl();
                    final ProviderInfo provider = ProviderRegistry.getDefault().get(pUrl);
                    final DocumentsModel dm = ServiceUIUtilities.findDocumentsModelFromProvider(provider);
                    documentsModel[0] = dm;
                } catch (Exception exception) {
                    documentsModel[0] = exception;
                }
            }
        }
        if (documentsModel[0] instanceof DocumentsModel) {
            return (DocumentsModel) documentsModel[0];
        }
        throw (RuntimeException) documentsModel[0];
    }
    
    @Override
    protected boolean createKeys(List<DocumentId> toPopulate) {
        final ContainerBuilder builder = new ContainerBuilder();
        final String[] path = Paths.SIGNEES_TARGET_DOCUMENTS_PATH;
        final org.thespheres.betula.document.Action action = org.thespheres.betula.document.Action.REQUEST_COMPLETION;
        final Template<?> node = builder.createTemplate(null, signee.getSignee(), null, path, null, action);
        Container response;
        try {
            response = signee.findWebServiceProvider().createServicePort().solicit(builder.getContainer());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        final List<Envelope> l = DocumentUtilities.findEnvelope(response, path);
        final DocumentsModel docModel = findDocumentsModel();
        final Map<DocumentId, List<DocumentInfo>> ret;
        try {
            ret = l.stream()
                    .filter(Entry.class::isInstance)
                    .map(Entry.class::cast)
                    .filter(t -> t.getIdentity().equals(signee.getSignee()))
                    .flatMap(t -> t.getChildren().stream())
                    .filter(XmlDocumentEntry.class::isInstance)
                    .map(XmlDocumentEntry.class::cast)
                    .collect(Collectors.groupingBy(de -> docModel.convert(de.getIdentity()),
                            Collectors.mapping(de -> new DocumentInfo(de.getIdentity(), de.getValue().getSigneeInfos().keySet().stream().toArray(String[]::new)),
                                    Collectors.toList())));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        processException(ret, d);
//        final Map<DocumentId, Set<String>> res = udb.getTargetAssessmentDocuments(signee.getSignee(), null);
        map.clear();

//        final Map<DocumentId, List<DocumentInfo>> m = res.keySet().stream()
//                .collect(Collectors.groupingBy(docModel::convert,
//                        Collectors.mapping(d -> new DocumentInfo(d, res.get(d).stream().toArray(String[]::new)),
//                                Collectors.toList())));
        map.putAll(ret);
        map.keySet().stream()
                .forEach(toPopulate::add);
//        Arrays.stream(arr)
//                .forEach(toPopulate::add);
        return true;
    }
    
    @Override
    protected Node createNodeForKey(DocumentId key) {
        final Object[] arr = Stream.concat(map.get(key).stream(), Stream.of(signee, model)).toArray();
        return new DocumentIdNode(map.get(key), key, arr);
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
        final LocalFileProperties lfp = LocalFileProperties.find(model.getProviderInfo().getURL());
        final String jmsProp = lfp.getProperty("jms.providerURL", model.getProviderInfo().getURL());
        if (jmsProp != null) {
            service = JMSTopicListenerService.find(jmsProp, JMSTopic.DOCUMENTS_TOPIC.getJmsResource());
            if (service != null) {
                service.registerListener(MultiTargetAssessmentEvent.class, listener);
            }
        }
    }
    
    @Override
    protected void removeNotify() {
        super.removeNotify();
        if (service != null) {
            service.unregisterListener(listener);
            service = null;
            update = null;
        }
    }
    
    private RequestProcessor.Task getUpdateTask() {
        if (update == null) {
            update = Util.RP(model.getProviderInfo().getURL()).create(this::reload);
        }
        return update;
    }
    
    private void reload() {
        refresh(false);
    }
    
    private class Listener implements JMSListener<MultiTargetAssessmentEvent> {
        
        @Override
        public void addNotify() {
        }
        
        @Override
        public void removeNotify() {
        }
        
        @Override
        public void onMessage(MultiTargetAssessmentEvent event) {
            final RequestProcessor.Task t = getUpdateTask();
            if (t != null) {
                if (event.getType().equals(DocumentEventType.CHANGE) && event.getUpdates() != null) {
                    return;
                }
                t.schedule(WAIT_TIME);
            }
        }
        
    }
    
    private class DocumentIdNode extends AbstractNode {
        
        private final DocumentId baseDocument;
        private final DocumentInfo[] document;
        
        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private DocumentIdNode(final List<DocumentInfo> key, final DocumentId base, final Object[] arr) {
            super(key.size() == 1 ? Children.LEAF : Children.create(new BaseDocumentIdChildren(key), true), Lookups.fixed(arr));
            final String name = key.stream()
                    .map(k -> k.getDocument().toString())
                    .collect(Collectors.joining(","));
            setName(name);
            document = key.toArray(new DocumentInfo[key.size()]);
            baseDocument = base;
            setShortDescription(key.size() == 1 ? key.get(0).getDocument().getId() : baseDocument.getId());//Tooltip
            final String ib = key.size() == 1 ? RemoteTargetNode.ICON : RemoteTargetNode.ICON_COLL;
            setIconBaseWithExtension(ib);
//            annotationListener = new RemoteTargetNode.AnnotationListener();
//            annotationListener.init();
            updateName();
        }
        
        private void updateName() {
            final DocumentId disp = document.length == 1 ? document[0].getDocument() : baseDocument;
            final String resolve = getNamingResolver()
                    .map(nr -> {
                        try {
                            return nr.resolveDisplayName(disp);
                        } catch (IllegalAuthorityException ex) {
                            return null;
                        }
                    })
                    .orElse(disp.getId());
            final String e = Arrays.stream(document)
                    .flatMap(d -> Arrays.stream(d.getEntitlement()))
                    .filter(v -> !v.equals("entitled.signee"))
                    .map(v -> SigneeEntitlement.find(v).map(SigneeEntitlement::getDisplayName).orElse(v))
                    .collect(Collectors.joining(",", "[", "]"));
            setDisplayName(resolve + " " + e);
        }

        //
        @Override
        public Action[] getActions(boolean context) {
            return Utilities.actionsForPath("Loaders/" + MIME + "/Actions").stream()
                    .toArray(Action[]::new);
        }
        
    }
    
    private class BaseDocumentIdChildren extends ChildFactory<DocumentInfo> {
        
        private final DocumentInfo[] document;
        
        @SuppressWarnings("LeakingThisInConstructor")
        private BaseDocumentIdChildren(List<DocumentInfo> keys) {
            document = keys.toArray(new DocumentInfo[keys.size()]);
        }
        
        @Override
        protected boolean createKeys(List<DocumentInfo> toPopulate) {
            Arrays.stream(document)
                    .forEach(toPopulate::add);
            return true;
        }
        
        @Override
        protected Node createNodeForKey(DocumentInfo key) {
            final Object[] arr = new Object[]{document, signee, model};
            return new DocumentIdNode(Collections.singletonList(key), null, arr);
        }
        
    }
}
