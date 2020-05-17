package com.github.devex.demo;

import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.ide.plugins.CountComponent;
import com.intellij.ide.plugins.CountIcon;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.InstalledPluginsState;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.plugins.PluginManagerMain;
import com.intellij.ide.plugins.PluginNode;
import com.intellij.ide.plugins.PluginRepositoryRequests;
import com.intellij.ide.plugins.RepositoryHelper;
import com.intellij.ide.plugins.newui.LinkComponent;
import com.intellij.ide.plugins.newui.ListPluginComponent;
import com.intellij.ide.plugins.newui.MultiSelectionEventHandler;
import com.intellij.ide.plugins.newui.MyPluginModel;
import com.intellij.ide.plugins.newui.PluginDetailsPageComponent;
import com.intellij.ide.plugins.newui.PluginListLayout;
import com.intellij.ide.plugins.newui.PluginLogo;
import com.intellij.ide.plugins.newui.PluginUpdatesService;
import com.intellij.ide.plugins.newui.PluginsGroup;
import com.intellij.ide.plugins.newui.PluginsGroupComponent;
import com.intellij.ide.plugins.newui.PluginsGroupComponentWithProgress;
import com.intellij.ide.plugins.newui.PluginsTab;
import com.intellij.ide.plugins.newui.SearchPopup;
import com.intellij.ide.plugins.newui.SearchQueryParser;
import com.intellij.ide.plugins.newui.SearchResultPanel;
import com.intellij.ide.plugins.newui.SearchUpDownPopupController;
import com.intellij.ide.plugins.newui.TabbedPaneHeaderComponent;
import com.intellij.ide.plugins.newui.UIPluginGroup;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CheckedActionGroup;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.updateSettings.impl.PluginDownloader;
import com.intellij.openapi.updateSettings.impl.UpdateSettings;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.ThrowableNotNullFunction;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.SeparatorWithText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import com.intellij.ui.popup.PopupFactoryImpl;
import com.intellij.ui.popup.list.PopupListElementRenderer;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.Url;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StatusText;
import com.intellij.util.ui.UIUtil;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import static com.intellij.ide.plugins.PluginManagerConfigurable.getTags;
import static com.intellij.ide.plugins.PluginManagerConfigurable.registerCopyProvider;
import static com.intellij.ui.ScrollPaneFactory.createScrollPane;

public class Test {
    private JPanel test;

    private LinkListener<String> mySearchListener;
    private LinkListener<IdeaPluginDescriptor> myNameListener;
    private PluginsTab myMarketplaceTab;
    private static final Logger LOG = Logger.getInstance(Test.class);
    private PluginsGroupComponentWithProgress myMarketplacePanel;
    private final Object myRepositoriesLock = new Object();
    private Collection<IdeaPluginDescriptor> myCustomRepositoryPluginsList;
    private Map<String, List<IdeaPluginDescriptor>> myCustomRepositoryPluginsMap;
    private final MyPluginModel myPluginModel = new MyPluginModel() {

        @NotNull
        public Collection<IdeaPluginDescriptor> getCustomRepoPlugins() {
            return getCustomRepositoryPlugins();
        }
    };

    private PluginUpdatesService myPluginUpdatesService;

    private SearchResultPanel myMarketplaceSearchPanel;
    private List<IdeaPluginDescriptor> myAllRepositoryPluginsList;
    private Map<String, IdeaPluginDescriptor> myAllRepositoryPluginsMap;

    private void addGroup(@NotNull List<? super PluginsGroup> groups,
                          @NotNull Map<String, IdeaPluginDescriptor> allRepositoriesMap,
                          @NotNull String name,
                          @NotNull String query,
                          @NotNull String showAllQuery) throws IOException {
        addGroup(groups, name, showAllQuery, descriptors -> PluginRepositoryRequests.loadPlugins(descriptors, allRepositoriesMap, query));
    }

    private void addGroup(@NotNull List<? super PluginsGroup> groups,
                          @NotNull String name,
                          @NotNull String showAllQuery,
                          @NotNull ThrowableNotNullFunction<? super List<IdeaPluginDescriptor>, Boolean, ? extends IOException> function)
            throws IOException {
        PluginsGroup group = new PluginsGroup(name);

        if (Boolean.TRUE.equals(function.fun(group.descriptors))) {
            //noinspection unchecked
            group.rightAction = new LinkLabel("Show All", null, myMarketplaceTab.mySearchListener, showAllQuery);
            group.rightAction.setBorder(JBUI.Borders.emptyRight(5));
        }

        if (!group.descriptors.isEmpty()) {
            groups.add(group);
        }
    }

    public Test() {
        myUpdateAll.setVisible(false);
        myUpdateCounter.setVisible(false);

        // System.setProperty("idea.plugins.host", "https://plugins.jetbrains.com/plugins/list/?uuid=0ba1421c-c8ba-41da-bef5-090718227436&build=IC-193.6494.35");
        myPluginUpdatesService = PluginUpdatesService.connectConfigurable(countValue -> {
            int count = countValue == null ? 0 : countValue;
            String text = String.valueOf(count);
            boolean visible = count > 0;

            myUpdateAll.setEnabled(true);
            myUpdateAll.setVisible(visible);

            myUpdateCounter.setText(text);
            myUpdateCounter.setVisible(visible);

            myCountIcon.setText(text);
            myTabHeaderComponent.update();
        });

        myPluginModel.setPluginUpdatesService(myPluginUpdatesService);

        myNameListener = (aSource, aLinkData) -> {
            // TODO: unused
        };
        mySearchListener = (aSource, aLinkData) -> {
            // TODO: unused
        };

        createMarketplaceTab();
        createInstalledTab();

        test.add(myMarketplaceTab.createPanel());
    }

