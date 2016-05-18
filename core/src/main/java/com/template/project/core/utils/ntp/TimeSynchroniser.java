package com.template.project.core.utils.ntp;

import com.template.project.core.utils.LogMe;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpUtils;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;

public class TimeSynchroniser {

    private final String TAG = TimeSynchroniser.class.getSimpleName();

    public static final String NTP_SERVER = "asia.pool.ntp.org";

    private static TimeSynchroniser timeSync;

    private long systemTime = 0, offset = 0, broadCastDelay = 0;

    public static TimeSynchroniser getInstance() {
        if (timeSync == null) {
            timeSync = new TimeSynchroniser();
        }

        return timeSync;
    }

    /** Get the system time, or the clock time of the device. */
    public long getSystemTime() {
        return System.currentTimeMillis();
    }

    /** Set the offset of system time from NTP Server */
    public void setClockOffset(long offset) {
        this.offset = offset;
    }

    /** Get the offset of the system time from NTP Server */
    public long getClockOffset() {
        return this.offset;
    }

    /**
     * Set the broadcast delay of the Player obtained from broadcastDelay
     * http request during fingerprint detection.
     */
    public void setBroadCastDelay(long broadCastDelay) {
        this.broadCastDelay = broadCastDelay;
    }

    /**
     * Return the broadcast delay obtained from broadcastDelay
     * http request during fingerprint detection.
     */
    public long getBroadCastDelay() {
        return this.broadCastDelay;
    }

    /**
     * Get the sychronized time with corresponding broadcastdelay.
     * This will be used for submission of answer.
     */
    public long getSynchronisedTime() {
        systemTime = getSystemTime();
        long synchronisedTime = systemTime + offset - broadCastDelay;
        return synchronisedTime;
    }

    /**
     * Sync the timestamp with NTP time based on the offset.
     * @param timestamp String value of timestamp to sync with NTP
     */
    public long syncTimeStampWithNtp(String timestamp) {
        long tsToSetOffset = Long.parseLong(timestamp);
        long synchronisedTime = tsToSetOffset + offset;
        return synchronisedTime;
    }

    /**
     * Sync the timestamp with NTP time based on the offset.
     * @param timestamp Value of timestamp to sync with NTP
     */
    public long syncTimeStampWithNtp(long timestamp) {
        long synchronisedTime = timestamp + offset;
        return synchronisedTime;
    }

    /** Get the synchronize time with NTP Server. */
    public long getNtpTime() {
        systemTime = System.currentTimeMillis();
        long ntpTime = systemTime + offset;
        return ntpTime;
    }

    private static final NumberFormat numberFormat = new java.text.DecimalFormat("0.00");

