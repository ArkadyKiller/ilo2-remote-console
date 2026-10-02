/*
<applet code='ParamApplet' width='200' height='200'>
<param name='param' value='foo'>
</applet>
*/


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.security.Security;
import java.util.*;
import java.util.List;

import com.hp.ilo2.remcons.remcons;

/*


Set-Cookie: hp-iLO-Login=; Expires=Sun, 01 Jan 1990 12:00:00 GMT
Set-Cookie: hp-iLO-Session=00000005:::LMQJVGLGKQGMIAAEGQHZJUORCOBVQOUZIEXNVTUO; Path=/; Secure



var sessionkey="LMQJVGLGKQGMIAAEGQHZJUORCOBVQOUZIEXNVTUO";
var sessionindex="00000005";


*/

public class Main {

    private static String splitSafe(String text, String delimiter, String endDelim) {
        try {
            String[] parts = text.split(delimiter, 2);
            if (parts.length < 2) return "";
            String[] after = parts[1].split(endDelim, 2);
            return after[0];
        } catch (Exception e) {
            return "";
        }
    }

    private static final String USAGE_TEXT = "Usage: \n" +
            "- ILO2RemCon.jar <Hostname or IP> <Username> <Password>\n" +
            "- ILO2RemCon.jar -c <Path to config.properties>";

    private static final String DEFAULT_CONFIG_PATH = "config.properties";
    private static final String COOKIE_FILE = "data.cook";

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko";


    private static String username = "";
    private static String password = "";
    private static String hostname = "";

    public static void setHostname(String hostname) {
        Main.hostname = hostname;
        Main.loginURL = "https://" + hostname + "/login.htm";
    }

    private static String loginURL = "";

    private static String sessionKey = "";
    private static String sessionIndex = "";
    private static String supercookie = "";

    private final static CookieManager cookieManager = new CookieManager();


    private static void Stage1() throws Exception {
        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
        System.setProperty("https.protocols", "TLSv1");
        System.setProperty("javax.net.debug", "");
        Security.setProperty("jdk.tls.disabledAlgorithms", "");
        URL obj = new URL(loginURL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setConnectTimeout(30000);
        con.setReadTimeout(90000);

        // optional default is GET
        //con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Referer", loginURL);
        con.setRequestProperty("Host", hostname);
        con.setRequestProperty("Accept-Language", "de-DE");
        con.setRequestProperty("Cookie", "hp-iLO-Login=");


        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String res = response.toString();
        sessionKey = res.split("var sessionkey=\"")[1].split("\";")[0];
        sessionIndex = res.split("var sessionindex=\"")[1].split("\";")[0];
        System.out.println("Session key: " + sessionKey);
        System.out.println("Session  ID: " + sessionIndex);
    }


    private static void Stage2() throws Exception {
        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();

        URL obj = new URL("https://" + hostname + "/index.htm");

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setConnectTimeout(30000);
        con.setReadTimeout(90000);

        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Referer", loginURL);
        con.setRequestProperty("Host", hostname);
        con.setRequestProperty("Accept-Language", "de-DE");
        //Cookie:

        Base64.Encoder enc2 = Base64.getMimeEncoder(); //Authenticate
        String cookieVal = String.format("hp-iLO-Login=%s:%s:%s:%s",
                sessionIndex,
                enc2.encodeToString(username.getBytes()),
                enc2.encodeToString(password.getBytes()),
                sessionKey
        );
        con.setRequestProperty("Cookie", cookieVal);


        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));

