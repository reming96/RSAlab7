package lab7.data.fxlab7;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

import static java.lang.System.out;

public class RsaEncryptor {
    public RsaEncryptor(int keyCapacity) {
        bitLength = keyCapacity;
    }

    private final Scanner sc = new Scanner(System.in);

    // public String inputPath   = "./src/input.txt";
    public String outputPath  = "./output.txt";
    public String privateKeys = "./private.txt";
    public String publicKeys  = "./public.txt";
    String publicK;
    String privateK;

    public String p, // Простое число
            q, // Простое число
            n, // p * q
            e, // Простое число
            d; // Экспонента для закрытого ключа
    public int bitLength;

    public void GenKeys(String namePublic, String namePrivate) throws IOException {
        p = GetPrime(bitLength, 100).toString();
        q = GetPrime(bitLength, 100).toString();
        n = BigInt(p).multiply(BigInt(q)).toString();
        e = GetPrime(bitLength, 100).toString();
        d = Euclid(BigInt(p).subtract(BigInt(1)).multiply(BigInt(q).subtract(BigInt(1))), BigInt(e)).toString();

        File fPublic = new File("generatedFiles/publicKeys/"+namePublic+".txt");
        if (!fPublic.exists()) { fPublic.createNewFile(); }
        FileWriter fw = new FileWriter(fPublic.getPath());
        fw.write(e + "\n" + n);
        fw.close();

        File fPrivate = new File("generatedFiles/privateKeys/"+namePrivate+".txt");
        if (!fPrivate.exists()) { fPrivate.createNewFile(); }
        fw = new FileWriter(fPrivate.getPath());
        fw.write(
                String.format(
                        "%s\n%s\n%s\n%s\n%s\n%s",
                        d, p, q,
                        BigInt(d).mod(BigInt(p).subtract(BigInt(1))).toString(),
                        BigInt(d).mod(BigInt(q).subtract(BigInt(1))).toString(),
                        BigInt(q).modPow(BigInt(-1), BigInt(p)).toString()
                )
        );
        fw.close();
    }

    public void Encrypt(String text) throws IOException {
        ArrayList<String> pubKeys = ArrayFromFile(new File(publicKeys));
        e = pubKeys.get(0);
        n = pubKeys.get(1);

        var decimalText = BinToDec(BytesToBin(StrToBytes(text)));
        StringBuilder encryptedText = new StringBuilder();
        for (Integer value: decimalText) {
            encryptedText.append(BigInt(value).modPow(new BigInteger(e), new BigInteger(n)).toString()).append(" ");
        }
        WriteIntoFile("0\ntext\nrsaEncryption\n" + encryptedText.toString(), outputPath);
    }
    public void Decrypt(String text) {
        ArrayList<String> privkeys = ArrayFromFile(new File(privateKeys));
        p = privkeys.get(1);
        q = privkeys.get(2);
        n = BigInt(p).multiply(BigInt(q)).toString();
        d = privkeys.get(0);
        StringBuilder decryptedText = new StringBuilder();
        var decimalText = StrToDec(text.substring(21));
        ArrayList<Integer> decrypted = new ArrayList<>();
        for (var val: decimalText) { decrypted.add(val.modPow(BigInt(d), BigInt(n)).intValue()); }
        for (Integer Int: decrypted) { decryptedText.append(BinToStr(DecToBin(Int))); }
        WriteIntoFile(decryptedText.toString(), outputPath);
    }
    public static boolean MillerRabin(BigInteger num, int rounds){
        if (num.equals(BigInt(2)) || num.equals(BigInt(3)))
            return true;
        if ((num.compareTo(BigInt(2)) < 0) || num.mod(BigInt(2)).equals(BigInt(0)))
            return false;
        BigInteger t = num.subtract(BigInt(1));
        int s = 0;
        while (t.mod(BigInt(2)).equals(BigInt(0))) {
            t = t.divide(BigInt(2)); s += 1;
        }
        for (int i = 0; i < rounds; i++)
        {
            BigInteger x = millerRabinRandom(num).modPow(t, num);
            if (x.equals(BigInt(1)) || x.equals(num.subtract(BigInt(1)))) continue;
            for (int r = 1; r < s; r++)
            {
                x = x.modPow( BigInt(2), num);
                if (x.equals(BigInt(1))) return false;
                if (x.equals(num.subtract(BigInt(1)))) break;
            }
            if (!x.equals(num.subtract(BigInt(1)))) return false;
        }
        return true;
    }


