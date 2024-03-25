package com.cool.request.common.state;

import java.awt.event.InputEvent;

public class SettingsState {
    public String apiFoxAuthorization;
    public String openApiToken;
    public int languageValue;
    public String environmentSelectId;
    public boolean enableDynamicRefresh;
    public boolean autoRefreshData;
    public boolean listenerGateway;
    public boolean autoNavigation;
    public boolean mergeApiAndRequest;
    public boolean userIdeaIcon;
    public String proxyIp;
    public int proxyPort;
    public boolean parameterCoverage;
    public boolean enabledScriptLibrary;
    public boolean addQuickSendButtonOnMethodLeft;
    public boolean enableProxy;
    public int treeAppearanceMode = 0;
    public int maxHTTPResponseSize;
    public int searchApiKeyCode = 0;
    public int searchApiModifiers = 0;
    public boolean requestAddUserAgent;
    public String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36";
    public int requestTimeout;
    public boolean listenerCURL;


    public boolean enabledTrace;
    public boolean userByteCodeCache;
    public int maxTraceDepth;
    public int timeConsumptionThreshold;
    public boolean useTraceLog;

    public boolean traceMybatis;

    public SettingsState() {
        apiFoxAuthorization = "";
        openApiToken = "";
        languageValue = 0;
        environmentSelectId = "";
        autoNavigation = true;
        listenerGateway = true;
        autoRefreshData = true;
        enableDynamicRefresh = true;
        mergeApiAndRequest = false;
        parameterCoverage = false;
        proxyPort = 0;
        proxyIp = "";
        enabledScriptLibrary = false;
        addQuickSendButtonOnMethodLeft = true;
        enableProxy = false;
        treeAppearanceMode = 0;
        userIdeaIcon = false;
        maxHTTPResponseSize = 10;
        searchApiKeyCode = java.awt.event.KeyEvent.VK_S;
        searchApiModifiers = InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK;
        requestTimeout = 10;
        listenerCURL = true;
        enabledTrace = true;
        maxTraceDepth = 5;
        timeConsumptionThreshold = 5;
        userByteCodeCache = true;
        useTraceLog = true;
        traceMybatis = true;
    }
}
