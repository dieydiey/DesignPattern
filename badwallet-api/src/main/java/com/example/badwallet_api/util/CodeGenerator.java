package com.example.badwallet_api.util;

public class CodeGenerator {
    private static int counter = 1;

    public static synchronized String generateWalletCode() {
        return String.format("WLT-%07d", counter++);
    }
}