    public static BigInteger BigInt(int value) {
        return BigInteger.valueOf(value);
    }
    public static BigInteger BigInt(String value) { return new BigInteger(value); }
    public static ArrayList<String> ArrayFromFile(File file) {
        ArrayList<String> output = new ArrayList<>();
        try (FileReader fr = new FileReader(file)) {
            BufferedReader buffReader = new BufferedReader(fr);
            String line = buffReader.readLine();
            while (line != null) {
                output.add(line);
                line = buffReader.readLine();
            }
        } catch (IOException ignored) {}
        return output;
    }
    public static void WriteIntoFile(String str, String path) {
        try (FileWriter fw = new FileWriter(path)) {
            fw.write(str);
        } catch (IOException ignored) { }
    }
    public static int rnd(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }
    public static BigInteger millerRabinRandom(BigInteger num) {
        BigInteger a;
        do {
            a = new BigInteger(num.bitLength(), new SecureRandom());
        } while (a.compareTo(BigInt(2)) < 0 || a.compareTo(num.subtract(BigInt(2))) > 0);
        return a;
    }
    public static BigInteger GetPrime(Integer capacity, Integer t) {
        BigInteger prime;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < capacity; i++) { sb.append(rnd(0, 1)); }
            // Первый и последний бит = 1
            sb.setCharAt(sb.length() - 1, '1'); sb.setCharAt(0, '1');
            prime = new BigInteger(sb.toString(), 2);
        } while(!MillerRabin(prime, t));
        return prime;
    }
    public static BigInteger Euclid(BigInteger x, BigInteger e) {
        BigInteger a;
        BigInteger b = new BigInteger("0");
        while (!x.equals(BigInt(1))) {
            BigInteger y = e;
            if (y.compareTo(x) > 0)
                return null;
            BigInteger a1 = BigInteger.ZERO, a2 = BigInteger.ONE;
            BigInteger b1 = BigInteger.ONE, b2 = BigInteger.ZERO;
            while (y.intValue() != 0) {
                BigInteger q = x.divide(y);
                BigInteger r = x.subtract(q.multiply(y));
                a = a2.subtract(q.multiply(a1));
                b = b2.subtract(q.multiply(b1));
                x = y; y = r;
                a2 = a1; a1 = a;
                b2 = b1; b1 = b;
            }
            b = b2;
        }
        return b;
    }
    public static String DecToBin(Integer input) {
        return Integer.toBinaryString(input);
    }
    public static ArrayList<Integer> BinToDec(ArrayList<String> input) {
        ArrayList<Integer> array = new ArrayList<>();
        for (String str:input) { array.add(Integer.parseInt(str, 2)); }
        return array;
    }
    public static ArrayList<byte[]> StrToBytes(String text){
        ArrayList<byte[]> byteArrays = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            byteArrays.add(String.valueOf(text.charAt(i)).getBytes(StandardCharsets.UTF_8));
        }
        return byteArrays;
    }
    public static ArrayList<BigInteger> StrToDec(String text){
        ArrayList<BigInteger> integerArrays = new ArrayList<>();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i)!= ' ') { temp.append(text.charAt(i)); }
            else {
                integerArrays.add(BigInt(temp.toString()));
                temp = new StringBuilder();
            }
        }
        return integerArrays;
    }
    public static String BinToStr(String binary) {
        return new String(ByteBuffer.allocate(4).putInt(Integer.parseInt(binary, 2)).array(), StandardCharsets.UTF_8);
    }
    public static ArrayList<String> BytesToBin(ArrayList<byte[]> input) {
        ArrayList<String> binaryArray = new ArrayList<>();
        for (byte[] bytes : input) {
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                int val = b;
                for (int j = 0; j < 8; j++) {
                    result.append((val & 128) == 0 ? 0 : 1);
                    val <<= 1;
                }
            }
            binaryArray.add(result.toString());
        }
        return binaryArray;
    }
}
