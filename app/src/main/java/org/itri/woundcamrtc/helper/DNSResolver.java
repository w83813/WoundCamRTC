package org.itri.woundcamrtc.helper;


import java.net.InetAddress;
import java.net.URI;

public class DNSResolver implements Runnable {
    private String domain = "";
    private InetAddress inetAddr = null;

    public DNSResolver(String domain) {
        this.domain = getDomainName(domain);
    }

    public void run() {
        try {
            InetAddress addr = InetAddress.getByName(this.domain);
            set(addr);
        } catch (Exception e) {
        }
    }

    public String getDomainName(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (Exception e) {
            return "";
        }
    }

    public String getDomainName() {
        return domain;
    }

    public synchronized void set(InetAddress inetAddr) {
        this.inetAddr = inetAddr;
    }

    public synchronized InetAddress get() {
        return inetAddr;
    }
}