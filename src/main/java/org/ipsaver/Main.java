package org.ipsaver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.file.Files;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
A program that checks for the computer's external ip.
It will update a file in the user's onedrive with the most updated ip
the last check time
and the last time the ip has updated
 */
public class Main {

    public static final int ONE_HOUR = 3600000;

    public static void main(String[] args) throws IOException {
        File statusFile = getStatusFile();
        IPStatus oldIpStatus = getIpStatus(statusFile);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
                    try {
                        InetAddress[] newAddresses = new InetAddress[2];
                        checkExternalIp(newAddresses);
                        writeNewStatusFile(newAddresses,
                                oldIpStatus,
                                statusFile);
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                },
                getInitialDelay(oldIpStatus.getLastCheckTime()),
                ONE_HOUR,
                TimeUnit.MILLISECONDS);
    }


    //Gets the last check time and returns the time to sleep until the next check
    //The time to sleep is 1 hour minus the time passed since the last check
    //If the time passed is more than 1 hour - returns 0
    //If the last check time is null - returns 0
    private static int getInitialDelay(Date lastCheckTime) {
        if (lastCheckTime == null) {
            return 0;
        }
        Date now = new Date();
        long timePassed = now.getTime() - lastCheckTime.getTime();
        long timeToSleep = ONE_HOUR - timePassed;
        return timeToSleep > 0 ? (int) timeToSleep : 0;
    }

    private static void writeNewStatusFile(InetAddress[] newAddresses,
                                           IPStatus oldIpStatus,
                                           File statusFile) {
        Date checkDate = new Date();
        Date updateDate = getUpdateDate(newAddresses, oldIpStatus, checkDate);

        IPStatus newIpStatus = new IPStatus((Inet4Address) newAddresses[0],
                (Inet6Address) newAddresses[1],
                checkDate,
                updateDate);
        newIpStatus.toJson();

        //Overwrites the file with the new ip status and data
        try {
            Files.write(statusFile.toPath(), newIpStatus.toJson().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Date getUpdateDate(InetAddress[] newAddresses, IPStatus oldIpStatus, Date checkDate) {
        Date updateDate;
        Inet4Address oldInet4Address = oldIpStatus.getIpv4();
        Inet6Address oldInet6Address = oldIpStatus.getIpv6();
        if (
                (oldInet4Address == null && newAddresses[0] != null) ||
                        (oldInet4Address != null && !oldInet4Address.equals(newAddresses[0])) ||
                        (oldInet6Address == null && newAddresses[1] != null) ||
                        (oldInet6Address != null && !oldInet6Address.equals(newAddresses[1]))
        ) {
            updateDate = checkDate;
        } else {
            updateDate = oldIpStatus.getLastUpdateTime();
        }
        return updateDate;
    }

    //Gets the external ipv4 and ipv6 of this machine and returns them.
    private static void checkExternalIp(InetAddress[] addresses) throws IOException, URISyntaxException {

        URL urlIpv4 = new URI("https://api.ipify.org").toURL();
        HttpURLConnection connIpv4 = (HttpURLConnection) urlIpv4.openConnection();
        connIpv4.setRequestMethod("GET");

        BufferedReader readerIpv4 = new BufferedReader(new InputStreamReader(connIpv4.getInputStream()));
        String ipv4String = readerIpv4.readLine();

        readerIpv4.close();
        connIpv4.disconnect();

        addresses[0] = InetAddress.getByName(ipv4String);

        URL urlIpv6 = new URI("https://api6.ipify.org").toURL();
        HttpURLConnection connIpv6 = (HttpURLConnection) urlIpv6.openConnection();
        connIpv6.setRequestMethod("GET");

        BufferedReader readerIpv6 = new BufferedReader(new InputStreamReader(connIpv6.getInputStream()));
        String ipv6String = readerIpv6.readLine();

        addresses[1] = InetAddress.getByName(ipv6String);

        readerIpv6.close();
        connIpv6.disconnect();
    }

    //Reads the file and converts it to an instance ot IPStatus
    private static IPStatus getIpStatus(File statusFile) throws IOException {
        String json = new String(Files.readAllBytes(statusFile.toPath()));
        IPStatus result;
        try {
            if (!json.isEmpty()) {
                result = IPStatus.fromJson(json);
            } else {
                result = new IPStatus(null, null, null, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = new IPStatus(null, null, null, null);
        }
        return result;
    }

    //Checks if a file named "ipStatus.txt" if found in the onedrive directory.
    //if it does not exist - tries to create one.
    //returns the file
    private static File getStatusFile() throws IOException {
        String oneDrivePath = System.getenv().get("OneDrive");
        String statusFilePath = oneDrivePath + File.separator + "ipStatus.txt";
        File statusFile = new File(statusFilePath);
        if (!statusFile.exists()) {
            statusFile.createNewFile();
        }
        return statusFile;
    }
}