    /**
     * Get the time from Ntp Server
     * @param ntpServerAddress The NTP Server address. Ex. time-a.nist.gov
     * @return The offset of device time from Ntp Server time in millisecond
     */
    public String getNtpOffset(String ntpServerAddress) {
        NumberFormat numberFormat = new java.text.DecimalFormat("0.00");
        Long offsetValue;

        //initialize TimeInfo
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(30000);
        InetAddress hostAddr = null;
        TimeInfo info = null;
        try {
            hostAddr = InetAddress.getByName(ntpServerAddress);
            info = client.getTime(hostAddr);
        } catch (Exception e) {
            LogMe.e(TAG, "ERROR retrieving time!");
            return "ERROR";
        }

        NtpV3Packet message = info.getMessage();
        int stratum = message.getStratum();
        String refType;
        if (stratum <= 0) {
            refType = "(Unspecified or Unavailable)";
        } else if (stratum == 1) {
            refType = "(Primary Reference; e.g., GPS)"; // GPS, radio clock, etc.
        } else {
            refType = "(Secondary Reference; e.g. via NTP or SNTP)";
        }
        // stratum should be 0..15...
        LogMe.d(TAG, " Stratum: " + stratum + " " + refType);
        int version = message.getVersion();
        int li = message.getLeapIndicator();
        LogMe.d(TAG, " leap=" + li + ", version=" + version + ", precision=" + message.getPrecision());

        LogMe.d(TAG, " mode=" + message.getModeName() + " (" + message.getMode() + ")");
        int poll = message.getPoll();
        // poll value typically btwn MINPOLL (4) and MAXPOLL (14)
        LogMe.d(TAG, " poll: " + (poll <= 0 ? 1 : (int) Math.pow(2, poll))
                + " seconds" + " (2 ** " + poll + ")");
        double disp = message.getRootDispersionInMillisDouble();
        LogMe.d(TAG, " rootdelay=" + numberFormat.format(message.getRootDelayInMillisDouble())
                + ", rootdispersion(ms): " + numberFormat.format(disp));

        int refId   = message.getReferenceId();
        String refAddr = NtpUtils.getHostAddress(refId);
        String refName = null;
        if (refId != 0) {
            if (refAddr.equals("127.127.1.0")) {
                refName = "LOCAL"; // This is the ref address for the Local Clock
            } else if (stratum >= 2) {
                // If reference id has 127.127 prefix then it uses its own reference clock
                // defined in the form 127.127.clock-type.unit-num (e.g. 127.127.8.0 mode 5
                // for GENERIC DCF77 AM; see refclock.htm from the NTP software distribution.
                if (!refAddr.startsWith("127.127")) {
                    try {
                        InetAddress addr = InetAddress.getByName(refAddr);
                        String name = addr.getHostName();
                        if (name != null && !name.equals(refAddr)) {
                            refName = name;
                        }
                    } catch (UnknownHostException e) {
                        // some stratum-2 servers sync to ref clock device but fudge stratum level higher... (e.g. 2)
                        // ref not valid host maybe it's a reference clock name?
                        // otherwise just show the ref IP address.
                        refName = NtpUtils.getReferenceClock(message);
                        LogMe.e(TAG, "ERROR address loopback failed!");
                    }
                }
            } else if (version >= 3 && (stratum == 0 || stratum == 1)) {
                refName = NtpUtils.getReferenceClock(message);
                // refname usually have at least 3 characters (e.g. GPS, WWV, LCL, etc.)
            }
        }

        // Log details
        if (refName != null && refName.length() > 1) {
            refAddr += " (" + refName + ")";
        }

        LogMe.d(TAG, hostAddr.getHostName() + "/" + hostAddr.getHostAddress());
        LogMe.d(TAG, " Reference Identifier:\t" + refAddr);

        TimeStamp refNtpTime = message.getReferenceTimeStamp();
        LogMe.d(TAG, " Reference Timestamp:\t" + refNtpTime + "  " + refNtpTime.toDateString());

        // Originate Time is time request sent by client (t1)
        TimeStamp origNtpTime = message.getOriginateTimeStamp();
        LogMe.d(TAG, " Originate Timestamp:\t" + origNtpTime + "  " + origNtpTime.toDateString());

        // Receive Time is time request received by server (t2)
        TimeStamp rcvNtpTime = message.getReceiveTimeStamp();
        LogMe.d(TAG, " Receive Timestamp:\t" + rcvNtpTime + "  " + rcvNtpTime.toDateString());

        // Transmit time is time reply sent by server (t3)
        TimeStamp xmitNtpTime = message.getTransmitTimeStamp();
        LogMe.d(TAG, " Transmit Timestamp:\t" + xmitNtpTime + "  " + xmitNtpTime.toDateString());

        // Destination time is time reply received by client (t4)
        long destTime = info.getReturnTime();
        TimeStamp destNtpTime = TimeStamp.getNtpTime(destTime);
        LogMe.d(TAG, " Destination Timestamp:\t" + destNtpTime + "  " + destNtpTime.toDateString());

        info.computeDetails();
        offsetValue = info.getOffset();
        Long delayValue = info.getDelay();

        String delay = (delayValue == null) ? "N/A" : delayValue.toString();
        String offset = (offsetValue == null) ? "N/A" : offsetValue.toString();
        LogMe.d(TAG, " Roundtrip delay(ms)=" + delay);
        LogMe.d(TAG, " Clock offset(ms)=" + offset);// offset in ms

        if(offsetValue == null) {
            return "ERROR";
        }

        setClockOffset(offsetValue);
        return String.valueOf(offsetValue.toString());
    }

}
