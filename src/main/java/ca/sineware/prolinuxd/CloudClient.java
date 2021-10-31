package ca.sineware.prolinuxd;

import ca.sineware.prolinuxd.ws.Action;
import ca.sineware.prolinuxd.ws.payloads.ErrorPayload;
import ca.sineware.prolinuxd.ws.payloads.hello.HelloAckPayload;
import ca.sineware.prolinuxd.ws.payloads.hello.HelloPayload;
import ca.sineware.prolinuxd.ws.payloads.unsafe_exec_cmd.StreamTerminalPayload;
import ca.sineware.prolinuxd.ws.payloads.unsafe_exec_cmd.UnsafeExecCmdPayload;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;

@Slf4j
public class CloudClient extends WebSocketClient {
    Gson gson = new Gson();

    HelloAckPayload self;

    public CloudClient(URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft);
    }
    public CloudClient(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("new connection opened");
        send("{\"action\":\"ping\",\"payload\":{}}");

        // Authenticate
        Action<HelloPayload> helloAction = new Action<>();
        HelloPayload helloPayload = new HelloPayload();
        helloPayload.type = "device";
        helloPayload.token = System.getenv("SINEWARE_CLOUD_TOKEN");

        helloAction.action = "hello";
        helloAction.payload = helloPayload;

        String jsonMsg = gson.toJson(helloAction);
        System.out.println(jsonMsg);
        send(jsonMsg);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received message: " + message);
        JsonParser parser = new JsonParser();
        JsonObject msgJsonObj = parser.parse(message).getAsJsonObject();

        System.out.println(msgJsonObj.get("action"));
        switch (msgJsonObj.get("action").getAsString()) {
            case "ping-ack" -> {
                Type actionType = new TypeToken<Action<String>>() {}.getType();
                Action<String> msg = gson.fromJson(message, actionType);

                System.out.println(msg.payload);
            }

            case "hello-ack" -> {
                Type actionType = new TypeToken<Action<HelloAckPayload>>() {}.getType();
                Action<HelloAckPayload> msg = gson.fromJson(message, actionType);
                this.self = msg.payload;
                log.info("Successfully authenticated with WS Gateway!");

            }

            case "unsafe-exec-cmd" -> {
                log.info("About to unsafely execute a command");
                Type actionType = new TypeToken<Action<UnsafeExecCmdPayload>>() {}.getType();
                Action<UnsafeExecCmdPayload> msg = gson.fromJson(message, actionType);
                try {
                    Runtime rt = Runtime.getRuntime();
                    Process pr = rt.exec(msg.payload.cmd);
                    BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

                    String line = null;

                    while((line=input.readLine()) != null) {
                        System.out.println(line);

                        Action<StreamTerminalPayload> streamAction = new Action<>();
                        StreamTerminalPayload streamPayload = new StreamTerminalPayload();
                        streamPayload.userID = msg.payload.requestUserID;
                        streamPayload.output = line;

                        streamAction.action = "stream-terminal";
                        streamAction.payload = streamPayload;

                        String jsonMsg = gson.toJson(streamAction);
                        System.out.println(jsonMsg);
                        send(jsonMsg);
                    }

                    int exitVal = pr.waitFor();
                    System.out.println("Exited with error code "+exitVal);
                } catch (Exception e) {

                }
            }

            case "error" -> {
                Type actionType = new TypeToken<Action<ErrorPayload>>() {}.getType();
                Action<ErrorPayload> error = gson.fromJson(message, actionType);

                log.error("Received error from gateway: " + error.payload.message);
            }

            default -> {
                log.error("Unknown Action: " + msgJsonObj.get("action").getAsString());
            }
        }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("an error occurred:" + ex);
    }
}
