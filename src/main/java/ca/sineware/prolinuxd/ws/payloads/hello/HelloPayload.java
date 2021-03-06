package ca.sineware.prolinuxd.ws.payloads.hello;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class HelloPayload {
    public String type;
    public String token;
    public HelloPayloadInfo info = new HelloPayloadInfo();
}