        //noinspection StatementWithEmptyBody
        while (in.readLine() != null) {
        } // discard
        in.close();

        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        PrintWriter writer = new PrintWriter(COOKIE_FILE, "UTF-8");
        for (HttpCookie cookie : cookies) {
            System.out.format("Session cookie: %s: %s\n", cookie.getDomain(), cookie);
            writer.println(cookie.toString().replace("\"", ""));
        }
        writer.close();

    }


    private final static HashMap<String, String> hmap = new HashMap<>();

    private static void Stage3() throws Exception {
        // https://" + hostname + "/drc2fram.htm?restart=1
        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
        String url = "https://" + hostname + "/drc2fram.htm?restart=1";
        URL obj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setConnectTimeout(30000);
        con.setReadTimeout(90000);

        // optional default is GET
        //con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Referer", loginURL);
        con.setRequestProperty("Host", hostname);
        con.setRequestProperty("Accept-Language", "de-DE");
        if (!supercookie.equals("")) {
            con.setRequestProperty("Cookie", supercookie);
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String res = response.toString();

        hmap.put("INFO0", splitSafe(res, "info0=\"", "\";"));
        hmap.put("INFO1", splitSafe(res, "info1=\"", "\";"));
        hmap.put("INFO3", splitSafe(res, "info3=\"", "\";"));
        hmap.put("INFO6", splitSafe(res, "info6=\"", "\";"));
        hmap.put("INFO7", splitSafe(res, "info7=", ";"));
        hmap.put("INFO8", splitSafe(res, "info8=\"", "\";"));

        hmap.put("INFOA", splitSafe(res, "infoa=\"", "\";"));
        hmap.put("INFOB", splitSafe(res, "infob=\"", "\";"));
        hmap.put("INFOC", splitSafe(res, "infoc=\"", "\";"));
        hmap.put("INFOD", splitSafe(res, "infod=\"", "\";"));

        hmap.put("INFOM", splitSafe(res, "infom=", ";"));
        hmap.put("INFOMM", splitSafe(res, "infomm=", ";"));

        hmap.put("INFON", splitSafe(res, "infon=", ";"));
        hmap.put("INFOO", splitSafe(res, "infoo=\"", "\";"));

        hmap.put("CABBASE", splitSafe(res, "<PARAM NAME=CABBASE VALUE=", ">\""));

        System.out.println("CABBASE = " + hmap.get("CABBASE"));
    }


    public static boolean isValid(String cookie) throws Exception {
        CookieHandler.setDefault(cookieManager);
        String url = "https://" + hostname + "/ie_index.htm";
        URL obj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        //con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Referer", loginURL);
        con.setRequestProperty("Host", hostname);
        con.setRequestProperty("Accept-Language", "de-DE");
        con.setRequestProperty("Cookie", cookie);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String res = response.toString();

        return !(res.contains("Login Delay") || res.contains("Integrated Lights-Out 2 Login"));
    }


    private static void setupLogging(String[] args) {
        try {
            java.io.File f = new java.io.File("D:\\AI\\Temp\\ilo2_run.log");
            java.io.PrintStream file = new java.io.PrintStream(new java.io.FileOutputStream(f, false), true, "UTF-8");
            java.io.PrintStream oo = System.out;
            java.io.PrintStream oe = System.err;
            System.setOut(new java.io.PrintStream(new TeeOutputStream(oo, file), true));
            System.setErr(new java.io.PrintStream(new TeeOutputStream(oe, file), true));
            System.out.println("=== iLO2 start " + new java.util.Date() + " args=" + java.util.Arrays.toString(args));
        } catch (Exception ignored) {
        }
    }

    public static void main(String[] args) {
        setupLogging(args);
        Optional<String> configPath = Optional.empty();

        switch (args.length) {
            case 0:
                // <no args> -> GUI form (address host:port, login, password)
                LoginDialog dlg = new LoginDialog();
                dlg.setVisible(true);
                if (!dlg.isOk()) {
                    return;
                }
                setHostname(dlg.server);
                username = dlg.user;
                password = dlg.password;
                break;
            case 2:
                // -c <path>
                if (args[0].equals("-c")) {
                    configPath = Optional.of(args[1]);
                } else {
                    System.out.println(USAGE_TEXT);
                }
                break;
            case 3:
                // <Hostname or IP> <Username> <Password>
                setHostname(args[0]);
                username = args[1];
                password = args[2];
                break;
            default:
                System.out.println(USAGE_TEXT);
                return;
        }

        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
        CookieHandler.setDefault(cookieManager);

        if (configPath.isPresent()) {
            try (FileInputStream fis = new FileInputStream(configPath.get())) {
                Properties p = new Properties();
                p.load(fis);

                setHostname(p.getProperty("hostname"));
                username = p.getProperty("username");
                password = p.getProperty("password");
            } catch (Exception e) {
                System.err.println("Error in reading/parsing config file!");
                e.printStackTrace();
                return;
            }
        }

        try {
            try (BufferedReader br = new BufferedReader(new FileReader("data.cook"))) {
                System.out.println("Found datastore");
                String line;
                String lastline = "";
                while ((line = br.readLine()) != null) {
                    cookieManager.getCookieStore().add(new URI("https://" + hostname), new HttpCookie(line.split("=")[0], line.split("=")[1]));
                    lastline = line;
                }

                if (!isValid(lastline)) {
                    System.out.println("Datastore not valid, requesting Cookie");
                    Stage1();
                    Stage2();
                } else {
                    supercookie = lastline;
                }
            } catch (FileNotFoundException e) {
                System.out.println("Couldn't find datastore, requesting Cookie");
                Stage1();
                Stage2();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Stage3();
            System.out.println("HMAP=" + hmap);
            //hmap.put("IPADDR", hostname);
            //hmap.put("DEBUG", "suckAdIck");

            remcons rmc = new remcons(hmap);
            rmc.SetHost(hostname.split(":")[0]);

            JFrame jf = new JFrame();
            Container c = jf.getContentPane();
            jf.setBounds(0, 0, 1070, 880);
            jf.setTitle(hostname + " - iLO2");
            jf.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            jf.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        rmc.stop();
                    } catch (Exception ignored) {}
                    try {
                        HttpURLConnection lc = (HttpURLConnection) new URL(
                                "https://" + hostname + "/logout.htm").openConnection();
                        lc.setConnectTimeout(10000);
                        lc.setReadTimeout(10000);
                        lc.getInputStream().close();
                    } catch (Exception ignored) {}
                    System.exit(0);
                }
            });
            c.add(rmc);
            rmc.init();
            jf.setLocation(0, 0);
            jf.setAlwaysOnTop(true);
            jf.setVisible(true);
            jf.toFront();
            jf.requestFocus();
            jf.validate();
            rmc.start();

            final JFrame fjf = jf;
            new Thread(() -> {
                int lastW = 0, lastH = 0;
                for (int i = 0; i < 240; i++) {
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    int w = com.hp.ilo2.remcons.dvcwin.LAST_W;
                    int h = com.hp.ilo2.remcons.dvcwin.LAST_H;
                    if (w >= 640 && h >= 480 && (w != lastW || h != lastH)) {
                        lastW = w;
                        lastH = h;
                        fjf.setSize(w + 16, h + 70);
                        fjf.validate();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
