package litresbot.util;

import java.io.UnsupportedEncodingException;

public final class Convert {

    private static final char[] hexChars = { '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' };

    private Convert() {} //never

    public static byte[] parseHexString(String hex) {
        if (hex == null) {
            return null;
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int char1 = hex.charAt(i * 2);
            char1 = char1 > 0x60 ? char1 - 0x57 : char1 - 0x30;
            int char2 = hex.charAt(i * 2 + 1);
            char2 = char2 > 0x60 ? char2 - 0x57 : char2 - 0x30;
            if (char1 < 0 || char2 < 0 || char1 > 15 || char2 > 15) {
                throw new NumberFormatException("Invalid hex number: " + hex);
            }
            bytes[i] = (byte)((char1 << 4) + char2);
        }
        return bytes;
    }

    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            chars[i * 2] = hexChars[((bytes[i] >> 4) & 0xF)];
            chars[i * 2 + 1] = hexChars[(bytes[i] & 0xF)];
        }
        return String.valueOf(chars);
    }

    public static long parseLong(Object o) {
        if (o == null) {
            return 0;
        } else if (o instanceof Long) {
            return ((Long)o);
        } else if (o instanceof String) {
            return Long.parseLong((String)o);
        } else {
            throw new IllegalArgumentException("Not a long: " + o);
        }
    }
    
    public static Long zeroToNull(long l) {
        return l == 0 ? null : l;
    }

    public static long nullToZero(Long l) {
        return l == null ? 0 : l;
    }

    public static int nullToZero(Integer i) {
        return i == null ? 0 : i;
    }

    public static String emptyToNull(String s) {
        return s == null || s.length() == 0 ? null : s;
    }

    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public static byte[] emptyToNull(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        for (byte b : bytes) {
            if (b != 0) {
                return bytes;
            }
        }
        return null;
    }

    public static byte[] toBytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static String toString(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8").trim().intern();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static Boolean parseBoolean(String value) {
      if (Boolean.TRUE.toString().equals(value)) {
          return true;
      } else if (Boolean.FALSE.toString().equals(value)) {
          return false;
      }
      throw new IllegalArgumentException("Not a boolean: " + value);
    }
    
    public static String fillPrepend(String text, int size) {
      StringBuilder builder = new StringBuilder();
      while ((builder.length() + text.length()) < size) {
          builder.append('0');
      }
      return builder.toString() + text;
    }
    
    public static String fillAppend(String text, int size) {
      StringBuilder builder = new StringBuilder(text);
      while (builder.length() < size) {
          builder.append('0');
      }
      return builder.toString();
    }
    
}
