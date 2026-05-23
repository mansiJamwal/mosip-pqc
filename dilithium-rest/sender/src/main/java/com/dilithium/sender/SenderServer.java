package com.dilithium.sender;

import io.javalin.Javalin;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Base64;
import java.util.Map;

import org.xipki.pkcs11.wrapper.*;

public class SenderServer {

    static Session session;

    static long privateKey;

    static String publicKeyPem;

    public static void main(String[] args) throws Exception {
        // Replace with your own path to softhsm2 library
        String lib =
            "/home/mansi/Desktop/mosip/SoftHSMv2/src/lib/.libs/libsofthsm2.so";

        PKCS11Module module =
            PKCS11Module.getInstance(lib);

        module.initialize();

        Slot[] slots =
            module.getSlotList(true);

        Slot slot = slots[0];

        Token token = slot.getToken();

        session =
            token.openSession(true);

        String pin =
            System.getenv("SOFTHSM_PIN");

        if (pin == null) {
            throw new RuntimeException(
                "SOFTHSM_PIN not set"
            );
        }
        session.login(
            PKCS11Constants.CKU_USER,
            pin.toCharArray()
        );

        AttributeVector template =
            new AttributeVector();

        template.class_(
            PKCS11Constants.CKO_PRIVATE_KEY
        );

        session.findObjectsInit(template);

        long[] objects =
            session.findObjects(10);

        session.findObjectsFinal();

        privateKey = objects[0];

        publicKeyPem =
            Files.readString(
                Paths.get("public.pem")
            );

        Javalin app =
            Javalin.create();

        app.get("/sign", ctx -> {

            String message =
                ctx.queryParam("message");

            byte[] data =
                message.getBytes();

            Mechanism mechanism =
                new Mechanism(0x1D);

            session.signInit(
                mechanism,
                privateKey
            );

            byte[] signature =
                session.sign(data);

            String sigBase64 =
                Base64.getEncoder()
                    .encodeToString(signature);

            ctx.json(
                Map.of(
                    "message", message,
                    "signature", sigBase64,
                    "publicKey", publicKeyPem
                )
            );
        });

        app.start(8000);

        System.out.println(
            "Sender running on port 8000"
        );
    }
}