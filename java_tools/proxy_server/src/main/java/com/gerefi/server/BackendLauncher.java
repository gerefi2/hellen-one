package com.gerefi.server;

import com.gerefi.proxy.client.LocalApplicationProxy;
import com.gerefi.tools.online.HttpUtil;
import com.gerefi.tools.online.ProxyClient;

import java.io.IOException;

public class BackendLauncher {
    public static void main(String[] args) throws IOException {
        /* todo
        gerefiSSLContext.setupCertificates(new File("keystore.jks"), System.getProperty("gerefi_KEYSTORE_PASSWORD"));
         */

        UserDetailsResolver userDetailsFunction = new JsonUserDetailsResolver();

        Backend backend = new Backend(userDetailsFunction, HttpUtil.PROXY_JSON_API_HTTP_PORT);
        backend.runApplicationConnector(LocalApplicationProxy.SERVER_PORT_FOR_APPLICATIONS, parameter -> {
        });
        backend.runControllerConnector(ProxyClient.SERVER_PORT_FOR_CONTROLLERS, parameter -> {
        });
    }
}
