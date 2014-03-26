package com.couchbase.lite.listener;

// https://github.com/couchbase/couchbase-lite-java-listener/issues/27
public interface LiteAcceptor {
    public SocketStatus getSocketStatus();
}
