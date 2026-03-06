package com.banking.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class AccountUtils {
    private static final SecureRandom random = new SecureRandom();

    public String generateAccountNumber() {
        return String.valueOf(100000 + random.nextInt(900000));
    }

    public String generateTempPassword() {
        String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String lower = "abcdefghjkmnpqrstuvwxyz";
        String digits = "23456789";
        String special = "@#$!";
        String all = upper + lower + digits + special;
        StringBuilder sb = new StringBuilder();
        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(special.charAt(random.nextInt(special.length())));
        for (int i = 4; i < 10; i++) sb.append(all.charAt(random.nextInt(all.length())));
        // Shuffle
        char[] arr = sb.toString().toCharArray();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
        return new String(arr);
    }
}
