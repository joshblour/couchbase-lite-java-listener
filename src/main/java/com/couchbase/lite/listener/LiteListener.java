package com.couchbase.lite.listener;

import Acme.Serve.Serve; // https://github.com/couchbase/couchbase-lite-java-listener/issues/24
import com.couchbase.lite.Manager;
import com.couchbase.lite.router.RequestAuthorization; // Needed for https://github.com/couchbase/couchbase-lite-java-core/issues/44
import com.couchbase.lite.router.URLStreamHandlerFactory;

import java.util.Properties; // https://github.com/couchbase/couchbase-lite-java-listener/issues/24
import java.util.concurrent.ScheduledExecutorService;

public class LiteListener implements Runnable {

    private Thread thread;
    private Manager manager;
    private LiteServer httpServer;
    public static final String TAG = "LiteListener";

    //static initializer to ensure that cblite:// URLs are handled properly
    {
        URLStreamHandlerFactory.registerSelfIgnoreError();
    }

    /**
     * LiteListener constructor
     *
     * @param manager the Manager instance
     * @param port the suggested port to use. If 0 is specified then the next available port will be picked. // https://github.com/couchbase/couchbase-lite-java-listener/issues/26
     */
    public LiteListener(Manager manager, int port) {
        // Needed to support https://github.com/couchbase/couchbase-lite-java-listener/issues/24 and
        // https://github.com/couchbase/couchbase-lite-java-core/issues/44
        this(manager, port, new Properties(), null);
    }

    /**
     * Created to enable https://github.com/couchbase/couchbase-lite-java-listener/issues/24 and
     * https://github.com/couchbase/couchbase-lite-java-core/issues/44
     * @param manager 
     * @param port the port to use.  If 0 is chosen then the next free port will be used, the port
							chosen can be discovered via getSocketStatus() - https://github.com/couchbase/couchbase-lite-java-listener/issues/26
     * @param tjwsProperties    properties to be passed into the TJWS server instance. Note that if
     *                          port is set in these properties they will be overwritten by suggestedPort
     * @param requestAuthorization Specifies the authorization policy, can be NULL
     */
    public LiteListener(Manager manager, int port, Properties tjwsProperties, RequestAuthorization requestAuthorization) {
        this.manager = manager;
        tjwsProperties.put(Serve.ARG_PORT, port); // https://github.com/couchbase/couchbase-lite-java-listener/issues/24  & https://github.com/couchbase/couchbase-lite-java-listener/issues/26
        this.httpServer = new LiteServer(manager, tjwsProperties, requestAuthorization);
    }

    @Override
    public void run() {
        // Removed reference to this.serverStatus because of https://github.com/couchbase/couchbase-lite-java-listener/issues/23
        httpServer.serve();
    }

    // https://github.com/couchbase/couchbase-lite-java-listener/issues/42
    public int start() {
        thread = new Thread(this);
        thread.start();
        return 0;
    }

    public void stop() {
        httpServer.notifyStop();
    }

    public void onServerThread(Runnable r) {
        ScheduledExecutorService workExecutor = manager.getWorkExecutor();
        workExecutor.submit(r);
    }

    // https://github.com/couchbase/couchbase-lite-java-listener/issues/25
    public SocketStatus getSocketStatus() {
        return this.httpServer.getSocketStatus();
    }
}
