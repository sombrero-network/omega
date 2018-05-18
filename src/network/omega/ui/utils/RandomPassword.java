package network.omega.ui.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;

public class RandomPassword {
    public static String newPassword(int randomStrLength) {
        char[] possibleCharacters =
                (new String("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?")).toCharArray();
        String randomStr = RandomStringUtils.random(randomStrLength, 0, possibleCharacters.length - 1, false, false, possibleCharacters, new SecureRandom());
        return randomStr;
    }
}