    @NotNull
    private Collection<IdeaPluginDescriptor> getCustomRepositoryPlugins() {
        synchronized (myRepositoriesLock) {
            if (myCustomRepositoryPluginsList != null) {
                return myCustomRepositoryPluginsList;
            }
        }
        LOG.info("PluginManagerConfigurable#getCustomRepoPlugins() has been called before PluginManagerConfigurable#createMarketplaceTab()"); // XXX
        return ContainerUtil.emptyList();
    }

    @NotNull
    public List<String> getPluginHosts() {
        /*List<String> hosts = new ArrayList<>(UpdateSettings.getInstance().getPluginHosts());
        ContainerUtil.addIfNotNull(hosts, ApplicationInfoEx.getInstanceEx().getBuiltinPluginsUrl());
        hosts.add(null);  // main plugin repository*/
        List<String> hosts = new ArrayList<>(UpdateSettings.getInstance().getPluginHosts());
        hosts.add("https://plugins.jetbrains.com/plugins/list/?uuid=0ba1421c-c8ba-41da-bef5-090718227436&build=IC-193.6494.35");
        return hosts;
    }

    @NotNull
    private Pair<Map<String, IdeaPluginDescriptor>, Map<String, List<IdeaPluginDescriptor>>> loadRepositoryPlugins() {
        synchronized (myRepositoriesLock) {
            if (myAllRepositoryPluginsMap != null) {
                return Pair.create(myAllRepositoryPluginsMap, myCustomRepositoryPluginsMap);
            }
        }

        List<IdeaPluginDescriptor> list = new ArrayList<>();
        Map<String, IdeaPluginDescriptor> map = new HashMap<>();
        Map<String, List<IdeaPluginDescriptor>> custom = new HashMap<>();

        for (String host : RepositoryHelper.getPluginHosts()) {
            try {
                List<IdeaPluginDescriptor> descriptors = RepositoryHelper.loadPlugins(host, null);
                if (host != null) {
                    custom.put(host, descriptors);
                }
                for (IdeaPluginDescriptor plugin : descriptors) {
                    String id = plugin.getPluginId().getIdString();
                    if (!map.containsKey(id)) {
                        list.add(plugin);
                        map.put(id, plugin);
                    }
                }
            } catch (IOException e) {
                if (host == null) {
                    LOG
                            .info("Main plugin repository is not available ('" + e.getMessage() + "'). Please check your network settings.");
                } else {
                    LOG.info(host, e);
                }
            }
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            InstalledPluginsState state = InstalledPluginsState.getInstance();
            for (IdeaPluginDescriptor descriptor : list) {
                state.onDescriptorDownload(descriptor);
            }
        });

