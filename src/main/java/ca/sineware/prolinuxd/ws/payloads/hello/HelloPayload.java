package ca.sineware.prolinuxd.ws.payloads.hello;

public class HelloPayload {
    public String type;
    public String token;
    public HelloPayloadInfo info = new HelloPayloadInfo();
}
class HelloPayloadInfo {
    String deviceName = "prolinuxd-java";
}
