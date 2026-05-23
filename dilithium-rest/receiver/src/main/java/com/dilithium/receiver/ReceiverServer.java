package com.dilithium.receiver;

import io.javalin.Javalin;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Base64;
import java.util.Map;

public class ReceiverServer {

    public static void main(String[] args) {

        Javalin app =
            Javalin.create();

        app.post("/verify", ctx -> {

            VerifyRequest req =
                ctx.bodyAsClass(
                    VerifyRequest.class
                );

            Path msg =
                Files.createTempFile(
                    "msg",
                    ".txt"
                );

            Path sig =
                Files.createTempFile(
                    "sig",
                    ".bin"
                );

            Path pub =
                Files.createTempFile(
                    "pub",
                    ".pem"
                );

            Files.writeString(
                msg,
                req.message
            );

            Files.write(
                sig,
                Base64.getDecoder()
                    .decode(req.signature)
            );

            Files.writeString(
                pub,
                req.publicKey
            );
            // Replace with your own path to openssl-3.5
            ProcessBuilder pb =
                new ProcessBuilder(
                    "/home/mansi/openssl-3.5/bin/openssl",
                    "pkeyutl",
                    "-verify",
                    "-pubin",
                    "-inkey",
                    pub.toString(),
                    "-sigfile",
                    sig.toString(),
                    "-rawin",
                    "-in",
                    msg.toString()
                );

            Process p = pb.start();

            int exit =
                p.waitFor();

            boolean verified =
                exit == 0;

            ctx.json(
                Map.of(
                    "verified",
                    verified,
                    "message",
                    req.message
                )
            );
        });

        app.start(9000);

        System.out.println(
            "Receiver running on port 9000"
        );
    }
}