        synchronized (myRepositoriesLock) {
            if (myAllRepositoryPluginsList == null) {
                myAllRepositoryPluginsList = list;
                myAllRepositoryPluginsMap = map;
                myCustomRepositoryPluginsMap = custom;
            }
            return Pair.create(myAllRepositoryPluginsMap, myCustomRepositoryPluginsMap);
        }
    }


    public JComponent getContent() {
        return test;
    }

    @NotNull
    private List<IdeaPluginDescriptor> getRepositoryPlugins() {
        synchronized (myRepositoriesLock) {
            if (myAllRepositoryPluginsList != null) {
                return myAllRepositoryPluginsList;
            }
        }
        try {
            List<IdeaPluginDescriptor> list = RepositoryHelper.loadCachedPlugins();
            if (list != null) {
                return list;
            }
        } catch (IOException e) {
            PluginManagerMain.LOG.info(e);
        }
        return Collections.emptyList();
    }

    private static final int MARKETPLACE_TAB = 0;
    private static final int INSTALLED_TAB = 1;
    private Runnable myMarketplaceRunnable;
    public static final int ITEMS_PER_GROUP = 9;
    private List<String> myTagsSorted;
    private List<String> myVendorsSorted;

    private DefaultActionGroup myMarketplaceSortByGroup;
    private Consumer<MarketplaceSortByAction> myMarketplaceSortByCallback;
    private LinkComponent myMarketplaceSortByAction;

    public void createMarketplaceTab() {
        myMarketplaceTab = new PluginsTab() {
            @Override
            protected void createSearchTextField(int flyDelay) {
                super.createSearchTextField(250);
                mySearchTextField.setHistoryPropertyName("MarketplacePluginsSearchHistory");
            }

            @NotNull
            @Override
            protected PluginDetailsPageComponent createDetailsPanel(@NotNull LinkListener<Object> searchListener) {
                PluginDetailsPageComponent detailPanel = new PluginDetailsPageComponent(myPluginModel, searchListener, true);
                myPluginModel.addDetailPanel(detailPanel);
                return detailPanel;
            }

            @NotNull
            @Override
            protected JComponent createPluginsPanel(@NotNull Consumer<? super PluginsGroupComponent> selectionListener) {
                MultiSelectionEventHandler eventHandler = new MultiSelectionEventHandler();
                myMarketplacePanel =
                        new PluginsGroupComponentWithProgress(new PluginListLayout(), eventHandler, myNameListener,
                                Test.this.mySearchListener,
                                d -> new ListPluginComponent(myPluginModel, d, mySearchListener, true));

                myMarketplacePanel.setSelectionListener(selectionListener);
                registerCopyProvider(myMarketplacePanel);

                //noinspection ConstantConditions
                ((SearchUpDownPopupController) myMarketplaceSearchPanel.controller).setEventHandler(eventHandler);

                Runnable runnable = () -> {
                    List<PluginsGroup> groups = new ArrayList<>();

                    try {
                        Pair<Map<String, IdeaPluginDescriptor>, Map<String, List<IdeaPluginDescriptor>>> pair = loadRepositoryPlugins();
                        Map<String, IdeaPluginDescriptor> allRepositoriesMap = pair.first;
                        Map<String, List<IdeaPluginDescriptor>> customRepositoriesMap = pair.second;

                        try {
                            addGroup(groups, allRepositoriesMap, "Featured", "is_featured_search=true", "/sortBy:featured");
                            addGroup(groups, allRepositoriesMap, "New and Updated", "orderBy=update+date", "/sortBy:updated");
                            addGroup(groups, allRepositoriesMap, "Top Downloads", "orderBy=downloads", "/sortBy:downloads");
                            addGroup(groups, allRepositoriesMap, "Top Rated", "orderBy=rating", "/sortBy:rating");
                        } catch (IOException e) {
                            PluginManagerMain.LOG
                                    .info("Main plugin repository is not available ('" + e.getMessage() + "'). Please check your network settings.");
                        }

                        for (String host : UpdateSettings.getInstance().getPluginHosts()) {
                            List<IdeaPluginDescriptor> allDescriptors = customRepositoriesMap.get(host);
                            if (allDescriptors != null) {
                                addGroup(groups, "Repository: " + host, "/repository:\"" + host + "\"", descriptors -> {
                                    int allSize = allDescriptors.size();
                                    descriptors.addAll(ContainerUtil.getFirstItems(allDescriptors, ITEMS_PER_GROUP));
                                    PluginsGroup.sortByName(descriptors);
                                    return allSize > ITEMS_PER_GROUP;
                                });
                            }
                        }
                    } catch (IOException e) {
                        PluginManagerMain.LOG.info(e);
                    } finally {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            myMarketplacePanel.stopLoading();
                            PluginLogo.startBatchMode();

                            for (PluginsGroup group : groups) {
                                myMarketplacePanel.addGroup(group);
                            }

                            PluginLogo.endBatchMode();
                            myMarketplacePanel.doLayout();
                            myMarketplacePanel.initialSelection();
                        }, ModalityState.any());
                    }
                };

                myMarketplaceRunnable = () -> {
                    myMarketplacePanel.clear();
                    myMarketplacePanel.startLoading();
                    ApplicationManager.getApplication().executeOnPooledThread(runnable);
                };

                myMarketplacePanel.getEmptyText().setText("Marketplace plugins are not loaded.")
                        .appendSecondaryText("Check the internet connection and ", StatusText.DEFAULT_ATTRIBUTES, null)
                        .appendSecondaryText("refresh", SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES, e -> myMarketplaceRunnable.run());

                ApplicationManager.getApplication().executeOnPooledThread(runnable);
                return createScrollPane(myMarketplacePanel, false);
            }

            @Override
            protected void updateMainSelection(@NotNull Consumer<? super PluginsGroupComponent> selectionListener) {
                selectionListener.accept(myMarketplacePanel);
            }

            @NotNull
            @Override
            protected SearchResultPanel createSearchPanel(@NotNull Consumer<? super PluginsGroupComponent> selectionListener) {
                SearchUpDownPopupController marketplaceController = new SearchUpDownPopupController(mySearchTextField) {
                    @NotNull
                    @Override
                    protected List<String> getAttributes() {
                        List<String> attributes = new ArrayList<>();
                        attributes.add("/tag:");
                        attributes.add("/sortBy:");
                        attributes.add("/vendor:");
                        if (!UpdateSettings.getInstance().getPluginHosts().isEmpty()) {
                            attributes.add("/repository:");
                        }
                        return attributes;
                    }

                    @Nullable
                    @Override
                    protected List<String> getValues(@NotNull String attribute) {
                        switch (attribute) {
                            case "/tag:":
                                if (ContainerUtil.isEmpty(myTagsSorted)) {
                                    Set<String> allTags = new HashSet<>();
                                    for (IdeaPluginDescriptor descriptor : getRepositoryPlugins()) {
                                        if (descriptor instanceof PluginNode) {
                                            List<String> tags = ((PluginNode) descriptor).getTags();
                                            if (!ContainerUtil.isEmpty(tags)) {
                                                allTags.addAll(tags);
                                            }
                                        }
                                    }
                                    myTagsSorted = ContainerUtil.sorted(allTags, String::compareToIgnoreCase);
                                }
                                return myTagsSorted;
                            case "/sortBy:":
                                return Arrays.asList("downloads", "name", "rating", "updated");
                            case "/vendor:":
                                if (ContainerUtil.isEmpty(myVendorsSorted)) {
                                    myVendorsSorted = MyPluginModel.getVendors(getRepositoryPlugins());
                                }
                                return myVendorsSorted;
                            case "/repository:":
                                return UpdateSettings.getInstance().getPluginHosts();
                        }
                        return null;
                    }

                    @Override
                    protected void handleAppendToQuery() {
                        showPopupForQuery();
                    }

                    @Override
                    protected void handleAppendAttributeValue() {
                        showPopupForQuery();
                    }

                    @Override
                    protected void showPopupForQuery() {
                        hidePopup();
                        showSearchPanel(mySearchTextField.getText());
                    }

                    @Override
                    protected void handleEnter() {
                        if (!mySearchTextField.getText().isEmpty()) {
                            handleTrigger("marketplace.suggest.popup.enter");
                        }
                    }

                    @Override
                    protected void handlePopupListFirstSelection() {
                        handleTrigger("marketplace.suggest.popup.select");
                    }

                    private void handleTrigger(@NonNls String key) {
                        if (myPopup != null && myPopup.type == SearchPopup.Type.SearchQuery) {
                            FeatureUsageTracker.getInstance().triggerFeatureUsed(key);
                        }
                    }
                };

                myMarketplaceSortByGroup = new DefaultActionGroup();

                for (SortBySearchOption option : SortBySearchOption.values()) {
                    myMarketplaceSortByGroup.addAction(new MarketplaceSortByAction(option));
                }

                myMarketplaceSortByAction = new LinkComponent() {
                    @Override
                    protected boolean isInClickableArea(Point pt) {
                        return true;
                    }
                };
                myMarketplaceSortByAction.setIcon(new Icon() {
                    @Override
                    public void paintIcon(Component c, Graphics g, int x, int y) {
                        getIcon().paintIcon(c, g, x, y + 1);
                    }

                    @Override
                    public int getIconWidth() {
                        return getIcon().getIconWidth();
                    }

                    @Override
                    public int getIconHeight() {
                        return getIcon().getIconHeight();
                    }

                    @NotNull
                    private Icon getIcon() {
                        return AllIcons.General.ButtonDropTriangle;
                    }
                }); // TODO: icon
                myMarketplaceSortByAction.setPaintUnderline(false);
                myMarketplaceSortByAction.setIconTextGap(JBUIScale.scale(4));
                myMarketplaceSortByAction.setHorizontalTextPosition(SwingConstants.LEFT);
                myMarketplaceSortByAction.setForeground(PluginsGroupComponent.SECTION_HEADER_FOREGROUND);

                //noinspection unchecked
                myMarketplaceSortByAction.setListener(
                        (component, __) -> showRightBottomPopup(component.getParent().getParent(), "Sort By", myMarketplaceSortByGroup), null);

                myMarketplaceSortByCallback = updateAction -> {
                    MarketplaceSortByAction removeAction = null;
                    MarketplaceSortByAction addAction = null;

                    if (updateAction.myState) {
                        for (AnAction action : myMarketplaceSortByGroup.getChildren(null)) {
                            MarketplaceSortByAction sortByAction = (MarketplaceSortByAction) action;
                            if (sortByAction != updateAction && sortByAction.myState) {
                                sortByAction.myState = false;
                                removeAction = sortByAction;
                                break;
                            }
                        }
                        addAction = updateAction;
                    } else {
                        if (updateAction.myOption == SortBySearchOption.Relevance) {
                            updateAction.myState = true;
                            return;
                        }

                        for (AnAction action : myMarketplaceSortByGroup.getChildren(null)) {
                            MarketplaceSortByAction sortByAction = (MarketplaceSortByAction) action;
                            if (sortByAction.myOption == SortBySearchOption.Relevance) {
                                sortByAction.myState = true;
                                break;
                            }
                        }

                        removeAction = updateAction;
                    }

                    List<String> queries = new ArrayList<>();
                    new SearchQueryParser.Marketplace(mySearchTextField.getText()) {
                        @Override
                        protected void addToSearchQuery(@NotNull String query) {
                            super.addToSearchQuery(query);
                            queries.add(query);
                        }

                        @Override
                        protected void handleAttribute(@NotNull String name, @NotNull String value, boolean invert) {
                            super.handleAttribute(name, value, invert);
                            queries.add(name + ":" + SearchQueryParser.wrapAttribute(value));
                        }
                    };
                    if (removeAction != null) {
                        String query = removeAction.getQuery();
                        if (query != null) {
                            queries.remove(query);
                        }
                    }
                    if (addAction != null) {
                        String query = addAction.getQuery();
                        if (query != null) {
                            queries.add(query);
                        }
                    }

                    String query = StringUtil.join(queries, " ");
                    mySearchTextField.setTextIgnoreEvents(query);
                    if (query.isEmpty()) {
                        myMarketplaceTab.hideSearchPanel();
                    } else {
                        myMarketplaceTab.showSearchPanel(query);
                    }
                };

                MultiSelectionEventHandler eventHandler = new MultiSelectionEventHandler();
                marketplaceController.setSearchResultEventHandler(eventHandler);

                PluginsGroupComponentWithProgress panel =
                        new PluginsGroupComponentWithProgress(new PluginListLayout(), eventHandler, myNameListener,
                                Test.this.mySearchListener,
                                descriptor -> new ListPluginComponent(myPluginModel, descriptor, mySearchListener,
                                        true));

                panel.setSelectionListener(selectionListener);
                registerCopyProvider(panel);

                myMarketplaceSearchPanel =
                        new SearchResultPanel(marketplaceController, panel, 0, 0) {
                            @Override
                            protected void handleQuery(@NotNull String query, @NotNull PluginsGroup result) {
                                try {
                                    Pair<Map<String, IdeaPluginDescriptor>, Map<String, List<IdeaPluginDescriptor>>> p = loadRepositoryPlugins();
                                    Map<String, IdeaPluginDescriptor> allRepositoriesMap = p.first;
                                    Map<String, List<IdeaPluginDescriptor>> customRepositoriesMap = p.second;

                                    SearchQueryParser.Marketplace parser = new SearchQueryParser.Marketplace(query);

                                    // TODO: parser.vendors on server
                                    if (!parser.vendors.isEmpty()) {
                                        for (IdeaPluginDescriptor descriptor : getRepositoryPlugins()) {
                                            if (MyPluginModel.isVendor(descriptor, parser.vendors)) {
                                                result.descriptors.add(descriptor);
                                            }
                                        }
                                        ContainerUtil.removeDuplicates(result.descriptors);
                                        result.sortByName();
                                        return;
                                    }

                                    if (!parser.repositories.isEmpty()) {
                                        for (String repository : parser.repositories) {
                                            List<IdeaPluginDescriptor> descriptors = customRepositoriesMap.get(repository);
                                            if (descriptors == null) {
                                                continue;
                                            }
                                            if (parser.searchQuery == null) {
                                                result.descriptors.addAll(descriptors);
                                            } else {
                                                for (IdeaPluginDescriptor descriptor : descriptors) {
                                                    if (StringUtil.containsIgnoreCase(descriptor.getName(), parser.searchQuery)) {
                                                        result.descriptors.add(descriptor);
                                                    }
                                                }
                                            }
                                        }
                                        ContainerUtil.removeDuplicates(result.descriptors);
                                        result.sortByName();
                                        return;
                                    }

                                    Url url = PluginRepositoryRequests.createSearchUrl(parser.getUrlQuery(), 10000);
                                    for (String pluginId : PluginRepositoryRequests.requestToPluginRepository(url)) {
                                        IdeaPluginDescriptor descriptor = allRepositoriesMap.get(pluginId);
                                        if (descriptor != null) {
                                            result.descriptors.add(descriptor);
                                        }
                                    }

                                    if (parser.searchQuery != null) {
                                        String builtinUrl = ApplicationInfoEx.getInstanceEx().getBuiltinPluginsUrl();
                                        List<IdeaPluginDescriptor> builtinList = new ArrayList<>();

                                        for (Map.Entry<String, List<IdeaPluginDescriptor>> entry : customRepositoriesMap.entrySet()) {
                                            List<IdeaPluginDescriptor> descriptors = entry.getKey().equals(builtinUrl) ? builtinList : result.descriptors;
                                            for (IdeaPluginDescriptor descriptor : entry.getValue()) {
                                                if (StringUtil.containsIgnoreCase(descriptor.getName(), parser.searchQuery)) {
                                                    descriptors.add(descriptor);
                                                }
                                            }
                                        }

                                        result.descriptors.addAll(0, builtinList);
                                    }

                                    if (result.descriptors.isEmpty() && "/tag:Paid".equals(query)) {
                                        for (IdeaPluginDescriptor descriptor : getRepositoryPlugins()) {
                                            if (descriptor.getProductCode() != null) {
                                                result.descriptors.add(descriptor);
                                            }
                                        }
                                        result.sortByName();
                                    }

                                    ContainerUtil.removeDuplicates(result.descriptors);

                                    if (!result.descriptors.isEmpty()) {
                                        String title = "Sort By";

                                        for (AnAction action : myMarketplaceSortByGroup.getChildren(null)) {
                                            MarketplaceSortByAction sortByAction = (MarketplaceSortByAction) action;
                                            sortByAction.setState(parser);
                                            if (sortByAction.myState) {
                                                title = "Sort By: " + sortByAction.myOption.name();
                                            }
                                        }

                                        myMarketplaceSortByAction.setText(title);
                                        result.addRightAction(myMarketplaceSortByAction);
                                    }
                                } catch (IOException e) {
                                    PluginManagerMain.LOG.info(e);

                                    ApplicationManager.getApplication().invokeLater(() -> myPanel.getEmptyText().setText("Search result are not loaded.")
                                            .appendSecondaryText("Check the internet connection.", StatusText.DEFAULT_ATTRIBUTES, null), ModalityState.any());
                                }
                            }
                        };

                return myMarketplaceSearchPanel;
            }
        };
    }

    /*
    public final LinkListener<Object> mySearchListener = (__, data) -> {
        String query;
        if (data instanceof String) {
            query = (String)data;
        } else {
            if (!(data instanceof TagComponent)) {
                return;
            }

            query = "/" + SearchQueryParser.getTagQuery(((TagComponent)data).getText());
        }

        this.mySearchTextField.setTextIgnoreEvents(query);
        IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> {
            IdeFocusManager.getGlobalInstance().requestFocus(this.mySearchTextField, true);
        });
        this.mySearchPanel.setEmpty();
        this.showSearchPanel(query);
    };*/

    private class MarketplaceSortByAction extends ToggleAction implements DumbAware {
        private final SortBySearchOption myOption;
        private boolean myState;

        private MarketplaceSortByAction(@NotNull SortBySearchOption option) {
            super(option.name());
            myOption = option;
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return myState;
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            myState = state;
            myMarketplaceSortByCallback.accept(this);
        }

        public void setState(@NotNull SearchQueryParser.Marketplace parser) {
            if (myOption == SortBySearchOption.Relevance) {
                myState = parser.sortBy == null;
                getTemplatePresentation().setVisible(parser.sortBy == null || !parser.tags.isEmpty() || parser.searchQuery != null);
            } else {
                myState = parser.sortBy != null && myOption.name().equalsIgnoreCase(parser.sortBy);
            }
        }

        @Nullable
        public String getQuery() {
            switch (myOption) {
                case Downloads:
                    return "/sortBy:downloads";
                case Name:
                    return "/sortBy:name";
                case Rating:
                    return "/sortBy:rating";
                case Updated:
                    return "/sortBy:updated";
                case Relevance:
                default:
                    return null;
            }
        }
    }

    private enum SortBySearchOption {
        Downloads, Name, Rating, Relevance, Updated
    }

    private static class GroupByActionGroup extends DefaultActionGroup implements CheckedActionGroup {
    }

    private static void showRightBottomPopup(@NotNull Component component, @NotNull String title, @NotNull ActionGroup group) {
        DefaultActionGroup actions = new GroupByActionGroup();
        actions.addSeparator("  " + title);
        actions.addAll(group);

        DataContext context = DataManager.getInstance().getDataContext(component);

        JBPopup popup = new PopupFactoryImpl.ActionGroupPopup(null, actions, context, false, false, false, true, null, -1, null, null) {
            @Override
            protected ListCellRenderer getListElementRenderer() {
                return new PopupListElementRenderer(this) {
                    @Override
                    protected SeparatorWithText createSeparator() {
                        return new SeparatorWithText() {
                            {
                                setTextForeground(JBColor.BLACK);
                                setFont(UIUtil.getLabelFont());
                                setCaptionCentered(false);
                            }

                            @Override
                            protected void paintLine(Graphics g, int x, int y, int width) {
                            }
                        };
                    }

                    @Override
                    protected Border getDefaultItemComponentBorder() {
                        return new EmptyBorder(JBInsets.create(UIUtil.getListCellVPadding(), 15));
                    }
                };
            }
        };
        popup.addListener(new JBPopupListener() {
            @Override
            public void beforeShown(@NotNull LightweightWindowEvent event) {
                Point location = component.getLocationOnScreen();
                Dimension size = popup.getSize();
                popup.setLocation(new Point(location.x + component.getWidth() - size.width, location.y + component.getHeight()));
            }
        });
        popup.show(component);
    }

    private DefaultActionGroup myInstalledSearchGroup;
    private Consumer<InstalledSearchOptionAction> myInstalledSearchCallback;
    private boolean myInstalledSearchSetState = true;
    private PluginsTab myInstalledTab;
    private PluginsGroupComponent myInstalledPanel;
    private SearchResultPanel myInstalledSearchPanel;

    private final LinkLabel<Object> myUpdateAll = new LinkLabel<>("Update All", null);
    private final JLabel myUpdateCounter = new CountComponent();
    private final CountIcon myCountIcon = new CountIcon();
    private TabbedPaneHeaderComponent myTabHeaderComponent;


    private void createInstalledTab() {
        myInstalledSearchGroup = new DefaultActionGroup();

        for (InstalledSearchOption option : InstalledSearchOption.values()) {
            myInstalledSearchGroup.add(new InstalledSearchOptionAction(option));
        }

        myInstalledTab = new PluginsTab() {
            @Override
            protected void createSearchTextField(int flyDelay) {
                super.createSearchTextField(flyDelay);

                JBTextField textField = mySearchTextField.getTextEditor();
                textField.putClientProperty("search.extension", ExtendableTextComponent.Extension
                        .create(AllIcons.Actions.More, AllIcons.Actions.More, "Search Options", // TODO: icon
                                () -> showRightBottomPopup(textField, "Show", myInstalledSearchGroup)));
                textField.putClientProperty("JTextField.variant", null);
                textField.putClientProperty("JTextField.variant", "search");

                mySearchTextField.setHistoryPropertyName("InstalledPluginsSearchHistory");
            }

            @NotNull
            @Override
            protected PluginDetailsPageComponent createDetailsPanel(@NotNull LinkListener<Object> searchListener) {
                PluginDetailsPageComponent detailPanel = new PluginDetailsPageComponent(myPluginModel, searchListener, false);
                myPluginModel.addDetailPanel(detailPanel);
                return detailPanel;
            }

            @NotNull
            @Override
            protected JComponent createPluginsPanel(@NotNull Consumer<? super PluginsGroupComponent> selectionListener) {
                MultiSelectionEventHandler eventHandler = new MultiSelectionEventHandler();
                myInstalledPanel =
                        new PluginsGroupComponent(new PluginListLayout(), eventHandler, myNameListener,
                                Test.this.mySearchListener,
                                descriptor -> new ListPluginComponent(myPluginModel, descriptor, mySearchListener, false));

                myInstalledPanel.setSelectionListener(selectionListener);
                registerCopyProvider(myInstalledPanel);

                //noinspection ConstantConditions
                ((SearchUpDownPopupController) myInstalledSearchPanel.controller).setEventHandler(eventHandler);

                PluginLogo.startBatchMode();

                PluginsGroup installing = new PluginsGroup("Installing");
                installing.descriptors.addAll(MyPluginModel.getInstallingPlugins());
                if (!installing.descriptors.isEmpty()) {
                    installing.sortByName();
                    installing.titleWithCount();
                    myInstalledPanel.addGroup(installing);
                }

                PluginsGroup downloaded = new PluginsGroup("Downloaded");
                downloaded.descriptors.addAll(InstalledPluginsState.getInstance().getInstalledPlugins());

                PluginsGroup bundled = new PluginsGroup("Bundled");

                ApplicationInfoEx appInfo = ApplicationInfoEx.getInstanceEx();
                int bundledEnabled = 0;
                int downloadedEnabled = 0;

                boolean hideImplDetails = !Registry.is("plugins.show.implementation.details");

                for (IdeaPluginDescriptor descriptor : PluginManagerCore.getPlugins()) {
                    if (!appInfo.isEssentialPlugin(descriptor.getPluginId().getIdString())) {
                        if (descriptor.isBundled()) {
                            if (hideImplDetails && descriptor.isImplementationDetail()) {
                                continue;
                            }
                            bundled.descriptors.add(descriptor);
                            if (descriptor.isEnabled()) {
                                bundledEnabled++;
                            }
                        } else {
                            downloaded.descriptors.add(descriptor);
                            if (descriptor.isEnabled()) {
                                downloadedEnabled++;
                            }
                        }
                    }
                }

                if (!downloaded.descriptors.isEmpty()) {
                    myUpdateAll.setListener(new LinkListener<Object>() {
                        @Override
                        public void linkSelected(LinkLabel aSource, Object aLinkData) {
                            myUpdateAll.setEnabled(false);

                            for (ListPluginComponent plugin : downloaded.ui.plugins) {
                                ((ListPluginComponent) plugin).updatePlugin();
                            }
                        }
                    }, null);
                    downloaded.addRightAction(myUpdateAll);

                    downloaded.addRightAction(myUpdateCounter);

                    downloaded.sortByName();
                    downloaded.titleWithCount(downloadedEnabled);
                    myInstalledPanel.addGroup(downloaded);
                    myPluginModel.addEnabledGroup(downloaded);
                }

                myPluginModel.setDownloadedGroup(myInstalledPanel, downloaded, installing);

                bundled.sortByName();
                bundled.titleWithCount(bundledEnabled);
                myInstalledPanel.addGroup(bundled);
                myPluginModel.addEnabledGroup(bundled);

                myPluginUpdatesService.connectInstalled(updates -> {
                    if (ContainerUtil.isEmpty(updates)) {
                        clearUpdates(myInstalledPanel);
                        clearUpdates(myInstalledSearchPanel.getPanel());
                    } else {
                        applyUpdates(myInstalledPanel, updates);
                        applyUpdates(myInstalledSearchPanel.getPanel(), updates);
                    }
                    selectionListener.accept(myInstalledPanel);
                });

                PluginLogo.endBatchMode();

                return createScrollPane(myInstalledPanel, true);
            }

            @Override
            protected void updateMainSelection(@NotNull Consumer<? super PluginsGroupComponent> selectionListener) {
                selectionListener.accept(myInstalledPanel);
            }

            @Override
            public void hideSearchPanel() {
                super.hideSearchPanel();
                if (myInstalledSearchSetState) {
                    for (AnAction action : myInstalledSearchGroup.getChildren(null)) {
                        ((InstalledSearchOptionAction) action).setState(null);
                    }
                }
                myPluginModel.setInvalidFixCallback(null);
            }

            @NotNull
            @Override
            protected SearchResultPanel createSearchPanel(@NotNull Consumer<? super PluginsGroupComponent> selectionListener) {
                SearchUpDownPopupController installedController = new SearchUpDownPopupController(mySearchTextField) {
                    @NotNull
                    @Override
                    protected List<String> getAttributes() {
                        return Arrays.asList("/downloaded", "/outdated", "/enabled", "/disabled", "/invalid", "/bundled", "/vendor:", "/tag:");
                    }

                    @Nullable
                    @Override
                    protected List<String> getValues(@NotNull String attribute) {
                        if ("/vendor:".equals(attribute)) {
                            return myPluginModel.getVendors();
                        }
                        if ("/tag:".equals(attribute)) {
                            return myPluginModel.getTags();
                        }
                        return null;
                    }

                    @Override
                    protected void handleAppendToQuery() {
                        showPopupForQuery();
                    }

                    @Override
                    protected void handleAppendAttributeValue() {
                        showPopupForQuery();
                    }

                    @Override
                    protected void showPopupForQuery() {
                        showSearchPanel(mySearchTextField.getText());
                    }
                };

                MultiSelectionEventHandler eventHandler = new MultiSelectionEventHandler();
                installedController.setSearchResultEventHandler(eventHandler);

                PluginsGroupComponent panel =
                        new PluginsGroupComponent(new PluginListLayout(), eventHandler, myNameListener,
                                Test.this.mySearchListener,
                                descriptor -> new ListPluginComponent(myPluginModel, descriptor, mySearchListener, false));

                panel.setSelectionListener(selectionListener);
                registerCopyProvider(panel);

                myInstalledSearchCallback = updateAction -> {
                    List<String> queries = new ArrayList<>();
                    new SearchQueryParser.InstalledWithVendorAndTag(mySearchTextField.getText()) {
                        @Override
                        protected void addToSearchQuery(@NotNull String query) {
                            super.addToSearchQuery(query);
                            queries.add(query);
                        }

                        @Override
                        protected void handleAttribute(@NotNull String name, @NotNull String value, boolean invert) {
                            super.handleAttribute(name, value, invert);
                            if (!updateAction.myState) {
                                queries.add("/" + name + (value.isEmpty() ? "" : ":" + SearchQueryParser.wrapAttribute(value)));
                            }
                        }
                    };

                    if (updateAction.myState) {
                        for (AnAction action : myInstalledSearchGroup.getChildren(null)) {
                            if (action != updateAction) {
                                ((InstalledSearchOptionAction) action).myState = false;
                            }
                        }

                        queries.add(updateAction.getQuery());
                    } else {
                        queries.remove(updateAction.getQuery());
                    }

                    try {
                        myInstalledSearchSetState = false;

                        String query = StringUtil.join(queries, " ");
                        mySearchTextField.setTextIgnoreEvents(query);
                        if (query.isEmpty()) {
                            myInstalledTab.hideSearchPanel();
                        } else {
                            myInstalledTab.showSearchPanel(query);
                        }
                    } finally {
                        myInstalledSearchSetState = true;
                    }
                };

                myInstalledSearchPanel = new SearchResultPanel(installedController, panel, 0, 0) {
                    @Override
                    protected void setEmptyText() {
                        myPanel.getEmptyText().setText("Nothing found.");
                        myPanel.getEmptyText().appendSecondaryText("Search in Marketplace", SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES,
                                e -> myTabHeaderComponent.setSelectionWithEvents(MARKETPLACE_TAB));
                    }

                    @Override
                    protected void handleQuery(@NotNull String query, @NotNull PluginsGroup result) {
                        myPluginModel.setInvalidFixCallback(null);

                        SearchQueryParser.InstalledWithVendorAndTag parser = new SearchQueryParser.InstalledWithVendorAndTag(query);

                        if (myInstalledSearchSetState) {
                            for (AnAction action : myInstalledSearchGroup.getChildren(null)) {
                                ((InstalledSearchOptionAction) action).setState(parser);
                            }
                        }

                        List<IdeaPluginDescriptor> descriptors = myPluginModel.getInstalledDescriptors();

                        if (!parser.vendors.isEmpty()) {
                            for (Iterator<IdeaPluginDescriptor> I = descriptors.iterator(); I.hasNext(); ) {
                                if (!MyPluginModel.isVendor(I.next(), parser.vendors)) {
                                    I.remove();
                                }
                            }
                        }
                        if (!parser.tags.isEmpty()) {
                            for (Iterator<IdeaPluginDescriptor> I = descriptors.iterator(); I.hasNext(); ) {
                                if (!ContainerUtil.intersects(getTags(I.next()), parser.tags)) {
                                    I.remove();
                                }
                            }
                        }
                        for (Iterator<IdeaPluginDescriptor> I = descriptors.iterator(); I.hasNext(); ) {
                            IdeaPluginDescriptor descriptor = I.next();
                            if (parser.attributes) {
                                if (parser.enabled && (!myPluginModel.isEnabled(descriptor) || myPluginModel.hasErrors(descriptor))) {
                                    I.remove();
                                    continue;
                                }
                                if (parser.disabled && (myPluginModel.isEnabled(descriptor) || myPluginModel.hasErrors(descriptor))) {
                                    I.remove();
                                    continue;
                                }
                                if (parser.bundled && !descriptor.isBundled()) {
                                    I.remove();
                                    continue;
                                }
                                if (parser.downloaded && descriptor.isBundled()) {
                                    I.remove();
                                    continue;
                                }
                                if (parser.invalid && !myPluginModel.hasErrors(descriptor)) {
                                    I.remove();
                                    continue;
                                }
                                if (parser.needUpdate && !PluginUpdatesService.isNeedUpdate(descriptor)) {
                                    I.remove();
                                    continue;
                                }
                            }
                            if (parser.searchQuery != null && !StringUtil.containsIgnoreCase(descriptor.getName(), parser.searchQuery)) {
                                I.remove();
                            }
                        }

                        result.descriptors.addAll(descriptors);

                        if (!result.descriptors.isEmpty()) {
                            if (parser.invalid) {
                                myPluginModel.setInvalidFixCallback(() -> {
                                    PluginsGroup group = myInstalledSearchPanel.getGroup();
                                    if (group.ui == null) {
                                        myPluginModel.setInvalidFixCallback(null);
                                        return;
                                    }

                                    PluginsGroupComponent resultPanel = myInstalledSearchPanel.getPanel();

                                    for (IdeaPluginDescriptor descriptor : new ArrayList<>(group.descriptors)) {
                                        if (!myPluginModel.hasErrors(descriptor)) {
                                            resultPanel.removeFromGroup(group, descriptor);
                                        }
                                    }

                                    group.titleWithCount();
                                    myInstalledSearchPanel.fullRepaint();

                                    if (group.descriptors.isEmpty()) {
                                        myPluginModel.setInvalidFixCallback(null);
                                    }
                                });
                            }

                            Collection<PluginDownloader> updates = PluginUpdatesService.getUpdates();
                            if (!ContainerUtil.isEmpty(updates)) {
                                myPostFillGroupCallback = () -> {
                                    applyUpdates(myPanel, updates);
                                    selectionListener.accept(myInstalledPanel);
                                };
                            }
                        }
                    }
                };

                return myInstalledSearchPanel;
            }
        };
    }

    private enum InstalledSearchOption {
        Downloaded, NeedUpdate, Enabled, Disabled, Invalid, Bundled
    }

    private class InstalledSearchOptionAction extends ToggleAction implements DumbAware {
        private final InstalledSearchOption myOption;
        private boolean myState;

        private InstalledSearchOptionAction(@NotNull InstalledSearchOption option) {
            super(option == InstalledSearchOption.NeedUpdate ? "Update available" : option.name());
            myOption = option;
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return myState;
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            myState = state;
            myInstalledSearchCallback.accept(this);
        }

        public void setState(@Nullable SearchQueryParser.InstalledWithVendorAndTag parser) {
            if (parser == null) {
                myState = false;
                return;
            }

            switch (myOption) {
                case Enabled:
                    myState = parser.enabled;
                    break;
                case Disabled:
                    myState = parser.disabled;
                    break;
                case Downloaded:
                    myState = parser.downloaded;
                    break;
                case Bundled:
                    myState = parser.bundled;
                    break;
                case Invalid:
                    myState = parser.invalid;
                    break;
                case NeedUpdate:
                    myState = parser.needUpdate;
                    break;
            }
        }

        @NotNull
        public String getQuery() {
            return myOption == InstalledSearchOption.NeedUpdate ? "/outdated" : "/" + StringUtil.decapitalize(myOption.name());
        }
    }

    private static void clearUpdates(@NotNull PluginsGroupComponent panel) {
        for (UIPluginGroup group : panel.getGroups()) {
            for (ListPluginComponent plugin : group.plugins) {
                ((ListPluginComponent) plugin).setUpdateDescriptor(null);
            }
        }
    }

    private static void applyUpdates(@NotNull PluginsGroupComponent panel, @NotNull Collection<? extends PluginDownloader> updates) {
        for (PluginDownloader downloader : updates) {
            IdeaPluginDescriptor descriptor = downloader.getDescriptor();
            for (UIPluginGroup group : panel.getGroups()) {
                ListPluginComponent component = group.findComponent(descriptor);
                if (component != null) {
                    ((ListPluginComponent) component).setUpdateDescriptor(descriptor);
                    break;
                }
            }
        }
    }
}
